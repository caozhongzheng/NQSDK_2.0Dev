package com.nqmobile.livesdk.modules.app;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.commons.AppConstants;
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
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * 应用详情页面
 *
 * @author changxiaofei
 * @time 2013-11-21 下午5:04:13
 */
public class AppDetailActivity extends BaseActvity implements View.OnClickListener, AppDetailListener {
	private static final ILogger NqLog = LoggerFactory.getLogger(AppModule.MODULE_NAME);

	public static final String INTENT_ACTION = "com.nqmobile.live.AppDetail";
    /**
     * bundle参数key，App对象
     */
    public static final String KEY_APP = "app";
    /**
     * bundle参数key，App资源id
     */
    public static final String KEY_APP_ID = "appId";
    public static final String KEY_APP_SOURCE_TYPE = "sourceType";

    public static final String KEY_COLUMN = "column";
    
    private ImageView 			ivBack;
    private AsyncImageView 		ivIcon;
    private TextView 			tvCategory;
    private RatingBar 			rbRate;
    private TextView 			tvVersion;
    private TextView 			tvUpdatetime;
    private TextView 			tvSummary;
    private LinearLayout 		llAppPreview;
    private ViewPager      		viewPager;
    private AsyncImageView[] 	mAsyncImageView;
    private AppDetailFooter 	rlDetailFooter;
    private App 				app;
    private int 				mSourceType;
    private boolean 			mFromDaily;
    private boolean 			mFromPush;
    private int 			TOTAL_COUNT;
    private int 				mShowItem = 0;
    private int mColumn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(MResource.getIdByName(getApplication(), "layout", "nq_app_detail_activity"));
        mFromDaily = getIntent().getBooleanExtra(LauncherSDK.KEY_FROM_DAILY,false);
        mFromPush = getIntent().getBooleanExtra(DownloadReceiver.KEY_FROM_PUSH,false);
        findViews();
        setListener();

