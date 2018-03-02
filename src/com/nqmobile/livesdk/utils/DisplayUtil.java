package com.nqmobile.livesdk.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * 屏幕适配相关工具类
 * @author changxiaofei
 * @time 2013-11-28 下午2:14:22
 */
public class DisplayUtil {
	/**
	 * 将px值转换为dip或dp值，保证尺寸大小不变
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {
		float density = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / density + 0.5f);
	}
	
	/**
	 * 将dip或dp值转换为px值，保证尺寸大小不变
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static int dip2px(Context context, float dipValue) {
		float density = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * density + 0.5f);
	}
	
	/**
	 * 将px值转换为sp值，保证文字大小不变
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2sp(Context context, float pxValue) {
		float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}
	
	/**
	 * 将sp值转换为px值，保证文字大小不变
	 * @param context
	 * @param spValue
	 * @return
	 */
	public static int sp2px(Context context, float spValue) {
		float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
	
	/**
	 * 客户端分辨率Width
	 * @param context
	 * @return
	 */
	public static int getDisplayWidth(Context context){
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
	}
	
	/**
	 * 客户端分辨率Height
	 * @param context
	 * @return
	 */
	public static int getDisplayHeight(Context context){
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
	}
}
