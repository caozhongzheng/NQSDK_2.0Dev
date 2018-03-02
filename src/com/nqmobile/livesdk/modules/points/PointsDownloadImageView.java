package com.nqmobile.livesdk.modules.points;

import java.text.DateFormat;
import java.util.Date;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.mydownloadmanager.IDownloadObserver;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.app.AppManager.Status;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PackageUtils;
import com.nqmobile.livesdk.utils.ToastUtils;
import com.nqmobile.livesdk.utils.Tools;

public class PointsDownloadImageView extends LinearLayout implements OnClickListener, IDownloadObserver {
	private static final ILogger NqLog = LoggerFactory.getLogger(PointModule.MODULE_NAME);

	private App mApp;
    private Status mStatus;
    private int pos;
    private ImageView mStatusImage;
    private TextView mStatusTv;
    private Context mContext;

    public PointsDownloadImageView(Context context) {
        super(context);
        mContext = context;
        findView();
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public PointsDownloadImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public PointsDownloadImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    private void findView(){
        mStatusImage = (ImageView) findViewById(MResource.getIdByName(mContext, "id", "iv_download"));
        mStatusTv = (TextView) findViewById(MResource.getIdByName(mContext, "id", "state_tv"));
    }

    public void init(App app) {
        if (app == null) {
            return;
        }

        mApp = app;
        findView();
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
            mStatus = AppManager.getInstance(context).getStatus(mApp);
            NqLog.d(mApp.getStrName() + " doInBackground: statusCode=" + mStatus.statusCode + ",downloadedBytes=" + mStatus.downloadedBytes + ",totalBytes=" + mStatus.totalBytes
                    + " ," + DateFormat.getDateTimeInstance().format(new Date()));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            NqLog.d(mApp.getStrName() + " onPostExecute: statusCode=" + mStatus.statusCode + ",downloadedBytes=" + mStatus.downloadedBytes + ",totalBytes=" + mStatus.totalBytes
                    + " ," + DateFormat.getDateTimeInstance().format(new Date()));
            switch (mStatus.statusCode) {
                case AppManager.STATUS_UNKNOWN: //按STATUS_NONE处理
                case AppManager.STATUS_NONE:
                    mStatusImage.setImageDrawable(MResource.getDrawable(context, "nq_store_app_download_x"));
                    mStatusTv.setText(mContext.getString(MResource.getIdByName(mContext, "string", "nq_point_download"), mApp.getRewardPoints()));
                    break;
                case AppManager.STATUS_DOWNLOADING:
                    NqLog.d("status=STATUS_DOWNLOADING");
                    mStatusImage.setImageDrawable(MResource.getDrawable(context, "nq_store_app_downloading_x"));
                    mStatusTv.setText(MResource.getIdByName(mContext,"string","nq_point_downloading"));
                    break;
                case AppManager.STATUS_PAUSED:
                    mStatusImage.setImageDrawable(MResource.getDrawable(context, "nq_store_app_downloading_x"));
                    mStatusTv.setText(MResource.getIdByName(mContext,"string","nq_point_downloading"));
                    break;
                case AppManager.STATUS_DOWNLOADED:
                    mStatusImage.setImageDrawable(MResource.getDrawable(context, "nq_store_app_install"));
                    mStatusTv.setText(MResource.getIdByName(mContext, "string", "nq_point_install"));
                    break;
                case AppManager.STATUS_INSTALLED:
                    mStatusImage.setImageDrawable(MResource.getDrawable(context, "nq_store_app_open"));
                    mStatusTv.setText(MResource.getIdByName(mContext, "string", "nq_point_open"));
                    unregisterDownloadObserver();
                    break;
                default:
                    break;
            }
        }

    }

    public void registerDownloadObserver() {
        AppManager.getInstance(mContext).registerDownloadObserver(mApp, this);
    }

    public void unregisterDownloadObserver() {
        AppManager.getInstance(mContext).unregisterDownloadObserver(mApp);
    }

    @Override
    public void onClick(View v) {
        if(Tools.isFastDoubleClick())
            return;
        int id = v.getId();
        Context context = getContext();
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
                        ToastUtils.toast(context, "nq_start_download");
                    }

                    PointsManager.getInstance(mContext).flagPointResource(mApp,downloadId == null ? -1:downloadId, "");
                    StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,PointActionConstants.ACTION_LOG_2504,
                    		mApp.getStrId(), StatManager.ACTION_CLICK ,mApp.getStrPackageName());
                    break;
                case AppManager.STATUS_DOWNLOADING:
                    setEnabled(false);
                    break;
                case AppManager.STATUS_PAUSED:
                    setEnabled(false);
                    break;
                case AppManager.STATUS_DOWNLOADED:
                	PackageUtils.installApp(context, mApp.getStrAppPath());
                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,PointActionConstants.ACTION_LOG_2505,mApp.getStrId(),0,mApp.getStrPackageName());
                    break;
                case AppManager.STATUS_INSTALLED:
                    PointsManager.getInstance(mContext).launchApp(mApp);
                    if(!PointsPreference.getInstance().getBooleanValue(PointsPreference.KEY_FIRST_LAUNCH_POINT_TIP)) {
                    	PointsPreference.getInstance().setBooleanValue(PointsPreference.KEY_FIRST_LAUNCH_POINT_TIP, true);
                    	StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,PointActionConstants.ACTION_LOG_2506,mApp.getStrId(),5,mApp.getStrPackageName());
                    } else {
                    	StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,PointActionConstants.ACTION_LOG_2506,mApp.getStrId(),1,mApp.getStrPackageName());
                    }
                    break;
                default:
                    break;
            }

            updateViews();
        }
    }

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
