package com.nqmobile.livesdk.modules.stat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.format.DateUtils;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.commons.info.ProcessInfo;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.receiver.ConnectivityChangeEvent;
import com.nqmobile.livesdk.commons.receiver.PackageAddedEvent;
import com.nqmobile.livesdk.commons.receiver.PackageInstallLogEvent;
import com.nqmobile.livesdk.commons.receiver.PackageRemoveEvent;
import com.nqmobile.livesdk.commons.service.PeriodCheckEvent;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.modules.app.AppActionConstants;
import com.nqmobile.livesdk.modules.stat.network.StatServiceFactory;
import com.nqmobile.livesdk.modules.stat.network.UploadAdStatProtocol;
import com.nqmobile.livesdk.modules.stat.network.UploadLogProtocol;
import com.nqmobile.livesdk.modules.stat.table.AdStatTable;
import com.nqmobile.livesdk.modules.stat.table.NewPackageTable;
import com.nqmobile.livesdk.modules.stat.table.StoreStatTable;
import com.nqmobile.livesdk.utils.NetworkUtils;

public class StatManager extends AbsManager{
	private static final ILogger NqLog = LoggerFactory.getLogger(StatModule.MODULE_NAME);

	private static StatManager mInstance;

	private Context mContext;

	private static final int MAX_LINE = 300;

	private static final int MSG_UPLOAD_AD_STAT = 0;
	private static final int MSG_UPLOAD_NORMAL_STAT = 1;

	public static final int TYPE_STORE_ACTION = 0;
	public static final int TYPE_AD_STAT = 1;

	public static final int ACTION_DISPLAY = 0;
	public static final int ACTION_CLICK = 1;
	public static final int ACTION_DOWNLOAD = 2;
	public static final int ACTION_FIRST_LAUNCH = 5;
	public static final int ACTION_INSTALL = 6;

	private volatile boolean mIsUploadingLog;

	private IGAnalytics mGa;

	private Handler mHandler;
	private StatPreference mPreference;
	
	private StatManager() {
		mContext = ApplicationContext.getContext();
		HandlerThread mThread = new HandlerThread("StatManager");
		mThread.start();
		mHandler = new RequestHandler(mThread.getLooper());
		if (ProcessInfo.isLauncherProcess(mContext)){
			EventBus.getDefault().register(this);
		}
		
		mPreference = StatPreference.getInstance();

		if(ClientInfo.isGP()){
			mGa = GAnalyticsFactory.getIGAnalytics(mContext);
		}
	}

	@Override
	public void init() {
	}

	private class RequestHandler extends Handler{

