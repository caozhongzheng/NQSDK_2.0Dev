/**
 * 
 */
package com.nqmobile.livesdk.modules.feedback.network;

import com.nqmobile.livesdk.commons.net.AbsService;

/**
 * @author HouKangxi
 *
 */
public class FeedBackService extends AbsService {
	public void uploadFeedback(String contact, String content, Object tag) {
		getExecutor().submit(new FeedBackProtocol(contact, content, tag));
	}
}
