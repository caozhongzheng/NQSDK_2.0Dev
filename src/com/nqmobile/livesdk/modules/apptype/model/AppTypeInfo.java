package com.nqmobile.livesdk.modules.apptype.model;

public class AppTypeInfo {
	public final static int CODE_UNKNOWN = 0;
	public final static int CODE_GAME = 200;//200游戏
	
	/** 应用包名 */
	private String packageName;
	/** 类型定义 122工具,200游戏,120社交,114影音,124生活,103阅读,106娱乐,117摄影,119购物,100其他,0未知 */
	private int code;
	/** 应用一级分类，如：效率、系统 */
	private String category1;
	/** 应用 二级分类 */
	private String category2;

	public AppTypeInfo() {
	}

	public AppTypeInfo(String packageName, int code, String category1,
			String category2) {
		super();
		this.packageName = packageName;
		this.code = code;
		this.category1 = category1;
		this.category2 = category2;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getCategory1() {
		return category1;
	}

	public void setCategory1(String category1) {
		this.category1 = category1;
	}

	public String getCategory2() {
		return category2;
	}

	public void setCategory2(String category2) {
		this.category2 = category2;
	}

}
