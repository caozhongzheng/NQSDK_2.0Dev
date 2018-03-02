package com.nqmobile.livesdk.modules.font;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nqmobile.live.store.ui.StoreMainActivity;
import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.receiver.DownloadReceiver;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.commons.ui.PreviewActivity;
import com.nqmobile.livesdk.utils.DisplayUtil;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.ToastUtils;


public class FontDetailActivity extends BaseActvity implements View.OnClickListener,FontDetailListener {
	private static final ILogger NqLog = LoggerFactory.getLogger(FontModule.MODULE_NAME);
	
	public static final String INTENT_ACTION = "com.nqmobile.live.FontDetail";
	/** bundle参数key，Font对象 */
	public static final String KEY_FONT = 		"Font";
	/** bundle参数key，Font资源id */
	public static final String KEY_FONT_ID = 	"fontId";
	
	private ImageView 			ivBack;
	private TextView 			tvAuthor;
	private TextView 			tvSource;
	private TextView 			tvDownloadCount;
	private View 		llPreview;
	private ViewPager      		viewPager;
	private AsyncImageView[] 	mAsyncImageView;
	private FontDetailFooter rlDetailFooter;
	private NqFont font;

    private boolean 	mFromDaily;
    private boolean 	mFromPush;
    private int 	TOTAL_COUNT;
    private int 		mShowItem = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(MResource.getIdByName(getApplication(), "layout", "nq_detail_activity"));
		((TextView) findViewById(MResource.getIdByName(getApplication(), "id", "activity_name"))).setText(MResource.getIdByName(getApplication(),
				"string", "nq_font_detail"));
        mFromDaily = getIntent().getBooleanExtra(LauncherSDK.KEY_FROM_DAILY,false);
        mFromPush = getIntent().getBooleanExtra(DownloadReceiver.KEY_FROM_PUSH,false);
		findViews();
		setListener();

        NqLog.i("FontId="+getIntent().getStringExtra(KEY_FONT_ID));

        if (INTENT_ACTION.equals(getIntent().getAction())) {
			// 接收参数
			String fontId = getIntent().getStringExtra(KEY_FONT_ID);
			if (!TextUtils.isEmpty(fontId)){
				getFontDetail(fontId);
			}
			else {
                NqLog.e("fontId is null in intent");
            }
		} else {
			// 接收参数
            font = (NqFont) getIntent().getSerializableExtra(KEY_FONT);

            if (font == null){
                NqLog.e("font is null in intent");
            }else{
                if (font.getArrPreviewUrl() != null && font.getArrPreviewUrl().size() >= 2){
                	List<String> arrayList = new ArrayList<String>(font.getArrPreviewUrl());
                	arrayList.remove(0);
                	font.setArrPreviewUrl(arrayList);
                }
            }
		}
        setView();
	}

    private void setView() {
        if(font != null){
            if (null != font.getStrName() && !TextUtils.isEmpty(font.getStrName()))
                ((TextView) findViewById(MResource.getIdByName(getApplication(), "id", "activity_name"))).setText(font.getStrName());
            tvAuthor.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_author"), font.getStrAuthor()));
            tvSource.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_source"), font.getStrSource()));
            if (font.getLongDownloadCount() <= 10) {
                tvDownloadCount.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_download_count"), "10+"));
            } else {
                tvDownloadCount.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_download_count"),
                        String.valueOf(font.getLongDownloadCount()) + "+"));
            }

            TOTAL_COUNT = font.getArrPreviewUrl().size();
            TOTAL_COUNT = TOTAL_COUNT > 4 ? 4 : TOTAL_COUNT;
            mAsyncImageView = new AsyncImageView[TOTAL_COUNT];

            viewPager.setAdapter(new MyPagerAdapter(font));
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
		View root = LayoutInflater.from(getApplication()).inflate(MResource.getIdByName(getApplication(), "layout", "nq_font_detail_footer"), null);
		vGroup.addView(root);
		rlDetailFooter = (FontDetailFooter) root.findViewById(MResource.getIdByName(getApplication(), "id", "rl_footer"));

        ImageView icon = (ImageView) findViewById(MResource.getIdByName(this, "id", "icon"));
        icon.setImageResource(MResource.getIdByName(this,"drawable","nq_font_selected"));
	}
	
	private void setListener() {
		ivBack.setOnClickListener(this);
	}
	
	/**
	 * 获取Font详情数据
	 */
	private void getFontDetail(final String fontId) {
		font = FontManager.getInstance(this).getFontDetailFromCache(fontId);
		if (font != null) {
			onGetDetailSucc();
		} else {
			FontManager.getInstance(this).getFontDetail(fontId, this);
		}
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == MResource.getIdByName(getApplication(), "id", "iv_back")) {
            if(mFromDaily || mFromPush){
                LauncherSDK.getInstance(this).gotoStore(FontConstants.STORE_FRAGMENT_INDEX_FONT);
                finish();
            }else{
                finish();
            }
		}
	}
	private void gotoOnlineFont(Context context){
        Intent intent = new Intent(context, StoreMainActivity.class);
        intent.putExtra(StoreMainActivity.KEY_FRAGMENT_INDEX_TO_SHOW, 3);
        intent.putExtra(StoreMainActivity.KEY_FRAGMENT_COLUMN_TO_SHOW, 0);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
	}
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mFromPush){
            Intent i = new Intent();
            i.setClassName(getPackageName(), "com.lqsoft.launcher.LiveLauncher");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }else{
        	gotoOnlineFont(getApplicationContext());
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
		if(font == null)
			return;
		rlDetailFooter.init(font);
		rlDetailFooter.registerDownloadObserver();
	}
	
	class MyPagerAdapter extends PagerAdapter {

    	private NqFont font;
    	List<String> previewUrls;
    	List<String> previewpaths;
    	
        public MyPagerAdapter(NqFont font) {
        	this.font = font;
            this.previewUrls = (List<String>) font.getArrPreviewUrl();
            this.previewpaths = (List<String>) font.getArrPreviewPath();
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
        	NqLog.i("instantiateItem position = " + position + ", previewUrls = " + previewUrls.get(position));
        	RelativeLayout appPreviewLayout = (RelativeLayout) LayoutInflater.from(getApplicationContext()).inflate(
        			MResource.getIdByName(getApplication(), "layout", "nq_font_detail_viewpager_item"), null);
        	
        	AsyncImageView ivPreview = (AsyncImageView) appPreviewLayout.findViewById(
        			MResource.getIdByName(getApplication(), "id", "iv_preview"));
    		ProgressBar pbPreview = (ProgressBar) appPreviewLayout.findViewById(
        			MResource.getIdByName(getApplication(), "id", "loading"));
    		mAsyncImageView[position] = ivPreview;
			//临时去掉加载priview图片的优化, 因为回到主题列表时会引起图片缩放的问题
    		ivPreview.setScaleType(ScaleType.FIT_CENTER);
    		ivPreview.loadImage(previewpaths.get(position), previewUrls.get(position), pbPreview, MResource
                    .getIdByName(getApplication(), "drawable","nq_load_default"));
            final int index = position;
            
            ivPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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
				ToastUtils.toast(FontDetailActivity.this, "nq_detail_no_data");
			}
		});
	}

	@Override
	public void onGetDetailSucc(NqFont font) {
		this.font = font;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onGetDetailSucc();
			}
		});
	}
	
	public void onGetDetailSucc() {
		setView();
        rlDetailFooter.init(font);
        rlDetailFooter.registerDownloadObserver();
	}
}