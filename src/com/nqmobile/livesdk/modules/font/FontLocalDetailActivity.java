package com.nqmobile.livesdk.modules.font;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
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
import android.widget.ImageView.ScaleType;

import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.receiver.DownloadReceiver;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.modules.font.table.FontLocalTable;
import com.nqmobile.livesdk.utils.DisplayUtil;
import com.nqmobile.livesdk.utils.MResource;

public class FontLocalDetailActivity extends BaseActvity implements View.OnClickListener,FontDetailListener{

	private static final ILogger NqLog = LoggerFactory.getLogger(FontModule.MODULE_NAME);
	
	public static final String INTENT_ACTION = "com.nqmobile.live.FontLocalDetail";
	/** bundle参数key，Font对象 */
	public static final String KEY_FONT = 		"Font";
	/** bundle参数key，Font资源id */
	public static final String KEY_FONT_ID = 	"fontId";
	
	private ImageView 			ivBack;
	private ImageView 			ivDelete;
	private TextView 			tvAuthor;
	private TextView 			tvSource;
	private TextView 			tvDownloadCount;
	private View 		llPreview;
	private ViewPager      		viewPager;
	private AsyncImageView[] 	mAsyncImageView;
	private FontDetailFooter rlDetailFooter;
	private NqFont font;
	
	private Dialog mDialog;
	
    private boolean 	mFromDaily;
    private boolean 	mFromPush;
    private int 	TOTAL_COUNT;
    private int 		mShowItem = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(MResource.getIdByName(getApplication(), "layout", "nq_font_local_detail_activity"));
		
		((TextView) findViewById(MResource.getIdByName(getApplication(), "id", "activity_name"))).setText(MResource.getIdByName(getApplication(),
				"string", "nq_font_detail"));
        mFromDaily = getIntent().getBooleanExtra(LauncherSDK.KEY_FROM_DAILY,false);
        mFromPush = getIntent().getBooleanExtra(DownloadReceiver.KEY_FROM_PUSH,false);
		findViews();
		setListener();
		
		
        if (INTENT_ACTION.equals(getIntent().getAction())) {
			// 接收参数
			String fontId = getIntent().getStringExtra(KEY_FONT_ID);
			if (!TextUtils.isEmpty(fontId)){
				getLocalFontDetail(fontId);
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
        	    NqLog.i("FontId="+getIntent().getStringExtra(KEY_FONT_ID));

                if (font.getArrPreviewUrl() != null && font.getArrPreviewUrl().size() >= 2){
                	List<String> arrayList = new ArrayList<String>(font.getArrPreviewUrl());
                	arrayList.remove(0);
                	font.setArrPreviewUrl(arrayList);
                }
            }
		}

