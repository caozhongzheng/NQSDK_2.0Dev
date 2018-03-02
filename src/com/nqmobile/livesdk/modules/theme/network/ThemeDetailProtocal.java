package com.nqmobile.livesdk.modules.theme.network;

import org.apache.thrift.TApplicationException;

import android.content.Context;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TThemeResource;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.theme.ThemeModule;

public class ThemeDetailProtocal extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(ThemeModule.MODULE_NAME);

	private Context mContext;
	private String mId;

    public ThemeDetailProtocal(Context context, String id, Object tag) {
        setTag(tag);
        mContext = context;
        mId = id;
    }
    
	@Override
	protected int getProtocolId() {
		return 0x06;
	}

	@Override
	protected void process() {
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());

			TThemeResource resource = client.getThemeDetail(getUserInfo(), mId);
	            
            if (resource != null) {
            	Theme theme = new Theme(resource, mContext);
            	EventBus.getDefault().post(new GetThemeDetailSuccessEvent(theme, getTag(), true));
            }
        } catch (TApplicationException e) {//服务器端无数据
            NqLog.d("ThemeDetailProtocal process() server is empty");
            onError();
        } catch (Exception e) {
        	NqLog.e(e);
        	onError() ;
        }
	}
	@Override
	protected void onError() {
		EventBus.getDefault().post(new GetThemeDetailSuccessEvent(null, getTag(), false));
	}
	public static class GetThemeDetailSuccessEvent extends AbsProtocolEvent {
    	private Theme mResource;
    	private boolean isSuccess = false;
    	
    	public GetThemeDetailSuccessEvent(Theme resource, Object tag, boolean success){
    		setTag(tag);
    		mResource = resource;
    		isSuccess = success;
    	}

    	public Theme getTheme() {
    		return mResource;
    	}

    	public boolean isSuccess(){
    		return isSuccess;
    	}
    }

	
    
}
