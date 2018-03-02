package com.nqmobile.livesdk.modules.weather.network;

import com.nqmobile.livesdk.commons.net.AbsService;
import com.nqmobile.livesdk.commons.net.ServiceLock;

/**
 * 天气服务类，将外部请求提交到线程池执行
 * @author chenyanmin
 * @date 2014-11-16
 */
public class WeatherService extends AbsService {
	public void getCities(String keyword, Object tag){
		getExecutor().submit(new GetCitiesProtocol(keyword, tag));
	}

	public void getCity(double lat, double lon, Object tag){
		getExecutor().submit(new GetPositonProtocol(lat, lon, tag));
	}

	public void getWeather(String cityId, final double lat, double lon, int days, int hours, Object tag, boolean isBackground){
		if (isBackground) {
			ServiceLock lock = ServiceLock.getInstance(GetWeatherProtocol.class
					.getName());
			if (!lock.available()) {
				return;
			}
		}
		getExecutor().submit(new GetWeatherProtocol(cityId, lat, lon, days, hours, tag));
	}
}
