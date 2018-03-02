package com.nqmobile.livesdk.modules.installedrecommend;

import java.util.Date;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.mydownloadmanager.MyDownloadManager;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppDetailListener;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.app.AppManager.Status;
import com.nqmobile.livesdk.modules.appstub.AppStubManager;
import com.nqmobile.livesdk.modules.font.FontManager;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.GpUtils;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PackageUtils;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;
import com.nqmobile.livesdk.utils.Tools;

public class InstalledRecommendDetailActivity extends BaseActvity implements OnClickListener, AppDetailListener {
	private static final ILogger NqLog = LoggerFactory.getLogger(InstalledRecommendModule.MODULE_NAME);

	public static final String INTENT_ACTION = "com.nqmobile.live.InstalledRecommendDetail";
    /**
     * bundle参数key，App对象
     */
    public static final String KEY_APP = "app";
    /**
     * bundle参数key，App资源id
     */
    public static final String KEY_APP_ID = "appId";

    public static final String KEY_RESULT = "packagename";
    
    private Context mContext;
	private View mAppStubDetailLayout;
	private FrameLayout mFramLayout;
	private LinearLayout iconLinearLayout;
	private RelativeLayout detailRelativeLayout;
	private AsyncImageView ivIcon;
	private TextView tvName;
	private RatingBar rbRate;
	private TextView tvSize;
	private TextView tvDowncount;
	private TextView tvIntro;
	private Button btnCancel;
	private Button btnDownload;
	private Dialog dialog;
	private App mApp = null;
	// 行为日志
	private Handler mHandler;
	private static final int SHOW_EMPTY = 0;
	private static final int SHOW_LOADING = 1;
	private static final int SHOW_VIEW = 2;
	private long startTime = 0l;
	private long endTime = 0l;
	private boolean mIsGpRes = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		startTime = new Date().getTime();

		useTime("startTime : " + startTime);
		mContext = InstalledRecommendDetailActivity.this;
		
