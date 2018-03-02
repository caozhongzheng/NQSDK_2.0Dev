package com.nqmobile.livesdk.modules.app;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.nq.interfaces.launcher.TAppResource;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.StringUtil;

public class AppConverter {
	/**
     * AppResource转换成App对象
     *
     * @param appResource
     * @return
     */
	public static App convert(Context context, App app, TAppResource appResource) {
        if (appResource != null) {
        	if (app == null){
        		app = new App();
    		}
            app.setStrId(appResource.getResourceId());
            app.setStrName(StringUtil.nullToEmpty(appResource.getName()));
            app.setStrPackageName(StringUtil.nullToEmpty(appResource.getPackageName()));
            app.setStrDescription(StringUtil.nullToEmpty(appResource.getDescription()));
            app.setStrDevelopers(StringUtil.nullToEmpty(appResource.getDevelopers()));
            app.setStrVersion(StringUtil.nullToEmpty(appResource.getVersion()));
            app.setLongUpdateTime(appResource.getUpdateTime());
            app.setLongSize(appResource.getSize());
            app.setStrCategory1(StringUtil.nullToEmpty(appResource.getClassification1()));
            app.setStrCategory2(StringUtil.nullToEmpty(appResource.getClassification2()));
            app.setStrIconUrl(StringUtil.nullToEmpty(appResource.getIcon()));
            app.setStrImageUrl(StringUtil.nullToEmpty(appResource.getImage()));
            app.setArrPreviewUrl(appResource.getPicture());
            app.setLongDownloadCount(appResource.getDownloadNum());
            app.setIntClickActionType(appResource.getClickActionType());
            app.setIntDownloadActionType(appResource.getDownActionType());
            app.setStrAppUrl(StringUtil.nullToEmpty(appResource.getLinkUrl()));
            app.setFloatRate((float) appResource.getScore());
            app.setType(appResource.getType());
            app.setRewardPoints(appResource.getRewardPoints());
            app.setTrackId(appResource.getTrackId());
            app.setSlientDownload(appResource.isSilentDownload == 1);
            String SDCardPath = CommonMethod.getSDcardPath(context);
            if(SDCardPath == null)
            	SDCardPath = CommonMethod.getSDcardPathFromPref(context);
            //icon本地路径
            if (!TextUtils.isEmpty(app.getStrIconUrl())) {
                String strImgPath = new StringBuilder().append(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).toString();
                app.setStrIconPath(new StringBuilder().append(strImgPath).append(app.getStrId()).append("_icon")
                        .append(StringUtil.getExt(app.getStrIconUrl())).toString());
            }
            //image本地路径
            if (!TextUtils.isEmpty(app.getStrImageUrl())) {
                String strImgPath = new StringBuilder().append(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).toString();
                app.setStrImagePath(new StringBuilder().append(strImgPath).append(app.getStrId()).append("_imag")
                        .append(StringUtil.getExt(app.getStrImageUrl())).toString());
            }
            //apk本地路径
            if (!TextUtils.isEmpty(app.getStrAppUrl())) {
                String strAppPath = new StringBuilder().append(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).toString();
                app.setStrAppPath(new StringBuilder().append(strAppPath).append(app.getStrId())
                        .append(".apk").toString());
            }
            //预览图缓存、本地路径
            List<String> arrPreviewUrl = app.getArrPreviewUrl();
            if (arrPreviewUrl != null && arrPreviewUrl.size() > 0) {
                List<String> arrPreviewPath = new ArrayList<String>();
                String strPreviewPath = new StringBuilder().append(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).toString();
                for (int i = 0; i < arrPreviewUrl.size(); i++) {
                    if (!TextUtils.isEmpty(arrPreviewUrl.get(i))) {
                        arrPreviewPath.add(new StringBuilder().append(strPreviewPath).append(app.getStrId()).append("_preview").append(i)
                                .append(StringUtil.getExt(arrPreviewUrl.get(i))).toString());
                    }
                }
                app.setArrPreviewPath(arrPreviewPath);
            }
            //app.setLongLocalTime(longLocalTime);//这个在保存的时候去客户端时间
            app.setStrShortIntro(StringUtil.nullToEmpty(appResource.getIntroduction()));
            app.setStrAudioUrl(appResource.getAudio());
            app.setStrVideoUrl(appResource.getVideo());
            // 音频缓存、本地路径
            if (!TextUtils.isEmpty(app.getStrAudioUrl())) {
                String strAudioPath = new StringBuilder().append(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).toString();
                app.setStrAudioPath(new StringBuilder().append(strAudioPath).append(app.getStrId())
                        .append(StringUtil.getExt(app.getStrAudioUrl())).toString());
            }
            // 视频缓存、本地路径
            if (!TextUtils.isEmpty(app.getStrVideoUrl())) {
                String strVideoPath = new StringBuilder().append(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).toString();
                app.setStrVideoPath(new StringBuilder().append(strVideoPath).append(app.getStrId())
                        .append(StringUtil.getExt(app.getStrVideoUrl())).toString());
            }
            app.setIntEffect(appResource.getEffectId());
        }
        return app;
	}

    public static App convert(Context context, TAppResource appResource) {
    	App app = new App();
        return convert(context, app, appResource);
    }
    
    public static List<App> convert(Context context, List<TAppResource> resources) {
        if (resources == null || resources.isEmpty()){
            return null;
        }
        
        List<App> result = new ArrayList<App>(resources.size());
        for(TAppResource ar : resources){
            App app = convert(context, ar);
            if (app != null){
                result.add(app);
            }
        }
        
        return result;
    }
    
}
