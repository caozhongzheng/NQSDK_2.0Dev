package com.nqmobile.livesdk.modules.weather.model;

import com.nq.interfaces.weather.TRain;

public class Rain {
	private String rainValue;// 降雨量
	private String rainUnit;// 降雨量单位 mm
	private String rainPhrase;// 降雨情况描述

    public Rain(){

    }
	
	public Rain(TRain rain){
		this.rainValue = rain.rainValue;
		this.rainUnit = rain.rainUnit;
		this.rainPhrase = rain.rainPhrase;
	}

	public String getRainUnit() {
		return rainUnit;
	}

	public void setRainUnit(String rainUnit) {
		this.rainUnit = rainUnit;
	}

	public String getRainPhrase() {
		return rainPhrase;
	}

	public void setRainPhrase(String rainPhrase) {
		this.rainPhrase = rainPhrase;
	}

    public String getRainValue() {
        return rainValue;
    }

    public void setRainValue(String rainValue) {
        this.rainValue = rainValue;
    }
}
