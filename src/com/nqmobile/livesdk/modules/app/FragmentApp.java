package com.nqmobile.livesdk.modules.app;

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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.commons.ui.DownloadImageView;
import com.nqmobile.livesdk.commons.ui.MoveBackView;
import com.nqmobile.livesdk.commons.ui.PullToRefreshListView;
import com.nqmobile.livesdk.commons.ui.StoreMainActivity;
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
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * 应用主页
 * @author changxiaofei
 * @time 2013-11-18 下午4:52:37
 */
public class FragmentApp extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {
	private static final ILogger NqLog = LoggerFactory.getLogger(AppModule.MODULE_NAME);
	
	private static final int COLUMN_COUNT = 2;
	private static final int CURSOR_ANIMATION_MILLISECONDS = 1;
	private Context mContext;
	/*banner*/
	private LinearLayout mBannerLayout;
	private BannerUI mBannerUI;
	private RelativeLayout mBanner;
    private RelativeLayout mTitle;
	// ViewPager
	private ViewPager mViewPager;
	private LinearLayout[] mLoadFailedLayout;
	private MoveBackView[] mLoadingView;
	private ImageView[] mNoNetworkView;
	private ImageView[] mEmptyView;
	private TextView tvColumnTopGames, tvColumnTopApps;
	private ImageView ivCursor;
	private RelativeLayout.LayoutParams lpCursor;
	private PullToRefreshListView[] mListView;
	// 数据
	private AppListAdapter[] mAppListAdapters;
	private int screenWidth;
	private int cursorWidth;
	private int cursorLastX;
	private int cursorPadingPx;
	private int[] visibleLastIndex = {0, 0};
	private int currIndex = 0;// 应用列表ViewPager当前页卡编号
	private boolean[] loadedFlags;
	private boolean[] isScrolling;
	private int scrollBy;
	// 行为日志
	private Map<String, Boolean> mActionLogs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        NqLog.d("onCreate");
        super.onCreate(savedInstanceState);
		mContext = getActivity().getApplicationContext();
		if(ApplicationContext.getContext() == null)
			ApplicationContext.setContext(mContext);
		cursorPadingPx = DisplayUtil.dip2px(mContext, 6);
		scrollBy = DisplayUtil.dip2px(mContext, 10);
		mActionLogs = new ConcurrentHashMap<String, Boolean>();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		NqLog.d("onCreateView");

