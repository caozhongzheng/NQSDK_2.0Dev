package com.nqmobile.livesdk.commons.db;

import android.database.sqlite.SQLiteDatabase;


public class AbsDataTable implements IDataTable {
	public static final String _ID = "_id";

	@Override
	public String getName() {
		return null;
	}

	@Override
	public int getVersion() {
		return 0;
	}

	@Override
	public void create(SQLiteDatabase db) {

	}

	@Override
	public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
