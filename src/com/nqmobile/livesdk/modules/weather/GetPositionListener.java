package com.nqmobile.livesdk.modules.weather;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.weather.model.City;

/**
 * Created by Rainbow on 14-3-10.
 */
public interface GetPositionListener extends Listener{

    public void getPositionSucc(City c);
}
