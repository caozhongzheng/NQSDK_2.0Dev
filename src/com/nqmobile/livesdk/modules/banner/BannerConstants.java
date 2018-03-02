package com.nqmobile.livesdk.modules.banner;

/**
 * Banner常量
 * @author renys
 * @date 2014-11-24
 */
public class BannerConstants {

	/** store 模块编号，banner列表模块 */
	public static final int STORE_MODULE_TYPE_BANNER = 1;
    public static final long CACHE_MAX_TIME = 24 * 60 * 60 * 1000;//24小时
    /** store 应用、主题、壁纸下载路径 */
	public static final String STORE_IMAGE_LOCAL_PATH_BANNER = "/LiveStore/banner/";
	
	/** store 板块编号，应用、游戏，getBannerList_New2里的plate=3*/
	public static final int STORE_PLATE_TYPE_APP = 0; 
	/** store 板块编号，主题，getBannerList_New2里的plate=0*/
	public static final int STORE_PLATE_TYPE_THEME = 1;
	/** store 板块编号，锁屏，getBannerList_New2里的plate=1 */
	public static final int STORE_PLATE_TYPE_LOCKER = 3;
	/** store 板块编号，壁纸 ，getBannerList_New2里的plate=2*/
	public static final int STORE_PLATE_TYPE_WALLPAPER = 2;
}
