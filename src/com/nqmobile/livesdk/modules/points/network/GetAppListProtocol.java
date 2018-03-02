package com.nqmobile.livesdk.modules.points.network;

import java.util.ArrayList;
import java.util.List;

import com.nq.interfaces.launcher.TAppResource;
import com.nq.interfaces.launcher.TLauncherService;
import com.nqmobile.livesdk.commons.AppConstants;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppConstant;
import com.nqmobile.livesdk.modules.points.GetAppListTag;
import com.nqmobile.livesdk.modules.points.PointModule;
import com.nqmobile.livesdk.utils.CollectionUtils;

/**
 * Created by Rainbow on 2014/11/18.
 */
public class GetAppListProtocol extends AbsLauncherProtocol{
	private static final ILogger NqLog = LoggerFactory.getLogger(PointModule.MODULE_NAME);

    private int mColumn;

    public GetAppListProtocol(GetAppListTag tag){
        setTag(tag);
        mColumn = tag.column;
    }

    @Override
    protected int getProtocolId() {
        return 0x02;
    }

    @Override
    protected void process() {
        NqLog.i("GetAppListProtocol process!");
        try {
            TLauncherService.Iface client = TLauncherServiceClientFactory.getClient(getThriftProtocol());
            List<TAppResource> list = client.getAppList(getUserInfo(),mColumn,0, AppConstant.STORE_APP_LIST_PAGE_SIZE);
            NqLog.i("list.size="+list.size());
            if(!CollectionUtils.isEmpty(list)){
                List<App> appList = new ArrayList<App>();
                for(TAppResource t :list){
                    App app = new App(t,ApplicationContext.getContext());
                    app.setIntSourceType(AppConstants.STORE_MODULE_TYPE_POINTS_CENTER);
                    appList.add(app);
                }

                NqLog.i("appList.szie="+appList.size());

                EventBus.getDefault().post(new GetAppListSuccessEvent(appList,getTag()));
            }else{
                EventBus.getDefault().post(new GetAppListFailEvent(getTag()));
            }
        } catch (Exception e) {
            NqLog.e(e);onError();
        }
    }
    @Override
	protected void onError() {
    	EventBus.getDefault().post(new GetAppListFailEvent(getTag()));
	}
    public static class GetAppListSuccessEvent extends AbsProtocolEvent{

        public List<App> list;

        public GetAppListSuccessEvent(List<App> list,Object tag){
            setTag(tag);
            this.list = list;
        }
    }

    public static class GetAppListFailEvent extends AbsProtocolEvent{

        public GetAppListFailEvent(Object tag){
            setTag(tag);
        }
    }

	
}
