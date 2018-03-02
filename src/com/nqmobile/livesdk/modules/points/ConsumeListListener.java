package com.nqmobile.livesdk.modules.points;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.points.model.ConsumeListResp;

/**
 * Created by Rainbow on 2014/11/18.
 */
public interface ConsumeListListener extends Listener{

    public void onConsumeListSucc(ConsumeListResp resp);
}
