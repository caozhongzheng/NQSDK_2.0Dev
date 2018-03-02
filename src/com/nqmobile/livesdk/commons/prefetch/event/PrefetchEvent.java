/**
 * 
 */
package com.nqmobile.livesdk.commons.prefetch.event;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author HouKangxi
 *
 */
public class PrefetchEvent implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3329262120999084977L;
	private int mFeatureId;// int
	private int mSourceType;//int

	public List<PrefetchRequest> mRequests;

	public PrefetchEvent() {
	}

	public PrefetchEvent(int featureId, int sourceType, List<PrefetchRequest> requests) {
		mFeatureId = featureId;
		mSourceType = sourceType;
		mRequests = requests;
	}

	public int getFeatureId() {
		return mFeatureId;
	}

	public int getSourceType() {
		return mSourceType;
	}

	public void setSourceType(int mSourceType) {
		this.mSourceType = mSourceType;
	}

	public void setFeatureId(int mFeatureId) {
		this.mFeatureId = mFeatureId;
	}

	public List<PrefetchRequest> getRequests() {
		return mRequests;
	}

	public void setRequests(List<PrefetchRequest> mRequests) {
		this.mRequests = mRequests;
	}

	@Override
	public String toString() {
		return "PrefetchEvent [mFeatureId=" + mFeatureId + ", mSourceType="
				+ mSourceType + "]";
	}

}
