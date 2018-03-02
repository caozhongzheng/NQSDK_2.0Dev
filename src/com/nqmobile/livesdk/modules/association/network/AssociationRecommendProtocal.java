package com.nqmobile.livesdk.modules.association.network;

import org.apache.thrift.TApplicationException;

import com.nq.interfaces.launcher.TAssociateResp;
import com.nq.interfaces.launcher.TLauncherService;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.association.AssociationModule;
import com.nqmobile.livesdk.modules.association.AssociationPreference;

/**
 * 关联推荐获取
 * @author liujiancheng
 * @time 2014-11-18
 */
public class AssociationRecommendProtocal extends AbsLauncherProtocol{
	private static final ILogger NqLog = LoggerFactory.getLogger(AssociationModule.MODULE_NAME);
	
	private AssociationPreference mPreference;

    public AssociationRecommendProtocal(Object tag) {
        setTag(tag);
        mPreference = AssociationPreference.getInstance();
    }
    
	@Override
	protected int getProtocolId() {
		return 0x35;
	}

    @Override
    public void process() {
        try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());

			String versionTag = mPreference.getStringValue(AssociationPreference.KEY_MAX_ASSOCIATION_LAST_VERSIONTAG);
			
			TAssociateResp resp = client.getAssociationApps(getUserInfo(), versionTag);			
			if (resp != null){
				EventBus.getDefault().post(new GetAssociationSuccessEvent(resp, getTag()));
			} else {
	            EventBus.getDefault().post(new GetAssociationSuccessEvent(null, getTag()));
			}
        } catch (TApplicationException e) {//服务器端无数据
            NqLog.d("AssociationRecommendProtocal process() server is empty");
            EventBus.getDefault().post(new GetAssociationSuccessEvent(null, getTag()));
        } catch (Exception e) {
        	NqLog.e(e);onError();
        } finally {

        }
    }
    @Override
	protected void onError() {
    	EventBus.getDefault().post(new GetAssociationFailedEvent(getTag()));
	}
    
    public static class GetAssociationSuccessEvent extends AbsProtocolEvent {
    	private TAssociateResp mAssociationResp;
    	
    	public GetAssociationSuccessEvent(TAssociateResp resp, Object tag){
    		setTag(tag);
    		mAssociationResp = resp;
    	}

    	public TAssociateResp getAssociationResp() {
    		return mAssociationResp;
    	}
    }
    
    public static class GetAssociationFailedEvent extends AbsProtocolEvent {
    	public GetAssociationFailedEvent(Object tag) {
			setTag(tag);
		}
    }

	
}
