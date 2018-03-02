package com.nqmobile.livesdk.modules.weather.network;


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

/**
 * 自动定位城市 协议类
 * @author chenyanmin
 * @date 2014-11-17
 */
public class GetPositonProtocol extends AbsWeatherProtocol{
	private static final ILogger NqLog = LoggerFactory.getLogger(WeatherModule.MODULE_NAME);
	
	private double mLat;
	private double mLon;

    public GetPositonProtocol(double lat, double lon, Object tag) {
        mLat = lat;
        mLon = lon;
        setTag(tag);
    }
    
	@Override
	protected int getProtocolId() {
		return 0x19;
	}

    @Override
    protected void process() {
        NqLog.i("GetPositonProtocol process");
        try {
            TWeatherService.Iface client = TWeatherServiceClientFactory.getClient(getThriftProtocol());
            TCity c = client.getCity(getUserInfo(), mLat, mLon);
            if (c != null) {
            	City city = new City(c);
            	EventBus.getDefault().post(new GetPositionSuccessEvent(city, getTag()));
            } else {
            	EventBus.getDefault().post(new GetPositionFailedEvent(getTag()));
            }
        } catch(Exception e){
            NqLog.e(e); onError();
        }
    }
    @Override
	protected void onError() {
    	EventBus.getDefault().post(new GetPositionFailedEvent(getTag()));
	}
    public class GetPositionSuccessEvent extends AbsProtocolEvent {
    	private City mCity;
    	
    	public GetPositionSuccessEvent(City city, Object tag) {
    		setTag(tag);
    		mCity = city;
    	}	

    	public City getCity() {
    		return mCity;
    	}
    }
    
    public class GetPositionFailedEvent extends AbsProtocolEvent {
    	public GetPositionFailedEvent(Object tag) {
    		setTag(tag);
    	}	
    }

	
}
