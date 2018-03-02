package com.nqmobile.livesdk.modules.points.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;
import com.nqmobile.livesdk.utils.DatabaseUtil;

/**
 * Created by Rainbow on 2014/11/18.
 */
public class PointResourceTable extends AbsDataTable{

    public static final String TABLE_NAME = "points_resource";
    public static final int TABLE_VERSION = 2;
    public static final String DATA_AUTHORITY = DataProvider.DATA_AUTHORITY;
    public static final Uri TABLE_URI = Uri.parse("content://"+DATA_AUTHORITY+"/" + TABLE_NAME);

    public static final String _ID = "_id";
    public static final String POINT_APP_ID = "appId";
    public static final String POINT_APP_SOURCE_TYPE = "sourceType";
    public static final String POINT_BANNER_PLATE= "bannerPlate";
    public static final String POINT_APP_COLUMN = "column";
    public static final String POINT_APP_TYPE = "type";
    public static final String POINT_APP_CATEGORY1 = "Category1";
    public static final String POINT_APP_CATEGORY2 = "Category2";
    public static final String POINT_APP_NAME = "name";
    public static final String POINT_APP_DESCRIPTION = "description";
    public static final String POINT_APP_DEVELOPERS = "developers";
    public static final String POINT_APP_PRICE = "price";
    public static final String POINT_APP_POINT = "point";
    public static final String POINT_APP_RATE = "rate";
    public static final String POINT_APP_SIZE = "size";
    public static final String POINT_APP_ICON_URL = "iconUrl";
    public static final String POINT_APP_IMAGE_URL = "imageUrl";
    public static final String POINT_APP_URL = "appUrl";
    public static final String POINT_APP_GP_URL = "appGPUrl";
    public static final String POINT_APP_CLICK_ACTION_TYPE = "clickActionType";
    public static final String POINT_APP_DOWNLOAD_ACTION_TYPE = "downloadActionType";
    public static final String POINT_APP_PREVIEW_URL = "previewUrl";
    public static final String POINT_APP_ICON_PATH = "iconPath";
    public static final String POINT_APP_IMAGE_PATH = "imagePath";
    public static final String POINT_APP_PATH = "appPath";
    public static final String POINT_APP_PREVIEW_PATH = "previewPath";
    public static final String POINT_APP_PACKAGENAME = "packageName";
    public static final String POINT_APP_VERSION = "version";
    public static final String POINT_APP_DOWNLOAD_COUNT = "downloadCount";
    public static final String POINT_APP_UPDATETIME = "updateTime";
    public static final String POINT_APP_LOCALTIME = "localTime";
    public static final String POINT_APP_TRACKID = "trackId";
    public static final String POINT_APP_POINTS = "rewardpoints";
    public static final String POINT_APP_TIME = "time"; //下发时间
    public static final String POINT_APP_DOWNLOAD = "download"; //是否点击过下载
    public static final String POINT_APP_DOWNLOADID = "downloadId";
    public static final String POINT_APP_REWARD_STATE = "reward_state"; //获取积分状态 0未获取 1 已获取 2获取失败
    public static final String POINT_APP_FROM_THEME_ID = "themeid"; //对应的主题：从哪个主题过来的

    @Override
    public String getName() {
        return TABLE_NAME;
    }

    @Override
    public int getVersion() {
        return TABLE_VERSION;
    }

    @Override
    public void create(SQLiteDatabase db) {
        try{
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                            _ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
                            POINT_APP_ID +" VARCHAR(20)," +
                            POINT_APP_SOURCE_TYPE + " INTEGER default 0," +
                            POINT_BANNER_PLATE + " INTEGER default 0," +
                            POINT_APP_COLUMN + " INTEGER default 0," +
                            POINT_APP_TYPE +" VARCHAR(20)," +
                            POINT_APP_CATEGORY1 +" VARCHAR(20)," +
                            POINT_APP_CATEGORY2 +" VARCHAR(20)," +
                            POINT_APP_NAME +" VARCHAR(20)," +
                            POINT_APP_DESCRIPTION +" VARCHAR(50)," +
                            POINT_APP_DEVELOPERS +" VARCHAR(20)," +
                            POINT_APP_PRICE +" VARCHAR(20)," +
                            POINT_APP_POINT +" VARCHAR(20)," +
                            POINT_APP_RATE +" VARCHAR(20)," +
                            POINT_APP_DOWNLOAD_COUNT +" VARCHAR(20)," +
                            POINT_APP_SIZE +" VARCHAR(20)," +
                            POINT_APP_ICON_URL +" VARCHAR(20)," +
                            POINT_APP_IMAGE_URL +" VARCHAR(20)," +
                            POINT_APP_URL +" VARCHAR(20)," +
                            POINT_APP_GP_URL +" VARCHAR(20)," +
                            POINT_APP_CLICK_ACTION_TYPE + " INTEGER default 0," +
                            POINT_APP_DOWNLOAD_ACTION_TYPE + " INTEGER default 0," +
                            POINT_APP_PREVIEW_URL +" VARCHAR(20)," +
                            POINT_APP_ICON_PATH +" VARCHAR(20)," +
                            POINT_APP_IMAGE_PATH +" VARCHAR(20)," +
                            POINT_APP_PATH +" VARCHAR(20)," +
                            POINT_APP_PREVIEW_PATH +" VARCHAR(20)," +
                            POINT_APP_PACKAGENAME +" VARCHAR(20)," +
                            POINT_APP_VERSION +" VARCHAR(20)," +
                            POINT_APP_UPDATETIME +" INTEGER default 0," +
                            POINT_APP_LOCALTIME +" INTEGER default 0," +
                            POINT_APP_TRACKID + " VARCHAR(20)," +
                            POINT_APP_POINTS + " INTEGER default 0," +
                            POINT_APP_TIME + " INTEGER default 0," +
                            POINT_APP_DOWNLOAD + " INTEGER default 0," +
                            POINT_APP_DOWNLOADID + " INTEGER default 0," +
                            POINT_APP_REWARD_STATE + " INTEGER default 0, " +
                            POINT_APP_FROM_THEME_ID + " VARCHAR(50)" +
                            ")"
            );
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	boolean oldCol = DatabaseUtil.checkColumnExists(db, TABLE_NAME, "donwload");
        if(oldCol){
            db.execSQL("ALTER TABLE " + TABLE_NAME + " RENAME TO " + TABLE_NAME + "_TMP");
            create(db);
            db.execSQL("INSERT INTO " + TABLE_NAME + " SELECT * FROM " + TABLE_NAME + "_TMP");
            db.execSQL("DROP TABLE " + TABLE_NAME + "_TMP");
        }
        
    }
}
