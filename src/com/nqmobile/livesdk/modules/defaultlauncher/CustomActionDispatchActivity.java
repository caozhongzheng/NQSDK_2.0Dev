package com.nqmobile.livesdk.modules.defaultlauncher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Rainbow on 2015/1/23.
 */
public class CustomActionDispatchActivity extends Activity{

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private boolean isOnceMore;

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent().getParcelableExtra("extra_action_intent");
        if(i != null){
//            nY.a(this,i);
            finish();
        }else{
            String action = getIntent().getAction();
            if(action.equals("action.select.launcher")){
                Intent intent = new Intent(this,ResolverActivity.class);
                isOnceMore = getIntent().getBooleanExtra("isOnceMore",false);
                startActivityForResult(intent,1);
            }else{
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1){
            ResolverActivity.handleActivityResult(this,resultCode,data,isOnceMore);
        }

        finish();
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
