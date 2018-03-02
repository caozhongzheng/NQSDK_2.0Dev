package com.nqmobile.livesdk.commons.system;

import android.content.Context;

import com.nqmobile.livesdk.commons.ApplicationContext;

public class SystemFacadeFactory {
	private static SystemFacade sMock;
	private static SystemFacade sInstance;
	
	public static void setMock(SystemFacade mock){
		sMock = mock;
	}
	
	public static SystemFacade getSystem(){
		return getSystem(ApplicationContext.getContext());
	}
	
	public static SystemFacade getSystem(Context context){
		if (sMock != null){
			return sMock;
		} 

		if (sInstance == null){
			sInstance = new AndroidSystemFacade(context);
		}
		
		return sInstance;		
	}
}
