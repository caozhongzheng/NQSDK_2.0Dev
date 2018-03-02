package com.nqmobile.livesdk.modules.banner.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TApplicationException;

import android.content.Context;

import com.nq.interfaces.launcher.TBannerResource;
import com.nq.interfaces.launcher.TGetBannerListResp;
import com.nq.interfaces.launcher.TLauncherService;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.banner.Banner;
import com.nqmobile.livesdk.modules.banner.BannerConstants;
import com.nqmobile.livesdk.modules.banner.BannerManager;
import com.nqmobile.livesdk.modules.banner.BannerModule;
import com.nqmobile.livesdk.utils.CollectionUtils;

public class BannerListProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(BannerModule.MODULE_NAME);
	
    private int mPlate;
    private Context mContext;

    public BannerListProtocol(Context context, int plate, Object tag) {
        setTag(tag);
        mContext = context;
        mPlate = plate;
    }
    
	@Override
	protected int getProtocolId() {
		return 0x13;
	}

	@Override
	protected void process() {
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());

			TGetBannerListResp resBannerLists = client.getBannerList_New2(getUserInfo(), convertToThriftPlate(mPlate));
            List<TBannerResource> bannerList = resBannerLists.bannerList;
            
			ArrayList<Banner> banners = new ArrayList<Banner>();
			if (CollectionUtils.isNotEmpty(bannerList)) {
                Banner banner;
                BannerManager bManager = BannerManager.getInstance(mContext);
                for (TBannerResource br : bannerList) {
                	banner = new Banner(br, mContext);
                	banner.setIntSourceType(BannerConstants.STORE_MODULE_TYPE_BANNER);
                	banner.setFromPlate(mPlate);
                    banners.add(banner);
                }

                if (banners != null && banners.size() > 0) {//存入缓存
                    //mListener.onGetBannerListSucc(banners);
                	bManager.saveBannerCache2(banners, mPlate);
                }
			}
			EventBus.getDefault().post(new GetBannerListSuccessEvent(banners, mPlate, true, getTag()));
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
		EventBus.getDefault().post(new GetBannerListSuccessEvent(null, mPlate, false, getTag()));
	}
	/**
	 * 转换客户端编号到服务端编号
	 * @param plate
	 * @return 0主题页， 1锁屏页， 2壁纸页， 3应用页
	 */
	private int convertToThriftPlate(int plate){
		int result = -1;
		
		if (plate == BannerConstants.STORE_PLATE_TYPE_APP){
			result = 3;
		} else if (plate == BannerConstants.STORE_PLATE_TYPE_THEME){
			result = 0;
		} else if (plate == BannerConstants.STORE_PLATE_TYPE_WALLPAPER){
			result = 2;
		} else if (plate == BannerConstants.STORE_PLATE_TYPE_LOCKER){
			result = 1;
		} 
		
		return result;
	}

	public static class GetBannerListSuccessEvent extends AbsProtocolEvent {
    	private ArrayList<Banner> mResource;
    	private boolean isSuccess = false;
        private int mPlate;
        
    	public GetBannerListSuccessEvent(ArrayList<Banner> resource, int plate, boolean success, Object tag){
    		setTag(tag);
    		mResource = resource;
    		isSuccess = success;
    		mPlate = plate;
    	}

    	public ArrayList<Banner> getBanners() {
    		return mResource;
    	}
    	
    	public boolean isSuccess(){
    		return isSuccess;
    	}
    	
    	public int getPlate() {
			return mPlate;
		}
    	
    }

	
}
