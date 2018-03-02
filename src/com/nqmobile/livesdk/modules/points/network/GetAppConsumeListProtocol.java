package com.nqmobile.livesdk.modules.points.network;

import java.util.ArrayList;
import java.util.List;

import com.nq.interfaces.launcher.TAppResource;
import com.nq.interfaces.launcher.TLauncherService;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppConstant;
import com.nqmobile.livesdk.modules.points.PointModule;
import com.nqmobile.livesdk.utils.CollectionUtils;

/**
 * Created by Rainbow on 2014/11/18.
 */
public class GetAppConsumeListProtocol extends AbsLauncherProtocol{
	private static final ILogger NqLog = LoggerFactory.getLogger(PointModule.MODULE_NAME);

    private static final int COLUMN = 111;

    public GetAppConsumeListProtocol(Object tag){
        setTag(tag);
    }

    @Override
    protected int getProtocolId() {
        return 0x02;
    }

    @Override
    protected void process() {
        try {
            TLauncherService.Iface client = TLauncherServiceClientFactory.getClient(getThriftProtocol());
            List<TAppResource> list = client.getAppList(getUserInfo(),COLUMN,0, AppConstant.STORE_APP_LIST_PAGE_SIZE);
            if(!CollectionUtils.isEmpty(list)){
                List<App> appList = new ArrayList<App>();
                for(TAppResource t :list){
                    appList.add(new App(t, ApplicationContext.getContext()));
                }

                EventBus.getDefault().post(new GetAppConsumeListSuccessEvent(appList,getTag()));
            }else{
                EventBus.getDefault().post(new GetAppConsumeListFailEvent(getTag()));
            }
        } catch (Exception e) {
            NqLog.e(e);onError();
        }
    }
    @Override
	protected void onError() {
    	EventBus.getDefault().post(new GetAppConsumeListFailEvent(getTag()));
	}
    public static class GetAppConsumeListSuccessEvent extends AbsProtocolEvent {

        public List<App> list;

        public GetAppConsumeListSuccessEvent(List<App> list,Object tag){
            setTag(tag);
            this.list = list;
        }
    }

    public static class GetAppConsumeListFailEvent extends AbsProtocolEvent{

        public GetAppConsumeListFailEvent(Object tag){
            setTag(tag);
        }
    }

	
}
