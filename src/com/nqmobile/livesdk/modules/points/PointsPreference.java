package com.nqmobile.livesdk.modules.points;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

/**
 * Created by Rainbow on 2014/11/19.
 */
public class PointsPreference extends SettingsPreference{

    // ===========================================================
    // Constants
    // ===========================================================

    public static final String KEY_POINT_CENTER_ENABLE = "point_center_enable";
    public static final String KEY_USER_POINTS = "user_points";
    public static final String KEY_EXPERID_POINTS = "experid_points";
    public static final String KEY_EXPERID_TIME = "experid_time";
    public static final String KEY_NEED_SHOW_EXPOINT_TIP = "need_show_expoint_tip"; //是否需要显示过期叹号
    public static final String KEY_SHOW_POINT_TIP ="show_point_tip"; //首次进入积分中心是否显示了积分说明
    public static final String KEY_SHOW_EX_POINT_TIP = "show_ex_point_tip"; //积分过期提醒
    public static final String KEY_FIRST_LAUNCH_POINT_TIP = "first_launch_point_tip"; // 首次点击打开应用


    // ===========================================================
    // Fields
    // ===========================================================

    private static PointsPreference sInstance;

    // ===========================================================
    // Constructors
    // ===========================================================

    private PointsPreference() {
    }

    public static PointsPreference getInstance(){
        if(sInstance == null){
            sInstance = new PointsPreference();
        }

        return sInstance;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public boolean isPointCenterEnable(){
        return getBooleanValue(KEY_POINT_CENTER_ENABLE);
    }
    public void setPointCenterEnable(boolean enable){
        setBooleanValue(KEY_POINT_CENTER_ENABLE, enable);
    }
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
