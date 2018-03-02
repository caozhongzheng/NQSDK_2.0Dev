/**
 * 
 */
package com.nqmobile.livesdk.modules.locker.network;

import java.util.List;

import com.nqmobile.livesdk.commons.net.AbsEvent;
import com.nqmobile.livesdk.modules.locker.Locker;

/**
 * @author HouKangxi
 *
 */
public class GetLockerListEvent extends AbsEvent {
	// ===========================================================
	// Fields
	// ===========================================================
	private List<Locker> lockerList;
	private int column;
	private int offset;

	// ===========================================================
	// Constructors
	// ===========================================================
	public GetLockerListEvent() {
	}

	public GetLockerListEvent(List<Locker> lockerList) {
		this.lockerList = lockerList;
	}

	public GetLockerListEvent(List<Locker> lockerList, int column, int offset) {
		this.lockerList = lockerList;
		this.column = column;
		this.offset = offset;
	}

	public GetLockerListEvent(int column, int offset) {
		this.column = column;
		this.offset = offset;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public List<Locker> getLockerList() {
		return lockerList;
	}

	public GetLockerListEvent setLockerList(List<Locker> lockerList) {
		this.lockerList = lockerList;
		return this;
	}

	public GetLockerListEvent setSuccess(boolean success) {
		super.setSuccess(success);
		return this;
	}

	public GetLockerListEvent setTag(Object tag) {
		super.setTag(tag);
		return this;
	}

	public int getColumn() {
		return column;
	}

	public GetLockerListEvent setColumn(int column) {
		this.column = column;
		return this;
	}

	public int getOffset() {
		return offset;
	}

	public GetLockerListEvent setOffset(int offset) {
		this.offset = offset;
		return this;
	}

}
