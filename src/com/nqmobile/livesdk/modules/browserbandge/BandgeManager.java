package com.nqmobile.livesdk.modules.browserbandge;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.modules.browserbandge.model.Bandge;
import com.nqmobile.livesdk.modules.browserbandge.network.BrowserBandgeServiceFactory;
import com.nqmobile.livesdk.modules.browserbandge.network.GetBrowserBandgeProtocol.GetBrowserBandgeFailedEvent;
import com.nqmobile.livesdk.modules.browserbandge.network.GetBrowserBandgeProtocol.GetBrowserBandgeSuccessEvent;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.StringUtil;

public class BandgeManager extends AbsManager {
	private static final ILogger NqLog = LoggerFactory.getLogger(BrowserBandgeModule.MODULE_NAME);
	
	private static BandgeManager sInstance;
	
	private Context mContext;
	private BrowserBandgePreference mPreference;
	/** true 为常驻 false 一次 */
	private boolean mIsOverride = true;

	/** 浏览器角标广告ACTION */
	public static final String KEY_BROADCAST = "com.lqsoft.launcher.action.BANDGE_LAUNCHER";
	/** 在什么上面显示角标 type: 0 普通程序ICON, 1 快捷方式, 2 文件夹, 3 widget, 4 普通程序+快捷 */
	public static final String KEY_TYPE = "type";
	/** 在程序ICON/快捷上显示角标 */
	public static final String KEY_PACKAGE = "package";
	/** 在文件夹上显示角标[-1000是游戏文件夹] */
	public static final String KEY_FOLDER_ID = "folder_id";
	/** 在widget上显示角标 */
	public static final String KEY_CLASS = "class";
	/** 在快捷方式上显示角标 */
	public static final String KEY_ACTION = "action";
	/** 是否需要将之前的角标清除，默认false。 */
	public static final String KEY_CLEAR_ALL = "clear_all";
	/** 角标显示数字5 */
	public static final String KEY_NUM = "num";
	/** 角标点击时是否覆盖原Intent，执行角标行为。 true时不执行原intent, 反之执行原intent。默认false */
	public static final String KEY_OVERIDE = "override";
	/**
	 * 在什么位置上显示 position：0 所有出现的位置, 1只在第一个widget或快捷上示, 2 只在主屏上显示 3 只在dock中显示， 4
	 * 只在应用列表中显示 ，默认0
	 */
	public static final String KEY_POSITION = "position";
	/** 角标icon */
	public static final String KEY_BANDGE_ICON = "bandge_icon";
	/** 角标iconID */
	public static final String KEY_BANDGE_ICON_ID = "bandge_icon_id";
	/** 广告url */
	public static final String KEY_URL = "url";
	/** 常驻广告url */
	public static final String KEY_URL_LONG = "long_url";
	/** 服务端下发角标资源id */
	public static final String KEY_RES_ID = "res_id";
	/** 服务端下发角标资源常驻id */
	public static final String KEY_RES_ID_LONG = "res_id_long";
	/** 默认浏览器包名 */
	public static final String BROWSER_PACKAGE = "com.android.browser";
	/** 默认浏览器主activity名 */
	public static final String BROWSER_CLASS = "com.android.browser.BrowserActivity";
	
	public static final String KEY_BANDGE_TYPE = "bandge_type";

	public BandgeManager(Context context) {
		super();
		this.mContext = context;
		mPreference = BrowserBandgePreference.getInstance();
	}

