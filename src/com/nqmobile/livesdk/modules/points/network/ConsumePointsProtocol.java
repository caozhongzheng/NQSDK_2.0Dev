package com.nqmobile.livesdk.modules.points.network;

import android.database.Cursor;

import com.nq.interfaces.launcher.TConsumePointsResp;
import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.userinfo.TPointsChangeInfo;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.points.PointModule;
import com.nqmobile.livesdk.modules.points.model.ConsumePointsResp;
import com.nqmobile.livesdk.modules.theme.table.ThemeCacheTable;

/**
 * Created by Rainbow on 14-5-7.
 */
public class ConsumePointsProtocol extends AbsLauncherProtocol{
	private static final ILogger NqLog = LoggerFactory.getLogger(PointModule.MODULE_NAME);

    private static final String SCENE = "2000'";

    private String mResId;

    public ConsumePointsProtocol(String resId,Object tag) {
        setTag(tag);
        mResId = resId;
    }

    @Override
    protected int getProtocolId() {
        return 0x31;
    }

    @Override
    protected void process() {
        try{
            TPointsChangeInfo info = getInfo(mResId);
            if(info != null){
                TLauncherService.Iface client = TLauncherServiceClientFactory.getClient(getThriftProtocol());
                TConsumePointsResp resp = client.consumePointsNew(getUserInfo(), info);
                if(resp != null){
                    ConsumePointsResp rs = new ConsumePointsResp(resp);
                    EventBus.getDefault().post(new ConsumePointsSucessEvent(rs,getTag()));
                }else{
                    EventBus.getDefault().post(new ConsumePointsFailEvent(getTag()));
                }
            }
        }catch (Exception e){
            NqLog.e(e);onError();
        }
    }
    @Override
	protected void onError() {
    	EventBus.getDefault().post(new ConsumePointsFailEvent(getTag()));
	}
    private TPointsChangeInfo getInfo(String resId){
        TPointsChangeInfo info = new TPointsChangeInfo();
        Cursor c = null;
        try{
            c = ApplicationContext.getContext().getContentResolver().query(ThemeCacheTable.THEME_CACHE_URI, null,
                    ThemeCacheTable.THEME_ID + " = ?", new String[]{resId}, null);
            while(c != null && c.moveToNext()){
                info.clientTime = System.currentTimeMillis();
                info.trackId = "";
                info.points = c.getInt(c.getColumnIndex(ThemeCacheTable.THEME_COSUMEPOINTS));
                info.resourceId = resId;
                info.scene = SCENE;
            }
        }finally {
            if(c != null){
                c.close();
            }
        }
        return info;
    }

    public static class ConsumePointsSucessEvent extends AbsProtocolEvent{

        public ConsumePointsResp resp;

        public ConsumePointsSucessEvent(ConsumePointsResp resp,Object tag){
            setTag(tag);
            this.resp = resp;
        }
    }

    public static class ConsumePointsFailEvent extends AbsProtocolEvent{

        public ConsumePointsFailEvent(Object tag){
            setTag(tag);
        }
    }

	
}
