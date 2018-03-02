package com.nqmobile.livesdk.modules.newyear;

import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.PackageUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

public class ShareLauncherDialog extends Dialog {

    // ===========================================================
    // Constants
    // ===========================================================
	public static final String WXPackageName = "com.tencent.mm";
	public static final String QQPackageName = "com.tencent.mobileqq";
    // ===========================================================
    // Fields
    // ===========================================================

    private Context mContext;

    // ===========================================================
    // Constructors
    // ===========================================================

    public ShareLauncherDialog(Context context,int theme){
        super(context,theme);
        mContext = context;
    }

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
        setContentView(MResource.getIdByName(mContext, "layout", "nq_share_launcher_dialog"));

        final Activity activity = (Activity)mContext;
        ImageView shareWX = (ImageView) findViewById(MResource.getIdByName(mContext,"id","image_shareToWX"));
        shareWX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //行为日志
                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                		NewYearConstants.ACTION_LOG_3602, null, 0, null);	
                
            	if (!PackageUtils.isAppInstalled(mContext, WXPackageName)) {
            		Toast.makeText(mContext, MResource.getString(mContext,"nq_app_wx_not_installed"), Toast.LENGTH_LONG).show();            		
            	} else {
	                Intent i = new Intent(mContext, ShareLauncherToWXActivity.class);
	                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                activity.startActivityForResult(i,3);
            	}
                dismiss();
            }
        });

        ImageView shareQQ = (ImageView) findViewById(MResource.getIdByName(mContext,"id","image_shareToQQ"));
        shareQQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //行为日志
                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                		NewYearConstants.ACTION_LOG_3603, null, 0, null);	
                
            	if (!PackageUtils.isAppInstalled(mContext, QQPackageName)) {
            		Toast.makeText(mContext, MResource.getString(mContext,"nq_app_qq_not_installed"), Toast.LENGTH_LONG).show();
            	} else {       	
	                Intent i = new Intent(mContext, ShareLauncherToQQActivity.class);
	                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                activity.startActivity(i);
            	}
                dismiss();
            }
        });
        
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}