package com.nqmobile.livesdk.modules.weather.network;

import java.util.List;

import com.nq.interfaces.weather.TCity;
import com.nq.interfaces.weather.TWeatherService;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.net.AbsWeatherProtocol;
import com.nqmobile.livesdk.commons.thrift.TWeatherServiceClientFactory;
import com.nqmobile.livesdk.modules.weather.WeatherModule;
import com.nqmobile.livesdk.modules.weather.model.City;
import com.nqmobile.livesdk.modules.weather.model.CityConverter;
import com.nqmobile.livesdk.utils.CollectionUtils;

/**
 * 按关键字搜索城市 协议类
 * @author chenyanmin
 * @date 2014-11-17
 */
public class GetCitiesProtocol extends AbsWeatherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(WeatherModule.MODULE_NAME);
	private String mKeyword;
	
	public GetCitiesProtocol(String keyword, Object tag) {
		mKeyword = keyword;
		setTag(tag);
	}
	
	@Override
	protected int getProtocolId() {
		return 0x11;
	}

	@Override
	protected void process() {
		NqLog.i("GetCitiesProtocol process! keyword=" + mKeyword);
		try {
			TWeatherService.Iface client = TWeatherServiceClientFactory.getClient(getThriftProtocol());
			List<TCity> cityList = client.getCities(getUserInfo(), mKeyword);
			if (CollectionUtils.isNotEmpty(cityList)) {
				List<City> cities = CityConverter.convert(cityList);
				EventBus.getDefault().post(new GetCitiesSuccessEvent(cities, getTag()));
			} else {
				EventBus.getDefault().post(new GetCitiesFailedEvent(getTag()));
			}			
		} catch (Exception e) {
			NqLog.e(e); onError();
		}
	}
	@Override
	protected void onError() {
		EventBus.getDefault().post(new GetCitiesFailedEvent(getTag()));
	}
	public static class GetCitiesSuccessEvent extends AbsProtocolEvent {
		private List<City> mCities;
		
		public GetCitiesSuccessEvent(List<City> cities, Object tag) {
			setTag(tag);
			mCities = cities;
		}	

		public List<City> getCities() {
			return mCities;
		}
	}
	
	public static class GetCitiesFailedEvent extends AbsProtocolEvent {
		public GetCitiesFailedEvent(Object tag) {
			setTag(tag);
		}	
	}

	
}
