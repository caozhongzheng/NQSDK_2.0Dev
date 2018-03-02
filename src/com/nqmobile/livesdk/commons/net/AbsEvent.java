/**
 * 
 */
package com.nqmobile.livesdk.commons.net;

/**
 * @author HouKangxi
 *
 */
public class AbsEvent {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	private Object tag;
	private boolean success;

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public Object getTag() {
		return tag;
	}

	public AbsEvent setTag(Object tag) {
		this.tag = tag;
		return this;
	}

	public boolean isSuccess() {
		return success;
	}

	public AbsEvent setSuccess(boolean success) {
		this.success = success;
		return this;
	}
}
