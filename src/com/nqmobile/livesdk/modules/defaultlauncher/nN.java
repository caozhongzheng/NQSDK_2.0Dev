package com.nqmobile.livesdk.modules.defaultlauncher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Rainbow on 2015/1/23.
 */
public class nN {

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

    public static ResolveInfo f(Context context){
        PackageManager pm = context.getPackageManager();
        Intent i = new Intent("android.intent.action.MAIN");
        i.addCategory("android.intent.category.HOME");
        List<ResolveInfo> list = pm.queryIntentActivities(i,0);
        List<IntentFilter> intentFilters = new ArrayList<IntentFilter>();
        List<ComponentName> componentNames = new ArrayList<ComponentName>();
        for(Iterator iter = list.iterator();iter.hasNext();){
            ResolveInfo resolveInfo = (ResolveInfo) iter.next();
            intentFilters.clear();
            componentNames.clear();
            pm.getPreferredActivities(intentFilters,componentNames,resolveInfo.activityInfo.packageName);
            for(Iterator itt = intentFilters.iterator();itt.hasNext();){
                IntentFilter filter = (IntentFilter) itt.next();
                if(filter.hasAction("android.intent.action.MAIN") && filter.hasCategory("android.intent.category.HOME")){
                    return resolveInfo;
                }
            }
        }
        return null;
    }

    public static void c(Context context,String s){

    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
