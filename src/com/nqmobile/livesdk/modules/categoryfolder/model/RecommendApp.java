package com.nqmobile.livesdk.modules.categoryfolder.model;

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
import com.nqmobile.livesdk.modules.app.AppDetailActivity;
import com.nqmobile.livesdk.modules.categoryfolder.CategoryFolderConstants;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.GpUtils;
import com.nqmobile.livesdk.utils.ImagUtil;
import com.nqmobile.livesdk.utils.MResource;

/**
 * Created by Rainbow on 2014/12/11.
 */
public class RecommendApp {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    public App mApp;
    private Context mContext;

    // ===========================================================
    // Constructors
    // ===========================================================

    public RecommendApp(App app){
        mApp = app;
        mContext = ApplicationContext.getContext();
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    public void getIconBitmap(final LoadIconListener imageLoadListener){
        ImageLoader.getInstance(ApplicationContext.getContext()).getImage(mApp.getStrIconUrl(), new ImageListener() {
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

    public void viewDetail(RecommendApp app){
        if(app.mApp.isGpApp()){
            GpUtils.viewDetail(mContext,app.mApp.getStrAppUrl());

            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    CategoryFolderConstants.ACTION_LOG_2904,app.mApp.getStrId(),0,"");
        }else{
            Intent i = new Intent(mContext,AppDetailActivity.class);
            i.putExtra(AppDetailActivity.KEY_APP,app.mApp);
            i.putExtra(AppDetailActivity.KEY_APP_SOURCE_TYPE,CategoryFolderConstants.DOWNLOAD_SOURCE_TYPE_BOTEM);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);

            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    CategoryFolderConstants.ACTION_LOG_2904,app.mApp.getStrId(),0,"");
        }

    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
