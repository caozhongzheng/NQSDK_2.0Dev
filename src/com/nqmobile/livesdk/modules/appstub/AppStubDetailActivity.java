package com.nqmobile.livesdk.modules.appstub;

import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.pm.ActivityInfo;
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

import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LogLevel;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.mydownloadmanager.MyDownloadManager;
import com.nqmobile.livesdk.commons.prefetch.event.CancelSilentDownloadEvent;
import com.nqmobile.livesdk.commons.receiver.LiveReceiver;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppDetailListener;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.app.AppManager.Status;
import com.nqmobile.livesdk.modules.appstubfolder.AppStubFolderConstants;
import com.nqmobile.livesdk.modules.categoryfolder.CategoryFolderConstants;
import com.nqmobile.livesdk.modules.font.FontManager;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.GpUtils;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PackageUtils;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;
import com.nqmobile.livesdk.utils.Tools;

@SuppressLint("HandlerLeak")
public class AppStubDetailActivity extends BaseActvity implements OnClickListener, AppDetailListener {
	private static final ILogger NqLog = LoggerFactory.getLogger(AppStubModule.MODULE_NAME);
   
	public static final String INTENT_ACTION = "com.nqmobile.live.AppStubDetail";
    public static final String KEY_APP = "app";
    public static final String KEY_APP_ID = "appId";
    public static final String KEY_FROM = "from";
    private static final String KEY_FROM_CATEGORY_FODLER = "from_category_folder";
    private static final String VALUE_CATEGORY_FODLER = "category_folder";
    
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
	boolean FINI = false;

	private boolean mFromAppStubFolder;
	private boolean mFromCategoryFolder;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		startTime = new Date().getTime();

		useTime(System.identityHashCode(this) + " onCreate "+getClass().getName()+ " startTime : " + startTime);
		mContext = AppStubDetailActivity.this;
		
