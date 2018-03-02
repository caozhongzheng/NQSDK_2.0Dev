package com.nqmobile.livesdk.modules.weather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.modules.weather.model.City;
import com.nqmobile.livesdk.utils.MResource;

public class HotCities {
	private static final ILogger NqLog = LoggerFactory.getLogger(WeatherModule.MODULE_NAME);
	private static Map<String, List<City>> sHotCities;
	
	public synchronized static void initCity(Context context){
		if (sHotCities != null){
			return;
		}
		
		sHotCities = new HashMap<String, List<City>>();
		BufferedReader br = null;
		try {
			InputStream in = context.getResources().openRawResource(
					MResource.getIdByName(context, "raw", "citylist"));
			br = new BufferedReader(new InputStreamReader(in));
			String str = null;
			
			while((str = br.readLine()) != null){
				String[] cityLine = str.split(",");
				City city = new City();
				city.setCityName(cityLine[0]);
				city.setCityId(cityLine[2]);
				String language = cityLine[1];
				if (!sHotCities.containsKey(language)){
					sHotCities.put(language, new ArrayList<City>());
				}
				sHotCities.get(language).add(city);
			}
		} catch (IOException e) {
			NqLog.e(e);
		} finally {
			if (br != null){
				try {
					br.close();
				} catch (IOException e) {
					NqLog.e(e);
				}
			}
		}
	}
	
	public static List<City> queryHotCities(Context context, String language){
		if (sHotCities == null){
			initCity(context);
		}
		
		return sHotCities.get(language);
	}

}
