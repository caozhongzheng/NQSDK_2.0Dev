package com.nqmobile.livesdk.modules.apptype.model;

import com.nq.interfaces.launcher.TAppTypeInfo;
import com.nqmobile.livesdk.utils.StringUtil;

public class AppTypeInfoConverter {
    /**
     * TAppTypeInfo转换成AppTypeInfo对象
     *
     * @param TAppTypeInfo appTypeRes
     * @return
     */
    public static AppTypeInfo fromTAppTypeInfo(TAppTypeInfo appTypeRes) {
        AppTypeInfo app = null;
        if (appTypeRes != null) {
            app = new AppTypeInfo();
            app.setPackageName(StringUtil.nullToEmpty(appTypeRes.getPackageName()));
            app.setCode(appTypeRes.getCode());
            app.setCategory1(StringUtil.nullToEmpty(appTypeRes.getClassification1()));
            app.setCategory2(StringUtil.nullToEmpty(appTypeRes.getClassification2()));
        }
        return app;
    }
}
