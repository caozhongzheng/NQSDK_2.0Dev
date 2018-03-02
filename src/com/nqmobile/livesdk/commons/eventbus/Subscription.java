/**
 * 
 */
package com.nqmobile.livesdk.commons.eventbus;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订阅者类(仅包内使用)
 * 
 * @author HouKangxi
 * @date 2014年10月24日
 */
class Subscription {

	private final Object target;
	private final ConcurrentHashMap<Class<?>, Collection<Method>> eventTypeToMethods;

	public Subscription(Object target) {
		this.target = target;
		eventTypeToMethods = new ConcurrentHashMap<Class<?>, Collection<Method>>();
	}

	public Subscription(Object target, int size) {
		this.target = target;
		eventTypeToMethods = new ConcurrentHashMap<Class<?>, Collection<Method>>(
				size);
	}

	public Collection<Class<?>> getEventTypes() {
		return eventTypeToMethods.keySet();
	}

	public Collection<Method> getMethods(Class<?> evtClass) {
		return eventTypeToMethods.get(evtClass);
	}

	public void addMethod(Class<?> argClass, Method m) {
		Collection<Method> mColl = eventTypeToMethods.get(argClass);
		if (mColl == null) {
			eventTypeToMethods.putIfAbsent(argClass,
					mColl = new HashSet<Method>());
		}
		mColl.add(m);
	}

	public Object getTarget() {
		return target;
	}

}
