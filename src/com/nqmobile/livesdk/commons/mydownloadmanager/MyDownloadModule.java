package com.nqmobile.livesdk.commons.mydownloadmanager;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.commons.mydownloadmanager.features.MyDownloadSwitchFeature;
import com.nqmobile.livesdk.commons.mydownloadmanager.table.DownloadTable;

/**
 * Created by Rainbow on 2014/11/22.
 */
public class MyDownloadModule extends AbsModule {

	// ===========================================================
	// Constants
	// ===========================================================

    private static final ILogger NqLog = LoggerFactory.getLogger(MyDownloadModule.MODULE_NAME);
    public static final String MODULE_NAME = "MyDownload";

	// ===========================================================
	// Fields
	// ===========================================================

    private List<IDataTable> mTables;
    private List<IFeature> features;
    private MyDownloadPreference mPreference;
	// ===========================================================
	// Constructors
	// ===========================================================

    public MyDownloadModule(){
    	mTables = new ArrayList<IDataTable>();
    	mTables.add(new DownloadTable());
    	
    	features = new ArrayList<IFeature>();
    	features.add(new MyDownloadSwitchFeature());
    	
    	mPreference = MyDownloadPreference.getInstance();
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
    	NqLog.v("mPreference.isMyDownloadEnable() return " + mPreference.isMyDownloadEnable());
    	return mPreference.isMyDownloadEnable();
    }

	@Override
	protected void onEnabled(boolean enabled) {
		NqLog.i("enabled = "+ enabled);
		mPreference.setMyDownloadEnable(enabled);
	}
    public void init(){}
    
	@Override
	public String getLogTag() {
		return MODULE_NAME;
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
