package com.nqmobile.livesdk.modules.association;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.appstubfolder.AppStubFolderManager;
import com.nqmobile.livesdk.modules.association.features.AssocCheckUpdateFeature;
import com.nqmobile.livesdk.modules.association.features.AssocSwitchFeature;
import com.nqmobile.livesdk.modules.association.table.AssociationRecommendTable;
import com.nqmobile.livesdk.modules.association.table.AssociationTable;

public class AssociationModule extends AbsModule {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "Association";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	private List<IFeature> mFeatures;
    private AssociationPreference mPreference;

	// ===========================================================
	// Constructors
	// ===========================================================
	public AssociationModule() {
		mTables = new ArrayList<IDataTable>();
		mTables.add(new AssociationTable());
		mTables.add(new AssociationRecommendTable());

		mFeatures = new ArrayList<IFeature>();
		mFeatures.add(new AssocCheckUpdateFeature());
		mFeatures.add(new AssocSwitchFeature());
		
		mPreference = AssociationPreference.getInstance();
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
	protected void onEnabled(boolean enabled) {
	    mPreference.setAssociationEnable(enabled);
		AssociationManager mgr = AssociationManager.getInstance(getContext());
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
		AbsManager mgr = AssociationManager.getInstance(getContext());
		mgr.init();
	}
    @Override
    public boolean canEnabled() {
        return mPreference.isAssociationEnable();
    }

	@Override
	public String getLogTag() {
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

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
