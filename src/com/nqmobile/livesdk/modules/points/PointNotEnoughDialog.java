package com.nqmobile.livesdk.modules.points;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.nqmobile.livesdk.modules.font.FontManager;
import com.nqmobile.livesdk.utils.MResource;

/**
 * Created by Rainbow on 2014/12/22.
 */
public class PointNotEnoughDialog extends Dialog {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private Context mContext;

    // ===========================================================
    // Constructors
    // ===========================================================

    public PointNotEnoughDialog(Context context,int theme){
        super(context,theme);
        mContext = context;
    }

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
        setContentView(MResource.getIdByName(mContext, "layout", "nq_point_app_dialog_gp"));

        ImageView exit = (ImageView) findViewById(MResource.getIdByName(mContext,"id","btn_exit"));
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        Button btn_point_center = (Button) findViewById(MResource.getIdByName(mContext,"id","btn_point_center"));
        btn_point_center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, PointsCenterActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(i);
                dismiss();
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
