package com.nqmobile.livesdk.modules.weather;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.weather.model.Weather;

public interface WeatherListener extends Listener{

	public void getWeatherSucc(Weather w);
}
