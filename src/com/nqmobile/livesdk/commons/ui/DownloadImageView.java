package com.nqmobile.livesdk.commons.ui;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.commons.mydownloadmanager.IDownloadObserver;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppActionConstants;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.app.AppManager.Status;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PackageUtils;
import com.nqmobile.livesdk.utils.ToastUtils;
import com.nqmobile.livesdk.utils.Tools;

public class DownloadImageView extends ImageView implements OnClickListener, IDownloadObserver {
    private App mApp;
    private Status mStatus;
    private int pos;

    public DownloadImageView(Context context) {
        super(context);
    }

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public DownloadImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    public DownloadImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(App app) {
        if (app == null) {
            return;
        }

        mApp = app;
        updateViews();
       	setOnClickListener(this);
    }

    private void updateViews() {
    	new UpdateViewTask().execute(null, null, null);
    }

    private class UpdateViewTask extends AsyncTask<Void, Void, Void> {

    	Context context = getContext();
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
	        mStatus = AppManager.getInstance(context).getStatus(mApp);
	        NqLog.d(mApp.getStrName() + " doInBackground: statusCode=" + mStatus.statusCode + ",downloadedBytes=" + mStatus.downloadedBytes + ",totalBytes=" + mStatus.totalBytes
	        		+ " ," + DateFormat.getDateTimeInstance().format(new Date()));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			NqLog.d(mApp.getStrName() + " onPostExecute: statusCode=" + mStatus.statusCode + ",downloadedBytes=" + mStatus.downloadedBytes + ",totalBytes=" + mStatus.totalBytes
					+ " ," + DateFormat.getDateTimeInstance().format(new Date()));
			switch (mStatus.statusCode) {
            case AppManager.STATUS_UNKNOWN: //按STATUS_NONE处理
            case AppManager.STATUS_NONE:
           		setImageDrawable(MResource.getDrawable(context, "nq_store_app_download_x"));
                break;
            case AppManager.STATUS_DOWNLOADING:
                NqLog.d("status=STATUS_DOWNLOADING");
                //startAnimation(MResource.getAnimation(context, "nq_anim_app_downloading"));
                setImageDrawable(MResource.getDrawable(context, "nq_store_app_downloading_x"));
                break;
            case AppManager.STATUS_PAUSED:
                setImageDrawable(MResource.getDrawable(context, "nq_store_app_downloading_x"));
                break;
            case AppManager.STATUS_DOWNLOADED:
                setImageDrawable(MResource.getDrawable(context, "nq_store_app_install"));
                unregisterDownloadObserver();
                break;
            case AppManager.STATUS_INSTALLED:
                setImageDrawable(MResource.getDrawable(context, "nq_store_app_open"));
                break;
            default:
                break;
			}
		}
    	
    }
    
    public void registerDownloadObserver() {

    }

    public void unregisterDownloadObserver() {

    }

    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
    	if(Tools.isFastDoubleClick())
			return;
        int id = v.getId();
        Context context = getContext();
        int arg1 = 0;
        if (id == this.getId()) {
        	setEnabled(true);
            /**
             * 这里应该用上次获取到的mStatus，而不是从AppManager取最新的状态。比如刚打开详情页面，按钮显示为“打开应用”，
             * 用户通过PC卸载了这个应用
             * ，然后用户在这个界面点击该按钮，应该调用launchApp，只不过启动应用失败，而不是发现最新状态是STATUS_NONE
             * ，一点击就直接开始下载
             */
            switch (mStatus.statusCode) {
                case AppManager.STATUS_UNKNOWN://按STATUS_NONE处理
                case AppManager.STATUS_NONE:
                    if (!NetworkUtils.isConnected(context)) {
                        ToastUtils.toast(context, "nq_nonetwork");
                        return;
                    }

                    AppManager appManager = AppManager.getInstance(context);
                    Long downloadId = appManager.downloadApp(mApp);
                    if (downloadId != null){
                        appManager.registerDownloadObserver(mApp, this);
                        if(!(mApp.isGpApp())){
                        	ToastUtils.toast(context, "nq_start_download");
                        }
                    }
                    break;
                case AppManager.STATUS_DOWNLOADING:
                	setEnabled(false);
                    break;
                case AppManager.STATUS_PAUSED:
                	setEnabled(false);
                    break;
                case AppManager.STATUS_DOWNLOADED:
                	arg1 = 1;
                	PackageUtils.installApp(context, mApp.getStrAppPath());
                    break;
                case AppManager.STATUS_INSTALLED:
                	arg1 = 2;
                    PackageUtils.launchApp(context, mApp.getStrPackageName());
                    break;
                default:
                    break;
            }

            updateViews();

            StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
            		AppActionConstants.ACTION_LOG_1103, mApp.getStrId(), StatManager.ACTION_CLICK, 
					arg1 + "_" + getPos());
        }
    }

    /* (non-Javadoc)
     * @see com.nqmobile.live.store.logic.IDownloadObserver#onChange()
     */
    @Override
    public void onChange() {
        post(new Runnable() {
            @Override
            public void run() {
                updateViews();
            }
        });
    }
}
