package com.nqmobile.livesdk.modules.defaultlauncher;

import android.content.Context;

/**
 * Created by Rainbow on 2015/1/28.
 */
public class aiv {

    // ===========================================================
    // Constants
    // ===========================================================

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

    // ===========================================================
    // Methods
    // ===========================================================

    public static int a(Context context,float f){
        float density = context.getResources().getDisplayMetrics().density;
        return (int)(density * f + 0.5f);
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}