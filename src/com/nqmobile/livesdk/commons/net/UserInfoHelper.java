package com.nqmobile.livesdk.commons.net;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import com.nq.interfaces.userinfo.TClientInfo;
import com.nq.interfaces.userinfo.TMobileInfo;
import com.nq.interfaces.userinfo.TServiceInfo;
import com.nq.interfaces.userinfo.TUserInfo;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.commons.info.MobileInfo;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;

public class UserInfoHelper {
	public static TUserInfo getUserInfo() {
		Context context = ApplicationContext.getContext();
		TServiceInfo serviceInfo = new TServiceInfo();
    	serviceInfo.setBusinessId(String.valueOf(ClientInfo.getBusinessId()));
    	serviceInfo.setUid(PreferenceDataHelper.getInstance(context).getStringValue(PreferenceDataHelper.KEY_UID));

		TMobileInfo mobileInfo = new TMobileInfo();
		mobileInfo.setPlatformId(MobileInfo.getOSID());
		mobileInfo.setCellId(MobileInfo.getCellid(context));
		mobileInfo.setImsi(MobileInfo.getImsi(context));
		mobileInfo.setImei(MobileInfo.getImei(context));
		mobileInfo.setMac(MobileInfo.getLocalMacAddress(context));
		mobileInfo.setOsName(Build.VERSION.RELEASE);
		mobileInfo.setOsVersion(String.valueOf(Build.VERSION.SDK_INT));
		mobileInfo.setDeviceName(MobileInfo.getDeviceName());
		mobileInfo.setNet(MobileInfo.getNetworkType(context));
		mobileInfo.setDeviceWidth(MobileInfo.getDisplayWidth(context));
		mobileInfo.setDeviceHeight(MobileInfo.getDisplayHeight(context));
		mobileInfo.setLanguage(MobileInfo.getMobileLanguage(context));
		mobileInfo.setCountry(MobileInfo.getCountry(context));
		mobileInfo.setMcnc(MobileInfo.getMcnc(context));
		mobileInfo.setNetworkCountry(MobileInfo.getNetworkCountry(context));
		mobileInfo.setNetworkMcnc(MobileInfo.getNetworkMcnc(context));

    	TClientInfo clientInfo = new TClientInfo();    	
    	clientInfo.setLanguage(ClientInfo.getClientLanguage(context));
    	clientInfo.setEditionId(String.valueOf(ClientInfo.getEditionId()));
    	clientInfo.setCorporationId(ClientInfo.getChannelId((ContextWrapper) context));
    	clientInfo.setTimestamp(SystemFacadeFactory.getSystem().currentTimeMillis());
    	clientInfo.setTimezone(Integer.valueOf(MobileInfo.getTimeZone()));
    	
    	TUserInfo userInfo = new TUserInfo();
    	userInfo.setClientInfo(clientInfo);
    	userInfo.setMobileInfo(mobileInfo);
    	userInfo.setServiceInfo(serviceInfo);
		return userInfo;
	}
}
