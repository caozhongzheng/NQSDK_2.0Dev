package com.nqmobile.livesdk.modules.font;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
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
import com.nqmobile.livesdk.modules.font.FontManager.FontStatus;
import com.nqmobile.livesdk.modules.points.ConsumePointsListener;
import com.nqmobile.livesdk.modules.points.PointActionConstants;
import com.nqmobile.livesdk.modules.points.PointAppDialog;
import com.nqmobile.livesdk.modules.points.PointNotEnoughDialog;
import com.nqmobile.livesdk.modules.points.PointsManager;
import com.nqmobile.livesdk.modules.points.PointsPreference;
import com.nqmobile.livesdk.modules.points.model.ConsumePointsResp;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;
import com.nqmobile.livesdk.utils.Tools;

public class FontDetailFooter  extends RelativeLayout implements OnClickListener,IDownloadObserver {
	private static final ILogger NqLog = LoggerFactory.getLogger(FontModule.MODULE_NAME);
	
	private ImageButton mIbDownload;
	private ProgressBar mPbProgress;
	private TextView mTvDownload;
	private NqFont mFont;
	private FontStatus mStatus;
    private ProgressDialog mWaitDialog;
    private ImageView mDownloadIcon;
    private static final int FLAG_POINT = 1;
    private Handler mHandler = new Handler();

	/**
	 * @param context
	 */
	public FontDetailFooter(Context context) {
		super(context);
	}
	/**
	 * @param context
	 * @param attrs
	 */
	public FontDetailFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public FontDetailFooter(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void init(NqFont font){
		if (font == null){
			return;
		}
		
		mFont = font;
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

	int lastCode = FontManager.STATUS_UNKNOWN;
	private void updateViews() {
		Context context = getContext();
		mStatus = FontManager.getInstance(context).getStatus(mFont);
		if(mStatus.statusCode != lastCode){
	        NqLog.d(mFont.toString());
	        NqLog.d("statusCode=" + mStatus.statusCode + ",downloadedBytes=" + mStatus.downloadedBytes + ",totalBytes" + mStatus.totalBytes);
	        lastCode = mStatus.statusCode;
		}
		//缺省字体直接可以应用
		if ( TextUtils.equals(mFont.getStrId(),FontManager.getInstance(getContext()).DEFAULT_FONT_ID)){
			mStatus.statusCode = FontManager.STATUS_DOWNLOADED;
		}
        switch (mStatus.statusCode) {
		case FontManager.STATUS_UNKNOWN: //按STATUS_NONE处理
		case FontManager.STATUS_NONE:
			String apkSize = StringUtil.formatSize(mFont.getLongSize());

			NqLog.i("fix-me: keep eyes on this during debuging.");
            mTvDownload.setText(MResource.getString(context,"nq_label_download_with_param", apkSize));
			mIbDownload.setVisibility(View.VISIBLE);
			mPbProgress.setVisibility(View.GONE);
			break;
		case FontManager.STATUS_DOWNLOADING:
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
		case FontManager.STATUS_PAUSED:
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
		case FontManager.STATUS_DOWNLOADED:
			mTvDownload.setText(MResource.getString(context, "nq_label_use"));
			mIbDownload.setVisibility(View.VISIBLE);
			mPbProgress.setVisibility(View.GONE);
            mDownloadIcon.setVisibility(View.GONE);
			unregisterDownloadObserver();
			break;
		case FontManager.STATUS_CURRENT_FONT:
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
		FontManager.getInstance(getContext()).registerDownloadObserver(mFont, this);
	}
	
    public void unregisterDownloadObserver(){
    	FontManager.getInstance(getContext()).unregisterDownloadObserver(mFont);
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
			case FontManager.STATUS_UNKNOWN://按STATUS_NONE处理
			case FontManager.STATUS_NONE:
                NqLog.i("ThemeFlag="+"status_none");
                    if (!NetworkUtils.isConnected(context)) {
                    	ToastUtils.toast(context, "nq_nonetwork");
                        return;
                    }
                    mIbDownload.setVisibility(View.GONE);
                    mDownloadIcon.setVisibility(View.GONE);
                    mPbProgress.setVisibility(View.VISIBLE);
                    mPbProgress.setProgress(0);
                    downloadFont(context);
				break;
			case FontManager.STATUS_DOWNLOADING:
				mIbDownload.setEnabled(false);
				break;
			case FontManager.STATUS_PAUSED:
				mIbDownload.setEnabled(false);
				break;
			case FontManager.STATUS_DOWNLOADED:
                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,AppActionConstants.ACTION_LOG_1503, mFont.getStrId(), 0, "1");
				if(!FontManager.getInstance(context).applyFont(mFont)){
					ToastUtils.toast(context, "nq_label_apply_font_fail");
				}
				break;
			case FontManager.STATUS_CURRENT_FONT:
				break;
			default:
				break;
			}
			
			updateViews();
		}
	}

	
    private void downloadFont(Context context) {
    	FontManager fontManager = FontManager.getInstance(context);
        Long downloadId = fontManager.downloadFont(mFont);
        if (downloadId != null)
        	fontManager.registerDownloadObserver(mFont, this);

        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,AppActionConstants.ACTION_LOG_1501, mFont.getStrId(), 0,"4");
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
