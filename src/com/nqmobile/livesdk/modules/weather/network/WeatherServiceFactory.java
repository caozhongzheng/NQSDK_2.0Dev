package com.nqmobile.livesdk.modules.weather.network;


/**
 * 天气服务工厂
 * @author chenyanmin
 * @date 2014-11-12
 */
public class WeatherServiceFactory {
	private static WeatherService sMock;
	
	public static void setMock(WeatherService mock) {
		sMock = mock;
	}

	public static WeatherService getService() {
		if (sMock != null){
			return sMock;
		} else {
			return new WeatherService();
		}
	}
}
