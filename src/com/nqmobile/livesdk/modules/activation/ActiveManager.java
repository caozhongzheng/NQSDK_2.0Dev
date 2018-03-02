package com.nqmobile.livesdk.modules.activation;

import android.text.TextUtils;

import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.receiver.ConnectivityChangeEvent;
import com.nqmobile.livesdk.modules.activation.network.ActiveServiceFactory;

/**
 * Created by Rainbow on 2014/11/19.
 */
public class ActiveManager {

    // ===========================================================
    // Constants
    // ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(ActiveModule.MODULE_NAME);
	
    // ===========================================================
    // Fields
    // ===========================================================

    private static ActiveManager mInstance;

    // ===========================================================
    // Constructors
    // ===========================================================

    private ActiveManager() {
        EventBus.getDefault().register(this);
    }

    public synchronized static ActiveManager getInstance() {
        if (mInstance == null) {
            mInstance = new ActiveManager();
        }
        return mInstance;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    public void active(){
        NqLog.i("active!");
        ActiveServiceFactory.getService().active();
    }

    public void onEvent(ConnectivityChangeEvent event){
        String uid = ActivePreference.getInstance().getStringValue(ActivePreference.KEY_UID);
        if(TextUtils.isEmpty(uid)){
            active();
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
