package com.nqmobile.livesdk.modules.association.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;
import com.nqmobile.livesdk.utils.DatabaseUtil;

public class AssociationRecommendTable extends AbsDataTable {
    // ===========================================================
    // Constants
    // ===========================================================	
	public static final String TABLE_NAME = "association_recommend_cache";
	public static final int TABLE_VERSION = 5;
	public static final String DATA_AUTHORITY = DataProvider.DATA_AUTHORITY;
	public static final Uri TABLE_URI = Uri.parse("content://"+DATA_AUTHORITY+"/" + TABLE_NAME);
	
	public static final String _ID = "_id";	
	
	public static final String ASSOCIATION_RECOMMENDED_RESID =  "resouceId";//被推荐的应用资源id
	public static final String ASSOCIATION_RECOMMENDED_NAME =  "name";//被推荐的应用资源名称
	public static final String ASSOCIATION_RECOMMENDED_PACKAGENAME =  "recommPackageName";//被推荐的应用资源名称
	public static final String ASSOCIATION_LASTSHOWTIME =  "lastShowTime";//上次展示时间
	public static final String ASSOCIATION_SHOWTIMES =  "showTimes";//已经展示次数
	public static final String ASSOCIATION_RECOMMENDED_ICONURL =  "IconUrl";//被推荐的应用iconUrl
	public static final String ASSOCIATION_RECOMMENDED_ICON =  "IconLocalPath";//被推荐的应用icon本地路径
	public static final String ASSOCIATION_RECOMMENDED_LINKURL =  "linkUrl";//被推荐的应用下载地址
	public static final String ASSOCIATION_RECOMMENDED_DOWNLOADTYPE =  "downloadType";//被推荐的应用下载类型
	public static final String ASSOCIATION_RECOMMENDED_EFFECTID =  "effectId";//被推荐的应用特效id
	public static final String ASSOCIATION_RECOMMENDED_DOWNLOADCOUNT =  "downloadCount";//被推荐的应用下载次数
	public static final String ASSOCIATION_RECOMMENDED_SIZE =  "size";//被推荐的应用下载次数
	public static final String ASSOCIATION_RECOMMENDED_SCORE =  "score";//被推荐的应用评分
	public static final String ASSOCIATION_RECOMMENDED_ACTIONCLICKTYPE =  "actionClickType";// 应用资源点击的行为：0打开详情页，1打开GP， 3打开浏览器（外部网站或广告GP下载）， 9无行为 
    // ===========================================================
    // Fields
    // ===========================================================
	
    // ===========================================================
    // Constructors
    // ===========================================================
	
    // ===========================================================
    // Getter & Setter
    // ===========================================================
	@Override
	public String getName() {
		return TABLE_NAME;
	}
	
	@Override
	public int getVersion() {
		return TABLE_VERSION;
	}
	
    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================
	@Override
	public void create(SQLiteDatabase db) {
		try{
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
					_ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +
					ASSOCIATION_RECOMMENDED_RESID +" VARCHAR(200)," +
					ASSOCIATION_RECOMMENDED_NAME +" VARCHAR(200)," +
					ASSOCIATION_RECOMMENDED_PACKAGENAME +" VARCHAR(200)," +
					ASSOCIATION_LASTSHOWTIME +" VARCHAR(200)," +
					ASSOCIATION_SHOWTIMES +" INTEGER default 0," +
					ASSOCIATION_RECOMMENDED_ICONURL +" VARCHAR(200)," +
					ASSOCIATION_RECOMMENDED_ICON +" VARCHAR(200)," +					
					ASSOCIATION_RECOMMENDED_LINKURL +" VARCHAR(200)," +
					ASSOCIATION_RECOMMENDED_DOWNLOADTYPE +" INTEGER default -1," +
					ASSOCIATION_RECOMMENDED_EFFECTID +" INTEGER default -1," +
					ASSOCIATION_RECOMMENDED_DOWNLOADCOUNT + " INTEGER default -1," +
					ASSOCIATION_RECOMMENDED_SIZE + " INTEGER default -1," +
					ASSOCIATION_RECOMMENDED_SCORE + " VARCHAR(20)," +
					ASSOCIATION_RECOMMENDED_ACTIONCLICKTYPE + " INTEGER default -1" +
					")"
					);
		}catch (SQLException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub       	
        alterAssociationRecommendTable(db);
	}
	
	private void alterAssociationRecommendTable(SQLiteDatabase db){
		try{
			boolean result = DatabaseUtil.checkColumnExists(db,TABLE_NAME,ASSOCIATION_RECOMMENDED_SCORE);
			if(!result){
				db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + ASSOCIATION_RECOMMENDED_DOWNLOADCOUNT + " INTEGER default -1");
				db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + ASSOCIATION_RECOMMENDED_SIZE + " INTEGER default -1");
				db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + ASSOCIATION_RECOMMENDED_SCORE + " VARCHAR(20)");
				db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + ASSOCIATION_RECOMMENDED_ACTIONCLICKTYPE + " INTEGER default -1");
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
