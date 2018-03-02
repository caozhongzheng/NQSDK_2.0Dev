package com.nqmobile.livesdk.modules.locker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.content.ActivityNotFoundException;
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
import com.nqmobile.livesdk.modules.points.PointsCenterActivity;
import com.nqmobile.livesdk.modules.points.PointsPreference;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.AnimationUtil;
import com.nqmobile.livesdk.utils.DisplayUtil;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * 锁屏主页
 * @author caozhongzheng
 * @time 2014/5/8 9:41:38
 */
public class FragmentLocker extends Fragment implements View.OnClickListener {
	private static final ILogger NqLog = LoggerFactory.getLogger(LockerModule.MODULE_NAME);
	
	private static final int COLUMN_COUNT = 2; //2个栏目
	private static final int CURSOR_ANIMATION_MILLISECONDS = 1;
	private static final int ITEMS_PER_ROW = 3; //每行3个锁屏icon
	//private static final int WHAT_GET_BANNER_LIST = 1;
//	private static final int WHAT_GET_LOCKER_LIST = 2;
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
	private LockerArrListAdapter[] mLockerArrListAdapters;
	private int[] visibleLastIndex = {0, 0};
	private int currIndex = 0;// 锁屏列表ViewPager当前页卡编号

	private boolean[] loadedFlags;
	private boolean[] isScrolling;
	private int scrollBy;
    private ImageView mLocalLocker;
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
        View lockerView = inflater.inflate(MResource.getIdByName(context, "layout", "nq_fragment_locker"), container, false);
		// Banner控件
		mBannerLayout = (LinearLayout) lockerView.findViewById(MResource.getIdByName(context, "id", "bannerLayout"));
		mBannerUI = new BannerUI(mBannerLayout, new Handler(), BannerConstants.STORE_PLATE_TYPE_LOCKER);
		mBanner = (RelativeLayout) lockerView.findViewById(MResource.getIdByName(context, "id", "rl_banner"));
		mBannerUI.enableAutoSlide();
		// 初始化栏目指示器
		ivCursor = (ImageView) lockerView.findViewById(MResource.getIdByName(context, "id", "tv_cursor"));
		screenWidth = context.getResources().getDisplayMetrics().widthPixels;// 获取分辨率宽度
		cursorWidth = (int) (screenWidth / 2 + 0.5f);
		lpCursor = new RelativeLayout.LayoutParams(cursorWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		ivCursor.setPadding(2 * cursorPadingPx, 0, cursorPadingPx, 0);
		ivCursor.setLayoutParams(lpCursor);
		// 初始化栏目
		tvColumnNew = (TextView) lockerView.findViewById(MResource.getIdByName(context, "id", "tv_column_new_lockers"));
		tvColumnNew.setTextColor(getResources().getColor(MResource.getIdByName(context, "color", "nq_text_store_column_title_selected")));
		tvColumnTop = (TextView) lockerView.findViewById(MResource.getIdByName(context, "id", "tv_column_top_lockers"));
		// 初始化栏目列表
		viewPager = (ViewPager) lockerView.findViewById(MResource.getIdByName(context, "id", "vp_list"));
		viewPager.setAdapter(new LockerPagerAdapter());
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new LockerOnPageChangeListener());

		mLoadFailedLayout = new LinearLayout[COLUMN_COUNT];
		mLoadingView = new MoveBackView[COLUMN_COUNT];
		mNoNetworkView = new ImageView[COLUMN_COUNT];
		mEmptyView = new ImageView[COLUMN_COUNT];

		mLockerArrListAdapters = new LockerArrListAdapter[COLUMN_COUNT];
		for(int i=0; i<COLUMN_COUNT; i++){
			mLockerArrListAdapters[i] = new LockerArrListAdapter();
		}
		mListView = new PullToRefreshListView[COLUMN_COUNT];

		loadedFlags = new boolean[2];
		isScrolling = new boolean[2];
		tvColumnNew.setOnClickListener(this);
		tvColumnTop.setOnClickListener(this);

