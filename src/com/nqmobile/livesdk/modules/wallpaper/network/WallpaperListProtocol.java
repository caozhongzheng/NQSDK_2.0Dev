package com.nqmobile.livesdk.modules.wallpaper.network;

import java.util.ArrayList;
import java.util.List;

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
import com.nqmobile.livesdk.modules.wallpaper.WallpaperConstants;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperManager;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperModule;
import com.nqmobile.livesdk.utils.CollectionUtils;

public class WallpaperListProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(WallpaperModule.MODULE_NAME);
	
	private Context mContext;
    private int mColumn;
    private int mOffset;

    public WallpaperListProtocol(Context context, int column, int offset, Object tag) {
        setTag(tag);
        mContext = context;
        mColumn = column;
        mOffset = offset;
    }
    
	@Override
	protected int getProtocolId() {
		return 0x04;
	}

	@Override
	protected void process() {
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());

			List<TWallpaperResource> resources = client.getWallpaperList(getUserInfo(), 0, mColumn, mOffset, WallpaperConstants.STORE_WALLPAPER_LIST_PAGE_SIZE);
			
			ArrayList<Wallpaper[]> arrWallpapersList = new ArrayList<Wallpaper[]>();
			if (CollectionUtils.isNotEmpty(resources)) {
				arrWallpapersList = new ArrayList<Wallpaper[]>();
                Wallpaper[] arrWallpaper = null;
                for (int i = 0; i < resources.size(); i++) {
                    if(i%3 == 0)
                    	arrWallpaper = new Wallpaper[3];
                    arrWallpaper[i%3] = new Wallpaper(resources.get(i), mContext);
					if(i%3 == 2)
						arrWallpapersList.add(arrWallpaper);
                }
                if(resources.size()%3 != 0){
                	arrWallpapersList.add(arrWallpaper);
				}
                
                if (arrWallpapersList != null && arrWallpapersList.size() > 0) {//存入缓存
                   // mListener.onGetWallpaperListSucc(mColumn, mOffset, arrWallpapersList);
                    WallpaperManager.getInstance(mContext).saveWallpaperCache(mColumn, mOffset, arrWallpapersList);
                }
			}
			EventBus.getDefault().post(new GetWallpaperListSuccessEvent(arrWallpapersList, mColumn, mOffset, true, getTag()));
        } catch (TApplicationException e) {//服务器端无数据
            NqLog.d("BannerListProtocol process() server is empty");
            onError();
        } catch (Exception e) {
        	NqLog.e(e);
        	onError();
        }
	}
	@Override
	protected void onError() {
		EventBus.getDefault().post(new GetWallpaperListSuccessEvent(null, mColumn, mOffset, false, getTag()));
	}
	public static class GetWallpaperListSuccessEvent extends AbsProtocolEvent {
    	private ArrayList<Wallpaper[]> mResource;
    	private boolean isSuccess = false;
        private int mColumn;
        private int mOffset;
    	
    	public GetWallpaperListSuccessEvent(ArrayList<Wallpaper[]> resource, int column, int offset, boolean success, Object tag){
    		setTag(tag);
    		mResource = resource;
    		isSuccess = success;
    		mColumn = column;
    		mOffset = offset;
    	}

    	public ArrayList<Wallpaper[]> getWallpapers() {
    		NqLog.d("getWallpapers mColumn:" + mColumn + " offset:" + mOffset + " resource:" + mResource);
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
