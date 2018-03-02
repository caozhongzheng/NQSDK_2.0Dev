/**
 * 
 */
package com.nqmobile.livesdk.modules.locker;

import android.content.Context;
import android.content.Intent;
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
import com.nqmobile.livesdk.modules.app.AppActionConstants;
import com.nqmobile.livesdk.modules.locker.LockerManager.LockerStatus;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;
import com.nqmobile.livesdk.utils.Tools;

/**
 * @author caozhongzheng
 * @time 2014/5/8 14:19:44
 */
public class LockerDetailFooter extends RelativeLayout implements OnClickListener,IDownloadObserver {
	private static final ILogger NqLog = LoggerFactory.getLogger(LockerModule.MODULE_NAME);

	private ImageButton mIbDownload;
	private ProgressBar mPbProgress;
	private TextView mTvDownload;
	private RelativeLayout mRelativeBtn;
	private ImageButton mIbApply;
	private ImageButton mIbPreview;
	private Locker mLocker;
	private LockerStatus mStatus;
	

	/**
	 * @param context
	 */
	public LockerDetailFooter(Context context) {
		super(context);
	}
	/**
	 * @param context
	 * @param attrs
	 */
	public LockerDetailFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public LockerDetailFooter(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void init(Locker locker){
		if (locker == null){
			return;
		}
		
		mLocker = locker;
		findViews();
		updateViews();
		mIbDownload.setOnClickListener(this);
		mIbApply.setOnClickListener(this);
		mIbPreview.setOnClickListener(this);
	}
	
	private void findViews(){
		Context context = getContext();
		mIbDownload = (ImageButton) findViewById(MResource.getIdByName(context, "id", "ib_download"));
		mPbProgress = (ProgressBar) findViewById(MResource.getIdByName(context, "id", "pb_progress"));
		mTvDownload = (TextView) findViewById(MResource.getIdByName(context, "id", "tv_download"));
		mRelativeBtn = (RelativeLayout) findViewById(MResource.getIdByName(context, "id", "rl_apply"));
		mIbApply = (ImageButton) findViewById(MResource.getIdByName(context, "id", "ib_apply"));
		mIbPreview = (ImageButton) findViewById(MResource.getIdByName(context, "id", "ib_preview"));
	}
	
	int lastCode = LockerManager.STATUS_UNKNOWN;
	private void updateViews() {
		Context context = getContext();
		mStatus = LockerManager.getInstance(context).getStatus(mLocker);
		if(mStatus.statusCode != lastCode){
	        NqLog.d("LockerDetailFooter:" + mLocker.toString());
	        NqLog.d("statusCode=" + converStatus(mStatus.statusCode) + ",downloadedBytes=" + mStatus.downloadedBytes + ",totalBytes=" + mStatus.totalBytes);
	        lastCode = mStatus.statusCode;
		}
        switch (mStatus.statusCode) {
		case LockerManager.STATUS_UNKNOWN: //按STATUS_NONE处理
		case LockerManager.STATUS_NONE:
			String apkSize = StringUtil.formatSize(mLocker.getLongSize());
			mTvDownload.setText(MResource.getString(context,
					"nq_label_download_with_param", apkSize));
			mTvDownload.setVisibility(View.VISIBLE);
			mIbDownload.setVisibility(View.VISIBLE);
			mPbProgress.setVisibility(View.GONE);
			mRelativeBtn.setVisibility(View.GONE);
			break;
		case LockerManager.STATUS_DOWNLOADING:
            NqLog.d("status=STATUS_DOWNLOADING");
            mTvDownload.setVisibility(View.VISIBLE);
            mTvDownload.setText(MResource.getString(context, "nq_label_downloading"));
			mIbDownload.setVisibility(View.GONE);
			mRelativeBtn.setVisibility(View.GONE);
			mPbProgress.setVisibility(View.VISIBLE);
			if (mStatus.totalBytes > 0 && mStatus.downloadedBytes >= 0
					&& mStatus.downloadedBytes <= mStatus.totalBytes) {
				int percentage = (int) Math.floor(mStatus.downloadedBytes * 100
						/ mStatus.totalBytes);
				mTvDownload.setText(percentage + "%");
				mPbProgress.setProgress(percentage);
				if (percentage == 100) {
					ToastUtils.toast(context, "nq_label_download_success");
				}
			} else {//pending状态
				mPbProgress.setProgress(0);
			}
			break;
		case LockerManager.STATUS_PAUSED:
			mTvDownload.setVisibility(View.VISIBLE);
			mTvDownload.setText(MResource.getString(context, "nq_label_downloading"));
			mIbDownload.setVisibility(View.GONE);
			mRelativeBtn.setVisibility(View.GONE);
			mPbProgress.setVisibility(View.VISIBLE);
			if (mStatus.totalBytes > 0 && mStatus.downloadedBytes >= 0
					&& mStatus.downloadedBytes <= mStatus.totalBytes) {
				int percentage = (int) Math.floor(mStatus.downloadedBytes * 100
						/ mStatus.totalBytes);
				mPbProgress.setProgress(percentage);
			} else {//pending状态
				mPbProgress.setProgress(0);
			}
			break;
		case LockerManager.STATUS_DOWNLOADED:
		case LockerManager.STATUS_CURRENT_LOCKER:
			mRelativeBtn.setVisibility(View.VISIBLE);
			mIbDownload.setVisibility(View.GONE);
			mPbProgress.setVisibility(View.GONE);
			mTvDownload.setVisibility(View.GONE);
			unregisterDownloadObserver();
			break;
		default:
			break;
		}
	}
	
	private String converStatus(int statusCode) {
		// TODO Auto-generated method stub
		String result = "";
		switch (statusCode) {
		case LockerManager.STATUS_NONE:
			result = "STATUS_NONE";
			break;
		case LockerManager.STATUS_DOWNLOADING:
			result = "STATUS_DOWNLOADING";
			break;
		case LockerManager.STATUS_PAUSED:
			result = "STATUS_PAUSED";
			break;
		case LockerManager.STATUS_DOWNLOADED:
			result = "STATUS_DOWNLOADED";
			break;
		case LockerManager.STATUS_CURRENT_LOCKER:
			result = "STATUS_CURRENT_LOCKER";
			break;

		default:
			result = "STATUS_UNKNOWN";
			break;
		}
		return result;
	}
	public void registerDownloadObserver(){
		LockerManager.getInstance(getContext()).registerDownloadObserver(mLocker, this);
	}
	
    public void unregisterDownloadObserver(){
    	LockerManager.getInstance(getContext()).unregisterDownloadObserver(mLocker);
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
		if (id == MResource.getIdByName(context, "id", "ib_download")) {
			mIbDownload.setEnabled(true);
			/**
			 * 这里应该用上次获取到的mStatus，而不是从LockerManager取最新的状态。比如刚打开详情页面，按钮显示为“打开应用”，
			 * 用户通过PC卸载了这个应用
			 * ，然后用户在这个界面点击该按钮，应该调用launchApp，只不过启动应用失败，而不是发现最新状态是STATUS_NONE
			 * ，一点击就直接开始下载
			 */
			switch (mStatus.statusCode) {
			case LockerManager.STATUS_UNKNOWN://按STATUS_NONE处理
			case LockerManager.STATUS_NONE:
                if (!NetworkUtils.isConnected(context)) {
                	ToastUtils.toast(context, "nq_nonetwork");
                    return;
                }
				mIbDownload.setVisibility(View.GONE);
				mPbProgress.setVisibility(View.VISIBLE);
				mPbProgress.setProgress(0);
				LockerManager lockerManager = LockerManager.getInstance(context);
				Long downloadId = lockerManager.downloadLocker(mLocker);
				if (downloadId != null)
					lockerManager.registerDownloadObserver(mLocker, this);

                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                		AppActionConstants.ACTION_LOG_1501, mLocker.getStrId(), 0, 
    					"3");
				break;
			case LockerManager.STATUS_DOWNLOADING:
				mIbDownload.setEnabled(false);
				break;
			case LockerManager.STATUS_PAUSED:
				mIbDownload.setEnabled(false);
				break;
			case LockerManager.STATUS_DOWNLOADED:
			case LockerManager.STATUS_CURRENT_LOCKER:
				break;
			default:
				break;
			}
			
			updateViews();
		} else if (id == MResource.getIdByName(context, "id", "ib_apply")) {
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
            		AppActionConstants.ACTION_LOG_1503, mLocker.getStrId(), 0, 
					"3");
			LockerManager lmr = LockerManager.getInstance(context);
			boolean verCoverd = lmr.checkEngineVersion(mLocker);
			if(verCoverd){
				// 应用锁屏
				processLockerApply(0);
//				lmr.applyLocker(mLocker);
			} else {
				processLockerEngineUpgrade();
			}

		} else if (id == MResource.getIdByName(context, "id", "ib_preview")) {
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
					LockerActionConstants.ACTION_LOG_2108, mLocker.getStrId(), mLocker.getStrId().startsWith("AD_")?1:0, 
					"3");
			LockerManager lmr = LockerManager.getInstance(context);
			boolean verCoverd = lmr.checkEngineVersion(mLocker);
			if(verCoverd){
				// 体验锁屏
				processLockerApply(1);
//				lmr.previewLocker(mLocker);
			} else {
				processLockerEngineUpgrade();
			}
		}
	}
	
	private void processLockerApply(int i) {
		// TODO Auto-generated method stub
		Context context = getContext();
		Intent intent = new Intent(context, LockerApplyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(LockerApplyActivity.KEY_LOCKER, mLocker);
        intent.putExtra(LockerApplyActivity.KEY_APPLY, i);
        context.startActivity(intent);
	}
	private void processLockerEngineUpgrade(){
		Context context = getContext();
		Intent intent = new Intent(context, LockerUpgradeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(LockerUpgradeActivity.KEY_LOCKER_ENGINE, mLocker);
        context.startActivity(intent);
	}

	/* (non-Javadoc)
	 * @see com.nqmobile.live.store.logic.IDownloadObserver#onChange()
	 */
	@Override
	public void onChange() {
		post(new Runnable(){
			@Override
			public void run() {
				updateViews();
			}
		});
	}
}
