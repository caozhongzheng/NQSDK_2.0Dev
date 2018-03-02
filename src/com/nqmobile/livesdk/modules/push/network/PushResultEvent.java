/**
 * 
 */
package com.nqmobile.livesdk.modules.push.network;

import java.util.List;

import com.nqmobile.livesdk.modules.push.model.Push;

/**
 * @author HouKangxi
 *
 */
public class PushResultEvent {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	private List<Push> pushList;
	private Object tag;
	private boolean success;

	// ===========================================================
	// Constructors
	// ===========================================================

	public PushResultEvent() {
		super();
	}

	public PushResultEvent(List<Push> pushList) {
		super();
		this.pushList = pushList;
		success = true;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public List<Push> getPushList() {
		return pushList;
	}

	PushResultEvent setPushList(List<Push> pushList) {
		this.pushList = pushList;
		return this;
	}

	public Object getTag() {
		return tag;
	}

	PushResultEvent setTag(Object tag) {
		this.tag = tag;
		return this;
	}

	public boolean isSuccess() {
		return success;
	}

	PushResultEvent setSuccess(boolean success) {
		this.success = success;
		return this;
	}

	@Override
	public String toString() {
		return "PushResultEvent [tag=" + tag + ", success=" + success
				+ ", pushList=" + pushList + "]";
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
