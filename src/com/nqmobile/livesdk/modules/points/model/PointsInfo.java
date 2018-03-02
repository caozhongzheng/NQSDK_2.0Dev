package com.nqmobile.livesdk.modules.points.model;

import com.nq.interfaces.userinfo.TPointsInfo;

/**
 * Created by Rainbow on 2014/11/18.
 */
public class PointsInfo {

    public int expirePoints;

    public long expireTime;

    public int totalPoints;

    public PointsInfo(TPointsInfo info){
        expirePoints = info.expirePoints;
        expireTime = info.expireTime;
        totalPoints = info.totalPoints;
    }
}
