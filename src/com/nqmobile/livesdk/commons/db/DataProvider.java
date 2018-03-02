package com.nqmobile.livesdk.commons.db;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.commons.init.InitManager;
import com.nqmobile.livesdk.commons.init.InitPreference;
import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.points.PointsPreference;
import com.nqmobile.livesdk.utils.CollectionUtils;

public class DataProvider extends ContentProvider {
	public static final String TABLE_PREF_SERVICE = "PreferenceService";
	private static final int TABLE_PREF_SERVICE_CODE = 10000;

	public static final String KEY_PREF_FILE_NAME = "prefFileName";
	public static final String KEY_PREF_METHOD = "prefMethod";
	public static final String KEY_PREF_KEY = "prefKey";
	public static final String KEY_PREF_VAL = "prefValue";

	private List<IDataTable> mTables = new ArrayList<IDataTable>();

	private static final String DB_NAME = "data.db";
	private static final int DB_VERSION = 7;

	private DbHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	public static final String DATA_AUTHORITY;
	public static final Uri TABLE_PREF_SERVICE_URI;
	
	static {
		DATA_AUTHORITY = ClientInfo.getPackageName() + ".dataprovider";
		TABLE_PREF_SERVICE_URI = Uri.parse("content://" + DATA_AUTHORITY+"/" + TABLE_PREF_SERVICE);
	}
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	public void registerTables() {
		Collection<IModule> modules = ModuleContainer.getInstance().getModules();
		//NqLog.d("DataProviderNew registerTables modules:" + modules);
		for (IModule module : modules) {
			List<IDataTable> tables = module.getTables();
			//NqLog.d("DataProviderNew registerTables module:" + module + " tables:" + tables);
			if (tables != null && !tables.isEmpty()){	
				mTables.addAll(tables);
			}
		}

		int i = 0;
		for (IDataTable table : mTables) {
			String tableName = table.getName();
			sURIMatcher.addURI(DATA_AUTHORITY, tableName, i/*mTables.size() - 1*/);
			i++;
			//NqLog.d("DataProviderNew sURIMatcher:" + DATA_AUTHORITY + " " + tableName + " " + (mTables.size() - 1) + " i:" + i);
		}
		
		sURIMatcher.addURI(DATA_AUTHORITY, TABLE_PREF_SERVICE, TABLE_PREF_SERVICE_CODE);
	}
	
	@Override
	public boolean onCreate() {
		//NOTE：下面这句必须是第一行，首先初始化ApplicationContext
		ApplicationContext.setContext(getContext());
		
		NqLog.d("DataProviderNew onCreate " + getContext());
		new InitManager(getContext()).registerModules();
		
		registerTables();
		
		if(mDbHelper == null) {
			mDbHelper = new DbHelper(getContext());
		}
		if(mDb == null) {
			mDb = mDbHelper.getWritableDatabase();
		}

		return true;
	}
	
	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			ContentProviderResult[] results = super.applyBatch(operations);
			db.setTransactionSuccessful();
			return results;
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor c = null;
		int table = sURIMatcher.match(uri);
		if (TABLE_PREF_SERVICE_CODE == table){
			String preferenceFile = projection[0];
			String method = projection[1];
			String key = projection[2];
			Bundle extras = new Bundle();
			extras.putString(KEY_PREF_KEY, key);
			Bundle result = call(method, preferenceFile, extras);
			return new PreferenceCursor(result);
		}
        try{
			if (table < mTables.size()){
				c = mDb.query(mTables.get(table).getName(), projection,
                        selection, selectionArgs, null, null, sortOrder);
			} else {
				throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
		 }
		return c;
	}
	
