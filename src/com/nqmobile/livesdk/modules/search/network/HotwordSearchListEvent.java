/**
 * 
 */
package com.nqmobile.livesdk.modules.search.network;

import com.nqmobile.livesdk.commons.net.AbsEvent;

/**
 * @author HouKangxi
 *
 */
public class HotwordSearchListEvent extends AbsEvent {
	private String[] hotwords;
	private String soUrl;
	private String hotwordUrl;

	public HotwordSearchListEvent(String[] hotwords) {
		this.hotwords = hotwords;
	}

	public String[] getHotwords() {
		return hotwords;
	}

	public String getSoUrl() {
		return soUrl;
	}

	HotwordSearchListEvent setSoUrl(String soUrl) {
		this.soUrl = soUrl;
		return this;
	}

	public String getHotwordUrl() {
		return hotwordUrl;
	}

	HotwordSearchListEvent setHotwordUrl(String hotwordUrl) {
		this.hotwordUrl = hotwordUrl;
		return this;
	}

}
