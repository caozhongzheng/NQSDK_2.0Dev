package com.nqmobile.livesdk.modules.apptype;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.apptype.features.ApplibFeature;
import com.nqmobile.livesdk.modules.apptype.table.AppCodeTable;

public class AppTypeModule extends AbsModule {
    // ===========================================================
    // Constants
    // ===========================================================
    public final static String MODULE_NAME = "AppType";
    // ===========================================================
    // Fields
    // ===========================================================
    private ArrayList<IDataTable> mTables;
    private List<IFeature> mFeatures;

    // ===========================================================
    // Constructors
    // ===========================================================
    public AppTypeModule(){
        mTables = new ArrayList<IDataTable>();
        mTables.add(new AppCodeTable());
        
        mFeatures = new ArrayList<IFeature>();
        mFeatures.add(new ApplibFeature());
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
        return mFeatures;
    }

    @Override
    public List<IDataTable> getTables() {
        return mTables;
    }
    
    @Override
    public boolean canEnabled() {
        return true;
    }

    @Override
    protected void onEnabled(boolean enabled) {
        AbsManager mgr = AppTypeManager.getInstance();
        if (enabled) {
            mgr.init();
        } else {
            mgr.onDisable();
        }
    }
    public void init() {
    	AppTypeManager.getInstance().init();
	}    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
