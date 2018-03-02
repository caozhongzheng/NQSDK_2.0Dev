package com.nqmobile.livesdk.modules.appstubfolder.network;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.thrift.TApplicationException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.text.TextUtils;

import com.nq.interfaces.launcher.TAppResource;
import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TVirtualFolder;
import com.nq.interfaces.launcher.TVirtualFolderAppResp;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.image.ImageManager;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.appstubfolder.AppStubFolderModule;
import com.nqmobile.livesdk.modules.appstubfolder.AppStubFolderPreference;
import com.nqmobile.livesdk.modules.appstubfolder.model.AppStubFolder;
import com.nqmobile.livesdk.utils.FileUtil;
import com.nqmobile.livesdk.utils.Tools;

/**
 * Created by caozhongzheng on 14-10-10.
 */
public class AppStubFolderProtocol extends AbsLauncherProtocol{
	private static final ILogger NqLog = LoggerFactory.getLogger(AppStubFolderModule.MODULE_NAME);

	private Context mContext;
	private AppStubFolderPreference mPreference;
//    private StubFolderListener mListener;
//    public static final String KEY_SCENE = "scene";
//    public static final String KEY_FOLDERID_LIST = "list";
    private int scene; //0:请求新创建文件夹 1：更新文件夹 2：删除的文件夹
    private List<String> folderIdList;

    public AppStubFolderProtocol(int scene, List<String> folderIdList, Object tag) {
    	setTag(tag);
    	this.mContext = ApplicationContext.getContext();
    	this.mPreference = AppStubFolderPreference.getInstance();
    	this.scene = scene;
    	this.folderIdList = folderIdList;
    }
    
    @Override
	protected int getProtocolId() {
		return 0x36;// 分配一个协议ID
	}

    @Override
    protected void process() {
        try {
        	TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());
        	
//        	List<String> folderIDlist = getFolderList();
            TVirtualFolderAppResp resp = client.getVirtualFolderApps(getUserInfo(), folderIdList, scene);
            List<AppStubFolder> stubFolderList = null;
            if(resp != null && resp.virtualFolderResources != null && !resp.virtualFolderResources.isEmpty()){
            	int size = 0;
            	try {
            		NqLog.i("ok folderIsOpr:" + resp.folderIsOpr + "/" + resp.getFolderIsOpr());
            		NqLog.i("ok resourceIsOpr:" + resp.resourceIsOpr + "/" + resp.getResourceIsOpr());
            		NqLog.i("ok showCount:" + resp.showCount + "/" + resp.getShowCount());
            		
                	int count=0;
                	for(TVirtualFolder tv:resp.virtualFolderResources){
//                		NqLog.i("ok resp"+count+":" + tv.virtualApps);
                		if(tv.virtualApps != null && tv.virtualApps.size() > 0) {
                			size += tv.virtualApps.size();
                			for(TAppResource ta:tv.virtualApps) {
                				NqLog.i("ok respxx"+":" + ta.getResourceId() +"/"+ ta.getName());
                			}
                		} else if(tv.virtualApps == null ){
                			NqLog.i("ok resp virtualApps = null");
                		}
                		
                	}
                	
				} catch (Exception e) {
					NqLog.e(e);
					e.printStackTrace();
				}
            	if(size > 0) {
            		EventBus.getDefault().post(new AppStubFolderPrefChangeEvent(resp, getTag()));
            		EventBus.getDefault().post(new GetAppStubFolderListSuccEvent(resp, getTag(),scene));
            	}
            	
            }
        } catch (TApplicationException e) {//服务器端无数据
            NqLog.d("AppStubFolderProtocol process() server is empty");
            EventBus.getDefault().post(new GetAppStubFolderListFailedEvent(getTag()));
        } catch (Exception e) {
        	NqLog.e(e);onError();
        } finally {
        	mPreference.setLongValue(AppStubFolderPreference.KEY_LAST_GET_STUB_FOLDER_TIME, SystemFacadeFactory.getSystem().currentTimeMillis());
        }
    }
    @Override
	protected void onError() {
    	EventBus.getDefault().post(new GetAppStubFolderListFailedEvent(getTag()));
	}
//    private List<String> getFolderList() {
//		// TODO Auto-generated method stub
//    	List<String> list = new ArrayList<String>();
//    	if(!TextUtils.isEmpty(folderIdList)) {
//    		String[] arr = folderIdList.split(",");
//    		for(int i=0;i<arr.length;i++) {
//    			if(!TextUtils.isEmpty(arr[i])) {
//    				list.add(arr[i]);
//    			}
//    		}
//    	}
//    	
//    	return list;
//	}

	private boolean saveImageToFile(final String srcUrl, final String destFilePath) {
        boolean result = false;
        if (TextUtils.isEmpty(srcUrl)) {
            return result;
        }

        ImageManager imgMgr = ImageManager.getInstance(mContext);
        BitmapDrawable bd = imgMgr.loadImageFromMemoryOrDisk(srcUrl);
        Bitmap bitmap = null;
        if(bd != null){
			try {
//				NqLog.i(TAG, "StubFolderProtocol writeBmpToFile srcUrl:" + srcUrl + ",destFilePath="+destFilePath);
				bitmap = bd.getBitmap();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        if(bitmap == null){
        	InputStream input = null;
        	try {
        		NqLog.i("decodeStream srcUrl:" + srcUrl + ",destFilePath="+destFilePath);
        		input = (InputStream) new URL(srcUrl).getContent();
        		bitmap = BitmapFactory.decodeStream(input);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				FileUtil.closeStream(input);
			}
        }
        if(bitmap != null){
        	if (Tools.stringEquals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
				File file = new File(destFilePath);
				if (!file.getParentFile().exists()) 
					file.getParentFile().mkdirs();
			}
        	boolean w = FileUtil.writeBmpToFile(bitmap, new File(destFilePath));
//        	NqLog.i(TAG, "StubFolderProtocol saveImgfile :" + w);
        }
        result = (bitmap != null);
        return result;
    }
	
	public static class AppStubFolderPrefChangeEvent extends AbsProtocolEvent {

		int showCount;
		boolean isFolderOpratable;
		boolean isFolderResDeletable;

		public AppStubFolderPrefChangeEvent(TVirtualFolderAppResp resp,
				Object tag) {
			setTag(tag);
			showCount = resp.getShowCount();
			isFolderOpratable = resp.getFolderIsOpr() > 0;
			isFolderResDeletable = resp.getResourceIsOpr() > 0;
		}

		public int getShowCount() {
			return showCount;
		}

		public boolean isFolderOpratable() {
			return isFolderOpratable;
		}

		public boolean isFolderResDeletable() {
			return isFolderResDeletable;
		}

	}
	
	public static class GetAppStubFolderListSuccEvent extends AbsProtocolEvent {

        public int scene;
		TVirtualFolderAppResp appStubFolderResp;

		public GetAppStubFolderListSuccEvent(TVirtualFolderAppResp resp,
				Object tag,int scene) {
			setTag(tag);
			appStubFolderResp = resp;
            this.scene = scene;
		}

		public TVirtualFolderAppResp getAppStubFolderResp() {
			return appStubFolderResp;
		}
	}

	public static class GetAppStubFolderListFailedEvent extends AbsProtocolEvent {
    	public GetAppStubFolderListFailedEvent(Object tag) {
			setTag(tag);
		}
    }

	
}
