package com.nqmobile.livesdk.commons.ui;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.nqmobile.livesdk.commons.info.MobileInfo;

/**
 * 所有Actvity都扩展自此类。一些公共部分可以加到此类里。
 * @author changxiaofei
 * @time 2013-11-18 下午3:16:45
 */
public class BaseActvity extends Activity{
	private LayoutInflater mCustomInflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);
	}

    @Override
    protected void onResume() {
        super.onResume();
        if(MobileInfo.getDeviceName().equals("ZTE__ZTE__ZTE G718C")){
            getWindow().getDecorView().setPadding(0,50,0,0);
        }
    }

	@Override
	public Object getSystemService(final String name) {
	    if (Context.LAYOUT_INFLATER_SERVICE.equals(name)) {
	        return getCustomLayoutInflater();
	    }
	    return super.getSystemService(name);
	}

	@Override
	public LayoutInflater getLayoutInflater() {
	    return getCustomLayoutInflater();
	}

	private LayoutInflater getCustomLayoutInflater() {
	    if (mCustomInflater == null) {
	    	LayoutInflater systemInflater = (LayoutInflater)super.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (FontFactory.hasSetDefaultTypeface()) {
				mCustomInflater = new TypefaceLayoutInflater(systemInflater,
						this);
				mCustomInflater.setFactory(this);
			} else {
				mCustomInflater = systemInflater;
			}
	    }
	    
	    return mCustomInflater;
	}
}