	public synchronized static BandgeManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new BandgeManager(context);
		}
		return sInstance;
	}

	/**
	 * 通知launcher刷新浏览器角标
	 * 
	 * @return
	 */
	public void updateBandge(Bandge bandge, int num) {
		NqLog.i("updateBandge");
		if (bandge == null) {
			return;
		}
		if (num==0) {
			mPreference.setOverride(false);
		}
		sendBandgeIntent(bandge, num);
	}

	private void sendBandgeIntent(Bandge bandge, int num) {
		NqLog.i("sendBandgeIntent");
		if (bandge == null) {
			return;
		}
		if(StringUtil.isNullOrEmpty(bandge.getStrId()) || StringUtil.isNullOrEmpty(bandge.getJumpUrl())){
			NqLog.i("invalid bandge : " + bandge.toString());
			return;
		}
		sendShowBroadcast(bandge,num);
	}
	
	private void sendShowBroadcast(Bandge bandge, int num){
		NqLog.i("sendShowBroadcast");
		Intent bandgeIntent = new Intent(KEY_BROADCAST);
		bandgeIntent.putExtra(KEY_TYPE, 4);
		bandgeIntent.putExtra(KEY_PACKAGE, BROWSER_PACKAGE);
		bandgeIntent.putExtra(KEY_FOLDER_ID, 0);
		bandgeIntent.putExtra(KEY_CLASS, BROWSER_CLASS);
		bandgeIntent.putExtra(KEY_ACTION, "");
		bandgeIntent.putExtra(KEY_CLEAR_ALL, false);
		bandgeIntent.putExtra(KEY_NUM, num);	
		setIsOverride(bandge);
		NqLog.i("1 bandge.getType() " + bandge.getType());
		bandgeIntent.putExtra(KEY_URL_LONG, mPreference.getJumpUrl());
		bandgeIntent.putExtra(KEY_URL, bandge.getJumpUrl());
		bandgeIntent.putExtra(KEY_RES_ID, bandge.getStrId());
		bandgeIntent.putExtra(KEY_RES_ID_LONG,mPreference.getResourceIdLong());
		NqLog.i("1BandgeManager sendBandgeIntent isOverRide:"+mIsOverride);
		bandgeIntent.putExtra(KEY_OVERIDE, mIsOverride);
		bandgeIntent.putExtra(KEY_BANDGE_TYPE, bandge.getType());
		bandgeIntent.putExtra(KEY_POSITION, 0);
		bandgeIntent.putExtra(KEY_RES_ID, bandge.getStrId());
		mContext.sendBroadcast(bandgeIntent);
	}

	private void setIsOverride(Bandge bandge) {
		boolean is = mPreference.isOverride();
		if (is) {
			mIsOverride = is;
		}else{
			if (bandge.getType() == Bandge.TYPE_ONE_TIME) {
				mIsOverride = false;
			} else if(bandge.getType() == Bandge.TYPE_LONG){
				mIsOverride = true;
			}			
		}
	}
	/**
	 * 
	 * @param intent
	 * @param action
	 * @return
	 */
	public boolean onReceiveBandgeIntent(Context activityContext, Intent intent, int action) {
		NqLog.i("BandgeManager  onReceiveBandgeIntent:" + intent.getExtras().toString());
		boolean result = false;
		switch (action) {
		case 0:
			result = processBandgeShow(intent);
			break;
		case 1:
			result = processBandgeClick(activityContext, intent);
			break;
		default:
			break;
		}
		return result;
	}

	/** 处理角标点击,调起行为 */
	private boolean processBandgeClick(Context activityContext, Intent intent) {
		NqLog.i("handy processBandgeClick");
		DialogUtils dialogUtils = new DialogUtils(activityContext, intent);
		boolean isShow = dialogUtils.isShowDialog();
		NqLog.i("handy processBandgeClick isShow:" + isShow);
		
		if(isShow){
			dialogUtils.showConsumeDialog();
		} else {
			boolean clickedOk = mPreference.getRecordDialogOk();
			String url = intent.getStringExtra(BandgeManager.KEY_URL);
			if (!TextUtils.isEmpty(url)){//有新角标，可能是新的单次角标或者新的常驻角标
				processBrower(intent, true);
			} else if (clickedOk){//用户已经确认启用
				processBrower(intent, true);
			} else {
				intent.putExtra(BandgeManager.KEY_URL, "");
				intent.putExtra(BandgeManager.KEY_URL_LONG, "");
				processBrower(intent, false);
			}
		}
		return true;
	}

	public void processBrower(Intent intent, boolean isFilterUrl) {
		String resId = getResID(intent);
		NqLog.i("resId click  processBrower");
		if(!StringUtil.isNullOrEmpty(resId)){	
			if (isFilterUrl) {
			NqLog.i("click resId "+ resId);
				
				StatManager.getInstance().onAction(
						StatManager.TYPE_STORE_ACTION, BrowseBandgeActionConstants.ACTION_LOG_2202, resId, resId.startsWith("AD_")?1:0, null);
			}
		}

		launchBrowser(intent, isFilterUrl);
		sendClickBroadcast(intent);
	}
	
	private void sendClickBroadcast(Intent intent){
		NqLog.i("sendClickBroadcast");
		intent.setAction(KEY_BROADCAST);
		intent.putExtra(KEY_NUM, 0);
		intent.putExtra(KEY_CLEAR_ALL, false);
		intent.putExtra(KEY_RES_ID, "");
		intent.putExtra(KEY_RES_ID_LONG, mPreference.getResourceIdLong());
		intent.putExtra(KEY_URL_LONG, mPreference.getJumpUrl());
		intent.putExtra(KEY_OVERIDE, mPreference.isOverride());
		intent.putExtra(KEY_URL, "");
		mContext.sendBroadcast(intent);
		NqLog.i("3 PreferenceDataHelper  KEY_OVERIDE " +mPreference.isOverride());
	}

	private void launchBrowser(Intent i, boolean isFilterUrl) {
		String url = jumpUrl(i);
		Intent intent= new Intent();
		if (isFilterUrl && StringUtil.isNullOrEmpty(url)) {
			return;	
		}
		if(!StringUtil.isNullOrEmpty(url)){
			Uri content_url = Uri.parse(url);
			intent.setData(content_url);
		}
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName(i.getStringExtra(KEY_PACKAGE), i.getStringExtra(KEY_CLASS));
		mContext.startActivity(intent);
	}
	
	private String getResID(Intent i){
		String resID = null;
		NqLog.i("getResID" + i.getStringExtra(KEY_RES_ID));
		NqLog.i("getResID long resID" + i.getStringExtra(KEY_RES_ID_LONG));	
		if (StringUtil.isNullOrEmpty(i.getStringExtra(KEY_RES_ID))) {
			resID = i.getStringExtra(KEY_RES_ID_LONG);
		} else {
			resID = i.getStringExtra(KEY_RES_ID);
		}
		NqLog.i("return getResID 2 "+ resID);
		return resID;
	}
	
	private String jumpUrl(Intent i){
		String url = null;
		NqLog.i("2 launchBrowser url" + i.getStringExtra(KEY_URL));
		NqLog.i("2 launchBrowser long url" + i.getStringExtra(KEY_URL_LONG));	
		if (StringUtil.isNullOrEmpty(i.getStringExtra(KEY_URL))) {
			url = i.getStringExtra(KEY_URL_LONG);
		} else {
			url = i.getStringExtra(KEY_URL);
		}
		NqLog.i("handy url 2 "+ url);
		return url;
	}

	/** 处理角标展示,做不重复的统计 */
	private boolean processBandgeShow(Intent intent) {
		String resId = intent.getStringExtra(KEY_RES_ID);
		String url = intent.getStringExtra(KEY_URL);
		if (!StringUtil.isNullOrEmpty(resId)) {	
			NqLog.i("角标展示");
			NqLog.i("resId = " + resId);
			StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
					BrowseBandgeActionConstants.ACTION_LOG_2201, resId, 0, null);
		}
		if (StringUtil.isNullOrEmpty(url) || StringUtil.isNullOrEmpty(resId)) {
			NqLog.d("processBandgeShow intent is invalid, resId is " + resId
					+ ",url is " + url);
			return false;
		}
		return false;
	}



	@Override
	public void init() {
		EventBus.getDefault().register(this);
	}

	public void getBandge() {
		BrowserBandgeServiceFactory.getService().getBandge(null);
	}

	public void onEvent(GetBrowserBandgeSuccessEvent event) {
		NqLog.i("GetBrowserBandgeSuccessEvent");
		Bandge b = event.getBandge();
		if (b == null){
			return;
		}
		
		if (b.getType() == Bandge.TYPE_LONG){
			saveLongUrl(b);
		}
		updateBandge(b, 1);
		NqLog.i(b.toString());
	}

	public void onEvent(GetBrowserBandgeFailedEvent event) {
		NqLog.i("GetBrowserBandgeFailedEvent");
	}
	
	private void saveLongUrl(Bandge b) {
		mPreference.setResourceIdLong(b.getStrId());
		mPreference.setResourceId(b.getStrId());
		mPreference.setJumpUrl(b.getJumpUrl());
		mPreference.setOverride(true);
	}
}
