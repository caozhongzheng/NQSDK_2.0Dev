package com.nqmobile.livesdk.modules.gamefolder_v2.model;

import java.io.Serializable;

import android.graphics.Bitmap;

public class Game implements Serializable {

	/** app_name: 应用名 */
	private String app_name;
	/** package_name: 包名 */
	private String package_name;
	/** class_name: 类名 */
	private String class_name;
	/** type: 0 普通程序入口 1 普通快捷方式 2 固定位置的特殊快捷方式 */
	private int type;
	/** category: 0 应用 , 1快捷方式 */
	private int category;
	/** fixed: 0 置顶固定 1 置底固定 2 不用固定 */
	private int fixed;
	/** weight: 权重。多个元素同时置顶或置底时，依靠权重定义显示顺序，值越小越靠前。权重相等时，后添加的元素优先置顶或优先置底 */
	private int weight;
	/** shortcut_name: 快捷方式名 */
	private String shortcut_name;
	/** shortcut_name_id: 快捷方式名String id */
	private int shortcut_name_id;
	/** shortcut_icon_name: 快捷方式icon名 */
	private String shortcut_icon_name;
	/** shortcut_icon_name: 快捷方式icon Bitmap */
	private Bitmap shortcut_icon;

	public Game() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getApp_name() {
		return app_name;
	}

	public void setApp_name(String app_name) {
		this.app_name = app_name;
	}

	public String getPackage_name() {
		return package_name;
	}

	public void setPackage_name(String package_name) {
		this.package_name = package_name;
	}

	public String getClass_name() {
		return class_name;
	}

	public void setClass_name(String class_name) {
		this.class_name = class_name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int getFixed() {
		return fixed;
	}

	public void setFixed(int fixed) {
		this.fixed = fixed;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getShortcut_name() {
		return shortcut_name;
	}

	public void setShortcut_name(String shortcut_name) {
		this.shortcut_name = shortcut_name;
	}

	public int getShortcut_name_id() {
		return shortcut_name_id;
	}

	public void setShortcut_name_id(int shortcut_name_id) {
		this.shortcut_name_id = shortcut_name_id;
	}

	public String getShortcut_icon_name() {
		return shortcut_icon_name;
	}

	public void setShortcut_icon_name(String shortcut_icon_name) {
		this.shortcut_icon_name = shortcut_icon_name;
	}

	public Bitmap getShortcut_icon() {
		return shortcut_icon;
	}

	public void setShortcut_icon(Bitmap shortcut_icon) {
		this.shortcut_icon = shortcut_icon;
	}

}
