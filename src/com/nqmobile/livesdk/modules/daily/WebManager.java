package com.nqmobile.livesdk.modules.daily;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.nqmobile.livesdk.modules.daily.table.WebCacheTable;
import com.nqmobile.livesdk.utils.StringUtil;

/**
 * 业务类
 *
 * @author caozhongzheng
 * @time 2014/5/20 11:00:39
 */
public class WebManager {
    private Context mContext;
    private static WebManager mInstance;
    public static final long CACHE_MAX_TIME = 24L * 60 * 60 * 1000;//24小时
    
    public WebManager(Context context) {
        super();
        this.mContext = context;
    }

    public synchronized static WebManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new WebManager(context);
        }
        return mInstance;
    }

    /**
     * 将cursor对象转换Web
     *
     * @param cursor
     * @return
     */
    public Web cursorToWeb(Cursor cursor) {
        Web web = new Web();
        web.setStrId(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(WebCacheTable.WEB_ID))));
        web.setStrName(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(WebCacheTable.WEB_NAME))));
        web.setIntJumpType(cursor.getInt(cursor.getColumnIndex(WebCacheTable.WEB_JUMP_TYPE)));
        web.setStrLinkUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(WebCacheTable.WEB_URL))));
        web.setStrPreviewUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(WebCacheTable.WEB_PREVIEW_URL))));
        web.setStrIconUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(WebCacheTable.WEB_ICON_URL))));
        web.setStrDailyIconUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(WebCacheTable.WEB_DAILYICON))));
        return web;
    }

    /**
     * 将web对象转换层数据库操作的ContentValues对象
     *
     * @param web
     * @return
     */
    public ContentValues webToContentValues(Web web) {
        ContentValues values = null;
        if (web != null) {
            values = new ContentValues();
            values.put(WebCacheTable.WEB_ID, web.getStrId());
            values.put(WebCacheTable.WEB_NAME, web.getStrName());
            values.put(WebCacheTable.WEB_JUMP_TYPE, web.getIntJumpType());
            values.put(WebCacheTable.WEB_URL, web.getStrLinkUrl());
            values.put(WebCacheTable.WEB_PREVIEW_URL,web.getStrPreviewUrl());
            values.put(WebCacheTable.WEB_ICON_URL, web.getStrIconUrl());
            values.put(WebCacheTable.WEB_DAILYICON,web.getStrDailyIconUrl());
        }
        return values;
    }

}