package com.nqmobile.livesdk.modules.points.model;

import com.nq.interfaces.launcher.TConsumePointsResp;

/**
 * Created by Rainbow on 2014/11/18.
 */
public class ConsumePointsResp {

    public int respCode;

    public PointsInfo pointsInfo;

    public ConsumePointsResp(TConsumePointsResp resp){
        respCode = resp.respCode;
        pointsInfo = new PointsInfo(resp.pointsInfo);
    }
}
