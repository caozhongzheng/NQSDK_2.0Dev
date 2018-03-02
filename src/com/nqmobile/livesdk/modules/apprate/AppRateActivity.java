package com.nqmobile.livesdk.modules.apprate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.MResource;

public class AppRateActivity extends BaseActvity implements View.OnClickListener {
	// ===========================================================
	// Constants
	// ===========================================================
	private LinearLayout mRateBtn;
	private LinearLayout mFeedbackBtn;

	// ===========================================================
	// Fields
	// ===========================================================
	
	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        int layoutResID = MResource.getIdByName(this,"layout","nq_app_rate_dialog");
        setContentView(layoutResID);
        
        mRateBtn = (LinearLayout) findViewById(MResource
				.getIdByName(this, "id", "nq_app_rate_dialog_rate_btn"));
        mFeedbackBtn = (LinearLayout) findViewById(MResource
				.getIdByName(this, "id", "nq_app_rate_dialog_feedback_btn"));
        
        mRateBtn.setOnClickListener(this);
        mFeedbackBtn.setOnClickListener(this);
        
        StatManager.getInstance().onAction(
				StatManager.TYPE_STORE_ACTION,
				AppRateActionConstants.ACTION_LOG_3501, null, 0, null);
	}
	
	@Override
	public void onClick(View v) {
        int rateBtnId = mRateBtn.getId();
        int feedbackBtnId = mFeedbackBtn.getId();
		int id = v.getId();
		if (id == rateBtnId) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("market://details?id=" + getPackageName()));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			StatManager.getInstance().onAction(
					StatManager.TYPE_STORE_ACTION,
					AppRateActionConstants.ACTION_LOG_3502, null, 0, null);
			finish();
		} else if (id == feedbackBtnId){
			Intent intent = new Intent("com.lqsoft.launcher.feedback");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			StatManager.getInstance().onAction(
					StatManager.TYPE_STORE_ACTION,
					AppRateActionConstants.ACTION_LOG_3503, null, 0, null);
			finish();
		}
	}
	
	@Override
    protected void onDestroy(){
		if (isFinishing()){
			StatManager.getInstance().onAction(
					StatManager.TYPE_STORE_ACTION,
					AppRateActionConstants.ACTION_LOG_3504, null, 0, null);
		}
		super.onDestroy();
	}
	
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
