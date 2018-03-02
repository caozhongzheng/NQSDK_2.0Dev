package com.nqmobile.livesdk.modules.font;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.font.features.FontSwitchFeature;
import com.nqmobile.livesdk.modules.font.table.FontCacheTable;
import com.nqmobile.livesdk.modules.font.table.FontLocalTable;


public class FontModule extends AbsModule {

	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "Font";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	private List<IFeature> features;
    private FontPreference mPreference;
	// ===========================================================
	// Constructors
	// ===========================================================
	public FontModule() {
		mTables = new ArrayList<IDataTable>();
		mTables.add(new FontCacheTable());
		mTables.add(new FontLocalTable());
		
		features = new ArrayList<IFeature>();
		features.add(new FontSwitchFeature());
		
		mPreference = FontPreference.getInstance();
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
    public boolean canEnabled() {
        return mPreference.isFontEnable();
    }

	@Override
	protected void onEnabled(boolean enabled) {
	    mPreference.setFontEnable(enabled);
	    FontManager mgr = FontManager.getInstance(getContext());
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}

	public void init() {
		AbsManager mgr = FontManager.getInstance(getContext());
		mgr.init();
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
