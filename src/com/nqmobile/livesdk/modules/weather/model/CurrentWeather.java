package com.nqmobile.livesdk.modules.weather.model;

import com.nq.interfaces.weather.TCurrentWeather;

public class CurrentWeather {
	private int weatherIcon;// 天气现象图标类型
	private String weatherText;// 天气现象:描述短语
	private String temperature;// 当前温度
	private String temperatureUnit;// 温度单位C|F
	private String relativeHumidity;// 相对湿度
	private String uvIndex;// 紫外线强度
	private String visibilityValue;// 能见度:值
	private String visibilityUnit;// 能见度单位:km|mi
	private Wind wind;// 当前风速及方向
	private Rain precip1hr;// 最近一小时降雨情况

    public CurrentWeather(){

    }
	
	public CurrentWeather(TCurrentWeather cur){
		this.weatherIcon = cur.weatherIcon;
		this.weatherText = cur.weatherText;
		this.temperature = cur.temperature;
		this.temperatureUnit = cur.temperatureUnit;
		this.relativeHumidity = cur.relativeHumidity;
		this.uvIndex = cur.uvIndex;
		this.visibilityValue = cur.visibilityValue;
		this.visibilityUnit = cur.visibilityUnit;
		this.wind = cur.wind == null ? null : new Wind(cur.wind);		
		this.precip1hr = cur.precip1hr == null ? null : new Rain(cur.precip1hr);
	}

	public int getWeatherIcon() {
		return weatherIcon;
	}

	public void setWeatherIcon(int weatherIcon) {
		this.weatherIcon = weatherIcon;
	}

	public String getWeatherText() {
		return weatherText;
	}

	public void setWeatherText(String weatherTest) {
		this.weatherText = weatherTest;
	}

	public String getTemperatureUnit() {
		return temperatureUnit;
	}

	public void setTemperatureUnit(String temperatureUnit) {
		this.temperatureUnit = temperatureUnit;
	}

	public String getRelativeHumidity() {
		return relativeHumidity;
	}

	public void setRelativeHumidity(String relativeHumidity) {
		this.relativeHumidity = relativeHumidity;
	}

	public String getUvIndex() {
		return uvIndex;
	}

	public void setUvIndex(String uvIndex) {
		this.uvIndex = uvIndex;
	}

	public String getVisibilityUnit() {
		return visibilityUnit;
	}

	public void setVisibilityUnit(String visibilityUnit) {
		this.visibilityUnit = visibilityUnit;
	}

	public Wind getWind() {
		return wind;
	}

	public void setWind(Wind wind) {
		this.wind = wind;
	}

	public Rain getPrecip1hr() {
		return precip1hr;
	}

	public void setPrecip1hr(Rain precip1hr) {
		this.precip1hr = precip1hr;
	}

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getVisibilityValue() {
        return visibilityValue;
    }

    public void setVisibilityValue(String visibilityValue) {
        this.visibilityValue = visibilityValue;
    }
}
