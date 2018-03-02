/**
 * 
 */
package com.nqmobile.livesdk.modules.push.network;

import java.util.ArrayList;

import android.util.Log;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TPush;
import com.nq.interfaces.launcher.TPushResp;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.push.PushManager;
import com.nqmobile.livesdk.modules.push.PushModule;
import com.nqmobile.livesdk.modules.push.PushPreference;
import com.nqmobile.livesdk.modules.push.model.Push;

/**
 * @author HouKangxi
 *
 */
public class PushListProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(PushModule.MODULE_NAME);

	public PushListProtocol(Object tag) {
		setTag(tag);
	}

	@Override
	protected int getProtocolId() {
		return 0x16;
	}

	@Override
	protected void process() {
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());
			TPushResp resp = client.getPushList(getUserInfo());
			int size;
			if (resp.pushmsgs != null && (size = resp.pushmsgs.size()) > 0) {
				PushManager mgr = PushManager.getInstance();
				ArrayList<Push> pushList = new ArrayList<Push>(size);
				for (TPush push : resp.pushmsgs) {
					Push clientPush = mgr.tpushToPush(push);
					if (clientPush != null) {
						pushList.add(clientPush);
					}
				}
				EventBus.getDefault().post(
						new PushResultEvent(pushList).setTag(getTag()));
			}
		} catch (Throwable e) {
			NqLog.e(Log.getStackTraceString(e));onError();
		} finally {
			long now = SystemFacadeFactory.getSystem().currentTimeMillis();
			PushPreference.getInstance().setLastGetPushTime(now);
		}
	}
	@Override
	protected void onError() {
		EventBus.getDefault().post(new PushResultEvent().setTag(getTag()));
	}
	
}
