package com.nqmobile.livesdk.modules.gamefolder_v2;

import android.content.Context;
import android.text.format.DateUtils;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.moduleframework.IPolicy;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.NetworkUtils;

public class GameFolderV2Policy implements IPolicy {
    private final static long DEFAULT_GAME_FREQUENCY_WIFI = DateUtils.DAY_IN_MILLIS;
    private final static long DEFAULT_GAME_FREQUENCY_3G = 7 * DateUtils.DAY_IN_MILLIS;
    private final static int DEFAULT_GAME_MAX_DISPLAY_TIMES = 3;
    private static final long DEFAULT_GAME_CACHE_VALID_TIME = 10 * DateUtils.DAY_IN_MILLIS;
    private static final int DEFAULT_MINIMUM_GAME_COUNT = 3;
    
    public static boolean isExceedFrequency() {
        Context context = ApplicationContext.getContext();
        long now = SystemFacadeFactory.getSystem().currentTimeMillis();
        GameFolderV2Preference preference = GameFolderV2Preference.getInstance();
        long lastGetGameAdTime = preference.getLastGetGameAdTime();
        long gameFrequency_wifi = preference.getGameFreqWifiInMinutes() * DateUtils.MINUTE_IN_MILLIS;
        long gameFrequency_3g = preference.getGameFreq3GInMinutes() * DateUtils.MINUTE_IN_MILLIS;
        long gameFrequency = 0L;
        
        if (NetworkUtils.isWifi(context)) {
            if (gameFrequency_wifi > 0){
                gameFrequency = gameFrequency_wifi;
            } else {
                gameFrequency = DEFAULT_GAME_FREQUENCY_WIFI;
            }
        } else {
            if (gameFrequency_3g > 0){
                gameFrequency = gameFrequency_3g;
            } else {
                gameFrequency = DEFAULT_GAME_FREQUENCY_3G;
            }
        }
        
        if (Math.abs(now - lastGetGameAdTime) < gameFrequency) {
            return false;
        } else {
            return true;
        }
    }
    
    public static boolean isGameCacheExpired() {
        long now = SystemFacadeFactory.getSystem().currentTimeMillis();
        GameFolderV2Preference preference = GameFolderV2Preference.getInstance();
        long lastGetGameAdTime = preference.getLastGetGameAdTime();
        long gameCacheValidTime = getGameCacheValidTime();
        
        if (Math.abs(now - lastGetGameAdTime) >= gameCacheValidTime){
            return true;
        } else {
            return false;
        }
    }

    public static int getGameMaxDisplayTimes() {
        GameFolderV2Preference preference = GameFolderV2Preference.getInstance();
        long showNum = preference.getGameShowNum();
        if (showNum > 0) {
            return (int) showNum;
        } else {
            return DEFAULT_GAME_MAX_DISPLAY_TIMES;
        }
    }
    
    private static long getGameCacheValidTime() {
        GameFolderV2Preference preference = GameFolderV2Preference.getInstance();
        long gameCacheValidTime = preference.getGameCacheValidTimeInMinutes()
                * DateUtils.MINUTE_IN_MILLIS;
        if (gameCacheValidTime > 0) {
            return gameCacheValidTime;
        } else {
            return DEFAULT_GAME_CACHE_VALID_TIME;
        }
    }

    public static int getMinimumGameCount() {
        return DEFAULT_MINIMUM_GAME_COUNT;
    }
}
