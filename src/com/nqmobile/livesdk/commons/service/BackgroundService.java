package com.nqmobile.livesdk.commons.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.DateUtils;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;

public class BackgroundService extends Service{
	
	private PendingIntent mPendingIntent;

	public static final String IMMEDIATE_PERIOD_CHECK_ACTION = "com.nqmobile.live.BackgroundService.immediatePeriodCheck";
	public static final String REQUEST_PERIOD_CHECK_ACTION = "com.nqmobile.live.BackgroundService.requestPeriodCheck";
	private static final String SELF_PERIOD_CHECK_ACTION = "com.nqmobile.live.BackgroundService.selfPeriodCheck";

	private static final long CHECK_DELAY = 30 * DateUtils.MINUTE_IN_MILLIS;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null){
			return START_STICKY;
		}
		
		String action = intent.getAction();
		
		if(action == null){
			return START_STICKY;
		}
		
		if(REQUEST_PERIOD_CHECK_ACTION.equals(action)){
			long now = SystemFacadeFactory.getSystem().currentTimeMillis();
			ServicePreference pref = ServicePreference.getInstance();
			if (Math.abs(now - pref.getLastPeriodCheck()) >= CHECK_DELAY) {
				processAction(action);
			}
		} else if (IMMEDIATE_PERIOD_CHECK_ACTION.equals(action) || SELF_PERIOD_CHECK_ACTION.equals(action)) {
			processAction(action);
		}
		
		return START_STICKY;
	}

	private void processAction(String action) {
		if (ApplicationContext.isLauncherReady() || SELF_PERIOD_CHECK_ACTION.equals(action)) {
			EventBus.getDefault().post(new PeriodCheckEvent());
			long now = SystemFacadeFactory.getSystem().currentTimeMillis();
			ServicePreference pref = ServicePreference.getInstance();
			pref.setLastPeriodCheck(now);
		}
		cancelAlarmWark(SELF_PERIOD_CHECK_ACTION);
		startNextAlarmWark(SELF_PERIOD_CHECK_ACTION, CHECK_DELAY);
		stopSelf();
	}
	
	private void startNextAlarmWark(String command, long interval){
		Intent startIntent = new Intent(this, BackgroundService.class);
		startIntent.setAction(command);
		mPendingIntent = PendingIntent.getService(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		long now = SystemFacadeFactory.getSystem().currentTimeMillis();
		mAlarmManager.set(AlarmManager.RTC, now + interval, mPendingIntent);
	}

    private void cancelAlarmWark(String command){
        Intent startIntent = new Intent(this, BackgroundService.class);
        startIntent.setAction(command);
        mPendingIntent = PendingIntent.getService(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (mPendingIntent != null){
            AlarmManager mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            mAlarmManager.cancel(mPendingIntent);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
