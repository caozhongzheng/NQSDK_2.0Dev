package com.nqmobile.livesdk.modules.points;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.points.model.RewardPointsResp;

/**
 * Created by Rainbow on 2014/11/18.
 */
public interface RewardPointsListener extends Listener {

    public void onRewardSucc(RewardPointsResp resp);
}
