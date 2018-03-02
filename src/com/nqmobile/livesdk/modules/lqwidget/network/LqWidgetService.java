package com.nqmobile.livesdk.modules.lqwidget.network;


import com.nqmobile.livesdk.commons.net.AbsService;

public class LqWidgetService extends AbsService {
	
	public void getLqWidgetDetail(String resourceId,String lqWidgetId,String widgetName,Object tag) {
		getExecutor().submit(new LqWidgetDetailProtocol(resourceId, lqWidgetId, widgetName, tag));
	}
	public void getLqWidgetList(int offset,int length, Object tag) {
		getExecutor().submit(new LqWidgetListProtocol(offset, length, tag));
	}
	
}
