package com.nqmobile.livesdk.modules.browserbandge;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class BrowserBandgePreference extends SettingsPreference {
    private static final String KEY_BANDGE_ENABLE = "bandge_enable";

	/** 是否调起广告角标*/
	private static final String KEY_ISOVERRIDE = "isOverRide";
	
	/** 广告角标url*/
	private static final String KEY_JUMP_URL = "jump_url";
	
	/** 广告角标resource id*/
	private static final String KEY_RESOURCEID = "jump_resourceid";
	
	/** 服务端下发角标资源常驻id */
	private static final String KEY_RESOURCEID_LONG = "res_id_long";
	
	/** 常驻url 弹框时间  */
	private static final String KEY_RECORD_DIALOG_TIME = "record_dialog_time";
	
	/** 常驻url 三天内不再提示  */
	private static final String KEY_RECORD_CHECKED_THREE_DAY_TIPS = "record_three_date_tips";
	
	/** 常驻url 现在开启开关  */
	private static final String KEY_RECORD_DIALOG_OK = "record_dialog_ok";

    private static BrowserBandgePreference sInstance = new BrowserBandgePreference();

	private BrowserBandgePreference() {
	}
	
	public static BrowserBandgePreference getInstance(){
		return sInstance;
	}
	
    public boolean isBandgeEnable() {
        return getBooleanValue(KEY_BANDGE_ENABLE);
    }

    public void setBandgeEnable(boolean enable) {
        setBooleanValue(KEY_BANDGE_ENABLE, enable);
    }
    
    public boolean isOverride() {
        return getBooleanValue(KEY_ISOVERRIDE);
    }

    public void setOverride(boolean override) {
        setBooleanValue(KEY_ISOVERRIDE, override);
    }
    
    public String getResourceIdLong() {
        return getStringValue(KEY_RESOURCEID_LONG);
    }

    public void setResourceIdLong(String resId) {
        setStringValue(KEY_RESOURCEID_LONG, resId);
    }
    
    public String getJumpUrl() {
        return getStringValue(KEY_JUMP_URL);
    }

    public void setJumpUrl(String jumpUrl) {
        setStringValue(KEY_JUMP_URL, jumpUrl);
    }
    
    public String getResourceId() {
        return getStringValue(KEY_RESOURCEID);
    }

    public void setResourceId(String resId) {
        setStringValue(KEY_RESOURCEID, resId);
    }
    
    public boolean getRecordDialogOk() {
        return getBooleanValue(KEY_RECORD_DIALOG_OK);
    }

    public void setRecordDialogOk(boolean ok) {
        setBooleanValue(KEY_RECORD_DIALOG_OK, ok);
    }
    
    public long getRecordDialogTime() {
        return getLongValue(KEY_RECORD_DIALOG_TIME);
    }

    public void setRecordDialogTime(long time) {
        setLongValue(KEY_RECORD_DIALOG_TIME, time);
    }
    
    public boolean isRecordChecked() {
        return getBooleanValue(KEY_RECORD_CHECKED_THREE_DAY_TIPS);
    }

    public void setRecrodChecked(boolean checked) {
        setBooleanValue(KEY_RECORD_CHECKED_THREE_DAY_TIPS, checked);
    }

}
