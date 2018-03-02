package com.nqmobile.livesdk.modules.stat;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.modules.stat.feature.UploadLogSwitchFeature;
import com.nqmobile.livesdk.modules.stat.feature.UploadPackageSwitchFeature;
import com.nqmobile.livesdk.modules.stat.table.AdStatTable;
import com.nqmobile.livesdk.modules.stat.table.NewPackageTable;
import com.nqmobile.livesdk.modules.stat.table.StoreStatTable;

/**
 * Created by Rainbow on 2014/11/22.
 */
public class StatModule extends AbsModule{

    // ===========================================================
    // Constants
    // ===========================================================
    public static final String MODULE_NAME = "StatModule";

    // ===========================================================
    // Fields
    // ===========================================================

    private List<IDataTable> mTables;
	private StatPreference mPreference;
	private List<IFeature> mFeatures;
    // ===========================================================
    // Constructors
    // ===========================================================

    public StatModule(){
        mTables = new ArrayList<IDataTable>();
        mTables.add(new StoreStatTable());
        mTables.add(new NewPackageTable());
        mTables.add(new AdStatTable());
        
		mFeatures = new ArrayList<IFeature>();
		mFeatures.add(new UploadLogSwitchFeature());
		mFeatures.add(new UploadPackageSwitchFeature());		
		
        mPreference = StatPreference.getInstance();
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

    // ===========================================================
    // Methods
    // ===========================================================

    @Override
    public boolean canEnabled() {
        return true;
    }

    @Override
    protected void onEnabled(boolean enabled) {
        // do nothing
    	mPreference.setUploadLogEnabled(enabled);
    }
    public void init() {}
	@Override
	public void onAppFirstInit(boolean enabled) {
		long now = SystemFacadeFactory.getSystem().currentTimeMillis();
		mPreference.setLastUploadLog(now);
	}
    
    

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