		mApp = (App) getIntent().getSerializableExtra(KEY_APP);
		mFromAppStubFolder = "dashIconFolder".equals((String) getIntent().getSerializableExtra(KEY_FROM));
		mFromCategoryFolder = VALUE_CATEGORY_FODLER.equals(getIntent().getStringExtra(KEY_FROM_CATEGORY_FODLER));
		if(mApp == null){
			String appId = (String) getIntent().getSerializableExtra(KEY_APP_ID);
			useTime("mApp=null appId=" + appId + ", fromFolder=" + mFromAppStubFolder + ", " + startTime);
			if (!TextUtils.isEmpty(appId))
				mApp = AppStubManager.getInstance(mContext).getAppStubFromCache(appId);
			if(mApp == null && !TextUtils.isEmpty(appId)) {
				AppManager.getInstance(this).getAppDetail(appId, this);
            } else {
                NqLog.e("appId is null or getAppStub from cache");
            }
					}
		if(mApp == null){
			useTime("mApp === null finish: " + startTime);
			FINI = true;
		} else {
			int code = AppManager.getInstance(mContext).getStatus(mApp).statusCode;
			useTime(mApp.getStrName() + " code=: " + code + "/" + (code==3?"未安装，已下载":(code==4?"已安装":"?"))
					+ "/" + mApp.getStrAppPath());
			if(code == AppManager.STATUS_INSTALLED)
				FINI = true;
			else if(code == AppManager.STATUS_DOWNLOADED){
				PackageUtils.installApp(mContext, mApp.getStrAppPath());
				FINI = true;
			}
			if(mApp.isGpApp()){
				mIsGpRes = true;
				GpUtils.viewDetail(mContext, mApp.getStrAppUrl());
				if (mFromCategoryFolder) {
					StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
							CategoryFolderConstants.ACTION_LOG_2908,
							mApp.getStrId(), 0, mApp.getStrPackageName());
		            NqLog.i("ljc1234: ACTION_LOG_2908");
				} else {
					StatManager.getInstance().onAction(
							StatManager.TYPE_STORE_ACTION,
							mFromAppStubFolder ? AppStubFolderConstants.ACTION_LOG_2606
									: AppStubConstants.ACTION_LOG_2303,
							mApp.getStrId(), 0, mApp.getStrPackageName());
				}
				finish();
			}
		}
		if(FINI) {
			finishWithToast(true, null);
		} else {
			useTime("mApp != null : " + mApp);
			if (!mIsGpRes) {
				initHandler();
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		useTime(System.identityHashCode(this) + "onResume () "+getClass().getName()+" onResume : ");
		NqLog.d("onResume() FINI="+FINI);
		if(!FINI) {
			findViews();
			updateViews(SHOW_VIEW);
		}
	}

	private void initHandler() {
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
			}

		};
	}

	private void findViews() {
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
	}

	private void showDialog() {
		dialog = new Dialog(AppStubDetailActivity.this, MResource.getIdByName(this,"style","translucent"));
		dialog.setContentView(mAppStubDetailLayout);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				NqLog.d("dialog onDismiss");
				finish();
			}
		});
		dialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU
						|| keyCode == KeyEvent.KEYCODE_BACK) {
					finish();
					return true;
				}
				return false;
			}
		});
		dialog.show();
	}
    @Override
    public void finish() {
    	super.finish();
    	NqLog.d("finish()");
    }
	/**
	 * @param type
	 *            0:empty 1:loading 2:showView
	 * */
	private void updateViews(int type) {
		if(mApp == null)
			return;

		showDialog();
		useTime("updateViews "
				+ (type == 1 ? "LOADING" : (type == 2 ? "SHOW" : "EMPTY")));

		NqLog.i("App[Id=" + mApp.getStrId() + ",srcType=" + mApp.getIntSourceType()
				+ ",Name=" + mApp.getStrName() 
				+ ",ClickType=" + mApp.getIntClickActionType()
				+ ",DownType=" + mApp.getIntDownloadActionType()
				+ ",Pkg=" + mApp.getStrPackageName()
				+ ",isGp=" + mIsGpRes
				+ ",isFromFolder=" + mFromAppStubFolder
				+ ",Icon=" + mApp.getStrIconUrl()
				+ ",LocalTime=" + mApp.getLongLocalTime()
				+ ",UpdateTime=" + mApp.getLongUpdateTime() + "]");
		if(mIsGpRes) {
			GpUtils.viewDetail(mContext, mApp.getStrAppUrl());
		} else {
			final int intdex = type;
			if(mHandler == null) {
				initHandler();
			}
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
						mFramLayout.setOnClickListener(AppStubDetailActivity.this);
						iconLinearLayout.setOnClickListener(AppStubDetailActivity.this);
						detailRelativeLayout.setOnClickListener(AppStubDetailActivity.this);
						btnCancel.setOnClickListener(AppStubDetailActivity.this);
//						btnDownload.setEnabled(!isDownloading(mApp));
						btnDownload.setOnClickListener(AppStubDetailActivity.this);
						ivIcon.setOnClickListener(AppStubDetailActivity.this);
						int iconLayoutId = MResource.getIdByName(mContext, "id", "ll_icon");
						int detailLayoutId = MResource.getIdByName(mContext, "id", "rl_detail");
						break;

					default:
						break;
					}

				}
			});
		}
		if (mFromCategoryFolder) {
			StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
					CategoryFolderConstants.ACTION_LOG_2908,
					mApp.getStrId(), 0, mApp.getStrPackageName());
            NqLog.i("ljc1234: ACTION_LOG_2908");
		} else {
			StatManager.getInstance().onAction(
					StatManager.TYPE_STORE_ACTION,
					mFromAppStubFolder ? AppStubFolderConstants.ACTION_LOG_2606
							: AppStubConstants.ACTION_LOG_2303,
					mApp.getStrId(), 0, mApp.getStrPackageName());
		}
		if(mIsGpRes) {
			finish();
		}
	}

	@Override
	public void onClick(View v) {
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
			if (mFromCategoryFolder) {
	            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
	            		CategoryFolderConstants.ACTION_LOG_2910,
						mApp.getStrId(), 0, mApp.getStrPackageName());
	            NqLog.i("ljc1234: ACTION_LOG_2910");
			} else {
	            StatManager.getInstance().onAction(
						StatManager.TYPE_STORE_ACTION,
						mFromAppStubFolder ? AppStubFolderConstants.ACTION_LOG_2608
								: AppStubConstants.ACTION_LOG_2305,
						mApp.getStrId(), 0, mApp.getStrPackageName());
			}

			finishWithToast(true, null);
		} else if (id == downloadBtnId) {
			if (mFromCategoryFolder) {
	            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
	            		CategoryFolderConstants.ACTION_LOG_2909,
						mApp.getStrId(), 0, mApp.getStrPackageName());
	            NqLog.i("ljc1234: ACTION_LOG_2909");
			} else {
	            StatManager.getInstance().onAction(
						StatManager.TYPE_STORE_ACTION,
						mFromAppStubFolder ? AppStubFolderConstants.ACTION_LOG_2607
								: AppStubConstants.ACTION_LOG_2304,
						mApp.getStrId(), StatManager.ACTION_CLICK, mApp.getStrPackageName());
			}
            EventBus.getDefault().post(new CancelSilentDownloadEvent(mApp.getStrId()));

            Status mStatus = AppManager.getInstance(mContext).getStatus(mApp);
			switch (mStatus.statusCode) {
			case AppManager.STATUS_UNKNOWN: // 按STATUS_NONE处理
			case AppManager.STATUS_NONE:
				downloadAppStub(AppManager.STATUS_NONE);
				finishWithToast(true, null);
				break;
			case AppManager.STATUS_DOWNLOADING:
				finishWithToast(true, "nq_in_downloading");
				break;
			case AppManager.STATUS_PAUSED:
				downloadAppStub(AppManager.STATUS_PAUSED);
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
			
//		} else if (id == layoutId) {
			// AppManager.getInstance(mContext).viewAppDetail(-1, mApp);
		} else if (id == detailLayoutId || id == iconLayoutId) {
//			AppManager.getInstance(mContext).viewAppDetail(-1, mApp);
		} else if (id == frameId) {
			finishWithToast(true, null);
		}
	}

	private void downloadAppStub(int downType) {
		if (!NetworkUtils.isConnected(this)) {
			ToastUtils.toast(this, "nq_nonetwork");
            return;
        }

        if(NetworkUtils.isWifi(mContext)){
        	if(!mIsGpRes) {
        		ToastUtils.toast(mContext, "nq_start_download");
        	}
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
        } else {
        	Intent intent = new Intent();
        	intent.setClass(mContext, NetworkAlertActivity.class);
    		intent.setAction(NetworkAlertActivity.INTENT_ACTION);
    		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        	intent.putExtra(NetworkAlertActivity.KEY_APP, mApp);
			intent.putExtra(NetworkAlertActivity.KEY_APP_ID, mApp.getStrId());
			mContext.startActivity(intent);
        }
	}

	protected void finishWithToast(boolean finish, String toast) {
		NqLog.d("finishWithToast:finish=" + finish + ", toast=" + toast);
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
		runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	ToastUtils.toast(AppStubDetailActivity.this, "nq_detail_no_data");
            }
        });
	}


	@Override
	public void onGetDetailSucc(App app) {
		mApp = app;
		if(mApp.isGpApp()){
			mIsGpRes = true;
		}
		FINI = false;
		runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	NqLog.i("onGetDetailSucc " + mApp);
            	onResume();
            }
        });
	}
}
