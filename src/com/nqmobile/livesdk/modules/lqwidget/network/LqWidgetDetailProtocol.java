/**
 * 
 */
package com.nqmobile.livesdk.modules.lqwidget.network;

import org.apache.thrift.TApplicationException;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TLqWidget;
import com.nq.interfaces.launcher.TLqWidgetInfoReq;
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
public class LqWidgetDetailProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory
			.getLogger(LqWidgetModule.MODULE_NAME);
	private String resourceId;// #小组件资源ID
	private String lqWidgetId;// #小组件ID
    private String widgetName;
    
	public LqWidgetDetailProtocol(String resourceId, String lqWidgetId,String widgetName,
			Object tag) {
		setTag(tag);
		this.resourceId = resourceId;
		this.lqWidgetId = lqWidgetId;
		this.widgetName = widgetName;
	}

	@Override
	protected int getProtocolId() {
		return 0xde;
	}

	@Override
	protected void onError() {
		EventBus.getDefault().post(new LqWidgetDetailErrorEvent().setTag(getTag()));
	}

	protected void process() {
		NqLog.d("LqWidgetDetailProtocol process()");
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());
			TLqWidgetInfoReq req = new TLqWidgetInfoReq();
			req.setLqWidgetId(lqWidgetId);
			req.setResourceId(resourceId);
			TLqWidget widgetInfo = client.getLqWidgetInfo(getUserInfo(), req);
			LqWidgetInfo lqw = LqWidgetManager.getInstance().tResource2local(widgetInfo);
			NqLog.d("widgetInfo = "+widgetInfo+"\n local_lqwidget="+lqw);
			EventBus.getDefault().post(new LqWidgetDetailSuccessEvent(lqw).setWidgetName(widgetName).setTag(getTag()));
		} catch (TApplicationException e) {// 服务器端无数据
			NqLog.d("LqWidgetDetail process() server is empty");
			onError();
		} catch (Exception e) {
			NqLog.e(e);
			onError();
		}
	}

	public static class LqWidgetDetailErrorEvent extends AbsEvent{
		private LqWidgetDetailErrorEvent() {
		}
	}

	public static class LqWidgetDetailSuccessEvent extends AbsEvent{
		private LqWidgetInfo widgetInfo;
		private String widgetName;
		 
		private LqWidgetDetailSuccessEvent(LqWidgetInfo widgetInfo) {
			super();
			this.widgetInfo = widgetInfo;
		}

		public LqWidgetInfo getWidgetInfo() {
			return widgetInfo;
		}

		public void setWidgetInfo(LqWidgetInfo widgetInfo) {
			this.widgetInfo = widgetInfo;
		}

		public LqWidgetDetailSuccessEvent setWidgetName(String widgetName) {
			this.widgetName = widgetName;return this;
		}

		public String getWidgetName() {
			return widgetName;
		}
        
	}
}
