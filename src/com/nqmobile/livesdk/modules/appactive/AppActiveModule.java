package com.nqmobile.livesdk.modules.appactive;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.mustinstall.MustInstallManager;

import java.util.List;

/**
 * Created by Rainbow on 2014/12/3.
 */
public class AppActiveModule extends AbsModule{

    // ===========================================================
    // Constants
    // ===========================================================

    public static final String MODULE_NAME = "AppActive";

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
    protected void onEnabled(boolean enabled) {
        AppActiveManager mgr = AppActiveManager.getInstance();
        if (enabled) {
            mgr.init();
        } else {
            mgr.onDisable();
        }
    }
    public void init() {
		AbsManager mgr = AppActiveManager.getInstance();
		mgr.init();
	}
    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public boolean canEnabled() {
        return true;
    }

    @Override
    public List<IFeature> getFeatures() {
        return null;
    }

    @Override
    public List<IDataTable> getTables() {
        return null;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
