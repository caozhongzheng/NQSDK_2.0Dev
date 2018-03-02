package com.nqmobile.livesdk.modules.update;

import java.io.File;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateUtils;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.net.UpdateListener;
import com.nqmobile.livesdk.commons.receiver.DownloadCompleteEvent;
import com.nqmobile.livesdk.commons.service.PeriodCheckEvent;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.modules.update.network.UpdateProtocol;
import com.nqmobile.livesdk.modules.update.network.UpdateServiceFactory;
import com.nqmobile.livesdk.utils.NetworkUtils;

/**
 * Created by Rainbow on 2014/11/20.
 */
public class UpdateManager extends AbsManager{

    // ===========================================================
    // Constants
    // ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(UpdateModule.MODULE_NAME);
    // ===========================================================
    // Fields
    // ===========================================================

    private static UpdateManager mInstance;
    private Context mContext;

    // ===========================================================
    // Constructors
    // ===========================================================

    private UpdateManager() {
        mContext = ApplicationContext.getContext();
        EventBus.getDefault().register(this);
    }

    public synchronized static UpdateManager getInstance() {
        if (mInstance == null) {
            mInstance = new UpdateManager();
        }
        return mInstance;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    public void checkUpdate(UpdateListener l) {
        UpdateServiceFactory.getService().checkUpdate(l);
    }

    public void downloadApp(boolean backDownload) {
        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        UpdatePreference helper = UpdatePreference.getInstance();

        //check is the back download running
        long backRefer = helper.getLongValue(UpdatePreference.KEY_BACK_DOWNLOAD_REFER);
        NqLog.i("backRefer=" + backRefer);
        if (backRefer != 0) {
            downloadManager.remove(backRefer);
            String updateName = helper.getStringValue(UpdatePreference.KEY_UPDATE_FILE_NAME);
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), updateName);
            if (file.exists()) {
                file.delete();
            }
        }

        NqLog.i("downloadUrl=" + helper.getStringValue(UpdatePreference.KEY_DOWNLOAD_URL));
        Uri uri = Uri.parse(helper.getStringValue(UpdatePreference.KEY_DOWNLOAD_URL));

        try {
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setMimeType("application/vnd.android.package-archive");

            //check the updatefile exists
            String updateName = helper.getStringValue(UpdatePreference.KEY_UPDATE_FILE_NAME);
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), updateName);
            if (file.exists()) {
                NqLog.i("file exists!");
                helper.setBooleanValue(UpdatePreference.KEY_DOWNLOAD_FINISH, true);
                return;
            }

            request.setDestinationUri(Uri.fromFile(file));

            if (backDownload) {
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            }

            request.setShowRunningNotification(!backDownload);
            long reference = downloadManager.enqueue(request);
            NqLog.i("reference=" + reference);

            if (backDownload) {
                helper.setBooleanValue(UpdatePreference.KEY_DOWNLOAD_FINISH, false);
                helper.setLongValue(UpdatePreference.KEY_BACK_DOWNLOAD_REFER, reference);
            } else {
                helper.setBooleanValue(UpdatePreference.KEY_DOWNLOAD_FINISH, false);
                helper.setLongValue(UpdatePreference.KEY_DOWNLOAD_REFER, reference);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void onEvent(UpdateProtocol.UpdateSuccessEvent event){
        ((UpdateListener)event.getTag()).getUpdateSucc(event.b);
    }

    public void onEvent(UpdateProtocol.UpdateFailEvent event){
        ((UpdateListener)event.getTag()).onErr();
    }

    public void onEvent(DownloadCompleteEvent event) {
        long reference = event.mRefer;
        UpdatePreference helper = UpdatePreference.getInstance();
        if (reference != -1) {
            long refer = helper.getLongValue(UpdatePreference.KEY_DOWNLOAD_REFER);
            long backrefer = helper.getLongValue(UpdatePreference.KEY_BACK_DOWNLOAD_REFER);
            if (reference == refer) {
                helper.setBooleanValue(UpdatePreference.KEY_DOWNLOAD_FINISH, true);
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        helper.getStringValue(UpdatePreference.KEY_UPDATE_FILE_NAME));

                //if is the fore download ,pop the install
                Uri uri = Uri.fromFile(file); //这里是APK路径
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setDataAndType(uri, "application/vnd.android.package-archive");
                ApplicationContext.getContext().startActivity(i);
                return;
            } else if (reference == backrefer) {
                //when backdownload finish,set the backrefer to 0
                helper.setBooleanValue(UpdatePreference.KEY_DOWNLOAD_FINISH, true);
                helper.setLongValue(UpdatePreference.KEY_BACK_DOWNLOAD_REFER, 0);
                return;
            }
        }
    }

    public void onEvent(PeriodCheckEvent event){
        long lastCheckUpdateTime = UpdatePreference.getInstance().getLongValue(UpdatePreference.KEY_LAST_CHECK_UPDATE);
        long now = SystemFacadeFactory.getSystem().currentTimeMillis();

        if(Math.abs(now - lastCheckUpdateTime) >= 7 * DateUtils.DAY_IN_MILLIS) {
            checkUpdate(new UpdateListener() {
                @Override
                public void getUpdateSucc(Bundle b) {
                    final boolean needUpdate = b.getBoolean(UpdateProtocol.NEED_UPDATE);

                    if(needUpdate){
                        if(NetworkUtils.isWifi(ApplicationContext.getContext())){
                            downloadApp(true);
                        }
                    }
                }

                @Override
                public void onErr() {

                }
            });
        }
    }

    @Override
    public void init() {
        EventBus.getDefault().register(this);
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
