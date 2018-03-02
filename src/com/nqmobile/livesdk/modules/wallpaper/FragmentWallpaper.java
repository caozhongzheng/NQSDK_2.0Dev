package com.nqmobile.livesdk.modules.wallpaper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView.RecyclerListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.commons.ui.MoveBackView;
import com.nqmobile.livesdk.commons.ui.PullToRefreshListView;
import com.nqmobile.livesdk.modules.banner.BannerConstants;
import com.nqmobile.livesdk.modules.banner.BannerUI;
import com.nqmobile.livesdk.modules.points.PointActionConstants;
import com.nqmobile.livesdk.modules.points.PointsCenterActivity;
import com.nqmobile.livesdk.modules.points.PointsPreference;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.AnimationUtil;
import com.nqmobile.livesdk.utils.DisplayUtil;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * 壁纸主页
 * @author changxiaofei
 * @time 2013-11-18 下午4:53:18
 */
public class FragmentWallpaper extends Fragment implements View.OnClickListener {
	private static final ILogger NqLog = LoggerFactory.getLogger(WallpaperModule.MODULE_NAME);
	
	private static final int COLUMN_COUNT = 2;//2个栏目
	private static final int CURSOR_ANIMATION_MILLISECONDS = 1; 
	private static final int ITEMS_PER_ROW = 3; //每行3个壁纸icon
	//private static final int WHAT_GET_BANNER_LIST = 1;
//	private static final int WHAT_GET_WALLPAPER_LIST = 2;
	private Context context;
	/*banner*/
	private LinearLayout mBannerLayout;
	private BannerUI mBannerUI;
	private RelativeLayout mBanner;
    private RelativeLayout mTitle;
	//
	private ViewPager viewPager;
	private LinearLayout[] mLoadFailedLayout;
	private MoveBackView[] mLoadingView;
	private ImageView[] mNoNetworkView;
	private ImageView[] mEmptyView;
	private TextView tvColumnNew, tvColumnTop;
	private ImageView ivCursor;
	private int screenWidth;
	private int cursorWidth;
	private int cursorLastX;
	private int cursorPadingPx;
	private RelativeLayout.LayoutParams lpCursor;
	// 数据
	private PullToRefreshListView[] mListView;
	private WallpaperArrListAdapter[] mWallpaperArrListAdapters;
	private int[] visibleLastIndex = {0, 0};
	private int currIndex = 0;// 壁纸列表ViewPager当前页卡编号
	private boolean[] loadedFlags;
	private boolean[] isScrolling;
	private int scrollBy;
	// 行为日志
 	private Map<String, Boolean> mActionLogs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		NqLog.d("onCreate");
		super.onCreate(savedInstanceState);
		context = getActivity().getApplicationContext();
		cursorPadingPx = DisplayUtil.dip2px(context, 6);
		scrollBy = DisplayUtil.dip2px(context, 10);
		mActionLogs = new ConcurrentHashMap<String, Boolean>();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		NqLog.d("onCreateView");
		View wallpaperView = inflater.inflate(MResource.getIdByName(context, "layout", "nq_fragment_wallpaper"), container, false);
		// Banner控件
		mBannerLayout = (LinearLayout) wallpaperView.findViewById(MResource.getIdByName(context, "id", "bannerLayout"));
		mBannerUI = new BannerUI(mBannerLayout, new Handler(), BannerConstants.STORE_PLATE_TYPE_WALLPAPER);
		mBanner = (RelativeLayout) wallpaperView.findViewById(MResource.getIdByName(context, "id", "rl_banner"));
		mBannerUI.enableAutoSlide();
		//初始化栏目指示器
		ivCursor = (ImageView) wallpaperView.findViewById(MResource.getIdByName(context, "id", "tv_cursor"));
		screenWidth = context.getResources().getDisplayMetrics().widthPixels;//获取分辨率宽度
		cursorWidth = (int) (screenWidth/2 + 0.5f);
		lpCursor = new RelativeLayout.LayoutParams(cursorWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		ivCursor.setPadding(2*cursorPadingPx, 0, cursorPadingPx, 0);
		ivCursor.setLayoutParams(lpCursor);
		//初始化栏目
		tvColumnNew = (TextView) wallpaperView.findViewById(MResource.getIdByName(context, "id", "tv_column_new_wallpapers"));
		tvColumnNew.setTextColor(getResources().getColor(MResource.getIdByName(context, "color", "nq_text_store_column_title_selected")));
		tvColumnTop = (TextView) wallpaperView.findViewById(MResource.getIdByName(context, "id", "tv_column_top_wallpapers"));
		// 初始化栏目列表
		viewPager = (ViewPager) wallpaperView.findViewById(MResource.getIdByName(context, "id", "vp_list"));
		viewPager.setAdapter(new WallpaperPagerAdapter());
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new WallpaperOnPageChangeListener());
		
