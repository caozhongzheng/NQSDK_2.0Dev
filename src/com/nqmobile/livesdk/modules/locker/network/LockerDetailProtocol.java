/**
 * 
 */
package com.nqmobile.livesdk.modules.locker.network;

import com.nq.interfaces.launcher.TLauncherException;
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

/**
 * @author HouKangxi
 *
 */
class LockerDetailProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(LockerModule.MODULE_NAME);
	
	private final String id;

	public LockerDetailProtocol(String id, Object tag) {
		super();
		this.id = id;
		setTag(tag);
	}

	@Override
	protected int getProtocolId() {
		return 0x26;
	}

	@Override
	protected void process() {
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());
			TLockerResource resource = client
					.getLockerDetail(getUserInfo(), id);
			Locker locker = LockerManager.getInstance(
					ApplicationContext.getContext()).lockerResourceToLocker(
					resource);
			if (resource != null) {
				NqLog.d("LockerDetailProtocol process(), resource:Id= "
						+ resource.getId() + " ,LockerUrl= "
						+ resource.getDownloadUrl());
				GetLockerDetailEvent e = new GetLockerDetailEvent();
				e.setSuccess(true).setLocker(locker).setTag(getTag());
				EventBus.getDefault().post(e);
			} else {
				NqLog.d("LockerDetailProtocol process() resources == null");
			}
		} catch (TLauncherException e) {
			e.printStackTrace();
			GetLockerDetailEvent evt = new GetLockerDetailEvent();
			evt.setSuccess(true).setTag(getTag());
			EventBus.getDefault().post(evt);
		} catch (Exception e) {
			e.printStackTrace(); onError();
		}
	}

	@Override
	protected void onError() {
		EventBus.getDefault().post(
				new GetLockerDetailEvent().setSuccess(false).setTag(
						getTag()));
	}
}
