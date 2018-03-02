package com.nqmobile.livesdk.modules.update;

import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.modules.theme.ThemeManager;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;

/**
 * Created by Rainbow on 2014/11/20.
 */
public class UpdateModule extends AbsModule {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final String MODULE_NAME = "Update";

	// ===========================================================
	// Fields
	// ===========================================================
	private UpdatePreference mPreference;
	// ===========================================================
	// Constructors
	// ===========================================================
	public UpdateModule(){
		mPreference = UpdatePreference.getInstance();
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void onEnabled(boolean enabled) {
		AbsManager mgr = UpdateManager.getInstance();
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
		UpdateManager.getInstance().init();
	}
    @Override
    public boolean canEnabled() {
        return true;
    }

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public List<IFeature> getFeatures() {
		return null;
	}

	@Override
	public List<IDataTable> getTables() {
		return null;
	}

	@Override
	public void onAppFirstInit(boolean enabled) {
		long now = SystemFacadeFactory.getSystem().currentTimeMillis();
		mPreference.setLastCheckUpdate(now);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
