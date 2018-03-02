package com.nqmobile.livesdk.modules.locker.network;

import org.apache.thrift.TApplicationException;

import android.content.Context;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TLockerEnginReq;
import com.nq.interfaces.launcher.TLockerEngine;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsEvent;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.locker.LockerModule;
import com.nqmobile.livesdk.modules.locker.model.LockerEngine;
import com.nqmobile.livesdk.modules.locker.model.LockerEngineConverter;

/**
 * 获取最新引擎版本Protocol
 *
 * @author caozhongzheng
 * @time 2014/5/21 下午6:56:09
 */
public class GetNewLockerEngineProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(LockerModule.MODULE_NAME);
	
    private Context mContext;
    private int mType;
    private int mVersion;

    public GetNewLockerEngineProtocol(int type, int version, Object tag) {
        mType = type;
        mVersion = version;
        setTag(tag);
    }
    
	@Override
	protected int getProtocolId() {
		return 0x29;
	}

    @Override
    public void process() {
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());
            TLockerEnginReq req = new TLockerEnginReq();
    		req.setType(mType);
    		req.setVersion(mVersion);
            TLockerEngine resources = client.getNewLockerEngine(getUserInfo(), req);
            
            if (resources != null) {
            	LockerEngine lockerEngine = LockerEngineConverter.convert(mContext, resources);
            	GetNewLockerEngineSuccessEvent event = new GetNewLockerEngineSuccessEvent(lockerEngine, getTag());
				EventBus.getDefault().post(event);
            } else{
            	EventBus.getDefault().post(new GetNewLockerEngineSuccessEvent(null, getTag()));
            }
        } catch (TApplicationException e) {//服务器端无数据
            NqLog.e(e);
			EventBus.getDefault().post(new GetNewLockerEngineSuccessEvent(null, getTag()));
        } catch (Exception e) {
        	NqLog.e(e);
            onError();
        } finally {
        }
    }
    
    public static class GetNewLockerEngineSuccessEvent extends AbsEvent {
    	private LockerEngine mLockerEngine;

    	public GetNewLockerEngineSuccessEvent(LockerEngine lockerEngine, Object tag) {
    		mLockerEngine = lockerEngine;
    		setTag(tag);
    	}

    	public LockerEngine getLockerEngine() {
    		return mLockerEngine;
    	}
    }
    
    public static class GetNewLockerEngineFailedEvent extends AbsEvent {

    	public GetNewLockerEngineFailedEvent(Object tag) {
    		setTag(tag);
    	}
    }

	@Override
	protected void onError() {
		EventBus.getDefault().post(new GetNewLockerEngineFailedEvent(getTag()));		
	}


}
