package com.nqmobile.livesdk.modules.locker.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TApplicationException;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TLockerResource;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.locker.Locker;
import com.nqmobile.livesdk.modules.locker.LockerManager;
import com.nqmobile.livesdk.modules.locker.LockerModule;
import com.nqmobile.livesdk.modules.locker.LockerPreference;

/**
 * 獲取锁屏列表 Protocol
 * 
 * @author HouKangxi
 *
 */
class LockerListProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(LockerModule.MODULE_NAME);
	
	/** 拉风锁屏引擎版本 */
	public static final String KEY_LOCKER_ENGINE_LF = "lafengVersion";
	/** 灵动锁屏引擎版本 */
	public static final String KEY_LOCKER_ENGINE_LD = "lingdongVersion";
	
	private final int column;
	private final int offset;
	private final int length;

	public LockerListProtocol(int column, int offset, int length, Object tag) {
		this.column = column;
		this.offset = offset;
		this.length = length;
		setTag(tag);
	}

	@Override
	protected int getProtocolId() {
		return 0x25;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void process() {
		try {
			Map<String, String> enginParams = new LinkedHashMap<String, String>();
			LockerPreference helper = LockerPreference.getInstance();
			String kLd = KEY_LOCKER_ENGINE_LD;
			String kLf = KEY_LOCKER_ENGINE_LF;
			int lfEngine = helper.getIntValue(kLf);
			int ldEngine = helper.getIntValue(kLd);
			enginParams.put(kLf, String.valueOf(lfEngine));
			enginParams.put(kLd, String.valueOf(ldEngine));
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());
			List<TLockerResource> resources = client.getLockerList(
					getUserInfo(), column, offset, length, enginParams);
			int size = 0;
			if (resources != null && (size = resources.size()) > 0) {
				List<Locker> lockers = new ArrayList<Locker>(size);
				LockerManager mgr = LockerManager
						.getInstance(ApplicationContext.getContext());
				for (TLockerResource res : resources) {
					Locker locker = mgr.lockerResourceToLocker(res);
					lockers.add(locker);
				}
				EventBus.getDefault().post(
						new GetLockerListEvent(lockers, column, offset)
								.setSuccess(true).setTag(getTag()));
			}
			if (size == 0) {
				EventBus.getDefault().post(
						new GetLockerListEvent(column, offset).setSuccess(true)
								.setTag(getTag()));
			}
		} catch (TApplicationException e) {
			// 服务器端无数据
			NqLog.d("LockerListProtocol process() server is empty");
			e.printStackTrace();
			EventBus.getDefault().post(
					new GetLockerListEvent(Collections.EMPTY_LIST, column,
							offset).setSuccess(true).setTag(getTag()));
		} catch (Exception e) {
			e.printStackTrace();onError();
		}
	}

	@Override
	protected void onError() {
		EventBus.getDefault()
		.post(new GetLockerListEvent().setSuccess(false).setTag(
				getTag()));
	}

}
