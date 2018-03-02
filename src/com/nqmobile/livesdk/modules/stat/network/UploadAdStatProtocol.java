package com.nqmobile.livesdk.modules.stat.network;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TResourceAction;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.stat.StatModule;
import com.nqmobile.livesdk.modules.stat.UploadAdStatTag;
import com.nqmobile.livesdk.modules.stat.table.AdStatTable;
import com.nqmobile.livesdk.modules.stat.table.StoreStatTable;

import java.util.ArrayList;

public class UploadAdStatProtocol extends AbsLauncherProtocol{
	private static final ILogger NqLog = LoggerFactory.getLogger(StatModule.MODULE_NAME);

	private boolean mUploadFromDB;

	private ArrayList<TResourceAction> dataList = new ArrayList<TResourceAction>();
	private Context mContext;

	private static final String[] UPLOAD_IMAGE = {
			"1102", "1103", "1402", "1505", "1602", "1714",
			"1902", "2001", "2302" ,"2402" ,"2504", "2506",
			"2605", "2804", "2806", "2907" ,"1506"
	};
	
	public UploadAdStatProtocol(UploadAdStatTag tag) {
		setTag(tag);
		mUploadFromDB = tag.uploadFromDb;
		mContext = ApplicationContext.getContext();
	}

	private String getSeqString(){
		StringBuilder sb = new StringBuilder();
		sb.append(StoreStatTable.STORE_STAT_DESC + " IN (");
		for(int i = 0;i < UPLOAD_IMAGE.length;i++){
			sb.append(UPLOAD_IMAGE[i]);
			if(i != UPLOAD_IMAGE.length - 1){
				sb.append(",");
			}
		}

		sb.append(")");

		return sb.toString();
	}

    @Override
    protected int getProtocolId() {
        return 0x16;
    }

    @Override
	protected void process() {
		NqLog.i("UploadAdStatProtocol process!");
		try {
			if(mUploadFromDB){
				if(getStatData()){
					TLauncherService.Iface client = TLauncherServiceClientFactory.getClient(getThriftProtocol());
					for(TResourceAction action:dataList){
						client.uploadResourceAction(getUserInfo(), action);
					}

					//delete the db;
					mContext.getContentResolver().delete(AdStatTable.TABLE_URI,
							StoreStatTable.STORE_STAT_ISUPLOAD + " = 1", null);
					EventBus.getDefault().post(new UploadAdStatSuccessEvent(getTag()));
				}
			}else{
				TLauncherService.Iface client = TLauncherServiceClientFactory.getClient(getThriftProtocol());
				TResourceAction action = new TResourceAction();
				UploadAdStatTag tag = (UploadAdStatTag) getTag();
				NqLog.i("UploadAdStatProtocol processAdStat event=" + tag.event);
				action.actionType = tag.event.actionType;
				action.resourceId = tag.event.resourceId;
				action.scene = tag.event.desc + "_" + tag.event.scene;
				NqLog.i("UploadAdStatProtocol processAdStat action.scene=" + action.scene);
				client.uploadResourceAction(getUserInfo(), action);
				EventBus.getDefault().post(new UploadAdStatSuccessEvent(getTag()));
			}

		}catch(Exception e) {
			e.printStackTrace();onError();
		}
	}

	private boolean getStatData() {
		Cursor storeCursor = null;
		ArrayList<Long> mIds = new ArrayList<Long>();
		try{
			storeCursor = mContext.getContentResolver().
					query(AdStatTable.TABLE_URI, null, getSeqString(), null, "_id ASC");
			while(storeCursor != null && storeCursor.moveToNext()) {
				String desc = storeCursor.getString(storeCursor.getColumnIndex(AdStatTable.STORE_STAT_DESC));
				String resourceId = storeCursor.getString(storeCursor.getColumnIndex(AdStatTable.STORE_STAT_RESOURCEID));
				String scene = storeCursor.getString(storeCursor.getColumnIndex(AdStatTable.STORE_STAT_SCNE));
				int actionType = storeCursor.getInt(storeCursor.getColumnIndex(AdStatTable.STORE_STAT_ACTION));

				TResourceAction action = new TResourceAction();
				action.actionType = actionType;
				action.resourceId = resourceId;
				action.scene = desc + "_" + scene;
				dataList.add(action);
				mIds.add(storeCursor.getLong(storeCursor.getColumnIndex(StoreStatTable.STORE_STAT_ID)));
			}

			if (mIds.size() > 0) {
				ContentValues storevalue = new ContentValues();
				storevalue.put(StoreStatTable.STORE_STAT_ISUPLOAD, 1);
				mContext.getContentResolver().update(AdStatTable.TABLE_URI, storevalue, StoreStatTable.STORE_STAT_ID + " BETWEEN ? AND ?",
						new String[] { String.valueOf(mIds.get(0)),String.valueOf(mIds.get(mIds.size() - 1)) });
			}
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
	protected void onError() {
		EventBus.getDefault().post(new UploadAdStatFailEvent(getTag()));
	}
    public static class UploadAdStatSuccessEvent extends AbsProtocolEvent {

        public UploadAdStatSuccessEvent(Object tag){
            setTag(tag);
        }
    }

    public static class UploadAdStatFailEvent extends AbsProtocolEvent{

        public UploadAdStatFailEvent(Object tag) {
            setTag(tag);
        }
    }

}
