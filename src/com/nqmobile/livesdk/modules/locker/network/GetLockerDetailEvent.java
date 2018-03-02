/**
 * 
 */
package com.nqmobile.livesdk.modules.locker.network;

import com.nqmobile.livesdk.commons.net.AbsEvent;
import com.nqmobile.livesdk.modules.locker.Locker;

/**
 * @author HouKangxi
 *
 */
public class GetLockerDetailEvent extends AbsEvent {
	private Locker locker;

	@Override
	public GetLockerDetailEvent setTag(Object tag) {
		super.setTag(tag);
		return this;
	}

	@Override
	public GetLockerDetailEvent setSuccess(boolean success) {
		super.setSuccess(success);
		return this;
	}

	public Locker getLocker() {
		return locker;
	}

	public GetLockerDetailEvent setLocker(Locker locker) {
		this.locker = locker;
		return this;
	}

}
