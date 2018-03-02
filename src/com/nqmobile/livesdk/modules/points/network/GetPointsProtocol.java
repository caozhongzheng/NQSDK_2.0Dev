package com.nqmobile.livesdk.modules.points.network;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.userinfo.TPointsInfo;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.points.PointModule;
import com.nqmobile.livesdk.modules.points.model.PointsInfo;

/**
 * Created by Rainbow on 14-5-7.
 */
public class GetPointsProtocol extends AbsLauncherProtocol{
	private static final ILogger NqLog = LoggerFactory.getLogger(PointModule.MODULE_NAME);

    public GetPointsProtocol(Object tag) {
        setTag(tag);
    }

    @Override
    protected int getProtocolId() {
        return 0x28;
    }

    @Override
    protected void process() {
        NqLog.i("GetPointsProtocol process");
        try {
            TLauncherService.Iface client = TLauncherServiceClientFactory.getClient(getThriftProtocol());
            TPointsInfo info = client.getPoints(getUserInfo());
            NqLog.i("TPointsInfo info.point="+info.totalPoints+" expoint="+info.expirePoints+" extime="+info.expirePoints);
            EventBus.getDefault().post(new GetPointsSuccessEvent(new PointsInfo(info), getTag()));
        }catch (Exception e) {
            e.printStackTrace();onError();
        }
    }
    @Override
	protected void onError() {
    	EventBus.getDefault().post(new GetPointsFailEvent(getTag()));
	}
    public static class GetPointsSuccessEvent extends AbsProtocolEvent{

        public PointsInfo info;

        public GetPointsSuccessEvent(PointsInfo info,Object tag){
            setTag(tag);
            this.info = info;
        }
    }

    public static class GetPointsFailEvent extends AbsProtocolEvent{

        public GetPointsFailEvent(Object tag){
            setTag(tag);
        }
    }

	
}
