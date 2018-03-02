package com.nqmobile.livesdk.modules.browserbandge;

import android.net.Uri;

/**
 * 
 * @author hanxintong
 *
 */
public class BrowseBandgeConstants {

	public static final String DATA_AUTHORITY = "com.nqmobile.live";

	public static final String _ID = "_id";

	/** 浏览器角标广告缓存 */
	private static final String BANDGE_CACHE_TABLE = "bandge_cache";
	public static final Uri BANDGE_CACHE_URI = Uri.parse("content://"
			+ DATA_AUTHORITY + "/" + BANDGE_CACHE_TABLE);

	public static final String BANDGE_ID = "resId";
	public static final String BANDGE_URL = "url";
	public static final String BANDGE_IS_DELIVERED = "delivered";

	/** 浏览器角标广告缓存 */
	private static final String DOWNLOAD_CACHE_TABLE = "bandge_cache";
	public static final Uri DOWNLOAD_CACHE_URI = Uri.parse("content://"
			+ DATA_AUTHORITY + "/" + DOWNLOAD_CACHE_TABLE);

	public static final String DOWNLOAD_ID = "resId";
	public static final String DOWNLOADING_URL = "url";
	public static final String DOWNLOAD_IS_DELIVERED = "loading";
}
