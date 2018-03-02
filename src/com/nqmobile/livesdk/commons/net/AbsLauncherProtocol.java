package com.nqmobile.livesdk.commons.net;



public abstract class AbsLauncherProtocol extends AbsProtocol {
	@Override
	protected ThriftTransportPool getThriftTransportPool() {
		return ThriftTransportPool.getLauncherPool();
	}
	
}
