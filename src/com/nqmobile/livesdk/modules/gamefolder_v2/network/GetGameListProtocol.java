package com.nqmobile.livesdk.modules.gamefolder_v2.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TApplicationException;

import android.content.Context;

import com.nq.interfaces.launcher.TAppResource;
import com.nq.interfaces.launcher.TLauncherService;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.app.AppConverter;
import com.nqmobile.livesdk.modules.gamefolder_v2.GameFolderV2Module;
import com.nqmobile.livesdk.modules.gamefolder_v2.GameFolderV2Preference;
import com.nqmobile.livesdk.modules.gamefolder_v2.model.GameCache;

/**
 * 游戏文件夹广告协议
 * @author chenyanmin
 * @date 2014-11-11
 */
public class GetGameListProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(GameFolderV2Module.MODULE_NAME);
    private static final int COLUMN = 102; //thrift接口里： 3游戏排行栏目 4软件排行栏目 100 游戏文件夹 101 装机必备 102 新版游戏文件夹 110积分中心（不过滤GP） 111兑换时余额不足（过滤GP） 
    private Context mContext;
    private GameFolderV2Preference mPreference;

    public GetGameListProtocol(Object tag) {
        setTag(tag);
        mContext = ApplicationContext.getContext();
        mPreference = GameFolderV2Preference.getInstance();
    }
    
	@Override
	protected int getProtocolId() {
		return 0x02;//TODO 分配一个协议ID
	}

    @Override
    public void process() {
        try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());

            List<TAppResource> resources = client.getAppList(getUserInfo(), COLUMN, 0, 0);
			if (resources != null && resources.size() > 0){
				List<GameCache> apps = new ArrayList<GameCache>(resources.size());
				for(TAppResource ar : resources){
					GameCache app = new GameCache();
					AppConverter.convert(mContext, app, ar);
		            if (app != null){
		            	apps.add(app);
		            }
		        }
				EventBus.getDefault().post(new GetGameListSuccessEvent(apps, getTag()));
			}
        } catch (TApplicationException e) {//服务器端无数据
            NqLog.d("GetGameListProtocol process() server is empty");
            EventBus.getDefault().post(new GetGameListSuccessEvent(null, getTag()));
        } catch (Exception e) {
        	NqLog.e(e);onError();
        } finally {
        	mPreference.setLastGetGameAdTime(SystemFacadeFactory.getSystem().currentTimeMillis());
        }
    }
    @Override
	protected void onError() {
    	EventBus.getDefault().post(new GetGameListFailedEvent(getTag()));
	}
    public static class GetGameListSuccessEvent extends AbsProtocolEvent {
    	private List<GameCache> mApps;
    	
    	public GetGameListSuccessEvent(List<GameCache> apps, Object tag){
    		setTag(tag);
    		mApps = apps;
    	}

    	public List<GameCache> getApps() {
    		return mApps;
    	}
    }
    
    public static class GetGameListFailedEvent extends AbsProtocolEvent {
    	public GetGameListFailedEvent(Object tag) {
			setTag(tag);
		}
    }

	
}
