package com.nqmobile.livesdk.modules.points;

import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.utils.PackageUtils;
import com.nqmobile.livesdk.utils.Tools;

public class PointOpenNoitifiAppACT extends BaseActvity{
	private static final ILogger NqLog = LoggerFactory.getLogger(PointModule.MODULE_NAME);

	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = getApplicationContext();
		
		Intent intent = getIntent();
		String packagename = intent.getStringExtra("packagename");
		String themeid = intent.getStringExtra("themeid");
		String trackid = intent.getStringExtra("trackid");
		int downloadid = intent.getIntExtra("downloadid", 0);
		
		int randomId = downloadid + new Random().nextInt(100) + 10;
		NqLog.d("PointOpenNoitifiAppACT packagename:" + packagename + ", themeid:" + themeid + 
				", downloadid:" + downloadid + ", randomId:" + randomId + ", trackid:" + trackid);
		if(!Tools.isEmpty(trackid)){
			PointsManager.getInstance(mContext).rewardPoints(trackid, true, themeid, randomId,null);
		}
		PackageUtils.launchApp(mContext, packagename);
        finish();
	}
}