	@Override
	public Bundle call(String method, String arg, Bundle extras) {
		final String key = extras.getString(KEY_PREF_KEY);
		if (key == null) {
			return null;
		}
		Integer lock = key.hashCode();
		synchronized (lock) {
			String xmlFile = arg;
			SharedPreferences prefrce = getContext().getSharedPreferences(xmlFile, Context.MODE_PRIVATE);;
			if (method.startsWith("set")) {
				Editor editor = prefrce.edit();
				boolean validateMethod = false;
				if (method.equals("setBooleanValue")) {
					editor.putBoolean(key, extras.getBoolean(KEY_PREF_VAL));
					validateMethod = true;
				} else if (method.equals("setStringValue")) {
					editor.putString(key, extras.getString(KEY_PREF_VAL));
					validateMethod = true;
				} else if (method.equals("setLongValue")) {
					editor.putLong(key, extras.getLong(KEY_PREF_VAL));
					validateMethod = true;
				} else if (method.equals("setIntValue")) {
					editor.putInt(key, extras.getInt(KEY_PREF_VAL));
					validateMethod = true;
				}
				if (validateMethod) {
					editor.commit();
				}else{
					NqLog.e("DataProvider.call 无效的set方法:" + method);
				}
				NqLog.d("DataProvider.call "+this
						+ ":" + method + ", arg=" + arg + ",(" + extras + "), pid="
						+ Process.myPid() + ", threadId="
						+ Thread.currentThread().getId());
			} else if(method.startsWith("get")){
				Bundle rs = new Bundle();
				boolean validateMethod = false;
				Object vFromXml = null;
				if (method.equals("getBooleanValue")) {
					validateMethod = true;
					boolean v = prefrce.getBoolean(key, false);
					vFromXml = v;
					rs.putBoolean(key, v);
				} else if (method.equals("getStringValue")) {
					validateMethod = true;
					String v = prefrce.getString(key, "");
					vFromXml = v;
					rs.putString(key, v);
				} else if (method.equals("getLongValue")) {
					validateMethod = true;
					long v = prefrce.getLong(key, 0);
					vFromXml = v;
					rs.putLong(key, v);
				} else if (method.equals("getIntValue")) {
					validateMethod = true;
					int v = prefrce.getInt(key, 0);
					vFromXml = v;
					rs.putInt(key, v);
				}
				if(validateMethod){
					NqLog.d("DataProvider.call "+this
							+ ":" + method + ", arg=" + arg + ", ("+key + " = "+vFromXml
									+ "), pid="+Process.myPid()+", threadId="+Thread.currentThread().getId() );
				}else{
					NqLog.e("DataProvider.call无效的get方法:" + method);
				}
				return rs;
			} else if ("isAppInitDone".equals(method)) {
				File initFile = new File(getContext().getCacheDir(),"init");
				boolean isAppInitDone  = initFile.exists();
				if (!isAppInitDone) {
					try {
						initFile.createNewFile();
					} catch (IOException e) {
						NqLog.e("DataProvider.call",e);
					}
					Editor editor = prefrce.edit();
//					editor.putBoolean(InitPreference.KEY_APP_INIT_DONE, true);
//					editor.putBoolean(InitPreference.KEY_INIT_FINISH, true);
					
					editor.putBoolean(PointsPreference.KEY_SHOW_POINT_TIP, true);
					
					editor.commit();
				}
				{
					boolean oldInitDone = prefrce.getBoolean(InitPreference.KEY_APP_INIT_DONE, false)
							|| prefrce.getBoolean(InitPreference.KEY_INIT_FINISH, false);
					if(!oldInitDone){
						Editor editor = prefrce.edit();
						editor.putBoolean(InitPreference.KEY_APP_INIT_DONE, true);
						editor.putBoolean(InitPreference.KEY_INIT_FINISH, true);
						editor.commit();
					}
					isAppInitDone = oldInitDone;
					NqLog.d("DataProvider.call isAppInitDone "+this
							+ ":"+ " pid="
							+ Process.myPid() + ", threadId="
							+ Thread.currentThread().getId()+", isAppInitDone? "+isAppInitDone+", oldInitDone="+oldInitDone
							);
				}
				
				Bundle rs = new Bundle();
				rs.putBoolean("isAppInitDone", isAppInitDone);
				return rs;
			}
			else {
				NqLog.e("DataProvider.call无效的方法:" + method);
			}
			return null;
		}
	}
	
	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int table = sURIMatcher.match(uri);
		//NqLog.d("insert uri:" + uri + " table:" + table);
		Uri resultUri = null;
		try{
			if (table < mTables.size()){
				long id = mDb.insert(mTables.get(table).getName(), "", values);
                resultUri = ContentUris.withAppendedId(uri, id);
			} else {
				throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }catch(SQLiteException e){
			e.printStackTrace();
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return resultUri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int result = 0;
		int table = sURIMatcher.match(uri);
		try{
			if (CollectionUtils.isNotEmpty(mTables) && table < mTables.size()){
				result = mDb.delete(mTables.get(table).getName(), selection, selectionArgs);
			} else {
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int result = 0;
		int table = sURIMatcher.match(uri);
		if (TABLE_PREF_SERVICE_CODE == table){
			String prefFileName = values.getAsString(KEY_PREF_FILE_NAME);
			String method = values.getAsString(KEY_PREF_METHOD);
			String key = values.getAsString(KEY_PREF_KEY);
			Object value = values.get(KEY_PREF_VAL);
			Bundle extras = new Bundle();
			extras.putString(KEY_PREF_KEY, key);
			if (value instanceof Serializable){
				extras.putSerializable(KEY_PREF_VAL, (Serializable)value);
			}
			call(method, prefFileName, extras);
			return 1;
		}
		try{
			if (table < mTables.size()){
				result = mDb.update(mTables.get(table).getName(), values, selection, selectionArgs);
			} else {
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}
	
	private class DbHelper extends SQLiteOpenHelper{

		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			for(IDataTable table : mTables){
				table.create(db);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            for(IDataTable table : mTables){
                table.create(db);
            }
			
			new InitManager(getContext()).upgradeDb(db, oldVersion, newVersion);
		}
		
		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        // do nothing
	    }
	}
}
