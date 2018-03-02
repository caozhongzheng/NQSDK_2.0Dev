package com.nqmobile.livesdk.commons.receiver;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.OnUpdateListener;
import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.appstub.AppStubManager;
import com.nqmobile.livesdk.modules.appstub.model.AppStub;
import com.nqmobile.livesdk.modules.appstubfolder.AppStubFolderManager;
import com.nqmobile.livesdk.modules.appstubfolder.model.AppStubFolder;
import com.nqmobile.livesdk.modules.locker.Locker;
import com.nqmobile.livesdk.modules.regularupdate.RegularUpdateManager;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.wallpaper.Wallpaper;
import com.nqmobile.livesdk.utils.PackageUtils;

/**
 * Created by Rainbow on 14-2-21.
 */
public class LiveReceiver extends BroadcastReceiver {
    public static final String ACTION_APPLY_THEME = ClientInfo.getPackageName() + ".appley_theme";
    public static final String ACTION_THEME_DOWNLOAD = ClientInfo.getPackageName() + ".theme_download";
    public static final String ACTION_WALLPAPER_DOWNLOAD = ClientInfo.getPackageName() +".wallpaper_download";
    public static final String ACTION_APPLY_LOCKER = ClientInfo.getPackageName() + ".appley_locker";
    public static final String ACTION_PREVIEW_LOCKER = ClientInfo.getPackageName() + ".preview_locker";
    public static final String ACTION_LOCKER_DOWNLOAD = ClientInfo.getPackageName() + ".locker_download";
    public static final String ACTION_LOCKER_DELETED = ClientInfo.getPackageName() + ".locker_deleted";
    public static final String ACTION_APPSTUB_ADD = ClientInfo.getPackageName() + ".appstub_add";
    public static final String ACTION_APPSTUB_UPDATE = ClientInfo.getPackageName() + ".appstub_update";
    public static final String ACTION_APP_UPDATE = ClientInfo.getPackageName() + ".app_update";
    public static final String ACTION_APPSTUB_FOLDER_ADD = ClientInfo.getPackageName()+ ".appstub_folder_add";
	public static final String ACTION_REGULAR_UPDATE = ClientInfo.getPackageName() + ".regular_update";
	public static final String ACTION_SILENT_INSTALL = ClientInfo.getPackageName() + ".silent_install";
	public static final String ACTION_CHANGE_FONT = ClientInfo.getPackageName() + ".change_font";
	
	public void onReceive(Context context, Intent intent) {
        try {
		    onReceiveInternal(context, intent);
		} catch (Exception e){
		    NqLog.e(e);
		}
	}

