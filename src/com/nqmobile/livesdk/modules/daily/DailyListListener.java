package com.nqmobile.livesdk.modules.daily;

import java.util.List;

import com.nqmobile.livesdk.commons.net.Listener;

/**
 * 获取每日推荐应用列表，操作成功。
 */
public interface DailyListListener extends Listener{
	public void getDailyListSucc(List<Daily> dailys);
}
