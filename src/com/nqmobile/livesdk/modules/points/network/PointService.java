package com.nqmobile.livesdk.modules.points.network;

import com.nqmobile.livesdk.commons.net.AbsService;
import com.nqmobile.livesdk.modules.points.GetAppListTag;
import com.nqmobile.livesdk.modules.points.GetThemeListTag;
import com.nqmobile.livesdk.modules.points.RewardPointsTag;

/**
 * Created by Rainbow on 2014/11/18.
 */
public class PointService extends AbsService{

    public void getConsumeList(Object tag){
        getExecutor().submit(new ConsumeListProtocol(tag));
    }

    public void getPoints(Object tag){
        getExecutor().submit(new GetPointsProtocol(tag));
    }

    public void consumePoints(String resId,Object tag){
        getExecutor().submit(new ConsumePointsProtocol(resId,tag));
    }

    public void rewardPoints(RewardPointsTag tag){
        getExecutor().submit(new RewardPointsProtocol(tag));
    }

    public void getAppList(GetAppListTag tag){
        getExecutor().submit(new GetAppListProtocol(tag));
    }

    public void getThemeList(GetThemeListTag tag){
        getExecutor().submit(new GetThemeListProtocol(tag));
    }
}
