package com.nqmobile.livesdk.modules.feedback;

import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.modules.feedback.network.FeedBackProtocol.FeedbackUploadedEvent;
import com.nqmobile.livesdk.modules.feedback.network.FeedBackServiceFactory;

public class FeedBackManager extends AbsManager {

	private static FeedBackManager instance;

	private FeedBackManager() {
		EventBus.getDefault().register(this);
	}

	public static synchronized FeedBackManager getInstance() {
		if (instance != null) {
			return instance;
		}
		return instance = new FeedBackManager();
	}

	// ===========================================================
	// Methods
	// ===========================================================
	public void uploadFeedback(String contact, String content,
			FeedBackListener listener) {
		FeedBackServiceFactory.getService().uploadFeedback(contact, content,
				listener);
	}

	@Override
	public void init() {
		if (!EventBus.getDefault().containsSubcription(this)) {
			EventBus.getDefault().register(this);
		}
	}

	public void onEvent(final FeedbackUploadedEvent e) {
		Object tag = e.getTag();
		if (tag == null) {
			return;
		}
		if (e.isSuccess() && tag instanceof FeedBackListener) {
			((FeedBackListener) tag).feedbackSucc();
			// TODO 失败情况的处理
		}
	}
}
