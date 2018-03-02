package com.nqmobile.livesdk.modules.weather.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.nq.interfaces.weather.THourlyForecasts;

public class HourlyForecasts {
	private String date;//时间(yyyy-mm-dd)
	private int hour;//小时
	private int weatherIcon;//天气现象图标类型
	private String weatherText;//天气现象:描述短语
	private String temperature;//一小时内平均温度
	private String temperatureUnit;//温度单位°C|°F
	private String relativeHumidity;//相对湿度
	private String visibilityValue;//能见度:值
	private String visibilityUnit;//能见度单位:km|mi
	private Wind wind;
	private Rain rain;

    public HourlyForecasts(){

    }
	
	public HourlyForecasts(THourlyForecasts hourly){
		this.date =  hourly.date;
		this.hour = hourly.hour;
		this.weatherIcon = hourly.weatherIcon;
		this.weatherText = hourly.weatherText;
		this.temperature = hourly.temperature;
		this.temperatureUnit = hourly.temperatureUnit;
		this.relativeHumidity = hourly.relativeHumidity;
		this.visibilityValue = hourly.visibilityValue;
		this.visibilityUnit = hourly.visibilityUnit;
		this.wind = hourly.wind == null ? null : new Wind(hourly.wind);
		this.rain = hourly.rain == null ? null : new Rain(hourly.rain);
	}
	
	public int getHour() {
		return hour;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
	public int getWeatherIcon() {
		return weatherIcon;
	}
	public void setWeatherIcon(int weatherIcon) {
		this.weatherIcon = weatherIcon;
	}
	public String getWeatherTest() {
		return weatherText;
	}
	public void setWeatherTest(String weatherTest) {
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
	public Rain getRain() {
		return rain;
	}
	public void setRain(Rain rain) {
		this.rain = rain;
	}
	
	public Calendar getDate(){
		if (date == null)
			return null;
		
		Calendar c = null;
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		try {
			c = Calendar.getInstance();
			Date dt = fmt.parse(date.substring(0,10));
			c.setTimeInMillis(dt.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return c;
	}

    public void setDate(String date) {
        this.date = date;
    }

    public String getWeatherText() {
        return weatherText;
    }

    public void setWeatherText(String weatherText) {
        this.weatherText = weatherText;
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