		mApp = (App) getIntent().getSerializableExtra(KEY_APP);
		if(mApp == null){
			useTime("mApp = null : " + startTime);
			String appId = (String) getIntent().getSerializableExtra(KEY_APP_ID);
			if (!TextUtils.isEmpty(appId))
				mApp = AppStubManager.getInstance(mContext).getAppStubFromCache(appId);
			if(mApp == null) {
				AppManager.getInstance(this).getAppDetail(appId, this);
            } else {
                NqLog.e("appId is null in intent");
            }
		}
		boolean fini = false;
		if(mApp == null){
			useTime("mApp === null : " + startTime);
			fini = true;
		} else {
			int code = AppManager.getInstance(mContext).getStatus(mApp).statusCode;
			useTime(mApp.getStrName() + " code=: " + code + "/" + (code==3?"未安装，已下载":(code==4?"已安装":"?")));
			if(code == AppManager.STATUS_INSTALLED)
				fini = true;
			else if(code == AppManager.STATUS_DOWNLOADED){
				PackageUtils.installApp(mContext, mApp.getStrAppPath());
				fini = true;
			}
			if(mApp.isGpApp()){
				mIsGpRes = true;
			}
		}
		NqLog.e("mIsGpRes " + mIsGpRes);
		if(fini)
			finishWithToast(true, null);
		else{
			useTime("mApp != null : " + mApp);
			if (!mIsGpRes) {
				initHandler();
				findViews();
			}
		}
	}

	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateViews(SHOW_VIEW);
	}

	private void initHandler() {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
			}

		};
	}

	private void findViews() {
		// TODO Auto-generated method stub

		LayoutInflater inflate = LayoutInflater.from(mContext);
		mAppStubDetailLayout = inflate.inflate(
				MResource.getIdByName(mContext, "layout", "nq_appstub_detail"),
				null);

		mFramLayout = (FrameLayout) mAppStubDetailLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "fl_parent"));
		iconLinearLayout = (LinearLayout) mAppStubDetailLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "ll_icon"));
		detailRelativeLayout = (RelativeLayout) mAppStubDetailLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "rl_detail"));
		ivIcon = (AsyncImageView) mAppStubDetailLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "iv_icon"));
		tvName = (TextView) mAppStubDetailLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "tv_name"));
		rbRate = (RatingBar) mAppStubDetailLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "rb_rate"));
		tvDowncount = (TextView) mAppStubDetailLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "tv_downcount"));
		tvSize = (TextView) mAppStubDetailLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "tv_size"));
		tvIntro = (TextView) mAppStubDetailLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "tv_intro"));
		btnCancel = (Button) mAppStubDetailLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "btn_cancel"));
		btnDownload = (Button) mAppStubDetailLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "btn_download"));

		// updateViews(SHOW_LOADING);

		dialog = new Dialog(this, MResource.getIdByName(this,"style","translucent"));
		dialog.setContentView(mAppStubDetailLayout);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		dialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU
						|| keyCode == KeyEvent.KEYCODE_BACK) {
					finish();
				}
				return false;
			}
		});
		dialog.show();
		
	}

	/**
	 * @param type
	 *            0:empty 1:loading 2:showView
	 * */
	private void updateViews(int type) {
		// TODO Auto-generated method stub

		if(mApp == null)
			return;
		useTime("updateViews "
				+ (type == 1 ? "LOADING" : (type == 2 ? "SHOW" : "EMPTY")));

		NqLog.i("mApp:" + mApp);
		NqLog.i("mIsGpRes:" + mIsGpRes);
		if(mIsGpRes) {
			GpUtils.viewDetail(mContext, mApp.getStrAppUrl());
		} else {
			final int intdex = type;
			if(mHandler == null)
				initHandler();
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					switch (intdex) {
					case SHOW_EMPTY:
					case SHOW_LOADING:
						break;

					case SHOW_VIEW:
						ivIcon.loadImage(mApp.getStrIconPath(), mApp
								.getStrIconUrl(), null, MResource.getIdByName(
								getApplication(), "drawable", "nq_icon_default"));
						tvName.setText(mApp.getStrName());
						rbRate.setRating(mApp.getFloatRate());
						tvDowncount.setText(MResource.getString(getApplication(), "nq_stub_download_count", mApp.getLongDownloadCount()));
						tvSize.setText(MResource.getString(getApplication(), "nq_stub_size", StringUtil.formatSize(mApp.getLongSize())));
						tvIntro.setText(Html.fromHtml(mApp.getStrDescription()));

						endTime = new Date().getTime();
						NqLog.i("END used " + (endTime - startTime));
						mFramLayout.setOnClickListener(InstalledRecommendDetailActivity.this);
						iconLinearLayout.setOnClickListener(InstalledRecommendDetailActivity.this);
						detailRelativeLayout.setOnClickListener(InstalledRecommendDetailActivity.this);
						btnCancel.setOnClickListener(InstalledRecommendDetailActivity.this);
//						btnDownload.setEnabled(!isDownloading(mApp));
						btnDownload.setOnClickListener(InstalledRecommendDetailActivity.this);
						ivIcon.setOnClickListener(InstalledRecommendDetailActivity.this);
						int iconLayoutId = MResource.getIdByName(mContext, "id", "ll_icon");
						int detailLayoutId = MResource.getIdByName(mContext, "id", "rl_detail");
						break;

					default:
						break;
					}

				}
			});
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (Tools.isFastDoubleClick())
			return;
		        
		int id = v.getId();
		int frameId = MResource.getIdByName(mContext, "id", "fl_parent");
		int cancelBtnId = MResource.getIdByName(mContext, "id", "btn_cancel");
		int downloadBtnId = MResource.getIdByName(mContext, "id",
				"btn_download");
		int iconLayoutId = MResource.getIdByName(mContext, "id", "ll_icon");
		int detailLayoutId = MResource.getIdByName(mContext, "id", "rl_detail");
		if (id == cancelBtnId) {
			finishWithToast(true, null);
		} else if (id == downloadBtnId) {
			//记录用户点击下载的行为日志
	        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
	        		InstalledRecommendConstants.ACTION_LOG_2803, mApp.getStrId(), 1, mApp.getStrPackageName());
	        
			//发送result给主界面
	        Intent intent=new Intent();  
	        intent.putExtra(KEY_RESULT, mApp.getStrPackageName());  
	        InstalledRecommendDetailActivity.this.setResult(RESULT_OK, intent);
	        
			Status mStatus = AppManager.getInstance(mContext).getStatus(mApp);
			switch (mStatus.statusCode) {
			case AppManager.STATUS_UNKNOWN: // 按STATUS_NONE处理
			case AppManager.STATUS_NONE:
				downloadApp(AppManager.STATUS_NONE);
				finishWithToast(true, "nq_in_downloading");
				break;
			case AppManager.STATUS_DOWNLOADING:
				finishWithToast(true, "nq_in_downloading");
				break;
			case AppManager.STATUS_PAUSED:
				downloadApp(AppManager.STATUS_PAUSED);
				finishWithToast(true, null);
				break;
			case AppManager.STATUS_INSTALLED:
				break;
			case AppManager.STATUS_DOWNLOADED:
				PackageUtils.installApp(mContext, mApp.getStrAppPath());
				finishWithToast(true, null);
				break;
			default:
				break;
			}
			   
		} else if (id == frameId) {
			finishWithToast(true, null);
		}
		    
	}

	private void downloadApp(int downType) {
		// TODO Auto-generated method stub
		if (!NetworkUtils.isConnected(this)) {
			ToastUtils.toast(this, "nq_nonetwork");
            return;
        }

        if(NetworkUtils.isWifi(mContext)){
        	NqLog.i("App[Id=" + mApp.getStrId() + ",intSrcType=" + mApp.getIntSourceType()
    				+ ",Name=" + mApp.getStrName() 
    				+ ",IconUrl=" + mApp.getStrIconUrl()
    				+ ",ClickType=" + mApp.getIntClickActionType()
    				+ ",DownType=" + mApp.getIntDownloadActionType()
    				+ ",Pkg=" + mApp.getStrPackageName()
    				+ ",UpdateTime=" + mApp.getLongUpdateTime()
    				+ ",LocalTime=" + mApp.getLongLocalTime() + "]");
//        	Utility.getInstance(mContext).toast("nq_start_download");
        	Long downloadId = 0L;
        	mApp.setIntSourceType(InstalledRecommendConstants.APP_SOURCE_TYPE);
        	if(AppManager.STATUS_NONE == downType) {
            	downloadId = AppManager.getInstance(mContext).downloadApp(mApp);
        	} else if(AppManager.STATUS_PAUSED == downType) {
        		downloadId = MyDownloadManager.getInstance(mContext).getDownloadId(mApp.getStrAppUrl());
                if (downloadId == null) {
                	downloadId = MyDownloadManager.getInstance(mContext).getDownloadIdByResID(mApp.getStrId());
                }
        		MyDownloadManager.getInstance(mContext).resumeDownload(downloadId);
        	}
        } 
	}


	/**
	 * @param finish
	 * @param string
	 */
	protected void finishWithToast(boolean finish, String toast) {
		// TODO Auto-generated method stub
		if (toast != null && !toast.isEmpty()) {
			NqLog.i("toast string="+MResource.getString(mContext, toast)
					+",id="+MResource.getIdByName(mContext, "string", toast));
			ToastUtils.toast(mContext, toast);
		}

		if (finish) {
			if (dialog != null) {
				dialog.dismiss();
				dialog = null;
			}
			finish();
		}
	}

	private void useTime(String str) {
		long now = new Date().getTime();
		long used = now - startTime;
		NqLog.e(StringUtil.nullToEmpty(str) + " :used millsec: " + used);
	}


	@Override
	public void onErr() {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	ToastUtils.toast(InstalledRecommendDetailActivity.this, "nq_detail_no_data");
            }
        });
	}


	@Override
	public void onGetDetailSucc(App app) {
		mApp = app;
		if(mApp.isGpApp()){
			mIsGpRes = true;
		}
		runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	onResume();
            }
        });
	}
}
