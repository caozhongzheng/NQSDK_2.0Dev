package com.nqmobile.livesdk.modules.incrementupdate.model;

import java.io.Serializable;

public class NewVersion implements Serializable {
	private static final long serialVersionUID = -8112909223172038394L;
	// ===========================================================
    // Constants
    // ===========================================================
	
	// ===========================================================
	// Fields
	// ===========================================================
    //private int hasNewVersion;//是否有新版本, 0 无新版本， 1 新版本
    private String newVersionName; //新包版本名称, 例如 1.1.02.00
    private int upgradeType; //升级的方式：0 只做全量，1  增量优先
    private int autoDownload; //WIFI下是否自动下载更新包： 0 自动下载, 1 不自动下载
    private int promptType; //更新提示方式选择：0 桌面弹框 1 系统通知栏
    private String promptTitle; //更新提示标题
    private String promptContent; //更新提示内容
    private String promptStartTime; //更新提示时间段的开始时间(格式:08:30)
    private String promptEndTime; //更新提示时间段的结束时间
    private int promptInterval; //更新提示时间段的时间间隔 （天）
    private int promptNetwork; //更新提示展示场景：0 有网络  1 WIFI
    private int oldFileSize; //旧版本全量包的大小（byte）
    private String oldFileMd5; //旧版本全量包的md5
    private String patchAlgorithm; //生成patch包的算法版本号
    private int patchSize; //patch包大小（byte）
    private String patchMd5; //patch包md5
    private String patchUrl; //patch包下载地址 
    private int newFileSize; //新版本全量包的大小（byte）
    private String newFileMd5; //新版本全量包的md5
    private String newFileUrl; //新版本全量包下载url
    
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public String getNewVersionName() {
		return newVersionName;
	}
	public void setNewVersionName(String newVersionName) {
		this.newVersionName = newVersionName;
	}
	public int getUpgradeType() {
		return upgradeType;
	}
	public void setUpgradeType(int upgradeType) {
		this.upgradeType = upgradeType;
	}
	public int getAutoDownload() {
		return autoDownload;
	}
	public void setAutoDownload(int autoDownload) {
		this.autoDownload = autoDownload;
	}
	public int getPromptType() {
		return promptType;
	}
	public void setPromptType(int promptType) {
		this.promptType = promptType;
	}
	public String getPromptTitle() {
		return promptTitle;
	}
	public void setPromptTitle(String promptTitle) {
		this.promptTitle = promptTitle;
	}
	public String getPromptContent() {
		return promptContent;
	}
	public void setPromptContent(String promptContent) {
		this.promptContent = promptContent;
	}
	public String getPromptStartTime() {
		return promptStartTime;
	}
	public void setPromptStartTime(String promptStartTime) {
		this.promptStartTime = promptStartTime;
	}
	public String getPromptEndTime() {
		return promptEndTime;
	}
	public void setPromptEndTime(String promptEndTime) {
		this.promptEndTime = promptEndTime;
	}
	public int getPromptInterval() {
		return promptInterval;
	}
	public void setPromptInterval(int promptInterval) {
		this.promptInterval = promptInterval;
	}
	public int getPromptNetwork() {
		return promptNetwork;
	}
	public void setPromptNetwork(int promptNetwork) {
		this.promptNetwork = promptNetwork;
	}
	public int getOldFileSize() {
		return oldFileSize;
	}
	public void setOldFileSize(int oldFileSize) {
		this.oldFileSize = oldFileSize;
	}
	public String getOldFileMd5() {
		return oldFileMd5;
	}
	public void setOldFileMd5(String oldFileMd5) {
		this.oldFileMd5 = oldFileMd5;
	}
	public String getPatchAlgorithm() {
		return patchAlgorithm;
	}
	public void setPatchAlgorithm(String patchAlgorithm) {
		this.patchAlgorithm = patchAlgorithm;
	}
	public int getPatchSize() {
		return patchSize;
	}
	public void setPatchSize(int patchSize) {
		this.patchSize = patchSize;
	}
	public String getPatchMd5() {
		return patchMd5;
	}
	public void setPatchMd5(String patchMd5) {
		this.patchMd5 = patchMd5;
	}
	public String getPatchUrl() {
		return patchUrl;
	}
	public void setPatchUrl(String patchUrl) {
		this.patchUrl = patchUrl;
	}
	public int getNewFileSize() {
		return newFileSize;
	}
	public void setNewFileSize(int newFileSize) {
		this.newFileSize = newFileSize;
	}
	public String getNewFileMd5() {
		return newFileMd5;
	}
	public void setNewFileMd5(String newFileMd5) {
		this.newFileMd5 = newFileMd5;
	}
	public String getNewFileUrl() {
		return newFileUrl;
	}
	public void setNewFileUrl(String newFileUrl) {
		this.newFileUrl = newFileUrl;
	}
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public String toString() {
		return "NewVersion [newVersionName=" + newVersionName
				+ ", upgradeType=" + upgradeType
				+ ", autoDownload=" + autoDownload
				+ ", promptType=" + promptType				
				+ ", promptTitle=" + promptTitle
				+ ", promptContent=" + promptContent
				+ ", promptStartTime=" + promptStartTime
				+ ", promptEndTime=" + promptEndTime
				+ ", promptInterval=" + promptInterval
				+ ", promptNetwork=" + promptNetwork				
				+ ", oldFileSize=" + oldFileSize
				+ ", oldFileMd5=" + oldFileMd5
				+ ", patchAlgorithm=" + patchAlgorithm
				+ ", patchSize=" + patchSize
				+ ", patchMd5=" + patchMd5				
				+ ", patchUrl=" + patchUrl
				+ ", newFileSize=" + newFileSize
				+ ", newFileMd5=" + newFileMd5
				+ ", newFileUrl=" + newFileUrl
				+ "]";
	}
	
	// ===========================================================
	// Methods
	// ===========================================================

	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
