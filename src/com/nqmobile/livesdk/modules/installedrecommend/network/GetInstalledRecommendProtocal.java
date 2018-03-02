package com.nqmobile.livesdk.modules.installedrecommend.network;

import java.util.List;

import com.nq.interfaces.launcher.TAppResource;
import com.nq.interfaces.launcher.TLauncherService;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.installedrecommend.InstalledRecommendModule;
import com.nqmobile.livesdk.modules.installedrecommend.InstalledRecommendPreference;

/**
 * 安装后关联推荐获取
 * @author liujiancheng
 * @time 2014-11-26
 */
public class GetInstalledRecommendProtocal extends AbsLauncherProtocol{
	private static final ILogger NqLog = LoggerFactory.getLogger(InstalledRecommendModule.MODULE_NAME);
	
	private String mPackageName;
	private InstalledRecommendPreference mPreference;
	
	public GetInstalledRecommendProtocal(String packageName, Object tag) {		
		mPackageName = packageName;
        setTag(tag);
        
        mPreference = InstalledRecommendPreference.getInstance();
	}
	
	@Override
	protected int getProtocolId() {
		return 0x37;
	}

	@Override
	public void process() {
		NqLog.i("GetInstalledRecommendProtocal:request process! mPackageName = " + mPackageName);
//		String packageName = mPreference.getStringValue(InstalledRecommendPreference.KEY_LAST_INSTALLED_RECOMMEND_PACKAGE);
//		long lastRequestTime = mPreference.getLongValue(InstalledRecommendPreference.KEY_LAST_INSTALLED_RECOMMEND_TIME);
//	
//		//　如果同一个包在3秒钟之内连续请求，则返回，修复有时候连续请求服务器会重复下发同样广告的bug
//		long current = System.currentTimeMillis();
//		if (packageName.equals(mPackageName) && (current - lastRequestTime) < 3000) {
//			return;
//		}
		
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());
			
			List<TAppResource> resp = client.getInstallAssociationAppList(getUserInfo(), mPackageName);

			if (resp != null) {
				EventBus.getDefault().post(new GetInstalledRecommendSuccessEvent(resp, getTag()));
			} else {
				EventBus.getDefault().post(new GetInstalledRecommendFailedEvent(getTag()));
			}
		} catch(Exception e){
			NqLog.e(e);onError();
		} finally {
//			mPreference.setStringValue(InstalledRecommendPreference.KEY_LAST_INSTALLED_RECOMMEND_PACKAGE, mPackageName);
//			mPreference.setLongValue(InstalledRecommendPreference.KEY_LAST_INSTALLED_RECOMMEND_TIME, current);
		}
	}
	
	@Override
	protected void onError() {
		EventBus.getDefault().post(new GetInstalledRecommendFailedEvent(getTag()));
	}
	
	public static class GetInstalledRecommendSuccessEvent extends AbsProtocolEvent {
		private List<TAppResource> mApp;
		
		public GetInstalledRecommendSuccessEvent(List<TAppResource> resp, Object tag) {
			setTag(tag);
			mApp = resp;
		}	

		public List<TAppResource> getAppResource() {
			return mApp;
		}
	}
	
	public static class GetInstalledRecommendFailedEvent extends AbsProtocolEvent {
		public GetInstalledRecommendFailedEvent(Object tag) {
			setTag(tag);
		}
	}

	
}
