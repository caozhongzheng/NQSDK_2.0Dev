package com.nqmobile.livesdk.modules.locker;

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
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.DisplayUtil;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * 锁屏详情页面
 * @author caozhongzheng
 * @time 2014/5/8 14:00:46
 */
public class LockerDetailActivity extends BaseActvity implements View.OnClickListener,LockerDetailListener {
	private static final ILogger NqLog = LoggerFactory.getLogger(LockerModule.MODULE_NAME);
	
	public static final String TAG = "LockerDetailActivity";
	public static final String INTENT_ACTION = "com.nqmobile.live.LockerDetail";
	/** bundle参数key，Locker对象 */
	public static final String KEY_LOCKERS = 		"lockers";
	/** bundle参数key，Locker位置 */
	public static final String KEY_LOCKER_POS = 	"locker_pos";
	/** bundle参数key，Locker资源id */
	public static final String KEY_LOCKER_ID = 		"lockerId";
	
	private ImageView 			ivBack;
	private TextView 			tvAuthor;
//	private TextView 			tvSource;
	private TextView 			tvDownloadCount;
	private View 		llPreview;
	private ViewPager      		viewPager;
	private AsyncImageView[] 	mAsyncImageView;
	private LockerDetailFooter rlDetailFooter;
//	private Locker locker;
	private List<Locker> lockerList;
	private int currentIndex;
	
    private boolean 	mFromDaily;
    private boolean 	mFromPush;
    private static int 	TOTAL_COUNT;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(MResource.getIdByName(getApplication(), "layout", "nq_detail_activity"/*"nq_locker_detail_activity"*/));
		((TextView) findViewById(MResource.getIdByName(getApplication(), "id", "activity_name"))).setText(MResource.getIdByName(getApplication(),
				"string", "nq_locker_detail"));
        mFromDaily = getIntent().getBooleanExtra(LauncherSDK.KEY_FROM_DAILY,false);
        mFromPush = getIntent().getBooleanExtra(DownloadReceiver.KEY_FROM_PUSH,false);
		findViews();
		setListener();
		
		if (INTENT_ACTION.equals(getIntent().getAction())) {
			// 接收参数
			String lockerId = getIntent().getStringExtra(KEY_LOCKER_ID);
			if (!TextUtils.isEmpty(lockerId)){
				getLockerDetail(lockerId);
			}
			else {
                NqLog.e("lockerId is null in intent");
            }
		} else {
			// 接收参数
			lockerList = (List<Locker>) getIntent().getSerializableExtra(KEY_LOCKERS);
			currentIndex = getIntent().getIntExtra(KEY_LOCKER_POS, 0);
            if (lockerList == null || lockerList.isEmpty()){
                NqLog.e("lockerList is null or empty in intent");
            } else {
            	NqLog.i("lokerList size is " + lockerList.size() + ", pos is " + currentIndex);
            }
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void findViews() {
		// 初始化控件
		ivBack = (ImageView) findViewById(MResource.getIdByName(getApplication(), "id", "iv_back"));
		tvAuthor = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_author"));
//		tvSource = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_source"));
		tvDownloadCount = (TextView) findViewById(MResource.getIdByName(getApplication(), "id", "tv_download_count"));
		llPreview = findViewById(MResource.getIdByName(getApplication(), "id", "ll_preview"/*"ll_locker_preview"*/));
		llPreview.setMinimumWidth(DisplayUtil.getDisplayWidth(getApplicationContext()));
		viewPager = (ViewPager)findViewById(MResource.getIdByName(getApplication(), "id", "view_pager"));
		ViewGroup vGroup = (ViewGroup) findViewById(MResource.getIdByName(getApplication(), "id", "foot"));
		View root = LayoutInflater.from(getApplication()).inflate(MResource.getIdByName(getApplication(), "layout", "nq_locker_detail_footer"), null);
		vGroup.addView(root);
		rlDetailFooter = (LockerDetailFooter) findViewById(MResource.getIdByName(getApplication(), "id", "rl_footer"));

        ImageView icon = (ImageView) findViewById(MResource.getIdByName(this, "id", "icon"));
        icon.setImageResource(MResource.getIdByName(this,"drawable","nq_locker_icon"));
	}
	
	private void setListener() {
		ivBack.setOnClickListener(this);
	}
	
	/**
	 * 获取Locker详情数据, 从每日推荐过来的
	 */
	private void getLockerDetail(final String lockerId) {
		Locker locker = LockerManager.getInstance(this).getLockerDetailFromCache(lockerId);
		if (locker != null) {
			lockerList.add(locker);
			onGetDetailSucc();
		} else {
			LockerManager.getInstance(this).getLockerDetail(lockerId, this);
		}
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == MResource.getIdByName(getApplication(), "id", "iv_back")) {
            if(mFromDaily || mFromPush){
                LauncherSDK.getInstance(this).gotoStore(LockerConstants.STORE_FRAGMENT_INDEX_LOCKER);
                finish();
            }else{
                finish();
            }
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
		if(lockerList == null || lockerList.isEmpty())
			return;
		NqLog.d("LockerDetailActivity.onResume action=" + getIntent().getAction() + ", currentIndex=" +currentIndex);
		Locker locker = lockerList.get(currentIndex);
		if(null != locker.getStrName() && !TextUtils.isEmpty(locker.getStrName()))
			((TextView) findViewById(MResource.getIdByName(getApplication(), "id", "activity_name"))).setText(locker.getStrName());
		tvAuthor.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_author"), locker.getStrAuthor()));
//		tvSource.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_source"), locker.getStrSource()));
		if (locker.getLongDownloadCount() <= 10) {
			tvDownloadCount.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_download_count"), "10+"));
		} else {
			tvDownloadCount.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_download_count"),
					String.valueOf(locker.getLongDownloadCount()) + "+"));
		}
		
