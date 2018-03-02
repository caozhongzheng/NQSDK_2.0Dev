package com.nqmobile.livesdk.modules.gamefolder_v2;

import java.util.List;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.app.App;

/**
 * 获取广告推荐应用列表，操作成功。
 * @author caozhongzheng
 */
public interface AdvertisementListListener extends Listener{
	public void getAdvertisementListSucc(List<App> ads);
}
