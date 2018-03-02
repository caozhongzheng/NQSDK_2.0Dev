package com.nqmobile.livesdk.modules.batterypush;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.nqmobile.livesdk.commons.info.ProcessInfo;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.utils.ProcessUtils;

public class BatteryPushManger extends AbsManager {
	private static final ILogger NqLog = LoggerFactory.getLogger(BatteryPushModule.MODULE_NAME);

	private Context context;
	private static BatteryPushManger mInstance;

	public BatteryPushManger(Context context) {
		super();
		this.context = context;
	}

	public synchronized static BatteryPushManger getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new BatteryPushManger(context);
		}
		return mInstance;
	}

	PowerConnectionReceiver mPowerConnectionReceiver  = new PowerConnectionReceiver();

	@Override
	public void init() {
		NqLog.i("ProcessName= "+ ProcessUtils.getCurrentProcessName(context));
		if (ProcessInfo.isLauncherProcess(context)) {			
			NqLog.i("init context");
			context.registerReceiver(mPowerConnectionReceiver,
					new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		}
	}



	
}
