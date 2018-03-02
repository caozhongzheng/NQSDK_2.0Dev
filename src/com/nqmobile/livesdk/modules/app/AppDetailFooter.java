/**
 *
 */
package com.nqmobile.livesdk.modules.app;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.mydownloadmanager.IDownloadObserver;
import com.nqmobile.livesdk.modules.app.AppManager.Status;
import com.nqmobile.livesdk.modules.points.PointsManager;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PackageUtils;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;
import com.nqmobile.livesdk.utils.Tools;

/**
 * @author chenyanmin
 * @time 2014-1-23 上午9:46:43
 */
public class AppDetailFooter extends RelativeLayout implements OnClickListener, IDownloadObserver {
	private static final ILogger NqLog = LoggerFactory.getLogger(AppModule.MODULE_NAME);

	private ImageButton mIbDownload;
    private ProgressBar mPbProgress;
    private TextView mTvDownload;
    private App mApp;
    private Status mStatus;


    /**
     * @param context
     */
    public AppDetailFooter(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public AppDetailFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public AppDetailFooter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(App app) {
        if (app == null) {
            return;
        }

        mApp = app;
        findViews();
        updateViews();
        mIbDownload.setOnClickListener(this);
    }

    private void findViews() {
        Context context = getContext();
        mIbDownload = (ImageButton) findViewById(MResource.getIdByName(context, "id", "ib_download"));
        mPbProgress = (ProgressBar) findViewById(MResource.getIdByName(context, "id", "pb_progress"));
        mTvDownload = (TextView) findViewById(MResource.getIdByName(context, "id", "tv_download"));
    }

    int lastCode = AppManager.STATUS_UNKNOWN;
    private void updateViews() {
        Context context = getContext();
        mStatus = AppManager.getInstance(context).getStatus(mApp);
        if(mStatus.statusCode != lastCode){
        	NqLog.d(mApp.toString());
            NqLog.d("statusCode=" + mStatus.statusCode + ",downloadedBytes=" + mStatus.downloadedBytes + ",totalBytes" + mStatus.totalBytes);
	        lastCode = mStatus.statusCode;
		}
        switch (mStatus.statusCode) {
            case AppManager.STATUS_UNKNOWN: //按STATUS_NONE处理
            case AppManager.STATUS_NONE:
                String apkSize = StringUtil.formatSize(mApp.getLongSize());
                mTvDownload.setText(MResource.getString(context,
                        "nq_label_download_with_param", apkSize));
                mIbDownload.setVisibility(View.VISIBLE);
                mPbProgress.setVisibility(View.GONE);
                break;
            case AppManager.STATUS_DOWNLOADING:
                NqLog.d("status=STATUS_DOWNLOADING");
                mTvDownload.setText(MResource.getString(context, "nq_label_downloading"));
                mIbDownload.setVisibility(View.GONE);
                mPbProgress.setVisibility(View.VISIBLE);
                if (mStatus.totalBytes > 0 && mStatus.downloadedBytes >= 0
                        && mStatus.downloadedBytes <= mStatus.totalBytes) {

                    int percentage = (int) ((0.01 + mStatus.downloadedBytes)
                            / mStatus.totalBytes * 100);
                    mTvDownload.setText(percentage + "%");
                    mPbProgress.setProgress(percentage);
                    if (percentage == 100) {
                    	ToastUtils.toast(context, "nq_label_download_success");
                    }
                } else {//pending状态
                    mPbProgress.setProgress(0);
                }
                break;
            case AppManager.STATUS_PAUSED:
                mTvDownload.setText(MResource.getString(context, "nq_label_downloading"));
                mIbDownload.setVisibility(View.GONE);
                mPbProgress.setVisibility(View.VISIBLE);
                if (mStatus.totalBytes > 0 && mStatus.downloadedBytes >= 0
                        && mStatus.downloadedBytes <= mStatus.totalBytes) {
                    int percentage = (int) ((0.01 + mStatus.downloadedBytes)
                            / mStatus.totalBytes * 100);
                    mPbProgress.setProgress(percentage);
                } else {//pending状态
                    mPbProgress.setProgress(0);
                }
                break;
            case AppManager.STATUS_DOWNLOADED:
                mTvDownload.setText(MResource.getString(context, "nq_install"));
                mIbDownload.setVisibility(View.VISIBLE);
                mPbProgress.setVisibility(View.GONE);
                break;
            case AppManager.STATUS_INSTALLED:
                mTvDownload.setText(MResource.getString(context, "nq_launch"));
                mIbDownload.setVisibility(View.VISIBLE);
                mPbProgress.setVisibility(View.GONE);
                unregisterDownloadObserver();
                break;
            default:
                break;
        }
    }

    public void registerDownloadObserver() {
        AppManager.getInstance(getContext()).registerDownloadObserver(mApp, this);
    }

    public void unregisterDownloadObserver() {
        AppManager.getInstance(getContext()).unregisterDownloadObserver(mApp);
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
        int downloadBtnId = MResource.getIdByName(context, "id", "ib_download");
        if (id == downloadBtnId) {
        	mIbDownload.setEnabled(true);
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
                    mIbDownload.setVisibility(View.GONE);
                    mPbProgress.setVisibility(View.VISIBLE);
                    mPbProgress.setProgress(0);

                    AppManager appManager = AppManager.getInstance(context);
                    Long downloadId = appManager.downloadApp(mApp);
                    if (downloadId != null){
                        appManager.registerDownloadObserver(mApp, this);
                        if(mApp.getRewardPoints() > 0 && !TextUtils.isEmpty(mApp.getTrackId())){
                            PointsManager.getInstance(context).flagPointResource(mApp,downloadId, "");
                        }
                    }

                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    		AppActionConstants.ACTION_LOG_1501, mApp.getStrId(), mApp.getStrId().startsWith("AD_")?1:0, 
        					"0");
                    break;
                case AppManager.STATUS_DOWNLOADING:
                	mIbDownload.setEnabled(false);
                    break;
                case AppManager.STATUS_PAUSED:
                	mIbDownload.setEnabled(false);
                    break;
                case AppManager.STATUS_DOWNLOADED:
                	PackageUtils.installApp(context, mApp.getStrAppPath());
                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    		AppActionConstants.ACTION_LOG_1503, mApp.getStrId(), 0, 
        					"0");
                    break;
                case AppManager.STATUS_INSTALLED:
                    NqLog.i("rewardPoints="+mApp.getRewardPoints()+" trackId="+mApp.getTrackId());
                    if(mApp.getRewardPoints() > 0 && !TextUtils.isEmpty(mApp.getTrackId())){
                        PointsManager.getInstance(context).launchApp(mApp);
                    }else{
                    	PackageUtils.launchApp(context, mApp.getStrPackageName());
                    }
                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    		AppActionConstants.ACTION_LOG_1504, mApp.getStrId(), 0, 
        					null);
                    break;
                default:
                    break;
            }

            updateViews();
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
