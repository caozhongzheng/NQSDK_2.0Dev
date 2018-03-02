package com.nqmobile.livesdk.modules.locker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.ToastUtils;

public class LockerApplyActivity extends Activity {
	private static final ILogger NqLog = LoggerFactory.getLogger(LockerModule.MODULE_NAME);
	
	private Context mContext;
	private int mDisplayWidth;
	private Dialog dialog;
	public static final String KEY_LOCKER = "locker";
	public static final String KEY_APPLY = "apply";
	private Locker mLocker = null;
	private int apply = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = LockerApplyActivity.this;

		mLocker = (Locker) getIntent().getSerializableExtra(KEY_LOCKER);
		if (mLocker == null) {
			finishWithToast(true, null);
			return;
		}
		apply = getIntent().getIntExtra(KEY_APPLY, 0);
		findViews();
		
		new ApplyLockerTask().execute(apply);
	}

	private void findViews() {
		final DisplayMetrics display = mContext.getResources()
				.getDisplayMetrics();
		mDisplayWidth = display.widthPixels;
		LayoutInflater inflate = LayoutInflater.from(mContext);
		View v = inflate.inflate(
				MResource.getIdByName(mContext, "layout", "nq_locker_apply_activity"),
				null);

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				mDisplayWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;

		dialog = new Dialog(mContext, MResource.getIdByName(mContext, "style",
				"translucent"));
		dialog.setContentView(v, lp);
		dialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				return true;
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
	 * @param finish
	 * @param string
	 */
	private void finishWithToast(boolean finish, String toast) {
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
	
	class ApplyLockerTask extends AsyncTask<Integer, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Integer... types) {
			int apply = types[0];
			boolean result = false;
			if(apply == 0){
				result = LauncherSDK.getInstance(mContext).applyLocker(mLocker);
			} else if(apply == 1){
				result = LauncherSDK.getInstance(mContext).previewLocker(mLocker);
			}
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			NqLog.i("ApplyLockerTask locker apply " + (result ? "SUCCESS" : "FAILED"));
			finishWithToast(true, null);
		}

	}
}
