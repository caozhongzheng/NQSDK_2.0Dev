package com.nqmobile.livesdk.commons.db;

import android.database.sqlite.SQLiteDatabase;

public interface IDataTable {
	public String getName();
	public int getVersion();
	public void create(SQLiteDatabase db);
	public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion);
}
