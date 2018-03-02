package com.nqmobile.livesdk.modules.newyear;

import java.util.ArrayList;

import org.json.JSONObject;

import com.nqmobile.live.R;
import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.modules.font.FontConstants;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.MResource;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ShareLauncherToQQActivity extends Activity {
	private Tencent mTencent;
	private String APP_ID;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this.getApplicationContext();
		
		//test
		APP_ID = ClientInfo.getQQ_APPID();
		Log.i("ljc", "qq id=" + APP_ID);
		
		mTencent = Tencent.createInstance(APP_ID, mContext);
		if (mTencent == null) {
			Toast.makeText(mContext, MResource.getString(mContext,"nq_app_wx_share_ko"), Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		onClickShareToQQ();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private void onClickShareToQQ() {
		Bundle b = getShareBundle();
		if(b != null){
			shareParams = b;
			Thread thread = new Thread(shareThread);
			thread.start();
		}
	}

	private Bundle getShareBundle(){
		 Bundle bundle = new Bundle();
		 bundle.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
		 bundle.putString(QzoneShare.SHARE_TO_QQ_TITLE, MResource.getString(mContext,"nq_app_share_title"));
//		 bundle.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, MResource.getString(mContext,"nq_app_share_content"));
		 bundle.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL,  MResource.getString(mContext,"nq_app_share_url"));
        // 支持传多个imageUrl
        ArrayList<String> imageUrls = new ArrayList<String>();
        imageUrls.add("http://cdn-livecn.nq.com/app/2826fc54267169e761e893687684f474.png");
        bundle.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);
        return bundle;		
	}
	Bundle shareParams = null;


	// 线程类，该类使用匿名内部类的方式进行声明
	Runnable shareThread = new Runnable() {

		public void run() {
			doShareToQQ(shareParams);
		}
	};

	private void doShareToQQ(Bundle params) {
		mTencent.shareToQzone(ShareLauncherToQQActivity.this, params, new BaseUiListener() {
		});
	}

	private class BaseUiListener implements IUiListener {

		@Override
		public void onError(UiError e) {
			Log.i("ljc", "doShareToQQ:share to qq error!!");	
			finish();
		}

		@Override
		public void onCancel() {
			Log.i("ljc", "doShareToQQ:share to qq cancelled......");
			finish();			
		}

		@Override
		public void onComplete(Object arg0) {
			// TODO Auto-generated method stub
			Log.i("ljc", "doShareToQQ:share to qq succeed, begin to setResult back to lotteryActivity");
	        ShareLauncherToQQActivity.this.sendBroadcast(new Intent(LotteryActivity.MSG_SHARE_SUCCESS));
			finish();
		}
	}

	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != mTencent)    
            mTencent.onActivityResult(requestCode, resultCode, data);
		Log.i("ljc", "doShareToQQ:onActivityResult......");
		finish();
	 }}
