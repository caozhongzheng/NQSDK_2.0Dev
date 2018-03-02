package com.nqmobile.livesdk.modules.wallpaper;

import java.util.ArrayList;
import java.util.List;

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
import com.nqmobile.livesdk.utils.Tools;

/**
 * 主题详情页面
 * @author changxiaofei
 * @time 2013-11-21 下午5:04:13
 */
public class WallpaperDetailActivity extends BaseActvity implements View.OnClickListener,WallpaperDetailListener {
	private static final ILogger NqLog = LoggerFactory.getLogger(WallpaperModule.MODULE_NAME);
	
	public static final String INTENT_ACTION = "com.nqmobile.live.WallpaperDetail";
	/** bundle参数key，Wallpaper对象 */
	public static final String KEY_WALLPAPERS = "wallpapers";
	/** bundle参数key，Wallpaper位置 */
	public static final String KEY_WALLPAPER_POS = 	"wallpaper_pos";
	/** bundle参数key，Wallpaper资源id */
	public static final String KEY_WALLPAPER_ID = "wallpaperId";
	private ImageView ivBack;
	private TextView tvAuthor, tvSource, tvDownloadCount;
	private WallpaperDetailFooter rlDetailFooter;
	private View 		llPreview;
	private ViewPager      		viewPager;
	private AsyncImageView[] 	mAsyncImageView;
	private ProgressBar[] 	mProgressBar;
	private List<Wallpaper> wallpaperList = new ArrayList<Wallpaper>();
	private int currentIndex;
	private int 	TOTAL_COUNT;
    private static final int CACHE_SIZE = 5;
    private int mShowItem = 0;
    private boolean mFromDaily;
    private boolean mFromPush;    
    private Wallpaper mWallpaper;
    private String mWallpaperId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(MResource.getIdByName(getApplication(), "layout", "nq_detail_activity"/*"nq_wallpaper_detail_activity"*/));
		((TextView) findViewById(MResource.getIdByName(getApplication(), "id", "activity_name"))).setText(MResource.getIdByName(getApplication(),
				"string", "nq_wallpaper_detail"));
        mFromDaily = getIntent().getBooleanExtra(LauncherSDK.KEY_FROM_DAILY,false);
        mFromPush = getIntent().getBooleanExtra(DownloadReceiver.KEY_FROM_PUSH,false);
		findViews();
		setListener();
        processIntent(getIntent());
        setView();
    }

    private void processIntent(Intent intent) {
        NqLog.d("WallpaperDetailActivity.processIntent Action=" + intent.getAction());
        if (INTENT_ACTION.equals(intent.getAction())) {
			// 接收参数
        	mWallpaperId = getIntent().getStringExtra(KEY_WALLPAPER_ID);
			intent.getStringExtra(KEY_WALLPAPER_ID);
		} else {
			// 接收参数
			List<Wallpaper> list = (List<Wallpaper>) intent.getSerializableExtra(KEY_WALLPAPERS);
			int index = intent.getIntExtra(KEY_WALLPAPER_POS, 0);
			if (list != null && list.size() > index){
				mWallpaper = list.get(index);
				mWallpaperId = mWallpaper.getStrId();
				wallpaperList.clear();
				wallpaperList.add(mWallpaper);
				mShowItem = 0;
				currentIndex = 0;
			}
		}
	}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

	/**
	 * 初始化控件
	 */
	private void findViews() {
		// 初始化控件
		ivBack = (ImageView) findViewById(MResource.getIdByName(getApplication(), "id", "iv_back"));
		tvAuthor = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_author"));
		tvSource = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_source"));
		tvAuthor.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_author"), ""));
		tvDownloadCount = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_download_count"));
		tvDownloadCount.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_download_count"), ""));
		llPreview = findViewById(MResource.getIdByName(getApplication(), "id", "ll_preview"/*"ll_wallpaper_preview"*/));
		llPreview.setMinimumWidth(DisplayUtil.getDisplayWidth(getApplicationContext()));
		viewPager = (ViewPager)findViewById(MResource.getIdByName(getApplication(), "id", "view_pager"));
		ViewGroup vGroup = (ViewGroup) findViewById(MResource.getIdByName(getApplication(), "id", "foot"));
		View root = LayoutInflater.from(getApplication()).inflate(MResource.getIdByName(getApplication(), "layout", "nq_wallpaper_detail_footer"), null);
		vGroup.addView(root);
		rlDetailFooter = (WallpaperDetailFooter) findViewById(MResource.getIdByName(getApplication(), "id", "rl_footer"));

        ImageView icon = (ImageView) findViewById(MResource.getIdByName(this, "id", "icon"));
        icon.setImageResource(MResource.getIdByName(this,"drawable","nq_wallpaper_icon"));
	}
	
	private void setListener() {
		ivBack.setOnClickListener(this);
	}
	
	/**
	 * 获取Wallpaper详情数据
	 */
	private void getWallpaperDetail() {
        int index = getIndex(mWallpaperId);
        if(index != -1){
            mShowItem = index;
            currentIndex = index;
            viewPager.setCurrentItem(mShowItem);
        }else{
            Wallpaper wallpaper = WallpaperManager.getInstance(this).getWallpaperDetailFromLocal(mWallpaperId);
		if (wallpaper != null) {
                if(wallpaperList == null){
                    wallpaperList = new ArrayList<Wallpaper>();
                }
                wallpaperList.clear();
			wallpaperList.add(wallpaper);
			mWallpaper = wallpaper;
			onGetDetailSucc();
		} else {
                WallpaperManager.getInstance(this).getWallpaperDetail(mWallpaperId,this);
            }
        }
	}
    private int getIndex(String wallpaperId){
        int result = -1;
        if(wallpaperList != null && wallpaperList.size() > 0){
            for(int i=0;i<wallpaperList.size();i++){
                if(wallpaperList.get(i).getStrId().equals(wallpaperId)){
                    result = i;
                    break;
		}
            }
        }
        return result;
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == MResource.getIdByName(getApplication(), "id", "iv_back")) {
            if(mFromDaily || mFromPush){
                LauncherSDK.getInstance(this).gotoStore(WallpaperConstants.STORE_FRAGMENT_INDEX_WALLPAPER);
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
		
		if (mWallpaper == null) {
			getWallpaperDetail();
		} else {
			rlDetailFooter.init(mWallpaper);
			rlDetailFooter.registerDownloadObserver();
		}
		
	}
	private void setView() {
		if(wallpaperList == null || wallpaperList.isEmpty())
			return;
		NqLog.d("WallpaperDetailActivity.onResume action=" + getIntent().getAction() + ", currentIntdex=" + currentIndex);
		Wallpaper wallpaper = wallpaperList.get(currentIndex);
		if(null != wallpaper.getStrName() && !TextUtils.isEmpty(wallpaper.getStrName()))
			((TextView) findViewById(MResource.getIdByName(getApplication(), "id", "activity_name"))).setText(wallpaper.getStrName());
		tvAuthor.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_author"), wallpaper.getStrAuthor()));
		tvSource.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_source"), wallpaper.getStrSource()));

		if (wallpaper.getLongDownloadCount() <= 10) {
			tvDownloadCount.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_download_count"), "10+"));
		} else {
			tvDownloadCount.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_download_count"),
					String.valueOf(wallpaper.getLongDownloadCount()) + "+"));
		}
		
		TOTAL_COUNT = wallpaperList.size();
		mAsyncImageView = new AsyncImageView[TOTAL_COUNT];
		mProgressBar = new ProgressBar[TOTAL_COUNT];
		
		viewPager.setAdapter(new MyPagerAdapter(wallpaperList));
        // to cache all page, or we will see the right item delayed
        viewPager.setOffscreenPageLimit(CACHE_SIZE);
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
	
	class MyPagerAdapter extends PagerAdapter {

    	private List<Wallpaper> wallpaperList;
    	ArrayList<String> previewUrls;
    	ArrayList<String> previewpaths;
    	
        public MyPagerAdapter(List<Wallpaper> wallpaperList) {
			// TODO Auto-generated constructor stub
        	this.wallpaperList = wallpaperList;
            this.previewUrls = getPreviewUrls(wallpaperList);
            this.previewpaths = getPreviewPaths(wallpaperList);
		}

		private ArrayList<String> getPreviewPaths(List<Wallpaper> wallpaperList2) {
			// TODO Auto-generated method stub
			ArrayList<String> list = null;
			if(wallpaperList2 == null || wallpaperList2.isEmpty())
				return list;
			list = new ArrayList<String>();
			for(int i=0; i<wallpaperList2.size(); i++){
				list.add(wallpaperList2.get(i).getPreviewPicturePath());
			}
			return list;
		}

		private ArrayList<String> getPreviewUrls(List<Wallpaper> wallpaperList2) {
			// TODO Auto-generated method stub
			ArrayList<String> list = null;
			if(wallpaperList2 == null || wallpaperList2.isEmpty())
				return list;
			list = new ArrayList<String>();
			for(int i=0; i<wallpaperList2.size(); i++){
				list.add(wallpaperList2.get(i).getPreviewPicture());
			}
			return list;
		}

		@Override
        public int getCount() {
            return wallpaperList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
        	
        	RelativeLayout appPreviewLayout = (RelativeLayout) LayoutInflater.from(getApplicationContext()).inflate(
        			MResource.getIdByName(getApplication(), "layout", "nq_wallpaper_detail_viewpager_item"), null);
        	
        	AsyncImageView ivPreview = (AsyncImageView) appPreviewLayout.findViewById(
        			MResource.getIdByName(getApplication(), "id", "iv_preview"));
    		ProgressBar pbPreview = (ProgressBar) appPreviewLayout.findViewById(
        			MResource.getIdByName(getApplication(), "id", "loading"));
    		mAsyncImageView[position] = ivPreview;
    		mProgressBar[position] = pbPreview;
    		if(Math.abs(position - currentIndex) <2){
    			ivPreview.setType(AsyncImageView.WALLPAPER_DETAIL);
    			String iconUrl = wallpaperList.get(position).getStrIconUrl();
    			iconUrl = Tools.isEmpty(iconUrl) ? previewUrls.get(position): iconUrl;
    			NqLog.i("instantiateItem position = " + position
            			+ ", previewUrl = " + previewUrls.get(position));
    			//临时去掉加载priview图片的优化, 因为回到wallpapter列表时会引起图片缩放的问题
    			ivPreview.loadImage(previewpaths.get(position), previewUrls.get(position), pbPreview, MResource
    					.getIdByName(getApplication(), "drawable",
    							"nq_load_default"));
    		}

            final int index = position;
            ivPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	Intent intent = new Intent(WallpaperDetailActivity.this, PreviewActivity.class);
					intent.putExtra(PreviewActivity.KEY_INDEX, index);
					intent.putExtra(PreviewActivity.KEY_ID, wallpaperList.get(index).getStrId());
					intent.putExtra(PreviewActivity.KEY_PLATE, PreviewActivity.PLATE_WALLPAPER);
					intent.putStringArrayListExtra(
							PreviewActivity.KEY_PREVIEW_PATHS, previewpaths);
					intent.putStringArrayListExtra(
							PreviewActivity.KEY_PREVIEW_URLS, previewUrls);
					WallpaperDetailActivity.this.startActivity(intent);

                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    		WallpaperActionConstants.ACTION_LOG_1305, wallpaperList.get(index).getStrId(), 0, 
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
		if(requestCode == PreviewActivity.REQUEST_CODE_WALLPAPER && data!=null){
			mShowItem = data.getIntExtra(PreviewActivity.KEY_SHOW_ITEM, 0);
		}
	}
	
    public class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
            		WallpaperActionConstants.ACTION_LOG_1306, wallpaperList.get(position).getStrId(), 0, null);
        	showNextWallpaper(position);
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
    
    int lastPos = 0;
	public void showNextWallpaper(int position) {
		// TODO Auto-generated method stub
		currentIndex = position;
		NqLog.i("showNextWallpaper pos= " + position + ",currentIndex= " + currentIndex);
		
		if(wallpaperList == null || wallpaperList.isEmpty())
			return;
		NqLog.d("WallpaperDetailActivity.showNextWallpaper action=" + getIntent().getAction() + ", currentIndex=" +currentIndex);
		mWallpaper = wallpaperList.get(currentIndex);
		mWallpaperId = mWallpaper.getStrId();
		if(null != mWallpaper.getStrName() && !TextUtils.isEmpty(mWallpaper.getStrName()))
			((TextView) findViewById(MResource.getIdByName(getApplication(), "id", "activity_name"))).setText(mWallpaper.getStrName());
		tvAuthor.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_author"), mWallpaper.getStrAuthor()));
		tvSource.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_source"), mWallpaper.getStrSource()));
		if (mWallpaper.getLongDownloadCount() <= 10) {
			tvDownloadCount.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_download_count"), "10+"));
		} else {
			tvDownloadCount.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_download_count"),
					String.valueOf(mWallpaper.getLongDownloadCount()) + "+"));
		}
		for(int i=(position-1<0 ? 0 : position-1); i<(position+2>TOTAL_COUNT ? TOTAL_COUNT : position+2); i++){
			try {
				mAsyncImageView[i].setType(AsyncImageView.WALLPAPER_DETAIL);
				mAsyncImageView[i].loadImage(wallpaperList.get(i).getPreviewPicturePath(), wallpaperList.get(i).getPreviewPicture()
						, mProgressBar[i], MResource
		                .getIdByName(getApplication(), "drawable",
		                        "nq_load_default"));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
		}

		rlDetailFooter.init(mWallpaper);
		rlDetailFooter.registerDownloadObserver();
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
				ToastUtils.toast(WallpaperDetailActivity.this, "nq_detail_no_data");
			}
		});
	}

	@Override
	public void onGetDetailSucc(Wallpaper wallpaper) {
		wallpaperList.clear();
		wallpaperList.add(wallpaper);
		mWallpaper = wallpaper;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onGetDetailSucc();
			}
		});
	}
	
	public void onGetDetailSucc() {
		setView();
		rlDetailFooter.init(mWallpaper);
		rlDetailFooter.registerDownloadObserver();
	}
}
