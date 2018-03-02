package com.nqmobile.livesdk.modules.newyear;

import java.io.ByteArrayOutputStream;

import com.nqmobile.live.R;
import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.modules.newyear.LotteryActivity;
import com.nqmobile.livesdk.utils.MResource;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ShareLauncherToWXActivity extends Activity implements IWXAPIEventHandler{
	
	private String APP_ID;  
	private Context mContext;
	private Button shareBtn;
	
    private IWXAPI api;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(MResource.getIdByName(this, "layout", "wx"));
        
        mContext = this;
        APP_ID = ClientInfo.getWX_APPID();
		Log.i("ljc", "wx id=" + APP_ID);
        try {
	    	api = WXAPIFactory.createWXAPI(this, APP_ID, true);
	
	    	api.registerApp(APP_ID);
	        api.handleIntent(getIntent(), this);
	        
		    WXWebpageObject webpage = new WXWebpageObject();  
		    webpage.webpageUrl = MResource.getString(mContext,"nq_app_share_url");  
		    WXMediaMessage msg = new WXMediaMessage(webpage);  
		    msg.title = MResource.getString(mContext,"nq_app_share_title");  
			Bitmap thumb = BitmapFactory.decodeResource(mContext.getApplicationContext().getResources(), R.drawable.nq_lottery_icon);
	//				MResource.getIdByName(mContext, "drawable", "nq_lottery_icon"));
			msg.thumbData = Bitmap2Bytes(thumb);			    
		    msg.description = MResource.getString(mContext,"nq_app_share_content");  
		      
		    SendMessageToWX.Req req = new SendMessageToWX.Req();  
		    req.transaction = String.valueOf(System.currentTimeMillis());  
		    req.message = msg;  
		    //WXSceneSession  好友                           WXSceneTimeline 朋友圈
		    req.scene = SendMessageToWX.Req.WXSceneTimeline;  
		    api.sendReq(req); 
        } catch (Exception e) {
        	Toast.makeText(mContext, MResource.getString(mContext,"nq_app_wx_share_ko"), Toast.LENGTH_LONG).show();
        	finish();
        }
    	       
    }

    private byte[] Bitmap2Bytes(Bitmap bm) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
     }
    
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		setIntent(intent);
        api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
			break;
		default:
			break;
		}
	}

	@Override
	public void onResp(BaseResp resp) {
		Toast toast;
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			toast = Toast.makeText(ShareLauncherToWXActivity.this, MResource.getString(ShareLauncherToWXActivity.this,"nq_app_wx_share_ok"), Toast.LENGTH_LONG);
			Log.i("ljc", "WXEntryActivity: share to wx succeed, begin to sendBroadcast back to lotteryActivity");
			ShareLauncherToWXActivity.this.sendBroadcast(new Intent(LotteryActivity.MSG_SHARE_SUCCESS));
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			toast = Toast.makeText(ShareLauncherToWXActivity.this, MResource.getString(ShareLauncherToWXActivity.this,"nq_app_wx_share_cancel"), Toast.LENGTH_LONG);
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			toast = Toast.makeText(ShareLauncherToWXActivity.this, MResource.getString(ShareLauncherToWXActivity.this,"nq_app_wx_auth_ko"), Toast.LENGTH_LONG);
			break;
		default:
			toast = Toast.makeText(ShareLauncherToWXActivity.this, MResource.getString(ShareLauncherToWXActivity.this,"nq_app_wx_share_ko"), Toast.LENGTH_LONG);
			break;
		}
     	toast.show();
//        Intent intent=new Intent();  
//        intent.putExtra("share_status", resp.errCode);  
//        WXEntryActivity.this.setResult(RESULT_OK, intent);      
        finish();
	}
}