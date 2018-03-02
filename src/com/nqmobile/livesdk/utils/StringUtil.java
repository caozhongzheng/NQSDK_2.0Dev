package com.nqmobile.livesdk.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.nqmobile.livesdk.commons.log.NqLog;

/**
 * 字符串工具类
 *
 * @author changxiaofei
 * @time 2013-12-4 下午7:25:21
 */
public class StringUtil {
    /**
     * @param strDateTime 1970年1月1日到现在的毫秒数 ，类型为long
     * @param pattern     格式，如日期yyyy-MM-dd，如日期加时间yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String formatDate(long longDateTime, String pattern) {
        if (longDateTime == 0) {
            return "";
        }
        try {
            return new SimpleDateFormat(pattern).format(new Date(longDateTime));
        } catch (Exception e) {
            NqLog.e("formatDate " + e.toString());
            return "";
        }
    }

    /**
     * 根据格式化时间字符串时间，返回1970年1月1日到此时间的毫秒数
     *
     * @param formatDate
     * @param pattern    格式，如日期yyyy-MM-dd，如日期加时间yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static long getDateTime(String formatDate, String pattern) {
        try {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            return df.parse(formatDate).getTime();
        } catch (Exception e) {
            NqLog.e("getDateTime " + e.toString());
            return -1;
        }
    }

    /**
     * 是否为null或""
     *
     * @param param
     * @return
     */
    public static boolean isNullOrEmpty(String str) {
        if (str == null) {
            return true;
        }
        return str.length() == 0;
    }

    /**
     * null转为""
     *
     * @param param
     * @return
     */
    public static String nullToEmpty(String str) {
        if (str == null) {
            return "";
        } else {
            return str;
        }
    }

    /**
     * null转为"",为了不报错临时用，要删掉。????
     *
     * @param param
     * @return
     */
    public static String nullToEmpty(int param) {
        if (param == 0) {
            return "";
        } else {
            return String.valueOf(param);
        }
    }

    /**
     * 输出M、K等单位
     *
     * @param size
     * @return
     */
    public static String formatSize(long size) {
        if (size <= 0) {
            return "";
        }
        DecimalFormat df = new DecimalFormat("###.#");
        float f;
        f = (float) ((float) size / (float) (1024 * 1024));
        if (f < 0.1f)
            f = 0.1f;
        return (df.format(new Float(f).doubleValue()) + "MB");
        /*if (size < 1024 * 1024) {
			f = (float) ((float) size / (float) 1024);
			return (df.format(new Float(f).doubleValue()) + "K");
		} else {
			f = (float) ((float) size / (float) (1024 * 1024));
			return (df.format(new Float(f).doubleValue()) + "M");
		}*/
    }

    /**
     * 获取0到size范围内（包括0和size）的2个随机数。
     *
     * @param size
     */
    public static int[] getTwoRandomNum(int size) {
        int[] intRet = new int[2];
        intRet[0] = (int) Math.round(Math.random() * size);
        intRet[1] = (int) Math.round(Math.random() * size);
        return intRet;
    }

	public static Object getExt(String strIconUrl) {
		// TODO Auto-generated method stub
		String JPG = ".jpg";
		String JPEG = ".jpeg";
		String PNG = ".png";
		String ext = strIconUrl.substring(strIconUrl.lastIndexOf(".")).toLowerCase();
		if(ext.startsWith(JPG) || ext.startsWith(JPEG))
			return JPG;
		else if(ext.startsWith(PNG))
			return PNG;
		if(ext.contains("?"))
			return ext.substring(0, ext.lastIndexOf("?"));
		return ext;
	}

}
