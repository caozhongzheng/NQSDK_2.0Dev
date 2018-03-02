package com.nqmobile.livesdk.commons.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nqmobile.livesdk.utils.MResource;

@SuppressLint("NewApi")
public class MoveBackView extends FrameLayout {

	private static final int DURATION = 1200;
	private ImageView ballLeft, ballRight;
	private View animView, loading;
	private AnimatorSet as;
	private int off = 0;
	
	public MoveBackView(Context context) {
		this(context, null);
	}
	
	public MoveBackView(Context context, AttributeSet attrs) {
		this(context, attrs, -1);
	}
	
	public MoveBackView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ballLeft = (ImageView) findViewById(MResource.getIdByName(getContext(), "id", "nq_ball_left"));
        ballRight = (ImageView) findViewById(MResource.getIdByName(getContext(), "id", "nq_ball_right"));
        
        animView = findViewById(MResource.getIdByName(getContext(), "id", "nq_anim"));
        loading = findViewById(MResource.getIdByName(getContext(), "id", "nq_loading"));
        as = new AnimatorSet();
    }
	
	private boolean mIsShowLoading = false;
	public void startAnim(boolean isShowLoading) {
		mIsShowLoading = isShowLoading;
		if(!mIsShowLoading)	{
			if(off > 0)
				createAnim();
			animView.setVisibility(0);
			loading.setVisibility(8);
			setVisibility(0);
		}else{
			stopAnim();
			//loading.setVisibility(0);
		}
	}
	
	public void stopAnim(){
		as.cancel();
		animView.setVisibility(8);
		loading.setVisibility(8);
		setVisibility(8);
	}
	
    private void createAnim() {
    	ObjectAnimator translationLeftToRight = ObjectAnimator.ofFloat(ballLeft, "X", 0, off);
    	ObjectAnimator translationRightToLeft = ObjectAnimator.ofFloat(ballRight, "X", off, 0);
    	
    	as.playTogether(translationLeftToRight, translationRightToLeft);
    	
    	translationLeftToRight.setRepeatCount(-1);
    	translationLeftToRight.setRepeatMode(2);
    	translationRightToLeft.setRepeatCount(-1);
    	translationRightToLeft.setRepeatMode(2);
    	as.setDuration(DURATION);
    	as.start();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	super.onLayout(changed, l, t, r, b);
    	if(off == 0){
			off  = ((ViewGroup)ballLeft.getParent()).getWidth() - ballLeft.getWidth();
			startAnim(mIsShowLoading);
    	}
    }
    
}
