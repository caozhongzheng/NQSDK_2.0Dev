package com.nqmobile.livesdk.modules.theme;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.receiver.DownloadReceiver;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.commons.ui.PreviewActivity;
import com.nqmobile.livesdk.modules.font.FontManager;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.DisplayUtil;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * 主题详情页面
 * @author changxiaofei
 * @time 2013-11-21 下午5:04:13
 */
public class ThemeDetailActivity extends BaseActvity implements View.OnClickListener,ThemeDetailListener {
	private static final ILogger NqLog = LoggerFactory.getLogger(ThemeModule.MODULE_NAME);
	
	public static final String INTENT_ACTION = "com.nqmobile.live.ThemeDetail";
	/** bundle参数key，Theme对象 */
	public static final String KEY_THEME = 		"theme";
	/** bundle参数key，Theme资源id */
	public static final String KEY_THEME_ID = 	"themeId";
	
	private ImageView 			ivBack;
	private TextView 			tvAuthor;
	private TextView 			tvSource;
	private TextView 			tvDownloadCount;
	private View 		llPreview;
	private ViewPager      		viewPager;
	private AsyncImageView[] 	mAsyncImageView;
	private ThemeDetailFooter rlDetailFooter;
	private Theme theme;
	
    private boolean 	mFromDaily;
    private boolean 	mFromPush;
    private static int 	TOTAL_COUNT;
    private int 		mShowItem = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(MResource.getIdByName(getApplication(), "layout", "nq_detail_activity"/*"nq_theme_detail_activity"*/));
		((TextView) findViewById(MResource.getIdByName(getApplication(), "id", "activity_name"))).setText(MResource.getIdByName(getApplication(),
				"string", "nq_theme_detail"));
        mFromDaily = getIntent().getBooleanExtra(LauncherSDK.KEY_FROM_DAILY,false);
        mFromPush = getIntent().getBooleanExtra(DownloadReceiver.KEY_FROM_PUSH,false);
		findViews();
		setListener();

        NqLog.i("themeId="+getIntent().getStringExtra(KEY_THEME_ID));

        if (INTENT_ACTION.equals(getIntent().getAction())) {
			// 接收参数
			String themeId = getIntent().getStringExtra(KEY_THEME_ID);
			if (!TextUtils.isEmpty(themeId)){
				getThemeDetail(themeId);
			}
			else {
                NqLog.e("themeId is null in intent");
            }
		} else {
			// 接收参数
            theme = (Theme) getIntent().getSerializableExtra(KEY_THEME);
            if (theme == null){
                NqLog.e("theme is null in intent");
            }
		}

