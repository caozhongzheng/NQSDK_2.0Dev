package com.nqmobile.livesdk.modules.points;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.points.model.ConsumePointsResp;

/**
 * Created by Rainbow on 2014/11/18.
 */
public interface ConsumePointsListener extends Listener {

    public void onComsumeSucc(ConsumePointsResp resp);
}
