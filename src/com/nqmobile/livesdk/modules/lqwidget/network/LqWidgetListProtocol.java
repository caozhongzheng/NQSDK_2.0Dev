/**
 * 
 */
package com.nqmobile.livesdk.modules.lqwidget.network;

import java.util.List;

import org.apache.thrift.TApplicationException;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TLqWidget;
import com.nq.interfaces.launcher.TLqWidgetListReq;
import com.nq.interfaces.launcher.TLqWidgetListResp;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsEvent;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.lqwidget.LqWidgetManager;
import com.nqmobile.livesdk.modules.lqwidget.LqWidgetModule;
import com.nqmobile.livesdk.modules.lqwidget.model.LqWidgetInfo;

/**
 * @author nq
 *
 */
public class LqWidgetListProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(LqWidgetModule.MODULE_NAME);
	private  final int offset;
	private  final int length;
	
    public LqWidgetListProtocol(int offset,int length,Object tag) {
        setTag(tag);
        this.offset =offset;
        this.length = length;
    }
    
	@Override
	protected int getProtocolId() {
		return 0xde12;
	}

	@Override
	protected void onError() {
		EventBus.getDefault().post(new LqWidgetListErrorEvent().setTag(getTag()));
	}

	protected void process() {
		NqLog.d("LqWidgetListProtocol process");
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());
			TLqWidgetListReq req = new TLqWidgetListReq();
			req.offset = offset;
			req.length = length;
			TLqWidgetListResp resp = client.getLqWidgetList(getUserInfo(), req);
			List<TLqWidget> lqWidgetList = null;
			if (resp != null) {
				if (resp.lqWidgetList != null) {
					lqWidgetList = resp.lqWidgetList;
				}
			}
			List<LqWidgetInfo> wlist = LqWidgetManager.getInstance().tResource2local(lqWidgetList);
			NqLog.d("lqWidgetList = "+lqWidgetList+"\nlocalList = "+wlist);
			EventBus.getDefault().post(new LqWidgetListSuccessEvent(wlist).setTag(getTag()));
        } catch (TApplicationException e) {//服务器端无数据
            NqLog.d("LqWidgetDetail process() server is empty");
            onError();
        } catch (Exception e) {
        	NqLog.e(e);
        	onError();
        }
	}
	public static class LqWidgetListErrorEvent extends AbsEvent{
		private LqWidgetListErrorEvent(){}
	}
    public static class LqWidgetListSuccessEvent extends AbsEvent{
    	private List<LqWidgetInfo> lqWidgetList;

		private LqWidgetListSuccessEvent(List<LqWidgetInfo> lqWidgetList) {
			super();
			this.lqWidgetList = lqWidgetList;
		}

		public List<LqWidgetInfo> getWidgetList() {
			return lqWidgetList;
		}
    	
    }
}
