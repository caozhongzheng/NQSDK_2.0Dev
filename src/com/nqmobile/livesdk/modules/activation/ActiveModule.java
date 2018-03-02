package com.nqmobile.livesdk.modules.activation;

import java.util.List;

import android.util.Log;
import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;

/**
 * Created by Rainbow on 2014/11/19.
 */
public class ActiveModule extends AbsModule{

    // ===========================================================
    // Constants
    // ===========================================================
    public static final String MODULE_NAME = "Active";

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
    public void init(){}
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
    public boolean canEnabled() {
        return true;
    }

    @Override
    protected void onEnabled(boolean enabled) {
        Log.i("gqf","ActiveModule onEnabled!");
        ActiveManager.getInstance();
    }
    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
