package com.nqmobile.livesdk.modules.weather;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.text.TextUtils;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.commons.info.MobileInfo;
import com.nqmobile.livesdk.commons.info.NqTest;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.service.PeriodCheckEvent;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.modules.weather.model.City;
import com.nqmobile.livesdk.modules.weather.model.Weather;
import com.nqmobile.livesdk.modules.weather.network.GetCitiesProtocol.GetCitiesSuccessEvent;
import com.nqmobile.livesdk.modules.weather.network.GetPositonProtocol.GetPositionSuccessEvent;
import com.nqmobile.livesdk.modules.weather.network.GetWeatherProtocol.GetWeatherFailedEvent;
import com.nqmobile.livesdk.modules.weather.network.GetWeatherProtocol.GetWeatherSuccessEvent;
import com.nqmobile.livesdk.modules.weather.network.WeatherService;
import com.nqmobile.livesdk.modules.weather.network.WeatherServiceFactory;
import com.nqmobile.livesdk.utils.LocateUtil;
import com.nqmobile.livesdk.utils.LocateUtil.LocateListener;

public class WeatherManager extends AbsManager {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(WeatherModule.MODULE_NAME);
	// ===========================================================
	// Fields
	// ===========================================================
	private static WeatherManager sInstance;
	private List<String> mFavorCities;
	private WeatherPreference mPreference;
	private HashSet<WeatherUpdateListener> mWeatherUpdateListeners;
	private WeatherService mWeatherService;

	// ===========================================================
	// Constructors
	// ===========================================================
	private WeatherManager() {
		mFavorCities = new ArrayList<String>();
		mPreference = WeatherPreference.getInstance();
		mWeatherUpdateListeners = new HashSet<WeatherUpdateListener>();
		mWeatherService = WeatherServiceFactory.getService();
	}

	public synchronized static WeatherManager getInstance() {
		if (sInstance == null) {
			sInstance = new WeatherManager();
		}

		return sInstance;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public List<String> getFavorCities() {
		return mFavorCities;
	}

	public void setFavorCities(List<String> favorCities) {
		mFavorCities = favorCities;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	public void getCity(String keyword, String language, CityListener listener) {
		mWeatherService.getCities(keyword, listener);
	}

	public void getPosition(final GetPositionListener listener) {
		LocateListener locateListener = new LocateListener() {
			@Override
			public void onLocateFinish(String lat, String lon) {
				mWeatherService.getCity(Double.valueOf(lat),
						Double.valueOf(lon), listener);
			}

			@Override
			public void onLocateFailed() {
				String lat = mPreference
						.getStringValue(WeatherPreference.KEY_LAST_LAT);
				String lon = mPreference
						.getStringValue(WeatherPreference.KEY_LAST_LON);
				if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lon)) {
					mWeatherService.getCity(Double.valueOf(lat),
							Double.valueOf(lon), listener);
				} else {
					listener.onErr();
				}
			}
		};
		new LocationHelper(locateListener).getPosition();
	}

	public void getWeather(String cityId, String language,
			int days, int hours, WeatherListener listener) {
		getWeather(cityId, language, days, hours, listener, false);
	}
	
