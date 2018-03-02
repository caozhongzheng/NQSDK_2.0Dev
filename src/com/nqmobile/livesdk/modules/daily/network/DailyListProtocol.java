package com.nqmobile.livesdk.modules.daily.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TApplicationException;

import android.content.Context;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TWidgetResource;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.daily.Daily;
import com.nqmobile.livesdk.modules.daily.DailyManager;
import com.nqmobile.livesdk.modules.daily.DailyModule;
import com.nqmobile.livesdk.utils.CollectionUtils;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;

public class DailyListProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(DailyModule.MODULE_NAME);
	
	private Context mContext;
	private int mSize;

    public DailyListProtocol(Context context, int size, Object tag) {
        setTag(tag);
        mContext = context;
        mSize = size;
    }
    
	@Override
	protected int getProtocolId() {
		return 0x14;
	}

	@Override
	protected void process() {
		try {
			int lfEngine = PreferenceDataHelper.getInstance(mContext).getIntValue(PreferenceDataHelper.KEY_LOCKER_ENGINE_LF);
            int ldEngine = PreferenceDataHelper.getInstance(mContext).getIntValue(PreferenceDataHelper.KEY_LOCKER_ENGINE_LD);
            
            Map<String, String> mMap = new HashMap<String, String>();
            mMap.put(PreferenceDataHelper.KEY_LOCKER_ENGINE_LF, String.valueOf(lfEngine));
            mMap.put(PreferenceDataHelper.KEY_LOCKER_ENGINE_LD, String.valueOf(ldEngine));
            NqLog.d("DailyListProtocol process() lfEngine=" + lfEngine + ",ldEngine=" + ldEngine);

            TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());

            List<TWidgetResource> resources = client.getDailyList_New(getUserInfo(), mMap);
            ArrayList<Daily> dailys = new ArrayList<Daily>();
			if (CollectionUtils.isNotEmpty(resources)) {
                int seq = 0;
                for (TWidgetResource ar : resources) {
                    if (ar != null) {
                    	Daily daily = new Daily(ar, mContext);
                        daily.setSeq(seq++);
                        dailys.add(daily);
                    }
                    NqLog.i("DailyResource r="+ar);
                }
                if (dailys.size() > 0) {//存入缓存
                    DailyManager.getInstance(mContext).saveDailyCache(dailys);
                }
			}
			EventBus.getDefault().post(new GetDailyListSuccessEvent(dailys, mSize, true, getTag()));
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
		EventBus.getDefault().post(new GetDailyListSuccessEvent(null, mSize, false, getTag()));
	}
	public static class GetDailyListSuccessEvent extends AbsProtocolEvent {
    	private ArrayList<Daily> mResource;
    	private boolean isSuccess = false;
    	private int mSize;
        
    	public GetDailyListSuccessEvent(ArrayList<Daily> resource, int size, boolean success, Object tag){
    		setTag(tag);
    		mResource = resource;
    		isSuccess = success;
    		mSize = size;
    	}

    	public ArrayList<Daily> getDailys() {
    		return mResource;
    	}
    	
    	public boolean isSuccess(){
    		return isSuccess;
    	}
    	
    	public int getSize(){
    		return mSize;
    	}
    }

    
}
