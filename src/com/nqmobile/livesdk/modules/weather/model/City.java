package com.nqmobile.livesdk.modules.weather.model;

import com.nq.interfaces.weather.TCity;

public class City {

	private String cityId;// 城市ID
	private String cityName;// 城市名称（本地语言）
	private String adminareasId;// 行政区划ID
	private String adminareas;// 行政区划名称（本地语言）
	private String countryId;// 国家ID
	private String country;// 国家名称（本地语言）
	private String timeZone;// 时区
	private String mscCityId; //城市区号代码
    private String mscCityName; //城市区号名称
	
	public City(){
		
	}
	
	public City(TCity city){	
		this.cityId = city.cityId;
		this.cityName = city.cityName;
		this.adminareasId = city.adminareasId;
		this.adminareas = city.adminareas;
		this.countryId = city.countryId;
		this.country = city.country;
		this.timeZone = city.timeZone;
		this.mscCityId = city.mscCityId;
        this.mscCityName = city.mscCityName;
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getAdminareasId() {
		return adminareasId;
	}

	public void setAdminareasId(String adminareasId) {
		this.adminareasId = adminareasId;
	}

	public String getAdminareas() {
		return adminareas;
	}

	public void setAdminareas(String adminareas) {
		this.adminareas = adminareas;
	}

	public String getCountryId() {
		return countryId;
	}

	public void setCountryId(String countryId) {
		this.countryId = countryId;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

	public String getMscCityId() {
		return mscCityId;
	}

	public void setMscCityId(String mscCityId) {
		this.mscCityId = mscCityId;
	}

    public String getMscCityName() {
        return mscCityName;
    }

    public void setMscCityName(String mscCityName) {
        this.mscCityName = mscCityName;
    }
}