	private void getWeather(final String cityId, final String language,
			final int days, final int hours, final WeatherListener listener,
			final boolean isBackground) {
		if (!TextUtils.isEmpty(cityId)) {
			mWeatherService.getWeather(cityId, 0, 0, days, hours, listener, isBackground);
			return;
		}

		LocateListener locateListener = new LocateListener() {
			@Override
			public void onLocateFinish(String lat, String lon) {
				mWeatherService.getWeather(cityId, Double.valueOf(lat),
						Double.valueOf(lon), days, hours, listener, isBackground);
			}

			@Override
			public void onLocateFailed() {
				String lat = mPreference
						.getStringValue(WeatherPreference.KEY_LAST_LAT);
				String lon = mPreference
						.getStringValue(WeatherPreference.KEY_LAST_LON);
				if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lon)) {
					mWeatherService.getWeather(cityId, Double.valueOf(lat),
							Double.valueOf(lon), days, hours, listener, isBackground);
				} else {
					listener.onErr();
				}
			}
		};
		new LocationHelper(locateListener).getPosition();
	}

	@Override
	public void init() {
		EventBus.getDefault().register(this);
	}

	public boolean addWeatherUpdateListener(WeatherUpdateListener listener) {
		return mWeatherUpdateListeners.add(listener);
	}

	public boolean removeWeatherUpdateListener(WeatherUpdateListener listener) {
		return mWeatherUpdateListeners.remove(listener);
	}

	public void setWeatherUnitToFah(boolean value) {
    	WeatherPreference.getInstance().setBooleanValue(WeatherPreference.KEY_TEMPERATURE_UNIT_FAH, value);
	}
	
	public boolean isWeatherUnitToFah() {
		return WeatherPreference.getInstance().getBooleanValue(WeatherPreference.KEY_TEMPERATURE_UNIT_FAH);
	}
	
	public void onEvent(PeriodCheckEvent event) {
		if (!WeatherPolicy.needUpdateWeather()) {
			return;
		}

		if (mFavorCities == null || mFavorCities.size() == 0) {
			return;
		}

		String clientLanguage = ClientInfo
				.getClientLanguage(ApplicationContext.getContext());
		if (TextUtils.isEmpty(clientLanguage)) {
			return;
		}

		for (String s : mFavorCities) {
			getWeather(s, clientLanguage, 5, 0, new WeatherListener() {
				@Override
				public void getWeatherSucc(Weather w) {
					for (WeatherUpdateListener l : mWeatherUpdateListeners) {
						l.onWeatherUpdate(w);
					}
				}

				@Override
				public void onErr() {
				}
			}, true);
		}
	}

	public void onEvent(GetCitiesSuccessEvent event) {
		CityListener listener = null;

		List<City> cities = event.getCities();
		Object tag = event.getTag();
		if (tag != null && tag instanceof CityListener) {
			listener = (CityListener) event.getTag();
			listener.getCitySucc(cities);
		}
	}

	public void onEvent(GetPositionSuccessEvent event) {
		GetPositionListener listener = null;
		Object tag = event.getTag();
		City city = event.getCity();
		if (tag != null && tag instanceof GetPositionListener) {
			listener = (GetPositionListener) event.getTag();
			listener.getPositionSucc(city);
		}
	}

	public void onEvent(GetWeatherSuccessEvent event) {
		WeatherListener listener = null;

		Object tag = event.getTag();
		if (tag != null && tag instanceof WeatherListener) {
			listener = (WeatherListener) tag;
			Weather weather = event.getWeather();
			if (weather != null && weather.getCurrentWeather() != null && weather.getDailyForecasts() != null) {
				listener.getWeatherSucc(weather);
			} else {
				listener.onErr();
			}
		}
	}
	
	public void onEvent(GetWeatherFailedEvent event) {
		WeatherListener listener = null;

		Object tag = event.getTag();
		if (tag != null && tag instanceof WeatherListener) {
			listener = (WeatherListener) tag;
			listener.onErr();
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	private class LocationHelper {
		private LocateListener mListener;

		public LocationHelper(LocateListener listener) {
			mListener = listener;
		}

		public void getPosition() {
			if (NqTest.isNqTestEnable()) {
				String lat = MobileInfo.getLat();
				String lon = MobileInfo.getLon();

				if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lon)) {
					mListener.onLocateFinish(lat, lon);
					return;
				}
			}

			if (!WeatherPolicy.needUpdateLocation()) {
				String lat = mPreference
						.getStringValue(WeatherPreference.KEY_LAST_LAT);
				String lon = mPreference
						.getStringValue(WeatherPreference.KEY_LAST_LON);
				if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lon)) {
					mListener.onLocateFinish(lat, lon);
					return;
				}
			}

			LocateUtil util = new LocateUtil(ApplicationContext.getContext(),
					new LocateListener() {
						@Override
						public void onLocateFinish(String lat, String lon) {
							NqLog.i("onLocateFinish lat=" + lat + " lon=" + lon);
							saveLocation(lat, lon);
							mListener.onLocateFinish(lat, lon);
						}

						@Override
						public void onLocateFailed() {
							NqLog.i("onLocateFailed");
							mListener.onLocateFailed();
						}
					});
			int region = mPreference
					.getIntValue(WeatherPreference.KEY_SERVERREGION);
			util.startLocation(region);
		}

		private void saveLocation(String lat, String lon) {
			mPreference.setStringValue(WeatherPreference.KEY_LAST_LAT, lat);
			mPreference.setStringValue(WeatherPreference.KEY_LAST_LON, lon);
			mPreference.setLongValue(WeatherPreference.KEY_LAST_LOCATION_TIME,
					SystemFacadeFactory.getSystem().currentTimeMillis());
		}
	}
}
