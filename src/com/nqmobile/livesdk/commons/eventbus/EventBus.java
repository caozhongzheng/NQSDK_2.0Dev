/**
 * 
 */
package com.nqmobile.livesdk.commons.eventbus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;

/**
 * 事件总线-简单实现
 * 
 * @author HouKangxi
 * @date 2014年10月24日
 */
public class EventBus {

	// EventType -> List<Subscription>，事件到订阅对象之间的映射
	private final ConcurrentHashMap<Class<?>, Collection<Subscription>> subscriptionsByEventType = new ConcurrentHashMap<Class<?>, Collection<Subscription>>();

	// Subscriber -> List<EventType>，订阅源到它订阅的的所有事件类型的映射
	private final ConcurrentHashMap<Object, Subscription> typesBySubscriber = new ConcurrentHashMap<Object, Subscription>();

	/**
	 * 订阅方法的约定前缀
	 */
	private final String eventMethodPrefix;

	private static final EventBus defaultInstance = new EventBus();

	public static EventBus getDefault() {
		return defaultInstance;
	}

	public EventBus() {
		this("onEvent");
	}

	public EventBus(String eventMethodPrefix) {
		this.eventMethodPrefix = eventMethodPrefix;
	}

	/**
	 * 注册对象的所有满足以下条件的方法为订阅者: <br/>
	 * 以约定的前缀开头，或者带有@Subscribe注解
	 * 
	 * @param subscriber
	 */

	public void register(Object subscriber) {
		register(subscriber, subscriber.getClass());

	}

	public void register(Object subscriber, Class<?> clz) {
		if (subscriber == null) {
			return;
		}
		Subscription subscription = typesBySubscriber.get(subscriber);
		if (subscription == null) {
			typesBySubscriber.putIfAbsent(
					subscriber,
					subscription = new Subscription(subscriber, clz
							.getDeclaredMethods().length));
		} else {
			return;// 防止重复注册
		}
		for (Method m : clz.getMethods()) {
			Class<?>[] ptypes;
			if ((eventMethodPrefix != null
					&& m.getName().startsWith(eventMethodPrefix) || m
					.getAnnotation(Subscribe.class) != null)
					&& (ptypes = m.getParameterTypes()) != null
					&& ptypes.length == 1) {// 限制参数只有1个

				Class<?> eventType = ptypes[0];

				regist(subscription, eventType, m);
			}
		}
	}

	/**
	 * 注册指定对象的单个方法
	 * 
	 * @param subscriber
	 * @param methodName
	 * @param eventType
	 */
	public void register(Object subscriber, String methodName,
			Class<?> eventType) {
		if (subscriber == null) {
			return;
		}
		Subscription subscription = typesBySubscriber.get(subscriber);
		if (subscription == null) {
			typesBySubscriber.putIfAbsent(subscriber,
					subscription = new Subscription(subscriber, subscriber
							.getClass().getDeclaredMethods().length));

		}
		Method m = null;
		try {
			m = subscriber.getClass().getMethod(methodName, eventType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (m == null) {
			return;
		}
		regist(subscription, eventType, m);
	}

	private void regist(Subscription subscription, Class<?> eventType, Method m) {

		subscription.addMethod(eventType, m);

		synchronized(this){
			Collection<Subscription> subscriptionColl = subscriptionsByEventType
					.get(eventType);
			if (subscriptionColl == null) {
				subscriptionsByEventType.put(eventType,
						subscriptionColl = new CopyOnWriteArraySet<Subscription>());
			}
			subscriptionColl.add(subscription);
		}
	}

	/**
	 * 反注册
	 * 
	 * @param subscriber
	 */
	public void unregist(Object subscriber) {
		Subscription subscription = typesBySubscriber.remove(subscriber);
		if (subscription == null) {
			return;
		}
		Collection<Class<?>> evtypes = subscription.getEventTypes();
		if (evtypes == null) {
			return;
		}
		for (Class<?> evtype : evtypes) {
			Collection<Subscription> subs = subscriptionsByEventType
					.get(evtype);
			if (subs != null) {
				subs.remove(subscription);
			}
		}
	}

	/**
	 * 发布事件，在当前线程中通知订阅者
	 * 
	 * @param event
	 *            - 事件
	 */
	public void post(final Object event) {
		if (event == null) {
			return;
		}
		Class<?> evtype = event.getClass();
		Collection<Subscription> subcs = subscriptionsByEventType.get(evtype);
		if (subcs == null) {
			return;
		}
		for (final Subscription sub : subcs) {
			Collection<Method> ms = sub.getMethods(evtype);
			if (ms != null) {
				for (final Method m : ms) {
					executeMethod(sub, event, m);
				}
			}
		}
	}

	/**
	 * 判断指定对象是否已注册
	 * 
	 * @param subcription
	 * @return
	 */
	public boolean containsSubcription(Object subcription) {
		return typesBySubscriber.contains(subcription);
	}

	/**
	 * 返回指定Event类型已注册的子类型
	 * 
	 * @param superType
	 * @return
	 */
	public List<Class<?>> eventOfType(Class<?> superType) {
		List<Class<?>> rs = new ArrayList<Class<?>>(
				subscriptionsByEventType.size() / 2);
		for (Class<?> evtType : subscriptionsByEventType.keySet()) {
			if (superType.isAssignableFrom(evtType)) {
				rs.add(evtType);
			}
		}
		return rs;
	}

	/**
	 * 发布事件，使用指定的线程池通知订阅者
	 * 
	 * @param event
	 *            - 事件
	 * @param ex
	 *            - 线程池
	 */
	public void post(final Object event, ExecutorService ex) {
		if (ex == null) {
			post(event);
			return;
		}
		if (event == null) {
			return;
		}
		Class<?> evtype = event.getClass();
		Collection<Subscription> subcs = subscriptionsByEventType.get(evtype);
		if (subcs == null) {
			return;
		}
		for (final Subscription sub : subcs) {
			Collection<Method> ms = sub.getMethods(evtype);
			if (ms != null) {
				for (final Method m : ms) {
					ex.submit(new Runnable() {
						public void run() {
							executeMethod(sub, event, m);
						}
					});
				}
			}
		}
	}

	private void executeMethod(Subscription sub, final Object event,
			final Method m) {
		try {
			m.setAccessible(true);
			m.invoke(sub.getTarget(), event);
		} catch (Exception e) {
			Throwable t = e.getCause();
			if (t != null) {
				t.printStackTrace();
			} else {
				e.printStackTrace();
			}
		}
	}
}
