package com.nqmobile.livesdk.modules.appstub;

import java.util.Date;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.mydownloadmanager.MyDownloadManager;
import com.nqmobile.livesdk.commons.receiver.LiveReceiver;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.app.AppManager.Status;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PackageUtils;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;
import com.nqmobile.livesdk.utils.Tools;

public class NetworkAlertActivity extends BaseActvity implements OnClickListener {
	private static final ILogger NqLog = LoggerFactory.getLogger(AppStubModule.MODULE_NAME);
	
    public static final String INTENT_ACTION = "com.nqmobile.live.NetworkAlert";
    /**
     * bundle参数key，App对象
     */
    public static final String KEY_APP = "app";
    /**
     * bundle参数key，App资源id
     */
    public static final String KEY_APP_ID = "appId";

    private Context mContext;
	private int mDisplayWidth;
	private int mDisplayHeight;
	private View mFlowLayout;
	private RelativeLayout mParentLayout;
	private TextView tvName;
	private TextView tvSize;
	private TextView tvFlow;
	private RelativeLayout mRelativeLayoutIntro;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		startTime = new Date().getTime();

		useTime("startTime : " + startTime);
		mContext = NetworkAlertActivity.this;
		
		mApp = (App) getIntent().getSerializableExtra(KEY_APP);
		if(mApp == null){
			useTime("mApp = null : " + startTime);
			String appId = (String) getIntent().getSerializableExtra(KEY_APP_ID);
			if(appId != null)
				mApp = AppStubManager.getInstance(mContext).getAppStubFromCache(appId);
		}
		if(mApp == null){
			useTime("mApp === null : " + startTime);
			return;
		}
		
		useTime("mApp != null : " + mApp);
		initHandler();
		findViews();
		updateViews(SHOW_VIEW);
	}

	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
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
		final DisplayMetrics display = mContext.getResources()
				.getDisplayMetrics();
		mDisplayWidth = display.widthPixels;
		mDisplayHeight = display.heightPixels;

		LayoutInflater inflate = LayoutInflater.from(mContext);
		mFlowLayout = inflate.inflate(
				MResource.getIdByName(mContext, "layout", "nq_network"),
				null);

		mParentLayout = (RelativeLayout) mFlowLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "parent_layout"));
		tvFlow = (TextView) mFlowLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "tv_flow_header"));
		mRelativeLayoutIntro = (RelativeLayout) mFlowLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "rl_detail"));
		tvName = (TextView) mFlowLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "tv_detail_header"));
		tvSize = (TextView) mFlowLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "tv_size"));
		btnCancel = (Button) mFlowLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "btn_cancel"));
		btnDownload = (Button) mFlowLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "btn_download"));

		// updateViews(SHOW_LOADING);

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				mDisplayWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;

		dialog = new Dialog(mContext, MResource.getIdByName(mContext, "style",
				"translucent"));
		dialog.setContentView(mFlowLayout, lp);
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		dialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_MENU
						|| keyCode == KeyEvent.KEYCODE_BACK) {
					finish();
				}
				return false;
			}
		});
		Window window = dialog.getWindow();
		WindowManager.LayoutParams wmParams = window.getAttributes();
		wmParams.format = PixelFormat.RGBA_8888; // 设置图片格式，效果为背景透明
		wmParams.gravity = Gravity.CENTER_VERTICAL;
		// 设置悬浮窗口本身透明度
		wmParams.alpha = 1.0f;
		// 设置悬浮窗口长宽数据
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		dialog.onWindowAttributesChanged(wmParams);
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();

	}

	/**
	 * @param type
	 *            0:empty 1:loading 2:showView
	 * */
	private void updateViews(int type) {
		// TODO Auto-generated method stub

		useTime("updateViews "
				+ (type == 1 ? "LOADING" : (type == 2 ? "SHOW" : "EMPTY")));

		final int intdex = type;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				switch (intdex) {
				case SHOW_EMPTY:
				case SHOW_LOADING:
					break;

				case SHOW_VIEW:
					tvName.setText(MResource.getString(getApplication(), "nq_stub_flow_intro", mApp.getStrName()));

					tvSize.setText(MResource.getString(getApplication(), "nq_stub_size", StringUtil.formatSize(mApp.getLongSize())));

					endTime = new Date().getTime();
					NqLog.i("END used " + (endTime - startTime));
					btnCancel.setOnClickListener(NetworkAlertActivity.this);
					mParentLayout.setOnClickListener(NetworkAlertActivity.this);
					tvFlow.setOnClickListener(NetworkAlertActivity.this);
					mRelativeLayoutIntro.setOnClickListener(NetworkAlertActivity.this);
					AppManager.getInstance(mContext).getStatus(mApp);
					btnDownload.setEnabled(!isDownloading(mApp));
					btnDownload.setOnClickListener(NetworkAlertActivity.this);
					break;

				default:
					break;
				}

			}
		});

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (Tools.isFastDoubleClick())
			return;
		int id = v.getId();
		int cancelBtnId = MResource.getIdByName(mContext, "id", "btn_cancel");
		int downloadBtnId = MResource.getIdByName(mContext, "id",
				"btn_download");
