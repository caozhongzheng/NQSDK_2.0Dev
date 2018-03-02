package com.nqmobile.livesdk.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.nqmobile.livesdk.commons.log.NqLog;

/**
 * 根据资源的名字获取其ID值
 * @author changxiaofei
 * @time 2013-11-27 下午6:04:14
 */
public class MResource {
	/**
	 * 根据资源的名字获取字符串
	 * @author chenyanmin
	 * @time 2014-1-23 上午10:51:33
	 * @param context
	 * @param name
	 * @return
	 */
	public static String getString(Context context, String name) {
		String result = null;
		int id = getIdByName(context, "string", name);
		try{
		result = context.getString(id);
		} catch(Exception e){
			NqLog.e(e);			
		}
		
		return result;
	}
	
	/**
	 * 根据资源的名字获取字符串,并且接收格式化参数
	 * @author chenyanmin
	 * @time 2014-1-23 上午10:52:00
	 * @param context
	 * @param name
	 * @param formatArgs
	 * @return
	 */
	public static String getString(Context context, String name, Object... formatArgs) {
		String result = null;
		int id = getIdByName(context, "string", name);
		try{
		result = context.getString(id, formatArgs);
		} catch(Exception e){
			NqLog.e(e);			
		}
		
		return result;
	}
	
	public static Drawable getDrawable(Context context, String name){
		Drawable result = null;
		int id = getIdByName(context, "drawable", name);
		try{
		result = context.getResources().getDrawable(id);
		} catch(Exception e){
			NqLog.e(e);			
		} 
		
		return result;
	}
	
	public static Animation getAnimation(Context context, String name){
		Animation result = null;
		int id = getIdByName(context, "anim", name);
		try{
			result = AnimationUtils.loadAnimation(context, id);
		} catch(Exception e){
			NqLog.e(e);			
		} 
		
		return result;
	}
	
	public static int getIdByName(Context context, String className, String name) {
		String packageName = context.getPackageName();
		int id = 0;
		String tarClassName = packageName + ".R$" + className;
		try {
			Class<?> desireClass = Class.forName(tarClassName);
			id = desireClass.getField(name).getInt(null);
		} catch (ClassNotFoundException e) {
			NqLog.e(e);
		}  catch (NoSuchFieldException e) {
			NqLog.e("field '" + name + "' not found from class:" + tarClassName,
					e);
		}catch (Exception e) {
			NqLog.e(e);
		}
		return id;
	}
	
	public static int[] getIdsByName(Context context, String className, String name) {
		String packageName = context.getPackageName();
		String tarClassName = packageName + ".R$" + className;
		int[] ids = null;
		try {
			Class<?> desireClass = Class.forName(tarClassName);
			java.lang.reflect.Field field = desireClass.getField(name);
			if(field.getType().isArray()){
				ids  = (int[]) field.get(null);
			}
		} catch (Exception e) {
			NqLog.e(e);
		}
		return ids;
	}
}
