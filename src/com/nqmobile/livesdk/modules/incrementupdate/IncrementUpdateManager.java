package com.nqmobile.livesdk.modules.incrementupdate;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.nq.interfaces.launcher.TNewVersionResp;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.mydownloadmanager.MyDownloadManager;
import com.nqmobile.livesdk.commons.net.UpdateListener;
import com.nqmobile.livesdk.commons.receiver.DownloadCompleteEvent;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.incrementupdate.model.NewVersion;
import com.nqmobile.livesdk.modules.incrementupdate.network.GetNewVersionProtocol.GetNewVersionFailedEvent;
import com.nqmobile.livesdk.modules.incrementupdate.network.GetNewVersionProtocol.GetNewVersionSuccessEvent;
import com.nqmobile.livesdk.modules.incrementupdate.network.GetNewVersionServiceFactory;
import com.nqmobile.livesdk.modules.incrementupdate.table.NewVersionTable;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.GpUtils;
import com.nqmobile.livesdk.utils.IncrementUpdatePatcher;
import com.nqmobile.livesdk.utils.IncrementUpdateUtils;
import com.nqmobile.livesdk.utils.MD5;
import com.nqmobile.livesdk.utils.MResource;

public class IncrementUpdateManager extends AbsManager {
	private static final ILogger NqLog = LoggerFactory.getLogger(IncrementUpdateModule.MODULE_NAME);

	private static IncrementUpdateManager sIncUpdateMgr;
	private Context mContext;
	private MyDownloadManager mDownloader;
	private PackageManager mPkgManager;
	private IncrementUpdatePreference mHelper;
	private NotificationManager mNotificationMgr;
	
	private boolean mHasNewVersion = false;
	private String mNewVersionCode;     //新包版本号, 例如 1.1.02.00
	private int mUpgradeType;           //升级的方式  0 只做全量更新，1增量优先
	private int mAutoDownload;          //WIFI下是否自动下载更新包： 0 自动下载, 1 不自动下载

	private int mPromptType;            //更新提示方式选择：0 桌面弹框 1 系统通知栏
	private String mPromptTitle;        //更新提示标题
	private String mPromptContent;      //更新提示内容
	private String mPromptStartTime;    //更新提示时间段的开始时间 08：30
	private String mPromptEndTime;      //更新提示时间段的结束时间
	private int mPromptInterval;        //更新提示时间段的时间间隔 （天）
	private int mPromptNetwork;         //更新提示展示场景：0 有网络   1 WIFI

	private int mOldFileSize;           //旧版本全量包的大小（byte）
	private String mOldFileMd5;         //旧版本全量包的md5

	private String mPatchAlgorithm;     //生成patch包的算法版本号
	private int mPatchSize;             //patch包大小（byte）
	private String mPatchMd5;           //patch包md5
	private String mPatchUrl;           //patch包下载地址 

	private int mNewFileSize;           //新版本全量包的大小（byte）
	private String mNewFileMd5;         //新版本全量包的md5
	private String mNewFileUrl;         //新版本全量包下载url
	
	private static int NO_NETWORK = -1; //无网络
	private static int NETWORK_WIFI = 1;//WIFI
	private static int NETWORK_MOBILE = 0;//MOBILE

	private static final int NEWPACK_FAILED = 1;
	private static final int NEWPACK_SUCC = 2;
	
	private String mOriginalApkPath = null; //当前版本对应的apk安装文件路径	
	private String mPatchPath = null; //patch包的本地地址
	private String mNewApkPath = null; //和patch包合并后生成的新版本apk
	private long mDownloadPatchId = -1; //下载patch包的id
	
	private String mDownloadFullPackPath = null; //下载全量包的本地路径
	private long mDownloadFullPackId = -1; //下载全量包的id	
		
	private int mDownloadStatus; 
	public static final int DOWNLOAD_NOTSTART = -1;// -1 未有下载  
	public static final int DOWNLOAD_PATCH_START = 1;//1:增量包开始下载   
	public static final int DOWNLOAD_PATCH_START_BACK = 2;//2:增量包开始静默下载   
	public static final int DOWNLOAD_PATCH_COMPLETED = 3;// 3:增量包下载完成   
	public static final int DOWNLOAD_FULLPACKAGE_START = 10;//10:全量包开始下载  
	public static final int DOWNLOAD_FULLPACKAGE_START_BACK = 20;//20:全量包开始静默下载 
	public static final int DOWNLOAD_FULLPACKAGE_COMPLETED = 30;//30:全量包下载完成
	
	private static final int FULLPACK_UPDATE = 0;// 全量更新
	private static final int INCREMENT_UPDATE = 1;// 增量更新
	
	public static final int PROMPT_DIALOG = 0; //弹框提示	
	public static final int PROMPT_NOTIFICATION = 1; //系统通知栏提示
	
	private int mPromptTimes = 0; //目前提示次数
	private String mLastPromptTime; //前一次提示时间
	private static final int MAX_PROMPT_TIMES = 3; //最多提示次数
	private static String NEWVERSION_UPDATE = "com.nqmobile.live.newversion_update";
	private static String NEWVERSION_NOUPDATE = "com.nqmobile.live.newversion_notupdate";
	
	private Dialog mUpdateDialog = null;
	private Dialog mFullUpdateDialog = null;