        setView();
	}

    private void setView() {
        if(theme != null){
            if (null != theme.getStrName() && !TextUtils.isEmpty(theme.getStrName()))
                ((TextView) findViewById(MResource.getIdByName(getApplication(), "id", "activity_name"))).setText(theme.getStrName());
            tvAuthor.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_author"), theme.getStrAuthor()));
            tvSource.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_source"), theme.getStrSource()));
            if (theme.getLongDownloadCount() <= 10) {
                tvDownloadCount.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_download_count"), "10+"));
            } else {
                tvDownloadCount.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_download_count"),
                        String.valueOf(theme.getLongDownloadCount()) + "+"));
            }

            TOTAL_COUNT = theme.getArrPreviewUrl().size();
            TOTAL_COUNT = TOTAL_COUNT > 4 ? 4 : TOTAL_COUNT;
            mAsyncImageView = new AsyncImageView[TOTAL_COUNT];

            viewPager.setAdapter(new MyPagerAdapter(theme));
            // to cache all page, or we will see the right item delayed
            viewPager.setOffscreenPageLimit(TOTAL_COUNT);
            viewPager.setCurrentItem(mShowItem);
            viewPager.setPageMargin(getResources().getDimensionPixelSize(MResource
                    .getIdByName(getApplication(), "dimen", "nq_app_detail_page_margin")));
            MyOnPageChangeListener myOnPageChangeListener = new MyOnPageChangeListener();
            viewPager.setOnPageChangeListener(myOnPageChangeListener);

            llPreview.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // dispatch the events to the ViewPager, to solve the problem that we can swipe only the middle view.
                    return viewPager.dispatchTouchEvent(event);
                }
            });
        }
    }

    /**
	 * 初始化控件
	 */
	private void findViews() {
		// 初始化控件
		ivBack = (ImageView) findViewById(MResource.getIdByName(getApplication(), "id", "iv_back"));
		tvAuthor = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_author"));
		tvSource = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_source"));
		tvDownloadCount = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_download_count"));
		llPreview = findViewById(MResource.getIdByName(getApplication(), "id", "ll_preview"));
		llPreview.setMinimumWidth(DisplayUtil.getDisplayWidth(getApplicationContext()));
		viewPager = (ViewPager)findViewById(MResource.getIdByName(getApplication(), "id", "view_pager"));
		ViewGroup vGroup = (ViewGroup) findViewById(MResource.getIdByName(getApplication(), "id", "foot"));
		View root = LayoutInflater.from(getApplication()).inflate(MResource.getIdByName(getApplication(), "layout", "nq_theme_detail_footer"), null);
		vGroup.addView(root);
		rlDetailFooter = (ThemeDetailFooter) root.findViewById(MResource.getIdByName(getApplication(), "id", "rl_footer"));

        ImageView icon = (ImageView) findViewById(MResource.getIdByName(this, "id", "icon"));
        icon.setImageResource(MResource.getIdByName(this,"drawable","nq_theme_icon"));
	}
	
	private void setListener() {
		ivBack.setOnClickListener(this);
	}
	
	/**
	 * 获取Theme详情数据
	 */
	private void getThemeDetail(final String themeId) {
		theme = ThemeManager.getInstance(this).getThemeDetailFromCache(themeId);
		if (theme != null) {
			onGetDetailSucc();
		} else {
			ThemeManager.getInstance(this).getThemeDetail(themeId, this);
		}
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == MResource.getIdByName(getApplication(), "id", "iv_back")) {
            if(mFromDaily || mFromPush){
                LauncherSDK.getInstance(this).gotoStore(ThemeConstants.STORE_FRAGMENT_INDEX_THEME);
                finish();
            }else{
                finish();
            }
		}
	}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mFromPush){
            Intent i = new Intent();
            i.setClassName(getPackageName(), "com.lqsoft.launcher.LiveLauncher");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }
	
	@Override
	protected void onPause() {
		rlDetailFooter.unregisterDownloadObserver();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(theme == null)
			return;
		rlDetailFooter.init(theme);
		rlDetailFooter.registerDownloadObserver();
	}
	
	class MyPagerAdapter extends PagerAdapter {

    	private Theme theme;
    	ArrayList<String> previewUrls;
    	ArrayList<String> previewpaths;
    	
        public MyPagerAdapter(Theme theme) {
			// TODO Auto-generated constructor stub
        	this.theme = theme;
            this.previewUrls = (ArrayList<String>) theme.getArrPreviewUrl();
            this.previewpaths = (ArrayList<String>) theme.getArrPreviewPath();
		}

		@Override
        public int getCount() {
            return previewUrls.size() > 4 ? 4 : previewUrls.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
        	NqLog.i("instantiateItem position = " + position
        			+ ", previewUrls = " + previewUrls.get(position));
        	RelativeLayout appPreviewLayout = (RelativeLayout) LayoutInflater.from(getApplicationContext()).inflate(
        			MResource.getIdByName(getApplication(), "layout", "nq_theme_detail_viewpager_item"), null);
        	
        	AsyncImageView ivPreview = (AsyncImageView) appPreviewLayout.findViewById(
        			MResource.getIdByName(getApplication(), "id", "iv_preview"));
    		ProgressBar pbPreview = (ProgressBar) appPreviewLayout.findViewById(
        			MResource.getIdByName(getApplication(), "id", "loading"));
    		mAsyncImageView[position] = ivPreview;
			//临时去掉加载priview图片的优化, 因为回到主题列表时会引起图片缩放的问题
    		ivPreview.loadImage(previewpaths.get(position), previewUrls.get(position), pbPreview, MResource
                    .getIdByName(getApplication(), "drawable",
                            "nq_load_default"));
            final int index = position;
            ivPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	Intent intent = new Intent(ThemeDetailActivity.this
							.getApplicationContext(), PreviewActivity.class);
					intent.putExtra(PreviewActivity.KEY_INDEX, index);
					intent.putExtra(PreviewActivity.KEY_ID, theme.getStrId());
					intent.putExtra(PreviewActivity.KEY_PLATE, PreviewActivity.PLATE_THEME);
					intent.putStringArrayListExtra(
							PreviewActivity.KEY_PREVIEW_PATHS,
							(ArrayList<String>) theme.getArrPreviewPath());
					intent.putStringArrayListExtra(
							PreviewActivity.KEY_PREVIEW_URLS,
							(ArrayList<String>) theme.getArrPreviewUrl());
					ThemeDetailActivity.this.startActivityForResult(intent, PreviewActivity.REQUEST_CODE_THEME);

                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    		ThemeActionConstants.ACTION_LOG_1206, theme.getStrId(), 0, 
							"" + index);
                }
            });
    		container.addView(appPreviewLayout, 0);
    		
            return appPreviewLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager)container).removeView((View)object);
        }
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == PreviewActivity.REQUEST_CODE_THEME && data != null){
			mShowItem = data.getIntExtra(PreviewActivity.KEY_SHOW_ITEM, 0);
		}
	}
	
    public class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // to refresh frameLayout
        	
            if (llPreview != null) {
            	llPreview.invalidate();
            }
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
        for(int i=0; i<TOTAL_COUNT; i++){
            if(null != mAsyncImageView[i])
                mAsyncImageView[i].onHide();
        }
	}

	@Override
	public void onErr() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ToastUtils.toast(ThemeDetailActivity.this, "nq_detail_no_data");
			}
		});
	}

	@Override
	public void onGetDetailSucc(Theme theme) {
		this.theme = theme;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onGetDetailSucc();
			}
		});
	}
	
	public void onGetDetailSucc() {
		setView();
        rlDetailFooter.init(theme);
        rlDetailFooter.registerDownloadObserver();
	}
}