package com.nqmobile.livesdk.modules.regularupdate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.commons.moduleframework.IUpdateActionHandler;
import com.nqmobile.livesdk.commons.service.PeriodCheckEvent;
import com.nqmobile.livesdk.modules.regularupdate.model.UpdateAction;
import com.nqmobile.livesdk.modules.regularupdate.network.RegularUpdateProtocol.RegularUpdateSuccessEvent;
import com.nqmobile.livesdk.modules.regularupdate.network.RegularUpdateServiceFactory;
import com.nqmobile.livesdk.modules.regularupdate.processor.IUpdateActionProcessor;
import com.nqmobile.livesdk.modules.regularupdate.processor.UpdateActionProcessorFactory;
import com.nqmobile.livesdk.utils.CollectionUtils;

public class RegularUpdateManager extends AbsManager {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(RegularUpdateModule.MODULE_NAME);
	
	private static final RegularUpdateManager instance = new RegularUpdateManager();

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================
	private RegularUpdateManager() {
	}

	public static RegularUpdateManager getInstance() {
		return instance;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void init() {
		EventBus.getDefault().register(this);
	}

	// ===========================================================
	// Methods
	// ===========================================================
	public void regularUpdate(boolean immediate) {
		if (immediate || RegularUpdatePolicy.isExceedFrequency()) {
			RegularUpdateServiceFactory.getService().regularUpdate(null);
		}
	}

	public void onEvent(PeriodCheckEvent event) {
		NqLog.i("onEvent(PeriodCheckEvent=" + event + ")");
		if (RegularUpdatePolicy.isExceedFrequency()) {
			regularUpdate(false);
		}
	}

	public void onEvent(RegularUpdateSuccessEvent event) {
		List<IFeature> features = event.getFeatures();
		List<UpdateAction> actions = event.getActions();

		if (CollectionUtils.isEmpty(actions)) {
			return;
		}

		Map<Integer, IUpdateActionHandler> handlers = getHanlders(features);
		for (UpdateAction action : actions) {
			try {
				IUpdateActionProcessor processor = UpdateActionProcessorFactory
						.getProcessorByActionId(action.getActionId());
				processor.process(handlers, action);
			} catch (Exception e) {
				NqLog.e(e);
			}
		}

	}

	private Map<Integer, IUpdateActionHandler> getHanlders(
			List<IFeature> features) {
		Map<Integer, IUpdateActionHandler> result = new HashMap<Integer, IUpdateActionHandler>();
		for (IFeature f : features) {
			result.put(f.getFeatureId(), f.getHandler());
		}

		return result;
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
