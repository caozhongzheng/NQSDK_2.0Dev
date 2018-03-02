package com.nqmobile.livesdk.modules.defaultlauncher;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by Rainbow on 2015/1/26.
 */
public class TextView extends android.widget.TextView{

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    public TextView(Context context){
        super(context);
        a();
    }

    public TextView(Context context,AttributeSet set){
        super(context,set);
        a();
        a(set);
    }

    public TextView(Context context,AttributeSet set,int i){
        super(context,set,i);
        a();
        a(set);
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

    private void a(){
//        Typeface typeface = akb.a(context);
//        setTypeface(typeface);
    }

    private void a(AttributeSet set){
        if(set != null){
            int i = set.getAttributeResourceValue("http://schemas.android.com/apk/res/android","text",0);
            if(i > 0){
                setText(i);
            }

            i = set.getAttributeIntValue("http://schemas.android.com/apk/res/android","hint",0);
            if(i > 0){
                setHint(i);
            }
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
