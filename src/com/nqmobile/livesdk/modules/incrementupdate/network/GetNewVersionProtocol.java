package com.nqmobile.livesdk.modules.incrementupdate.network;

import org.apache.thrift.TApplicationException;

import android.content.Context;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TNewVersionReq;
import com.nq.interfaces.launcher.TNewVersionResp;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.incrementupdate.IncrementUpdateModule;
import com.nqmobile.livesdk.modules.incrementupdate.IncrementUpdatePreference;
import com.nqmobile.livesdk.utils.IncrementUpdateUtils;

/**
 * 新版本信息获取
 * @author chenyanmin
 * @time 2014-8-4 上午10:24:51
 */
public class GetNewVersionProtocol  extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(IncrementUpdateModule.MODULE_NAME);
	
	private IncrementUpdatePreference mPreference;
	private Context mContext;
	private static final String INCREMENT_UPDATE_ALGORITHM = "bsdiff1.0";

    public GetNewVersionProtocol(Object tag) {
        setTag(tag);
        mPreference = IncrementUpdatePreference.getInstance();
        mContext = ApplicationContext.getContext();
    }
    
	@Override
	protected int getProtocolId() {
		return 0x34;//TODO 分配一个协议ID
	}

    @Override
    public void process() {
        try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());

			TNewVersionReq req = new TNewVersionReq();
			req.patchAlgorithm = INCREMENT_UPDATE_ALGORITHM;
			req.pkgName = mContext.getPackageName();            
			req.md5 = IncrementUpdateUtils.getPackageMd5(mContext);
			req.signature = "";
			
			TNewVersionResp resp = client.getNewVersion(getUserInfo(), req);			
			if (resp != null){
				EventBus.getDefault().post(new GetNewVersionSuccessEvent(resp, getTag()));
			} else {
				EventBus.getDefault().post(new GetNewVersionSuccessEvent(null, getTag()));
			}
        } catch (TApplicationException e) {//服务器端无数据
            NqLog.d("GetNewVersionProtocol process() server is empty");
            EventBus.getDefault().post(new GetNewVersionSuccessEvent(null, getTag()));
        } catch (Exception e) {
        	NqLog.e(e);onError();
        } finally {
        	
        }
    }
    @Override
	protected void onError() {
    	EventBus.getDefault().post(new GetNewVersionFailedEvent(getTag()));
	}
    public static class GetNewVersionSuccessEvent extends AbsProtocolEvent {
    	private TNewVersionResp mNewVersion;
    	
    	public GetNewVersionSuccessEvent(TNewVersionResp resp, Object tag){
    		setTag(tag);
    		mNewVersion = resp;
    	}

    	public TNewVersionResp getNewVersion() {
    		return mNewVersion;
    	}
    }
    
    public static class GetNewVersionFailedEvent extends AbsProtocolEvent {
    	public GetNewVersionFailedEvent(Object tag) {
			setTag(tag);
		}
    }
//	private Context mContext;
//    private MyStoreListener.NewVersionListener mListener;
//
//    public GetNewVersionProtocol(Context context, ContentValues value, Listener l) {
//        super(context, value, l);
//        mContext = context;
//        mListener = (MyStoreListener.NewVersionListener)l;
//    }
//
//    @Override
//    protected void process() {
//        NqLog.i("ljc","GetNewVersionProtocol process!");
//        TTransport transport = null;
//        try {
//            TSSLTransportFactory.TSSLTransportParameters params = new TSSLTransportFactory.TSSLTransportParameters();
//            params.setTrustStore(mTrustPath, TRUST_STORE_PASSWORD, "X509", "BKS");
//            transport = TSSLTransportFactory.getClientSocket(CommonMethod.getAppUrl(mContext), PORT, TIMEOUT, params);
//            Stats.beginTrafficStats(0xF034, ((TSocket) transport));//stats
//            TProtocol protocol = new TCompactProtocol(transport);
//            TLauncherService.Iface client = ClientInterfaceFactory.getClientInterface(TLauncherService.Iface.class,protocol);
//            TNewVersionReq req = new TNewVersionReq();
//            req.patchAlgorithm = "bsdiff1.0";
//            req.pkgName = mContext.getPackageName();            
//            req.md5 = IncrementUpdateUtils.getPackageMd5(mContext);
//            //req.md5 = "EC767BD072B6F381D8428F4C6DF7058E".toLowerCase();
//            req.signature = "";
//
//            NqLog.v("ljc","mUserInfo:" + mUserInfo.toString());
//            NqLog.v("ljc","req.md5 = " + req.md5);
//            
//            TNewVersionResp resp = client.getNewVersion(mUserInfo, req);
//            
//			if (resp != null) {
//				NewVersionManager manager = NewVersionManager.getInstance(mContext);
//				NewVersion newVersion = null;
//				if (resp.hasNewVersion == 1) {// 有新版本
//					newVersion = manager.convert(resp);
//				} 
//				if (newVersion != null){
//					NqLog.v("ljc","GetNewVersionProtocol succ:"
//							+ newVersion.toString());
//					
//					manager.saveCache(newVersion);
//					PreferenceDataHelper helper = PreferenceDataHelper.getInstance(mContext);
//					helper.setBooleanValue(PreferenceDataHelper.KEY_HAVE_UPDATE, true);
//				}
//				else {
//			        NqLog.v("ljc","GetNewVersionProtocol no update!");
//					PreferenceDataHelper helper = PreferenceDataHelper.getInstance(mContext);
//					helper.setBooleanValue(PreferenceDataHelper.KEY_HAVE_UPDATE, false);
//				}
//				mListener.onGetNewVersionSucc(newVersion);
//			} else {
//		        NqLog.v("ljc","error:resp is null");
//				mListener.onErr();
//			}
//        } catch (TApplicationException e) {//服务器端无数据
//            NqLog.d("GetNewVersionProtocol process() server is empty");
//            e.printStackTrace();
//            mListener.onGetNewVersionSucc(null);
//        }catch (Exception e) {
//            e.printStackTrace();
//            mListener.onErr();
//        } finally {
//        	Stats.endTrafficStats(((TSocket) transport));//stats
//            try {
//                if (transport != null) {
//                    transport.close();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

	
}
