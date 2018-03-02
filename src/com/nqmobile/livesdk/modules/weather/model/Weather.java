package com.nqmobile.livesdk.modules.weather.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.nq.interfaces.weather.*;

public class Weather {
    private String dateTime; //当前时间2014-02-23T23:03:00
	private long publishTimeStamp;// 天气更新时间戳
    private String publishTime; //天气发布时间2014-02-23T23:03:00
	private City city;// 城市信息
	private CurrentWeather currentWeather;// 当前天气
	private List<DailyForecasts> dailyForecasts;// 未来几天天气
	private List<HourlyForecasts> hourlyForecasts;// 未来几小时天气
    private List<LifeExponent> lifeExponents; //生活指数
    private PM25 pm25; //pm25

    public Weather(){

    }

	public Weather(TWeather weather) {
        this.dateTime = weather.dateTime;
		this.publishTimeStamp = weather.publishTimeStamp;
        this.publishTime = weather.publishTime;
		this.city = weather.city == null ? null : new City(weather.city);
		this.currentWeather = weather.currentWeather == null ? null
				: new CurrentWeather(weather.currentWeather);
		if (weather.dailyForecasts != null && weather.dailyForecasts.size() > 0) {
			List<DailyForecasts> list = new ArrayList<DailyForecasts>();
			for (int i = 0; i < weather.dailyForecasts.size(); i++) {
				TDailyForecasts daily = weather.dailyForecasts
						.get(i);
				if (daily != null) {
					list.add(new DailyForecasts(daily));
				}
			}
			this.dailyForecasts = list;
		} else {
			this.dailyForecasts = null;
		}
		
		if (weather.hourlyForecasts != null && weather.hourlyForecasts.size() > 0) {
			List<HourlyForecasts> list = new ArrayList<HourlyForecasts>();
			for (int i = 0; i < weather.hourlyForecasts.size(); i++) {
				THourlyForecasts hourly = weather.hourlyForecasts
						.get(i);
				if (hourly != null) {
					list.add(new HourlyForecasts(hourly));
				}
			}
			this.hourlyForecasts = list;
		} else {
			this.hourlyForecasts = null;
		}

        if (weather.lifeExponents != null && weather.lifeExponents.size() > 0) {
            List<LifeExponent> list = new ArrayList<LifeExponent>();
            for (int i = 0; i < weather.lifeExponents.size(); i++) {
                TLifeExponent lifeExponent = weather.lifeExponents.get(i);
                if (lifeExponent != null) {
                    list.add(new LifeExponent(lifeExponent));
                }
            }
            this.lifeExponents = list;
        } else {
            this.lifeExponents = null;
        }

        this.pm25 = weather.pm25 == null ? null : new PM25(weather.pm25);
	}

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public long getPublishTimeStamp() {
        return publishTimeStamp;
    }

    public void setPublishTimeStamp(long publishTimeStamp) {
        this.publishTimeStamp = publishTimeStamp;
    }

    public List<LifeExponent> getLifeExponents() {
        return lifeExponents;
    }

    public void setLifeExponents(List<LifeExponent> lifeExponents) {
        this.lifeExponents = lifeExponents;
    }

    public PM25 getPm25() {
        return pm25;
    }

    public void setPm25(PM25 pm25) {
        this.pm25 = pm25;
    }

    public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public CurrentWeather getCurrentWeather() {
		return currentWeather;
	}

	public void setCurrentWeather(CurrentWeather currentWeather) {
		this.currentWeather = currentWeather;
	}

	public List<DailyForecasts> getDailyForecasts() {
		return dailyForecasts;
	}

	public void setDailyForecasts(List<DailyForecasts> dailyForecasts) {
		this.dailyForecasts = dailyForecasts;
	}

	public List<HourlyForecasts> getHourlyForecasts() {
		return hourlyForecasts;
	}

	public void setHourlyForecasts(List<HourlyForecasts> hourlyForecasts) {
		this.hourlyForecasts = hourlyForecasts;
	}
	
	public Calendar getUpdateTime(){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(this.publishTimeStamp);
		return c;
	}

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }
}
