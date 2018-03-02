package com.nqmobile.livesdk.modules.storeentry;


import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class StoreEntryPreference extends SettingsPreference {
	public static final String KEY_STORE_ENTRY_ENABLE = "store_entry_enable";//Store入口开关
	
    public static final String KEY_STORE_ENTRY_CREATED = "store_entry_created";//Store入口创建过
    
    private static StoreEntryPreference sInstance = new StoreEntryPreference();

	private StoreEntryPreference() {
	}
	
	public static StoreEntryPreference getInstance(){
		return sInstance;
	}

    public boolean isStoreEntryEnable() {
        return getBooleanValue(KEY_STORE_ENTRY_ENABLE);
    }

    public void setStoreEntryEnable(boolean enable) {
        setBooleanValue(KEY_STORE_ENTRY_ENABLE, enable);
    }
	
}
