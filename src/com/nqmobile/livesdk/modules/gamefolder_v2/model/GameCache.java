package com.nqmobile.livesdk.modules.gamefolder_v2.model;

import com.nqmobile.livesdk.modules.app.App;

public class GameCache extends App {
	private static final long serialVersionUID = 6951251834282527888L;
	
	private long showTime;
	private int gameEnable;
	private int preloadFail;
	
	public long getShowTime() {
		return showTime;
	}
	public void setShowTime(long showTime) {
		this.showTime = showTime;
	}
	public int getGameEnable() {
		return gameEnable;
	}
	public void setGameEnable(int gameEnable) {
		this.gameEnable = gameEnable;
	}
	public int getPreloadFail() {
		return preloadFail;
	}
	public void setPreloadFail(int preloadFail) {
		this.preloadFail = preloadFail;
	}
}
