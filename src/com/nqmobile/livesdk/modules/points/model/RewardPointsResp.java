package com.nqmobile.livesdk.modules.points.model;

import com.nq.interfaces.launcher.TRewardPointsResp;

import java.util.Map;

/**
 * Created by Rainbow on 2014/11/18.
 */
public class RewardPointsResp {

    public Map<String,Integer> respCodes;

    public PointsInfo pointInfo;

    public RewardPointsResp(TRewardPointsResp resp){
        respCodes = resp.respCodes;
        pointInfo = new PointsInfo(resp.pointsInfo);
    }
}
