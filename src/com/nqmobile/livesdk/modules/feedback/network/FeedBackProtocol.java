package com.nqmobile.livesdk.modules.feedback.network;

import android.util.Log;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.userinfo.TFeedback;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.feedback.FeedbackModule;

public class FeedBackProtocol extends AbsLauncherProtocol {

	private static final ILogger NqLog = LoggerFactory.getLogger(FeedbackModule.MODULE_NAME);
	// ===========================================================
	// Fields
	// ===========================================================
	private final TFeedback feedback;
	private final Object tag;

	// ===========================================================
	// Constructors
	// ===========================================================
	public FeedBackProtocol(String contact, String content, Object tag) {
		this.tag = tag;
		feedback = new TFeedback();
		feedback.setContact(contact);
		feedback.setContent(content);
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected int getProtocolId() {
		return 0xfeed;
	}

	@Override
	public void process() {
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());
			client.uploadFeedback(getUserInfo(), feedback);
			EventBus.getDefault().post(
					new FeedbackUploadedEvent().setTag(tag).setSuccess(true));
		} catch (Throwable e) {
			NqLog.e(Log.getStackTraceString(e)); onError();
		}
	}
	@Override
	protected void onError() {
		EventBus.getDefault().post(
				new FeedbackUploadedEvent().setTag(tag).setSuccess(false));
	}
	/**
	 * 用户反馈上传完毕的事件
	 * 
	 * @author HouKangxi
	 *
	 */
	public static class FeedbackUploadedEvent {
		/**
		 * 上传结果
		 */
		private boolean success;
		private Object tag;

		public boolean isSuccess() {
			return success;
		}

		public FeedbackUploadedEvent setSuccess(boolean success) {
			this.success = success;
			return this;
		}

		public Object getTag() {
			return tag;
		}

		public FeedbackUploadedEvent setTag(Object tag) {
			this.tag = tag;
			return this;
		}

	}

	
}
