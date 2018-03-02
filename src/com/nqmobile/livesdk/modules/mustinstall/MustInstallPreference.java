package com.nqmobile.livesdk.modules.mustinstall;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

/**
 * Created by Rainbow on 2014/11/19.
 */
public class MustInstallPreference  extends SettingsPreference{

    // ===========================================================
    // Constants
    // ===========================================================

    public static final String KEY_MUST_INSTALL_ENABLE = "must_install_enable"; //装机必备开关
    public static final String KEY_MUST_INSTALL_TIP_SHOW = "must_install_tip_show"; //进入桌面装机必备通知栏提示是否显示过
    public static final String KEY_MUST_INSTALL_TIP_INSTALLAPP_SHOW = "must_Install_tip_installapp_show"; //安装新应用装机必备通知栏提示是否显示过
    public static final String KEY_MUSI_INSTALL_ICON_CREATE = "must_install_icon_create"; //装机必备快捷方法是否创建过
    public static final String KEY_MUST_INSTALL_ENTERED = "must_install_entered"; //是否进入过装机必备页面
    public static final String KEY_MUST_INSTALL_PUSH_TIME = "must_install_push_time"; //上次弹出push的时间
    public static final String KEY_MUST_INSTALL_PUSH_COUNT = "must_install_push_count"; //弹出push的次数

    // ===========================================================
    // Fields
    // ===========================================================

    private static MustInstallPreference sInstance;

    // ===========================================================
    // Constructors
    // ===========================================================

    private MustInstallPreference() {
    }

    public static MustInstallPreference getInstance(){
        if(sInstance == null){
            sInstance = new MustInstallPreference();
        }

        return sInstance;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public boolean isMustInstallEnable(){
        return getBooleanValue(KEY_MUST_INSTALL_ENABLE);
    }
    public void setMustInstallEnable(boolean enable){
        setBooleanValue(KEY_MUST_INSTALL_ENABLE, enable);
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
