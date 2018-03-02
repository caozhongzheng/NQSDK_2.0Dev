package com.nqmobile.livesdk.modules.appactive;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.mydownloadmanager.table.DownloadTable;
import com.nqmobile.livesdk.commons.prefetch.table.PrefetchTable;
import com.nqmobile.livesdk.commons.receiver.PackageAddedEvent;
import com.nqmobile.livesdk.modules.appstub.AppStubConstants;
import com.nqmobile.livesdk.modules.appstub.table.AppStubCacheTable;
import com.nqmobile.livesdk.modules.appstubfolder.AppStubFolderConstants;
import com.nqmobile.livesdk.modules.gamefolder_v2.GameManager;

/**
 * Created by Rainbow on 2014/12/3.
 */
public class AppActiveManager extends AbsManager{

    // ===========================================================
    // Constants
    // ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(AppActiveModule.MODULE_NAME);
    // ===========================================================
    // Fields
    // ===========================================================

    private static AppActiveManager mInstance;
    private PackageManager mPackageManager;
    private Context mContext;

    // ===========================================================
    // Constructors
    // ===========================================================

    private AppActiveManager() {
        EventBus.getDefault().register(this);
        mContext = ApplicationContext.getContext();
        mPackageManager = mContext.getPackageManager();
    }

    public synchronized static AppActiveManager getInstance() {
        if (mInstance == null) {
            mInstance = new AppActiveManager();
        }
        return mInstance;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public void init() {
        NqLog.i("AppActiveManager init");
        EventBus.getDefault().register(this);
    }

    // ===========================================================
    // Methods
    // ===========================================================

    public void onEvent(PackageAddedEvent event){
        NqLog.i("AppActiveManager onEvent PackageAddedEvent pack="+event.getPackageName());
        final String packageName = event.getPackageName();
        new Thread(){
            @Override
            public void run() {
            	GameManager mGameManager = GameManager.getInstance();
                boolean isGame = mGameManager.isGameFromLocal(packageName);
                boolean isGFExist = mGameManager.isGameFolderExisted();
                NqLog.i("isGame="+isGame + ", isGFExist="+isGFExist);
                // 广告资源，非游戏+虚框类+（是游戏但是游戏文件夹不存在的）
                if(isAdResource(packageName) && (!isGame || isGame && !isGFExist || isStubResource(packageName))){
                    addShortcut(packageName);
                }
            }
        }.start();

    }

    public String getAppName(String packname){
        try {
            ApplicationInfo info = mPackageManager.getApplicationInfo(packname, 0);
            return info.loadLabel(mPackageManager).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public Drawable getAppIcon(String packname){
        try {
            ApplicationInfo info = mPackageManager.getApplicationInfo(packname, 0);
            return info.loadIcon(mPackageManager);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }

    private void addShortcut(String packageName){
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_AD_SHORTCUT");

        String name = getAppName(packageName);
        Drawable icon = getAppIcon(packageName);
        Bitmap bt = drawableToBitmap(icon);
        NqLog.i("addShortcut packageName="+packageName+" name="+name);
        if(!TextUtils.isEmpty(name) && icon != null){
            //快捷方式的名称
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
            shortcut.putExtra("duplicate", false); //不允许重复创建
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, mPackageManager.getLaunchIntentForPackage(packageName));
            shortcut.putExtra("mustCreate",true);

            //快捷方式的图标
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, bt);
            mContext.sendBroadcast(shortcut);
        }
    }

    private boolean isAdResource(String packageName){
        boolean result = false;
        Cursor c = null;
        try{
            c = mContext.getContentResolver().query(DownloadTable.TABLE_URI,null, DownloadTable.DOWNLOAD_PACKAGENAME + " = ?",
                    new String[]{packageName},null);
            if(c != null && c.moveToNext()){
                result = true;
            }
        }finally {
            if(c != null){
                c.close();
            }
        }

        if(!result) {
        	Cursor cursor = null;
            try{
                cursor = mContext.getContentResolver().query(PrefetchTable.TABLE_URI,null, PrefetchTable.PACKAGE + " = ?",
                        new String[]{packageName},null);
                if(cursor != null && cursor.moveToNext()){
                    result = true;
                }
            }finally {
                if(cursor != null){
                    cursor.close();
                }
            }
        }

        NqLog.i("isAdResource packageName="+packageName+" result="+result);
        return result;
    }

    private boolean isStubResource(String packageName){
        boolean result = false;
        Cursor c = null;
        try{
            c = mContext.getContentResolver().query(AppStubCacheTable.TABLE_URI,null,
            		AppStubCacheTable.APP_PACKAGENAME + " = ? and " +
            		AppStubCacheTable.APP_SOURCE_TYPE + " = ?",
                    new String[]{packageName, AppStubConstants.STORE_MODULE_TYPE_APPSTUB + ""},null);
            if(c != null && c.getCount() > 0){
            	result = true;
            }
        }finally {
            if(c != null){
                c.close();
            }
        }

        NqLog.i("isStubResource packageName="+packageName+" result="+result);
        return result;
    }
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
