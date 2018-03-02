package com.nqmobile.livesdk.modules.locker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
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
import android.widget.TextView;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.modules.locker.model.LockerEngine;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;
import com.nqmobile.livesdk.utils.Tools;

public class LockerUpgradeActivity extends Activity implements OnClickListener {
	private static final ILogger NqLog = LoggerFactory.getLogger(LockerModule.MODULE_NAME);
	
	private Context mContext;
	private int mDisplayWidth;
	private int mDisplayHeight;
	private View mLockerUpgradeLayout;
	private View mLockerLoading;
	private View mLockerDetail;
	private TextView tvSize;
	private Button btnDownload;
	private Dialog dialog;
	public static final String KEY_LOCKER_ENGINE = "locker_engine";
	private Locker mLocker = null;
	private LockerEngine mEngine = null;
	private Handler mHandler;
	private static final int SHOW_EMPTY = 0;
	private static final int SHOW_LOADING = 1;
	private static final int SHOW_VIEW = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mContext = LockerUpgradeActivity.this;
		
		mLocker = (Locker) getIntent().getSerializableExtra(KEY_LOCKER_ENGINE);
		if(mLocker == null){
			finishWithToast(true, null);
			return;
		}
		initHandler();
		findViews();
		updateViews(SHOW_LOADING);

		LockerManager lmr = LockerManager.getInstance(mContext);
		lmr.getNewLockerEngine(mLocker.getIntEngineType(), mLocker.getIntMinVersion(), new BasicNewLockerEngineListener());
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
		mLockerUpgradeLayout = inflate.inflate(
				MResource.getIdByName(mContext, "layout", "nq_locker_engine_upgrade"),
				null);

		mLockerLoading = mLockerUpgradeLayout.findViewById(
				MResource.getIdByName(mContext, "id", "ll_loading"));
		mLockerDetail = mLockerUpgradeLayout.findViewById(
				MResource.getIdByName(mContext, "id", "ll_detail"));
		
		tvSize = (TextView) mLockerUpgradeLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "tv_size"));
		btnDownload = (Button) mLockerUpgradeLayout.findViewById(MResource
				.getIdByName(getApplication(), "id", "btn_download"));

		// updateViews(SHOW_LOADING);

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				mDisplayWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;

		dialog = new Dialog(mContext, MResource.getIdByName(mContext, "style",
				"translucent"));
		dialog.setContentView(mLockerUpgradeLayout, lp);
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

		final int index = type;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				switch (index) {
				case SHOW_EMPTY:
					mLockerDetail.setVisibility(View.VISIBLE);
					btnDownload.setVisibility(View.VISIBLE);
					mLockerLoading.setVisibility(View.GONE);
					tvSize.setText(MResource.getString(LockerUpgradeActivity.this, "string", "nq_locker_no_res"));
					btnDownload.setText(MResource.getString(LockerUpgradeActivity.this, "string", "nq_text_ok"));
					btnDownload.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							// 上传异常
							
							finishWithToast(true, null);
						}
					});
					
					break;
				case SHOW_LOADING:
					mLockerDetail.setVisibility(View.GONE);
					btnDownload.setVisibility(View.GONE);
					mLockerLoading.setVisibility(View.VISIBLE);
					break;
				case SHOW_VIEW:
					mLockerDetail.setVisibility(View.VISIBLE);
					btnDownload.setVisibility(View.VISIBLE);
					mLockerLoading.setVisibility(View.GONE);
					tvSize.setText(getResources().getString(
							MResource.getIdByName(LockerUpgradeActivity.this, "string", "nq_locker_upgrade"),
							StringUtil.formatSize(mEngine.getSize())));
					btnDownload.setOnClickListener(LockerUpgradeActivity.this);
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
		int downloadBtnId = MResource.getIdByName(mContext, "id",
				"btn_download");
		if (id == downloadBtnId) {
			if (!NetworkUtils.isConnected(this)) {
				ToastUtils.toast(this, "nq_nonetwork");
                return;
            }
			new UpgradeTask().execute("");
			finishWithToast(true, "nq_locker_start_download");
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

	/**从网络获取锁屏新版本引擎*/
	private class BasicNewLockerEngineListener implements NewLockerEngineListener{

		@Override
		public void onNoNetwork() {
			// TODO Auto-generated method stub
			ToastUtils.toast(mContext, "nq_nonetwork");
		}

		@Override
		public void onErr() {
			// TODO Auto-generated method stub
			updateViews(SHOW_EMPTY);
//			finishWithToast(true, "nq_locker_no_res");
		}

		@Override
		public void onGetNewLockerEngineSucc(LockerEngine le) {
			// TODO Auto-generated method stub
			if(le == null)
				finishWithToast(true, "nq_locker_no_res");
			mEngine = le;
			updateViews(SHOW_VIEW);
		}
		
	}

	class UpgradeTask extends AsyncTask<String, Void, Boolean> {
		private Exception exception;
		@Override
		protected Boolean doInBackground(String... urls) {
			// TODO Auto-generated method stub
			// 下载完成,并且调用接口,让锁屏SDK动态加载新下载的引擎apk文件
			boolean down = LockerManager.getInstance(mContext).upgradeLibFile(mLocker.getIntEngineType());
			
			return down;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			if(result){
				NqLog.i("locker engine upgrade finished new ver= ");
				finishWithToast(true, "nq_locker_finish_download");
			} else {
				NqLog.i("locker engine upgrade failed");
				finishWithToast(true, "nq_label_download_error");
			}
		}

	}
}
