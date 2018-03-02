package com.nqmobile.livesdk.modules.stat.network;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TPackageAction;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.stat.StatModule;
import com.nqmobile.livesdk.modules.stat.table.NewPackageTable;

/**
 * Created by Rainbow on 14-3-11.
 */
public class UploadNewPackageProtocol extends AbsLauncherProtocol{
	private static final ILogger NqLog = LoggerFactory.getLogger(StatModule.MODULE_NAME);

    @Override
    protected int getProtocolId() {
        return 0x20;
    }

    @Override
    protected void process() {
        NqLog.i("UploadNewPackageProtocol process!");
        try {
            TPackageAction action = getNewPackageList();
            if(action != null){
                TLauncherService.Iface client = TLauncherServiceClientFactory.getClient(getThriftProtocol());
                client.uploadPackageAction(getUserInfo(), action);
                ApplicationContext.getContext().getContentResolver().delete(NewPackageTable.TABLE_URI,
                        NewPackageTable.NEW_PACKAGE_ISUPLOAD + " = 1", null);
                NqLog.i("UploadNewPackageProtocol success!");
            }
        }catch(Exception e) {
            e.printStackTrace();onError() ;
        }
    }
	@Override
	protected void onError() {
	}
    private TPackageAction getNewPackageList(){
        List<String> packageList = new ArrayList<String>();
        ArrayList<Long> mIds = new ArrayList<Long>();
        Cursor c = ApplicationContext.getContext().getContentResolver().query(NewPackageTable.TABLE_URI,null,null,null,null);
        while(c!= null && c.moveToNext()){
            packageList.add(c.getString(c.getColumnIndex(NewPackageTable.NEW_PACKAGE_PACKAGENAME)));
            mIds.add(c.getLong(c.getColumnIndex(NewPackageTable._ID)));
        }

        if(c != null){
            c.close();
        }

        if(packageList.size() > 0){
            TPackageAction mPackageAction = new TPackageAction();
            mPackageAction.actionType = 1;
            mPackageAction.setInstalls(packageList);

            ContentValues value = new ContentValues();
            value.put(NewPackageTable.NEW_PACKAGE_ISUPLOAD, 1);
            for (int j = 0; j < mIds.size(); j++) {
                ApplicationContext.getContext().getContentResolver().update(NewPackageTable.TABLE_URI, value, NewPackageTable._ID + " = ?",
                        new String[] { String.valueOf(mIds.get(j)) });
            }
            return mPackageAction;
        }
        return null;
    }


}
