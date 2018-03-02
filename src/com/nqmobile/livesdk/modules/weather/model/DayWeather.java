package com.nqmobile.livesdk.modules.weather.model;

import com.nq.interfaces.weather.TDay;

public class DayWeather {
	private int weatherIcon;// 天气现象图标类型
	private String weatherText;// 天气现象:描述短语
	private Wind wind;
	private Rain rain;

    public DayWeather(){

    }
	
	public DayWeather(TDay day){
		this.weatherIcon = day.weatherIcon;
		this.weatherText = day.weatherText;
		this.wind = day.wind == null? null : new Wind(day.wind);
		this.rain = day.rain == null? null : new Rain(day.rain);
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
