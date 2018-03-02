package com.nqmobile.livesdk.commons.moduleframework;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import android.text.TextUtils;

public class ModuleContainer {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	private static ModuleContainer sInstance = new ModuleContainer();
	private Map<String, IModule> mModuleMap;

	// ===========================================================
	// Constructors
	// ===========================================================
	private ModuleContainer() {
		mModuleMap = new LinkedHashMap<String, IModule>();
	}

	public static ModuleContainer getInstance() {
		return sInstance;
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
	public boolean addModule(IModule module) {
		boolean result = false;

		if (module == null) {
			return result;
		}

		String moduleName = module.getName();
		return mModuleMap.put(moduleName, module)==null;
	}

	public boolean removeModule(String moduleName) {
		boolean result = false;

		if (TextUtils.isEmpty(moduleName)) {
			return result;
		}

		return mModuleMap.remove(moduleName)!=null;
	}

	public IModule getModuleByName(String moduleName) {
		return mModuleMap.get(moduleName);
	}

	public Collection<IModule> getModules() {
		return mModuleMap.values();
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
