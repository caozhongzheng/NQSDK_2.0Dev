/**
 * 
 */
package com.nqmobile.livesdk.modules.theme;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.mydownloadmanager.IDownloadObserver;
import com.nqmobile.livesdk.modules.app.AppActionConstants;
import com.nqmobile.livesdk.modules.defaultlauncher.DefaultLauncherPreference;
import com.nqmobile.livesdk.modules.points.*;
import com.nqmobile.livesdk.modules.points.model.ConsumePointsResp;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.modules.theme.ThemeManager.ThemeStatus;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;
import com.nqmobile.livesdk.utils.Tools;

/**
 * @author chenyanmin
 * @time 2014-1-23 上午9:46:43
 */
public class ThemeDetailFooter extends RelativeLayout implements OnClickListener,IDownloadObserver {
	private static final ILogger NqLog = LoggerFactory.getLogger(ThemeModule.MODULE_NAME);
	
	private ImageButton mIbDownload;
	private ProgressBar mPbProgress;
	private TextView mTvDownload;
	private Theme mTheme;
	private ThemeStatus mStatus;
    private ProgressDialog mWaitDialog;
    private ImageView mDownloadIcon;
    private static final int FLAG_POINT = 1;
    private Handler mHandler = new Handler();

	/**
	 * @param context
	 */
	public ThemeDetailFooter(Context context) {
		super(context);
	}
	/**
	 * @param context
	 * @param attrs
	 */
	public ThemeDetailFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public ThemeDetailFooter(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void init(Theme theme){
		if (theme == null){
			return;
		}
		
		mTheme = theme;
		findViews();
		updateViews();
		mIbDownload.setOnClickListener(this);		
	}

    private void showAppLoading(){
    	// update by ysongren for android.view.WindowManager$BadTokenException: Unable to add window -- token null is not for an application
        mWaitDialog = new ProgressDialog(((View)getParent()).getContext()); // getContext()
        mWaitDialog.setMessage(getContext().getString(MResource.getIdByName(getContext(), "string", "nq_label_loading")));
        mWaitDialog.setProgress(ProgressDialog.STYLE_HORIZONTAL);
        mWaitDialog.setCancelable(false);
        mWaitDialog.show();
    }

    private void dismissAppLoading(){
        mWaitDialog.dismiss();
    }
	
	private void findViews(){
		Context context = getContext();
		mIbDownload = (ImageButton) findViewById(MResource.getIdByName(context, "id", "ib_download"));
		mPbProgress = (ProgressBar) findViewById(MResource.getIdByName(context, "id", "pb_progress"));
		mTvDownload = (TextView) findViewById(MResource.getIdByName(context, "id", "tv_download"));
        mDownloadIcon = (ImageView) findViewById(MResource.getIdByName(context,"id","download_icon"));
	}

	int lastCode = ThemeManager.STATUS_UNKNOWN;
	private void updateViews() {
		Context context = getContext();
		mStatus = ThemeManager.getInstance(context).getStatus(mTheme);
		if(mStatus.statusCode != lastCode){
	        NqLog.d(mTheme.toString());
	        NqLog.d("statusCode=" + mStatus.statusCode + ",downloadedBytes=" + mStatus.downloadedBytes + ",totalBytes" + mStatus.totalBytes);
	        lastCode = mStatus.statusCode;
		}
        switch (mStatus.statusCode) {
		case ThemeManager.STATUS_UNKNOWN: //按STATUS_NONE处理
		case ThemeManager.STATUS_NONE:
			String apkSize = StringUtil.formatSize(mTheme.getLongSize());
            if(PreferenceDataHelper.getInstance(context).getBooleanValue(PointsPreference.KEY_POINT_CENTER_ENABLE)
                    && mTheme.getPointsflag() == PointsManager.POINT_FLAG_NOT_CONSUME){
                mTvDownload.setText(MResource.getString(context,"nq_theme_detail_foot",
                        String.valueOf(mTheme.getConsumePoints()),
                        apkSize));
            }else{
                mTvDownload.setText(MResource.getString(context,"nq_label_download_with_param", apkSize));
            }

			mIbDownload.setVisibility(View.VISIBLE);
			mPbProgress.setVisibility(View.GONE);
			break;
		case ThemeManager.STATUS_DOWNLOADING:
            NqLog.d("status=STATUS_DOWNLOADING");
            mTvDownload.setText(MResource.getString(context, "nq_label_downloading"));
			mIbDownload.setVisibility(View.GONE);
            mDownloadIcon.setVisibility(View.GONE);
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
		case ThemeManager.STATUS_PAUSED:
			mTvDownload.setText(MResource.getString(context, "nq_label_downloading"));
			mIbDownload.setVisibility(View.GONE);
            mDownloadIcon.setVisibility(View.GONE);
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
		case ThemeManager.STATUS_DOWNLOADED:
			mTvDownload.setText(MResource.getString(context, "nq_label_use"));
			mIbDownload.setVisibility(View.VISIBLE);
			mPbProgress.setVisibility(View.GONE);
            mDownloadIcon.setVisibility(View.GONE);
			unregisterDownloadObserver();
			break;
		case ThemeManager.STATUS_CURRENT_THEME:
			mTvDownload.setText(MResource.getString(context, "nq_detail_have_used"));
			mIbDownload.setVisibility(View.VISIBLE);
			mPbProgress.setVisibility(View.GONE);
            mDownloadIcon.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}
	
	public void registerDownloadObserver(){
		ThemeManager.getInstance(getContext()).registerDownloadObserver(mTheme, this);
	}
	
    public void unregisterDownloadObserver(){
    	ThemeManager.getInstance(getContext()).unregisterDownloadObserver(mTheme);
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if(Tools.isFastDoubleClick())
			return;
        int id = v.getId();
        // update by ysongren for getParent.Context
		final Context context = ((View) getParent()).getContext();
		int downloadBtnId = MResource.getIdByName(context, "id", "ib_download");
		if (id == downloadBtnId) {
			mIbDownload.setEnabled(true);
			/**
			 * 这里应该用上次获取到的mStatus，而不是从ThemeManager取最新的状态。比如刚打开详情页面，按钮显示为“打开应用”，
			 * 用户通过PC卸载了这个应用
			 * ，然后用户在这个界面点击该按钮，应该调用launchApp，只不过启动应用失败，而不是发现最新状态是STATUS_NONE
			 * ，一点击就直接开始下载
			 */
			switch (mStatus.statusCode) {
			case ThemeManager.STATUS_UNKNOWN://按STATUS_NONE处理
			case ThemeManager.STATUS_NONE:
                NqLog.i("ThemeFlag="+mTheme.getPointsflag());
                    if (!NetworkUtils.isConnected(context)) {
                    	ToastUtils.toast(context, "nq_nonetwork");
                        return;
                    }
                PointsPreference pre = PointsPreference.getInstance();
                boolean pointEnable = pre.getBooleanValue(PointsPreference.KEY_POINT_CENTER_ENABLE);
                if(mTheme.getPointsflag() == PointsManager.POINT_FLAG_NOT_CONSUME && pointEnable){
                    showAppLoading();
                    PointsManager.getInstance(context).consumePoints(mTheme.getStrId(),new ConsumePointsListener() {
                        @Override
                        public void onComsumeSucc(ConsumePointsResp resp) {
                            dismissAppLoading();
                            int code = resp.respCode;
                            NqLog.i("code="+code);
                            if(code == PointsManager.CONSUME_STATE_SUCC){
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                    	ToastUtils.toast(context, "nq_consume_succ");
                                        mIbDownload.setVisibility(View.GONE);
                                        mDownloadIcon.setVisibility(View.GONE);
                                        mPbProgress.setVisibility(View.VISIBLE);
                                        mPbProgress.setProgress(0);
                                    }
                                });

                                PointsManager.getInstance(context).flagPointThemeConsume(mTheme.getStrId());
                                downloadTheme(context);
                            }else if(code == PointsManager.CONSUME_STATE_ALREADY_HAVE){
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mIbDownload.setVisibility(View.GONE);
                                        mDownloadIcon.setVisibility(View.GONE);
                                        mPbProgress.setVisibility(View.VISIBLE);
                                        mPbProgress.setProgress(0);
                                        downloadTheme(context);
                                    }
                                });
                            }else{
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
										if(ClientInfo.isGP()){
											PointNotEnoughDialog dialog = new PointNotEnoughDialog(context,MResource.getIdByName(context, "style", "Translucent_NoTitle"));
											dialog.show();
										}else{
											PointAppDialog dialog = new PointAppDialog(context, MResource.getIdByName(context, "style", "Translucent_NoTitle"), mTheme.getStrId());
											dialog.show();
										}

                                        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,PointActionConstants.ACTION_LOG_2508,null,0,null);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onErr() {
                            dismissAppLoading();
                        }
                    });
                }else{
                    mIbDownload.setVisibility(View.GONE);
                    mDownloadIcon.setVisibility(View.GONE);
                    mPbProgress.setVisibility(View.VISIBLE);
                    mPbProgress.setProgress(0);
                    downloadTheme(context);
                }
				break;
			case ThemeManager.STATUS_DOWNLOADING:
				mIbDownload.setEnabled(false);
				break;
			case ThemeManager.STATUS_PAUSED:
				mIbDownload.setEnabled(false);
				break;
			case ThemeManager.STATUS_DOWNLOADED:
				ThemeManager.getInstance(context).applyTheme(mTheme);
                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,AppActionConstants.ACTION_LOG_1503, mTheme.getStrId(), 0, "1");
				break;
			case ThemeManager.STATUS_CURRENT_THEME:
				break;
			default:
				break;
			}
			
			updateViews();
		}
	}

    private void downloadTheme(Context context) {
        ThemeManager themeManager = ThemeManager.getInstance(context);
        Long downloadId = themeManager.downloadTheme(mTheme);
        if (downloadId != null)
            themeManager.registerDownloadObserver(mTheme, this);

        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,AppActionConstants.ACTION_LOG_1501, mTheme.getStrId(), 0,"1");
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
