package com.nqmobile.livesdk.modules.defaultlauncher;

import android.app.ActivityManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.AbsListView;

/**
 * Created by Rainbow on 2015/1/28.
 */
public class OK {

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

    public static Drawable a(Resources arg2, int arg3, int arg4) {
        Drawable v0 = null;
        if(OK.a()) {
            try {
                v0 = arg2.getDrawableForDensity(arg3, arg4);
            }
            catch(Resources.NotFoundException v1) {
            }
        }

        return v0;
    }

    public static boolean a() {
        return Build.VERSION.SDK_INT > 15;
    }

    public static void a(ActivityManager arg4, int[] arg5) {
        if(Build.VERSION.SDK_INT >= 11) {
            arg5[0] = arg4.getLauncherLargeIconDensity();
            arg5[1] = arg4.getLauncherLargeIconSize();
        }
        else {
            arg5[1] = 0;
            arg5[0] = 0;
        }
    }

    public static void a(AbsListView arg1) {
        if((OK.a()) && arg1 != null) {
            arg1.setChoiceMode(1);
        }
    }

    public static void a(AbsListView arg1, int arg2) {
        if(OK.a()) {
            arg1.setItemChecked(arg2, true);
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
