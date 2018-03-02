package com.nqmobile.livesdk.modules.browserbandge.network;


import com.nq.interfaces.launcher.TBandge;
import com.nq.interfaces.launcher.TLauncherService;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.browserbandge.BrowserBandgeModule;
import com.nqmobile.livesdk.modules.browserbandge.model.Bandge;



/**
 * 
 * @ClassName: GetBrowserBandgeProtocol 
 * @Description: 获取浏览器角标协议
 * @author handy
 * @date 2014-11-21   
 *
 */
public class GetBrowserBandgeProtocol extends AbsLauncherProtocol{
	private static final ILogger NqLog = LoggerFactory.getLogger(BrowserBandgeModule.MODULE_NAME);

	public GetBrowserBandgeProtocol(Object tag){
		setTag(tag);
	}
	
	@Override
	protected int getProtocolId() {
		return 0x30;
	}

	@Override
	protected void process() {
		NqLog.i("GetBrowserBandgeProtocol process!");
        try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());

			TBandge tb =  client.getBandge(getUserInfo());
			if (tb != null) {
				NqLog.i("info:id="+tb.getResourceId()
						+", url="+tb.getJumpUrl()
						+", CornerType="+ tb.getCornerType()
						);
				Bandge b = new Bandge(tb);
				EventBus.getDefault().post(new GetBrowserBandgeSuccessEvent(b, getTag()));
			} else{				
				failed();
			}
        } catch (Exception e) {
        	e.printStackTrace();
        	NqLog.e(e);
            failed();
        } finally {
        }
    }
	@Override
	protected void onError() {
		 failed();
	}

	private void failed() {
		NqLog.i("GetBrowserBandgeProtocol failed!");
		EventBus.getDefault().post(new GetBrowserBandgeFailedEvent(getTag()));
	}
	
	public static class GetBrowserBandgeSuccessEvent extends AbsProtocolEvent {
    	private Bandge mBandge;
    	
    	public GetBrowserBandgeSuccessEvent(Bandge bandge, Object tag){
    		NqLog.i("GetBrowserBandgeProtocol Success!");
    		setTag(tag);
    		mBandge = bandge;
    	}

    	public Bandge getBandge() {
    		return mBandge;
    	}
    }
    
    public static class GetBrowserBandgeFailedEvent extends AbsProtocolEvent {
    	public GetBrowserBandgeFailedEvent(Object tag) {
			setTag(tag);
		}
    }

	

}