		public RequestHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
					case MSG_UPLOAD_AD_STAT:
						uploadAdStat((StatEvent) msg.obj);
						break;
					case MSG_UPLOAD_NORMAL_STAT:
						processNormalStat((StatEvent) msg.obj);
						break;
				}
			} catch (Exception e) {
				NqLog.e(e);
			}
		}
	}

	public synchronized static StatManager getInstance() {
		if(mInstance == null) {
			mInstance = new StatManager();
		}

		return mInstance;
	}

	public void onAction(StatEvent event) {
		if(event.desc.equals("1506")){
			NqLog.i("onAction event "+event);
			Message msg = mHandler.obtainMessage();
			msg.arg1 = event.type;
			msg.obj = event;
			msg.what = event.type == TYPE_AD_STAT ? MSG_UPLOAD_AD_STAT : MSG_UPLOAD_NORMAL_STAT;
			mHandler.sendMessage(msg);
			return;
		}

		if (mPreference.isUploadLogEnabled()) {
			NqLog.i("onAction event "+event);
			Message msg = mHandler.obtainMessage();
			msg.arg1 = event.type;
			msg.obj = event;
			msg.what = event.type == TYPE_AD_STAT ? MSG_UPLOAD_AD_STAT : MSG_UPLOAD_NORMAL_STAT;
			mHandler.sendMessage(msg);
		}
	}

	public void sendGaEvent(StatEvent event){
		if(ClientInfo.isGP() && mGa != null){
			mGa.sendGaEvent(event.desc,event.resourceId,event.scene,event.actionType);
		}
	}

	private void uploadAdStat(StatEvent event) {
		UploadAdStatTag tag = new UploadAdStatTag();
		tag.event = event;
		NqLog.i("uploadAdStat event=" + event);
		StatServiceFactory.getService().uploadAdStat(tag);
	}

	private void processNormalStat(StatEvent event) {
		ContentValues Storevalue = new ContentValues();
		Storevalue.put(StoreStatTable.STORE_STAT_DESC, event.desc);
		Storevalue.put(StoreStatTable.STORE_STAT_RESOURCEID, event.resourceId);
		Storevalue.put(StoreStatTable.STORE_STAT_SCNE, event.scene);
		Storevalue.put(StoreStatTable.STORE_STAT_TIME, System.currentTimeMillis());
		mContext.getContentResolver().insert(StoreStatTable.TABLE_URI, Storevalue);

		if (ProcessInfo.isLauncherProcess(mContext)){//只在桌面进程，实时检查是否满足MAX_LINE，需要wifi下即时上传
			checkStatLine();
		}
	}

	private void processAdStat(StatEvent event){
		NqLog.i("processAdStat event.desc="+event.desc);
		ContentValues Storevalue = new ContentValues();
		Storevalue.put(AdStatTable.STORE_STAT_DESC, event.desc);
		Storevalue.put(AdStatTable.STORE_STAT_RESOURCEID, event.resourceId);
		Storevalue.put(AdStatTable.STORE_STAT_SCNE, event.scene);
		Storevalue.put(AdStatTable.STORE_STAT_ACTION, event.actionType);
		mContext.getContentResolver().insert(AdStatTable.TABLE_URI, Storevalue);
	}

	private void checkStatLine() {
		int count = 0;
		Cursor c = mContext.getContentResolver().query(StoreStatTable.TABLE_URI, null, null, null, null);
		if(c != null) {
			count = c.getCount();
			c.close();
		}

		if(count >= MAX_LINE && NetworkUtils.isWifi(mContext)) {
			if(!mIsUploadingLog){
				NqLog.i("startUploadLog");
				mIsUploadingLog = true;
				StatServiceFactory.getService().uploadLog();
			}
		}
	}

	public void onAction(int type,String desc, String resourceId, int actionType, String scene) {
		StatEvent event = new StatEvent();
		event.type = type;
		event.desc = desc;
		event.resourceId = resourceId;
		if(event.resourceId != null && event.resourceId.startsWith("AD_")){
			event.type = StatManager.TYPE_AD_STAT;
		}
		event.actionType = actionType;
		event.scene = scene;
		onAction(event);
	}

	public void onEvent(PeriodCheckEvent event){
		if(!StatPreference.getInstance().getUploadPackageFinish() && mPreference.isUploadPackageEnabled()){
			StatServiceFactory.getService().uploadAllPackage();
		}

		if (!mPreference.isUploadLogEnabled()) {
			return;
		}
		long lastUploadLogtime = StatPreference.getInstance().getLastUploadLog();
		if(Math.abs(SystemFacadeFactory.getSystem().currentTimeMillis() - lastUploadLogtime)> DateUtils.DAY_IN_MILLIS){
			StatServiceFactory.getService().uploadLog();
			StatServiceFactory.getService().uploadCrashLog();
		}
	}

	public void onEvent(ConnectivityChangeEvent e){
		NqLog.i("onEvent ConnectivityChangeEvent");
		if(NetworkUtils.isConnected(mContext)){
			UploadAdStatTag tag = new UploadAdStatTag();
			tag.uploadFromDb = true;
			StatServiceFactory.getService().uploadAdStat(tag);
		}
	}

	public void onEvent(PackageAddedEvent event){
		NqLog.i("onEvent PackageAddedEvent packageName="+event.getPackageName());
		ContentValues value = new ContentValues();
		value.put(NewPackageTable.NEW_PACKAGE_PACKAGENAME,event.getPackageName());
		mContext.getContentResolver().insert(NewPackageTable.TABLE_URI,value);
		
		if (mPreference.isUploadPackageEnabled()) {
			StatServiceFactory.getService().uploadNewPackage();
		}
	}

	public void onEvent(PackageRemoveEvent event){
		onAction(TYPE_STORE_ACTION,ActionConstants.ACTION_LOG_2002,"",0,event.packageName);
	}

	public void onEvent(UploadAdStatProtocol.UploadAdStatSuccessEvent event){
		NqLog.i("onUploadAdStatSucc!");
	}

	public void onEvent(UploadAdStatProtocol.UploadAdStatFailEvent event){
		StatEvent e = ((UploadAdStatTag)event.getTag()).event;
		if(e != null){
			processAdStat(e);
		}
	}

	public void onEvent(UploadLogProtocol.UploadLogSuccessEvent event){
		mIsUploadingLog = false;
	}

	public void onEvent(UploadLogProtocol.UploadLogFailEvent event){
		mIsUploadingLog = false;
	}

	/**
	 * 应用安装成功发送的日记  1505
	 * @param event
	 */
	public void onEvent(PackageInstallLogEvent event){
		onAction(TYPE_AD_STAT, AppActionConstants.ACTION_LOG_1505, event.getResId(), ACTION_INSTALL, event.getPackageName());
	}
}
