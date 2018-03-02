package com.nqmobile.livesdk.commons.net;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.format.DateUtils;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.info.CommonDefine;
import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.utils.CommonMethod;

public class ThriftTransportPool {
	private static ThriftTransportPool sLauncherInstance;// = new ThriftTransportPool();
	private static ThriftTransportPool sWeatherInstance ;//= new ThriftTransportPool();
	
	public static final String TRUST_STORE_PASSWORD = "changeit";
	public static final String FILE_NAME = "truststore.bks";
	public static final int TIMEOUT = (int)(10 * DateUtils.SECOND_IN_MILLIS);
	public static final long KEEP_ALIVE_TIMEOUT = 5 * DateUtils.SECOND_IN_MILLIS;
	public static final long KEEP_ALIVE_CHECK_INTERVAL = 2 * DateUtils.SECOND_IN_MILLIS;

	
	private ConcurrentLinkedQueue<TransportHolder> mPool;
	private Object mPoolLock = new Object();
	
	private static HandlerThread sThread = new HandlerThread("NQ-HandlerThread");
	static{
		sThread.start();
	}
	private Handler mHandler;
	private KeepAliveRunnable mKeepAliveRunnable;

	private String mHost;
	private int mPort;
	public boolean mIsKeepAliveRunning;
	
	static {
		try {
			Context context = ApplicationContext.getContext();
			sLauncherInstance = new ThriftTransportPool();
			sLauncherInstance.mHost = CommonMethod.getLauncherUrl(context);
			sLauncherInstance.mPort = CommonDefine.APP_PORT;
			sWeatherInstance = new ThriftTransportPool();
			sWeatherInstance.mHost = CommonMethod.getWeatherUrl(context);
			sWeatherInstance.mPort = CommonDefine.WEATHER_PORT;
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	
	private ThriftTransportPool(){
		try {
			mHandler = new Handler(sThread.getLooper());
			mPool = new ConcurrentLinkedQueue<TransportHolder>();
			mKeepAliveRunnable = new KeepAliveRunnable();
			mHandler.postDelayed(mKeepAliveRunnable, KEEP_ALIVE_CHECK_INTERVAL);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static ThriftTransportPool getLauncherPool(){
		return sLauncherInstance;
	}
	
	public static ThriftTransportPool getWeatherPool(){
		return sWeatherInstance;
	}
	
	public TTransport getTransport() {
		TTransport result = null;

		synchronized (mPoolLock) {
			while (!mPool.isEmpty()) {
				TransportHolder holder = mPool.poll();
				TTransport transport = holder.transport;
				if (transport.isOpen()) {
					result = transport;
					break;
				}
			}
		}
		if (result == null) {
			result = createTransport();
		}

		return result;
	}
	
	public void closeTransport(TTransport transport){
		if (transport != null && transport.isOpen()) {
			TransportHolder holder = new TransportHolder();
			holder.transport = transport;
			holder.lastAliveTime = SystemFacadeFactory.getSystem().currentTimeMillis();
			synchronized (mPoolLock){
				mPool.add(holder);
				if (!mIsKeepAliveRunning){
					mHandler.postDelayed(mKeepAliveRunnable, KEEP_ALIVE_CHECK_INTERVAL);
				}
			}
		}
	}
	
	private TTransport createTransport() {
		TTransport transport = null;
		
		try {
			String trustPath = ApplicationContext.getContext().getFilesDir() + "/" + FILE_NAME;
			TSSLTransportFactory.TSSLTransportParameters params = new TSSLTransportFactory.TSSLTransportParameters();
			params.setTrustStore(trustPath, TRUST_STORE_PASSWORD, "X509",
					"BKS");
			transport = TSSLTransportFactory.getClientSocket(mHost, mPort,
					TIMEOUT, params);
		} catch (Exception e) {
			NqLog.e(e);throw new RuntimeException(e);
		}

		return transport;
	}
	
	private class TransportHolder {
		TTransport transport;
		long lastAliveTime;
	}
	
	private class KeepAliveRunnable implements Runnable {
		@Override
		public void run() {
			try {
				keepAlive();
				if (!mPool.isEmpty()){
					mHandler.postDelayed(this, KEEP_ALIVE_CHECK_INTERVAL);
					mIsKeepAliveRunning = true;
				} else {
					mIsKeepAliveRunning = false;
				}
			} catch (Exception e) {
				NqLog.e(e);
			}
		}

		private void keepAlive() {
			long now = SystemFacadeFactory.getSystem().currentTimeMillis();
			synchronized (mPoolLock) {
				while (!mPool.isEmpty()) {
					TransportHolder holder = mPool.peek();
					if (!holder.transport.isOpen()){
						mPool.poll();
					} else if (Math.abs(now - holder.lastAliveTime) >= KEEP_ALIVE_TIMEOUT) {
						holder.transport.close();
						mPool.poll();
					} else {
						break;
					}
				}
			}
		}
	}
	
}
