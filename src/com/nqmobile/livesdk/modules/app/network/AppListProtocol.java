package com.nqmobile.livesdk.modules.app.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TApplicationException;

import android.content.Context;

import com.nq.interfaces.launcher.TAppResource;
import com.nq.interfaces.launcher.TLauncherService;
import com.nqmobile.livesdk.commons.AppConstants;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppConstant;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.app.AppModule;

public class AppListProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(AppModule.MODULE_NAME);
	
	private Context mContext;
    private int mColumn;
    private int mOffset;
    private int mSize;

    public AppListProtocol(Context context, int column, int offset, int size, Object tag) {
        setTag(tag);
        mContext = context;
        mColumn = column;
        mOffset = offset;
        mSize = size;
        NqLog.d("AppListProtocol mColumn:" + mColumn + " mSize:" + mSize + " mOffset:" + mOffset);
    }
    
	@Override
	protected int getProtocolId() {
		return 0x02;
	}

	@Override
	public void process() {
		try {
			int thriftColumn = 0; //thrift接口里： 3游戏排行栏目 4软件排行栏目 100 游戏文件夹 101 装机必备 102 新版游戏文件夹 110积分中心（不过滤GP） 111兑换时余额不足（过滤GP） 
	        if (mColumn == 0) {
	            thriftColumn = 3;
	        } else if (mColumn == 1) {
	            thriftColumn = 4;
	        } else if (mColumn == 2) {
	            thriftColumn = 102;
	        } else if(mColumn == 3) {
	            thriftColumn = 101;
	        } else if(mColumn == 4) {
	            thriftColumn = 110;
	        } else if(mColumn == 5) {
	            thriftColumn = 111;
	        }

            NqLog.d("AppListProtocol process mColumn:" + mColumn + " thriftColumn:" + thriftColumn +
            		" mSize:" + mSize + " mOffset:" + mOffset +
            		" userinfo:" + getUserInfo());
            
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());

			List<TAppResource> resources = client.getAppList(getUserInfo(), thriftColumn, mOffset, 
            		mSize == 0 ? AppConstant.STORE_APP_LIST_PAGE_SIZE : mSize);
			ArrayList<App> apps = new ArrayList<App>();

            NqLog.d("AppListProtocol resources:" + resources);
            if (resources != null && resources.size() > 0) {
                AppManager appManager = AppManager.getInstance(mContext);
                for (TAppResource ar : resources) {
                    App app = new App(ar, mContext);
                    if(mColumn == 0 || mColumn == 1){
                        app.setIntSourceType(AppConstant.STORE_MODULE_TYPE_MAIN);
                    } else if(mColumn == 2){
                        app.setIntSourceType(AppConstants.STORE_MODULE_TYPE_GAME_AD);
                    } else if(mColumn == 3){
                        app.setIntSourceType(AppConstants.STORE_MODULE_TYPE_MUST_INSTALL);
                    } else if(mColumn == 4 || mColumn == 5){
                        app.setIntSourceType(AppConstants.STORE_MODULE_TYPE_POINTS_CENTER);
                    }
                    apps.add(app);
                }

                if (apps != null && apps.size() > 0) {//存入缓存
                    if (mColumn != 4 && mColumn != 5) {
                        appManager.cacheApp(mColumn, mOffset, apps);
                    }
                }
			}
            EventBus.getDefault().post(new GetAppListSuccessEvent(apps, mColumn, mOffset, true, getTag()));
        } catch (TApplicationException e) {//服务器端无数据
            NqLog.d("AppListProtocol process() server is empty");
            onError();
        } catch (Exception e) {
        	NqLog.e(e);onError();
        }
	}
	@Override
	protected void onError() {
		EventBus.getDefault().post(new GetAppListSuccessEvent(null, mColumn, mOffset, false, getTag()));
	}
    
	public static class GetAppListSuccessEvent extends AbsProtocolEvent {
    	private ArrayList<App> mResource;
    	private boolean isSuccess = false;
        private int mColumn;
        private int mOffset;
        
    	public GetAppListSuccessEvent(ArrayList<App> resource, int column, int offset, boolean success, Object tag){
    		setTag(tag);
    		mResource = resource;
    		isSuccess = success;
    		mColumn = column;
    		mOffset = offset;
    	}

    	public ArrayList<App> getApps() {
    		NqLog.d("getApps:" + mResource);
    		return mResource;
    	}
    	
    	public boolean isSuccess(){
    		return isSuccess;
    	}
    	
    	public int getColumn() {
			return mColumn;
		}
    	
    	public int getOffset() {
    		return mOffset;
    	}
    }

	
}
