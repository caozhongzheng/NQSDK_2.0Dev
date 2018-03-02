package com.nqmobile.livesdk.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.format.DateUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.nqmobile.livesdk.commons.log.NqLog;

public class LocateUtil {
    private MyGdLocationLitener mGdListener;
    private MyDefaultListener mListener;
    private LocateListener mCallBack;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private Context mContext;
    private LocationManagerProxy mAMapLocationManager;
    private LocationManager mLocationManager;

    private static final int MSG_TIME_OUT = 1;
    private static final int MSG_LOCATE_BY_GD = 2;
    private static final int MSG_LOCATE_BY_SYSTEM = 3;

    private static final long TIME_OUT = 20 * DateUtils.SECOND_IN_MILLIS;

    private final Object lock = new Object();
    
    private boolean hasResult; 

    public interface LocateListener {

        public void onLocateFinish(String lat, String lon);

        public void onLocateFailed();
    }

    public LocateUtil(Context context, LocateListener listener) {
        mContext = context;
        mCallBack = listener;
    }

    public void startLocation(int region) {
        NqLog.i("LocateUtil startLocation");

        mHandlerThread = new HandlerThread("LocateHandlerThread");
        mHandlerThread.start();
        mHandler = new MyHandler(mHandlerThread.getLooper());
        mHandler.sendEmptyMessageDelayed(MSG_TIME_OUT, TIME_OUT);

        NqLog.i("region="+region);
        if(region == 0){ //国内
            mHandler.sendEmptyMessage(MSG_LOCATE_BY_GD);
        }else{
            mHandler.sendEmptyMessage(MSG_LOCATE_BY_SYSTEM);
        }
    }

    private class MyHandler extends Handler {

        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try{
            	switch (msg.what) {
                case MSG_LOCATE_BY_GD:
                    mAMapLocationManager = LocationManagerProxy.getInstance(mContext);
                    mGdListener = new MyGdLocationLitener();
                    mAMapLocationManager.requestLocationUpdates(LocationProviderProxy.AMapNetwork,5*1000,10,mGdListener);
                    break;
                case MSG_TIME_OUT:
                	synchronized(lock){
                		if (hasResult){
                			return;
                		}
                		hasResult = true;
                	}
                	destory();
                	mCallBack.onLocateFailed();
                    break;
                case MSG_LOCATE_BY_SYSTEM:
                    mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

                    mListener = new MyDefaultListener();
                    boolean hasProvider = false;
                    if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    	mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000L, 0, mListener);
                    	hasProvider = true;
                    }
                    if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                    	mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 1000L, 0, mListener);
                    	hasProvider = true;
                    }
					if (!hasProvider) {
						destory();
						mCallBack.onLocateFailed();
					}
					break;
				}
            } catch(Exception e){
            	NqLog.e(e);
            }
        }
    }

    private class MyGdLocationLitener implements AMapLocationListener {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation == null){
                aMapLocation = new AMapLocation("AMapLocation");
                aMapLocation.setLatitude(0);
                aMapLocation.setLongitude(0);
            }
        	NqLog.i("location succ! lat=" + aMapLocation.getLatitude() + " lon=" + aMapLocation.getLongitude());
        	synchronized(lock){
        		if (hasResult){
        			return;
        		}
        		hasResult = true;
        	}        
        	destory();
            mCallBack.onLocateFinish(String.valueOf(aMapLocation.getLatitude()), String.valueOf(aMapLocation.getLongitude()));
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    private class MyDefaultListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            if (location == null){
                location = new Location("SystemLocation");
                location.setLatitude(0);
                location.setLongitude(0);
            }
        	NqLog.i("location succ! lat=" + location.getLatitude() + " lon=" + location.getLongitude());
        	synchronized(lock){
        		if (hasResult){
        			return;
        		}
        		hasResult = true;
        	}            
        	destory();
            mCallBack.onLocateFinish(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
    
    private void destory(){
    	if (mAMapLocationManager != null) {  
            mAMapLocationManager.removeUpdates(mGdListener);  
            mAMapLocationManager.destory();  
            mAMapLocationManager = null; 
        } 
    	if (mLocationManager != null){
    		mLocationManager.removeUpdates(mListener);
    		mLocationManager = null;
    	}
    	mHandler.removeMessages(MSG_TIME_OUT);
    	if (mHandlerThread != null){
    		mHandlerThread.quit();
    		mHandlerThread = null;
    	}
    }
}