//		int iconId = MResource.getIdByName(mContext, "id", "iv_icon");
		int parentId = MResource.getIdByName(mContext, "id", "parent_layout");
		int flowId = MResource.getIdByName(mContext, "id", "tv_flow_header");
		int introId = MResource.getIdByName(mContext, "id", "rl_detail");
		if (id == cancelBtnId) {
            StatManager.getInstance().onAction(
					StatManager.TYPE_STORE_ACTION,
					AppStubConstants.ACTION_LOG_2305, mApp.getStrId(), 0, "0");

			finishWithToast(true, null);
		} else if (id == downloadBtnId) {
            StatManager.getInstance().onAction(
					StatManager.TYPE_STORE_ACTION,
					AppStubConstants.ACTION_LOG_2304, mApp.getStrId(), mApp.getStrId().startsWith("AD_")?1:0, "0");
			Status mStatus = AppManager.getInstance(mContext).getStatus(mApp);
			switch (mStatus.statusCode) {
			case AppManager.STATUS_UNKNOWN: // 按STATUS_NONE处理
			case AppManager.STATUS_NONE:
				downloadAppStub(AppManager.STATUS_NONE);
				break;
			case AppManager.STATUS_DOWNLOADING:
				finishWithToast(true, "nq_in_downloading");
				break;
			case AppManager.STATUS_PAUSED:
				downloadAppStub(AppManager.STATUS_PAUSED);
				break;
			case AppManager.STATUS_INSTALLED:
				break;
			case AppManager.STATUS_DOWNLOADED:
				PackageUtils.installApp(mContext, mApp.getStrAppPath());
				break;
			default:
				break;
			}
			finishWithToast(true, null);
		} else if (id == flowId || id == introId) {
			// do nothing
		} else if (id == parentId) {
			finishWithToast(true, null);
//			AppManager.getInstance(mContext).viewAppDetail(-1, mApp);
		}
	}

	private void downloadAppStub(int downType) {
		// TODO Auto-generated method stub
		if (!NetworkUtils.isConnected(this)) {
			ToastUtils.toast(this, "nq_nonetwork");
            return;
        } else {
        	NqLog.i("App[Id=" + mApp.getStrId() + ",intSrcType=" + mApp.getIntSourceType()
    				+ ",Name=" + mApp.getStrName() 
    				+ ",IconUrl=" + mApp.getStrIconUrl()
    				+ ",ClickType=" + mApp.getIntClickActionType()
    				+ ",DownType=" + mApp.getIntDownloadActionType()
    				+ ",Pkg=" + mApp.getStrPackageName()
    				+ ",UpdateTime=" + mApp.getLongUpdateTime()
    				+ ",LocalTime=" + mApp.getLongLocalTime() + "]");
        	ToastUtils.toast(mContext, "nq_start_download");
        	Long downloadId = 0L;
        	if(AppManager.STATUS_NONE == downType) {
            	downloadId = AppManager.getInstance(mContext).downloadApp(mApp);
        	} else if(AppManager.STATUS_PAUSED == downType) {
        		downloadId = MyDownloadManager.getInstance(mContext).getDownloadId(mApp.getStrAppUrl());
                if (downloadId == null) {
                	downloadId = MyDownloadManager.getInstance(mContext).getDownloadIdByResID(mApp.getStrId());
                }
        		MyDownloadManager.getInstance(mContext).resumeDownload(downloadId);
        	}

            if(downloadId != null){
            	Intent i = new Intent();
                i.setAction(LiveReceiver.ACTION_APPSTUB_UPDATE);
                i.putExtra("stub_downid", downloadId);
                i.putExtra("stub_app", mApp);
                mContext.sendBroadcast(i);
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

	private boolean isDownloading(App app) {
		boolean result = false;
		Status mStatus = AppManager.getInstance(mContext).getStatus(app);
		switch (mStatus.statusCode) {
		case AppManager.STATUS_UNKNOWN: // 按STATUS_NONE处理
		case AppManager.STATUS_NONE:
			break;
		case AppManager.STATUS_DOWNLOADING:
		case AppManager.STATUS_PAUSED:
			result = true;
			break;
		case AppManager.STATUS_DOWNLOADED:
			break;
		case AppManager.STATUS_INSTALLED:
			break;
		default:
			break;
		}
		NqLog.d(app.getStrName() + " is " + (result ?" in Downloading" : " NOT in Downloading"));
		return result;
	}

	private void useTime(String str) {
		long now = new Date().getTime();
		long used = now - startTime;
		NqLog.e(StringUtil.nullToEmpty(str) + " :used millsec: " + used);
	}
}
