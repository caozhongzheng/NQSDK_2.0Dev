package com.nqmobile.livesdk.commons.info;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import com.nqmobile.livesdk.utils.Tools;

import android.os.Environment;

public class CommonDefine {
	// 不同版本对应的标志（和服务器地址有关）
	public final static int VERSION_STYLE_RELEASE 			= 0;		// Release
	public final static int VERSION_STYLE_VERIFY 			= 1;		// Verify
	public final static int VERSION_STYLE_SHOPPING 			= 2;		// 正式
	public final static int VERSION_STYLE_TEST 				= 3;		// 测试
	// 不同版本设置
	private static int VERSION_STYLE_TYPE	= VERSION_STYLE_SHOPPING;	// 不同版本	

	public final static String ANDROID_OSID_15 	= "350";			// 平台ID (Android 1.5: 350)
	public final static String ANDROID_OSID_20 	= "351";			// 平台ID (Android 2.0: 351)
	public final static String ANDROID_OSID_30 	= "352";			// 平台ID (Android 3.0: 352)
	public final static String ANDROID_OSID_40 	= "353";			// 平台ID (Android 4.0: 353)
	
	
	public static String LocationAPIURL;
	public static String UPDATE_URL;
	public static String APP_CN_URL;
	public static String WEATHER_CN_URL;
	public static String APP_EN_URL;
	public static String WEATHER_EN_URL;
	public static int APP_PORT;
	public static int WEATHER_PORT;
	
    static{
    	setStyleType(CommonDefine.VERSION_STYLE_TYPE);
    	NqTest.init();
    }
    
	static void setStyleType(int versionType){
		CommonDefine.VERSION_STYLE_TYPE = versionType;
		switch(CommonDefine.VERSION_STYLE_TYPE) {
			case CommonDefine.VERSION_STYLE_RELEASE:	// Release
				LocationAPIURL = "http://release.netqin.com:8297/BOSS_CS_IOS/geolocate";
				UPDATE_URL = "http://release.netqin.com:8297/BOSS_USServlet";
				APP_CN_URL = "launcher-cn-rel.nq.com";
				WEATHER_CN_URL = "weather-cn-rel.nq.com";
				APP_EN_URL = "launcher-cn-rel.nq.com";
				WEATHER_EN_URL = "weather-cn-rel.nq.com";
				APP_PORT = 9091;
				WEATHER_PORT = 9093;
				break;
			case CommonDefine.VERSION_STYLE_VERIFY:		// Verify
				LocationAPIURL = "http://verify.geolocation.nq.com/geolocate";
				UPDATE_URL = "http://us-verify.nqsrv.com/BOSS_US/UPSServlet";
                APP_CN_URL = "vrf-live.nq.com";
                WEATHER_CN_URL = "vrf-live.nq.com";
                APP_EN_URL = "vrf-live.nq.com";
                WEATHER_EN_URL = "vrf-live.nq.com";
                APP_PORT = 8081;
                WEATHER_PORT = 8083;
				break;
			case CommonDefine.VERSION_STYLE_SHOPPING:	// 正式
				LocationAPIURL = "https://geo-nqsecurity.nq.com/geolocate";
				UPDATE_URL = "https://moduleupgrade.nq.com/BOSS_US/UPSServlet";
				APP_CN_URL = "lau-livecn.nq.com";
				WEATHER_CN_URL = "plug-livecn.nq.com";
				APP_EN_URL = "lau-live.nq.com";
				WEATHER_EN_URL = "plug-live.nq.com";
				APP_PORT = 443;
				WEATHER_PORT = 443;
				break;
			// add by ysongren start for test
			case CommonDefine.VERSION_STYLE_TEST:	// 测试
				LocationAPIURL = "http://verify.geolocation.nq.com/geolocate";
				UPDATE_URL = "http://us-verify.nqsrv.com/BOSS_US/UPSServlet";
                APP_CN_URL = "test-live.nq.com";
                WEATHER_CN_URL = "test-live.nq.com";
                APP_EN_URL = "test-live.nq.com";
                WEATHER_EN_URL = "test-live.nq.com";
                APP_PORT = 8081;
                WEATHER_PORT = 8083;
				break;
		}
	}
}
