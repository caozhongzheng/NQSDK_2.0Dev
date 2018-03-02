package com.nqmobile.livesdk.modules.appstubfolder.model;

import java.io.Serializable;
import java.util.ArrayList;

import com.nqmobile.livesdk.modules.appstub.model.AppStub;

public class AppStubFolder implements Serializable {

	private static final long serialVersionUID = -2938119651487904373L;

	/** 虚框文件夹ID */
	private long folderId;
	/** 所在屏幕 0:主屏 1:主屏右1屏 -1:主屏左1屏 */
	private int screen;
	/** 所在x */
	private int x;
	/** 所在y */
	private int y;
	/** 文件夹名 */
	private String name;
	/** iconURL */
	private String iconUrl;
	/** iconPATH */
	private String iconPath;
	/** icon资源ID */
	private int iconId;
	/** 类型 0:预置 1:在线 */
	private int type;
	/** true:可编辑(文件名) 默认false */
	private boolean editable;
	/** true:可删除 默认false */
	private boolean deletable;
	/** 虚框应用列表 */
	private ArrayList<AppStub> stubList;
	/** 是否显示红点 */
	private boolean showRedPoint;

	public long getFolderId() {
		return folderId;
	}

	public void setFolderId(long folderId) {
		this.folderId = folderId;
	}

	public int getScreen() {
		return screen;
	}

	public void setScreen(int screen) {
		this.screen = screen;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public int getIconId() {
		return iconId;
	}

	public void setIconId(int iconId) {
		this.iconId = iconId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}

	public ArrayList<AppStub> getStubList() {
		return stubList;
	}

	public void setStubList(ArrayList<AppStub> stubList) {
		this.stubList = stubList;
	}

	@Override
	public String toString() {
		return "StubFolder [folderId=" + folderId + ", screen=" + screen
				+ ", x=" + x + ", y=" + y + ", name=" + name + ", iconUrl="
				+ iconUrl + ", iconId=" + iconId + ", type=" + type
				+ ", editable=" + editable + ", deletable=" + deletable
				+ ", stubList=" + stubList + "]";
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

    public boolean isShowRedPoint() {
        return showRedPoint;
    }

    public void setShowRedPoint(boolean showRedPoint) {
        this.showRedPoint = showRedPoint;
    }
}
