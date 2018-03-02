package com.nqmobile.livesdk.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import com.nqmobile.livesdk.commons.log.NqLog;

public class HttpUtil {
	private HttpObserver mObserver;
	
	public final static int ERROR_NONE = 0;
	public final static int ERROR_GENERAL = 1;
	
	private static final int CONNECT_TIMEOUT = 15000; // default value
	private static final int READ_TIMEOUT = 45000; // default value
	
	private final static int BUFFER_SIZE = 2 * 1024;
	
	private ByteArrayOutputStream mResponseDataStream = null;
	
	private boolean mCancle;
	
	private HttpClient httpClient;
	
	private int responseCode;

	public HttpUtil(HttpObserver ob){
		mObserver = ob;
		
		mResponseDataStream = new ByteArrayOutputStream();
	}
	
	public void cancle(){
		mCancle = true;
	}
	
	public void doGet(String url) {
		try {
			initHttpClient();
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader("Accept", "*/*");
			httpGet.setHeader("Connection", "close");
			httpGet.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECT_TIMEOUT);
			httpGet.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,READ_TIMEOUT);

			HttpResponse httpResponse = httpClient.execute(httpGet);
			processResponse(httpResponse);
			if (!mCancle) {
				mObserver.onHttpResult(ERROR_NONE,mResponseDataStream.toByteArray());
				
				mResponseDataStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			mObserver.onHttpResult(ERROR_GENERAL, null);
		}
	}
	
	private void initHttpClient() throws Exception{
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(null, null);

		SSLSocketFactory sf = new EasySSLSocketFactory(trustStore);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", sf, 443));

		ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

		httpClient = new DefaultHttpClient(ccm, params);
	}
	
    public void doPost(String url,byte[] data){
    	NqLog.i("HttpUtil doPose url="+url);
    	NqLog.i(new String(data));
		try{
			initHttpClient();
			
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Accept","*/*");  
			httpPost.setHeader("Connection", "close");
			httpPost.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECT_TIMEOUT);
			httpPost.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, READ_TIMEOUT);
			
			HttpEntity requestEntity = new ByteArrayEntity(data);
			httpPost.setEntity(requestEntity);
			
			HttpResponse httpResponse = httpClient.execute(httpPost);
			
			processResponse(httpResponse);
			if(!mCancle){
				mObserver.onHttpResult(ERROR_NONE, mResponseDataStream.toByteArray());
				
				mResponseDataStream.close();
			}
		}catch(Exception e){
			e.printStackTrace();
			mObserver.onHttpResult(ERROR_GENERAL, null);
		}
	}
	
	private void processResponse(HttpResponse httpResponse) throws IOException{
		responseCode = httpResponse.getStatusLine().getStatusCode();
		
		if (mCancle) {
			// user cancel
			throw new IOException();
		}
		if (responseCode != 200) {
			// failed
			throw new IOException();
		}
		
		InputStream inputStream = httpResponse.getEntity().getContent();
		// read the response
		if (mCancle) {
			// user cancel
			throw new IOException();
		}
		
		mObserver.onContentLength(httpResponse.getEntity().getContentLength());
		
		int bytesRead = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		while ((bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
			if (mCancle) {
				// user canceled
				inputStream.close();
				throw new IOException("HttpCanceled");
			}
			
			//store the response byte int the stream
			mResponseDataStream.write(buffer, 0, bytesRead);
			mResponseDataStream.flush();
			
			mObserver.onRecvProgress(buffer,bytesRead);
		}
		inputStream.close();
	}
	
	public class EasySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public EasySSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {

				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws CertificateException {
				}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
						throws CertificateException {
				}

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,
					port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}
}
