package com.nqmobile.livesdk.commons.ui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.modules.app.AppActionConstants;
import com.nqmobile.livesdk.modules.locker.LockerActionConstants;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.modules.theme.ThemeActionConstants;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperActionConstants;
import com.nqmobile.livesdk.utils.MResource;

/**
 * 应用预览图片展示页面
 * @author changxiaofei
 * @time 2013-11-21 下午5:04:13
 */
public class PreviewActivity extends BaseActvity implements View.OnClickListener {
	/** bundle参数key，预览图显示index*/
	public static final String KEY_INDEX = "index";
	/** bundle参数key，预览图资源ID*/
	public static final String KEY_ID = "id";
	/** bundle参数key，预览图资源类别*/
	public static final String KEY_PLATE = "plate";
	/** setResult参数*/
	public static final String KEY_SHOW_ITEM = "show_item";
	public static final int REQUEST_CODE_APP = 101;
	public static final int REQUEST_CODE_THEME = 102;
	public static final int REQUEST_CODE_WALLPAPER = 103;
	public static final int REQUEST_CODE_LOCKER = 104;
	
	public static final int PLATE_APP = 0;
	public static final int PLATE_THEME = 1;
	public static final int PLATE_WALLPAPER = 2;
	public static final int PLATE_LOCKER = 3;
	
	/** bundle参数key，预览图本地路径 */
	public static final String KEY_PREVIEW_PATHS = "preview_paths";
	/** bundle参数key，预览图URL */
	public static final String KEY_PREVIEW_URLS = "preview_urls";
	
	private ViewPager viewPager;
	private MyPagerAdapter pagerAdapter;
	private int currentItem = 0; // 当前要显示的位置
	private int plate = 0;
	private String resId = "";
	private List<String> previewPaths = null;
	private List<String> previewUrls = null;
	private Bitmap[] bmpPreviews = null;
	private Intent mIntent;
    private View mView;
    private LinearLayout mLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  //全屏
        mView = getLayoutInflater().from(this).inflate(MResource.getIdByName(getApplication(), "layout", "nq_preview_activity"), null);
        hideNavi();

        setContentView(mView);
		mIntent = new Intent();
		mIntent.putExtra(KEY_SHOW_ITEM, getIntent().getIntExtra(KEY_INDEX, 0));
		setResult(Activity.RESULT_OK, mIntent);
		findViews();
		setListener();
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void hideNavi() {
		// TODO Auto-generated method stub
		mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}

	/**
	 * 初始化控件
	 */
	private void findViews() {
		viewPager = (ViewPager) findViewById(MResource.getIdByName(getApplication(), "id", "vp_preview"));
		mLayout = (LinearLayout) mView.findViewById(MResource.getIdByName(getApplication(), "id", "ll_preview"));
	}
	
	private void setListener() {
		mLayout.setOnClickListener(this);
	}
	
	class MyPagerAdapter extends PagerAdapter {
		private Context context;
		private List<String> previewUrls;
		private List<String> previewPaths;
		
		
		public MyPagerAdapter(Context context, List<String> previewUrls, List<String> previewPaths) {
			super();
			this.context = context;
			if (previewUrls == null) {
				this.previewUrls = new ArrayList<String>();
			} else {
				this.previewUrls = previewUrls;
			}
			if (previewPaths == null) {
				this.previewPaths = new ArrayList<String>();
			} else {
				this.previewPaths = previewPaths;
			}
		}
		
		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}
		
		@Override
		public int getCount() {
			return previewUrls.size();
		}
		
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}
		
		@Override
		public Object instantiateItem(final View view, int position) {
            NqLog.d("PreviewActivity.MyPagerAdapter.instantiateItem position=" + position);
            View imageLayout = LayoutInflater.from(context).inflate(
					MResource.getIdByName(getApplication(), "layout", "nq_preview_viewpager_item"), null);
			final AsyncImageView imageView = (AsyncImageView) imageLayout.findViewById(MResource.getIdByName(getApplication(), "id", "image"));
			final ProgressBar loading = (ProgressBar) imageLayout.findViewById(MResource.getIdByName(getApplication(), "id", "loading"));
			final int index = position;
			imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mIntent.putExtra(KEY_SHOW_ITEM, index);
					setResult(Activity.RESULT_OK, mIntent);
					NqLog.i("instantiateItem  finish PreviewActivity after setResult " + System.currentTimeMillis());
					PreviewActivity.this.finish();
				}
			});
			
			imageView.loadImage(previewPaths.get(position), previewUrls.get(position), loading, 
					MResource.getIdByName(context, "drawable", "nq_preview_default"));
			
			((ViewPager) view).addView(imageLayout, 0);
			return imageLayout;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		previewPaths = (List<String>) getIntent().getSerializableExtra(KEY_PREVIEW_PATHS);
		previewUrls = (List<String>) getIntent().getSerializableExtra(KEY_PREVIEW_URLS);
		if(previewPaths!=null  && previewUrls!=null && previewPaths.size()==previewUrls.size()){
			bmpPreviews = new Bitmap[previewPaths.size()];
			pagerAdapter = new MyPagerAdapter(this, previewUrls, previewPaths);
		}else{
			bmpPreviews = new Bitmap[0];
			pagerAdapter = new MyPagerAdapter(this, null, null);
		}
		currentItem = getIntent().getIntExtra(KEY_INDEX, 0);
		resId = getIntent().getStringExtra(KEY_ID);
		plate = getIntent().getIntExtra(KEY_PLATE, 0);
		// 初始化UI
		viewPager.setAdapter(pagerAdapter);
		viewPager.setCurrentItem(currentItem);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				switch(plate){
				case PLATE_APP:
                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    		AppActionConstants.ACTION_LOG_1109, resId, 0, 
							null);
					break;
				case PLATE_THEME:
                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    		ThemeActionConstants.ACTION_LOG_1207, resId, 0, 
							null);
					break;
				case PLATE_WALLPAPER:
                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    		WallpaperActionConstants.ACTION_LOG_1307, resId, 0, 
							null);
					break;
				case PLATE_LOCKER:
                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
							LockerActionConstants.ACTION_LOG_2107, resId, 0, 
							null);
					break;
				}
				
				currentItem = position;
				mIntent.putExtra(KEY_SHOW_ITEM, position);
				setResult(Activity.RESULT_OK, mIntent);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mIntent.putExtra(KEY_SHOW_ITEM, viewPager.getCurrentItem());
		setResult(Activity.RESULT_OK, mIntent);
	}

	@Override
	protected void onDestroy() {
		if (bmpPreviews != null && bmpPreviews.length>0) {
			for (int i = 0; i < bmpPreviews.length; i++) {
				if (bmpPreviews[i] != null) {
					try {
						bmpPreviews[i].recycle();
					} catch (Exception e) {
						e.printStackTrace();
					}
					bmpPreviews[i] = null;
				}
			}
			bmpPreviews = null;
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		mIntent.putExtra(KEY_SHOW_ITEM, currentItem);
		setResult(Activity.RESULT_OK, mIntent);
		NqLog.i("onClick  finish PreviewActivity after setResult");
		PreviewActivity.this.finish();
	}
}
