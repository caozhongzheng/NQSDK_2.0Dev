package com.nqmobile.livesdk.utils;

import java.lang.reflect.Method;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.nqmobile.livesdk.commons.log.NqLog;

public class NQMultiCardUtil {
    /**
     * 是否是双卡手机 （应该是判断是否要执行双卡适配）
     * (判断条件：有以getDeviceID开始的多个方法， 并且 插入了两个Sim卡)
     *
     * @param mContext
     * @return
     */
    public static boolean isMultiSimCard(Context mContext) {
//		return (NumberOfCard(mContext) <= 1) ? false : true;
        return (NumberOfCard(mContext) > 1) ? true : false;
    }

    // 是否插入了两个sim卡
    public static boolean isInsertDoubleSim(Context ctx) {
        String[] mDoubleIMSI = getDuoIMSI(ctx);
        return getIMEnable(mDoubleIMSI) ? true : false;
    }

    public static String getIMEI(Context ctx) {
        String imei = "";
        if (isMultiSimCard(ctx)) {
            imei = getDuoIMEI(ctx)[0];
        }

        if (TextUtils.isEmpty(imei)) {
            imei = CommonMethod.getIMEI(ctx);
        }

        return imei;
    }

    public static String getIMSI(Context ctx) {
        String imsi = "";
        if (isMultiSimCard(ctx)) {
            String[] imsis = getDuoIMSI(ctx);
            for (int i = 0; i < imsis.length; i++) {
                if (imsis[i] != null && imsis[i].length() > 3) {
                    imsi = imsis[i];
                }
            }
        }

        if (TextUtils.isEmpty(imsi)) {
            imsi = CommonMethod.getIMSI(ctx);
        }

        return imsi;
    }

    private static int NumberOfCard(Context context) {
        TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Class<?> c = mTelephonyMgr.getClass();
        Method[] methods = c.getMethods();

        int count = 0;
        for (Method m : methods) {
            if (m.getName().startsWith("getDeviceId")) {
                count++;
            }
        }

        return count;
    }

