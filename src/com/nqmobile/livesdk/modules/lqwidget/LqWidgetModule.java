/**
 * 
 */
package com.nqmobile.livesdk.modules.lqwidget;

import java.util.LinkedList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.lqwidget.features.LqWidgetFeature;

/**
 * @author nq
 *
 */
public class LqWidgetModule extends AbsModule {
	public static final String MODULE_NAME = "LQWidget";
	
    public static ILogger Nqlog = LoggerFactory.getLogger(MODULE_NAME);
    
	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public boolean canEnabled() {
		return LqWidgetPreference.getInstance().isLqWidgetEnable();
	}
	
	
	@Override
	public List<IFeature> getFeatures() {
		List<IFeature> fs = new LinkedList<IFeature>();
		fs.add(new LqWidgetFeature());
		return fs;
	}

	@Override
	public List<IDataTable> getTables() {
		return null;
	}

	@Override
	protected void onEnabled(boolean enabled) {
		LqWidgetPreference.getInstance().setLqWidgetEnable(enabled);
//		if (enabled) {
			LqWidgetManager.getInstance().init();
//		} else {
//			LqWidgetManager.getInstance().onDisable();
//		}
	}

	public void init() {
		LqWidgetManager.getInstance().init();
	}
}
