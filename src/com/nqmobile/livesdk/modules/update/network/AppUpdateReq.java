package com.nqmobile.livesdk.modules.update.network;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.commons.info.MobileInfo;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.modules.activation.ActivePreference;
import com.nqmobile.livesdk.modules.update.UpdateModule;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;

//组件更新请求类
public class AppUpdateReq {
	private static final ILogger NqLog = LoggerFactory.getLogger(UpdateModule.MODULE_NAME);
	
	private StringBuffer sb = new StringBuffer();
	private Context context;

	public AppUpdateReq(Context context) {
		this.context = context;
	}

	public String getRequestXML() {
		NqLog.i("getRequestXML");
		sb.append("<Request>\n");
		sb.append("<Protocol>2.2.1</Protocol>\n");
		sb.append("<Command>6</Command>\n");
		sb.append("<ClientInfo>\n\t");
		sb.append("<Model type=\"5\">" + Build.MODEL + "</Model>\n\t");
		sb.append("<Language>");
		sb.append(CommonMethod.getPlatformLanguage());
		sb.append("</Language>\n\t");
		sb.append("<Country>");
		sb.append(CommonMethod.getCountryCode());
		sb.append("</Country>\n\t");//
		sb.append("<IMEI>");
		sb.append(CommonMethod.getIMEI(context));
		sb.append("</IMEI>\n\t");

		sb.append("<IMSI>");
		sb.append(CommonMethod.getIMSI(context));
		sb.append("</IMSI>\n\t");
		sb.append("<Timezone>" + MobileInfo.getTimeZone() + "</Timezone>\n\t");
		sb.append("<UpdateType>2</UpdateType>\n");
		sb.append("</ClientInfo>\n");
		sb.append("<UserInfo>\n\t");
		sb.append("<UID>");
		sb.append(ActivePreference.getInstance().getStringValue(PreferenceDataHelper.KEY_UID));
		sb.append("</UID>\n");
		sb.append("</UserInfo>\n");
		sb.append("<ServiceInfo>\n\t");
		sb.append("<Service>" + ClientInfo.getBusinessId() + "</Service>\n\t");
		sb.append("<Partner>");
		sb.append(ClientInfo.getChannelId((ContextWrapper) context));
		sb.append("</Partner>\n\t");
		sb.append("<WapMurl status=\"1\"/>\n");
		sb.append("</ServiceInfo>\n");
		sb.append("<VersionInfo os=\"" + MobileInfo.getOSID() + "\" version=\""
				+ ClientInfo.getEditionId() + "\">\n\t");
		sb.append("<Module id=\"4\" version=\"31\" />\n\t");
		sb.append("<Module id=\"2\" version=\"200216\"/>\n");
		sb.append("</VersionInfo>\n");
		sb.append("</Request>\n");

		return sb.toString();
	}

	public byte[] getRequestBytes() {
		return this.getRequestXML().getBytes();
	}
}
