package com.nqmobile.livesdk.commons.info;

import com.nqmobile.livesdk.commons.log.NqLog;

public class ClientInfoFactory {
	private static IClientInfo sMock;

	public static void setMock(IClientInfo mock) {
		sMock = mock;
	}

	public static IClientInfo getInstance() {
		if (sMock != null) {
			return sMock;
		} else {
			return loadClientInfo();
		}
	}
	
	private static IClientInfo loadClientInfo() {
		IClientInfo clientInfo = null;
		Class<?> clz = null;
		try {
			clz = Class.forName("com.nqmobile.livesdk.commons.info.CustomClientInfo");
			if (clz != null) {
				clientInfo = (IClientInfo) clz.newInstance();
			}
		} catch (ClassNotFoundException e) {
			NqLog.i("CustomClientInfo is not existing");
		} catch (Exception e) {
			NqLog.e(e);
		}
		
		if (clientInfo == null){
			clientInfo = new BaseClientInfo();
		}
		
		return clientInfo;
	}
}
