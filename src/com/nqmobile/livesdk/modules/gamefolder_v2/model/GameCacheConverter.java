package com.nqmobile.livesdk.modules.gamefolder_v2.model;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.nqmobile.livesdk.modules.gamefolder_v2.table.GameCacheTable;
import com.nqmobile.livesdk.utils.StringUtil;

public class GameCacheConverter {
	/**
	 * 将cursor对象转换GameCache
	 */
	public static GameCache cursorToApp(Cursor cursor) {
		GameCache app = new GameCache();
		app.setStrId(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_ID))));
		app.setIntSourceType(cursor.getInt(cursor.getColumnIndex(GameCacheTable.APP_SOURCE_TYPE)));
		app.setStrName(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_NAME))));
		app.setStrPackageName(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_PACKAGENAME))));
		app.setStrDescription(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_DESCRIPTION))));
		app.setStrDevelopers(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_DEVELOPERS))));
		app.setStrCategory1(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_CATEGORY1))));
		app.setStrCategory2(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_CATEGORY2))));
		app.setStrVersion(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_VERSION))));
		app.setFloatRate(cursor.getFloat(cursor.getColumnIndex(GameCacheTable.APP_RATE)));
		app.setLongDownloadCount(cursor.getLong(cursor.getColumnIndex(GameCacheTable.APP_DOWNLOAD_COUNT)));
		app.setLongSize(cursor.getLong(cursor.getColumnIndex(GameCacheTable.APP_SIZE)));
		app.setStrIconUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_ICON_URL))));
		app.setStrIconPath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_ICON_PATH))));
		app.setStrImageUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_IMAGE_URL))));
		app.setStrImagePath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_IMAGE_PATH))));
		String previewUrl = cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_PREVIEW_URL));
		if (!TextUtils.isEmpty(previewUrl)) {
			String[] s = previewUrl.split(";");
			List<String> list = new ArrayList<String>();
			for (int i = 0; i < s.length; i++) {
				list.add(s[i]);
			}
			app.setArrPreviewUrl(list);
		}
		String previewPath = cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_PREVIEW_PATH));
		if (!TextUtils.isEmpty(previewPath)) {
			String[] s = previewPath.split(";");
			List<String> listPath = new ArrayList<String>();
			for (int i = 0; i < s.length; i++) {
				listPath.add(s[i]);
			}
			app.setArrPreviewPath(listPath);
		}
		app.setStrAppUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_URL))));
		app.setStrAppPath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_PATH))));
		app.setIntClickActionType(cursor.getInt(cursor.getColumnIndex(GameCacheTable.APP_CLICK_ACTION_TYPE)));
		app.setIntDownloadActionType(cursor.getInt(cursor.getColumnIndex(GameCacheTable.APP_DOWNLOAD_ACTION_TYPE)));
		app.setLongUpdateTime(cursor.getLong(cursor.getColumnIndex(GameCacheTable.APP_UPDATETIME)));
		app.setLongLocalTime(cursor.getLong(cursor.getColumnIndex(GameCacheTable.APP_LOCALTIME)));
		app.setRewardPoints(cursor.getInt(cursor.getColumnIndex(GameCacheTable.APP_REWARDPOINTS)));
		app.setTrackId(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_TRACKID)));
		
		// new params
		app.setStrShortIntro(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_SHORT_INTRO))));
		app.setStrAudioUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_AUDIO_URL))));
		app.setStrAudioPath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_AUDIO_PATH))));
		app.setStrVideoUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_VIDEO_URL))));
		app.setStrVideoPath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(GameCacheTable.APP_VIDEO_PATH))));
		app.setIntEffect(cursor.getInt(cursor.getColumnIndex(GameCacheTable.APP_EFFECT)));
		app.setGameEnable(cursor.getInt(cursor.getColumnIndex(GameCacheTable.APP_GAME_ENABLE)));
		app.setShowTime(cursor.getLong(cursor.getColumnIndex(GameCacheTable.APP_SHOW_TIME)));
		app.setShowCount(cursor.getInt(cursor.getColumnIndex(GameCacheTable.APP_SHOW_COUNT)));
		app.setPreloadFail(cursor.getInt(cursor.getColumnIndex(GameCacheTable.APP_PRELOAD_FAIL)));

		return app;
	}
	
	/**
     * 将GameCache对象转换层数据库操作的ContentValues对象
     */
    public static ContentValues appToContentValues(GameCache app) {
        ContentValues values = null;
        if (app != null) {
            values = new ContentValues();
            values.put(GameCacheTable.APP_ID, app.getStrId());
            values.put(GameCacheTable.APP_SOURCE_TYPE, app.getIntSourceType());
            values.put(GameCacheTable.APP_COLUMN, 2);//2：游戏文件夹
            values.put(GameCacheTable.APP_TYPE, app.getType());
            values.put(GameCacheTable.APP_CATEGORY1, app.getStrCategory1());
            values.put(GameCacheTable.APP_CATEGORY2, app.getStrCategory2());
            values.put(GameCacheTable.APP_NAME, app.getStrName());
            values.put(GameCacheTable.APP_DESCRIPTION, app.getStrDescription());
            values.put(GameCacheTable.APP_DEVELOPERS, app.getStrDevelopers());
            values.put(GameCacheTable.APP_RATE, app.getFloatRate());
            values.put(GameCacheTable.APP_VERSION, app.getStrVersion());
            values.put(GameCacheTable.APP_SIZE, app.getLongSize());
            values.put(GameCacheTable.APP_DOWNLOAD_COUNT,
                    app.getLongDownloadCount());
            values.put(GameCacheTable.APP_PACKAGENAME, app.getStrPackageName());
            values.put(GameCacheTable.APP_ICON_URL, app.getStrIconUrl());
            values.put(GameCacheTable.APP_IMAGE_URL, app.getStrImageUrl());
            // 预览图网址
            StringBuilder previewUrl = new StringBuilder();
            List<String> previewUrls = app.getArrPreviewUrl();
            if (previewUrls != null && previewUrls.size() > 0) {
                for (int j = 0; j < previewUrls.size(); j++) {
                    previewUrl.append(previewUrls.get(j)).append(";");
                }
            }
            if (previewUrl.length() > 1) {
                values.put(GameCacheTable.APP_PREVIEW_URL,
                        previewUrl.substring(0, previewUrl.length() - 1));
            } else {
                values.put(GameCacheTable.APP_PREVIEW_URL, "");
            }
            values.put(GameCacheTable.APP_URL, app.getStrAppUrl());
            values.put(GameCacheTable.APP_CLICK_ACTION_TYPE,
                    app.getIntClickActionType());
            values.put(GameCacheTable.APP_DOWNLOAD_ACTION_TYPE,
                    app.getIntDownloadActionType());
            values.put(GameCacheTable.APP_ICON_PATH, app.getStrIconPath());
            values.put(GameCacheTable.APP_IMAGE_PATH, app.getStrImagePath());
            // 预览图本地路径
            StringBuilder previewPath = new StringBuilder();
            List<String> previewPaths = app.getArrPreviewPath();
            if (previewPaths != null && previewPaths.size() > 0) {
                for (int j = 0; j < previewPaths.size(); j++) {
                    previewPath.append(previewPaths.get(j)).append(";");
                }
            }
            if (previewPath.length() > 1) {
                values.put(GameCacheTable.APP_PREVIEW_PATH,
                        previewPath.substring(0, previewPath.length() - 1));
            } else {
                values.put(GameCacheTable.APP_PREVIEW_PATH, "");
            }
            values.put(GameCacheTable.APP_PATH, app.getStrAppPath());
            values.put(GameCacheTable.APP_UPDATETIME, app.getLongUpdateTime());
            values.put(GameCacheTable.APP_LOCALTIME, app.getLongLocalTime());
            values.put(GameCacheTable.APP_REWARDPOINTS, app.getRewardPoints());
            values.put(GameCacheTable.APP_TRACKID, app.getTrackId());
            // *****************************************************
            values.put(GameCacheTable.APP_SHORT_INTRO, app.getStrShortIntro());
            values.put(GameCacheTable.APP_AUDIO_URL, app.getStrAudioUrl());
            values.put(GameCacheTable.APP_AUDIO_PATH, app.getStrAudioPath());
            values.put(GameCacheTable.APP_VIDEO_URL, app.getStrVideoUrl());
            values.put(GameCacheTable.APP_VIDEO_PATH, app.getStrVideoPath());
            values.put(GameCacheTable.APP_EFFECT, app.getIntEffect());
            
            values.put(GameCacheTable.APP_GAME_ENABLE, app.getGameEnable());
            values.put(GameCacheTable.APP_SHOW_TIME, app.getShowTime());
            values.put(GameCacheTable.APP_SHOW_COUNT, app.getShowCount());
            values.put(GameCacheTable.APP_PRELOAD_FAIL, app.getPreloadFail());
        }
        return values;
    }
    
}
