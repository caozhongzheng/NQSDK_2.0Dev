package com.nqmobile.livesdk.modules.appstub.network;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TApplicationException;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.text.TextUtils;

import com.nq.interfaces.launcher.TAppResource;
import com.nq.interfaces.launcher.TAppTypeInfoQuery;
import com.nq.interfaces.launcher.TLauncherService;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.image.ImageManager;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.appstub.AppStubConstants;
import com.nqmobile.livesdk.modules.appstub.AppStubModule;
import com.nqmobile.livesdk.modules.appstub.AppStubPreference;
import com.nqmobile.livesdk.utils.FileUtil;
import com.nqmobile.livesdk.utils.Tools;

/**
 * Created by caozhongzheng on 14-6-23.
 */
public class AppStubProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(AppStubModule.MODULE_NAME);

	private AppStubPreference mPreference;
	private Context mContext;

    public AppStubProtocol(Object tag) {
        setTag(tag);
        this.mContext = ApplicationContext.getContext();
        mPreference = AppStubPreference.getInstance();
    }
    
	@Override
	protected int getProtocolId() {
		return 0x33;// 分配一个协议ID
	}

    @Override
    public void process() {
        try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());

			TAppTypeInfoQuery query = getAllPackages();
			List<TAppResource> resources = client.getRecommendAppList(
					getUserInfo(), query);
			if (resources != null && resources.size() > 0){
				List<App> list = preloadIcon(resources);
				EventBus.getDefault().post(new AppStubSuccessEvent(list, getTag()));
			}
        } catch (TApplicationException e) {//服务器端无数据
            NqLog.d("AppStubProtocol process() server is empty");
            EventBus.getDefault().post(new AppStubSuccessEvent(null, getTag()));
        } catch (Exception e) {
        	NqLog.e(e);
        	onError();
        } finally {
//        	mPreference.setLongValue(AppStubPreference.KEY_LAST_GET_GAME_AD_TIME, SystemFacadeFactory.getSystem().currentTimeMillis());
        }
    }
    @Override
	protected void onError() {
    	EventBus.getDefault().post(new AppStubFailedEvent(getTag()));
	}
    private List<App> preloadIcon(List<TAppResource> resources) {
    	List<App> appList = new ArrayList<App>();
    	if(resources != null && resources.size() > 0){
        	for(TAppResource t:resources){
        		boolean result = false;
        		App app = new App(t, mContext);
        		app.setIntSourceType(AppStubConstants.STORE_MODULE_TYPE_APPSTUB);
        		if(app != null){
        			result = saveImageToFile(app.getStrIconUrl(), app.getStrIconPath());
        			if(result){
        				NqLog.i("AppStubProtocol saveImageToFile ok app:" + app.getStrId() + "/" + app.getStrName());
        				appList.add(app);
        			} else {
        				NqLog.i("AppStubProtocol saveImageToFile failed app:" + app.getStrId() + "/" + app.getStrName());
        			}
        		}
        	}
    	}
		return appList;
	}

	public static class AppStubSuccessEvent extends AbsProtocolEvent {
    	private List<App> mApps;
    	
    	public AppStubSuccessEvent(List<App> apps, Object tag){
    		setTag(tag);
    		mApps = apps;
    	}

    	public List<App> getApps() {
    		return mApps;
    	}
    }
    
    public static class AppStubFailedEvent extends AbsProtocolEvent {
    	public AppStubFailedEvent(Object tag) {
			setTag(tag);
		}
    }

	private TAppTypeInfoQuery getAllPackages() {
		Intent it = new Intent(Intent.ACTION_MAIN);
		it.addCategory(Intent.CATEGORY_LAUNCHER);

		ArrayList<String> appLauncheredList = new ArrayList<String>();
		List<ResolveInfo> app = mContext.getPackageManager()
				.queryIntentActivities(it, 0);
		for (int i = 0; i < app.size(); i++) {
			ActivityInfo ai = app.get(i).activityInfo;
			appLauncheredList.add(ai.applicationInfo.packageName);
		}
		TAppTypeInfoQuery q = new TAppTypeInfoQuery();
		q.setPackageNames(appLauncheredList);
		return q;
	}
	
	private boolean saveImageToFile(final String srcUrl, final String destFilePath) {
        boolean result = false;
        if (TextUtils.isEmpty(srcUrl)) {
            return result;
        }

        ImageManager imgMgr = ImageManager.getInstance(mContext);
        BitmapDrawable bd = imgMgr.loadImageFromMemoryOrDisk(srcUrl);
        Bitmap bitmap = null;
        if(bd != null){
			try {
//				NqLog.i(TAG, "AppStubProtocol writeBmpToFile srcUrl:" + srcUrl + ",destFilePath="+destFilePath);
				bitmap = bd.getBitmap();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        if(bitmap == null){
        	InputStream input = null;
        	try {
//        		NqLog.i(TAG, "AppStubProtocol decodeStream srcUrl:" + srcUrl + ",destFilePath="+destFilePath);
        		input = (InputStream) new URL(srcUrl).getContent();
        		bitmap = BitmapFactory.decodeStream(input);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				FileUtil.closeStream(input);
			}
        }
        if(bitmap != null){
        	if (Tools.stringEquals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
				File file = new File(destFilePath);
				if (!file.getParentFile().exists()) 
					file.getParentFile().mkdirs();
			}
        	boolean w = FileUtil.writeBmpToFile(bitmap, new File(destFilePath));
//        	NqLog.i(TAG, "AppStubProtocol saveImgfile :" + w);
        }
        result = (bitmap != null);
        return result;
    }

	
}
