package com.nqmobile.livesdk.modules.banner;

import java.util.List;

import com.nqmobile.livesdk.commons.net.NetworkingListener;

/**
 * banner详情获取监听
 * @time 2013-12-5 下午5:28:56
 */
public interface BannerListListener extends NetworkingListener {
	public void onGetBannerListSucc(List<Banner> banners);
}
