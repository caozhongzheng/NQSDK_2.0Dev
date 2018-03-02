package com.nqmobile.livesdk.modules.stat;

import android.content.Context;
import com.nqmobile.livesdk.commons.log.NqLog;

import java.lang.reflect.Constructor;

/**
 * Created by Rainbow on 2014/12/24.
 */
public class GAnalyticsFactory {

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

    public static IGAnalytics getIGAnalytics(Context context) {
        IGAnalytics ga = null;
        Class<?> clz;
        try {
            Class cls = Class.forName("com.nqmobile.livesdk.commons.info.IGAnalyticsImpl");
            if(cls != null){
                Class[] paramTypes = { Context.class};
                Object[] params = {context}; // 方法传入的参数
                Constructor con = cls.getConstructor(paramTypes);     //主要就是这句了
                ga = (IGAnalytics) con.newInstance(params);  //BatcherBase 为自定义类
            }
        } catch (ClassNotFoundException e) {
            NqLog.i("CustomClientInfo is not existing");
        } catch (Exception e) {
            NqLog.e(e);
        }

        return ga;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
