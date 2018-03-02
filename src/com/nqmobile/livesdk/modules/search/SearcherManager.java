package com.nqmobile.livesdk.modules.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.nqmobile.livesdk.commons.concurrent.PriorityExecutor;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.modules.search.network.HotwordSearchListEvent;
import com.nqmobile.livesdk.modules.search.network.SearchServiceFactory;
import com.nqmobile.livesdk.utils.GpUtils;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.Tools;

public class SearcherManager extends AbsManager {
	private static final ILogger NqLog = LoggerFactory
			.getLogger(SearcherModule.MODULE_NAME);

	private Context mContext;
	private static SearcherManager mInstance;
	public boolean WP_APPLYING = false;
	private static final String FILE_cacheHotWords = "cacheHotWords";

	// ===========================================================
	// Constructors
	// ===========================================================
	private SearcherManager(Context context) {
		super();
		this.mContext = context.getApplicationContext();
		init();
	}

	public synchronized static SearcherManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SearcherManager(context);
		}
		return mInstance;
	}

	@Override
	public void init() {
		EventBus.getDefault().register(this);
	}

	public String getSearchBoxName() {
		return SearcherPreference.getInstance().getSearchBoxName();
	}

	public long getShowInterval() {
		return SearcherPreference.getInstance().getShowInterval();
	}

	public int getSearchBoxType() {
		return SearcherPreference.getInstance().getSearchBoxType();
	}

	/**
	 * 搜索url，包含通配符{KEYWORD}]
	 * 
	 * @return
	 */
	private String getUrl(String keyUrl) {
		String bingUrl = "http://www.bing.com/search?q=";
		String baiduUrl = "http://m.baidu.com/s?from=1010801a&word=";
		String url = SearcherPreference.getInstance().getStringValue(keyUrl);
		String defaultUrl = ClientInfo.isUseBingSearchUrl() ? bingUrl
				: baiduUrl;
		NqLog.d(getClass().getSimpleName() + " getUrl url:" + url + " keyUrl:"
				+ keyUrl + " defaultUrl:" + defaultUrl);
		return Tools.isEmpty(url) ? defaultUrl : url.replace("%3F", "?")
				.replace("|", "&");
	}

	/**
	 * 关键词搜索
	 * 
	 * @param keyWord
	 */
	public void gotoViewDetail(String keyWord) {
		jumpViewDetail(keyWord, SearcherPreference.KEY_SEARCH_URL_TG);
	}

	/**
	 * 热词搜索
	 * 
	 * @param hotword
	 */
	public void gotoHotwordDetail(String hotword) {
		jumpViewDetail(hotword, SearcherPreference.KEY_SEARCH_URL_RC);
	}

	private void jumpViewDetail(String keyWord, String keyUrl) {
		String gurl = getUrl(keyUrl);
		keyWord = Tools.isEmpty(keyWord) ? "" : keyWord;
		String key = "{KEYWORD}";
		String url = gurl.contains(key) ? gurl.replace(key, keyWord)
				: (gurl + keyWord);
		NqLog.d(getClass().getSimpleName() + " jumpViewDetail url:" + url
				+ " gurl:" + gurl);
		GpUtils.viewDetail(mContext, url);
	}

	public void getHotwords(SearchHotwordsCallback callback) {
		long now = SystemFacadeFactory.getSystem().currentTimeMillis();
		SearcherPreference helper = SearcherPreference.getInstance();
		long lastUpdateTime = helper.getLastUpdateTime();
		long frequency_wifi = helper.getWifiFrequency()
				* DateUtils.MINUTE_IN_MILLIS;
		long frequency_3g = helper.get3GFrequency()
				* DateUtils.MINUTE_IN_MILLIS;
		NqLog.d("getHotwords: now=" + now + ", lastUpdateTime="
				+ lastUpdateTime + ", frequency_wifi=" + frequency_wifi
				+ ", frequency_3g=" + frequency_3g);
		if (NetworkUtils.isWifi(mContext)) {
			if (Math.abs(now - lastUpdateTime) >= (frequency_wifi == 0L ? DateUtils.HOUR_IN_MILLIS
					: frequency_wifi)) {
				getHotWords(callback);
			} else {
				getCachesWords(callback);
			}
		} else {
			if (Math.abs(now - lastUpdateTime) >= (frequency_3g == 0L ? DateUtils.HOUR_IN_MILLIS
					: frequency_3g)) {
				getHotWords(callback);
			} else {
				getCachesWords(callback);
			}
		}
	}

	private void getCachesWords(final SearchHotwordsCallback callback) {
		final File cacheWordsFile = cacheFile();
		if(!cacheWordsFile.exists()){
			NqLog.w("try read cached hotWords, but file not exists!");
			getHotWords(callback);
			return;
		}
		PriorityExecutor.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
				ObjectInputStream oin = null;
				NqLog.d("try read cached hotWords");
				try {
					oin = new ObjectInputStream(new FileInputStream(
							cacheWordsFile));
					String[] hotwords = (String[]) oin.readObject();
					callback.onGetHotwords(hotwords);
				} catch (Exception e) {
					NqLog.e("fail to read cached hotWords", e);
					callback.onErr();
				} finally {
					if (oin != null) {
						try {
							oin.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}

	private void getHotWords(Object tag) {
		NqLog.d("getHotwords: tag = " + tag);
		SearchServiceFactory.getService().getSearchHotWordList(tag);
	}

	private File cacheFile() {
		File cacheWordsFile = new File(mContext.getCacheDir(),
				FILE_cacheHotWords);
		return cacheWordsFile;
	}

	public void onEvent(HotwordSearchListEvent wordsEvt) {
		final String[] words = wordsEvt.getHotwords();
		Object tag = wordsEvt.getTag();
		if (tag != null && tag instanceof SearchHotwordsCallback) {
			SearcherPreference helper = SearcherPreference.getInstance();
			// 先将推广url和热词url存入xml
			if(!TextUtils.isEmpty(wordsEvt.getSoUrl())){
				helper.setTagUrl(wordsEvt.getSoUrl());
			}
			if(!TextUtils.isEmpty(wordsEvt.getHotwordUrl())){
				helper.setRCUrl(wordsEvt.getHotwordUrl());
			}
			// 然后调用回调接口展示热词
			SearchHotwordsCallback callback = (SearchHotwordsCallback) tag;
			if (wordsEvt.isSuccess()) {
				callback.onGetHotwords(words);
				PriorityExecutor.getExecutor().submit(new Runnable() {
					@Override
					public void run() {
						// save cache
						ObjectOutputStream os = null;
						try {
							os = new ObjectOutputStream(new FileOutputStream(
									cacheFile()));
							os.writeObject(words);
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (os != null) {
								try {
									os.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				});
			} else {
				callback.onErr();
			}
		}
	}
}
