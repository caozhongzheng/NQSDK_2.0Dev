package com.nqmobile.livesdk.modules.weather;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.weather.model.Weather;

public interface WeatherUpdateListener extends Listener{
	
	/**
	 * @param w
	 */
	public void onWeatherUpdate(Weather w);
}
