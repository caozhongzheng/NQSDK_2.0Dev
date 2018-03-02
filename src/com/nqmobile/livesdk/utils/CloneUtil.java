/**
 * 
 */
package com.nqmobile.livesdk.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 父类向子类拷贝数据的工具类
 * <p>
 * 
 * @author HouKangxi
 * @date 2014年10月27日
 */
public class CloneUtil {
	public static void cloneFromParentToSub(Object parent, Object sub) {
		// long start = System.currentTimeMillis();
		copyFromParentToSub(parent, sub);
		// System.out
		// .println("Time(ms) : " + (System.currentTimeMillis() - start));
	}

	/**
	 * 
	 * @param parent
	 * @param sub
	 */
	private static void copyFromParentToSub(Object parent, Object sub) {
		try {
			for (Field field : parent.getClass().getDeclaredFields()) {
				if (Modifier.isFinal(field.getModifiers())) {
					continue;
				}

				field.setAccessible(true);
				Class<?> ftype = field.getType();
				Object fvalue = field.get(parent);
				if (fvalue == null) {
					continue;// 值为null,不需要复制
				}
				if (ftype.isPrimitive()) {
					field.set(sub, fvalue);// 基本类型，直接复制
					continue;
				}
				if (isSystemType(ftype)) {

					if (java.util.Collection.class.isAssignableFrom(ftype)) {
						// 字段类型为集合
						ParameterizedType ptype = (ParameterizedType) field
								.getGenericType();
						Class pt = (Class) ptype.getActualTypeArguments()[0];// 泛型类型

						Class<?> curClass = null;
						try {
							curClass = guessExpectClass(pt, sub.getClass());
						} catch (ClassNotFoundException e) {
							field.set(sub, fvalue);
							continue;
						}
						java.util.Collection colNew = null;
						java.util.Collection colOld = (Collection) fvalue;

						if (Modifier.isAbstract(ftype.getModifiers())) {
							if (List.class.isAssignableFrom(ftype)) {
								colNew = new ArrayList(colOld.size());
							} else if (Set.class.isAssignableFrom(ftype)) {
								colNew = new HashSet();
							}
						} else {
							colNew = (Collection) ftype.newInstance();
						}
						if (colNew != null) {
							for (Object p : colOld) {
								Object curIns = curClass.newInstance();
								cloneFromParentToSub(p, curIns);
								colNew.add(curIns);
							}
							fvalue = colNew;
						}
					} else if (ftype.isArray()) {
						// 字段类型为 数组
						Class<?> etype = ftype.getComponentType();// 数组的元素的类型
						Class<?> curClass = null;
						try {
							curClass = guessExpectClass(etype, sub.getClass());
						} catch (ClassNotFoundException e) {
							field.set(sub, fvalue);
							continue;
						}
						Object[] arrOld = (Object[]) fvalue;
						Object arrNew = Array.newInstance(curClass,
								arrOld.length);
						int i = 0;
						for (Object o : arrOld) {
							Object curIns = curClass.newInstance();
							cloneFromParentToSub(o, curIns);
							Array.set(arrNew, i++, curIns);
						}
					}
					field.set(sub, fvalue);
					continue;
				}
				Class<?> curClass = null;
				try {
					curClass = guessExpectClass(ftype, sub.getClass());
				} catch (ClassNotFoundException e) {
					field.set(sub, fvalue);
					continue;
				}
				Object curIns = curClass.newInstance();
				cloneFromParentToSub(fvalue, curIns);
				field.set(sub, curIns);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Class<?> guessExpectClass(Class<?> type, Class<?> cur)
			throws ClassNotFoundException {
		String pureName = type.getName().substring(
				type.getName().lastIndexOf('.') + 1);
		String pkg = cur.getPackage().getName();
		return Class.forName(pkg + "." + pureName);
	}

	private static boolean isSystemType(Class<?> type) {
		if (type.getPackage() == null) {
			return true;
		}
		String pkg = type.getPackage().getName();
		return pkg.startsWith("java.") || pkg.startsWith("android.");
	}
}
