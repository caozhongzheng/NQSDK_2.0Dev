package com.nqmobile.livesdk.commons.ui;

import java.util.List;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nqmobile.livesdk.commons.AppConstants;
import com.nqmobile.livesdk.commons.info.MobileInfo;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.preference.SettingsPreference;
import com.nqmobile.livesdk.commons.receiver.LiveReceiver;
import com.nqmobile.livesdk.modules.app.AppActionConstants;
import com.nqmobile.livesdk.modules.app.AppPreference;
import com.nqmobile.livesdk.modules.batterypush.BatteryPushActionLogConstants;
import com.nqmobile.livesdk.modules.font.FontConstants;
import com.nqmobile.livesdk.modules.font.FontManager;
import com.nqmobile.livesdk.modules.font.FontPreference;
import com.nqmobile.livesdk.modules.locker.LockerActionConstants;
import com.nqmobile.livesdk.modules.mustinstall.MustInstallActivity;
import com.nqmobile.livesdk.modules.mustinstall.MustInstallPreference;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.modules.storeentry.StoreEntry;
import com.nqmobile.livesdk.modules.theme.ThemeActionConstants;
import com.nqmobile.livesdk.modules.theme.ThemePreference;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperActionConstants;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperPreference;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;
import com.xinmei365.fontsdk.FontCenter;
import com.xinmei365.fontsdk.bean.Font;
import com.xinmei365.fontsdk.net.IHttpCallBack;

/**
 * store框架
 * 
 * @author changxiaofei
 * @time 2013-11-15 下午6:04:41
 */
