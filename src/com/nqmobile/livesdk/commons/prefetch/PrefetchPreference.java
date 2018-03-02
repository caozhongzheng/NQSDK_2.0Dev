package com.nqmobile.livesdk.commons.prefetch;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class PrefetchPreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
	/* 预取开关 */
	public static final String KEY_PREFETCH_ENABLE = "prefecth_enable";
//	public static final String KEY_DOWNLOAD_ALLOW_SIZE = "l_silent_download_allowsize";
//	public static final String KEY_DOWNLOAD_MODULE = "s_silent_download_module";
	public static final String KEY_DOWNLOAD_CACHESIZE = "l_silent_download_cachesize";
//	private static final long DEFAULT_DOWNLOAD_ALLOW_SIZE = 15 << 20;// 默认15M
	private static final long DEFAULT_DOWNLOAD_CACHESIZE = 300 << 20;// 默认300M
	// ===========================================================
	// Fields
	// ===========================================================
	private static PrefetchPreference sInstance = new PrefetchPreference();

	// ===========================================================
	// Constructors
	// ===========================================================
	private PrefetchPreference() {
	}

	public static PrefetchPreference getInstance() {
		return sInstance;
	}

	public long getMaxCacheSize() {
		long rs = getLongValue(KEY_DOWNLOAD_CACHESIZE);
		if (rs <= 0) {
			rs = DEFAULT_DOWNLOAD_CACHESIZE;
		}
		return rs;
	}

//	public long getMaxFileSize() {
//		long rs = getLongValue(KEY_DOWNLOAD_ALLOW_SIZE);
//		if (rs <= 0) {
//			rs = DEFAULT_DOWNLOAD_ALLOW_SIZE;
//		}
//		return rs;
//	}

//	private volatile SoftReference<Object[]> allowDownloadFeatureIdsCache;

//	public Collection<Integer> getAllowSlientDownloadFeatureIds() {
//		String fstr = getStringValue(KEY_DOWNLOAD_MODULE);
//		if (fstr == null) {
//			return null;
//		}
//		if (allowDownloadFeatureIdsCache != null) {
//			Object[] cacheData = allowDownloadFeatureIdsCache.get();
//			if(cacheData != null){
//				String key =  (String) cacheData[0];
//				if(fstr.equals(key)){
//					@SuppressWarnings("unchecked")
//					Collection<Integer> cacheRs = (Collection<Integer>) cacheData[1];
//					if (cacheRs != null) {
//						return cacheRs;
//					}
//				}
//			}
//		}
//		String[] ss = fstr.split(",");
//		Collection<Integer>  rs = null;
//		if (ss.length == 1) {
//			try {
//				int v = Integer.parseInt(ss[0].trim());
//				rs =  Collections.singleton(v);
//			} catch (NumberFormatException e) {
//				e.printStackTrace();
//			}
//		} else {
//			HashSet<Integer> set = new HashSet<Integer>(ss.length);
//			for (String s : ss) {
//				try {
//					int v = Integer.parseInt(s.trim());
//					set.add(v);
//				} catch (NumberFormatException e) {
//					e.printStackTrace();
//				}
//			}
//			rs =  set;
//		}
//		allowDownloadFeatureIdsCache = new SoftReference<Object[]>(new Object[]{fstr,rs});
//		return rs;
//	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public boolean isPrefetchEnable(){
        return getBooleanValue(KEY_PREFETCH_ENABLE);
    }
    public void setPrefetchEnable(boolean enable){
        setBooleanValue(KEY_PREFETCH_ENABLE, enable);
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
