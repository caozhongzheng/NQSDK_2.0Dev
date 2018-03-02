package com.nqmobile.livesdk.modules.regularupdate.processor;

import android.util.SparseArray;

public class UpdateActionProcessorFactory {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	private static SparseArray<IUpdateActionProcessor> sProcessors;

	// ===========================================================
	// Constructors
	// ===========================================================
	static {
		sProcessors = new SparseArray<IUpdateActionProcessor>();
		SwitchFeatureProcessor swprocessor = new SwitchFeatureProcessor();
		registerProcessor(1, swprocessor);
		registerProcessor(2, swprocessor);
		registerProcessor(4, new UpgradeAssetsProcessor());
		registerProcessor(7, new UpdatePreferencesProcessor());
		registerProcessor(8, new HasUpdateProcessor());
		registerProcessor(9, new UpdateSearchConfigProcessor());
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	private static void registerProcessor(int actionId,
			IUpdateActionProcessor processor) {
		sProcessors.put(actionId, processor);
	}

	public static IUpdateActionProcessor getProcessorByActionId(int actionId) {
		return sProcessors.get(actionId);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
