package com.nqmobile.livesdk.modules.browserbandge;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.Tools;

public class DialogUtils {
	private static final ILogger NqLog = LoggerFactory.getLogger(BrowserBandgeModule.MODULE_NAME);

	public static long nativeLastRecordDialogTime = 0;
	private Context mContext;
	private Dialog mDialog;
	private ImageView mCheckMark;
	private String mResId = "";
	private boolean isChecked = false;
	
	private Intent mIntent;

	private BrowserBandgePreference mPreference;

	
	public DialogUtils(Context activityContext, Intent intent){
		mContext = activityContext;
		mIntent = intent;
		mResId = intent.getStringExtra(BandgeManager.KEY_RES_ID);
		mPreference = BrowserBandgePreference.getInstance();
	}
	
	public boolean isShowDialog(){
		// 过滤是否为常驻URL
		boolean isShow = false;
		
		boolean clickedOk = mPreference.getRecordDialogOk();
		NqLog.d("isClickOk:" + clickedOk);
		if(clickedOk){
			return false;
		}
		
		String url = mIntent.getStringExtra(BandgeManager.KEY_URL);
		String urlLong = mIntent.getStringExtra(BandgeManager.KEY_URL_LONG);
		boolean isOverride = mIntent.getBooleanExtra(BandgeManager.KEY_OVERIDE, false);
		NqLog.d("isOverride:" + isOverride + " url:" + url + " urlLong:" + urlLong);
		if (isOverride && !Tools.isEmpty(urlLong) && (Tools.isEmpty(url) || Tools.stringEquals(url, urlLong))) {
			long now = SystemFacadeFactory.getSystem().currentTimeMillis();
			long lastRecordDialogTime = mPreference.getRecordDialogTime();
			boolean isCheckedThreeDayTips = mPreference.isRecordChecked();
			//选择了为3天，否则为1天， 提示
			int day = isCheckedThreeDayTips ? 3 : 1;
			if (nativeLastRecordDialogTime == 0 || Math.abs(now - lastRecordDialogTime) >= day * DateUtils.DAY_IN_MILLIS) {
				isShow = true;
			}
			NqLog.d(isShow + " lastRecordDialogTime:" + lastRecordDialogTime + " isCheckedThreeDayTips:" + 
						isCheckedThreeDayTips);
		}
		
		return isShow;
	}
	
	public void showConsumeDialog() {
		((Activity)mContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				View view = LayoutInflater.from(mContext).inflate(MResource.getIdByName(mContext, "layout", "nq_consume_data_dialog"), null);
				mCheckMark = (ImageView) view.findViewById(MResource.getIdByName(mContext, "id", "check_mark"));
				mCheckMark.setOnClickListener(clickListener);
				view.findViewById(MResource.getIdByName(mContext, "id", "cancel")).setOnClickListener(clickListener);
				view.findViewById(MResource.getIdByName(mContext, "id", "ok")).setOnClickListener(clickListener);
				
				mDialog = new Dialog(mContext, MResource.getIdByName(mContext, "style", "translucent"));
				mDialog.setContentView(view);
				mDialog.setCanceledOnTouchOutside(true);
				mDialog.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						//if (keyCode == KeyEvent.KEYCODE_BACK) return true;
						return false;
					}
				});
				mDialog.show();
				
				doAction(BrowseBandgeActionConstants.ACTION_LOG_2203, null);
			}
		});
	}

	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(v.getId() == MResource.getIdByName(mContext, "id", "cancel")){
				 doCancel();
			}else if (v.getId() == MResource.getIdByName(mContext, "id", "ok")) {
				doOk();
			}else if (v.getId() == MResource.getIdByName(mContext, "id", "check_mark")) {
				isChecked = !isChecked;
				mCheckMark.setImageResource(MResource.getIdByName(mContext, "drawable", isChecked ? "nq_pop_check_mark_select" : "nq_pop_check_mark_def"));
				return;
			}
			dismissDialog();
		}
	};
	
	private void dismissDialog() {
		if(mDialog != null){
			mDialog.dismiss();
		}
	}
	
	/**
	 * 以后再说
	 */
	private void doCancel() {
		doAction(BrowseBandgeActionConstants.ACTION_LOG_2205, null);
		doAction(BrowseBandgeActionConstants.ACTION_LOG_2206, isChecked ? "0" : "1");
		
		nativeLastRecordDialogTime = SystemFacadeFactory.getSystem().currentTimeMillis();
		mPreference.setRecordDialogTime(nativeLastRecordDialogTime);
		mPreference.setRecrodChecked(isChecked);
		
		mIntent.putExtra(BandgeManager.KEY_URL, "");
		mIntent.putExtra(BandgeManager.KEY_URL_LONG, "");
		BandgeManager.getInstance(mContext).processBrower(mIntent, false);
	}
	
	/**
	 * 现在开启
	 */
	private void doOk() {
		doAction(BrowseBandgeActionConstants.ACTION_LOG_2204, null);
		
		BrowserBandgePreference.getInstance().setRecordDialogOk(true);
		BandgeManager.getInstance(mContext).processBrower(mIntent, true);
	}
	
	/**
	 * 记录行为日记
	 * @param action
	 * @param scene
	 */
	private void doAction(String action, String scene) {
		StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION, action, mResId, mResId.startsWith("AD_") ? 1 : 0, scene);
	}
}
