package com.nqmobile.livesdk.modules.weather.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.nq.interfaces.weather.TDailyForecasts;

public class DailyForecasts {
	private String date;// 日期(yyyy-mm-dd)
    private LunarCalendar lunarCalendar; //农历信息
	private String minTemp;// 最低气温
	private String maxTemp;// 最高气温
	private String tempUnit;// 温度单位°C|°F
	private DayWeather day;// 白天天气情况
	private NightWeather night;// 夜晚天气情况

    public DailyForecasts(){

    }
	
	public DailyForecasts(TDailyForecasts daily){
		this.date = daily.date;
		this.minTemp = daily.minTemp;
		this.maxTemp =  daily.maxTemp;
		this.tempUnit = daily.tempUnit;
		this.day =  daily.day == null ? null : new DayWeather(daily.day);
		this.night = daily.night == null ? null : new NightWeather(daily.night);
        this.lunarCalendar = daily.lunarCalendar == null ? null : new LunarCalendar(daily.lunarCalendar);
	}

    public void setDate(String date) {
        this.date = date;
    }

    public LunarCalendar getLunarCalendar() {
        return lunarCalendar;
    }

    public void setLunarCalendar(LunarCalendar lunarCalendar) {
        this.lunarCalendar = lunarCalendar;
    }

    public String getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(String minTemp) {
        this.minTemp = minTemp;
    }

    public String getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(String maxTemp) {
        this.maxTemp = maxTemp;
    }

    public String getTempUnit() {
		return tempUnit;
	}

	public void setTempUnit(String tempUnit) {
		this.tempUnit = tempUnit;
	}

	public DayWeather getDay() {
		return day;
	}

	public void setDay(DayWeather day) {
		this.day = day;
	}

	public NightWeather getNight() {
		return night;
	}

	public void setNight(NightWeather night) {
		this.night = night;
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
}
