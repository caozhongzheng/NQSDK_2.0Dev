package com.nqmobile.livesdk.modules.update.network;

import java.io.File;

import org.xml.sax.SAXException;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.info.CommonDefine;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.net.UpdateListener;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.modules.update.UpdateModule;
import com.nqmobile.livesdk.modules.update.UpdatePreference;
import com.nqmobile.livesdk.utils.HttpObserver;
import com.nqmobile.livesdk.utils.HttpUtil;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;

public class UpdateProtocol extends AbsLauncherProtocol implements HttpObserver{
	private static final ILogger NqLog = LoggerFactory.getLogger(UpdateModule.MODULE_NAME);
	
	private static final String NO_UPDATE_CMD = "1";
	private static final String NEED_UPDATE_CMD = "6";
	
	public static final String NEED_UPDATE = "needUpdate";
	public static final String DOWNLOAD_URL = "downloadUrl";
	public static final String NEW_VERSION = "newVersion";

    private Context mContext;
    private ContentValues mReponseValue;

	public UpdateProtocol(UpdateListener listener) {
		setTag(listener);
        mContext = ApplicationContext.getContext();
        mReponseValue = new ContentValues();
	}

    @Override
    protected int getProtocolId() {
        return 0x09;
    }

    @Override
	protected void process() {
		NqLog.i("UpdateProtocol process!");

		AppUpdateReq req = new AppUpdateReq(mContext);
		HttpUtil util = new HttpUtil(UpdateProtocol.this);
		try {
			util.doPost(CommonDefine.UPDATE_URL, req.getRequestBytes());
		}catch(Throwable e){
			e.printStackTrace();
			onError();
		}finally {
			long now = SystemFacadeFactory.getSystem().currentTimeMillis();
			UpdatePreference.getInstance().setLongValue(UpdatePreference.KEY_LAST_CHECK_UPDATE,
                    now);
		}
		NqLog.i("UpdateProtocol process finish!");
	}
    @Override
	protected void onError() {
	}
	@Override
	public void onRecvProgress(byte[] data, int length) {
	}

	@Override
	public void onHttpResult(int resultCode, byte[] respone) {
		NqLog.i("onHttpResut resultCode=" + resultCode);
        PreferenceDataHelper helper = PreferenceDataHelper.getInstance(mContext);
		if (resultCode != HttpUtil.ERROR_NONE) {
            EventBus.getDefault().post(new UpdateFailEvent(getTag()));
		} else {
			String rs = new String(respone);
			NqLog.i(rs);
			try {
				Bundle b = new Bundle();
				android.util.Xml.parse(rs, new AppUpdateHandler(mReponseValue));
				if (mReponseValue.getAsString("Command").equals(NO_UPDATE_CMD)) {
					b.putBoolean(NEED_UPDATE, false);
                    helper.setBooleanValue(PreferenceDataHelper.KEY_HAVE_UPDATE, false);
                    EventBus.getDefault().post(new UpdateSuccessEvent(b,getTag()));
				} else if (mReponseValue.getAsString("Command").equals(NEED_UPDATE_CMD)) {
                    String downloadUrl = mReponseValue.getAsString("AppUpdateSrc");
                    b.putBoolean(NEED_UPDATE, true);
                    b.putString(DOWNLOAD_URL, downloadUrl);
                    b.putString(NEW_VERSION,mReponseValue.getAsString("subtitle"));
                    helper.setStringValue(PreferenceDataHelper.KEY_UPDATE_SUBTITLE, mReponseValue.getAsString("subtitle"));

                    String oldUpdateFileName = helper.getStringValue(PreferenceDataHelper.KEY_UPDATE_FILE_NAME);
                    String newUpdateFileName = mReponseValue.getAsString("AppUpdateFileName");

                    //when the newUpdateFile is not same the oldUpdateFile,delete the oldUpdateFile
                    if(!oldUpdateFileName.equals(newUpdateFileName)){
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),oldUpdateFileName);
                        if(file.exists()){
                            file.delete();
                        }
                    }

                    helper.setStringValue(PreferenceDataHelper.KEY_UPDATE_FILE_NAME, mReponseValue.getAsString("AppUpdateFileName"));
                    helper.setBooleanValue(PreferenceDataHelper.KEY_HAVE_UPDATE, true);
                    helper.setStringValue(PreferenceDataHelper.KEY_DOWNLOAD_URL, downloadUrl);
                    NqLog.i("UpdateProtocol subtitle: " + mReponseValue.getAsString("subtitle"));
                    EventBus.getDefault().post(new UpdateSuccessEvent(b,getTag()));
				}
			} catch (SAXException e) {
				e.printStackTrace();
                EventBus.getDefault().post(new UpdateFailEvent(getTag()));
			}
		}
	}

	@Override
	public void onContentLength(long length) {
		// TODO Auto-generated method stub
	}

    public static class UpdateSuccessEvent extends AbsProtocolEvent {

        public Bundle b;

        public UpdateSuccessEvent(Bundle b,Object tag){
            setTag(tag);
            this.b = b;
        }
    }

    public static class UpdateFailEvent extends AbsProtocolEvent{

        public UpdateFailEvent(Object tag){
            setTag(tag);
        }
    }

	
}
