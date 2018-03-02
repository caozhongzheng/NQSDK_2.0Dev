package com.nqmobile.livesdk.utils;

import java.io.File;

import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.theme.ThemeDetailActivity;
import com.nqmobile.livesdk.modules.theme.ThemeManager;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperDetailActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Rainbow on 14-4-10.
 */
public class NotificationUtil {

    public static final int NOTIF_ID_THEME = 1;
    public static final int NOTIF_ID_WALLPAPER = 2;
    public static final int NOTIF_ID_MUSTINSTALL = 3;
    public static final int NOTIF_ID_LOCKER = 4;
    public static final int NOTIF_ID_EX_POINT = 5;
    public static final int NOTIF_ID_GET_POINT = 6;
    public static final int NOTIF_ID_INSTALL_FINISH = 7;
    public static final int NOTIF_ID_POINT_THEME = 8;
    public static final int NOTIF_ID_INSTALL_APP = 9;
    public static final int NOTIF_ID_LAUNCH_APP = 10;
    public static final int NOTIF_ID_DOWNLOAD_NOTINSTALL = 11;
    public static final int NOTIF_ID_BATTERY = 12;//power full
    
    public static void showNoti(Context context,int iconId,int titleId,int contentId,Intent i,int notifyId){
        Notification notification = new Notification(iconId,context.getString(contentId),System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        PendingIntent intent = PendingIntent.getActivity(context, notifyId/*0*/, i, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(context,context.getString(titleId), context.getString(contentId),intent);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.cancel(notifyId);
        nm.notify(notifyId, notification);
    }

    public static void showNoti(Context context,int iconId,String title,String content,Intent i,int notifyId){
        Notification notification = new Notification(iconId,content,System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        PendingIntent intent = PendingIntent.getActivity(context, notifyId/*0*/, i, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(context,title, content,intent);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.cancel(notifyId);
        nm.notify(notifyId, notification);
    }
    
    public static void cancleNoti(Context context,int notifyId){
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(notifyId);
    }
    
    public static void showInstallAppNotifi(Context context,String resid,String path){
        File file = new File(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
        String name = AppManager.getInstance(context).getAppNameByResId(resid);
        showNoti(context,MResource.getIdByName(context,"drawable","nq_download_notfi_icon"),
                name,
                MResource.getString(context,"nq_install_app_tip"),
                intent,
                NotificationUtil.NOTIF_ID_INSTALL_APP);
    }
    
    public static void showThemeNotifi(Context context,String resId){
        Intent i = new Intent();
        i.setAction(ThemeDetailActivity.INTENT_ACTION);
        i.putExtra(ThemeDetailActivity.KEY_THEME_ID,resId);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        showNoti(context,MResource.getIdByName(context,"drawable","nq_live"),
                MResource.getIdByName(context,"string","nq_theme_download_finish_tip"),
                MResource.getIdByName(context,"string","nq_theme_download_finish_tip"),
                i,
                NotificationUtil.NOTIF_ID_THEME);
    }

    public static void showPointThemeNofi(Context context,String resId){
        Intent intent = new Intent();
        intent.setAction(ThemeDetailActivity.INTENT_ACTION);
        intent.putExtra(ThemeDetailActivity.KEY_THEME_ID, resId);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String name = ThemeManager.getInstance(context).getThemeName(resId);

        showNoti(context,MResource.getIdByName(context,"drawable","nq_live"),
                MResource.getString(context, "nq_consume_succ_tip"),
                MResource.getString(context, "nq_consume_succ_msg",name),
                intent,
                NotificationUtil.NOTIF_ID_POINT_THEME);
    }
    
    public static void showWallpaperNotifi(Context context,String resId){
        Intent i = new Intent();
        i.setAction("com.nqmobile.live.WallpaperDetail");
        i.putExtra(WallpaperDetailActivity.KEY_WALLPAPER_ID,resId);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        showNoti(context,MResource.getIdByName(context,"drawable","nq_live"),
                MResource.getIdByName(context,"string","nq_theme_download_finish_tip"),
                MResource.getIdByName(context,"string","nq_theme_download_finish_tip"),
                i,
                NotificationUtil.NOTIF_ID_WALLPAPER);
    }

    public static void showLockerNotifi(Context context,String resId){
        Intent i = new Intent();
        i.setAction("com.lqlauncher.LocalLocker");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        showNoti(context,MResource.getIdByName(context,"drawable","nq_live"),
                MResource.getIdByName(context,"string","nq_theme_download_finish_tip"),
                MResource.getIdByName(context,"string","nq_theme_download_finish_tip"),
                i,
                NotificationUtil.NOTIF_ID_LOCKER);
    }
    
    public static void showLauncherAppNotifi(Context context,String packageName){
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String name = AppManager.getInstance(context).getAppNameByPackName(packageName);
        showNoti(context,MResource.getIdByName(context,"drawable","nq_download_notfi_icon"),
                name,
                MResource.getString(context,"nq_launch_app_tip"),
                intent,
                NotificationUtil.NOTIF_ID_LAUNCH_APP);
    }

}
