package com.nqmobile.livesdk.modules.weather.model;

import com.nq.interfaces.weather.TWind;

public class Wind {
	private String speedValue;// 风速:值
	private String speedUnit;// 风速:km/h|mi/h
	private String direction;// 风向
	private String phrase;// 描述微风,大风等

    public Wind(){

    }
	
	public Wind(TWind wind){
		this.speedValue = wind.speedValue;
		this.speedUnit = wind.speedUnit;
		this.direction = wind.direction;
		this.phrase = wind.phrase;
	}

	public String getSpeedUnit() {
		return speedUnit;
	}

	public void setSpeedUnit(String speedUnit) {
		this.speedUnit = speedUnit;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

    public String getSpeedValue() {
        return speedValue;
    }

    public void setSpeedValue(String speedValue) {
        this.speedValue = speedValue;
    }

	public String getPhrase() {
		return phrase;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}
}
