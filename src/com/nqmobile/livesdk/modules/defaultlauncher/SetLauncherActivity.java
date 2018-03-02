package com.nqmobile.livesdk.modules.defaultlauncher;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import com.nqmobile.live.R;
import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.modules.stat.StatManager;

/**
 * Created by Rainbow on 2015/2/4.
 */
public class SetLauncherActivity extends BaseActvity{

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.set_launcher_dialog);

        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,"3401","",0,"");
        DefaultLauncherPreference.getInstance().setLastShowDialogTime(System.currentTimeMillis());

        Button btn_set = (Button) findViewById(R.id.lf_common_dialog_button_ok);
        btn_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,"3402","",0,"");
                LauncherSDK.getInstance(SetLauncherActivity.this).setDefaultLaucnher();
                SetLauncherActivity.this.finish();
            }
        });

        Button btn_cancle = (Button) findViewById(R.id.lf_common_dialog_button_cancel);
        btn_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,"3403","",0,"");
                SetLauncherActivity.this.finish();
            }
        });
    }


    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
