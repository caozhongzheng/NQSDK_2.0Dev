package com.nqmobile.livesdk.modules.wallpaper.network;

import org.apache.thrift.TApplicationException;

import android.content.Context;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TWallpaperResource;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.wallpaper.Wallpaper;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperModule;

public class WallpaperDetailProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(WallpaperModule.MODULE_NAME);

	private Context mContext;
	private String mId;

    public WallpaperDetailProtocol(Context context, String id, Object tag) {
        setTag(tag);
        mContext = context;
        mId = id;
    }
    
	@Override
	protected int getProtocolId() {
		return 0x07;
	}

	@Override
	protected void process() {
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());

			TWallpaperResource resource = client.getWallpaperDetail(getUserInfo(), mId);
            if (resource != null) {
            	Wallpaper wallpaper =new Wallpaper(resource, mContext);
            	EventBus.getDefault().post(new GetWallpaperDetailSuccessEvent(wallpaper, getTag(), true));
            }
        } catch (TApplicationException e) {//服务器端无数据
            NqLog.d("WallpaperDetailProtocal process() server is empty");
            onError();
        } catch (Exception e) {
        	NqLog.e(e);
        	onError();
        }
	}
	@Override
	protected void onError() {
		EventBus.getDefault().post(new GetWallpaperDetailSuccessEvent(null, getTag(), false));
	}
	public static class GetWallpaperDetailSuccessEvent extends AbsProtocolEvent {
    	private Wallpaper mResource;
    	private boolean isSuccess = false;
    	
    	public GetWallpaperDetailSuccessEvent(Wallpaper resource, Object tag, boolean success){
    		setTag(tag);
    		mResource = resource;
    		isSuccess = success;
    	}

    	public Wallpaper getWallpaper() {
    		return mResource;
    	}
    	
    	public boolean isSuccess(){
    		return isSuccess;
    	}
    }

	
}
