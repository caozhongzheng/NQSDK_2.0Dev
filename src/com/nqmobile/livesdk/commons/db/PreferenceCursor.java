package com.nqmobile.livesdk.commons.db;

import android.database.AbstractCursor;
import android.os.Bundle;

public class PreferenceCursor extends AbstractCursor {
	private Bundle mExtras;
	private String[] mColumnNames;

	public PreferenceCursor(Bundle extras){
		mExtras = extras;
		mColumnNames = new String[]{DataProvider.KEY_PREF_KEY};
	}
	
	@Override
	public Bundle getExtras() {
        return mExtras;
    }

	@Override
	public int getCount() {
		return 1;
	}

	@Override
	public String[] getColumnNames() {
		return mColumnNames;
	}

	@Override
	public String getString(int column) {
		return null;
	}

	@Override
	public short getShort(int column) {
		return 0;
	}

	@Override
	public int getInt(int column) {
		return 0;
	}

	@Override
	public long getLong(int column) {
		return 0;
	}

	@Override
	public float getFloat(int column) {
		return 0;
	}

	@Override
	public double getDouble(int column) {
		return 0;
	}

	@Override
	public boolean isNull(int column) {
		return false;
	}

}
