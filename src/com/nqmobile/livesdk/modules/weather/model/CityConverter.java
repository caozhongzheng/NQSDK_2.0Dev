package com.nqmobile.livesdk.modules.weather.model;

import java.util.ArrayList;
import java.util.List;

import com.nq.interfaces.weather.TCity;

public class CityConverter {
	public static List<City> convert(List<TCity> cities){
		if (cities == null || cities.isEmpty()){
			return null;
		}
		
		List<City> result = new ArrayList<City>(cities.size());
		for(TCity city : cities){
			City c = new City(city);
			result.add(c);
		}
		
		return result;		
	}
}