        if (INTENT_ACTION.equals(getIntent().getAction())) {
            // 接收参数
            String appId = getIntent().getStringExtra(KEY_APP_ID);
            mSourceType = getIntent().getIntExtra(KEY_APP_SOURCE_TYPE, -1);
            if (!TextUtils.isEmpty(appId)) {
                getAppDetail(appId);
            } else {
                NqLog.e("appId is null in intent");
            }
        } else {
            // 接收参数
            app = (App) getIntent().getSerializableExtra(KEY_APP);
            mSourceType = -1;
            mColumn = getIntent().getIntExtra(KEY_COLUMN,-1);
            if (app == null) {
                NqLog.e("app is null in intent");
            }
        }
    }

    /**
     * 初始化控件
     */
    private void findViews() {
        // 初始化控件
        ivBack = (ImageView) findViewById(MResource.getIdByName(getApplication(), "id", "iv_back"));
        ivIcon = (AsyncImageView) findViewById(MResource.getIdByName(getApplication(), "id", "iv_icon"));
        tvCategory = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_category"));
        rbRate = (RatingBar) findViewById(MResource.getIdByName(getApplication(), "id", "rb_rate"));
        tvVersion = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_version"));
        tvUpdatetime = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_updatetime"));
        tvSummary = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_summary"));
        llAppPreview = (LinearLayout) findViewById(MResource.getIdByName(getApplication(), "id", "ll_app_preview"));
        llAppPreview.setMinimumWidth(DisplayUtil.getDisplayWidth(getApplicationContext()));
        viewPager = (ViewPager)findViewById(MResource.getIdByName(getApplication(), "id", "view_pager"));
        rlDetailFooter = (AppDetailFooter) findViewById(MResource.getIdByName(getApplication(), "id", "rl_footer"));

        ImageView icon = (ImageView) findViewById(MResource.getIdByName(this, "id", "icon"));
        icon.setImageResource(MResource.getIdByName(this,"drawable","nq_app_icon"));
    }

    private void setListener() {
        ivBack.setOnClickListener(this);
    }

    /**
     * 获取App详情数据
     */
    private void getAppDetail(final String appId) {
        AppManager.getInstance(this).getAppDetail(appId, this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == MResource.getIdByName(getApplication(), "id", "iv_back")) {
            if(mFromDaily || mFromPush){
                LauncherSDK.getInstance(this).gotoStore(AppConstant.STORE_FRAGMENT_INDEX_APP);
                finish();
            }else{
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mSourceType == AppConstants.STORE_MODULE_TYPE_PUSH){
            Intent i = new Intent();
            i.setClassName(getPackageName(), "com.lqsoft.launcher.LiveLauncher");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(app == null)
        	return;
        NqLog.d("AppDetailActivity.onResume action=" + getIntent().getAction());
        
        if (null != app.getStrName() && !TextUtils.isEmpty(app.getStrName()))
            ((TextView) findViewById(MResource.getIdByName(getApplication(), "id", "activity_name"))).setText(app.getStrName());
        rbRate.setRating(app.getFloatRate());
        tvCategory.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_category"),
                new StringBuilder(app.getStrCategory2())));
        tvVersion.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_version"), app.getStrVersion()));
        tvUpdatetime.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_updatetime"),
                StringUtil.formatDate(app.getLongUpdateTime(), "yyyy-MM-dd")));
        tvSummary.setText(Html.fromHtml(app.getStrDescription()));
        String iconUrl = app.getStrIconUrl();
        NqLog.d("iconUrl:" + iconUrl);
        ivIcon.loadImage(iconUrl, null,
                MResource.getIdByName(this, "drawable", "nq_icon_default"));

        List<String> previewList = app.getArrPreviewUrl();
        if(previewList != null){
            TOTAL_COUNT = app.getArrPreviewUrl().size();
            TOTAL_COUNT = TOTAL_COUNT > 6 ? 6 : TOTAL_COUNT;
            mAsyncImageView = new AsyncImageView[TOTAL_COUNT];

            viewPager.setAdapter(new MyPagerAdapter(app));
            // to cache all page, or we will see the right item delayed
            viewPager.setOffscreenPageLimit(TOTAL_COUNT);
            viewPager.setCurrentItem(mShowItem);
            viewPager.setPageMargin(getResources().getDimensionPixelSize(MResource
                    .getIdByName(getApplication(), "dimen", "nq_app_detail_page_margin")));
            MyOnPageChangeListener myOnPageChangeListener = new MyOnPageChangeListener();
            viewPager.setOnPageChangeListener(myOnPageChangeListener);
        }

        rlDetailFooter.init(app);
        rlDetailFooter.registerDownloadObserver();
    }

    class MyPagerAdapter extends PagerAdapter {

    	private App app;
    	ArrayList<String> previewUrls;
    	ArrayList<String> previewpaths;
    	
        public MyPagerAdapter(App app) {
        	this.app = app;
            this.previewUrls = (ArrayList<String>) app.getArrPreviewUrl();
            this.previewpaths = (ArrayList<String>) app.getArrPreviewPath();
		}

		@Override
        public int getCount() {
            return previewUrls.size() > 6 ? 6 : previewUrls.size();
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
        			MResource.getIdByName(getApplication(), "layout", "nq_app_detail_viewpager_item"), null);
        	
        	AsyncImageView ivPreview = (AsyncImageView) appPreviewLayout.findViewById(
        			MResource.getIdByName(getApplication(), "id", "iv_preview"));
    		ProgressBar pbPreview = (ProgressBar) appPreviewLayout.findViewById(
        			MResource.getIdByName(getApplication(), "id", "loading"));
    		mAsyncImageView[position] = ivPreview;
    		ivPreview.loadImage(previewpaths.get(position), previewUrls.get(position), pbPreview, MResource
                    .getIdByName(getApplication(), "drawable",
                            "nq_load_default"));
            final int index = position;
            ivPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AppDetailActivity.this
                            .getApplicationContext(), PreviewActivity.class);
                    intent.putExtra(PreviewActivity.KEY_INDEX, index);
					intent.putExtra(PreviewActivity.KEY_ID, app.getStrId());
					intent.putExtra(PreviewActivity.KEY_PLATE, PreviewActivity.PLATE_APP);
                    intent.putStringArrayListExtra(
                            PreviewActivity.KEY_PREVIEW_PATHS,
                            (ArrayList<String>) app.getArrPreviewPath());
                    intent.putStringArrayListExtra(
                            PreviewActivity.KEY_PREVIEW_URLS,
                            (ArrayList<String>) app.getArrPreviewUrl());
                    AppDetailActivity.this.startActivityForResult(intent, PreviewActivity.REQUEST_CODE_APP);

                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    		AppActionConstants.ACTION_LOG_1108, app.getStrId(), app.getStrId().startsWith("AD_")?1:0, 
							null);
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
		if(requestCode == PreviewActivity.REQUEST_CODE_APP && data != null){
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
        	
            if (llAppPreview != null) {
            	llAppPreview.invalidate();
            }
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
    
    @Override
	protected void onStop() {
		super.onStop();
		try {
			for(int i=0; i<TOTAL_COUNT; i++){
				if(null != mAsyncImageView[i])
					mAsyncImageView[i].onHide();
			}
		} catch (ArrayIndexOutOfBoundsException ae) {
			NqLog.e(ae);
		} catch (Exception e) {
			NqLog.e(e);
		}
		
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rlDetailFooter.unregisterDownloadObserver();
    }

    @Override
    public void onErr() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	ToastUtils.toast(AppDetailActivity.this, "nq_detail_no_data");
            }
        });
    }

    @Override
    public void onGetDetailSucc(App app) {
        this.app = app;
        if (mSourceType != -1 && app != null){
        	app.setIntSourceType(mSourceType); 
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	onResume();
            }
        });
    }
}
