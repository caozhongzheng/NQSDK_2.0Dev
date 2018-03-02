package com.nqmobile.livesdk.modules.stat.network;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TLogUploadRequest;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.concurrent.Priority;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.stat.StatModule;
import com.nqmobile.livesdk.modules.stat.StatPreference;
import com.nqmobile.livesdk.modules.stat.table.StoreStatTable;
import com.nqmobile.livesdk.utils.GZipUtils;

public class UploadLogProtocol extends AbsLauncherProtocol{
	private static final ILogger NqLog = LoggerFactory.getLogger(StatModule.MODULE_NAME);
	
	private static final int MAX_SZIE = 300; //一次上传最大条数
	
	private ArrayList<String> dataList;

    private Context mContext;
	
	private static final String LOG_TYPES = "store_action_zip";

	public UploadLogProtocol() {
        mContext = ApplicationContext.getContext();
	}

	@Override
	protected void process() {
        NqLog.i("UploadLogProtocol process");
		try {
            TLauncherService.Iface client = TLauncherServiceClientFactory.getClient(getThriftProtocol());
            while (getStatData()) {
                StringBuilder builder = new StringBuilder();
                for (String s : dataList) {
                    builder.append(s);
                    builder.append("\n");
                }

                //upload to server
                TLogUploadRequest req = new TLogUploadRequest();
                req.logType = LOG_TYPES;
                req.byteStream = ByteBuffer.wrap(GZipUtils.compress(builder.toString().getBytes()));

                client.uploadLog(getUserInfo(), req);

                //delete the db;
                mContext.getContentResolver().delete(StoreStatTable.TABLE_URI,
                        StoreStatTable.STORE_STAT_ISUPLOAD + " = 1", null);
            }

            NqLog.i("upload log succ!");
            EventBus.getDefault().post(new UploadLogSuccessEvent());
		}catch(Exception e) {
            e.printStackTrace(); onError();
        }finally {
            StatPreference.getInstance().setLastUploadLog(System.currentTimeMillis());
        }
    }
	@Override
	protected void onError() {
		EventBus.getDefault().post(new UploadLogFailEvent());
	}

	private boolean getStatData() {
		dataList = new ArrayList<String>();
		ArrayList<Long> mIds = new ArrayList<Long>();
		String selection = "limit " + 0 + "," + MAX_SZIE;

        Cursor storeCursor = null;
        try{
            storeCursor = mContext.getContentResolver().query(StoreStatTable.TABLE_URI, null, null, null, "_id ASC " + selection);
            while(storeCursor != null && storeCursor.moveToNext()) {
                String desc = storeCursor.getString(storeCursor.getColumnIndex(StoreStatTable.STORE_STAT_DESC));
                String resourceId = storeCursor.getString(storeCursor.getColumnIndex(StoreStatTable.STORE_STAT_RESOURCEID));
                String scene = storeCursor.getString(storeCursor.getColumnIndex(StoreStatTable.STORE_STAT_SCNE));
                long time = storeCursor.getLong(storeCursor.getColumnIndex(StoreStatTable.STORE_STAT_TIME));
                dataList.add(desc + "|" + resourceId  + "|" + scene + "|" + time);
                mIds.add(storeCursor.getLong(storeCursor.getColumnIndex(StoreStatTable.STORE_STAT_ID)));
            }

            ContentValues storevalue = new ContentValues();
            storevalue.put(StoreStatTable.STORE_STAT_ISUPLOAD, 1);
            mContext.getContentResolver().update(StoreStatTable.TABLE_URI, storevalue, StoreStatTable.STORE_STAT_ID + " BETWEEN ? AND ?",
                new String[] { String.valueOf(mIds.get(0)),String.valueOf(mIds.get(mIds.size() - 1)) });
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(storeCursor != null){
                storeCursor.close();
            }
        }

        NqLog.i("getStatData dataList.size="+dataList.size());

		return (dataList.size()) > 0;
	}
	
	@Override
    public int getPriority() {
        return Priority.PRIORITY_MINOR;
    }

    @Override
    protected int getProtocolId() {
        return 0x15;
    }

    public static class UploadLogSuccessEvent extends AbsProtocolEvent{
    }

    public static class UploadLogFailEvent extends AbsProtocolEvent{

    }

	
}