		View appView = inflater.inflate(MResource.getIdByName(mContext, "layout", "nq_fragment_app"), container, false);
		// 初始化Banner控件
		mBannerLayout = (LinearLayout) appView.findViewById(MResource.getIdByName(mContext, "id", "bannerLayout"));
		mBannerUI = new BannerUI(mBannerLayout, new Handler(), BannerConstants.STORE_PLATE_TYPE_APP);
		mBanner = (RelativeLayout) appView.findViewById(MResource.getIdByName(mContext, "id", "rl_banner"));
		mBannerUI.enableAutoSlide();
		// 初始化栏目指示器
		ivCursor = (ImageView) appView.findViewById(MResource.getIdByName(mContext, "id", "tv_cursor"));
		screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;// 获取分辨率宽度
		cursorWidth = (int) (screenWidth / 2 + 0.5f);
		lpCursor = new RelativeLayout.LayoutParams(cursorWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		ivCursor.setPadding(2 * cursorPadingPx, 0, cursorPadingPx, 0);
		ivCursor.setLayoutParams(lpCursor);
		// 初始化栏目
		tvColumnTopGames = (TextView) appView.findViewById(MResource.getIdByName(mContext, "id", "tv_column_top_games"));
        tvColumnTopGames.setTextColor(getResources().getColor(MResource.getIdByName(mContext, "color", "nq_text_store_column_title_selected")));
		tvColumnTopApps = (TextView) appView.findViewById(MResource.getIdByName(mContext, "id", "tv_column_top_apps"));
		// 初始化栏目的列表
		mViewPager = (ViewPager) appView.findViewById(MResource.getIdByName(mContext, "id", "vp_list"));
		mViewPager.setAdapter(new AppColumnsPagerAdapter());
		mViewPager.setCurrentItem(0);
		mViewPager.setOnPageChangeListener(new AppColumnsOnPageChangeListener());
		
		mAppListAdapters = new AppListAdapter[COLUMN_COUNT];
		mListView = new PullToRefreshListView[COLUMN_COUNT];
		for(int i=0; i<COLUMN_COUNT; i++){
			mAppListAdapters[i] = new AppListAdapter();
		}
		mLoadFailedLayout = new LinearLayout[COLUMN_COUNT];
		mLoadingView = new MoveBackView[COLUMN_COUNT];
		mNoNetworkView = new ImageView[COLUMN_COUNT];
		mEmptyView = new ImageView[COLUMN_COUNT];
		
		loadedFlags = new boolean[2];
		isScrolling = new boolean[2];
		tvColumnTopGames.setOnClickListener(this);
		tvColumnTopApps.setOnClickListener(this);

        mTitle = (RelativeLayout) appView.findViewById(MResource.getIdByName(mContext,"id","title_layout"));
        mTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mListView[currIndex].setSelection(1);
            }
        });

        ImageView pointCenter = (ImageView) appView.findViewById(MResource.getIdByName(mContext,"id","pointCenter"));
        pointCenter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, PointsCenterActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(i);

                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION, PointActionConstants.ACTION_LOG_2501,null,0,null);
            }
        });

        if(PointsPreference.getInstance().getBooleanValue(PointsPreference.KEY_POINT_CENTER_ENABLE)){
            pointCenter.setVisibility(View.VISIBLE);
        }else{
            pointCenter.setVisibility(View.GONE);
        }
