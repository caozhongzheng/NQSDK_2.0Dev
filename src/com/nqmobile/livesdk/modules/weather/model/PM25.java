package com.nqmobile.livesdk.modules.weather.model;

import com.nq.interfaces.weather.TPM25;

/**
 * Created by Rainbow on 14-2-24.
 */
public class PM25 {

    public String value; //pm值:189
    public String desc; //pm描述:中度污染
    public String tips;  //pm提示:天气状况不佳，外出应采取必要的防护措施，特别是易感人群。
    public String updateTime; //pm发布时间:12时更新。
    public int icon;

    public PM25(){

    }

    public PM25(TPM25 tpm25){
        value = tpm25.value;
        desc = tpm25.desc;
        tips = tpm25.tips;
        updateTime = tpm25.updateTime;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}
}
