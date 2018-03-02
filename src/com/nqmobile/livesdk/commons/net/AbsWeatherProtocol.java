package com.nqmobile.livesdk.commons.net;



/**
 * 天气协议基类
 * @author chenyanmin
 * @date 2014-11-17
 */
public abstract class AbsWeatherProtocol extends AbsProtocol {
	@Override
	protected ThriftTransportPool getThriftTransportPool() {
		return ThriftTransportPool.getWeatherPool();
	}
}
