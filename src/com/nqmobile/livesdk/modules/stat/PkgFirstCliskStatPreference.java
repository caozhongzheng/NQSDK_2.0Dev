package com.nqmobile.livesdk.modules.stat;

import com.nqmobile.livesdk.commons.preference.PkgsPreference;

/**
 * Created by Rainbow on 2014/11/22.
 */
public class PkgFirstCliskStatPreference extends PkgsPreference{

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private static PkgFirstCliskStatPreference sInstance;

    // ===========================================================
    // Constructors
    // ===========================================================

    private PkgFirstCliskStatPreference() {
    	super();
    }

    public static PkgFirstCliskStatPreference getInstance(){
        if(sInstance == null){
            sInstance = new PkgFirstCliskStatPreference();
        }

        return sInstance;
    }


    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
