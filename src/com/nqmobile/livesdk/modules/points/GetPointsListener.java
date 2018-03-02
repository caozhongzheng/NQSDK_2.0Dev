package com.nqmobile.livesdk.modules.points;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.points.model.PointsInfo;

/**
 * Created by Rainbow on 2014/11/18.
 */
public interface GetPointsListener extends Listener {

    public void onGetPointsSucc(PointsInfo info);
}

