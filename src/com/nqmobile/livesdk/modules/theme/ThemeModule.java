package com.nqmobile.livesdk.modules.theme;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.theme.features.ThemeSwitchFeature;
import com.nqmobile.livesdk.modules.theme.table.ThemeCacheTable;
import com.nqmobile.livesdk.modules.theme.table.ThemeLocalTable;

public class ThemeModule extends AbsModule {

	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "Theme";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	private List<IFeature> features;
    private ThemePreference mPreference;

	// ===========================================================
	// Constructors
	// ===========================================================
	public ThemeModule() {
		mTables = new ArrayList<IDataTable>();
		mTables.add(new ThemeCacheTable());
		mTables.add(new ThemeLocalTable());

		features = new ArrayList<IFeature>();
		features.add(new ThemeSwitchFeature());
		
		mPreference = ThemePreference.getInstance();
	}

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
	public List<IFeature> getFeatures() {
		return features;
	}

	@Override
	public List<IDataTable> getTables() {
		return mTables;
	}

	@Override
	protected void onEnabled(boolean enabled) {
	    mPreference.setThemeEnable(enabled);
		AbsManager mgr = ThemeManager.getInstance(getContext());
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
		ThemeManager.getInstance(getContext()).init();
	}
    @Override
    public boolean canEnabled() {
        return mPreference.isThemeEnable();
    }
    
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
