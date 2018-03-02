/**
 * 
 */
package com.nqmobile.livesdk.modules.lqwidget;

import java.util.List;

import com.nqmobile.livesdk.modules.lqwidget.model.LqWidgetInfo;

/**
 * @author nq
 *
 */
public interface LqWidgetListener {
	void onErr();
	void onSuccess(List<LqWidgetInfo> widgets);
}
