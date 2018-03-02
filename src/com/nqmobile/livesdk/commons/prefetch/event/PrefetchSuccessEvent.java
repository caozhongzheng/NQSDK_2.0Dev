/**
 * 
 */
package com.nqmobile.livesdk.commons.prefetch.event;

/**
 * 静默下载完毕的事件
 */
public class PrefetchSuccessEvent {
	private String mResId;
	private String mPackageName;
	private int featureId;
	private int sourceType;
	private String mUrl;
	private String mPath;
	private long mSize;
	/**
	 * one of below:<ul>
	 *  <li>{@link PrefetchRequest#TYPE_ICON} </li>
	 *  <li>{@link PrefetchRequest#TYPE_PREVIEW}</li>
	 *  <li>{@link PrefetchRequest#TYPE_APK}</li>
	 *  <li>{@link PrefetchRequest#TYPE_AUDIO}</li>
	 *  <li>{@link PrefetchRequest#TYPE_VIDEO}</li>
	 */
	private int mType;//
	
	public String getResId() {
		return mResId;
	}

	public PrefetchSuccessEvent setResId(String mResId) {
		this.mResId = mResId;return this;
	}

	public String getUrl() {
		return mUrl;
	}

	public PrefetchSuccessEvent setUrl(String mUrl) {
		this.mUrl = mUrl;return this;
	}

	public String getPath() {
		return mPath;
	}

	public PrefetchSuccessEvent setPath(String mPath) {
		this.mPath = mPath;return this;
	}

	public long getSize() {
		return mSize;
	}

	public PrefetchSuccessEvent setSize(long mSize) {
		this.mSize = mSize;return this;
	}

	public int getFeatureId() {
		return featureId;
	}

	public PrefetchSuccessEvent setFeatureId(int featureId) {
		this.featureId = featureId;return this;
	}
	/**
	 * @return one of below:<ul>
	 *  <li>{@link PrefetchRequest#TYPE_ICON} </li>
	 *  <li>{@link PrefetchRequest#TYPE_PREVIEW}</li>
	 *  <li>{@link PrefetchRequest#TYPE_APK}</li>
	 *  <li>{@link PrefetchRequest#TYPE_AUDIO}</li>
	 *  <li>{@link PrefetchRequest#TYPE_VIDEO}</li>
	 */
	public int getType() {
		return mType;
	}

	public PrefetchSuccessEvent setType(int mType) {
		this.mType = mType;return this;
	}

	public String getPackageName() {
		return mPackageName;
	}

	public PrefetchSuccessEvent setPackageName(String packageName) {
		this.mPackageName = packageName;return this;
	}

	public int getSourceType() {
		return sourceType;
	}

	public PrefetchSuccessEvent setSourceType(int sourceType) {
		this.sourceType = sourceType; return this;
	}
}