		mLoadFailedLayout = new LinearLayout[COLUMN_COUNT];
		mLoadingView = new MoveBackView[COLUMN_COUNT];
		mNoNetworkView = new ImageView[COLUMN_COUNT];
		mEmptyView = new ImageView[COLUMN_COUNT];
		
		mWallpaperArrListAdapters = new WallpaperArrListAdapter[COLUMN_COUNT];
		for(int i=0; i<COLUMN_COUNT; i++){
			mWallpaperArrListAdapters[i] = new WallpaperArrListAdapter();
		}
		mListView = new PullToRefreshListView[COLUMN_COUNT];

		loadedFlags = new boolean[2];
		isScrolling = new boolean[2];
		tvColumnNew.setOnClickListener(this);
		tvColumnTop.setOnClickListener(this);

        mTitle = (RelativeLayout) wallpaperView.findViewById(MResource.getIdByName(context,"id","title_layout"));
        mTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mListView[currIndex].setSelection(1);
            }
        });

        ImageView pointCenter = (ImageView) wallpaperView.findViewById(MResource.getIdByName(context,"id","pointCenter"));
        pointCenter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, PointsCenterActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);

                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION, PointActionConstants.ACTION_LOG_2501,null,0,null);
            }
        });

        if(PointsPreference.getInstance().getBooleanValue(PointsPreference.KEY_POINT_CENTER_ENABLE)){
            pointCenter.setVisibility(View.VISIBLE);
        }

        if(PointsPreference.getInstance().getBooleanValue(PointsPreference.KEY_POINT_CENTER_ENABLE)){
            pointCenter.setVisibility(View.VISIBLE);
        }else{
            pointCenter.setVisibility(View.GONE);
        }

		return wallpaperView;
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		NqLog.d("FragmentWallpaper.onHiddenChanged " + isHidden());
        if (isHidden()){
			hideColumn(0);
			hideColumn(1);
			mBannerUI.onHide();
			System.gc();
		} else {
			showColumn(viewPager.getCurrentItem());
			mBannerUI.onShow();
		}
		super.onHiddenChanged(hidden);
	}
	
	/**
	 * 栏目切换PagerAdapter
	 */
	public class WallpaperPagerAdapter extends PagerAdapter {
		
		public WallpaperPagerAdapter() { }
		
		@Override
		public int getCount() {
			return COLUMN_COUNT;
		}
		
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			final LinearLayout wallpaperListLayout = (LinearLayout)LayoutInflater.from(context).inflate(MResource.getIdByName(context, "layout", "nq_wallpaper_list"), null);

			LinearLayout loadingLayout = (LinearLayout) wallpaperListLayout.findViewById(MResource.getIdByName(context, "id", "ll_load_failed"));
			mLoadFailedLayout[position] = loadingLayout;
			
			MoveBackView moveBackView = (MoveBackView) wallpaperListLayout.findViewById(MResource.getIdByName(context, "id", "nq_moveback"));
			mLoadingView[position] = moveBackView;
			
			mNoNetworkView[position] = (ImageView) wallpaperListLayout.findViewById(MResource.getIdByName(context, "id", "iv_nonetwork"));
			mEmptyView[position] = (ImageView) wallpaperListLayout.findViewById(MResource.getIdByName(context, "id", "iv_empty"));
			final int column = position;
			OnClickListener clickListener = new OnClickListener(){
				@Override
				public void onClick(View v) {
					getWallpaperList(column, 0);
				}
			};
			mLoadFailedLayout[position].setOnClickListener(clickListener);
			mNoNetworkView[position].setOnClickListener(clickListener);
			mEmptyView[position].setOnClickListener(clickListener);

            PullToRefreshListView listView = (PullToRefreshListView) wallpaperListLayout.findViewById(MResource.getIdByName(context, "id", "lv_list"));
			mListView[position] = listView;
			listView.setAdapter(mWallpaperArrListAdapters[position]);
			mListView[position].setOnScrollListener(new WallpaperListScrollListener());
			mListView[position].setRecyclerListener(new RecyclerListener() {
				@Override
				public void onMovedToScrapHeap(View view) {
					ViewHolder holder = (ViewHolder)view.getTag();
					hideViewHolder(holder);					
				}
			});
            listView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadedFlags[column] = false;
                    WallpaperManager.getInstance(context).getWallpaperList(column, 0, new BasicWallpaperListListener(column, 0,true));
                }

				@Override
				public void onLoadMore() {
					loadMoreData();
				}
            });

			container.addView(wallpaperListLayout, 0);
			
			int offset = mWallpaperArrListAdapters[position].getWallpaperList().size();
			if (viewPager.getCurrentItem() == position) {//只加载当前栏目
				getWallpaperList(position, offset);
			}
			return wallpaperListLayout;
		}
	}
	
	/**
	 * 壁纸模块，栏目切换viewPager，切换监听。
	 */
	public class WallpaperOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageScrollStateChanged(int state) {
//			NqLog.d("FragmentWallpaper.MyOnPageChangeListener.onPageScrollStateChanged()");
		}
		
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//			NqLog.d("FragmentWallpaper.MyOnPageChangeListener.onPageScrolled() position=" + position + " positionOffset="+positionOffset );
		}
		
		@Override
		public void onPageSelected(int position) {
			NqLog.d("FragmentWallpaper.MyOnPageChangeListener.onPageSelected() currIndex=" + position);
			currIndex = position;
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
					currIndex == 0 ? WallpaperActionConstants.ACTION_LOG_1303 : WallpaperActionConstants.ACTION_LOG_1302, null, 0, null);
			
			tvColumnNew.setTextColor(getResources().getColor(MResource.getIdByName(context, "color", "nq_text_store_column_title")));
			tvColumnTop.setTextColor(getResources().getColor(MResource.getIdByName(context, "color", "nq_text_store_column_title")));
			switch (currIndex) {
			case 0:
				tvColumnNew.setTextColor(getResources().getColor(MResource.getIdByName(context, "color", "nq_text_store_column_title_selected")));
				AnimationUtil.moveTo(ivCursor, CURSOR_ANIMATION_MILLISECONDS, cursorLastX, 0, 0, 0);
				ivCursor.setPadding(2*cursorPadingPx, 0, cursorPadingPx, 0);
				cursorLastX = 0;
				break;
			case 1:
				tvColumnTop.setTextColor(getResources().getColor(MResource.getIdByName(context, "color", "nq_text_store_column_title_selected")));
				AnimationUtil.moveTo(ivCursor, CURSOR_ANIMATION_MILLISECONDS, cursorLastX, cursorWidth, 0, 0);
				ivCursor.setPadding(cursorPadingPx, 0, 2*cursorPadingPx, 0);
				cursorLastX = cursorWidth;
				break;
			default:
				break;
			}
			int offset = mWallpaperArrListAdapters[currIndex].getWallpaperList().size();
            if(offset == 0) {//ListView为空，需要重新加载
				getWallpaperList(currIndex, offset);
            }
            showColumn(position);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == MResource.getIdByName(context, "id", "tv_column_new_wallpapers")) {
			viewPager.setCurrentItem(0);
		} else if (id == MResource.getIdByName(context, "id", "tv_column_top_wallpapers")) {
			viewPager.setCurrentItem(1);
		} else if (id == MResource.getIdByName(context, "id", "iv_nonetwork")) {
			getWallpaperList(currIndex, 0);
		} else if (id == MResource.getIdByName(context, "id", "iv_empty")) {
			getWallpaperList(currIndex, 0);
		}
	}
	
	/**
	 * 监听滚动条滚动。
	 */
	private class WallpaperListScrollListener implements AbsListView.OnScrollListener {
		
		/**
		 * 滚动条的滚动状态改变监听函数 滚动条的滚动状态scrollState：
		 * 0.SCROLL_STATE_IDLE 滑动后静止;视图没有滚动；
		 * 1.SCROLL_STATE_TOUCH_SCROLL 手指在屏幕上滑动;用户通过触控滚动，并且手指没有离开屏幕；
		 * 2.SCROLL_STATE_FLING 手指离开屏幕后，惯性滑动;正在滚动，但此时没有触摸屏幕。
		*/
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
	        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
	            NqLog.d("column=" + currIndex 
	            		+ ", scrolling... visible=" + visibleLastIndex[currIndex] 
	            		+ ", Count=" + view.getCount() 
	            		+ " loadedFlags[" + currIndex + "]=" + loadedFlags[currIndex]);

	            if(visibleLastIndex[currIndex] == view.getCount() - 1 && loadedFlags[currIndex]){
					toast("nq_list_end");
					mListView[currIndex].onLoadMoreComplete();
	            }
	            if(!isScrolling[currIndex] && visibleLastIndex[currIndex] == view.getCount() - 1 
	        			&& !loadedFlags[currIndex]){ //加上底部的loadMoreView项
	                loadMoreData();
	        	}
	        }
		}
		
		/*
		 * 当列表的滚动已经完成时调用的回调函数。会在滚动完成后调用。
		 */
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int viewIndex = (view == mListView[0]) ? 0: 1;
            if (currIndex == viewIndex) {
                if (firstVisibleItem == 1){
                    View headView1 = view.getChildAt(0);
                    if (headView1 != null) {
                        setY(mBannerLayout, Math.max(-1 * mBanner.getHeight(), headView1.getTop()));
                    }
                } else {
                    if(firstVisibleItem > 2){
                        setY(mBannerLayout, -1 * mBanner.getHeight());
                    }else if(firstVisibleItem == 0){
                        setY(mBannerLayout, 0);
                    }
                }
                visibleLastIndex[currIndex] = firstVisibleItem + visibleItemCount - 1;
            }
		}
	}

    private void setY(LinearLayout v, int y){
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
        params.topMargin = y;
        v.setLayoutParams(params);
    }
	
    ArrayList<Wallpaper> WallpaperArrToWallpaperList (List<Wallpaper[]> list) {
		ArrayList<Wallpaper> result = new ArrayList<Wallpaper>();
		if(list == null || list.isEmpty())
			return result;

		for(Wallpaper[] group : list){
			for(Wallpaper wallpaper : group){
				if(wallpaper != null)
					result.add(wallpaper);
			}
		}
		return result;
	}
    
	/**
	 * 列表Adapter
	 */
	private class WallpaperArrListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<Wallpaper[]> mWallpaperArrList;
		private int mLast = -1;
		
		public WallpaperArrListAdapter() {
			this.mInflater = LayoutInflater.from(context);
			this.mWallpaperArrList = new ArrayList<Wallpaper[]>();
		}

		public List<Wallpaper[]> getWallpaperList(){
			return mWallpaperArrList;
		}
		
		@Override
		public int getCount() {
			if (mWallpaperArrList != null) {
				return mWallpaperArrList.size();
			}
			return 0;
		}
		
		@Override
		public Object getItem(int position) {
			return position;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(MResource.getIdByName(context, "layout", "nq_wallpaper_list_item3"), null);
				for (int i = 0; i < 3; i++) {
					holder.rlLayout[i] = (RelativeLayout) convertView.findViewById(MResource.getIdByName(context, "id", "nq_wallpaper_list_item"+i));
					holder.ivPreview[i] = (AsyncImageView) holder.rlLayout[i].findViewById(MResource.getIdByName(context, "id", "iv_preview"));
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// 赋值
			final Wallpaper[] wallpapers = mWallpaperArrList.get(position);
			if(mLast != position){
				mLast = position;
				
				for (int i = 0; i < 3; i++) {
					
					if(wallpapers[i] != null){
						String key = new StringBuilder().append(currIndex).append(wallpapers[i].getStrId()).toString();
						if((null == mActionLogs.get(key) || !mActionLogs.get(key))){
							mActionLogs.put(key, true);

                            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                            		WallpaperActionConstants.ACTION_LOG_1301, wallpapers[i].getStrId(), 0, 
									currIndex + "_" + (3 * position + i));
						}
					}
				}
				
			}
			for (int i = 0; i < 3; i++) {
				if(wallpapers[i] != null){
					holder.rlLayout[i].setVisibility(View.VISIBLE);
					holder.ivPreview[i].setTag(wallpapers[i].getStrId());
					String url = wallpapers[i].getStrIconUrl();
					holder.ivPreview[i].loadImage(url, null, MResource
							.getIdByName(context, "drawable",
									"nq_load_default"));
					final int index = i;
					/**壁纸列表item点击事件*/
					holder.ivPreview[i].setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(context, WallpaperDetailActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.putExtra(WallpaperDetailActivity.KEY_WALLPAPERS, WallpaperArrToWallpaperList(mWallpaperArrList));
							intent.putExtra(WallpaperDetailActivity.KEY_WALLPAPER_POS, 3 * position + index);
							context.startActivity(intent);

                            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                            		WallpaperActionConstants.ACTION_LOG_1304, wallpapers[index].getStrId(), 0, 
									currIndex + "_" + position);
						}
					});
					
				} else {
					holder.rlLayout[i].setVisibility(View.INVISIBLE);
				}
			}
			return convertView;
		}
	}
	
	private static class ViewHolder {
		RelativeLayout[] rlLayout = new RelativeLayout[3];
		AsyncImageView[] ivPreview = new AsyncImageView[3];
	}
	
	/**获取壁纸列表*/
	private void getWallpaperList(int column, int offset){
		NqLog.d("getWallpaperList: column=" + column + " offset=" + offset);
		WallpaperManager wallpaperMgr = WallpaperManager.getInstance(context);
		
		if(offset == 0){
			// 从缓存中获取壁纸列表
			List<Wallpaper[]> wallpapers = wallpaperMgr.getArrWallpaperListFromCache(column);
			log("getWallpaperListFromCache", wallpapers);
			
			if (wallpapers != null && wallpapers.size() > 0) {
				hideWallpaperListLoading(column, 0, false);
				updateWallpaperList(column, offset, wallpapers);
			}
		}
		
		boolean bStarted = false;
		// 缓存过期，或者初始进入时网络是wifi情况下，需要联网获取
		if (wallpaperMgr.isCacheExpired(column) || 
				offset == 0 && NetworkUtils.isWifi(context)) {
			bStarted = true;
			showWallpaperListLoading(column);
			wallpaperMgr.getWallpaperList(column, 0, new BasicWallpaperListListener(column, offset,false));
		}
		// 下拉刷新
		if (offset > 0 && !bStarted) {
			showWallpaperListLoading(column);
			wallpaperMgr.getWallpaperList(column, offset, new BasicWallpaperListListener(column, offset,false));
		}
	}
	
	private void log(String TAG, List<Wallpaper[]> wallpapers){
		if (wallpapers != null) {
            NqLog.d(TAG + " wallpapers.size()" + wallpapers.size());
            Wallpaper[] ts = null;
            for (int i = 0; i < wallpapers.size(); i++) {
            	ts = wallpapers.get(i);
            	for(int j = 0; j < ts.length; j++) {
            		if(ts[j] != null && !TextUtils.isEmpty(ts[j].getStrId()))
            			NqLog.d(i + " " + TAG + " , ResourceId==" + ts[j].getStrId() + ", name=" + ts[j].getStrName());
            	}
            }
        } else {
            NqLog.d(TAG + " wallpapers == null");
        }
	}
	
	/** 刷新壁纸列表视图*/
	private void updateWallpaperList(int column, int offset, List<Wallpaper[]> wallpapers) {
		NqLog.d("updateWallpaperList: column=" + column + " offset=" + offset + " wallpapers=" + wallpapers);
		if (wallpapers == null || wallpapers.size() == 0){
			return;
		}
		
		log("updateWallpaperList", wallpapers);
		if (offset == 0) {
			mWallpaperArrListAdapters[column].getWallpaperList().clear();
		}
		
		mWallpaperArrListAdapters[column].getWallpaperList().addAll(wallpapers);
		mWallpaperArrListAdapters[column].notifyDataSetChanged();
		
		if(offset > 0) {
			mListView[column].scrollBy(0, scrollBy);
		}
	}
	
	/**
	 * 隐藏loadingView
	 * @param column 0:新品板块，1:热门板块
	 * @param type 0:获取成功，1:失败，2:无网络
	 * */
	private void hideWallpaperListLoading(final int column, final int type, boolean isErr) {
		NqLog.d("hideWallpaperListLoading: column=" + column);
		MoveBackView loadingView = mLoadingView[column];
		if (loadingView == null){
			return;
		}
		loadingView.stopAnim();

		if(isErr)
			mListView[column].onLoadErr(mWallpaperArrListAdapters[column].getCount() > 0);
		else {
			mListView[column].onLoadMoreComplete();
		}
		int newType = WallpaperManager.getInstance(context).hadAvailableWallpaperListCashe(column) ? 0 : type;
		mLoadFailedLayout[column].setVisibility(newType == 0 ? View.GONE : View.VISIBLE);
		switch(newType){
		case 0:
			mEmptyView[column].setVisibility(View.GONE);
			mNoNetworkView[column].setVisibility(View.GONE);
			mListView[column].setVisibility(View.VISIBLE);
			break;
		case 1:
			mEmptyView[column].setVisibility(View.VISIBLE);
			mNoNetworkView[column].setVisibility(View.GONE);
			mListView[column].setVisibility(View.GONE);
			break;
		case 2:
			mEmptyView[column].setVisibility(View.GONE);
			mNoNetworkView[column].setVisibility(View.VISIBLE);
			mListView[column].setVisibility(View.GONE);
			break;
		}
	}
	
	/**显示loadingView*/
	private void showWallpaperListLoading(final int column) {
		NqLog.d("showWallpaperListLoading: column=" + column);
		MoveBackView loadingView = mLoadingView[column];
		if (loadingView == null){
			return;
		}
		boolean isMore = mWallpaperArrListAdapters[column].getCount() > 0;
		loadingView.startAnim(isMore);
		if(isMore)
			mListView[column].onLoadMore();
	}
	
	/**从网络获取壁纸列表监听*/
	private class BasicWallpaperListListener implements WallpaperListStoreListener {
		private int mColumn;
		private int mOffset;
        private boolean mPullrefresh;

		public BasicWallpaperListListener(int column, int offset,boolean pullrefresh) {
			mColumn = column;
			mOffset = offset;
            mPullrefresh = pullrefresh;
		}

		@Override
		public void onErr() {
			NqLog.d("onErr");
			isScrolling[currIndex] = false;
			Activity activity = getActivity();
			if (activity == null){
				return;
			}
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					NqLog.d("onErr.runOnUiThread");
					if (mWallpaperArrListAdapters[mColumn].getWallpaperList().size() > 0){
						hideWallpaperListLoading(mColumn, 0, true);
					} else {
						// 显示下载失败，请重试的视图
						ToastUtils.toast(context, "nq_connection_failed");
						hideWallpaperListLoading(mColumn, 1, true);
					}
                    if(mPullrefresh){
                        mListView[currIndex].onRefreshComplete();
                    }
				}
			});
		}

		@Override
		public void onNoNetwork() {
			NqLog.d("onNoNetwork");
			isScrolling[currIndex] = false;
			Activity activity = getActivity();
			if (activity == null){
				return;
			}
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					NqLog.d("onNoNetwork.runOnUiThread");
					hideWallpaperListLoading(mColumn, 2, true);
                    if(mPullrefresh){
                        mListView[currIndex].onRefreshComplete();
                    }
				}
			});
			
			ToastUtils.toast(context, "nq_nonetwork");
		}

		@Override
		public void onGetWallpaperListSucc(final int column, final int offset,
				final List<Wallpaper[]> wallpapers) {
			NqLog.d("onGetWallpaperListSucc: column=" + column + " offset=" + offset + " wallpapers=" + wallpapers);
			isScrolling[currIndex] = false;
			Activity activity = getActivity();
			if (activity == null){
				return;
			}
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					NqLog.d("onGetThemeListSucc.runOnUiThread: column=" + column + " offset=" + offset + " wallpapers=" + wallpapers);

                    if(wallpapers != null){
                    	if(wallpapers.size() == 0 && mWallpaperArrListAdapters[column].getWallpaperList().size() == 0){
                    		onErr();
                    	}else {
                    		hideWallpaperListLoading(column, 0, false);
                    		updateWallpaperList(column, offset, wallpapers);
                    		
                    		if(WallpaperManager.getCount(wallpapers) < WallpaperConstants.STORE_WALLPAPER_LIST_PAGE_SIZE
                    				|| loadedFlags[column]){//视为末页
                    			loadedFlags[column] = true;
                    		} else {
                    			loadedFlags[column] = false;
                    		}
                    	}
                    }else{
                        hideWallpaperListLoading(column, 1, false);
                    }

                    if(mPullrefresh){
                        mListView[currIndex].onRefreshComplete();
                    }
				}
			});
		}
	}
	
	void toast(String text){
		ToastUtils.toast(context, text);
	}
	
	@Override
	public void onResume() {
		NqLog.d("FragmentWallpaper.onResume");
		super.onResume();
		if (!isHidden()) {
			mBannerUI.startAutoSlide();
		}
	}
	
	@Override
	public void onPause() {
		NqLog.d("FragmentWallpaper.onPause");
		if (!isHidden()) {
			mBannerUI.stopAutoSlide();
		}
		super.onPause();
	}
	
	@Override
	public void onDestroyView() {
        NqLog.d("onDestroyView");
        mBannerUI.disableAutoSlide();
        super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		if(mActionLogs != null){
        	mActionLogs.clear();
        	mActionLogs = null;
        }
		super.onDestroy();
	}
	
	private void hideColumn(int column) {
		ListView curListView = mListView[column];
		if (curListView == null) {
			return;
		}
		for (int i = 0; i < curListView.getLastVisiblePosition()
				- curListView.getFirstVisiblePosition() + 1; i++) {
			View v = curListView.getChildAt(i);
			ViewHolder holder = (ViewHolder) v.getTag();
			hideViewHolder(holder);
		}
	}
	
	private void showColumn(int column) {
		if (mWallpaperArrListAdapters[column].getCount() <= 0) {
			//getThemeList(column, 0);
			return;
		}

		ListView curListView = mListView[column];
		if (curListView == null) {
			return;
		}
		for (int i = 0; i < curListView.getLastVisiblePosition()
				- curListView.getFirstVisiblePosition() + 1; i++) {
			View v = curListView.getChildAt(i);
			ViewHolder holder = (ViewHolder) v.getTag();
			showViewHolder(holder);
		}
	}
	
	private void hideViewHolder(ViewHolder holder){
		if (holder == null) {
			return;
		}
		for (int j = 0; j < ITEMS_PER_ROW; j++) {
			AsyncImageView imgView = holder.ivPreview[j];
			if (imgView == null) {
				continue;
			}
			NqLog.d("hideViewHolder: ivPreview.mUrl = " + imgView.getUrl());
			imgView.onHide();
		}
	}
	
	private void showViewHolder(ViewHolder holder){
		if (holder == null) {
			return;
		}
		for (int j = 0; j < ITEMS_PER_ROW; j++) {
			AsyncImageView imgView = holder.ivPreview[j];
			if (imgView == null) {
				continue;
			}
			NqLog.d("showViewHolder: ivPreview.mUrl = " + imgView.getUrl());
			imgView.onShow();
		}
	}

	public void loadMoreData() {
		//如果是自动加载,可以在这里放置异步加载数据的代码
		isScrolling[currIndex] = true;
		int size = mWallpaperArrListAdapters[viewPager.getCurrentItem()].getWallpaperList().size();
		Wallpaper[] tmp = mWallpaperArrListAdapters[viewPager.getCurrentItem()].getWallpaperList().get(size - 1);
		int offset = 3 * size;
		for(int i=0; i<tmp.length; i++){
			if(tmp[i] == null || TextUtils.isEmpty(tmp[i].getStrId()))
				offset --;
		}
		
		NqLog.d("loading... " + visibleLastIndex[currIndex] + ",offset=" + offset);
		getWallpaperList(currIndex, offset);
	}

}
