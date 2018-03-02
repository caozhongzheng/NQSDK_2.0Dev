package com.nqmobile.livesdk.modules.regularupdate.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TProductFeature;
import com.nq.interfaces.launcher.TRegularUpdateReq;
import com.nq.interfaces.launcher.TRegularUpdateResp;
import com.nq.interfaces.launcher.TUpdateAction;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.regularupdate.RegularUpatePreference;
import com.nqmobile.livesdk.modules.regularupdate.RegularUpdateModule;
import com.nqmobile.livesdk.modules.regularupdate.model.UpdateAction;
import com.nqmobile.livesdk.utils.CollectionUtils;

/**
 * 定期联网协议
 * 
 * @author chenyanmin
 * @date 2014-11-22
 */
public class RegularUpdateProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(RegularUpdateModule.MODULE_NAME);
	
	private final static int TRUE = 1;
	private final static int FALSE = 0;

	private final static int REQUEST_TYPE_NORMAL = 0;
	private final static int REQUEST_TYPE_REPORT = 1;
	
	private boolean mHaveSwitchAction;

	public RegularUpdateProtocol(Object tag) {
		setTag(tag);
	}

	@Override
	protected int getProtocolId() {
		return 0x21;
	}

	@Override
	protected void process() {
		NqLog.i("RegularUpdateProtocol.process()");
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());
			doProcess(client, REQUEST_TYPE_NORMAL);
			if (mHaveSwitchAction) {
				doProcess(client, REQUEST_TYPE_REPORT);// TODO: 哪些需要上报？
			}
		} catch (Exception e) {
			NqLog.e(e);
			EventBus.getDefault().post(new RegularUpdateFailedEvent(null));
		} finally {
			long now = SystemFacadeFactory.getSystem().currentTimeMillis();
			RegularUpatePreference.getInstance().setLastRegularUpdateTime(now);
		}
	}

	@Override
	protected void onError() {
		EventBus.getDefault().post(new RegularUpdateFailedEvent(null));
	}

	private void doProcess(TLauncherService.Iface client, int reqType) throws Exception {
		mHaveSwitchAction = false;
		List<IFeature> features = getFeatures();
		TRegularUpdateReq req = getRequest(features,reqType);
		req.reqType = reqType;
		TRegularUpdateResp resp = client.regularUpdate(getUserInfo(), req);
		
		if (reqType == REQUEST_TYPE_REPORT){
			return;
		}
		
		if (resp != null && CollectionUtils.isNotEmpty(resp.updateActions)) {
			List<UpdateAction> actions = new ArrayList<UpdateAction>(
					resp.updateActions.size());
			for (TUpdateAction act : resp.updateActions) {
				NqLog.i("regularProtocol actId=" + act.actionId
						+ " para=" + act.parameters);
				UpdateAction action = new UpdateAction();
				action.setActionId(act.actionId);
				action.setParameters(act.parameters);
				actions.add(action);
				if (act.actionId == 1 || act.actionId == 2) {//有开关启用或关闭操作
					mHaveSwitchAction = true;
				}
			}
			EventBus.getDefault().post(
					new RegularUpdateSuccessEvent(features, actions, getTag()));
		} else {
			EventBus.getDefault().post(
					new RegularUpdateSuccessEvent(features, null, getTag()));
		}
	}

	private TRegularUpdateReq getRequest(List<IFeature> features,int reqType) {
		TRegularUpdateReq req = new TRegularUpdateReq();
		if (CollectionUtils.isNotEmpty(features)) {
			List<TProductFeature> featureList = new ArrayList<TProductFeature>(
					features.size());
			StringBuilder sb = new StringBuilder("{reqType="+reqType+", productFeatures=[");
			for (IFeature f : features) {
				TProductFeature feature = new TProductFeature();
				feature.featureId = f.getFeatureId();
				feature.enabled = f.isEnabled() ? TRUE : FALSE;
				feature.status = f.getStatus();
				feature.versionTag = f.getVersionTag();
				
				featureList.add(feature);
				sb.append(" Feature( featureId=");
				sb.append(feature.featureId);
				sb.append(", ");
				sb.append("enabled=");
				sb.append(feature.enabled);
				sb.append(", ");
				sb.append("status=");
				sb.append(feature.status);
				sb.append(", ");
				sb.append("versionTag=");
				sb.append(feature.versionTag);
				sb.append(" ),");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(" ]} ");
			NqLog.d("Request = "+sb);
			req.productFeatures = featureList;
		}

		return req;
	}

	private List<IFeature> getFeatures() {
		List<IFeature> result = new ArrayList<IFeature>();
		Collection<IModule> modules = ModuleContainer.getInstance()
				.getModules();
		for (IModule module : modules) {
			List<IFeature> features = module.getFeatures();
			if (features != null && !features.isEmpty()) {
				result.addAll(features);
			}
		}

		Collections.sort(result, new Comparator<IFeature>() {
			@Override
			public int compare(IFeature lhs, IFeature rhs) {
				return lhs.getFeatureId() - rhs.getFeatureId();
			}
		});
		return result;
	}

	public static class RegularUpdateSuccessEvent extends AbsProtocolEvent {
		private List<IFeature> mFeatures;
		private List<UpdateAction> mActions;

		public RegularUpdateSuccessEvent(List<IFeature> features,
				List<UpdateAction> actions, Object tag) {
			setTag(tag);
			mFeatures = features;
			mActions = actions;
		}

		public List<UpdateAction> getActions() {
			return mActions;
		}

		public List<IFeature> getFeatures() {
			return mFeatures;
		}

		public void setFeatures(List<IFeature> mFeatures) {
			this.mFeatures = mFeatures;
		}
	}

	public static class RegularUpdateFailedEvent extends AbsProtocolEvent {
		public RegularUpdateFailedEvent(Object tag) {
			setTag(tag);
		}
	}

}
