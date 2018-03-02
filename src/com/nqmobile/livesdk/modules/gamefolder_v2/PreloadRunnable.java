package com.nqmobile.livesdk.modules.gamefolder_v2;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;

import com.nqmobile.livesdk.commons.image.ImageListener;
import com.nqmobile.livesdk.commons.image.ImageLoader;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.utils.FileUtil;

public class PreloadRunnable implements Runnable {
	private static final ILogger NqLog = LoggerFactory.getLogger(GameFolderV2Module.MODULE_NAME);

	static final String TAG = "PreloadRunnable";
	public boolean B_LOAD_ICON = true;
	public boolean B_LOAD_PREVIEW = true;
	public boolean B_LOAD_AUDIO = true;
	private Context context;
	private PreloadListener mListener;
	private int mLoadSize;
	private Object mLock = new Object();
	private int mCallBackTime;
	private List<String> mSuccLoadSet = new ArrayList<String>();// 下载成功图片或音频Url集合
	private App mApp;

	public PreloadRunnable(Context context, App mApp, PreloadListener preloadListener) {
		super();
		this.context = context;
		this.mApp = mApp;
		this.mListener = preloadListener;
	}

	@Override
	public void run() {
		if (context == null || mApp == null || mListener == null)
			return;
		if (context == null || mApp == null) {
			if (mListener != null)
				mListener.onErr();
			return;
		}
		// TODO Auto-generated method stub
		mCallBackTime = 0;
		mSuccLoadSet.clear();
		HashMap<String, String> imageMap = new HashMap<String, String>();
		boolean isGpResource = mApp.isGpApp();
		if (B_LOAD_ICON) {
			// 获取icon
			imageMap.put(mApp.getStrIconUrl(), mApp.getStrIconPath());
		}
		if(!isGpResource && B_LOAD_PREVIEW) {
			// 获取预览图
			if (mApp.getArrPreviewUrl() != null
					&& !mApp.getArrPreviewUrl().isEmpty()) {
				Iterator it1 = mApp.getArrPreviewUrl().iterator();
				Iterator it2 = mApp.getArrPreviewPath().iterator();
				while (it1.hasNext()) {
					imageMap.put(it1.next().toString(), it2.next().toString());
				}
			}
		}
		HashMap<String, String> audioMap = new HashMap<String, String>();
		if (!isGpResource && B_LOAD_AUDIO) {
			// 获取音频
			if (!TextUtils.isEmpty(mApp.getStrAudioUrl())) {
				audioMap.put(mApp.getStrAudioUrl(), mApp.getStrAudioPath());
			}
		}

		mLoadSize = imageMap.size() + audioMap.size();
		Iterator iterImg = imageMap.entrySet().iterator();
		while (iterImg.hasNext()) {
			Map.Entry entry = (Map.Entry) iterImg.next();
			String url = entry.getKey().toString();
			final String path = entry.getValue().toString();

			if (path != null) {
				File f = new File(path);
				if (f.exists()) {
					mSuccLoadSet.add(url);
					checkStatus();
				} else {
					ImageLoader.getInstance(context).getImage(path, url,
							new ImageListener() {
								@Override
								public void onErr() {
									checkStatus();
								}

								@Override
								public void getImageSucc(String url,
										BitmapDrawable drawable) {
									if (drawable != null) {
										mSuccLoadSet.add(url);
										FileUtil.writeBmpToFile(drawable
												.getBitmap(), new File(path));
									}

									checkStatus();
								}
							});
				}
			}

		}

		Iterator iterAudio = audioMap.entrySet().iterator();
		while (iterAudio.hasNext()) {
			Map.Entry entry = (Map.Entry) iterAudio.next();
			String url = entry.getKey().toString();
			final String path = entry.getValue().toString();

			if (path != null) {
				File f = new File(path);
				if (f.exists()) {
					mSuccLoadSet.add(url);
					checkStatus();
				} else {
					AudioManager.getInstance(context).getAudio(path, url,
							new AudioListener() {
								@Override
								public void onErr() {
									checkStatus();
								}

								@Override
								public void getAudioSucc(String url,
										String path) {
									// TODO Auto-generated method stub
									NqLog.i(mApp.getStrName() + 
											"preLoadAudio getAudioSucc url="
													+ url + " path= " + path);
									mSuccLoadSet.add(url);

									checkStatus();
								}
							});
				}
			}

		}
	}

	/** 检查预取状态 */
	protected void checkStatus() {
		// TODO Auto-generated method stub
		synchronized (mLock) {
			mCallBackTime++;
			checkCallBackTime();
		}
	}

	/** 游戏文件夹缓存图片获取成功后，启动创建游戏文件夹 */
	private void checkCallBackTime() {
		if (mCallBackTime == mLoadSize) {
			if (mSuccLoadSet.size() == mLoadSize) {
				// updateDb();
				// NqLog.i(TAG + ", after all recommend game fetched");

				// 是不是应该，如果未创建才创建文件夹？？
				// GameManager.getInstance(context).sendGameFolderBroadcast(null);
				if (mListener != null)
					mListener.onPreloadSucc(mApp);
			} else {
				// 图片下载失败 不更新资源列表
				if (mListener != null)
					mListener.onErr();
			}
		}
	}

	public boolean isB_LOAD_ICON() {
		return B_LOAD_ICON;
	}

	public void setB_LOAD_ICON(boolean b_LOAD_ICON) {
		B_LOAD_ICON = b_LOAD_ICON;
	}

	public boolean isB_LOAD_PREVIEW() {
		return B_LOAD_PREVIEW;
	}

	public void setB_LOAD_PREVIEW(boolean b_LOAD_PREVIEW) {
		B_LOAD_PREVIEW = b_LOAD_PREVIEW;
	}

	public boolean isB_LOAD_AUDIO() {
		return B_LOAD_AUDIO;
	}

	public void setB_LOAD_AUDIO(boolean b_LOAD_AUDIO) {
		B_LOAD_AUDIO = b_LOAD_AUDIO;
	}

}
