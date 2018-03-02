package com.nqmobile.livesdk.commons.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.commons.modal.ResItem;
import com.nqmobile.livesdk.commons.mydownloadmanager.IDownloadObserver;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.mustinstall.MustInstallActivity;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * Created by Rainbow on 14-6-8.
 */
@SuppressLint("NewApi")
public class DownloadStateView extends FrameLayout implements IDownloadObserver {
	private int mType;

    private ResItem mResItem;

    private Status mStatus;

    private int mLastStateCode = -1;

    private Context mContext;

    private CircleProgressDownloadView mProgressView;
    private View mMaskView;

    public static final int TYPE_APP = 0;
    public static final int TYPE_WALLPAPER = 1;
    public static final int TYPE_ICON = 2;
    public static final int TYPE_LIVEWALLPAPER = 3;

    private static final int STATE_CURRENT = 0;
    private static final int STATE_DOWNLOAD_COMPLETE = 1;
    private static final int STATE_NOT_DOWNLOAD = 2;
    private static final int STATE_DOWNLOADING = 3;

    public DownloadStateView(Context context) {
        super(context);
        mContext = context;
    }

    public DownloadStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public DownloadStateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }
    
    View mTopView;
    Handler mHandler;
    public void setResItem(App item, int type, Handler handler, View topView){
        mType = type;
        mResItem = item;
        mHandler = handler;
        mTopView = topView;
        findView();
        updateView(false);

        switch (type){
            case TYPE_APP:
            	AppManager.getInstance(getContext()).registerDownloadObserver((App) mResItem, this);
                break;
        }
    }

    private void findView(){
        mProgressView = (CircleProgressDownloadView) findViewById(MResource.getIdByName(mContext, "id", "progressView"));
        mMaskView = findViewById(MResource.getIdByName(mContext, "id", "downState"));
    }

    private void updateView(boolean fromObserver){
        mStatus = getStatus();
        if(mStatus.statusCode != mLastStateCode){
            mLastStateCode = mStatus.statusCode;
        }
        NqLog.d("statusCode=" + mStatus.statusCode + ",downloadedBytes=" + mStatus.downloadedBytes + 
        		",totalBytes" + mStatus.totalBytes + ", " + mResItem);
        switch (mStatus.statusCode){
            case STATE_CURRENT:
                mProgressView.setVisibility(View.GONE);
                mTopView.setVisibility(View.GONE);
                mMaskView.setVisibility(View.GONE);
                break;
            case STATE_DOWNLOAD_COMPLETE:
                mProgressView.setVisibility(View.GONE);
                mMaskView.setVisibility(View.GONE);
                switch (mType){
                    case TYPE_APP:
                    	AppManager appManager = AppManager.getInstance(getContext());
                    	appManager.unregisterDownloadObserver((App) mResItem);
                    	if(mHandler != null) mHandler.sendEmptyMessage(MustInstallActivity.ADAPTER_ALL_REFRESH);
                        break;
                }
                break;
            case AppManager.STATUS_UNKNOWN: //按STATUS_NONE处理
            case STATE_NOT_DOWNLOAD:
                mProgressView.setVisibility(View.GONE);
                mMaskView.setVisibility(View.GONE);
                mTopView.setVisibility(View.VISIBLE);
                break;
            case STATE_DOWNLOADING:
                mTopView.setVisibility(View.GONE);
                mProgressView.setVisibility(View.VISIBLE);
                mMaskView.setVisibility(View.VISIBLE);
                if (mStatus.totalBytes > 0 && mStatus.downloadedBytes >= 0
                        && mStatus.downloadedBytes <= mStatus.totalBytes) {
                    int percentage = (int) ((0.01 + mStatus.downloadedBytes)
                            / mStatus.totalBytes * 100);
                    NqLog.d("DownloadStateView percentage:" + percentage);
                    mProgressView.setProgress(percentage);
                    if(percentage == 100){
                    	ToastUtils.toast(getContext(), "nq_label_download_success");
                    }
				}else {
                    mProgressView.setProgress(0);
				}
                break;
        }
    }

    private Status getStatus(){
        Status s = new Status();
        switch (mType){
            case TYPE_APP:
                App app = (App) mResItem;
                AppManager.Status status = AppManager.getInstance(mContext).getStatus(app);
                switch (status.statusCode){
                    case AppManager.STATUS_UNKNOWN:
                    case AppManager.STATUS_NONE:
                        s.statusCode = STATE_NOT_DOWNLOAD;
                        break;
                    case AppManager.STATUS_DOWNLOADING:
                    case AppManager.STATUS_PAUSED:
                        s.statusCode = STATE_DOWNLOADING;
                        s.totalBytes = status.totalBytes;
                        s.downloadedBytes = status.downloadedBytes;
                        break;
                    case AppManager.STATUS_DOWNLOADED:
                        s.statusCode = STATE_DOWNLOAD_COMPLETE;
                        break;
                }
                break;
        }
        return s;
    }

    @Override
    public void onChange() {
        post(new Runnable() {
            @Override
            public void run() {
                updateView(true);
            }
        });
    }

   /* @Override
    public void onClick(View view) {
    	Long downloadId;
    	boolean apply = false;
        switch (mStatus.statusCode) {
            case STATE_CURRENT:
                break;
            case STATE_DOWNLOAD_COMPLETE:
            	 if(apply && mHandler != null) mHandler.sendEmptyMessage(MustInstallActivity.ADAPTER_ALL_REFRESH);
                break;
            case STATE_DOWNLOADING:
                break;
            case STATE_NOT_DOWNLOAD:
                switch (mType){
                    case TYPE_APP:
                    	AppManager appManager = AppManager.getInstance(mContext);
                        MyDownloadManager.getInstance(getContext()).registerDownloadObserverUri(((App) mResItem).getStrAppUrl(), this);
                        downloadId = appManager.downloadApp((App) mResItem);
                        break;
                }
                break;
        }
    }*/

    private class Status{
        public int statusCode;
        public long downloadedBytes;
        public long totalBytes;
    }
}
