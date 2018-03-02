package com.nqmobile.livesdk.modules.search;

import java.io.Serializable;

public class Searcher implements Serializable {

	private static final long serialVersionUID = -5558760468924107582L;

	private String url;
	private String category;
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

}
