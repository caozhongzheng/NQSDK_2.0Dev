package com.nqmobile.livesdk.commons.ui;

import com.nqmobile.livesdk.commons.info.MobileInfo;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;

public class BaseFragmentActivity extends FragmentActivity {
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