		TOTAL_COUNT = lockerList.size();
        mAsyncImageView = new AsyncImageView[TOTAL_COUNT];
        
        viewPager.setAdapter(new MyPagerAdapter(lockerList));
        // to cache all page, or we will see the right item delayed
        viewPager.setOffscreenPageLimit(TOTAL_COUNT);
        viewPager.setCurrentItem(currentIndex);
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
        rlDetailFooter.init(locker);
		rlDetailFooter.registerDownloadObserver();
	}
	
	class MyPagerAdapter extends PagerAdapter {

    	private List<Locker> lockerList;
    	ArrayList<String> previewUrls;
    	ArrayList<String> previewpaths;
    	
        public MyPagerAdapter(List<Locker> lockerList) {
			// TODO Auto-generated constructor stub
        	this.lockerList = lockerList;
            this.previewUrls = getPreviewUrls(lockerList);
            this.previewpaths = getPreviewPaths(lockerList);
		}

		private ArrayList<String> getPreviewPaths(List<Locker> lockerList2) {
			// TODO Auto-generated method stub
			ArrayList<String> list = null;
			if(lockerList2 == null || lockerList2.isEmpty())
				return list;
			list = new ArrayList<String>();
			for(int i=0; i<lockerList2.size(); i++){
				list.add(lockerList2.get(i).getStrPreviewPath());
			}
			return list;
		}

		private ArrayList<String> getPreviewUrls(List<Locker> lockerList2) {
			// TODO Auto-generated method stub
			ArrayList<String> list = null;
			if(lockerList2 == null || lockerList2.isEmpty())
				return list;
			list = new ArrayList<String>();
			for(int i=0; i<lockerList2.size(); i++){
				list.add(lockerList2.get(i).getStrPreviewUrl());
			}
			return list;
		}

		@Override
        public int getCount() {
            return lockerList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
        	NqLog.i("instantiateItem position = " + position
        			+ ", previewUrl = " + previewUrls.get(position));
        	RelativeLayout appPreviewLayout = (RelativeLayout) LayoutInflater.from(getApplicationContext()).inflate(
        			MResource.getIdByName(getApplication(), "layout", "nq_locker_detail_viewpager_item"), null);
        	
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
                	Intent intent = new Intent(LockerDetailActivity.this
							.getApplicationContext(), PreviewActivity.class);
					intent.putExtra(PreviewActivity.KEY_INDEX, index);
					intent.putExtra(PreviewActivity.KEY_ID, lockerList.get(index).getStrId());
					intent.putExtra(PreviewActivity.KEY_PLATE, PreviewActivity.PLATE_LOCKER);
					intent.putStringArrayListExtra(
							PreviewActivity.KEY_PREVIEW_PATHS, previewpaths);
					intent.putStringArrayListExtra(
							PreviewActivity.KEY_PREVIEW_URLS, previewUrls);
					LockerDetailActivity.this.startActivityForResult(intent, PreviewActivity.REQUEST_CODE_LOCKER);

                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
							LockerActionConstants.ACTION_LOG_2106, lockerList.get(index).getStrId(), lockerList.get(index).getStrId().startsWith("AD_")?1:0, 
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
		// TODO Auto-generated method stub
		if(requestCode == PreviewActivity.REQUEST_CODE_LOCKER){
			currentIndex = data.getIntExtra(PreviewActivity.KEY_SHOW_ITEM, 0);
		}
	}
	
    public class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
					LockerActionConstants.ACTION_LOG_2105, lockerList.get(position).getStrId(), 0, null);
        	showNextLocker(position);
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
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		for(int i=0; i<TOTAL_COUNT; i++){
			if(null != mAsyncImageView[i])
				mAsyncImageView[i].onHide();
		}
	}

	int lastPos = 0;
	public void showNextLocker(int position) {
		// TODO Auto-generated method stub
//		if(lastPos != position){
//			currentIndex += (position - lastPos);
//			lastPos = position;
//		}
		currentIndex = position;
		NqLog.i("showNextLocker pos= " + position + ",currentIndex= " + currentIndex);
		
		if(lockerList == null || lockerList.isEmpty())
			return;
		NqLog.d("LockerDetailActivity.showNextLocker action=" + getIntent().getAction() + ", currentIndex=" +currentIndex);
		Locker locker = lockerList.get(currentIndex);
		if(null != locker.getStrName() && !TextUtils.isEmpty(locker.getStrName()))
			((TextView) findViewById(MResource.getIdByName(getApplication(), "id", "activity_name"))).setText(locker.getStrName());
		tvAuthor.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_author"), locker.getStrAuthor()));
//		tvSource.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_source"), locker.getStrSource()));
		if (locker.getLongDownloadCount() <= 10) {
			tvDownloadCount.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_download_count"), "10+"));
		} else {
			tvDownloadCount.setText(getString(MResource.getIdByName(getApplication(), "string", "nq_detail_download_count"),
					String.valueOf(locker.getLongDownloadCount()) + "+"));
		}
		
		rlDetailFooter.init(locker);
		rlDetailFooter.registerDownloadObserver();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onErr() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ToastUtils.toast(LockerDetailActivity.this, "nq_detail_no_data");
			}
		});
	}

	@Override
	public void onGetDetailSucc(Locker locker) {
		this.lockerList.add(locker);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onGetDetailSucc();
			}
		});
	}
	
	public void onGetDetailSucc() {
		onResume();
	}
}