    /**
     * 判断获取双卡手机的代码 是否可行。
     *
     * @param str
     * @return
     */
    private static boolean getIMEnable(String[] str) {
        for (int i = 0; i < str.length; i++) {
            if (str[i] == null || str[i].length() < 3) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取双卡的方法不可用，只能取到其中一个值,
     * 为防止连一个也取不到，调用该该方法时，判断返回值是否为空，
     * 如果null，则调用 CommonMethod.getIMEI(IMSI),作为补充返回
     *
     * @param str
     * @return
     */
    private static String getOneIMData(String[] str) {
        String data = "";
        for (int i = 0; i < str.length; i++) {
            if (str[i] != null) {
                data = str[i];
            }
        }

        return data;
    }

    private static int getPhoneType(Context ctx) {
        if (IsGemini(ctx)) {
            return 1;
        } else if (IsMsim(ctx)) {
            return 2;
        } else if (isHtc(ctx)) {
            return 3;
        } else if (isXT(ctx)) {
            return 4;
        } else if (isXt882(ctx)) {
            return 5;
        } else {
            return 0;
        }

    }

    /**
     * 是否是某种双卡机型（fpt？ 越南的手机），
     * 这个getXXXGemini是一款双卡手机上获取XXX信息的厂商自己的实现方式
     * 具有一定的代表性
     * 海信和酷派上的接口为getDeviceId(int)
     *
     * @param context
     * @return
     */
    private static boolean IsGemini(Context context) {
        TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Class<?> c = mTelephonyMgr.getClass();
        Method[] methods = c.getMethods();
        boolean IsG = false;
        for (Method m : methods) {
            if (m.getName().startsWith("getDeviceId")) {
                if (m.getName().endsWith("Gemini")) {
                    IsG = true;
                    return IsG;
                }
            }
        }
        return IsG;
    }

    private static boolean IsMsim(Context context) {
        TelephonyManager mTelephonyMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        Class<?> c = mTelephonyMgr.getClass();
        if (c.toString().trim().endsWith("MSimTelephonyManager")) {
            return true;
        }
        return false;
    }

    private static boolean isHtc(Context context) {
        return (check_device_model(context).startsWith("HTC")) ? true : false;
    }

    private static boolean isXt882(Context context) {
        return (check_device_model(context).equalsIgnoreCase("XT882")) ? true : false;
    }

    private static boolean isXT(Context context) {
        return (check_device_model(context).equalsIgnoreCase("XT")) ? true : false;
    }


    public static String check_device_model(Context context) {
        int version = 3;
        String manufacturer = null;
        String model = "nofound";
        String device = null;
        Class<android.os.Build.VERSION> build_version_class = android.os.Build.VERSION.class;
        //取得 android 版本
        java.lang.reflect.Field field;
        try {
            field = build_version_class.getField("SDK_INT");

            version = (Integer) field.get(new android.os.Build.VERSION());
            Class<android.os.Build> build_class = android.os.Build.class;
            //取得牌子
            java.lang.reflect.Field manu_field = build_class.getField("MANUFACTURER");
            manufacturer = (String) manu_field.get(new android.os.Build());
            //取得型號
            java.lang.reflect.Field field2 = build_class.getField("MODEL");
            model = (String) field2.get(new android.os.Build());
            //模組號碼
            java.lang.reflect.Field device_field = build_class.getField("DEVICE");
            device = (String) device_field.get(new android.os.Build());
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        NqLog.e("牌子:" + manufacturer + " 型號:" + model + " SDK版本:" + version + " 模組號碼:" + device);
        return model;
    }

    public static String[] getDuoIMEI(Context context) {
        TelephonyManager mTelephonyMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        Class<?> c = mTelephonyMgr.getClass();
        NqLog.e("TELEPHONY_SERVICE: " + c.getName());
//        Method[] methods = c.getMethods();
        String deviceId = "";
        String[] mIMEI = new String[2];

        try {
            if (IsGemini(context)) {
                Method getDeviceId = c.getMethod("getDeviceIdGemini", new Class[]{int.class});
                // 0：卡槽一， 1：卡槽二； 获取对应卡槽的IMEI和对于卡槽的IMSI；
                deviceId = deviceId + "Duo_t_IMEI1:" + (String) (getDeviceId.invoke(mTelephonyMgr, 0));
                deviceId = deviceId + "\nDuo_t_IMEI2:" + (String) (getDeviceId.invoke(mTelephonyMgr, 1));
                mIMEI[0] = (String) (getDeviceId.invoke(mTelephonyMgr, 0));
                mIMEI[1] = (String) (getDeviceId.invoke(mTelephonyMgr, 1));
            } else {
                Method getDeviceId = c.getMethod("getDeviceId", new Class[]{int.class});
                deviceId = deviceId + "Duo_f_IMEI1:" + (String) (getDeviceId.invoke(mTelephonyMgr, 0));
                deviceId = deviceId + "\nDuo_f_IMEI2:" + (String) (getDeviceId.invoke(mTelephonyMgr, 1));
                mIMEI[0] = (String) (getDeviceId.invoke(mTelephonyMgr, 0));
                mIMEI[1] = (String) (getDeviceId.invoke(mTelephonyMgr, 1));
            }
        } catch (NoSuchMethodException  e) {
            NqLog.e(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        NqLog.i("deviceId imei=" + deviceId);
        return mIMEI;
    }

    public static String[] getDuoIMSI(Context context) {
        TelephonyManager mTelephonyMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        Class<?> c = mTelephonyMgr.getClass();
//        Method[] methods = c.getMethods();
        String deviceId = "";
        String[] mIMSI = new String[2];
        try {
            if (IsGemini(context)) {
                Method getDeviceId = c.getMethod("getSubscriberIdGemini", new Class[]{int.class});
                deviceId = deviceId + "Duo_t_IMSI1:" + (String) (getDeviceId.invoke(mTelephonyMgr, 0));
                deviceId = deviceId + "\nDuo_t_IMSI2:" + (String) (getDeviceId.invoke(mTelephonyMgr, 1));
                mIMSI[0] = (String) (getDeviceId.invoke(mTelephonyMgr, 0));
                mIMSI[1] = (String) (getDeviceId.invoke(mTelephonyMgr, 1));
            } else {
                Method getDeviceId = c.getMethod("getSubscriberId", new Class[]{int.class});
                deviceId = deviceId + "Duo_f_IMSI1:" + (String) (getDeviceId.invoke(mTelephonyMgr, 0));
                deviceId = deviceId + "\nDuo_f_IMSI2:" + (String) (getDeviceId.invoke(mTelephonyMgr, 1));
                mIMSI[0] = (String) (getDeviceId.invoke(mTelephonyMgr, 0));
                mIMSI[1] = (String) (getDeviceId.invoke(mTelephonyMgr, 1));
            }
            
        } catch (NoSuchMethodException  e) {
        	 NqLog.e(e);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        NqLog.i("deviceId imsi=" + deviceId);
        return mIMSI;
    }

}
