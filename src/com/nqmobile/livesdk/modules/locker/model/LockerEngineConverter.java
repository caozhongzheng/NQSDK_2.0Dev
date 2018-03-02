package com.nqmobile.livesdk.modules.locker.model;

import android.content.Context;
import android.text.TextUtils;

import com.nq.interfaces.launcher.TLockerEngine;
import com.nqmobile.livesdk.modules.locker.LockerConstants;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.StringUtil;

public class LockerEngineConverter {
	/**
	 * TLockerEngine转换成LockerEngine对象
	 * 
	 * @param TLockerEngine
	 * @return
	 */
	public static LockerEngine convert(Context context, TLockerEngine tLocker) {
		LockerEngine lockerEngine = null;
		if (tLocker != null) {
			lockerEngine = new LockerEngine();
			lockerEngine.setType(tLocker.getType());
			lockerEngine.setVersion(tLocker.getVersion());
			lockerEngine.setVersionName(StringUtil.nullToEmpty(tLocker
					.getVersionName()));
			lockerEngine.setSize(tLocker.getSize());

			String SDCardPath = CommonMethod.getSDcardPath(context);
			if (SDCardPath == null)
				SDCardPath = CommonMethod.getSDcardPathFromPref(context);
			// icon本地路径
			if (!TextUtils.isEmpty(tLocker.getDownUrl())) {
				String strLibPath = new StringBuilder().append(SDCardPath)
						.append(LockerConstants.STORE_APP_LIB_LOCAL_PATH)
						.toString();
				String fileName = null;
				if (lockerEngine.getType() == LockerEngine.LIB_ENGINE_LD)
					fileName = LockerConstants.ENGINE_LIB_LD_FILE_NAME;
				else if (lockerEngine.getType() == LockerEngine.LIB_ENGINE_LF)
					fileName = LockerConstants.ENGINE_LIB_LF_FILE_NAME;
				lockerEngine.setPath(new StringBuilder().append(strLibPath)
						.append(fileName).toString());
			}
			lockerEngine.setDownUrl(tLocker.getDownUrl());
			lockerEngine.setMd5(tLocker.getMd5());
		}
		return lockerEngine;
	}
}