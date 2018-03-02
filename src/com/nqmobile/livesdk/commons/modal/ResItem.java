package com.nqmobile.livesdk.commons.modal;

import java.io.Serializable;


public class ResItem implements Serializable {
	private static final long serialVersionUID = 7689657161342714419L;
	
	public int index;
    protected String groupName;
    /** 下载时间 **/
    protected long downloadtime;
    public int mStatus;
    public int order = 0;
	
	public String getGroupName(){
		return groupName;
	}
	
	public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
	
	public long getDownloadtime() {
		return downloadtime;
	}

	public void setDownloadtime(long downloadTime) {
		this.downloadtime = downloadTime;
	}

	@Override
	public String toString() {
		return "ResItem [index=" + index + ", groupName=" + groupName + "]";
	}
	
}
