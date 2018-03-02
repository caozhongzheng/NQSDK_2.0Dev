package com.nqmobile.livesdk.modules.browserbandge;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.browserbandge.model.Bandge;

/**
 * 浏览器角标广告获取监听
 * @author caozhongzheng
 * @time 2014/5/22 15:33:44
 */
public interface BandgeListener extends Listener{
    public void onGetBandgeSucc(Bandge bandge);
}