//        FontManager.getInstance(mContext).setTypeFace(container);
		return appView;
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		NqLog.d("onHiddenChanged: hidden = " + hidden);
		
		if (isHidden()){
			hideColumn(0);
			hideColumn(1);
			mBannerUI.onHide();
			System.gc();
		} else {
			int columnToShow = this.getActivity().getIntent().getIntExtra(StoreMainActivity.KEY_COLUMN, 0);
			int value = this.getActivity().getIntent().getIntExtra(StoreMainActivity.KEY_FROM, 0);
			NqLog.i("ljc1234: onHiddenChanged-->columnToShow =" + columnToShow);
			NqLog.i("ljc1234: onHiddenChanged-->value =" + value);
			if (value == 1003) {
				mViewPager.setCurrentItem(columnToShow);
			}

			showColumn(mViewPager.getCurrentItem());
			mBannerUI.onShow();
		}
		super.onHiddenChanged(hidden);
	}
	
	/**
	 * 应用模块，栏目切换PagerAdapter
	 */
	public class AppColumnsPagerAdapter extends PagerAdapter {
		
		public AppColumnsPagerAdapter() { }
		
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
			final LinearLayout appListLayout = (LinearLayout)LayoutInflater.from(mContext).inflate(MResource.getIdByName(mContext, "layout", "nq_app_list"), null);

			LinearLayout loadingLayout = (LinearLayout) appListLayout.findViewById(MResource.getIdByName(mContext, "id", "ll_load_failed"));
			mLoadFailedLayout[position] = loadingLayout;
			MoveBackView moveBackView = (MoveBackView) appListLayout.findViewById(MResource.getIdByName(mContext, "id", "nq_moveback"));
			mLoadingView[position] = moveBackView;
			
			mNoNetworkView[position] = (ImageView) appListLayout.findViewById(MResource.getIdByName(mContext, "id", "iv_nonetwork"));
			mEmptyView[position] = (ImageView) appListLayout.findViewById(MResource.getIdByName(mContext, "id", "iv_empty"));
			final int column = position;
			OnClickListener clickListener = new OnClickListener(){
				@Override
				public void onClick(View v) {
					getAppList(column, 0);
				}
			};
			mLoadFailedLayout[position].setOnClickListener(clickListener);
			mNoNetworkView[position].setOnClickListener(clickListener);
			mEmptyView[position].setOnClickListener(clickListener);

            PullToRefreshListView listView = (PullToRefreshListView) appListLayout.findViewById(MResource.getIdByName(mContext, "id", "lv_list"));
			mListView[position] = listView;
			listView.setAdapter(mAppListAdapters[position]);
            listView.setOnScrollListener(new AppListScrollListener());
			listView.setOnItemClickListener(FragmentApp.this);
			//listView.setFastScrollEnabled(true);
			listView.setRecyclerListener(new RecyclerListener() {
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
                    AppManager.getInstance(mContext).getAppList(column, 0, new BasicAppListListener(column, 0,true));
                }

				@Override
				public void onLoadMore() {
					loadMoreData();
				}
            });

			
			container.addView(appListLayout, 0);
			
			int offset = mAppListAdapters[position].getAppList().size();
			if (mViewPager.getCurrentItem() == position) {//只加载当前栏目
				getAppList(position, offset);
			}
			return appListLayout;
		}
	}
	
	/**
	 * 应用模块，栏目切换viewPager，切换监听。
	 */
	public class AppColumnsOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageScrollStateChanged(int state) {
//			NqLog.d("FragmentApp.MyOnPageChangeListener.onPageScrollStateChanged()");
		}
		
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//			NqLog.d("FragmentApp.MyOnPageChangeListener.onPageScrolled() position=" + position + " positionOffset="+positionOffset );
		}
		
		@Override
		public void onPageSelected(int position) {
			NqLog.d("FragmentApp.MyOnPageChangeListener.onPageSelected() currIndex=" + position);
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
					position == 0 ? AppActionConstants.ACTION_LOG_1105 : AppActionConstants.ACTION_LOG_1104, null, 0, null);
			currIndex = position;
			tvColumnTopGames.setTextColor(getResources().getColor(MResource.getIdByName(mContext, "color", "nq_text_store_column_title")));
			tvColumnTopApps.setTextColor(getResources().getColor(MResource.getIdByName(mContext, "color", "nq_text_store_column_title")));
			switch (position) {
			case 0:
				tvColumnTopGames
						.setTextColor(getResources().getColor(MResource.getIdByName(mContext, "color", "nq_text_store_column_title_selected")));
				AnimationUtil.moveTo(ivCursor, CURSOR_ANIMATION_MILLISECONDS, cursorLastX, 0, 0, 0);
				ivCursor.setPadding(2 * cursorPadingPx, 0, cursorPadingPx, 0);
				cursorLastX = 0;
				break;
			case 1:
				tvColumnTopApps.setTextColor(getResources().getColor(MResource.getIdByName(mContext, "color", "nq_text_store_column_title_selected")));
				AnimationUtil.moveTo(ivCursor, CURSOR_ANIMATION_MILLISECONDS, cursorLastX, cursorWidth, 0, 0);
				ivCursor.setPadding(cursorPadingPx, 0, 2 * cursorPadingPx, 0);
				cursorLastX = cursorWidth;
				break;
			default:
				break;
			}
			
			int offset = mAppListAdapters[position].getAppList().size();
			if (offset == 0) {//当前listview没数据
				getAppList(position, offset);
			}
			showColumn(position);
		}
	}
	
	/**
	 * 应用列表Adapter
	 */
	private class AppListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<App> mAppList;
		private int mLast = -1;
		
		public AppListAdapter() {
			mInflater = LayoutInflater.from(mContext);
			mAppList = new ArrayList<App>();
		}
		
		public List<App> getAppList(){
			return mAppList;
		}
		
		@Override
		public int getCount() {
			if (mAppList != null) {
				return mAppList.size();
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
		public View getView(int position, View convertView, ViewGroup parent) {
			NqLog.d("getView: position = " + position + " convertView=" + convertView);
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(MResource.getIdByName(mContext, "layout", "nq_app_list_item"), null);
				holder.ivIcon = (AsyncImageView) convertView.findViewById(MResource.getIdByName(mContext, "id", "iv_icon"));
				holder.ivDownload = (DownloadImageView) convertView.findViewById(MResource.getIdByName(mContext, "id", "iv_download"));
				holder.ivDownload.setPos(position);
				holder.tvName = (TextView) convertView.findViewById(MResource.getIdByName(mContext, "id", "tv_name"));
				holder.rbRate = (RatingBar) convertView.findViewById(MResource.getIdByName(mContext, "id", "rb_rate"));
				holder.tvSize = (TextView) convertView.findViewById(MResource.getIdByName(mContext, "id", "tv_size"));
				convertView.setTag(holder);				
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// 赋值
			App app = mAppList.get(position);
			holder.tvName.setText(app.getStrName());
			holder.rbRate.setRating(app.getFloatRate());
			holder.tvSize.setText(StringUtil.formatSize(app.getLongSize()));
			holder.ivDownload.init(app);
			String iconUrl = app.getStrIconUrl();
			holder.ivIcon.loadImage(iconUrl, null, MResource.getIdByName(
					mContext, "drawable", "nq_icon_default"));
			
			if(mLast != position){
				mLast = position;
				String key = new StringBuilder().append(mViewPager.getCurrentItem()).append(app.getStrId()).toString();
				if((null == mActionLogs.get(key) || !mActionLogs.get(key))){
					mActionLogs.put(key, true);
					
					if(app.getIntClickActionType() == App.CLICK_ACTION_TYPE_DIRECT){
                        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
								AppActionConstants.ACTION_LOG_1101, app.getStrId(), 0, 
								mViewPager.getCurrentItem() + "_" + position);
					}else if(app.getIntClickActionType() == App.CLICK_ACTION_TYPE_BROWSER ||
							app.getIntClickActionType() == App.CLICK_ACTION_TYPE_GP){
                        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                        		AppActionConstants.ACTION_LOG_1101, app.getStrId(), 0,
								mViewPager.getCurrentItem() + "_" + position);//yanmin: 这里为何用ACTION_LOG_1401？可能有问题
					}

				}

			}
						
			return convertView;
		}
	}
	
	private static class ViewHolder {
		AsyncImageView ivIcon;
		DownloadImageView ivDownload;
		TextView tvName;
		RatingBar rbRate;
		TextView tvSize;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		List<App> list = mAppListAdapters[mViewPager.getCurrentItem()].getAppList();
		int pos = (int)parent.getAdapter().getItemId(position);//由于加了header，position会有偏差，因此需要先getAdapter获取内部包装过的Adapter
		if (list!=null && pos >=0 && pos < list.size()) {
			App app = list.get(pos);
			AppManager.getInstance(mContext).viewAppDetail(mViewPager.getCurrentItem(),app);

            StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
            		AppActionConstants.ACTION_LOG_1102, app.getStrId(), StatManager.ACTION_CLICK, 
					mViewPager.getCurrentItem() + "_" + (position - 2));
			
			if(app.getIntClickActionType() == App.CLICK_ACTION_TYPE_DIRECT)
                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                		AppActionConstants.ACTION_LOG_1106, app.getStrId(), app.getStrId().startsWith("AD_")?1:0, null);
			
		}
	}
	
	/**
	 * 监听滚动条滚动。
	 */
	private class AppListScrollListener implements AbsListView.OnScrollListener {
		/*
		 * 滚动条的滚动状态改变监听函数 滚动条的滚动状态scrollState：0视图没有滚动；1用户通过触控滚动，并且手指没有离开屏幕；2正在滚动，但此时没有触摸屏幕。
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

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == MResource.getIdByName(mContext, "id", "tv_column_top_games")) {
			mViewPager.setCurrentItem(0, true);
		} else if (id == MResource.getIdByName(mContext, "id", "tv_column_top_apps")) {
			mViewPager.setCurrentItem(1, true);
		}
	}
	
	@Override
	public void onResume() {
		NqLog.d("FragmentApp.onResume");
		super.onResume();
		if (!isHidden()) {
			mBannerUI.startAutoSlide();
			mAppListAdapters[currIndex].notifyDataSetInvalidated();
		}
	}
	
	@Override
	public void onPause() {
		NqLog.d("FragmentApp.onPause");
		if (!isHidden()) {
			mBannerUI.stopAutoSlide();
		}
		super.onPause();
	}
	
	@Override
	public void onDestroyView() {
		NqLog.d("FragmentApp.onDestroyView");
		mBannerUI.disableAutoSlide();
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		NqLog.d("FragmentApp.onDestroy");
		if(mActionLogs != null){
        	mActionLogs.clear();
        	mActionLogs = null;
        }
		super.onDestroy();
	}
	
	private void setY(LinearLayout v, int y){
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
		params.topMargin = y;
		v.setLayoutParams(params);
	}
	

	private class BasicAppListListener implements AppListStoreListener {
		private int mColumn;
		private int mOffset;
        private boolean mPullrefresh;

		public BasicAppListListener(int column, int offset,boolean pullrefresh) {
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
					if (mAppListAdapters[mColumn].getAppList().size() > 0){
						hideAppListLoading(mColumn, 0, true);
					} else {
						// 显示下载失败，请重试的视图
						toast("nq_connection_failed");
						hideAppListLoading(mColumn, 1, true);
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
					hideAppListLoading(mColumn, 2, true);
                    if(mPullrefresh){
                        mListView[currIndex].onRefreshComplete();
                    }
				}
			});
			
			toast("nq_nonetwork");
		}

		@Override
		public void onGetAppListSucc(final int column, final int offset,
				final List<App> apps) {
			NqLog.d("onGetAppListSucc: column=" + column + " offset=" + offset + " apps=" + apps);
			isScrolling[currIndex] = false;
			Activity activity = getActivity();
			if (activity == null){
				return;
			}
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					NqLog.d("onGetAppListSucc.runOnUiThread: column=" + column + " offset=" + offset + 
							" apps=" + (apps != null ? apps.size() : 0) + " oldApps：" + mAppListAdapters[column].getAppList().size());
                    if(apps != null){
                    	if(apps.size() == 0 && mAppListAdapters[column].getAppList().size() == 0){
                    		onErr();
                    	}else {
                    		hideAppListLoading(column, 0, false);
                    		updateAppList(column, offset, apps);
                    		
                    		if(AppManager.getCount(apps) < AppConstant.STORE_APP_LIST_PAGE_SIZE
                    				|| loadedFlags[column]){//视为末页
                    			loadedFlags[column] = true;
                    		} else {
                    			loadedFlags[column] = false;
                    		}
						}
                    }else {
                    	hideAppListLoading(column, 1, false);
					}

                    if(mPullrefresh){
                        mListView[currIndex].onRefreshComplete();
                    }
				}
			});
		}
	}
	
	void toast(String text){
		NqLog.d("toast:" + text);
		ToastUtils.toast(mContext, text);
	}

	/**
	 * @param column 0:游戏板块，1:应用板块
	 * @param type 0:获取成功，1:失败，2:无网络
	 * */
	private void hideAppListLoading(final int column, final int type, boolean isErr) {
		MoveBackView loadingView = mLoadingView[column];
		NqLog.d("hideAppListLoading: column=" + column + " loadingView:" + loadingView);
		if (loadingView == null){
			return;
		}
		loadingView.stopAnim();

		if(isErr)
			mListView[column].onLoadErr(mAppListAdapters[column].getCount() > 0);
		else {
			mListView[column].onLoadMoreComplete();
		}
		int newType = AppManager.getInstance(mContext).hadAvailableAppListCashe(column) ? 0 : type;
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
		default:
			break;
		
		}
	}

	private void showAppListLoading(final int column) {
		NqLog.d("showAppListLoading: column=" + column);
		MoveBackView loadingView = mLoadingView[column];
		if (loadingView == null){
			return;
		}
		boolean isMore = mAppListAdapters[column].getCount() > 0;
		loadingView.startAnim(isMore);
		if(isMore)
			mListView[column].onLoadMore();
	}
	
	private void updateAppList(int column, int offset, List<App> apps) {
		NqLog.d("updateAppList: column=" + column + " offset=" + offset + " apps=" + apps);
		if (apps == null || apps.size() == 0){
			return;
		}
		
		log("updateAppList", apps);
		
		if (offset == 0) {
			mAppListAdapters[column].getAppList().clear();
		}
		mAppListAdapters[column].getAppList().addAll(apps);
		mAppListAdapters[column].notifyDataSetChanged();
		
		if(offset > 0) {
			mListView[column].scrollBy(0, scrollBy);
		}
	}
	
	private void getAppList(int column, int offset){
		NqLog.d("getAppList: column=" + column + " offset=" + offset);
		AppManager appManager = AppManager.getInstance(mContext);
		
		if(offset == 0){
			// 从缓存中获取壁纸列表
			List<App> apps = appManager.getAppListFromCache(column);
			
			log("getAppListFromCache", apps);
			
			if (apps != null && apps.size() > 0) {
				hideAppListLoading(column, 0, false);
				updateAppList(column, offset, apps);
			}
		}
		boolean bStarted = false;
		// 缓存过期，或者初始进入时网络是wifi情况下，需要联网获取
		if (appManager.isCacheExpired(column) || 
				offset == 0 && NetworkUtils.isWifi(mContext)) {
			bStarted = true;
			showAppListLoading(column);
			appManager.getAppList(column, 0, new BasicAppListListener(column, offset,false));
		}
		// 下拉刷新
		if (offset > 0 && !bStarted) {
			showAppListLoading(column);
			appManager.getAppList(column, offset, new BasicAppListListener(column, offset,false));
		}
	}
	
	private void log(String tag, List<App> apps){
		if (apps != null) {
            NqLog.d(tag + " apps.size()" + apps.size());
            App ts = null;
            for (int i = 0; i < apps.size(); i++) {
            	ts = apps.get(i);
           		if(ts != null && !TextUtils.isEmpty(ts.getStrId()))
           			NqLog.d(i + " " + tag + " , ResourceId==" + ts.getStrId() + ", name=" + ts.getStrName());
            }
        } else {
            NqLog.d(tag + " apps == null");
        }
	}
	
	public void hideColumn(int column){
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
	
	public void showColumn(int column) {
		if (mAppListAdapters[column].getCount() <= 0) {
			//getAppList(column, 0);
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
		NqLog.d("hideViewHolder: ivIcon.mUrl = " + holder.ivIcon.getUrl());
		holder.ivIcon.onHide();
	}
	
	private void showViewHolder(ViewHolder holder){
		if (holder == null) {
			return;
		}
		NqLog.d("showViewHolder: ivIcon.mUrl = " + holder.ivIcon.getUrl());
		holder.ivIcon.onShow();
	}
	
	public void loadMoreData() {
		//如果是自动加载,可以在这里放置异步加载数据的代码
		isScrolling[currIndex] = true;
		ArrayList<App> list = (ArrayList<App>) mAppListAdapters[mViewPager.getCurrentItem()].getAppList();
		int offset = list == null ? 0 : list.size();
//		int offset = size;
//		for(int i=0; i<size; i++){
//			if(list.get(i).getStrId().startsWith("AD_"))
//				offset --;
//		}
		NqLog.d("loading... " + visibleLastIndex[currIndex] + ",offset=" + offset);
		getAppList(currIndex, offset);
	}
}
