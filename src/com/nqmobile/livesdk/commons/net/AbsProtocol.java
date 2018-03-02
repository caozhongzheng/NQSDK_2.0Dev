package com.nqmobile.livesdk.commons.net;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

import android.content.Intent;
import android.os.Process;
import android.text.TextUtils;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.userinfo.TActiveResult;
import com.nq.interfaces.userinfo.TPointsInfo;
import com.nq.interfaces.userinfo.TUserInfo;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.concurrent.Priority;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.receiver.LiveReceiver;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.activation.ActivePreference;
import com.nqmobile.livesdk.utils.Stats;

/**
 * 抽象协议基类
 * 
 * @author chenyanmin
 * @date 2014-11-16
 */
public abstract class AbsProtocol implements Runnable, Priority {
	private static ILogger NqLog = LoggerFactory.getLogger("AbsProtocol");
	private Object mTag;
	private ThriftTransportPool mPool;
	private TTransport mTransport;
	private TProtocol mThriftProtocol;
	private TUserInfo mUserInfo;
   
	private static final int FLAG_FORE = 0;
	private static final int FLAG_BACK = 1;
	
	public AbsProtocol(){
		mUserInfo = UserInfoHelper.getUserInfo();
	}

	@Override
	public int getPriority() {
		return Priority.PRIORITY_CRITICAL;
	}

	protected TUserInfo getUserInfo() {
		return mUserInfo;
	}

	protected TProtocol getThriftProtocol() {
		return mThriftProtocol;
	}

	protected abstract int getProtocolId();

	protected abstract void onError();

	public Object getTag() {
		return mTag;
	}

	public void setTag(Object mTag) {
		this.mTag = mTag;
	}

	public static final Object ACTIVE_LOCK = new Object();

	@Override
	public final void run() {
		beforeCall();
		if (mTransport == null) {
			onError();
			return;
		}
		ActivePreference helper = ActivePreference.getInstance();
		String uid = helper.getStringValue(ActivePreference.KEY_UID);
		boolean foreActiveSucc = helper
				.getBooleanValue(ActivePreference.KEY_FORE_ACTIVE_SUCC);
		boolean needForeActive = helper
				.getBooleanValue(ActivePreference.KEY_NEED_FORE_ACTIVE);
		boolean result;
		NqLog.d("run() in " + this + ", uid= " + uid + ",ThreadId="
				+ Thread.currentThread().getId() + ", pid=" + Process.myPid());
		if (TextUtils.isEmpty(uid)) {
			synchronized (ACTIVE_LOCK) {
				NqLog.d("ACTIVE_LOCK1 in " + this + ", ThreadId="+Thread.currentThread().getId()+", pid="+Process.myPid());
				uid = helper.getStringValue(ActivePreference.KEY_UID);
				if (TextUtils.isEmpty(uid)) {
					result = active(needForeActive ? FLAG_FORE : FLAG_BACK);
				} else {
					result = true;
				}
			}

			if (result) {
				getUserInfo().getServiceInfo().uid = helper
						.getStringValue(ActivePreference.KEY_UID);
				execute();
			}else{
				onError();
			}
		} else {
			if (!foreActiveSucc && needForeActive) {
				synchronized (ACTIVE_LOCK) {
					NqLog.d("ACTIVE_LOCK2 in " + this + ", ThreadId="+Thread.currentThread().getId()+", pid="+Process.myPid());
					foreActiveSucc = helper
							.getBooleanValue(ActivePreference.KEY_FORE_ACTIVE_SUCC);
					if (foreActiveSucc) {
						result = true;
					} else {
						result = active(FLAG_FORE);
					}
				}

				if (result) {
					getUserInfo().getServiceInfo().uid = helper
							.getStringValue(ActivePreference.KEY_UID);
					execute();
				}else{
					if (ActivePreference.getInstance().isOverrideInstall()) {
						// 覆盖安装的情况,忽略激活失败
						execute();
					} else {
						// 其他激活失败情况，只有当uid仍然为空时才报错
						String curUid = helper
								.getStringValue(ActivePreference.KEY_UID);
						if (TextUtils.isEmpty(curUid)) {
							onError();
						}
					}
				}
			} else {
				execute();
			}
		}
	}

	private void execute() {
		process();
		afterCall();
	}

	protected abstract void process();

	protected abstract ThriftTransportPool getThriftTransportPool();

	private boolean active(int flag) {
		NqLog.v("AbsProtocol active in " + this + " , flag=" + flag+", ThreadId="+Thread.currentThread().getId()+", pid="+Process.myPid());
		boolean result = false;
		try {
			TCompactProtocol launcherThriftProtocol = new TCompactProtocol(ThriftTransportPool.getLauncherPool().getTransport());
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(launcherThriftProtocol);
			// 激活接口不允许带UID
			getUserInfo().serviceInfo.uid = "";
			TActiveResult activeResult = client.activateUser(getUserInfo(),
					flag);
			if (activeResult != null) {
				ActivePreference helper = ActivePreference.getInstance();
				TPointsInfo info = activeResult.pointsInfo;
				helper.setStringValue(ActivePreference.KEY_UID, info.uid);
				NqLog.i("active success uid=" + info.uid);
				result = true;
				helper.setIntValue(ActivePreference.KEY_SERVERREGION,
						activeResult.serverRegion);
				if (flag == FLAG_FORE) {
					helper.setBooleanValue(
							ActivePreference.KEY_FORE_ACTIVE_SUCC, true);
				}

                new Thread(){
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(15*1000L);
                            ApplicationContext.getContext().sendBroadcast(new Intent(LiveReceiver.ACTION_REGULAR_UPDATE));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
			} else {
				NqLog.i("active failed. result is null");
			}
		} catch (Exception e) {
			NqLog.e("激活失败", e);
		}
		return result;
	}

	protected void beforeCall() {
		try {
			mPool = getThriftTransportPool();
			mTransport = mPool.getTransport();
			mThriftProtocol = new TCompactProtocol(mTransport);
			int protocolId = 0xF000 + getProtocolId();
			Stats.beginTrafficStats(protocolId);// stats
		} catch (Exception e) {
			NqLog.e(e);
		}
	}

	protected void afterCall() {
		Stats.endTrafficStats();// stats
		try {
			if (mPool != null) {
				mPool.closeTransport(mTransport);
				mTransport = null;
			}
		} catch (Exception e) {
			NqLog.e(e);
		}
	}

}
