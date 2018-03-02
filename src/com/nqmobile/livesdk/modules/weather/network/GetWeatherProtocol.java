package com.nqmobile.livesdk.modules.weather.network;


import com.nq.interfaces.weather.TWeather;
import com.nq.interfaces.weather.TWeatherService;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.net.AbsWeatherProtocol;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.commons.thrift.TWeatherServiceClientFactory;
import com.nqmobile.livesdk.modules.weather.WeatherModule;
import com.nqmobile.livesdk.modules.weather.WeatherPreference;
import com.nqmobile.livesdk.modules.weather.model.Weather;

public class GetWeatherProtocol extends AbsWeatherProtocol{
	private static final ILogger NqLog = LoggerFactory.getLogger(WeatherModule.MODULE_NAME);
	
    private WeatherPreference mPreference;
	private String mCityId;
	private double mLat;
	private double mLon;
	private int mDays;
	private int mHours;

	public GetWeatherProtocol(String cityId, double lat, double lon, int days, int hours, Object tag) {
		mCityId = cityId;
		mLat = lat;
		mLon = lon;
		mDays = days;
		mHours = hours;
		setTag(tag);
		
		mPreference = WeatherPreference.getInstance();
	}
	
	@Override
	protected int getProtocolId() {
		return 0x1;
	}

	@Override
	public void process() {
		NqLog.i("WeatherProtocol process!");
		try {
			TWeatherService.Iface client = TWeatherServiceClientFactory
					.getClient(getThriftProtocol());
			TWeather w = client.getWeather(getUserInfo(), mCityId, mLat, mLon,
					mDays, mHours);
			if ( w != null ) {
				Weather weather = new Weather(w);
				EventBus.getDefault().post(new GetWeatherSuccessEvent(weather, getTag()));
			} else {
				EventBus.getDefault().post(new GetWeatherFailedEvent(getTag()));
			}
		} catch(Exception e){
			NqLog.e(e);onError();
		} finally {
			mPreference.setLongValue(
					WeatherPreference.KEY_LAST_GET_WEATHER_TIME,
					SystemFacadeFactory.getSystem().currentTimeMillis());
		}
	}
	@Override
	protected void onError() {
		EventBus.getDefault().post(new GetWeatherFailedEvent(getTag()));
	}
	public static class GetWeatherSuccessEvent extends AbsProtocolEvent {
		private Weather mWeather;
		
		public GetWeatherSuccessEvent(Weather weather, Object tag) {
			setTag(tag);
			mWeather = weather;
		}	

		public Weather getWeather() {
			return mWeather;
		}
	}
	
	public static class GetWeatherFailedEvent extends AbsProtocolEvent {
		public GetWeatherFailedEvent(Object tag) {
			setTag(tag);
		}
	}


}
