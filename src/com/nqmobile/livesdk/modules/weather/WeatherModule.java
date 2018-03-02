package com.nqmobile.livesdk.modules.weather;

import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperManager;

public class WeatherModule extends AbsModule {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "Weather";

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    protected void onEnabled(boolean enabled) {
        AbsManager mgr = WeatherManager.getInstance();
        if (enabled) {
            mgr.init();
            WeatherPolicy.initLastGetWeatherTime();
        } else {
            mgr.onDisable();
        }
    }
    public void init() {
    	 WeatherManager.getInstance().init();
    	 WeatherPolicy.initLastGetWeatherTime();
	}
	@Override
	public List<IFeature> getFeatures() {
		return null;// 天气模块未定义开关
	}

	@Override
	public List<IDataTable> getTables() {
		return null;// 天气模块没有数据库表
	}
	
    @Override
    public boolean canEnabled() {
        return true;
    }
    
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
