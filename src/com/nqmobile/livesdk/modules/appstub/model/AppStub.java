package com.nqmobile.livesdk.modules.appstub.model;

import java.io.Serializable;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.image.ImageListener;
import com.nqmobile.livesdk.commons.image.ImageLoader;
import com.nqmobile.livesdk.commons.image.LoadIconListener;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.utils.ImagUtil;
import com.nqmobile.livesdk.utils.MResource;

public class AppStub implements Serializable {

	private static final long serialVersionUID = -2382630462281011968L;
	App app; // 虚框应用包含的应用信息
	private Context mContext;
	transient Intent open; // 打开虚框应用的intent

	public AppStub() {
		super();
		 mContext = ApplicationContext.getContext();
	}

	public AppStub(App app, Intent open) {
		this.app = app;
		this.open = open;
		 mContext = ApplicationContext.getContext();
	}

	public App getApp() {
		return app;
	}

	public void setApp(App app) {
		this.app = app;
	}

	public Intent getOpen() {
		return open;
	}

	public void setOpen(Intent open) {
		this.open = open;
	}

	@Override
	public String toString() {
		return "AppStub [app=" + app + "]";
	}
	
	public void getIconBitmap(final LoadIconListener imageLoadListener){
        ImageLoader.getInstance(ApplicationContext.getContext()).getImage(app.getStrIconUrl(), new ImageListener() {
            @Override
            public void getImageSucc(String url, BitmapDrawable drawable) {
                Bitmap result = drawable.getBitmap();
                Bitmap r = result.copy(result.getConfig(),false);
                if (r == null) {
                    r = getDefaultIcon();
                }
                imageLoadListener.onLoadComplete(r);
            }

            @Override
            public void onErr() {
                imageLoadListener.onErr();
            }
        });
    }

	 public Bitmap getDefaultIcon() {
	        return BitmapFactory.decodeResource(mContext.getResources(),
	                MResource.getIdByName(mContext, "drawable", "nq_icon_default"));
	    }
}
