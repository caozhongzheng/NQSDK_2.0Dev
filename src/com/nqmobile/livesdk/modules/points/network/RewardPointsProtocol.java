package com.nqmobile.livesdk.modules.points.network;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TRewardPointsResp;
import com.nq.interfaces.userinfo.TPointsChangeInfo;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.points.PointModule;
import com.nqmobile.livesdk.modules.points.PointsManager;
import com.nqmobile.livesdk.modules.points.RewardPointsTag;
import com.nqmobile.livesdk.modules.points.model.RewardPointsResp;
import com.nqmobile.livesdk.modules.points.table.RewardPointsTable;

/**
 * Created by Rainbow on 14-5-7.
 */
public class RewardPointsProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(PointModule.MODULE_NAME);

    private String mTrackid;

    private static final Object LOCK = new Object();

    public RewardPointsProtocol(RewardPointsTag tag) {
        setTag(tag);
        mTrackid = tag.trackId;
    }

    @Override
    protected int getProtocolId() {
        return 0x27;
    }

    @Override
    protected void process() {
        NqLog.i("RewardPointsProtocol process");
        try {
            List<TPointsChangeInfo> mList = getChangeList();
            if(mList.size() == 0){
                RewardPointsFailEvent e = new RewardPointsFailEvent(getTag());
                EventBus.getDefault().post(e);
                return;
            }

            synchronized (LOCK){
                TLauncherService.Iface client = TLauncherServiceClientFactory.getClient(getThriftProtocol());
                TRewardPointsResp resp = client.rewardPointsNew(getUserInfo(),mList);
                RewardPointsResp rs = new RewardPointsResp(resp);
                EventBus.getDefault().post(new RewardPointsSuccessEvent(rs,getTag()));
            }
        }catch (Exception e) {
            NqLog.e(e);onError();
        }
    }
    @Override
	protected void onError() {
    	EventBus.getDefault().post(new RewardPointsFailEvent(getTag()));
	}
    private List<TPointsChangeInfo> getChangeList(){
        List<TPointsChangeInfo> list = new ArrayList<TPointsChangeInfo>();
        NqLog.i("mTrackid="+mTrackid);
        if(mTrackid != null){
            list.add(PointsManager.getInstance(ApplicationContext.getContext()).getPointInfo(mTrackid));
        }else{
            Cursor c = null;
            try{
                c = ApplicationContext.getContext().getContentResolver().query(RewardPointsTable.TABLE_URI, null,null, null, null);
                while(c != null && c.moveToNext()){
                    String trackId = c.getString(c.getColumnIndex(RewardPointsTable.REWARD_POINTS_HISTORY_TRACKID));
                    TPointsChangeInfo info = new TPointsChangeInfo();
                    info.clientTime = System.currentTimeMillis();
                    info.trackId = trackId;
                    info.points = c.getInt(c.getColumnIndex(RewardPointsTable.REWARD_POINTS_HISTORY_POINTS));
                    info.resourceId = c.getString(c.getColumnIndex(RewardPointsTable.REWARD_POINTS_HISTORY_RESID));
                    info.scene = c.getString(c.getColumnIndex(RewardPointsTable.REWARD_POINTS_HISTORY_SCENE));
                    list.add(info);
                }
            }finally {
                if(c != null){
                    c.close();
                }
            }
        }

        return list;
    }

    public static class RewardPointsSuccessEvent extends AbsProtocolEvent{

        public RewardPointsResp resp;

        public RewardPointsSuccessEvent(RewardPointsResp resp,Object tag){
            setTag(tag);
            this.resp = resp;
        }
    }

    public static class RewardPointsFailEvent extends AbsProtocolEvent{

        public String trackId;

        public RewardPointsFailEvent(Object tag) {
            setTag(tag);
        }
    }

	
}
