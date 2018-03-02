package com.nqmobile.livesdk.commons;

/**
 * 存常量
 * @author changxiaofei
 * @time 2013-11-27 下午12:33:20
 */
public class AppConstants {
	/* 主页fragment的项索引 */
	/** store 模块编号 widget模块*/
	public static final int STORE_MODULE_TYPE_WIDGET = 3;
	/** store 模块编号 Launcher壁纸和主题图标列表模块*/
	public static final int STORE_MODULE_TYPE_LAUNCHER_ICON = 4;
    /** store 模块编号 游戏广告*/
    public static final int STORE_MODULE_TYPE_GAME_AD = 5;
    /** store 模块编号 装机必备 */
    public static final int STORE_MODULE_TYPE_MUST_INSTALL = 6;
	/** store 模块编号，PUSH模块 */
	public static final int STORE_MODULE_TYPE_PUSH = 7;
	
	/** store 模块编号，积分中心 */
	public static final int STORE_MODULE_TYPE_POINTS_CENTER = 9;
	/** 模块编号，小组件模块 */
	public static final int LQ_MODULE_TYPE_LqWidget = 30;
	
	/************************** 板块编号 重新定义 by 2014-6-30 ysongren start **********************/
	

	/************************** 板块编号 重新定义 by 2014-6-30 ysongren end **********************/
	
	
	public static final int PUSH_CACHE_TYPE_APP = 0;
	public static final int PUSH_CACHE_TYPE_THEME = 1;
	public static final int PUSH_CACHE_TYPE_WALLPAPER = 2;
	
	/** store 应用、主题、壁纸下载路径 */
	public static final String STORE_MAIN = "/LiveStore/";
	public static final String STORE_IMAGE_LOCAL_PATH = "/LiveStore/";
	/**行为日志*/
	/**进入store*/
	public static final String ACTION_LOG_1000 = "1000";
	
    
//	/**创建游戏文件夹*/
//	public static final String ACTION_LOG_1701 = "1701";
//	/**点击打开游戏文件夹*/
//	public static final String ACTION_LOG_1702 = "1702";
//	/**碰运气图标的点击*/
//	public static final String ACTION_LOG_1703 = "1703";
//	/**游戏文件夹内推荐资源展示*/
//	public static final String ACTION_LOG_1704 = "1704";
//	/**游戏文件夹内推荐资源点击*/
//	public static final String ACTION_LOG_1705 = "1705";
//	/**推荐弹框中安装按钮的点击*/
//	public static final String ACTION_LOG_1706 = "1706";
//	/**推荐弹框中换一个按钮的点击*/
//	public static final String ACTION_LOG_1707 = "1707";
//	/**游戏文件夹内推荐资源的下载*/
//	public static final String ACTION_LOG_1708 = "1708";
//	/**创建广告按钮（加号按钮）*/
////	public static final String ACTION_LOG_1709 = "1709";
//	/**删除游戏文件夹*/
//	public static final String ACTION_LOG_1710 = "1710";
//	/**********新版游戏文件夹**********/
//	/**创建游戏文件夹*/
//	public static final String ACTION_LOG_1711 = "1711";
//	/**点击打开游戏文件夹*/
//	public static final String ACTION_LOG_1712 = "1712";
//	/**游戏文件夹内推荐资源展示*/
//	public static final String ACTION_LOG_1713 = "1713";
//	/**游戏文件夹内推荐资源点击*/ //need SH
//	public static final String ACTION_LOG_1714 = "1714";
//	/**推荐弹框中下载按钮的点击*/ //need SH
//	public static final String ACTION_LOG_1715 = "1715";
//	/**游戏文件夹内推荐资源的下载*/ //need SH
//	public static final String ACTION_LOG_1716 = "1716";
//	/**删除游戏文件夹*/
//	public static final String ACTION_LOG_1717 = "1717";
//	/**关闭热门推荐*/
//	public static final String ACTION_LOG_1718 = "1718";
//	/**加速报告按钮点击*/  //need SH
//	public static final String ACTION_LOG_1719 = "1719";
//	/**加速报告成功展示*/  //need SH
//	public static final String ACTION_LOG_1720 = "1720";
//	/**游戏文件夹内拉杆的点击*/  
//	public static final String ACTION_LOG_1725 = "1725";
//	/**游戏推荐详情页资源展示*/  
//	public static final String ACTION_LOG_1726 = "1726";
//	
	
//    /** 弹出装机必备push信息 */
//    public static final String ACTION_LOG_1801 = "1801";
//    /** 装机必备首次弹出 */
//    public static final String ACTION_LOG_1802 = "1802";
//    /** 创建装机必备快捷方式到桌面 */
//    public static final String ACTION_LOG_1803 = "1803";
//    /** 点击一键安装按钮 */
//    public static final String ACTION_LOG_1804 = "1804";
//    /** 点击桌面快捷方式打开装机必备 */
//    public static final String ACTION_LOG_1805 = "1805";
//    /** 装机必备资源展示 */
//    public static final String ACTION_LOG_1806 = "1806";
//    /** 装机必备资源下载 */
//    public static final String ACTION_LOG_1807 = "1807";
//    /** 获取更多按钮的点击*/
//    public static final String ACTION_LOG_1808 = "1808";

//    /** push信息展示 */
//    public static final String ACTION_LOG_1901 = "1901";
//    /** push信息点击 */
//    public static final String ACTION_LOG_1902 = "1902";
//    /** 应用资源下载（来源是push）*/
//    public static final String ACTION_LOG_1903 = "1903";
//    /** 点击不喜欢按钮*/
//    public static final String ACTION_LOG_1904 = "1904";
//    /** 点击下载按钮*/
//    public static final String ACTION_LOG_1905 = "1905";
    


//    /** 角标展示 */
//    public static final String ACTION_LOG_2201 = "2201";
//    /** 角标点击 */
//    public static final String ACTION_LOG_2202 = "2202";
    
//    /** 关联推荐应用的展示 */
//    public static final String ACTION_LOG_2401 = "2401";
//    /** 关联推荐应用点击下载*/
//    public static final String ACTION_LOG_2402 = "2402";

//    /**点击store右上角入口进入积分中心 */
//    public static final String ACTION_LOG_2501 = "2501";
//    /** 切换到积分主题栏目 */
//    public static final String ACTION_LOG_2502 = "2502";
//    /**切换到获取更多积分栏目 */
//    public static final String ACTION_LOG_2503 = "2503";
//    /**获取更多积分列表中应用下载按钮点击 */
//    public static final String ACTION_LOG_2504 = "2504";
//    /**获取更多积分列表中应用安装按钮点击 */
//    public static final String ACTION_LOG_2505 = "2505";
//    /**获取更多积分列表中应用打开按钮点击 */
//    public static final String ACTION_LOG_2506 = "2506";
//    /**积分主题栏目中积分主题点击 */
//    public static final String ACTION_LOG_2507 = "2507";
//    /**积分不足弹框显示 */
//    public static final String ACTION_LOG_2508 = "2508";
//    /**积分不足弹框中应用icon点击 */
//    public static final String ACTION_LOG_2509 = "2509";
//    /**积分不足弹框中一键安装按钮点击 */
//    public static final String ACTION_LOG_2510 = "2510";
//    /**积分不足弹框中进入积分中心按钮点击 */
//    public static final String ACTION_LOG_2511 = "2511";
//    /**兑换记录按钮点击 */
//    public static final String ACTION_LOG_2512 = "2512";
//    /**兑换记录页面成功展示 */
//    public static final String ACTION_LOG_2513 = "2513";


}
