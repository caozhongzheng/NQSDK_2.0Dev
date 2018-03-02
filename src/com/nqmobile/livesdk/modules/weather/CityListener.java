package com.nqmobile.livesdk.modules.weather;

import java.util.List;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.weather.model.City;

public interface CityListener extends Listener{
	
	/**
	 * 获取城市结果成功
	 * @param list 城市列表
	 */
	public void getCitySucc(List<City> list);
}
