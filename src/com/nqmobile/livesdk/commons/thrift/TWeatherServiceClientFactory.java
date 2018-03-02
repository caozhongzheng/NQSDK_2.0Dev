package com.nqmobile.livesdk.commons.thrift;

import org.apache.thrift.protocol.TProtocol;

import com.nq.interfaces.weather.TWeatherService;
import com.nq.thriftcommon.ClientInterfaceFactory;

public class TWeatherServiceClientFactory {
private static TWeatherService.Iface sMock;
	
	public static void setMock(TWeatherService.Iface mock) {
		sMock = mock;
	}

	public static TWeatherService.Iface getClient(TProtocol protocol) {
		if (sMock != null){
			return sMock;
		} else {
			return ClientInterfaceFactory.getClientInterface(
					TWeatherService.Iface.class, protocol);
		}
	}
}
