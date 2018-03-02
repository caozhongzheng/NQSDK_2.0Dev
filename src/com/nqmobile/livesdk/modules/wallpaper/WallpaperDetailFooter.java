/**
 * 
 */
package com.nqmobile.livesdk.modules.wallpaper;

import android.content.Context;
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
import com.nqmobile.livesdk.modules.locker.LockerManager;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperManager.WallpaperStatus;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;
import com.nqmobile.livesdk.utils.Tools;

/**
 * @author chenyanmin
 * @time 2014-1-23 上午9:46:43
 */
public class WallpaperDetailFooter extends RelativeLayout implements OnClickListener,IDownloadObserver {
	private static final ILogger NqLog = LoggerFactory.getLogger(WallpaperModule.MODULE_NAME);
	
	private ImageButton mIbDownload;
	private ProgressBar mPbProgress;
	private TextView mTvDownload;
	private Wallpaper mWallpaper;
	private WallpaperStatus mStatus;
	

	/**
	 * @param context
	 */
	public WallpaperDetailFooter(Context context) {
		super(context);
	}
	/**
	 * @param context
	 * @param attrs
	 */
	public WallpaperDetailFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public WallpaperDetailFooter(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void init(Wallpaper wallpaper){
		if (wallpaper == null){
			return;
		}
		
		mWallpaper = wallpaper;
		findViews();
		updateViews();
		mIbDownload.setOnClickListener(this);		
	}
	
	private void findViews(){
		Context context = getContext();
		mIbDownload = (ImageButton) findViewById(MResource.getIdByName(context, "id", "ib_download"));
		mPbProgress = (ProgressBar) findViewById(MResource.getIdByName(context, "id", "pb_progress"));
		mTvDownload = (TextView) findViewById(MResource.getIdByName(context, "id", "tv_download"));
	}
	
	int lastCode = LockerManager.STATUS_UNKNOWN;
	private void updateViews() {
		Context context = getContext();
		mStatus = WallpaperManager.getInstance(context).getStatus(mWallpaper);
		if(mStatus.statusCode != lastCode){
			NqLog.d(mWallpaper.toString());
	        NqLog.d("statusCode=" + mStatus.statusCode + ",downloadedBytes=" + mStatus.downloadedBytes + ",totalBytes" + mStatus.totalBytes);
	        lastCode = mStatus.statusCode;
		}
        switch (mStatus.statusCode) {
		case WallpaperManager.STATUS_UNKNOWN: //按STATUS_NONE处理
		case WallpaperManager.STATUS_NONE:
			String apkSize = StringUtil.formatSize(mWallpaper.getLongSize());
			mTvDownload.setText(MResource.getString(context,
					"nq_label_download_with_param", apkSize));
			mIbDownload.setVisibility(View.VISIBLE);
			mPbProgress.setVisibility(View.GONE);
			break;
		case WallpaperManager.STATUS_DOWNLOADING:
            NqLog.d("status=STATUS_DOWNLOADING");
            mTvDownload.setText(MResource.getString(context, "nq_label_downloading"));
			mIbDownload.setVisibility(View.GONE);
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
		case WallpaperManager.STATUS_PAUSED:
			mTvDownload.setText(MResource.getString(context, "nq_label_downloading"));
			mIbDownload.setVisibility(View.GONE);
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
		case WallpaperManager.STATUS_DOWNLOADED:
			mTvDownload.setText(MResource.getString(context, "nq_label_use"));
			mIbDownload.setVisibility(View.VISIBLE);
			mPbProgress.setVisibility(View.GONE);
			unregisterDownloadObserver();
			break;
		case WallpaperManager.STATUS_CURRENT_WALLPAPER:
			mTvDownload.setText(MResource.getString(context, "nq_label_use"));
			mIbDownload.setVisibility(View.VISIBLE);
			mPbProgress.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}
	
	public void registerDownloadObserver(){
		WallpaperManager.getInstance(getContext()).registerDownloadObserver(mWallpaper, this);
	}
	
    public void unregisterDownloadObserver(){
    	WallpaperManager.getInstance(getContext()).unregisterDownloadObserver(mWallpaper);
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
			 * 这里应该用上次获取到的mStatus，而不是从WallpaperManager取最新的状态。比如刚打开详情页面，按钮显示为“打开应用”，
			 * 用户通过PC卸载了这个应用
			 * ，然后用户在这个界面点击该按钮，应该调用launchApp，只不过启动应用失败，而不是发现最新状态是STATUS_NONE
			 * ，一点击就直接开始下载
			 */
			switch (mStatus.statusCode) {
			case WallpaperManager.STATUS_UNKNOWN://按STATUS_NONE处理
			case WallpaperManager.STATUS_NONE:
                if (!NetworkUtils.isConnected(context)) {
                	ToastUtils.toast(context, "nq_nonetwork");
                    return;
                }
				mIbDownload.setVisibility(View.GONE);
				mPbProgress.setVisibility(View.VISIBLE);
				mPbProgress.setProgress(0);
				WallpaperManager wallpaperMgr = WallpaperManager.getInstance(context);
				Long downloadId = wallpaperMgr.downloadWallpaper(mWallpaper);
				if (downloadId != null)
					wallpaperMgr.registerDownloadObserver(mWallpaper, this);

                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                		AppActionConstants.ACTION_LOG_1501, mWallpaper.getStrId(), 0, 
    					"2");
				break;
			case WallpaperManager.STATUS_DOWNLOADING:
				mIbDownload.setEnabled(false);
				break;
			case WallpaperManager.STATUS_PAUSED:
				mIbDownload.setEnabled(false);
				break;
			case WallpaperManager.STATUS_DOWNLOADED:
			case WallpaperManager.STATUS_CURRENT_WALLPAPER://备注：用户可以重复设置壁纸。防止用户通过其他途径设置壁纸，而我们程序不知道壁纸已经改变
				WallpaperManager wallpaperMgr2 = WallpaperManager.getInstance(context);
				if(wallpaperMgr2.isWP_APPLYING())
					return;
				wallpaperMgr2.applyWallpaper(mWallpaper,true);
                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                		AppActionConstants.ACTION_LOG_1503, mWallpaper.getStrId(), 0, 
    					"2");
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
		post(new Runnable(){
			@Override
			public void run() {
				updateViews();
			}
		});		
	}
}
