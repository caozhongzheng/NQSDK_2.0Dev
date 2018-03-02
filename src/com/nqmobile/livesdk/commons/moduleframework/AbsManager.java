package com.nqmobile.livesdk.commons.moduleframework;

import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.system.SystemFacade;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;

public abstract class AbsManager {
	protected SystemFacade getSystem(){
		return SystemFacadeFactory.getSystem();
	}
	
	public abstract void init();

	public void onDisable() {
		EventBus.getDefault().unregist(this);
	}
}
