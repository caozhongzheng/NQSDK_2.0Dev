package com.nqmobile.livesdk.modules.daily;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.modules.feedback.FeedbackModule;
import com.nqmobile.livesdk.utils.MResource;

public class WebActivity extends BaseActvity {
	private static final ILogger NqLog = LoggerFactory.getLogger(DailyModule.MODULE_NAME);
	
	private Context mContext;
	private View mWebLayout;
	private WebView webview;
	/** bundle参数key，url */
	public static final String KEY_WEB_URL = "url";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mContext = WebActivity.this;
		LayoutInflater inflate = LayoutInflater.from(mContext);
		mWebLayout = inflate.inflate(
				MResource.getIdByName(mContext, "layout", "nq_web"), null);
		setContentView(mWebLayout);
		webview = (WebView) mWebLayout.findViewById(MResource.getIdByName(
				getApplication(), "id", "webview"));

		String url = (String) getIntent().getSerializableExtra(KEY_WEB_URL);
		if (url == null || url.isEmpty()) {
			NqLog.e("web url is null or empty");
			finish();
			return;
		}

		// 设置WebView属性，能够执行Javascript脚本
		webview.getSettings().setJavaScriptEnabled(true);
		// 加载需要显示的网页
		webview.loadUrl(url);
		// 设置Web视图
		webview.setWebViewClient(new HelloWebViewClient());
	}

	@Override
	// 设置回退
	// 覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if(webview.canGoBack()){
				webview.goBack(); // goBack()表示返回WebView的上一页面
			} else {
				finish();
			}
			return true;
		}
		return false;
	}

	// Web视图
	private class HelloWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}
}
