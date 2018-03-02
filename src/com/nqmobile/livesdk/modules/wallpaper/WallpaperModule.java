package com.nqmobile.livesdk.modules.wallpaper;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.update.UpdateManager;
import com.nqmobile.livesdk.modules.wallpaper.features.WallpaperSwitchFeature;
import com.nqmobile.livesdk.modules.wallpaper.table.WallpaperCacheTable;
import com.nqmobile.livesdk.modules.wallpaper.table.WallpaperLocalTable;

public class WallpaperModule extends AbsModule {

	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "Wallpaper";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	private List<IFeature> features;
    private WallpaperPreference mPreference;

	// ===========================================================
	// Constructors
	// ===========================================================
	public WallpaperModule() {

		mTables = new ArrayList<IDataTable>();
		mTables.add(new WallpaperCacheTable());
		mTables.add(new WallpaperLocalTable());

		features = new ArrayList<IFeature>();
		features.add(new WallpaperSwitchFeature());
		
		mPreference = WallpaperPreference.getInstance();
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
	    mPreference.setWallpaperEnable(enabled);
		AbsManager mgr = WallpaperManager.getInstance(getContext());
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
		WallpaperManager.getInstance(getContext()).init();
	}
    @Override
    public boolean canEnabled() {
        return mPreference.isWallpaperEnable();
    }
    
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
