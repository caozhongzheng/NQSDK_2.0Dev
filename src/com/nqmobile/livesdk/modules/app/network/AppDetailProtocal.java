package com.nqmobile.livesdk.modules.app.network;

import org.apache.thrift.TApplicationException;

import android.content.Context;

import com.nq.interfaces.launcher.TAppResource;
import com.nq.interfaces.launcher.TLauncherService;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppModule;

public class AppDetailProtocal extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(AppModule.MODULE_NAME);

	private Context mContext;
	private String mId;

    public AppDetailProtocal(Context context, String id, Object tag) {
        setTag(tag);
        mContext = context;
        mId = id;
    }
    
	@Override
	protected int getProtocolId() {
		return 0x05;
	}

	@Override
	protected void process() {
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());

			TAppResource resource = client.getAppDetail(getUserInfo(), mId);
			
            if (resource != null) {
            	App app = new App(resource, mContext);
            	EventBus.getDefault().post(new GetAppDetailSuccessEvent(app, getTag(), true));
            }
        } catch (TApplicationException e) {//服务器端无数据
            NqLog.d("AppDetailProtocal process() server is empty");
            onError();
        } catch (Exception e) {
        	NqLog.e(e);
        	onError();
        }
	}

	@Override
	protected void onError() {
		EventBus.getDefault().post(new GetAppDetailSuccessEvent(null, getTag(), false));
	}
	
	public static class GetAppDetailSuccessEvent extends AbsProtocolEvent {
    	private App mResource;
    	private boolean isSuccess = false;
    	
    	public GetAppDetailSuccessEvent(App resource, Object tag, boolean success){
    		setTag(tag);
    		mResource = resource;
    		isSuccess = success;
    	}

    	public App getApp() {
    		return mResource;
    	}

    	public boolean isSuccess(){
    		return isSuccess;
    	}
    }

    
}
