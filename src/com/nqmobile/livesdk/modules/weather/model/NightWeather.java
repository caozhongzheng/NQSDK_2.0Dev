package com.nqmobile.livesdk.modules.weather.model;

import com.nq.interfaces.weather.TNight;

public class NightWeather {
	private int weatherIcon;// 天气现象图标类型
	private String weatherText;// 天气现象:描述短语
	private Wind wind;
	private Rain rain;

    public NightWeather(){

    }
	
	public NightWeather(TNight night){
		this.weatherIcon = night.weatherIcon;
		this.weatherText = night.weatherText;
		this.wind = night.wind == null? null : new Wind(night.wind);
		this.rain = night.rain == null? null : new Rain(night.rain);
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

	public void setWeatherText(String weatherText) {
		this.weatherText = weatherText;
	}

	public Wind getWind() {
		return wind;
	}

	public void setWind(Wind wind) {
		this.wind = wind;
	}

	public Rain getRain() {
		return rain;
	}

	public void setRain(Rain rain) {
		this.rain = rain;
	}
}