        setView();
	}


    @Override
	public void onBackPressed() {
		super.onBackPressed();
//		goToFontLocalManagerActivity();
        Intent intent = null;
        intent = new Intent(this, FontLocalManagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent); 
	}

	private void setView() {
        if(font != null){
            if (null != font.getStrName() && !TextUtils.isEmpty(font.getStrName()))
                ((TextView) findViewById(MResource.getIdByName(getApplication(), "id", "activity_name"))).setText(font.getStrName());
            tvAuthor.setText("");
            tvSource.setText("");

            tvDownloadCount.setText("");

            if ( !TextUtils.equals(font.getStrId(),FontManager.getInstance(getApplicationContext()).DEFAULT_FONT_ID)){
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
	            
				if (FontManager.getInstance(getApplicationContext()).isCurrentFont(font)){
					ivDelete.setVisibility(View.GONE);
				}
	
	            llPreview.setOnTouchListener(new OnTouchListener() {
	
	                @Override
	                public boolean onTouch(View v, MotionEvent event) {
	                    // dispatch the events to the ViewPager, to solve the problem that we can swipe only the middle view.
	                    return viewPager.dispatchTouchEvent(event);
	                }
	            });
            }else{
//	            TOTAL_COUNT = 1;
//	            mAsyncImageView = new AsyncImageView[TOTAL_COUNT];
//	
//	            viewPager.setAdapter(new MyPagerAdapter(font));
//	            // to cache all page, or we will see the right item delayed
//	            viewPager.setOffscreenPageLimit(TOTAL_COUNT);
//	            viewPager.setCurrentItem(mShowItem);
//	            viewPager.setPageMargin(getResources().getDimensionPixelSize(MResource
//	                    .getIdByName(getApplication(), "dimen", "nq_app_detail_page_margin")));
//	            MyOnPageChangeListener myOnPageChangeListener = new MyOnPageChangeListener();
//	            viewPager.setOnPageChangeListener(myOnPageChangeListener);
//	
//	            llPreview.setOnTouchListener(new OnTouchListener() {
//	
//	                @Override
//	                public boolean onTouch(View v, MotionEvent event) {
//	                    // dispatch the events to the ViewPager, to solve the problem that we can swipe only the middle view.
//	                    return viewPager.dispatchTouchEvent(event);
//	                }
//	            });
	            ivDelete.setVisibility(View.GONE);
            }
        }
    }

	/**
	 * 获取Font详情数据
	 */
	private void getLocalFontDetail(final String fontId) {
		font = FontManager.getInstance(this).getFontDetailFromCache(fontId);
		if (font != null) {
			onGetDetailSucc();
		}else{
			NqLog.i("fix-me: why font is null,the fontId is " + fontId);
		}
	}
	public void onGetDetailSucc() {
		setView();
        rlDetailFooter.init(font);
        rlDetailFooter.registerDownloadObserver();
	}
    /**
	 * 初始化控件
	 */
	private void findViews() {
		// 初始化控件
		ivBack = (ImageView) findViewById(MResource.getIdByName(getApplication(), "id", "iv_back"));
		ivDelete = (ImageView) findViewById(MResource.getIdByName(getApplication(), "id", "iv_delete"));
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
		ivDelete.setOnClickListener(this);
	}
	@Override
	public void onErr() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetDetailSucc(NqFont font) {
		// TODO Auto-generated method stub
		
	}
	private void goToFontLocalManagerActivity(){
        Intent intent = null;
        intent = new Intent(this, FontLocalManagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);   
        finish();
	}
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == MResource.getIdByName(getApplication(), "id", "iv_back")) {
            if(mFromDaily || mFromPush){
                LauncherSDK.getInstance(this).gotoStore(FontConstants.STORE_FRAGMENT_INDEX_FONT);
                finish();
            }else{
            	goToFontLocalManagerActivity();
           }
		}
		else if (id == MResource.getIdByName(getApplication(), "id", "iv_delete")) {
			NqLog.i("fix-me: implement to show a confirm dialog when delete a local font file.");
			//delete an item from list and notify list
			showConfirmDeleteDialog();
		}
		else{
			NqLog.i("fix-me: why there is a so weird id : " + id);
		}		
	}
    private void showConfirmDeleteDialog(){
        View view = LayoutInflater.from(this).inflate(MResource.getIdByName(getApplication(), "layout", "nq_font_delete_dialog"), null);
        
        view.findViewById(MResource.getIdByName(getApplication(), "id", "ok")).setOnClickListener(
        		new View.OnClickListener() {
        			@Override
        			public void onClick(View view) {
        				mDialog.dismiss();
        				
        				String mSelectionClause = FontLocalTable.FONT_ID + " =?"  ;
        				int mRowsDeleted = 0;
        				NqLog.i("font.StrId" + "=" + font.getStrId());
        				mRowsDeleted = getContentResolver().delete(FontLocalTable.LOCAL_FONT_URI,mSelectionClause,new String[]{font.getStrId()}   ); 
        				NqLog.i("font.StrId" + "=" + font.getStrId() + " " + "mRowsDeleted=" + mRowsDeleted);
        				goToFontLocalManagerActivity();
        			}
        		});

        view.findViewById(MResource.getIdByName(getApplication(), "id", "cancel")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });

        mDialog = new Dialog(this, MResource.getIdByName(this,"style","Translucent_NoTitle"));
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
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
			// TODO Auto-generated constructor stub
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
        	NqLog.i("instantiateItem position = " + position
        			+ ", previewUrls = " + previewUrls.get(position));
        	RelativeLayout appPreviewLayout = (RelativeLayout) LayoutInflater.from(getApplicationContext()).inflate(
        			MResource.getIdByName(getApplication(), "layout", "nq_font_detail_viewpager_item"), null);
        	
        	AsyncImageView ivPreview = (AsyncImageView) appPreviewLayout.findViewById(
        			MResource.getIdByName(getApplication(), "id", "iv_preview"));
    		ProgressBar pbPreview = (ProgressBar) appPreviewLayout.findViewById(
        			MResource.getIdByName(getApplication(), "id", "loading"));
    		mAsyncImageView[position] = ivPreview;
			//临时去掉加载priview图片的优化, 因为回到字体列表时会引起图片缩放的问题
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
		if ( !TextUtils.equals(font.getStrId(),FontManager.getInstance(getApplicationContext()).DEFAULT_FONT_ID)){
	        for(int i=0; i<TOTAL_COUNT; i++){
	            if(null != mAsyncImageView[i])
	                mAsyncImageView[i].onHide();
	        }
		}
	}
}