        mLocalLocker = (ImageView) lockerView.findViewById(MResource.getIdByName(context, "id", "btn_local"));
        mLocalLocker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Intent i = new Intent();
                    i.setAction("com.lqlauncher.LocalLocker");
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    		LockerActionConstants.ACTION_LOG_2109, null, 0, 
        					"0");
                }catch (ActivityNotFoundException e){
                    e.printStackTrace();
                }
            }
        });

        mTitle = (RelativeLayout) lockerView.findViewById(MResource.getIdByName(context,"id","title_layout"));
        mTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mListView[currIndex].setSelection(1);
            }
        });

        ImageView pointCenter = (ImageView) lockerView.findViewById(MResource.getIdByName(context,"id","pointCenter"));
        pointCenter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, PointsCenterActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });

        if(PreferenceDataHelper.getInstance(context).getBooleanValue(PointsPreference.KEY_POINT_CENTER_ENABLE)){
            pointCenter.setVisibility(View.VISIBLE);
        }else{
            pointCenter.setVisibility(View.GONE);
        }

        return lockerView;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
        NqLog.d("onHiddenChanged: hidden= " + isHidden());
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

	protected List<Locker[]> mergeFetched(List<Locker[]> listShow, List<Locker[]> list) {
		// TODO Auto-generated method stub
		if(list == null || LockerManager.getCount(list) == 0)
			return listShow;
		if(listShow == null || LockerManager.getCount(listShow) == 0)
			return list;

		List<Locker> have = LockerArrToLocker(listShow);
		List<Locker> gets = LockerArrToLocker(list);
		int sizeh = have.size();
		int sizeg = gets.size();
		int k=0;
		for(int j=0; j<sizeg; j++){
			for(int i=k; i<sizeh; i++){
				if(TextUtils.isEmpty(gets.get(j).getStrId()) || have.get(i).getStrId().equals(gets.get(j))){
					gets.get(j).setStrId("");
					k = ++i;
					break;
				}
			}
		}

		for(int i=0; i<sizeg; i++){
			if(TextUtils.isEmpty(gets.get(i).getStrId())){
				have.add(gets.get(i));
			}
		}

		return LockerToLockerArr(have);
	}

	List<Locker[]> LockerToLockerArr (List<Locker> list) {
		List<Locker[]> result = new ArrayList<Locker[]>();
		if(list == null || list.size() < 1)
			return result;

		Locker[] arrLocker = null;
		int size = list.size();
		for(int i=0; i<size; i++){
			if(i%3 == 0){
				arrLocker = new Locker[3];
			}
			arrLocker[i%3] = list.get(i);
			if(i%3 == 2){
				result.add(arrLocker);
			}
		}

		if(size%3 != 0){
			result.add(arrLocker);
		}
		return result;
	}

	ArrayList<Locker> LockerArrToLocker (List<Locker[]> list) {
		ArrayList<Locker> result = new ArrayList<Locker>();
		if(list == null || list.isEmpty())
            return result;                                   

		for(Locker[] group : list){
			for(Locker locker : group ){
				if(locker != null){
					result.add(locker);
				}
			}
		}
		return result;
	}

	/**
	 * 锁屏模块，栏目切换PagerAdapter
	 */
	public class LockerPagerAdapter extends PagerAdapter {

		public LockerPagerAdapter() {
		}

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
			final LinearLayout lockerListLayout = (LinearLayout)LayoutInflater.from(context).inflate(MResource.getIdByName(context, "layout", "nq_locker_list"), null);

			LinearLayout loadingLayout = (LinearLayout) lockerListLayout.findViewById(MResource.getIdByName(context, "id", "ll_load_failed"));
			mLoadFailedLayout[position] = loadingLayout;
			/*ImageView loadingView = (ImageView) lockerListLayout.findViewById(MResource.getIdByName(context, "id", "iv_loading_anim"));
			AnimationDrawable loadingAnimation = (AnimationDrawable) loadingView.getBackground();
			loadingAnimation.start();
			mLoadingView[position] = loadingView;*/
			
			MoveBackView moveBackView = (MoveBackView) lockerListLayout.findViewById(MResource.getIdByName(context, "id", "nq_moveback"));
			//moveBackView.startAnim();
			mLoadingView[position] = moveBackView;

			mNoNetworkView[position] = (ImageView) lockerListLayout.findViewById(MResource.getIdByName(context, "id", "iv_nonetwork"));
			mEmptyView[position] = (ImageView) lockerListLayout.findViewById(MResource.getIdByName(context, "id", "iv_empty"));
			final int column = position;
			OnClickListener clickListener = new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					getLockerList(column, 0);
				}

			};
			mLoadFailedLayout[position].setOnClickListener(clickListener);
			mNoNetworkView[position].setOnClickListener(clickListener);
			mEmptyView[position].setOnClickListener(clickListener);

            final PullToRefreshListView listView = (PullToRefreshListView) lockerListLayout.findViewById(MResource.getIdByName(context, "id", "lv_list"));
			mListView[position] = listView;
			listView.setAdapter(mLockerArrListAdapters[position]);
			mListView[position].setOnScrollListener(new LockerListScrollListener());
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
                    LockerManager.getInstance(context).getLockerList(column, 0, new BasicLockerListListener(column, 0,true));
                }

				@Override
				public void onLoadMore() {
					loadMoreData();
				}
            });

			container.addView(lockerListLayout, 0);

			int offset = mLockerArrListAdapters[position].getLockerList().size();
			if (viewPager.getCurrentItem() == position) {//只加载当前栏目
				getLockerList(position, offset);
			}
			return lockerListLayout;
		}
	}

	/**
	 * 锁屏模块viewPager，栏目切换监听。
	 */
	public class LockerOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageScrollStateChanged(int state) {
//			NqLog.d("FragmentLocker.MyOnPageChangeListener.onPageScrollStateChanged()");
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//			NqLog.d("FragmentLocker.MyOnPageChangeListener.onPageScrolled() position=" + position + " positionOffset="+positionOffset );
		}

		@Override
		public void onPageSelected(int position) {
			NqLog.d("FragmentLocker.MyOnPageChangeListener.onPageSelected() currIndex=" + position);
			currIndex = position;
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
					currIndex == 0 ? LockerActionConstants.ACTION_LOG_2103 : LockerActionConstants.ACTION_LOG_2102, null, 0, null);

			tvColumnNew.setTextColor(getResources().getColor(MResource.getIdByName(context, "color", "nq_text_store_column_title")));
			tvColumnTop.setTextColor(getResources().getColor(MResource.getIdByName(context, "color", "nq_text_store_column_title")));
			switch (currIndex) {
			case 0:
				tvColumnNew.setTextColor(getResources().getColor(MResource.getIdByName(context, "color", "nq_text_store_column_title_selected")));
				AnimationUtil.moveTo(ivCursor, CURSOR_ANIMATION_MILLISECONDS, cursorLastX, 0, 0, 0);
				ivCursor.setPadding(2 * cursorPadingPx, 0, cursorPadingPx, 0);
				cursorLastX = 0;
				break;
			case 1:
				tvColumnTop.setTextColor(getResources().getColor(MResource.getIdByName(context, "color", "nq_text_store_column_title_selected")));
				AnimationUtil.moveTo(ivCursor, CURSOR_ANIMATION_MILLISECONDS, cursorLastX, cursorWidth, 0, 0);
				ivCursor.setPadding(cursorPadingPx, 0, 2 * cursorPadingPx, 0);
				cursorLastX = cursorWidth;
				break;
			default:
				break;
			}
            int offset = mLockerArrListAdapters[currIndex].getLockerList().size();
            if(offset == 0) {//ListView为空，需要重新加载
				getLockerList(currIndex, offset);
            }
            showColumn(position);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == MResource.getIdByName(context, "id", "tv_column_new_lockers")) {
			viewPager.setCurrentItem(0);
		} else if (id == MResource.getIdByName(context, "id", "tv_column_top_lockers")) {
			viewPager.setCurrentItem(1);
		} else if (id == MResource.getIdByName(context, "id", "iv_nonetwork")) {

			getLockerList(currIndex, 0);
		} else if (id == MResource.getIdByName(context, "id", "iv_empty")) {
			getLockerList(currIndex, 0);
		}
	}

	/**
	 * 监听滚动条滚动。
	 */
	private class LockerListScrollListener implements AbsListView.OnScrollListener {

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

	/**
	 * 锁屏列表Adapter
	 */
	private class LockerArrListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<Locker[]> mLockerArrList;
		private int mLast = -1;

		public LockerArrListAdapter() {
			this.mInflater = LayoutInflater.from(context);
			this.mLockerArrList = new ArrayList<Locker[]>();
		}

		public List<Locker[]> getLockerList(){
			return mLockerArrList;
		}

		@Override
		public int getCount() {
			int count = 0;
			if (mLockerArrList != null) {
				return mLockerArrList.size();
			}
			return count;
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
				convertView = mInflater.inflate(MResource.getIdByName(context, "layout", "nq_locker_list_item3"), null);
				for (int i = 0; i < 3; i++) {
					holder.rl[i] = (RelativeLayout) convertView.findViewById(MResource.getIdByName(context, "id", "nq_locker_list_item"+i));
					holder.ivPreview[i] = (AsyncImageView) holder.rl[i].findViewById(MResource.getIdByName(context, "id", "iv_preview"));
					holder.tvName[i] = (TextView) holder.rl[i].findViewById(MResource.getIdByName(context, "id", "tv_name"));
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// 赋值
			final Locker[] lockers = mLockerArrList.get(position);
			if(mLast != position){
				mLast = position;

				for (int i = 0; i < 3; i++) {

					if(lockers[i] != null){
						String key = new StringBuilder().append(currIndex).append(lockers[i].getStrId()).toString();
						if((null == mActionLogs.get(key) || !mActionLogs.get(key))){
							mActionLogs.put(key, true);

                            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                            		LockerActionConstants.ACTION_LOG_2101, lockers[i].getStrId(), 0,
									currIndex + "_" + (3 * position + i));
						}
					}
				}

			}
			for (int i = 0; i < 3; i++) {

				if(lockers[i] != null){
					holder.rl[i].setVisibility(View.VISIBLE);
					holder.tvName[i].setText(lockers[i].getStrName());
					holder.ivPreview[i].setTag(lockers[i].getStrId());
					String url = lockers[i].getStrIconUrl();
					holder.ivPreview[i].loadImage(url, null, MResource
							.getIdByName(context, "drawable",
									"nq_load_default"));
					final int index = i;
					/**锁屏列表item点击事件*/
					holder.ivPreview[i].setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(context, LockerDetailActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.putExtra(LockerDetailActivity.KEY_LOCKERS, LockerArrToLocker(mLockerArrList));
							intent.putExtra(LockerDetailActivity.KEY_LOCKER_POS, 3 * position + index);
							context.startActivity(intent);

                            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                            		LockerActionConstants.ACTION_LOG_2104, lockers[index].getStrId(), lockers[index].getStrId().startsWith("AD_")?1:0,
									currIndex + "_" + position);
						}
					});

				} else {
					holder.rl[i].setVisibility(View.INVISIBLE);
				}
			}

			return convertView;
		}

	}

	private static class ViewHolder {
		RelativeLayout[] rl = new RelativeLayout[3];
		AsyncImageView[] ivPreview = new AsyncImageView[3];
		TextView[] tvName = new TextView[3];
	}

	/**获取锁屏列表*/
	private void getLockerList(int column, int offset){
		NqLog.d("getLockerListFromCache: column=" + column + " offset=" + offset);
		LockerManager lockerMgr = LockerManager.getInstance(context);

		if(offset == 0){
			// 从缓存中获取锁屏列表
			List<Locker[]> lockers = lockerMgr.getArrLockerListFromCache(column);

			log("getLockerListFromCache", lockers);

			if (lockers != null && lockers.size() > 0) {
				hideLockerListLoading(column, 0, false);
				updateLockerList(column, offset, lockers);
			}
		}

		boolean bStarted = false;
		// 缓存过期，或者初始进入时网络是wifi情况下，需要联网获取
		if (lockerMgr.isCacheExpired(column) ||
				offset == 0 && NetworkUtils.isWifi(context)) {
			bStarted = true;
			showLockerListLoading(column);
			lockerMgr.getLockerList(column, 0, new BasicLockerListListener(column, offset,false));
		}
		// 下拉刷新
		if (offset > 0 && !bStarted) {
			showLockerListLoading(column);
			lockerMgr.getLockerList(column, offset, new BasicLockerListListener(column, offset,false));
		}

	}

	private void log(String TAG, List<Locker[]> lockers){
		if (lockers != null) {
            NqLog.d(TAG + " lockers.size()" + lockers.size());
            Locker[] ts = null;
            for (int i = 0; i < lockers.size(); i++) {
            	ts = lockers.get(i);
            	for(int j = 0; j < ts.length; j++) {
            		if(ts[j] != null && !TextUtils.isEmpty(ts[j].getStrId()))
            			NqLog.d(i + " " + TAG + " , ResourceId==" + ts[j].getStrId() + ", name=" + ts[j].getStrName());
            	}
            }
        } else {
            NqLog.d(TAG + " lockers == null");
        }
	}
	/** 刷新锁屏列表视图*/
	private void updateLockerList(int column, int offset, List<Locker[]> lockers) {
		NqLog.d("updateLockerList: column=" + column + " offset=" + offset + " lockers=" + lockers);
		if (lockers == null || lockers.size() == 0){
			return;
		}

		log("updateLockerList", lockers);

		if (offset == 0) {
			mLockerArrListAdapters[column].getLockerList().clear();
		}
//		List<Locker[]> merge = mergeFetched(mLockerArrListAdapters[column].getLockerList(), lockers);
//		mLockerArrListAdapters[column].getLockerList().clear();

//		log("merge", lockers);

		mLockerArrListAdapters[column].getLockerList().addAll(lockers);
		mLockerArrListAdapters[column].notifyDataSetChanged();

		if(offset > 0) {
			mListView[column].scrollBy(0, scrollBy);
		}
	}

	/**
	 * 隐藏loadingView
	 * @param column 0:新品板块，1:热门板块
	 * @param type 0:获取成功，1:失败，2:无网络
	 * */
	private void hideLockerListLoading(final int column, final int type, boolean isErr) {
		NqLog.d("hideLockerListLoading: column=" + column);
		MoveBackView loadingView = mLoadingView[column];
		if (loadingView == null){
			return;
		}
		loadingView.stopAnim();
		/*AnimationDrawable loadingAnimation = (AnimationDrawable) loadingView
				.getBackground();
		loadingAnimation.stop();
		loadingView.setVisibility(View.GONE);*/
		
		if (isErr) 
			mListView[column].onLoadErr(mLockerArrListAdapters[column].getCount() > 0);
		int newType = LockerManager.getInstance(context).hadAvailableLockerListCashe(column) ? 0 : type;
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

	/**显示loadingView*/
	private void showLockerListLoading(final int column) {
		NqLog.d("showLockerListLoading: column=" + column);
		MoveBackView loadingView = mLoadingView[column];
		if (loadingView == null){
			return;
		}
		boolean isMore = mLockerArrListAdapters[column].getCount() > 0;
		loadingView.startAnim(isMore);
		if(isMore)
			mListView[column].onLoadMore();
		/*AnimationDrawable loadingAnimation = (AnimationDrawable) loadingView
				.getBackground();
		loadingAnimation.start();
		loadingView.setVisibility(View.VISIBLE);*/
	}

	/**从网络获取锁屏列表监听*/
	private class BasicLockerListListener implements LockerListStoreListener {
		private int mColumn;
		private int mOffset;
        private boolean mPullrefresh;

		public BasicLockerListListener(int column, int offset,boolean pullrefresh) {
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
					if (mLockerArrListAdapters[mColumn].getLockerList().size() > 0){
						hideLockerListLoading(mColumn, 0, true);
					} else {
						//TODO 显示下载失败，请重试的视图
						ToastUtils.toast(context, "nq_connection_failed");
						hideLockerListLoading(mColumn, 1, true);
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
					hideLockerListLoading(mColumn, 2, true);
                    if(mPullrefresh){
                        mListView[currIndex].onRefreshComplete();
                    }
				}
			});

			ToastUtils.toast(context, "nq_nonetwork");
		}

		@Override
		public void onGetLockerListSucc(final int column, final int offset,
				final List<Locker[]> lockers) {
			NqLog.d("onGetLockerListSucc: column=" + column + " offset=" + offset + " lockers=" + lockers);
			isScrolling[currIndex] = false;
			Activity activity = getActivity();
			if (activity == null){
				return;
			}
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					NqLog.d("onGetLockerListSucc.runOnUiThread: column=" + column + " offset=" + offset + " lockers=" + lockers);
					hideLockerListLoading(column, 0, false);
					updateLockerList(column, offset, lockers);

                    if(LockerManager.getCount(lockers) < LockerConstants.STORE_LOCKER_LIST_PAGE_SIZE
							|| loadedFlags[column]){//视为末页
						loadedFlags[column] = true;
					} else {
						loadedFlags[column] = false;
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
	public void onActivityCreated(Bundle savedInstanceState) {
        NqLog.d("FragmentLocker.onActivityCreated");
        super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
        NqLog.d("FragmentLocker.onAttach");
        super.onAttach(activity);
	}

	@Override
	public void onStart() {
        NqLog.d("FragmentLocker.onStart");
        super.onStart();
	}

	@Override
	public void onResume() {
        NqLog.d("FragmentLocker.onResume");
		super.onResume();
		if (!isHidden()) {
			mBannerUI.startAutoSlide();
		}
	}

	@Override
	public void onPause() {
        NqLog.d("FragmentLocker.onPause");
		if (!isHidden()) {
			mBannerUI.stopAutoSlide();
		}
		super.onPause();
	}

	@Override
	public void onStop() {
        NqLog.d("FragmentLocker.onStop");
        super.onStop();
	}

	@Override
	public void onDestroyView() {
        NqLog.d("FragmentLocker.onDestroyView");
        mBannerUI.disableAutoSlide();
        super.onDestroyView();
	}

	@Override
	public void onDestroy() {
        NqLog.d("FragmentLocker.onDestroy");
        if(mActionLogs != null){
        	mActionLogs.clear();
        	mActionLogs = null;
        }
        super.onDestroy();
	}

	@Override
	public void onDetach() {
        NqLog.d("FragmentLocker.onDetach");
        super.onDetach();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
        NqLog.d("FragmentLocker.onViewCreated");
        super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
        NqLog.d("FragmentLocker.onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
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
		if (mLockerArrListAdapters[column].getCount() <= 0) {
			//getLockerList(column, 0);
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
		for (int i = 0; i < ITEMS_PER_ROW; i++) {
			AsyncImageView imgView = holder.ivPreview[i];
			if (imgView == null) {
				continue;
			}
//			NqLog.d("hideViewHolder: ivPreview.mUrl = " + imgView.getUrl()  + " i=" + i);
			imgView.onHide();
			//imgView.setVisibility(View.INVISIBLE);
		}
	}
	
	private void showViewHolder(ViewHolder holder){
        if(holder == null){
            return;
        }

		for (int i = 0; i < ITEMS_PER_ROW; i++) {
			AsyncImageView imgView = holder.ivPreview[i];
			if (imgView == null) {
				continue;
			}
			NqLog.d("showViewHolder: ivPreview.mUrl = " + imgView.getUrl()  + " i=" + i);
			imgView.onShow();
			//imgView.setVisibility(View.VISIBLE);
		}
	}
	
	public void loadMoreData() {
		//如果是自动加载,可以在这里放置异步加载数据的代码
		isScrolling[currIndex] = true;
		int size = mLockerArrListAdapters[viewPager.getCurrentItem()].getLockerList().size();
		Locker[] tmp = mLockerArrListAdapters[viewPager.getCurrentItem()].getLockerList().get(size - 1);
		int offset = 3 * size;
		for(int i=0; i<tmp.length; i++){
			if(tmp[i] == null || TextUtils.isEmpty(tmp[i].getStrId()))
				offset --;
		}

		NqLog.d("loading... " + visibleLastIndex[currIndex] + ",offset=" + offset);
		getLockerList(currIndex, offset);
	}
}