	private Handler mHandler;
	private String mSDCardPath;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {     
		@Override    
		public void onReceive(Context context, Intent intent) {          
	        String action = intent.getAction();
		    NqLog.i("mReceiver received intent: " + action);
		    
		    try {
			    if (action.equals(NEWVERSION_UPDATE)) {    	
					processUpgrade(mHandler);
			        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
			        		IncrementUpdateConstants.ACTION_LOG_3002, null, 0, null);
			    } else if (action.equals(NEWVERSION_NOUPDATE)){
			        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
			        		IncrementUpdateConstants.ACTION_LOG_3003, null, 0, null);
			    }
		    } catch (Exception e) {
		    	e.printStackTrace();
		    } finally {
		    	mNotificationMgr.cancel(NEWVERSION_UPDATE.hashCode());
		    	mPromptTimes++;
		    	writeConfigData();
		    }
		}     
	};	
	
	private IncrementUpdateManager(Context context) {
		mContext = context;
		mDownloader = MyDownloadManager.getInstance(context);
		mPkgManager = mContext.getPackageManager();
		mHelper = IncrementUpdatePreference.getInstance();
		mNotificationMgr = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mSDCardPath = CommonMethod.getSDcardPath(mContext);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(NEWVERSION_UPDATE);
		filter.addAction(NEWVERSION_NOUPDATE);
	    context.registerReceiver(mReceiver, filter);
	        
	    EventBus.getDefault().register(this);
	    // 从db中读取new version信息
//	    refreshData();
	    getUpdate(null);
	}
	
	public synchronized static IncrementUpdateManager getInstance(Context context) {
		if (sIncUpdateMgr == null) {
			sIncUpdateMgr = new IncrementUpdateManager(context.getApplicationContext());
			NqLog.i("sIncUpdateMgr is null, new one");
		}else {
			NqLog.i("sIncUpdateMgr is not null, use old one");
		}
		
		return sIncUpdateMgr;
	}
	
	@Override
	public void init() {
//		EventBus.getDefault().register(this);
	}
	
	private void refreshData() {
		new Thread(new ReadLastNewVersionFromDbThread()).start();
	}
	
	private class ReadLastNewVersionFromDbThread implements Runnable {
		
		public ReadLastNewVersionFromDbThread() {

		}
		
		@Override
		public void run() {		
			readNewVersionFromDatabase();
		}
	}
	
	private void readNewVersionFromDatabase() {	
        Cursor cursor = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        try {
            cursor = contentResolver.query(NewVersionTable.TABLE_URI, null,
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                mNewVersionCode = cursor.getString(cursor.getColumnIndex(NewVersionTable.NEWVERSION_NEW_VERSION_NAME));
                mUpgradeType = cursor.getInt(cursor.getColumnIndex(NewVersionTable.NEWVERSION_UPGRADE_TYPE));
                mAutoDownload = cursor.getInt(cursor.getColumnIndex(NewVersionTable.NEWVERSION_AUTO_DOWNLOAD));
                mPromptType = cursor.getInt(cursor.getColumnIndex(NewVersionTable.NEWVERSION_PROMPT_TYPE));
                mPromptTitle = cursor.getString(cursor.getColumnIndex(NewVersionTable.NEWVERSION_PROMPT_TITLE));
                mPromptContent = cursor.getString(cursor.getColumnIndex(NewVersionTable.NEWVERSION_PROMPT_CONTENT));
                mPromptStartTime = cursor.getString(cursor.getColumnIndex(NewVersionTable.NEWVERSION_PROMPT_START_TIME));
                mPromptEndTime = cursor.getString(cursor.getColumnIndex(NewVersionTable.NEWVERSION_PROMPT_END_TIME));
                mPromptInterval = cursor.getInt(cursor.getColumnIndex(NewVersionTable.NEWVERSION_PROMPT_INTERVAL));
                mPromptNetwork = cursor.getInt(cursor.getColumnIndex(NewVersionTable.NEWVERSION_PROMPT_NETWORK)); 
                mOldFileSize = cursor.getInt(cursor.getColumnIndex(NewVersionTable.NEWVERSION_OLD_FILE_SIZE)); 
                mOldFileMd5 = cursor.getString(cursor.getColumnIndex(NewVersionTable.NEWVERSION_OLD_FILE_MD5));
                mPatchAlgorithm = cursor.getString(cursor.getColumnIndex(NewVersionTable.NEWVERSION_PATCH_ALGORITHM));
                mPatchSize = cursor.getInt(cursor.getColumnIndex(NewVersionTable.NEWVERSION_PATCH_SIZE)); 
                mPatchMd5 = cursor.getString(cursor.getColumnIndex(NewVersionTable.NEWVERSION_PATCH_MD5));
                mPatchUrl = cursor.getString(cursor.getColumnIndex(NewVersionTable.NEWVERSION_PATCH_URL));
                mNewFileSize = cursor.getInt(cursor.getColumnIndex(NewVersionTable.NEWVERSION_NEW_FILE_SIZE)); 
                mNewFileMd5 = cursor.getString(cursor.getColumnIndex(NewVersionTable.NEWVERSION_NEW_FILE_MD5));
                mNewFileUrl = cursor.getString(cursor.getColumnIndex(NewVersionTable.NEWVERSION_NEW_FILE_URL)); 
            }
        } catch (Exception e) {
            NqLog.e(e);            
        } finally {
            try {
                if (cursor != null)
                    cursor.close();
            } catch (Exception e) {
                NqLog.e(e);
            }
        }		
		
		NqLog.i("mNewVersionCode= " + mNewVersionCode);	
		NqLog.i("mUpgradeType= " + mUpgradeType);		
		NqLog.i("mAutoDownload= " + mAutoDownload);		
		NqLog.i("mPromptType= " + mPromptType);
		NqLog.i("mPromptStartTime= " + mPromptStartTime);	
		NqLog.i("mPromptEndTime= " + mPromptEndTime);		
		NqLog.i("mPromptInterval= " + mPromptInterval);		
		NqLog.i("mPromptNetwork= " + mPromptNetwork);	
		NqLog.i("mOldFileSize= " + mOldFileSize);	
		NqLog.i("mOldFileMd5= " + mOldFileMd5);		
		NqLog.i("mPatchSize= " + mPatchSize);		
		NqLog.i("mPatchMd5= " + mPatchMd5);	
		NqLog.i("mPatchUrl= " + mPatchUrl);		
		NqLog.i("mNewFileSize= " + mNewFileSize);		
		NqLog.i("mNewFileMd5= " + mNewFileMd5);		
		NqLog.i("mNewFileUrl= " + mNewFileUrl);			

		mDownloadFullPackPath = new StringBuilder().append(mSDCardPath).append(IncrementUpdateUtils.INCREASE_UPDATE_PATH).append(mNewVersionCode)
				.append(IncrementUpdateUtils.FULLPACKAGE_SUFFIX).toString();
		mPatchPath = new StringBuilder().append(mSDCardPath).append(IncrementUpdateUtils.INCREASE_UPDATE_PATH).append(mNewVersionCode)
				.append(IncrementUpdateUtils.PATCH_SUFFIX).toString();
		
		mHasNewVersion = checkNewVersion();
		resetConfigData();
		
		//gp版本的话不自动下载
		if (!ClientInfo.isGP()) {
			if (mHasNewVersion && !isPackageReady()) {
				wifiAutoDownload();
			}
		}
	}
	
	private boolean checkNewVersion() {
		if (mNewFileMd5 == null || mNewFileMd5.isEmpty()) {
			return false;
		}
		String currentMD5 = IncrementUpdateUtils.getPackageMd5(mContext);
		NqLog.i("currentMD5= " + currentMD5);
		
		if (currentMD5.equalsIgnoreCase(mNewFileMd5)) {
			return false;
		}
		
		return true;
	}
	
    private int getNetWorkConnectState() {
        int netConnect = NO_NETWORK;
        ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi != null && wifi.isConnected()) {
            netConnect = NETWORK_WIFI;
        }
        if (mobile != null && mobile.isConnected()) {
            netConnect = NETWORK_MOBILE;
        }
        return netConnect;
    }	
	
	/**
	 * wifi自动下载
	 */
	private void wifiAutoDownload() {
		if (mAutoDownload == 0 && getNetWorkConnectState() == NETWORK_WIFI) {
			//wifi下自动下载,自动下载全量包或增量包
			NqLog.i("wifiAutoDownload is coming");
			downloadPackage(mUpgradeType, true);
		}
	}
	
	
	/**
	 * 是否显示升级提示框
	 */
	public boolean isReadyToPrompt(){
		NqLog.i("isReadyToPrompt is coming");	
		NqLog.i("mHasNewVersion =" + mHasNewVersion);	
		if (!mHasNewVersion) {
			return false;
		}
		int network = getNetWorkConnectState();		
		if (network == NO_NETWORK) {
			return false;
		} 
		if (network == NETWORK_MOBILE && mPromptNetwork == 1) {
			return false;
		}
		
		readConfigData();			
		if (mPromptTimes >= MAX_PROMPT_TIMES) {
			return false;
		}
			
		//是否处于时间间隔
		if (isInPromptInterval() == false) {
			return false;
		}
		
		//当前时间是否处于提示段
		if (isInPromptPeriod() == false) {				
			return false;
		}
		
		if (isUpdateProcessing()) {			
			return false;
		}
		return true;
	}	

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void sendUpdateNotify() {		
		// click intent
		PendingIntent contentPendingIntent = getUpdatePendingIntent();
		
 		NotificationCompat.Builder nb = new NotificationCompat.Builder(mContext)
 		.setContentTitle(mPromptTitle)
 		.setContentText(convertPromptContent(mPromptContent))
 		.setAutoCancel(false)
 		.setSmallIcon(MResource.getIdByName(mContext, "drawable", "logo"))
// 		.setPriority(NotificationCompat.PRIORITY_MAX)
 		.setContentIntent(contentPendingIntent);
 		
 		Notification notification = nb.build(); 		
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.icon = (MResource.getIdByName(mContext, "drawable", "logo"));
		
		if(Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN){  
//			Intent detailIntent = new Intent();
//			contentPendingIntent = PendingIntent.getBroadcast(mContext, 0, detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			
	        // 是否升级提示文本
	        String text_noupdate;
	        String text_update;
			if (mPromptTimes < MAX_PROMPT_TIMES - 1) {
				text_noupdate = MResource.getString(mContext,"nq_update_not_update");				
			} else {
				text_noupdate = MResource.getString(mContext,"nq_update_not_reminder");
			}  
			text_update =  MResource.getString(mContext,"nq_update_immediately");		
			 					
	 		RemoteViews expandedView = buildExpendView(contentPendingIntent, text_noupdate, text_update);			
	 		notification.bigContentView = expandedView;
		}

		mNotificationMgr.notify(NEWVERSION_UPDATE.hashCode(), notification);
        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
        		IncrementUpdateConstants.ACTION_LOG_3001, null, 0, null);
	}
    	
	private RemoteViews buildExpendView(PendingIntent contentPendingIntent, String text_noupdate, String text_update) {
 		int expandedLayoutId = MResource.getIdByName(mContext, "layout", "nq_update_notification_expanded");
 		
		RemoteViews expandedView = new RemoteViews(mContext.getPackageName(), expandedLayoutId);
        int bigImageViewId = MResource.getIdByName(mContext, "id", "notification_expanded_big_image");
		expandedView.setImageViewBitmap(bigImageViewId, null);
		expandedView.setOnClickPendingIntent(bigImageViewId, contentPendingIntent);
		
		int imageView = MResource.getIdByName(mContext, "id", "update_notification_image");
		int srcId = MResource.getIdByName(mContext, "drawable", "logo");
		expandedView.setImageViewResource(imageView, srcId);
		
		int updateTitle = MResource.getIdByName(mContext, "id", "update_title");
		expandedView.setTextViewText(updateTitle, mPromptTitle);
		
		int updateContent = MResource.getIdByName(mContext, "id", "update_content");
		expandedView.setTextViewText(updateContent, convertPromptContent(mPromptContent)); 
		
		int seperatorLineViewId = MResource.getIdByName(mContext, "id", "expanded_notification_seperator_line");
		int actionsViewId = MResource.getIdByName(mContext, "id", "expanded_notification_actions");

		expandedView.setViewVisibility(seperatorLineViewId, View.VISIBLE);
		expandedView.setViewVisibility(actionsViewId, View.VISIBLE);
		
		int unlikeActionViewId = MResource.getIdByName(mContext, "id", "notification_expanded_unlike");
		int unlikeTextViewId = MResource.getIdByName(mContext, "id", "notification_expanded_unlike_textview");
		expandedView.setTextViewText(unlikeTextViewId, text_noupdate);
		expandedView.setOnClickPendingIntent(unlikeActionViewId, getNotUpdatePendingIntent());
		
		int downloadActionViewId = MResource.getIdByName(mContext, "id", "notification_expanded_download");
		int downloadTextViewId = MResource.getIdByName(mContext, "id", "notification_expanded_download_textview");
		expandedView.setTextViewText(downloadTextViewId, text_update);
		expandedView.setOnClickPendingIntent(downloadActionViewId, getUpdatePendingIntent());
		
		return expandedView;
	}
	
	private PendingIntent getUpdatePendingIntent() {
		Intent updateIntent = new Intent(NEWVERSION_UPDATE);
		PendingIntent updatePendingIntent = PendingIntent.getBroadcast(mContext, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		return updatePendingIntent;		
	}

	private PendingIntent getNotUpdatePendingIntent() {
		Intent notUpdateIntent = new Intent(NEWVERSION_NOUPDATE);
		PendingIntent updatePendingIntent = PendingIntent.getBroadcast(mContext, 0, notUpdateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		return updatePendingIntent;		
	}
	
	public void promptUpdateInfo(Handler handler) {
		mHandler = handler;		

		if (mPromptType == PROMPT_NOTIFICATION) {
			sendUpdateNotify();
			return;
			
		} else if (mPromptType == PROMPT_DIALOG) {
			String text = "";

			if (mPromptTimes < MAX_PROMPT_TIMES - 1) {
				text = MResource.getString(mContext,"nq_update_not_update");	
			} else {
				text = MResource.getString(mContext,"nq_update_not_reminder");
			} 
			
			if (mUpdateDialog == null) {
				mUpdateDialog = new Dialog(mContext, MResource.getIdByName(mContext,"style","Translucent_NoTitle"));
			}
	        View view = LayoutInflater.from(mContext).inflate(MResource.getIdByName(mContext, "layout", "nq_update_dialog_layout"), null);
	        int titleId = MResource.getIdByName(mContext, "id", "nq_update_dialog_title");
	        TextView title = (TextView) view.findViewById(titleId);
	        title.setText(mPromptTitle);
	        
	        int contentId = MResource.getIdByName(mContext, "id", "updatedialog_content");      
	        TextView content = (TextView) view.findViewById(contentId);
	        content.setText(convertPromptContent(mPromptContent));
	        
	        int btnCancelId = MResource.getIdByName(mContext, "id", "no_update");
	        Button btnCancel = (Button) view.findViewById(btnCancelId);
	        btnCancel.setText(text);
	        btnCancel.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	            	mUpdateDialog.dismiss();
			        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
			        		IncrementUpdateConstants.ACTION_LOG_3006, null, 0, null);
	            }
	        });

	        int btnOkId = MResource.getIdByName(mContext, "id", "ok_update");
	        Button btnOk = (Button) view.findViewById(btnOkId);
	        btnOk.setText(MResource.getString(mContext,"nq_update_immediately"));
	        btnOk.setOnClickListener(new View.OnClickListener() {
	            @Override
	            
	            public void onClick(View view) {
		        	   //处理升级
		        	   try {
		        		   mUpdateDialog.dismiss();
						processUpgrade(mHandler);
				        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
				        		IncrementUpdateConstants.ACTION_LOG_3005, null, 0, null);
					} catch (Exception e) {
						e.printStackTrace();
					}           	
	            }
	        });
			
	        mUpdateDialog.setContentView(view);			
	        mUpdateDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);  
	        mUpdateDialog.setCanceledOnTouchOutside(true);
	        
	        if (!mUpdateDialog.isShowing()) {
        	    mPromptTimes++;
        	    writeConfigData();
        	    mUpdateDialog.show();
		        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
		        		IncrementUpdateConstants.ACTION_LOG_3004, null, 0, null);
	        }
		} else {
			return;
		}
	}	
	
	
	/**
	 * 增量升级失败，显示全量升级提示框
	 */
	private void showFullUpdateDialog() {
		if (mHandler == null) {
			return;
		}
		mHandler.post(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
		        displayFullUpdateDialog();
			}
		});
	}
	
	/**
	 * 创建并显示全量升级提示框
	 */
	private void displayFullUpdateDialog() {
        
		if (mFullUpdateDialog == null) {
			mFullUpdateDialog = new Dialog(mContext, MResource.getIdByName(mContext,"style","Translucent_NoTitle"));
		}
        View view = LayoutInflater.from(mContext).inflate(MResource.getIdByName(mContext, "layout", "nq_update_dialog_layout"), null);
        int titleId = MResource.getIdByName(mContext, "id", "nq_update_dialog_title");
        TextView title = (TextView) view.findViewById(titleId);
        title.setText(mPromptTitle);
        
        int contentId = MResource.getIdByName(mContext, "id", "updatedialog_content");      
        TextView content = (TextView) view.findViewById(contentId);
        content.setText(MResource.getString(mContext,"nq_update_fullupdate_prompt") + convertPackageSize2M(mNewFileSize));
        
        int btnCancelId = MResource.getIdByName(mContext, "id", "no_update");
        Button btnCancel = (Button) view.findViewById(btnCancelId);
        btnCancel.setText(MResource.getString(mContext,"nq_update_not_update")); 
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	mFullUpdateDialog.dismiss();
            }
        });

        int btnOkId = MResource.getIdByName(mContext, "id", "ok_update");
        Button btnOk = (Button) view.findViewById(btnOkId);
        btnOk.setText(MResource.getString(mContext,"nq_update_immediately"));
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	//处理升级
            	downloadPackage(FULLPACK_UPDATE, false);
            	mFullUpdateDialog.dismiss();
            }
        });
        
        mFullUpdateDialog.setContentView(view);			
        mFullUpdateDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);  
        mFullUpdateDialog.setCanceledOnTouchOutside(true);
        if (!mFullUpdateDialog.isShowing()) {
        	mFullUpdateDialog.show();
        }
	}

	/**
	 * 点击立刻升级后进行升级
	 */
	public void processUpgrade(Handler handler) throws Exception  {	
		mHandler = handler;
		mNotificationMgr.cancel(NEWVERSION_UPDATE.hashCode());
		
		//暂时为GP版本写死直接跳转gp，以后需要根据下载来源判断
		if (ClientInfo.isGP()) {
			String url = "https://play.google.com/store/apps/details?id=" + mContext.getPackageName();
			GpUtils.viewDetail(mContext, url);
			return;
		}
		
		mDownloadStatus = (int) mHelper.getLongValue(IncrementUpdatePreference.KEY_INCREMENT_UPDATE_DOWNLOAD_STATUS);
		if (mUpgradeType == FULLPACK_UPDATE) { //只做全量更新		
			if (isPackageReady()) {
				installAPK(mDownloadFullPackPath);					
			} else {
				if (mDownloadStatus == DOWNLOAD_FULLPACKAGE_START_BACK && mDownloadFullPackId != -1) {
					mDownloader.cancelDownload(mDownloadFullPackId);
				}
				//下载全量包
				downloadPackage(FULLPACK_UPDATE, false);
			}
		} else { //增量优先
			mOriginalApkPath = getOriginalInstallPath();
			if (mOriginalApkPath == null) {			
				//show下载全量包对话框
    			showFullUpdateDialog();	
				return;
			}				
			
			processIncrementUpdate();
		}
	}
	
	/**
	 * 检查是否升级包已经下载完毕且正确
	 * @return
	 */
	private boolean isPackageReady() {
		if (mUpgradeType == FULLPACK_UPDATE) {//全量包
			return checkPackage(mDownloadFullPackPath, mNewFileMd5);
		} else if (mUpgradeType == INCREMENT_UPDATE) {
			return checkPackage(mPatchPath, mPatchMd5);
		} else {
			return false;
		}
	}
	
	private boolean checkPackage(String filePath, String md5) {
		boolean ret = false;
		
		File file = null;
		if (filePath != null) {
			file = new File(filePath);
			if (getVersionFromFileName(filePath).equals(mNewVersionCode)
				&& file != null 
				&& file.exists() 
				&& MD5.checkMD5(md5, file)) {		
				ret = true;
			}
		}
		return ret;
	}
	

	private void processIncrementUpdate() {
		//判断patch是否已经自动下载
		if (isPackageReady()) {
			//patch包已经wifi自动下载完开始合并patch包
			startPatch();
		} else {
			if (mDownloadStatus == DOWNLOAD_PATCH_START_BACK && mDownloadPatchId != -1) {
				mDownloader.cancelDownload(mDownloadPatchId);
			}
			downloadPackage(INCREMENT_UPDATE, false);
		}	 
	}

	
	private void startPatch() {
		new Thread(new PatchThread()).start();
	}
	
	class PatchThread implements Runnable {	
		
		@Override
		public void run() {		
			int ret = 0;
			            
	        try {            	
	        	NqLog.i("coming into patch thread......");
	        	NqLog.i("mPatchPath = " + mPatchPath);
	        	NqLog.i("mDownloadPatchId = " + mDownloadPatchId);
	        	if (mPatchPath != null && validateFile(new File(mPatchPath), mPatchMd5, mPatchSize)) {
	               	IncrementUpdatePatcher incUpdate = new IncrementUpdatePatcher();
	               	String SDCardPath = CommonMethod.getSDcardPath(mContext);
	               	mNewApkPath = new StringBuilder().append(SDCardPath).append(IncrementUpdateUtils.INCREASE_UPDATE_PATH).append(mNewVersionCode)
	               					.append(IncrementUpdateUtils.FULLPACKAGE_SUFFIX).toString();
	               	NqLog.i("mNewApkPath = " + mNewApkPath);
               	
	               	ret = incUpdate.patcher(mOriginalApkPath, mNewApkPath, mPatchPath);
	               	if (ret == 0) {
	               		NqLog.i("generate new apk file succeed!");
	               		
	               		//验证本地生成的新apk md5
	               		File file = new File(mNewApkPath);
	               		if (mNewApkPath != null && validateFile(file, mNewFileMd5, mNewFileSize)) {
	               			ret = NEWPACK_SUCC;
	               		} else {
	               			//验证失败
	               			file.delete();
	               			ret = NEWPACK_FAILED;
	               		}
	               	} else {//合并失败
	               		NqLog.i("generate new apk file failed!");               	
	               		ret = NEWPACK_FAILED;
	               	}
	        	} else {//patch包验证失败
	        		ret = NEWPACK_FAILED;
	        	}
	        } catch (Exception e) {
	        	NqLog.i("generate new apk file failed!");
	        	ret = NEWPACK_FAILED;
	        } finally {
	        	//new File(mPatchPath).delete();
				//mHandler.sendEmptyMessage(ret);
	        	
	        	if (ret == NEWPACK_SUCC) {
	        		installAPK(mNewApkPath);
	        	} else {
					//增量更新失败，show下载全量包对话框
	    			showFullUpdateDialog();	       		
	        	}
	        }            
		}// end of run

	}

	private String getOriginalInstallPath() {	
        try{  
            //原始位置  
            return mPkgManager.getApplicationInfo(mContext.getPackageName(),0).sourceDir;  
        }catch (NameNotFoundException e) {  
            e.printStackTrace();
            return null;
        }		
	}
	
	/**
	 * 下载patch增量包或者全量包
	 * @param upgradeType 全量或增量更新标志
	 * @param back_download 是否静默下载
	 */	
	private void downloadPackage(int upgradeType, boolean back_download) {
		MyDownloadManager downloader = MyDownloadManager.getInstance(mContext);
		
		App app = new App();
		app.setStrId("");

		NqLog.i("upgradeType = " + upgradeType + "back_download = " + back_download);
		mDownloadStatus = (int) mHelper.getLongValue(IncrementUpdatePreference.KEY_INCREMENT_UPDATE_DOWNLOAD_STATUS);
		if (upgradeType == FULLPACK_UPDATE) {//只做全量更新			
			app.setStrAppUrl(mNewFileUrl);
			app.setStrAppPath(mDownloadFullPackPath);
			app.setStrName(MResource.getString(mContext,"app_name"));
			app.setLongSize(-1);
			
			if (back_download) {
				if (mDownloadStatus == DOWNLOAD_FULLPACKAGE_START_BACK && mDownloadFullPackId != -1) {
					mDownloader.cancelDownload(mDownloadFullPackId);
					return;
				}				
				mDownloadFullPackId = downloader.downloadApp_background(app).intValue();
				mDownloadStatus = DOWNLOAD_FULLPACKAGE_START_BACK;
			} else {
				if (mDownloadStatus == DOWNLOAD_FULLPACKAGE_START && mDownloadFullPackId != -1) {
					mDownloader.cancelDownload(mDownloadFullPackId);
					//return;
				}				
				mDownloadFullPackId = downloader.downloadApp(app).intValue();
				mDownloadStatus = DOWNLOAD_FULLPACKAGE_START;
			}
			mHelper.setLongValue(IncrementUpdatePreference.KEY_INCREMENT_UPDATE_DOWNLOAD_STATUS, mDownloadStatus);
			mHelper.setLongValue(IncrementUpdatePreference.KEY_INCREMENT_UPDATE_DOWNLOAD_FULLPACK_ID, mDownloadFullPackId);
			mHelper.setStringValue(IncrementUpdatePreference.KEY_INCREMENT_UPDATE_DOWNLOAD_FULLPACK_PATH, mDownloadFullPackPath);
			
		} else if (upgradeType == INCREMENT_UPDATE) {//增量更新			
			NqLog.i("mPatchPath = "+ mPatchPath);
			app.setStrAppUrl(mPatchUrl);
			app.setStrAppPath(mPatchPath);
			app.setStrName(MResource.getString(mContext,"app_name"));
			app.setLongSize(-1);	
			if (back_download) {				
				if (mDownloadStatus == DOWNLOAD_PATCH_START_BACK && mDownloadPatchId != -1) {
					mDownloader.cancelDownload(mDownloadPatchId);
					//return;
				}
				
				mDownloadPatchId = downloader.downloadApp_background(app).intValue();
				mDownloadStatus = DOWNLOAD_PATCH_START_BACK;
			} else {
				if (mDownloadStatus == DOWNLOAD_PATCH_START && mDownloadPatchId != -1) {
					mDownloader.cancelDownload(mDownloadPatchId);
					//return;
				}				
				mDownloadPatchId = downloader.downloadApp(app).intValue();
				mDownloadStatus = DOWNLOAD_PATCH_START;
			}
			mHelper.setLongValue(IncrementUpdatePreference.KEY_INCREMENT_UPDATE_DOWNLOAD_STATUS, mDownloadStatus);
			mHelper.setLongValue(IncrementUpdatePreference.KEY_INCREMENT_UPDATE_DOWNLOAD_PATCH_ID, mDownloadPatchId);
			NqLog.i("set mDownloadPatchId = " + mDownloadPatchId);
			NqLog.i("set mDownloadStatus = " + mDownloadStatus);
		}		
		
	}


       
	/**
	 * 用文件大小和md5校验包的正确性
	 * @param updateFile: 待校验的文件
	 * @param md5 
	 * @param fileSize
	 */	
	private boolean validateFile(File updateFile, String md5, int fileSize) throws Exception {
	    long size = 0;
	    NqLog.i("coming into validateFile");
	    
		if (updateFile.exists()) {
			FileInputStream fis = null;
			fis = new FileInputStream(updateFile);
			size = fis.available();
			fis.close();
		} else {
			return false;
		}

		NqLog.i("file: "+ updateFile.getName() + "size is:" + size);
		if (size != fileSize) {
			return false;
		}
		return MD5.checkMD5(md5, updateFile);
	}
	

	/**
	 * 安装APK
	 * 	 * @param apkUrl
	 */
	private void installAPK(String apkUrl) {   
		try {
			if (!validateFile(new File(apkUrl), mNewFileMd5, mNewFileSize)) {
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.parse("file://" + apkUrl),
				"application/vnd.android.package-archive");
		mContext.startActivity(intent);
		
		//reset flag
//		mHasNewVersion = false;
	}

	/**
	 * get version code from input filename
	 * 	 * @param apkUrl
	 */
	private String getVersionFromFileName(String filename) {
		return filename.substring(filename.lastIndexOf("/") + 1,filename.lastIndexOf("."));
	}	
	

	/**
	 * 当前时间是否处于提示时间段范围内
	 */	
	private boolean isInPromptPeriod() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String sDate=sdf.format(new java.util.Date());

		if (mPromptStartTime.compareTo(sDate) > 0 
				|| mPromptEndTime.compareTo(sDate) < 0 ) {
			return false;
		}
		return true;
	}

	/**
	 * 当前时间是否处于提示时间间隔天数
	 */	
    private boolean isInPromptInterval(){
    	if (mLastPromptTime.isEmpty()) {
    		return true;
    	}
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");    	
    	String strCurrentDay = sdf.format(new java.util.Date());

		Date dateLastPrompt;
		Date dateCurrent;

    	try {
			dateLastPrompt = sdf.parse(mLastPromptTime);
			dateCurrent = sdf.parse(strCurrentDay);
			if (dateLastPrompt.equals(dateCurrent) && mPromptTimes > 0) {//当天已经弹过一次
				return false;
			}
			long diff = dateCurrent.getTime() - dateLastPrompt.getTime();
			long days = diff / (1000 * 60 * 60 * 24);
			if ( days % mPromptInterval == 0) {
				return true;
			}
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}   
                                              
        return false;  
    }	
    
    private boolean isUpdateProcessing() {
    	Long downloadId = null;
    	boolean ret = false;
    	
    	mDownloadStatus = (int) mHelper.getLongValue(IncrementUpdatePreference.KEY_INCREMENT_UPDATE_DOWNLOAD_STATUS);
    	if (mUpgradeType == INCREMENT_UPDATE) {
    		downloadId = mDownloader.getDownloadId(mPatchUrl);
    		NqLog.i("mUpgradeType = " + mUpgradeType + " mDownloadStatus =" + mDownloadStatus + " downloadId = " + downloadId);
    		if (mDownloadStatus == DOWNLOAD_PATCH_START && downloadId != null) {
    			ret = true;
    		}
    	} else {
    		downloadId = mDownloader.getDownloadId(mNewFileUrl);
    		NqLog.i("mUpgradeType = " + mUpgradeType + " mDownloadStatus =" + mDownloadStatus + " downloadId = " + downloadId);  		
    		if (mDownloadStatus == DOWNLOAD_FULLPACKAGE_START && downloadId != null) {
    			ret = true;
    		}    		
    	}
    	
    	return ret;
    }

    private void readConfigData() {		
		mLastPromptTime = mHelper.getStringValue(IncrementUpdatePreference.KEY_LAST_INCREMENT_UPDATE_PROMPT_TIME);
		NqLog.i("mLastPromptTime = " +  mLastPromptTime);
		
		mPromptTimes = mHelper.getIntValue(IncrementUpdatePreference.KEY_LAST_INCREMENT_UPDATE_PROMPT_COUNT);
		NqLog.i("mPromptTimes = " +  mPromptTimes);
    }
    
    private void resetConfigData() {
    	NqLog.i("coming into resetConfigData");
		String version = mHelper.getStringValue(IncrementUpdatePreference.KEY_LAST_INCREMENT_UPDATE_VERSION);
		if (!version.equalsIgnoreCase(mNewVersionCode)) {
			NqLog.i("new version:" + version + "write reset value!");
			//有新版本
			mHelper.setIntValue(IncrementUpdatePreference.KEY_LAST_INCREMENT_UPDATE_PROMPT_COUNT, 0);
			mHelper.setStringValue(IncrementUpdatePreference.KEY_LAST_INCREMENT_UPDATE_PROMPT_TIME, "");
		}
		mHelper.setStringValue(IncrementUpdatePreference.KEY_LAST_INCREMENT_UPDATE_VERSION, mNewVersionCode);
    }
    
    private void writeConfigData() {
    	NqLog.i("coming into writeConfigData");
    	NqLog.i("write mPromptTimes = " + mPromptTimes);

		mHelper.setIntValue(IncrementUpdatePreference.KEY_LAST_INCREMENT_UPDATE_PROMPT_COUNT, mPromptTimes);
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");    	
    	String strCurrentDay = sdf.format(new java.util.Date());
		mHelper.setStringValue(IncrementUpdatePreference.KEY_LAST_INCREMENT_UPDATE_PROMPT_TIME, strCurrentDay);
    	NqLog.i("write mLastPromptTime = " + strCurrentDay);		
    }
    
    
	/**
	 * 转换安装包大小。从byte到M
	 */	
	private String convertPackageSize2M(long packageSize) {
		double f = (double)packageSize / 1024 / 1024;
		return String.format("%.1f", f) + "M";
	}
	
	private String convertPromptContent(String content) {
		return content.replaceAll("(?:<br>)","\n");
	}
	
	/**
	 * 获取新版本信息的接口
	 * @param listener: 获取新版本信息后的回调
	 */
	public void getUpdate(UpdateListener listener) {
		GetNewVersionServiceFactory.getService().getNewVersion(listener);
	}
	
	/**
	 * 获取新版本信息成功的的事件处理
	 * @param event
	 */
	public void onEvent(GetNewVersionSuccessEvent event) {		
		NqLog.i("onEvent(GetNewVersionSuccessEvent) is called!");
		TNewVersionResp resp = event.getNewVersion();
		UpdateListener listener = (UpdateListener)event.getTag();
			
		if (resp == null) {
			sendNoNeedUpdateCallback(listener);
		} else {			
			NewVersion newVersion = null;
			if (resp.hasNewVersion == 1) {// 有新版本
				newVersion = convert(resp);
			} 
			if (newVersion != null){
				NqLog.i("GetNewVersionProtocol succ:"
						+ newVersion.toString());
				
				saveCache(newVersion);
				mHelper.setBooleanValue(IncrementUpdatePreference.KEY_HAVE_UPDATE, true);
				refreshData();
//				wifiAutoDownload();
				sendNeedUpdateCallback(newVersion,listener);
			} else {
				mHelper.setBooleanValue(IncrementUpdatePreference.KEY_HAVE_UPDATE, false);
				clearCache();
				sendNoNeedUpdateCallback(listener);
			}
		}
	}
	
	/**
	 * 获取新版本信息失败后的事件处理 
	 * @param event
	 */
	public void onEvent(GetNewVersionFailedEvent event) {
		UpdateListener listener = (UpdateListener)event.getTag();
		sendOnErrorCallback(listener);
	}

	/**
	 * 下载完成后收到通知的事件处理 
	 * @param event
	 */
	public void onEvent(DownloadCompleteEvent event) {
		long reference = event.getRefer();
		
		long update_fullpack_refer = mHelper.getLongValue(IncrementUpdatePreference.KEY_INCREMENT_UPDATE_DOWNLOAD_FULLPACK_ID);
		long update_patch_refer = mHelper.getLongValue(IncrementUpdatePreference.KEY_INCREMENT_UPDATE_DOWNLOAD_PATCH_ID);
		if (reference == update_fullpack_refer) {//全量包下载成功
			long status = mHelper.getLongValue(IncrementUpdatePreference.KEY_INCREMENT_UPDATE_DOWNLOAD_STATUS);
			mHelper.setLongValue(IncrementUpdatePreference.KEY_INCREMENT_UPDATE_DOWNLOAD_STATUS, DOWNLOAD_FULLPACKAGE_COMPLETED);
			String apkUrl = mHelper.getStringValue(IncrementUpdatePreference.KEY_INCREMENT_UPDATE_DOWNLOAD_FULLPACK_PATH);
			if (status == DOWNLOAD_FULLPACKAGE_START) {
				installAPK(apkUrl);
			} 
		} else if (reference == update_patch_refer) {//增量包下载成功
			long status = mHelper.getLongValue(IncrementUpdatePreference.KEY_INCREMENT_UPDATE_DOWNLOAD_STATUS);
			NqLog.i("DownloadReceiver: reference == update_patch_refer");
			NqLog.i("DownloadReceiver: status =" + status);
			mHelper.setLongValue(IncrementUpdatePreference.KEY_INCREMENT_UPDATE_DOWNLOAD_STATUS,DOWNLOAD_PATCH_COMPLETED);
			if (status == DOWNLOAD_PATCH_START) {
				startPatch();
			} else if (status == IncrementUpdateManager.DOWNLOAD_PATCH_START_BACK) {
				return;
			} else {//增量包下载状态不对，提示更新失败,下载全量包对话框
    			showFullUpdateDialog();	
			}
		}
	}
	
	private void sendNoNeedUpdateCallback(UpdateListener listener) {
		if (listener != null) {
			Bundle b = new Bundle();
			b.putBoolean("needUpdate", false);
			listener.getUpdateSucc(b);
		}
	}
	
	private void sendNeedUpdateCallback(NewVersion newVersion, UpdateListener listener) {
		if (listener != null) {
			Bundle b = new Bundle();
			String content = convertPromptContent(newVersion.getPromptContent());
			b.putBoolean("needUpdate", true);
			b.putString("title",newVersion.getPromptTitle());
			b.putString("content", content);
			listener.getUpdateSucc(b);
		}
	}
	
	private void sendOnErrorCallback(UpdateListener listener) {
		if (listener != null) {
			listener.onErr();
		}
	}
	
	/**
	 * 转换成NewVersion类型
	 * @author chenyanmin
	 * @time 2014-8-4 下午5:07:36
	 * @param resp
	 * @return
	 */
	private NewVersion convert(TNewVersionResp resp) {
		NewVersion result = null;
		if (resp != null) {
			NewVersion newVersion = new NewVersion();
			newVersion.setAutoDownload(resp.getAutoDownload());
			newVersion.setNewFileMd5(resp.getNewFileMd5());
			newVersion.setNewFileSize(resp.getNewFileSize());
			newVersion.setNewFileUrl(resp.getNewFileUrl());
			newVersion.setNewVersionName(resp.getNewVersionName());
			newVersion.setOldFileMd5(resp.getOldFileMd5());
			newVersion.setOldFileSize(resp.getOldFileSize());
			newVersion.setPatchAlgorithm(resp.getPatchAlgorithm());
			newVersion.setPatchMd5(resp.getPatchMd5());
			newVersion.setPatchSize(resp.getPatchSize());
			newVersion.setPatchUrl(resp.getPatchUrl());
			newVersion.setPromptContent(resp.getPromptContent());
			newVersion.setPromptEndTime(resp.getPromptEndTime());
			newVersion.setPromptInterval(resp.getPromptInterval());
			newVersion.setPromptNetwork(resp.getPromptNetwork());
			newVersion.setPromptStartTime(resp.getPromptStartTime());
			newVersion.setPromptTitle(resp.getPromptTitle());
			newVersion.setPromptType(resp.getPromptType());
			newVersion.setUpgradeType(resp.getUpgradeType());
			result = newVersion;
		}
		return result;
	}
	
	private boolean saveCache(NewVersion newVersion){
		ContentValues values = null;
        if (newVersion != null) {
            values = new ContentValues();
            values.put(NewVersionTable.NEWVERSION_NEW_VERSION_NAME, newVersion.getNewVersionName());
    		values.put(NewVersionTable.NEWVERSION_UPGRADE_TYPE, newVersion.getUpgradeType());
    		values.put(NewVersionTable.NEWVERSION_AUTO_DOWNLOAD, newVersion.getAutoDownload());
    		values.put(NewVersionTable.NEWVERSION_PROMPT_TYPE, newVersion.getPromptType());
    		values.put(NewVersionTable.NEWVERSION_PROMPT_TITLE, newVersion.getPromptTitle());
    		values.put(NewVersionTable.NEWVERSION_PROMPT_CONTENT, newVersion.getPromptContent());
    		values.put(NewVersionTable.NEWVERSION_PROMPT_START_TIME, newVersion.getPromptStartTime());
    		values.put(NewVersionTable.NEWVERSION_PROMPT_END_TIME, newVersion.getPromptEndTime());
    		values.put(NewVersionTable.NEWVERSION_PROMPT_INTERVAL, newVersion.getPromptInterval());
    		values.put(NewVersionTable.NEWVERSION_PROMPT_NETWORK, newVersion.getPromptNetwork());
    		values.put(NewVersionTable.NEWVERSION_OLD_FILE_SIZE, newVersion.getOldFileSize());
    		values.put(NewVersionTable.NEWVERSION_OLD_FILE_MD5, newVersion.getOldFileMd5());
    		values.put(NewVersionTable.NEWVERSION_PATCH_ALGORITHM, newVersion.getPatchAlgorithm());
    		values.put(NewVersionTable.NEWVERSION_PATCH_SIZE, newVersion.getPatchSize());
    		values.put(NewVersionTable.NEWVERSION_PATCH_MD5, newVersion.getPatchMd5());
    		values.put(NewVersionTable.NEWVERSION_PATCH_URL, newVersion.getPatchUrl());
    		values.put(NewVersionTable.NEWVERSION_NEW_FILE_SIZE, newVersion.getNewFileSize());
    		values.put(NewVersionTable.NEWVERSION_NEW_FILE_MD5, newVersion.getNewFileMd5());
    		values.put(NewVersionTable.NEWVERSION_NEW_FILE_URL, newVersion.getNewFileUrl());
            try {
            	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                ContentResolver contentResolver = mContext.getContentResolver();
                Builder delete = ContentProviderOperation.newDelete(NewVersionTable.TABLE_URI);//delete all
                Builder insert = ContentProviderOperation.newInsert(NewVersionTable.TABLE_URI).withValues(values);
                ops.add(delete.build());
                ops.add(insert.build());
                contentResolver.applyBatch(NewVersionTable.DATA_AUTHORITY,ops);
                return true;
			} catch (Exception e) {
				NqLog.e("saveCache error " + e.toString());
	            e.printStackTrace();
			}
        }
        return false;
	}
	
	private void clearCache() {
        try {
        	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            ContentResolver contentResolver = mContext.getContentResolver();
            Builder delete = ContentProviderOperation.newDelete(NewVersionTable.TABLE_URI);//delete all
            ops.add(delete.build());
            contentResolver.applyBatch(NewVersionTable.DATA_AUTHORITY,ops);
		} catch (Exception e) {
			NqLog.e("saveCache error " + e.toString());
            e.printStackTrace();
		}		
	}
}