public class StoreMainActivity extends BaseFragmentActivity implements
		View.OnClickListener {
	private static final ILogger NqLog = LoggerFactory.getLogger("StoreMainActivity");
	/** bundle参数key，要显示的fragment */
	public static final String KEY_FRAGMENT_INDEX_TO_SHOW = "fragment_index_to_show";
	/** bundle参数key，要显示的fragment里的column */
	public static final String KEY_FRAGMENT_COLUMN_TO_SHOW = "fragment_column_to_show";
	private Fragment[] fragments;
	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;
	private int fragmentToShow;
	private int columnToShow;

	private ImageView mTips;
	private ImageView mTheme;
	private ImageView mWallpaper;
	private ImageView mFont;
	private View mLineOne;
	private View mLineTwo;
	private View mLineThree;
	private View mLineFour;
	private TextView mTextTip;
	private TextView mTextTheme;
	private TextView mTextWallpaper;
	private TextView mTextFont;
	private View[] mLines;
	private RelativeLayout mTipLayout;
	private RelativeLayout mThemeLayout;
	private RelativeLayout mWallpaperLayout;
	private RelativeLayout mFontLayout;
	private RelativeLayout[] tabs = new RelativeLayout[4];
	private LinearLayout mTabLayout;
	
	private static final int TAB_TIP = 0;
	private static final int TAB_THEME = 1;
	private static final int TAB_WALLPAPER = 2;
	 private static final int TAB_FONT = 3;

    private static final String[] TAB_KEY = {
            AppPreference.KEY_APP_ENABLE,
            ThemePreference.KEY_THEME_ENABLE,
            WallpaperPreference.KEY_WALLPAPER_ENABLE,
            FontPreference.KEY_FONT_ENABLE
    };

	private static final int TAB_NUM = 4;
	public static String KEY_FROM = "from";
	public static String KEY_COLUMN = "column";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		NqLog.d("StoreMainActivity.onCreate");
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(MResource.getIdByName(getApplication(), "layout","nq_store_main_activity"));

		findViews();
		onAction();
        sendBroadcast(new Intent(LiveReceiver.ACTION_REGULAR_UPDATE));

		// showMustInstall();
	}
	
	private void onAction(){
		 Intent intent = getIntent();
	        if (intent != null) {
	        	int value = intent.getIntExtra(KEY_FROM, 0);
                if (value==1001) {
		            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
		            		BatteryPushActionLogConstants.ACTION_LOG_1907, null, 0, null);
		        }else if(value == StoreEntry.FROM_SHORTCUT){
                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                            "1111", null, 0, null);
                }
	        }
	}

	@Override
	protected void onNewIntent(Intent intent) {
		NqLog.d("StoreMainActivity.onNewIntent()");
		super.onNewIntent(intent);
		this.setIntent(intent);
		setView();
	}

	private boolean isShowMustInstall() {
		boolean mustEnable = MustInstallPreference.getInstance()
				.getBooleanValue(MustInstallPreference.KEY_MUST_INSTALL_ENABLE);
		boolean entered = MustInstallPreference.getInstance()
				.getBooleanValue(MustInstallPreference.KEY_MUST_INSTALL_ENTERED);
		NqLog.d("isShowMustInstall mustEnable:" + mustEnable
				+ ", enter:" + entered + ", wifi:" + NetworkUtils.isWifi(this));
		if (mustEnable && NetworkUtils.isWifi(this) && !entered)
			return true;
		return false;
	}

	private void showMustIntall() {
		Intent i = new Intent(this, MustInstallActivity.class);
		i.putExtra("from", MustInstallActivity.FROM_STORE);
		startActivity(i);
		finish();
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
		if (arg0 == 11) {
			finish();
		}
	}

	/**
	 * 初始化控件
	 */
	private void findViews() {
		NqLog.d("StoreMainActivity.findViews()");
		mTips = (ImageView) findViewById(MResource.getIdByName(this, "id",
				"image_tip"));
		mTheme = (ImageView) findViewById(MResource.getIdByName(this, "id",
				"image_theme"));
		mWallpaper = (ImageView) findViewById(MResource.getIdByName(this, "id",
				"image_wallpaper"));
		mFont = (ImageView) findViewById(MResource.getIdByName(this, "id",
		 "image_font"));

		
		mLines = new View[TAB_NUM];

		mLineOne = findViewById(MResource.getIdByName(this, "id", "line_one"));
		mLineTwo = findViewById(MResource.getIdByName(this, "id", "line_two"));
		mLineThree = findViewById(MResource.getIdByName(this, "id",
				"line_three"));
		 mLineFour = findViewById(MResource.getIdByName(this, "id", "line_four"));

		mLines[0] = mLineOne;
		mLines[1] = mLineTwo;
		mLines[2] = mLineThree;
		mLines[3] = mLineFour;

		mTextTip = (TextView) findViewById(MResource.getIdByName(this, "id",
				"text_tip"));
		mTextTheme = (TextView) findViewById(MResource.getIdByName(this, "id",
				"text_theme"));
		mTextWallpaper = (TextView) findViewById(MResource.getIdByName(this,
				"id", "text_wallpaper"));
		mTextFont = (TextView) findViewById(MResource.getIdByName(this, "id",
		 "text_font"));

		mTabLayout = (LinearLayout) findViewById(MResource.getIdByName(this,
				"id", "tab_layout"));
		mTipLayout = (RelativeLayout) findViewById(MResource.getIdByName(this,
				"id", "tab_tip"));
		mThemeLayout = (RelativeLayout) findViewById(MResource.getIdByName(
				this, "id", "tab_theme"));
		mWallpaperLayout = (RelativeLayout) findViewById(MResource.getIdByName(
				this, "id", "tab_wallpaper"));
		mFontLayout = (RelativeLayout) findViewById(MResource.getIdByName(this, "id",
		 "tab_font"));

		setView();
	}

	private void setView() {
		fragmentToShow = getIntent().getIntExtra(KEY_FRAGMENT_INDEX_TO_SHOW, 0);
		columnToShow = getIntent().getIntExtra(KEY_FRAGMENT_COLUMN_TO_SHOW, 0);		
		NqLog.d("StoreMainActivity.setView() fragmentToShow==" + fragmentToShow
				+ ", columnToShow==" + columnToShow);

		tabs[0] = mTipLayout;
		tabs[1] = mThemeLayout;
		tabs[2] = mWallpaperLayout;
		tabs[3] = mFontLayout;

		boolean[] enableState = new boolean[TAB_NUM];
		int enableSum = 0;
        SettingsPreference helper = new SettingsPreference();
        for(int i = 0;i < TAB_NUM;i++){
            enableState[i] = helper.getBooleanValue(TAB_KEY[i]);
//        	enableState[i] = true;//test only
            NqLog.i("key="+TAB_KEY[i]+" enable="+enableState[i]);
            if(enableState[i]){
                enableSum++;
            }
        }

		boolean getFragmentToShow = false;
		if (enableSum == 1) {
			mTabLayout.setVisibility(View.GONE);
			for (int i = 0; i < TAB_NUM; i++) {
				if (enableState[i]) {
					fragmentToShow = i;
				}
			}
		} else {
			mTabLayout.setVisibility(View.VISIBLE);
			for (int i = 0; i < enableState.length; i++) {
				if (enableState[i]) {
					tabs[i].setVisibility(View.VISIBLE);
					if (enableState[fragmentToShow]) { // 外部传入的显示标签是启用状态
						getFragmentToShow = true;
					} else {
						if (!getFragmentToShow) {
							fragmentToShow = i;
							getFragmentToShow = true;
						}
					}
				} else {
					tabs[i].setVisibility(View.GONE);
				}
			}
		}

		mTipLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				selectTab(TAB_TIP);
			}
		});

		mThemeLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				selectTab(TAB_THEME);
			}
		});

		mWallpaperLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				selectTab(TAB_WALLPAPER);
			}
		});

		 mFontLayout.setOnClickListener(new View.OnClickListener() {
		 @Override
		 public void onClick(View view) {
		 selectTab(TAB_FONT);
		 }
		 });

		fragments = new Fragment[TAB_NUM];
		fragmentManager = getSupportFragmentManager();
		fragments[0] = fragmentManager.findFragmentById(MResource.getIdByName(
				getApplication(), "id", "fragement_application"));
		fragments[1] = fragmentManager.findFragmentById(MResource.getIdByName(
				getApplication(), "id", "fragement_theme"));
		fragments[2] = fragmentManager.findFragmentById(MResource.getIdByName(
				getApplication(), "id", "fragement_wallpaper"));
		fragments[3] = fragmentManager.findFragmentById(MResource.getIdByName(
				getApplication(), "id", "fragement_font"));

		selectTab(fragmentToShow);
	}

	private void selectTab(int index) {
		fragmentTransaction = fragmentManager.beginTransaction();
		for (int i = 0; i < TAB_NUM; i++) {
			mLines[i].setVisibility(View.INVISIBLE);
			fragmentTransaction.hide(fragments[i]);
		}

		mTextTip.setTextColor(getResources().getColor(
				MResource.getIdByName(this, "color", "nq_store_tab_text_sel")));
		mTextTheme.setTextColor(getResources().getColor(
				MResource.getIdByName(this, "color", "nq_store_tab_text_sel")));
		mTextWallpaper.setTextColor(getResources().getColor(
				MResource.getIdByName(this, "color", "nq_store_tab_text_sel")));
		mTextFont.setTextColor(getResources().getColor(
				MResource.getIdByName(this, "color", "nq_store_tab_text_sel")));

		mTips.setImageResource(MResource.getIdByName(this, "drawable",
				"nq_app_normal"));
		mTheme.setImageResource(MResource.getIdByName(this, "drawable",
				"nq_theme_normal"));
		mWallpaper.setImageResource(MResource.getIdByName(this, "drawable",
				"nq_wallpaper_normal"));
		mFont.setImageResource(MResource.getIdByName(this, "drawable",
				"nq_font_normal"));

		switch (index) {
		case 0:
			StatManager.getInstance().onAction(
					StatManager.TYPE_STORE_ACTION,
					AppActionConstants.ACTION_LOG_1100, null, 0, null);
			mTips.setImageResource(MResource.getIdByName(this, "drawable",
					"nq_app_selected"));
			mTextTip.setTextColor(getResources().getColor(
					MResource.getIdByName(this, "color", "nq_white")));
			break;
		case 1:
            StatManager.getInstance().onAction(
					StatManager.TYPE_STORE_ACTION,
					ThemeActionConstants.ACTION_LOG_1200, null, 0, null);
			mTheme.setImageResource(MResource.getIdByName(this, "drawable",
					"nq_theme_selected"));
			mTextTheme.setTextColor(getResources().getColor(
					MResource.getIdByName(this, "color", "nq_white")));
			break;
		case 2:
            StatManager.getInstance().onAction(
					StatManager.TYPE_STORE_ACTION,
					WallpaperActionConstants.ACTION_LOG_1300, null, 0, null);
			mWallpaper.setImageResource(MResource.getIdByName(this, "drawable",
					"nq_wallpaper_selected"));
			mTextWallpaper.setTextColor(getResources().getColor(
					MResource.getIdByName(this, "color", "nq_white")));
			break;
		case 3:
            StatManager.getInstance().onAction(
					StatManager.TYPE_STORE_ACTION,
					FontConstants.ACTION_LOG_3300, null, 0, null);
			mFont.setImageResource(MResource.getIdByName(this, "drawable",
					"nq_font_selected"));
			mTextFont.setTextColor(getResources().getColor(
					MResource.getIdByName(this, "color", "nq_white")));
			break;
		}

		try {
			mLines[index].setVisibility(View.VISIBLE);
			fragmentTransaction.show(fragments[index]);
		} catch (ArrayIndexOutOfBoundsException ae) {
			NqLog.d("StoreMainActivity.selectTab " + System.currentTimeMillis()
					+ ae);
		} catch (Exception e) {
			NqLog.d("StoreMainActivity.selectTab " + System.currentTimeMillis()
					+ e);
		} finally {
			fragmentTransaction.commit();
		}
	}

	@Override
	public void onClick(View v) {

	}

	@Override
	protected void onStart() {
		NqLog.d("StoreMainActivity.onStart " + System.currentTimeMillis());
		super.onStart();

		PreferenceDataHelper helper = PreferenceDataHelper
				.getInstance(getApplication());
		helper.setLongValue(PreferenceDataHelper.KEY_START_STORE,
				System.currentTimeMillis());

        StatManager.getInstance().onAction(
				StatManager.TYPE_STORE_ACTION, AppConstants.ACTION_LOG_1000,
				null, 0, "" + fragmentToShow);
	}

	@Override
	protected void onResume() {
		NqLog.d("StoreMainActivity.onResume " + System.currentTimeMillis());
		super.onResume();
		// onConfigurationChanged not beding called when locale change in
		// FragmentActivity
		mTextTip.setText(MResource.getString(getApplication(), "nq_store_tips",
				null));
		mTextTheme.setText(MResource.getString(getApplication(),
				"nq_store_themes", null));
		mTextWallpaper.setText(MResource.getString(getApplication(),
				"nq_store_wallpapers", null));
		mTextFont.setText(MResource.getString(getApplication(),
				"nq_store_fonts", null));
		
        if(MobileInfo.getDeviceName().equals("ZTE__ZTE__ZTE G718C")){
            getWindow().getDecorView().setPadding(0,50,0,0);
        }

	}

	@Override
	protected void onPause() {
		// TqLog.d("ccc", "StoreMainActivity.onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		NqLog.d("StoreMainActivity.onStop " + System.currentTimeMillis());
		super.onStop();
		// finish();
	}

	@Override
	protected void onDestroy() {
		NqLog.d("StoreMainActivity.onDestroy " + System.currentTimeMillis());
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && isShowMustInstall()) {
			showMustIntall();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	};

}