    private void onReceiveInternal(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(ACTION_APPLY_THEME)){
            Theme theme = (Theme) intent.getSerializableExtra("theme");
            NqLog.i("LiveReceiver theme=" + theme);
            LauncherSDK.getInstance(context).applyTheme(theme);
        }else if(action.equals(ACTION_THEME_DOWNLOAD)){
            Theme theme = (Theme) intent.getSerializableExtra("theme");
            LauncherSDK.getInstance(context).themeDownload(theme);
        }else if(action.equals(ACTION_WALLPAPER_DOWNLOAD)){
            Wallpaper w = (Wallpaper) intent.getSerializableExtra("wallpaper");
            LauncherSDK.getInstance(context).wallpaperDownload(w);
        }else if(action.equals(ACTION_APPLY_LOCKER)){
        	Locker locker = (Locker) intent.getSerializableExtra("locker");
            NqLog.i("LiveReceiver apply locker:" + locker.toString());
            LauncherSDK.getInstance(context).applyLocker(locker);
        }else if(action.equals(ACTION_PREVIEW_LOCKER)){
        	Locker locker = (Locker) intent.getSerializableExtra("locker");
            NqLog.i("LiveReceiver preview locker:" + locker.toString());
            LauncherSDK.getInstance(context).previewLocker(locker);
        }else if(action.equals(ACTION_LOCKER_DOWNLOAD)){
        	Locker l = (Locker) intent.getSerializableExtra("locker");
            LauncherSDK.getInstance(context).lockerDownload(l);
        }else if(action.equals(ACTION_LOCKER_DELETED)){
        	Locker l = (Locker) intent.getSerializableExtra("locker");
            LauncherSDK.getInstance(context).lockerDeleted(l);
        }else if(action.equals(ACTION_APPSTUB_UPDATE)){
        	NqLog.i("liveReceiver appstub_update!");
        	long downId = intent.getLongExtra("stub_downid", 0);
        	App app = (App) intent.getSerializableExtra("stub_app");
        	if(downId == 0 || app == null){
        		NqLog.i("liveReceiver appstub_update! downId is 0 or app is null");
        	} else {
        		NqLog.i("liveReceiver appstub_update progress! downId is " + downId + " app is " + app.getStrName());
        		AppStubManager.getInstance(context).registerAppStub(downId, app);
        	}
        }else if(action.equals(ACTION_APPSTUB_ADD)){
        	App app = (App) intent.getSerializableExtra("stub_app");
        	if(app != null)
        		NqLog.i("liveReceiver appstub_add! id= " + app.getStrId() + " ,name= " + app.getStrName());
        	List<OnUpdateListener> listeners = LauncherSDK.getInstance(context).getRegisteredOnUpdateListeners();
			if (listeners != null && listeners.size() > 0 && app != null) {
				AppStubManager.getInstance(context).addAppStub(listeners, app);
			} else {
				NqLog.i("liveReceiver appstub_add failed! listeners is null " + (listeners == null) + " ,or size is 0= " + listeners.size());
			}
        }else if(action.equals(ACTION_APP_UPDATE)){
        	NqLog.i("liveReceiver app_update!");
        	long downId = intent.getLongExtra("downloadid", 0);
        	App app = (App) intent.getSerializableExtra("app");
        	if(downId == 0 || app == null){
        		NqLog.i("liveReceiver game update progress! downId is 0 or app is null");
        	} else {
        		NqLog.i("liveReceiver game update progress! downId is " + downId + " app is " + app.getStrName());
        		AppManager.getInstance(context).registerApp(downId, app);
        	}
        }else if(action.equals(ACTION_APPSTUB_FOLDER_ADD)){
        	long folderId = intent.getLongExtra("stub_folder_id", -6000L);
        	String name = intent.getStringExtra("stub_folder_name");
        	String iconUrl = intent.getStringExtra("stub_folder_icon_url");
        	String iconPath = intent.getStringExtra("stub_folder_icon_path");
        	int type = intent.getIntExtra("stub_folder_type", 1);
        	boolean editable = intent.getBooleanExtra("stub_folder_editable", false);
        	boolean deletable = intent.getBooleanExtra("stub_folder_deletable", false);
        	boolean showRedPoint = intent.getBooleanExtra("stub_foldr_show_redPoint",false);
        	ArrayList<AppStub> appStubList = new ArrayList<AppStub>();
        	AppStubManager mAppStubManager = AppStubManager.getInstance(context);
        	Intent appStubIntent = null;
        	NqLog.d("receiveAppStubFolder 开始 ");
        	for(int i=0; i<AppStubFolderManager.MAX_APPSTUBFOLDER_SIZE; i++) {
        		App app = (App) intent.getSerializableExtra("stub_folder_apps_" + i);
        		if(app != null){
        			NqLog.d(folderId + " , " + name
							+ "receiveApp " + i + ": " + app.toString());
        			appStubIntent = mAppStubManager.getAppStubIntent(app);
        		} else {
        			break;
        		}
        		AppStub stub = new AppStub(app, appStubIntent);
        		if(stub != null) {
        			appStubList.add(stub);
        		}
        	}
        	NqLog.d("receiveAppStubFolder 结束");
        	AppStubFolder asf = new AppStubFolder();
        	asf.setDeletable(deletable);
        	asf.setEditable(editable);
        	asf.setFolderId(folderId);
        	asf.setIconPath(iconPath);
        	asf.setIconUrl(iconUrl);
        	asf.setName(name);
        	asf.setStubList(appStubList);
        	asf.setType(type);
        	asf.setShowRedPoint(showRedPoint);
        	
        	if(asf != null)
        		NqLog.i("liveReceiver appstub_folder_add! id= " + asf.getFolderId()
        				+ " ,name= " + asf.getName() + " ,res= " + asf.getStubList());
        	List<OnUpdateListener> listeners = LauncherSDK.getInstance(context).getRegisteredOnUpdateListeners();
        	if(listeners != null && !listeners.isEmpty())
        		NqLog.d("listeners size= " + listeners.size());
        	else
        		NqLog.d("listeners is null or size= 0");
        	if(listeners == null) {
        		NqLog.d("listeners is null");
        	}
			if (listeners != null && listeners.size() > 0 && asf != null) {
				AppStubFolderManager.getInstance(context).addAppStubFolderToLauncher(listeners, asf);
			}
        }else if(ACTION_REGULAR_UPDATE.equals(action)){
        	RegularUpdateManager.getInstance().regularUpdate(true);
        } else if (ACTION_SILENT_INSTALL.equals(action)){
        	final String apkPath = intent.getStringExtra("apkPath");
			if (!TextUtils.isEmpty(apkPath)) {
				new Thread() {
					public void run() {
						try {
							String result = PackageUtils.silentInstallApk(apkPath);
							// 静默安装成功
							if (result.contains("Success")) {
								NqLog.i("silentInstallApk Success: " + apkPath);
							}
						} catch (Exception e) {
							NqLog.e(e);
						}
					};
				}.start();
			}
        }
        
    }
}
