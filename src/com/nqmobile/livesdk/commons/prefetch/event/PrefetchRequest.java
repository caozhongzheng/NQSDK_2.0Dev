/**
 * 
 */
package com.nqmobile.livesdk.commons.prefetch.event;

import java.io.Serializable;

/**
 * @author HouKangxi
 *
 */
public class PrefetchRequest implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2752622963977065163L;
	public final static int TYPE_ICON = 1;
	public final static int TYPE_PREVIEW = 2;
	public final static int TYPE_APK = 3;
	public final static int TYPE_AUDIO = 4;
	public final static int TYPE_VIDEO = 5;
	
	private String mResId;// string
	private String mPackageName;
	private int mType;// int
	private String mUrl;
	private String mPath;
	private long mSize;
	private PrefetchEvent mPrefetchEventEvent;
	
	public PrefetchRequest(PrefetchEvent prefetchEventEvent,String resId, int type,
			String url, String path, long size) {
		super();
		this.mPrefetchEventEvent = prefetchEventEvent;
		this.mResId = resId;
		this.mType = type;
		this.mUrl = url;
		this.mPath = path;
		this.mSize = size;
	}
	
	PrefetchRequest(){
	}
    public int getFeatureId(){
    	return mPrefetchEventEvent.getFeatureId();
    }
	public PrefetchEvent getPrefetchEventEvent() {
		return mPrefetchEventEvent;
	}

	public String getResId() {
		return mResId;
	}

	public void setResId(String mResId) {
		this.mResId = mResId;
	}

	public int getType() {
		return mType;
	}

	public void setType(int mType) {
		this.mType = mType;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public String getPath() {
		return mPath;
	}

	public void setPath(String mPath) {
		this.mPath = mPath;
	}

	public long getSize() {
		return mSize;
	}

	public void setSize(long mSize) {
		this.mSize = mSize;
	}

	public String getPackageName() {
		return mPackageName;
	}

	public void setPackageName(String packageName) {
		this.mPackageName = packageName;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()+" [mResId=" + mResId + ", mType=" + mType
				+ ", mUrl=" + mUrl + ", mPath=" + mPath + ", mSize=" + mSize
				+ ", mPrefetchEventEvent=" + mPrefetchEventEvent + "]";
	}

}
