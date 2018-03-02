package com.nqmobile.livesdk.modules.newyear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.nqmobile.live.R;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.MResource;

public class LotteryActivity extends BaseActvity {
    /**
     * Called when the activity is first created.
     */
    private ImageView mFuView;
    private ImageView mMusicView;
    private ImageView mSnow;
    private ImageView mFlower;

    private ImageView mOne;
    private ImageView mTwo;
    private ImageView mThree;
    private ImageView mFour;

    private ImageView mLeave;
    private Button mShare;
    private ImageView[] mItems = new ImageView[4];
    private int[] mOnImage = new int[4];
    private int[] mOffImage = new int[4];
    private static final int ROUNT = 15;

    private boolean mMusicOn;

    private MediaPlayer mPlayer;
    private ScrollView mScroll;

    private static final int MSG_LOTTERY_START = 0;
    private static final int MSG_LOTTERY_END = 1;
    public static final String MSG_SHARE_SUCCESS = "newyear.share.success";

    private Context mContext;
    private ShareLauncherDialog mDialog;
    private ShareReceiver mReceiver;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_LOTTERY_START:
                    int show = msg.arg1;
                    for(int i=0;i<4;i++){
                        if(i == show){
                            mItems[show].setImageResource(mOnImage[show]);
                        }else{
                            mItems[i].setImageResource(mOffImage[i]);
                        }
                    }
                    break;
                case MSG_LOTTERY_END:
                    GetPointViewUtil.setPointResultView(LotteryActivity.this);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.nq_lottery_activity);
        int from = getIntent().getIntExtra("from",0);
        Log.i("gqf","from="+from);
        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                from == 2 ?NewYearConstants.ACTION_LOG_3607 :NewYearConstants.ACTION_LOG_3600, null, 0, null);
        setView();
        playMusic();
        mContext = this;
        mDialog = new ShareLauncherDialog(mContext,MResource.getIdByName(mContext, "style", "Translucent_NoTitle"));
        
        mReceiver = new ShareReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MSG_SHARE_SUCCESS);
        this.registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void playMusic(){
        mPlayer = MediaPlayer.create(LotteryActivity.this, R.raw.sound);
        mPlayer.setLooping(true);
        mPlayer.setVolume(1f,1f);
        mPlayer.start();
        mMusicOn = true;
    }

    private void startLottery() {
        mScroll.smoothScrollTo(0,0);
        NewYearPreference.getInstance().setLottery(true);
        long delay = 500;
        int dex = 500;
        for (int i = 0; i < 4*ROUNT + 3; i++) {
            Message msg = Message.obtain();
            msg.arg1 = i % 4;
            mHandler.sendMessageDelayed(msg, delay);
            dex = dex - 10*i;
            if(dex < 100){
                dex = 100;
            }
            delay+=dex;
        }

        delay+=100;
        mHandler.sendEmptyMessageDelayed(MSG_LOTTERY_END,delay);
    }

    private void setView() {
        mFuView = (ImageView) findViewById(R.id.fu);
        mScroll = (ScrollView) findViewById(R.id.scrollView);
        mMusicView = (ImageView) findViewById(R.id.music);
        mMusicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMusicOn){
                    mPlayer.pause();
                    mMusicOn = false;
                    mMusicView.setImageResource(R.drawable.nq_spring_nomusic_btn);
                }else{
                    mMusicOn = true;
                    mPlayer.start();
                    mMusicView.setImageResource(R.drawable.nq_spring_music_btn);
                }
            }
        });
        mOne = (ImageView) findViewById(R.id.item_one);
        mTwo = (ImageView) findViewById(R.id.item_two);
        mThree = (ImageView) findViewById(R.id.item_three);
        mFour = (ImageView) findViewById(R.id.item_four);
        mItems[0] = mOne;
        mItems[1] = mTwo;
        mItems[2] = mThree;
        mItems[3] = mFour;

        mOnImage[0] = R.drawable.nq_spring_gift_iphone_on;
        mOnImage[1] = R.drawable.nq_spring_gift_card_on;
        mOnImage[2] = R.drawable.nq_spring_gift_credit_on;
        mOnImage[3] = R.drawable.nq_spring_gift_100yuan_on;

        mOffImage[0] = R.drawable.nq_spring_gift_iphone;
        mOffImage[1] = R.drawable.nq_spring_gift_card;
        mOffImage[2] = R.drawable.nq_spring_gift_credit;
        mOffImage[3] = R.drawable.nq_spring_gift_100yuan;

        mLeave = (ImageView) findViewById(R.id.leave);
        mLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //行为日志
                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                		NewYearConstants.ACTION_LOG_3605, null, 0, null);	
                
                LotteryActivity.this.finish();
            }
        });

        mShare = (Button) findViewById(R.id.share);
        mShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!NewYearPreference.getInstance().getLottery()){
                    //设置对话框显示位置为靠底部
                    Window window = mDialog.getWindow();
                    WindowManager.LayoutParams wmParams = window.getAttributes();
                    wmParams.gravity = Gravity.BOTTOM;
                    mDialog.onWindowAttributesChanged(wmParams);

                    mDialog.show();

                    //行为日志
                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    		NewYearConstants.ACTION_LOG_3601, null, 0, null);
                }else{
                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                            NewYearConstants.ACTION_LOG_3608, null, 0, null);
                    Toast.makeText(LotteryActivity.this,R.string.nq_already_lottery,Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSnow = (ImageView) findViewById(R.id.snow);
        playAm(mSnow, 0, R.anim.snow_top_to_down_one);

        ImageView snow2= (ImageView) findViewById(R.id.snow2);
        playAm(snow2, 10 * 1000L, R.anim.flower_top_to_down_two);

        ImageView snow3 = (ImageView) findViewById(R.id.snow3);
        playAm(snow3, 15 * 1000L, R.anim.flower_top_to_down_three);

        mFlower = (ImageView) findViewById(R.id.flower);
        playAm(mFlower, 5 * 1000L, R.anim.flower_top_to_down_one);

        ImageView flower2 = (ImageView) findViewById(R.id.flower2);
        playAm(flower2, 13 * 1000L, R.anim.flower_top_to_down_two);

        ImageView flower3 = (ImageView) findViewById(R.id.flower3);
        playAm(flower3, 18 * 1000L, R.anim.flower_top_to_down_three);

        Animation fuAm = AnimationUtils.loadAnimation(this,R.anim.luck_left_to_right);
        mFuView.startAnimation(fuAm);
    }

    private void playAm(final ImageView view,long delay,final int amid){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation am = AnimationUtils.loadAnimation(LotteryActivity.this,amid);
                view.startAnimation(am);
            }
        },delay);
    }

    private void createShortcut(){
        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                NewYearConstants.ACTION_LOG_3606, null, 0, null);
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_ENTRY_SHORTCUT");
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.nq_spring_lottery));
        shortcut.putExtra("duplicate", false); // 不允许重复创建
        Intent i = new Intent();
        i.setAction(Intent.ACTION_MAIN);
        i.setClassName(getPackageName(), LotteryActivity.class.getName());
        i.putExtra("from", 2);
        i.putExtra("shortcutIcon", R.drawable.ic_launcher_gift);
        i.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.nq_spring_lottery));
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
        Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher_gift);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
        sendBroadcast(shortcut);
    }
    
    @Override  
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.i("ljc", "LotteryActivity onActivityResult return!! resultCode = " + resultCode + " data =" + data);
    	if (resultCode == RESULT_OK) {
	        if (data != null) {
	        	String result = data.getStringExtra("share_status");
	        	if ("0".equals(result)) {
	        		mDialog.dismiss();
	                startLottery();
	        	}
	        }
	        
    	}
    }
    
    private class ShareReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
        	Log.i("ljc", "LotteryActivity:ShareReceiver received broadcast...");
        	
            //行为日志
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
            		NewYearConstants.ACTION_LOG_3604, null, 0, null);	
            
        	if (mDialog.isShowing())
        		mDialog.dismiss();
        	startLottery();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mPlayer != null){
        	mPlayer.stop();
        }
    	if (mDialog.isShowing())
    		mDialog.dismiss();
        unregisterReceiver(mReceiver);
        if(!NewYearPreference.getInstance().getLottery()){
            createShortcut();
        }
    }
    
}
