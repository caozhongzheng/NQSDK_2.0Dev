package com.nqmobile.livesdk.modules.feedback;

import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.modules.feedback.network.FeedBackProtocol.FeedbackUploadedEvent;
import com.nqmobile.livesdk.modules.font.FontManager;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.MResource;

public class FeedBackActivity extends BaseActvity {

	private EditText mContent;
	private EditText mContact;
	private Button btnSubmit;
	private boolean mShowConfirm;
	private static final int MAX_LENGTH = 600;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(MResource.getIdByName(getApplication(), "layout",
				"nq_feedback_layout"));

		mContent = (EditText) findViewById(MResource.getIdByName(
				getApplication(), "id", "feedback_content"));
		mContact = (EditText) findViewById(MResource.getIdByName(
				getApplication(), "id", "feedback_contact"));

		mContent.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				MAX_LENGTH) {

			@Override
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				if (source.length() > 0 && dest.length() == MAX_LENGTH) {
                    Toast.makeText(FeedBackActivity.this, MResource.getString(FeedBackActivity.this, "nq_over_length_limit"),
                            Toast.LENGTH_SHORT).show();
				}
				return super.filter(source, start, end, dest, dstart, dend);
			}
		} });

		btnSubmit = (Button) findViewById(MResource.getIdByName(
				getApplication(), "id", "submit"));
		btnSubmit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String content = mContent.getText().toString().trim();
				String contact = mContact.getText().toString().trim();

				if (TextUtils.isEmpty(content)) {
                    Toast.makeText(FeedBackActivity.this, MResource.getString(FeedBackActivity.this, "nq_feedback_empty"),
                            Toast.LENGTH_SHORT).show();
					return;
				}

				if (!CommonMethod.hasActiveNetwork(FeedBackActivity.this)) {
                    Toast.makeText(FeedBackActivity.this, MResource.getString(FeedBackActivity.this, "nq_nonetwork"),
                            Toast.LENGTH_SHORT).show();
					return;
				}
				// 发请求
				FeedBackManager.getInstance().uploadFeedback(contact, content,null);
			}
		});

		ImageView back = (ImageView) findViewById(MResource.getIdByName(
				getApplication(), "id", "back"));
		back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}

	@Override
	public void onBackPressed() {
		String content = mContent.getText().toString().trim();
		if (!TextUtils.isEmpty(content) && !mShowConfirm) {
            Toast.makeText(this, MResource.getString(this, "nq_feedback_confirm"), Toast.LENGTH_SHORT).show();
			mShowConfirm = true;
		} else {
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregist(this);
	}

	public void onEvent(final FeedbackUploadedEvent e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context mContext = FeedBackActivity.this;
                if (e.isSuccess()) {
                    Toast.makeText(mContext, MResource.getString(mContext, "nq_feedback_succ"), Toast.LENGTH_SHORT).show();
                    btnSubmit.setEnabled(false);
                    finish();
                } else {
                    Toast.makeText(mContext, "Feedback Error",Toast.LENGTH_SHORT).show();
                }
            }
        });
	}
}
