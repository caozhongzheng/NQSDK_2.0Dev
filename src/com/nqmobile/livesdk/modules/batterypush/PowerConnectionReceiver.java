package com.nqmobile.livesdk.modules.batterypush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.StoreMainActivity;
import com.nqmobile.livesdk.modules.push.PushPreference;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NotificationUtil;

public class PowerConnectionReceiver extends BroadcastReceiver {
	private static final ILogger NqLog = LoggerFactory.getLogger(BatteryPushModule.MODULE_NAME);
	
	private static int num = 0;
	private int status = 0;
	private boolean isTest = false;

	private boolean isBatteryFull(final Intent intent) {
		int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		int battery = (int) (level / (float) scale * 100);
		NqLog.i("current battery  = "+ battery + " max battery " + scale + " the level is " + level);
		return (scale- battery <=0?true:false) ;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		NqLog.i("num = "+ num);
		
		if (!BatteryPushPreference.getInstance().getBooleanValue(
				BatteryPushPreference.KEY_BATTERY_PUSH_ENABLE)) {
			NqLog.i("BatteryPushPreference KEY_BATTERY_PUSH_ENABLE is false.");
			return;
		}else{
			NqLog.i("BatteryPushPreference KEY_BATTERY_PUSH_ENABLE is true 2222.");
		}
		if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
			status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
					BatteryManager.BATTERY_STATUS_UNKNOWN);
			//test start
			if (isTest) {		
				if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
					if (num < 1) {
						NqLog.e("showNotification");
						num++;
						noti(context);
					}
				}
			}
			//test end
			if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
				NqLog.i("status == BATTERY_STATUS_CHARGING  num = " + num);
				if (isBatteryFull(intent)){
					if (num < 1) {
						NqLog.e("showNotification BATTERY_STATUS_FULL in BATTERY_STATUS_CHARGING");
						noti(context);
						num++;
					}
				}
			}else if(status == BatteryManager.BATTERY_STATUS_UNKNOWN){
				NqLog.i("status == BATTERY_STATUS_UNKNOWN  num = " + num);
			}else if(status == BatteryManager.BATTERY_STATUS_DISCHARGING){
				num = 0;
				NqLog.i("status == BATTERY_STATUS_DISCHARGING  num turn to = " + num);
			}else if (status ==BatteryManager.BATTERY_STATUS_NOT_CHARGING){
				NqLog.i("status == BATTERY_STATUS_NOT_CHARGING = " + num);
			}else if(status ==BatteryManager.BATTERY_STATUS_FULL){
				NqLog.i("status == BATTERY_STATUS_FULL = " + num);
				NqLog.i("status == BATTERY_STATUS_FULL  num = " + num);
				if (num < 1) {
					NqLog.e("showNotification BATTERY_STATUS_FULL");
					noti(context);
					num++;
				}
			}
			else{
				NqLog.i("status == in default = " + num);
			}
		}

	}
		
	private Intent gotoStoreIntent(Context context) {
		Intent intent = new Intent(context, StoreMainActivity.class);
		intent.putExtra("from", 1001);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}
	private void noti(Context context){
		NqLog.i("noti");
		NotificationUtil.showNoti(context,MResource.getIdByName(context, "drawable", "nq_power_full"),
                context.getString(MResource.getIdByName(context, "string", "nq_owner")),
                context.getString(MResource.getIdByName(context, "string", "nq_say_googbye")),
                gotoStoreIntent(context),
                NotificationUtil.NOTIF_ID_BATTERY);
		StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
				BatteryPushActionLogConstants.ACTION_LOG_1906, null, 0, null);
	}
	

}