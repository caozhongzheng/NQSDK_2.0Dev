package com.nqmobile.livesdk.modules.apptype.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import android.content.Context;

import com.nq.interfaces.launcher.TAppTypeInfo;
import com.nq.interfaces.launcher.TAppTypeInfoQuery;
import com.nq.interfaces.launcher.TAppTypeInfoQueryResult;
import com.nq.interfaces.launcher.TLauncherService;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.net.ThriftTransportPool;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.apptype.AppTypeModule;
import com.nqmobile.livesdk.modules.apptype.model.AppTypeInfo;
import com.nqmobile.livesdk.modules.apptype.model.AppTypeInfoConverter;
import com.nqmobile.livesdk.utils.CollectionUtils;
import com.nqmobile.livesdk.utils.PackageUtils;

public class GetAppTypeProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(AppTypeModule.MODULE_NAME);
    private List<String> mPackageList;

    public GetAppTypeProtocol(List<String> packageList, Object tag) {
        setTag(tag);
        mPackageList = packageList;
    }
    
	@Override
	protected int getProtocolId() {
		return 0x24;
	}

	private List<String> appendAppName(){
		int initialSize = mPackageList.size();
		if (initialSize <= 0){
			return new ArrayList<String>();
		}
		
		List<String> packageAppNameList = new ArrayList<String>(initialSize);
		Context context = ApplicationContext.getContext();
		for (String pkgName : mPackageList) {
			pkgName += "," + PackageUtils.getAppName(context, pkgName);
			packageAppNameList.add(pkgName);

		}
		return packageAppNameList;
	}
	
	@Override
	protected void process() {
		if (CollectionUtils.isEmpty(mPackageList)){
			onError();
			return; // mPackageList is empty, do nothing and return
		}
		TTransport transport = null;
		TSocket socket = null;
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());
			if (mPackageList.size() > 1) {
				transport = getThriftProtocol().getTransport();
				socket = (TSocket) transport;
				socket.setTimeout(3000);//这里请求识别多个包名，应该是桌面首次启动第一次分类，如果超过3秒没能从服务端获取结果，就不用服务端的
			}

			TAppTypeInfoQuery req = new TAppTypeInfoQuery();
			
//			req.packageNames = mPackageList;
			req.packageNames = appendAppName();
			TAppTypeInfoQueryResult result = client.getAppTypeInfo(getUserInfo(), req);
			Map<String, AppTypeInfo> appTypeMap = new HashMap<String, AppTypeInfo>(mPackageList.size());
			if (result != null && CollectionUtils.isNotEmpty(result.appTypeInfos)) {
				List<TAppTypeInfo> list = result.appTypeInfos;
				for (TAppTypeInfo at : list) {
					AppTypeInfo appType = AppTypeInfoConverter
							.fromTAppTypeInfo(at);
					appTypeMap.put(appType.getPackageName(), appType);
				}
            }
			
			List<AppTypeInfo> appTypeList = new ArrayList<AppTypeInfo>(mPackageList.size());
			for(String pkgName : mPackageList){
				AppTypeInfo appType = appTypeMap.get(pkgName);
				if (appType == null){
					appType = new AppTypeInfo(pkgName, AppTypeInfo.CODE_UNKNOWN, "", "");
				}
				appTypeList.add(appType);
				}
				EventBus.getDefault().post(new GetAppTypeSuccessEvent(appTypeList, getTag()));
        } catch (Exception e) {
        	NqLog.e(e); 
        	onError();
        } finally {
        	if (socket != null) {
        		socket.setTimeout(ThriftTransportPool.TIMEOUT);//改回默认超时时间
        	}
        }
	}
	@Override
	protected void onError() {
		if (CollectionUtils.isEmpty(mPackageList)){
			EventBus.getDefault().post(new GetAppTypeFailedEvent(null, getTag()));
		} else {
			List<AppTypeInfo> appTypeList = new ArrayList<AppTypeInfo>(mPackageList.size());
			for(String pkgName : mPackageList){
				AppTypeInfo appType = new AppTypeInfo(pkgName, AppTypeInfo.CODE_UNKNOWN, "", "");
				appTypeList.add(appType);
			}
			EventBus.getDefault().post(new GetAppTypeFailedEvent(appTypeList, getTag()));
		}
	}
	public static class GetAppTypeSuccessEvent extends AbsProtocolEvent {
    	private List<AppTypeInfo> mAppTypeList;
        
    	public GetAppTypeSuccessEvent(List<AppTypeInfo> list, Object tag){
    		setTag(tag);
    		mAppTypeList = list;
    	}

    	public List<AppTypeInfo> getAppTypeInfos() {
    		return mAppTypeList;
    	}
    	
    }
	
	public static class GetAppTypeFailedEvent extends AbsProtocolEvent{
		private List<AppTypeInfo> mAppTypeList;
		
	    public GetAppTypeFailedEvent(List<AppTypeInfo> list, Object tag){
	        setTag(tag);
	        mAppTypeList = list;
	    }
	    
	    public List<AppTypeInfo> getAppTypeInfos() {
    		return mAppTypeList;
	    }
	}
    
}
