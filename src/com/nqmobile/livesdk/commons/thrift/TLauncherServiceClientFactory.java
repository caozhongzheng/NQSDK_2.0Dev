package com.nqmobile.livesdk.commons.thrift;

import org.apache.thrift.protocol.TProtocol;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.thriftcommon.ClientInterfaceFactory;

public class TLauncherServiceClientFactory {
private static TLauncherService.Iface sMock;
	
	public static void setMock(TLauncherService.Iface mock) {
		sMock = mock;
	}

	public static TLauncherService.Iface getClient(TProtocol protocol) {
		if (sMock != null){
			return sMock;
		} else {
			return ClientInterfaceFactory.getClientInterface(
					TLauncherService.Iface.class, protocol);
		}
	}
}
