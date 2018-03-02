package com.nqmobile.livesdk.commons.info;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.text.TextUtils;

import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.commons.preference.CommonsPreference;
import com.nqmobile.livesdk.utils.CRC16;
import com.nqmobile.livesdk.utils.FileUtil;


public class ClientInfo {
	private static IClientInfo sClientInfo;
	
	static {
		NqTest.init();
		sClientInfo = ClientInfoFactory.getInstance();
	}

	public static int getBusinessId(){
		return sClientInfo.getBusinessId();
	}
	
	public static int getEditionId(){
		return sClientInfo.getEditionId();
	}
	
	public static String getPackageName(){
		return sClientInfo.getPackageName();
	}
	
	public static boolean isGP() {
		return sClientInfo.isGP();
	}

	public static boolean hasLocalTheme(){
		return sClientInfo.hasLocalTheme();
	}
	
	public static boolean isUseBingSearchUrl(){
		return sClientInfo.isUseBingSearchUrl();
	}

	public static void onUpgrade(int lastVer){
		sClientInfo.onUpgrade(lastVer);
	}
	
	public static String getClientLanguage(Context context) {
		Resources resources = context.getResources();
		String lang = null;
		

		int resource_id = resources.getIdentifier("lang", "string",
				context.getPackageName());
		if (resource_id != 0) {
			try {
				lang = resources.getString(resource_id);
				NqLog.d("lang in resource file: " + lang);
			} catch (Exception e) {
				NqLog.e(e);
			}
		}
		if (!TextUtils.isEmpty(lang)){
			lang = lang.replace('_', '-');//防御性
			int n = lang.indexOf("-");
			if (n < 2){//语言代码至少2个字符
				lang = null;
			}
				
		}
		
		if (TextUtils.isEmpty(lang)){
			NqLog.e("client language is unknown!");
			lang = MobileInfo.getMobileLanguage(context);
		}
		
		return lang;
	}
	
	public static String getChannelId(ContextWrapper cw) {
		CommonsPreference pref = CommonsPreference.getInstance();
		String result = pref.getChannelId();
	   	if (!TextUtils.isEmpty(result)) {
	   		NqLog.i("getChannelId from xml file: " + result);
		   	return result;
	   	}
		String chanelid = sClientInfo.getChannelId();

		ZipFile zf = null;
		InputStream is = null;
		try {
			byte buffer[] = new byte[10];
			zf = new ZipFile(cw.getPackageResourcePath());
			ZipEntry ze = zf.getEntry("res/raw/channel.dat");
			if (ze != null) {
				is = zf.getInputStream(ze);
				if(is.read(buffer) > 0){
					if (buffer[0] == -75 && buffer[1] == -98 
							&&buffer[2] == 7 && buffer[3] == -80) {

							int id = (buffer[4] & 0xff) | ((buffer[5] << 8) & 0xff00) // | 表示安位�?
							| ((buffer[6] << 24) >>> 8) | (buffer[7] << 24); 
							
							byte crcBuffer[] = new byte[4];
							for (int i = 0; i < 4; i++) 
							{ 
								crcBuffer[i]= buffer[i+4]; 
							} 
							
							short crc = CRC16.toCrc(crcBuffer);
			
							short crcApk =  (short) ((buffer[8] & 0xff) | (buffer[9] << 8));
							if( crcApk == crc )
							{
								chanelid = id + "";
							}
						}
				}
			}
		} catch (FileNotFoundException e) {
			NqLog.i("channel file not found");
		} catch (Exception e) {
			NqLog.e(e);
		} finally {
			FileUtil.closeStream(is);
			if (zf != null){
				try {
					zf.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// 把原channel id 写入配置文件
		pref.setChannelId(chanelid);
		return chanelid;
	}

	public static Map<String, Boolean> overrideModuleDefaults() {
		return sClientInfo.overrideModuleDefaults();
	}
	
	public static String getWX_APPID() {
		return sClientInfo.getWX_APPID();
	}
	
	public static String getQQ_APPID() {
		return sClientInfo.getQQ_APPID();
	}
}
