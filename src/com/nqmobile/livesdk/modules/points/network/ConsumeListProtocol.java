package com.nqmobile.livesdk.modules.points.network;

import com.nq.interfaces.launcher.TConsumeListResp;
import com.nq.interfaces.launcher.TLauncherService;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.points.PointModule;
import com.nqmobile.livesdk.modules.points.model.ConsumeListResp;

/**
 * Created by Rainbow on 14-5-21.
 */
public class ConsumeListProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(PointModule.MODULE_NAME);

    public ConsumeListProtocol(Object tag) {
        setTag(tag);
    }

    @Override
    protected int getProtocolId() {
        return 0x32;
    }

    @Override
    protected void process() {
        NqLog.i("ConsumeListProtocol process");
        try {
            TLauncherService.Iface client = TLauncherServiceClientFactory.getClient(getThriftProtocol());
            TConsumeListResp resp = client.getConsumeListNew(getUserInfo());
            ConsumeListResp rs = new ConsumeListResp(resp);
            EventBus.getDefault().post(new ConsumeListSuccessEvent(rs, getTag()));
        }catch (Exception e) {
            NqLog.e(e);onError();
        }
    }
    @Override
	protected void onError() {
    	EventBus.getDefault().post(new ConsumeListFailedEvent(getTag()));
	}
    public static class ConsumeListSuccessEvent extends AbsProtocolEvent{

        public ConsumeListResp mResp;

        public ConsumeListSuccessEvent(ConsumeListResp resp,Object tag){
            setTag(tag);
            mResp = resp;
        }
    }

    public static class ConsumeListFailedEvent extends AbsProtocolEvent {
        public ConsumeListFailedEvent(Object tag) {
            setTag(tag);
        }
    }

	
}
