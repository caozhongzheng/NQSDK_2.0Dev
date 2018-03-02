package com.nqmobile.livesdk.modules.theme;

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

import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.commons.ui.MoveBackView;
import com.nqmobile.livesdk.commons.ui.PullToRefreshListView;
import com.nqmobile.livesdk.modules.banner.BannerConstants;
import com.nqmobile.livesdk.modules.banner.BannerUI;
import com.nqmobile.livesdk.modules.daily.DailyActionConstants;
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
 * 主题主页
 * @author changxiaofei
 * @time 2013-11-18 下午4:52:58
 */
public class FragmentTheme extends Fragment implements View.OnClickListener {
	private static final ILogger NqLog = LoggerFactory.getLogger(ThemeModule.MODULE_NAME);
	
	private static final int COLUMN_COUNT = 2; //2个栏目
	private static final int CURSOR_ANIMATION_MILLISECONDS = 1;
	private static final int ITEMS_PER_ROW = 3; //每行3个主题icon
	//private static final int WHAT_GET_BANNER_LIST = 1;
//	private static final int WHAT_GET_THEME_LIST = 2;
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
	private ThemeArrListAdapter[] mThemeArrListAdapters;
	private int[] visibleLastIndex = {0, 0};
	private int currIndex = 0;// 主题列表ViewPager当前页卡编号

	private boolean[] loadedFlags;
	private boolean[] isScrolling;
	private int scrollBy;
    private ImageView mLocalTheme;
    // 行为日志
 	private Map<String, Boolean> mActionLogs;
    private boolean mPointCenterEnable;

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
        View themeView = inflater.inflate(MResource.getIdByName(context, "layout", "nq_fragment_theme"), container, false);
		// Banner控件
		mBannerLayout = (LinearLayout) themeView.findViewById(MResource.getIdByName(context, "id", "bannerLayout"));
		mBannerUI = new BannerUI(mBannerLayout, new Handler(), BannerConstants.STORE_PLATE_TYPE_THEME);
		mBanner = (RelativeLayout) themeView.findViewById(MResource.getIdByName(context, "id", "rl_banner"));
		mBannerUI.enableAutoSlide();
		// 初始化栏目指示器
		ivCursor = (ImageView) themeView.findViewById(MResource.getIdByName(context, "id", "tv_cursor"));
		screenWidth = context.getResources().getDisplayMetrics().widthPixels;// 获取分辨率宽度
		cursorWidth = (int) (screenWidth / 2 + 0.5f);
		lpCursor = new RelativeLayout.LayoutParams(cursorWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		ivCursor.setPadding(2 * cursorPadingPx, 0, cursorPadingPx, 0);
		ivCursor.setLayoutParams(lpCursor);
		// 初始化栏目
		tvColumnNew = (TextView) themeView.findViewById(MResource.getIdByName(context, "id", "tv_column_new_themes"));
		tvColumnNew.setTextColor(getResources().getColor(MResource.getIdByName(context, "color", "nq_text_store_column_title_selected")));
		tvColumnTop = (TextView) themeView.findViewById(MResource.getIdByName(context, "id", "tv_column_top_themes"));
		// 初始化栏目列表
		viewPager = (ViewPager) themeView.findViewById(MResource.getIdByName(context, "id", "vp_list"));
		viewPager.setAdapter(new ThemePagerAdapter());
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new ThemeOnPageChangeListener());

		mLoadFailedLayout = new LinearLayout[COLUMN_COUNT];
		mLoadingView = new MoveBackView[COLUMN_COUNT];
		mNoNetworkView = new ImageView[COLUMN_COUNT];
		mEmptyView = new ImageView[COLUMN_COUNT];

		mThemeArrListAdapters = new ThemeArrListAdapter[COLUMN_COUNT];
		for(int i=0; i<COLUMN_COUNT; i++){
			mThemeArrListAdapters[i] = new ThemeArrListAdapter();
		}
		mListView = new PullToRefreshListView[COLUMN_COUNT];

		loadedFlags = new boolean[2];
		isScrolling = new boolean[2];
		tvColumnNew.setOnClickListener(this);
		tvColumnTop.setOnClickListener(this);

		mLocalTheme = (ImageView) themeView.findViewById(MResource.getIdByName(context, "id", "btn_local"));
		if(ClientInfo.hasLocalTheme()){
			mLocalTheme.setVisibility(View.VISIBLE);
			mLocalTheme.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					try{
						StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION, DailyActionConstants.ACTION_LOG_1603, null, 0, null);

						Intent i = new Intent();
						i.setAction("com.lqlauncher.LocalTheme");
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(i);
					}catch (ActivityNotFoundException e){
						e.printStackTrace();
					}
				}
			});
		}

        mTitle = (RelativeLayout) themeView.findViewById(MResource.getIdByName(context,"id","title_layout"));
        mTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mListView[currIndex].setSelection(1);
            }
        });

        mPointCenterEnable = PointsPreference.getInstance().getBooleanValue(PointsPreference.KEY_POINT_CENTER_ENABLE);
        ImageView pointCenter = (ImageView) themeView.findViewById(MResource.getIdByName(context,"id","pointCenter"));
        pointCenter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, PointsCenterActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);

                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION, PointActionConstants.ACTION_LOG_2501,null,0,null);
            }
        });

        if(mPointCenterEnable){
            pointCenter.setVisibility(View.VISIBLE);
        }else{
            pointCenter.setVisibility(View.GONE);
        }

        return themeView;
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

	protected List<Theme[]> mergeFetched(List<Theme[]> listShow, List<Theme[]> list) {
		if(list == null || ThemeManager.getCount(list) == 0)
			return listShow;
		if(listShow == null || ThemeManager.getCount(listShow) == 0)
			return list;

		List<Theme> have = ThemeArrToTheme(listShow);
		List<Theme> gets = ThemeArrToTheme(list);
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

		return ThemeToThemeArr(have);
	}

	List<Theme[]> ThemeToThemeArr (List<Theme> list) {
		List<Theme[]> result = new ArrayList<Theme[]>();
		if(list == null || list.size() < 1)
			return result;

		Theme[] arrTheme = null;
		int size = list.size();
		for(int i=0; i<size; i++){
			if(i%3 == 0){
				arrTheme = new Theme[3];
			}
			arrTheme[i%3] = list.get(i);
			if(i%3 == 2){
				result.add(arrTheme);
			}
		}

		if(size%3 != 0){
			result.add(arrTheme);
		}
		return result;
	}

	List<Theme> ThemeArrToTheme (List<Theme[]> list) {
		List<Theme> result = new ArrayList<Theme>();
		if(list == null || list.isEmpty())
			return result;

		for(Theme[] group : list){
			for(Theme theme : group ){
				if(theme != null){
					result.add(theme);
				}
			}
		}
		return result;
	}

	/**
	 * 主题模块，栏目切换PagerAdapter
	 */
	public class ThemePagerAdapter extends PagerAdapter {

		public ThemePagerAdapter() {
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
			final LinearLayout themeListLayout = (LinearLayout)LayoutInflater.from(context).inflate(MResource.getIdByName(context, "layout", "nq_theme_list"), null);

			LinearLayout loadingLayout = (LinearLayout) themeListLayout.findViewById(MResource.getIdByName(context, "id", "ll_load_failed"));
			mLoadFailedLayout[position] = loadingLayout;
			
			MoveBackView moveBackView = (MoveBackView) themeListLayout.findViewById(MResource.getIdByName(context, "id", "nq_moveback"));
			mLoadingView[position] = moveBackView;
			
			mNoNetworkView[position] = (ImageView) themeListLayout.findViewById(MResource.getIdByName(context, "id", "iv_nonetwork"));
			mEmptyView[position] = (ImageView) themeListLayout.findViewById(MResource.getIdByName(context, "id", "iv_empty"));
			final int column = position;
			OnClickListener clickListener = new OnClickListener(){

				@Override
				public void onClick(View v) {
					getThemeList(column, 0);
				}

			};
			mLoadFailedLayout[position].setOnClickListener(clickListener);
			mNoNetworkView[position].setOnClickListener(clickListener);
			mEmptyView[position].setOnClickListener(clickListener);

            final PullToRefreshListView listView = (PullToRefreshListView) themeListLayout.findViewById(MResource.getIdByName(context, "id", "lv_list"));
			mListView[position] = listView;
			listView.setAdapter(mThemeArrListAdapters[position]);
			mListView[position].setOnScrollListener(new ThemeListScrollListener());
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
                    ThemeManager.getInstance(context).getThemeList(column, 0, new BasicThemeListListener(column, 0,true));
                }

				@Override
				public void onLoadMore() {
					loadMoreData();
				}
            });

			container.addView(themeListLayout, 0);

			int offset = mThemeArrListAdapters[position].getThemeList().size();
			if (viewPager.getCurrentItem() == position) {//只加载当前栏目
				getThemeList(position, offset);
			}
			return themeListLayout;
		}
	}

	/**
	 * 主题模块viewPager，栏目切换监听。
	 */
	public class ThemeOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageScrollStateChanged(int state) {
//			NqLog.d("FragmentTheme.MyOnPageChangeListener.onPageScrollStateChanged()");
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//			NqLog.d("FragmentTheme.MyOnPageChangeListener.onPageScrolled() position=" + position + " positionOffset="+positionOffset );
		}

		@Override
		public void onPageSelected(int position) {
			NqLog.d("FragmentTheme.MyOnPageChangeListener.onPageSelected() currIndex=" + position);
			currIndex = position;
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
					currIndex == 0 ? ThemeActionConstants.ACTION_LOG_1203 : ThemeActionConstants.ACTION_LOG_1202, null, 0, null);

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
            int offset = mThemeArrListAdapters[currIndex].getThemeList().size();
            if(offset == 0) {//ListView为空，需要重新加载
				getThemeList(currIndex, offset);
            }
            showColumn(position);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == MResource.getIdByName(context, "id", "tv_column_new_themes")) {
			viewPager.setCurrentItem(0);
		} else if (id == MResource.getIdByName(context, "id", "tv_column_top_themes")) {
			viewPager.setCurrentItem(1);
		} else if (id == MResource.getIdByName(context, "id", "iv_nonetwork")) {

			getThemeList(currIndex, 0);
		} else if (id == MResource.getIdByName(context, "id", "iv_empty")) {
			getThemeList(currIndex, 0);
		}
	}

	/**
	 * 监听滚动条滚动。
	 */
	private class ThemeListScrollListener implements AbsListView.OnScrollListener {

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
	 * 主题列表Adapter
	 */
	private class ThemeArrListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<Theme[]> mThemeArrList;
		private int mLast = -1;

		public ThemeArrListAdapter() {
			this.mInflater = LayoutInflater.from(context);
			this.mThemeArrList = new ArrayList<Theme[]>();
		}

		public List<Theme[]> getThemeList(){
			return mThemeArrList;
		}

		@Override
		public int getCount() {
			int count = 0;
			if (mThemeArrList != null) {
				return mThemeArrList.size();
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
				convertView = mInflater.inflate(MResource.getIdByName(context, "layout", "nq_theme_list_item3"), null);
				for (int i = 0; i < 3; i++) {
					holder.rl[i] = (LinearLayout) convertView.findViewById(MResource.getIdByName(context, "id", "nq_theme_list_item"+i));
					holder.ivPreview[i] = (AsyncImageView) holder.rl[i].findViewById(MResource.getIdByName(context, "id", "iv_preview"));
					holder.tvName[i] = (TextView) holder.rl[i].findViewById(MResource.getIdByName(context, "id", "tv_name"));
                    holder.tvPoint[i] = (TextView) holder.rl[i].findViewById(MResource.getIdByName(context,"id","tv_points"));
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// 赋值
			final Theme[] themes = mThemeArrList.get(position);
			if(mLast != position){
				mLast = position;
				for (int i = 0; i < 3; i++) {
					if(themes[i] != null){
						String key = new StringBuilder().append(currIndex).append(themes[i].getStrId()).toString();
						if((null == mActionLogs.get(key) || !mActionLogs.get(key))){
							mActionLogs.put(key, true);
                            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                            		ThemeActionConstants.ACTION_LOG_1201, themes[i].getStrId(), 0,
									currIndex + "_" + (3 * position + i));
						}
					}
				}

			}
			for (int i = 0; i < 3; i++) {
				if(themes[i] != null){
					holder.rl[i].setVisibility(View.VISIBLE);
					holder.tvName[i].setText(themes[i].getStrName());
					holder.ivPreview[i].setTag(themes[i].getStrId());
					String url = themes[i].getStrIconUrl();
					holder.ivPreview[i].loadImage(url, null, MResource.getIdByName(context, "drawable","nq_load_default"));
					final int index = i;
					/**主题列表item点击事件*/
					holder.ivPreview[i].setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(context, ThemeDetailActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.putExtra(ThemeDetailActivity.KEY_THEME, themes[index]);
							context.startActivity(intent);

                            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                            		ThemeActionConstants.ACTION_LOG_1204, themes[index].getStrId(), 0,
									currIndex + "_" + position);
						}
					});

                    if(mPointCenterEnable){
                        holder.tvPoint[i].setVisibility(View.VISIBLE);
                        int points = themes[i].getConsumePoints();
                        if(points > 0){
                            holder.tvPoint[i].setText(MResource.getString(context,"nq_theme_points",String.valueOf(points)));
                        }else{
                            holder.tvPoint[i].setText(MResource.getString(context,"nq_theme_free"));
                        }
                    }else{
                        holder.tvPoint[i].setVisibility(View.GONE);
                    }
				} else {
					holder.rl[i].setVisibility(View.INVISIBLE);
				}
			}

			return convertView;
		}

	}

	private static class ViewHolder {
        LinearLayout[] rl = new LinearLayout[3];
		AsyncImageView[] ivPreview = new AsyncImageView[3];
		TextView[] tvName = new TextView[3];
        TextView[] tvPoint = new TextView[3];
	}

	/**获取主题列表*/
	private void getThemeList(int column, int offset){
		NqLog.d("getThemeListFromCache: column=" + column + " offset=" + offset);
		ThemeManager themeMgr = ThemeManager.getInstance(context);

		if(offset == 0){
			// 从缓存中获取主题列表
			List<Theme[]> themes = themeMgr.getArrThemeListFromCache(column);

			log("getThemeListFromCache", themes);

			if (themes != null && themes.size() > 0) {
				hideThemeListLoading(column, 0, false);
				updateThemeList(column, offset, themes);
			}
		}

		boolean bStarted = false;
		// 缓存过期，或者初始进入时网络是wifi情况下，需要联网获取
		if (themeMgr.isCacheExpired(column) ||
				offset == 0 && NetworkUtils.isWifi(context)) {
			bStarted = true;
			showThemeListLoading(column);
			themeMgr.getThemeList(column, 0, new BasicThemeListListener(column, offset,false));
		}
		// 下拉刷新
		if (offset > 0 && !bStarted) {
			showThemeListLoading(column);
			themeMgr.getThemeList(column, offset, new BasicThemeListListener(column, offset,false));
		}

	}

	private void log(String TAG, List<Theme[]> themes){
		if (themes != null) {
            NqLog.d(TAG + " themes.size()" + themes.size());
            Theme[] ts = null;
            for (int i = 0; i < themes.size(); i++) {
            	ts = themes.get(i);
            	for(int j = 0; j < ts.length; j++) {
            		if(ts[j] != null && !TextUtils.isEmpty(ts[j].getStrId()))
            			NqLog.d(i + " " + TAG + " , ResourceId==" + ts[j].getStrId() + ", name=" + ts[j].getStrName());
            	}
            }
        } else {
            NqLog.d(TAG + " themes == null");
        }
	}

	/** 刷新主题列表视图*/
	private void updateThemeList(int column, int offset, List<Theme[]> themes) {
		NqLog.d("updateThemeList: column=" + column + " offset=" + offset + " themes=" + themes);
		if (themes == null || themes.size() == 0){
			return;
		}

		log("updateThemeList", themes);

		if (offset == 0) {
			mThemeArrListAdapters[column].getThemeList().clear();
		}
//		List<Theme[]> merge = mergeFetched(mThemeArrListAdapters[column].getThemeList(), themes);
//		mThemeArrListAdapters[column].getThemeList().clear();

		mThemeArrListAdapters[column].getThemeList().addAll(themes);
		mThemeArrListAdapters[column].notifyDataSetChanged();

		if(offset > 0) {
			mListView[column].scrollBy(0, scrollBy);
		}
	}

	/**
	 * 隐藏loadingView
	 * @param column 0:新品板块，1:热门板块
	 * @param type 0:获取成功，1:失败，2:无网络
	 * */
	private void hideThemeListLoading(final int column, final int type, boolean isErr) {
		NqLog.d("hideThemeListLoading: column=" + column);
		MoveBackView loadingView = mLoadingView[column];
		if (loadingView == null){
			return;
		}
		loadingView.stopAnim();

		if(isErr)
			mListView[column].onLoadErr(mThemeArrListAdapters[column].getCount() > 0);
		else {
			mListView[column].onLoadMoreComplete();
		}
		int newType = ThemeManager.getInstance(context).hadAvailableThemeListCashe(column) ? 0 : type;
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
	private void showThemeListLoading(final int column) {
		NqLog.d("showThemeListLoading: column=" + column);
		MoveBackView loadingView = mLoadingView[column];
		if (loadingView == null){
			return;
		}
		boolean isMore = mThemeArrListAdapters[column].getCount() > 0;
		loadingView.startAnim(isMore);
		if(isMore)
			mListView[column].onLoadMore();
	}

	/**从网络获取主题列表监听*/
	private class BasicThemeListListener implements ThemeListStoreListener {
		private int mColumn;
		private int mOffset;
        private boolean mPullrefresh;

		public BasicThemeListListener(int column, int offset,boolean pullrefresh) {
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
					if (mThemeArrListAdapters[mColumn].getThemeList().size() > 0){
						hideThemeListLoading(mColumn, 0, true);
					} else {
						// 显示下载失败，请重试的视图
						toast("nq_connection_failed");
						hideThemeListLoading(mColumn, 1, true);
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
					hideThemeListLoading(mColumn, 2, true);
                    if(mPullrefresh){
                        mListView[currIndex].onRefreshComplete();
                    }
				}
			});

			ToastUtils.toast(context, "nq_nonetwork");
		}

		@Override
		public void onGetThemeListSucc(final int column, final int offset,
				final List<Theme[]> themes) {
			NqLog.d("onGetThemeListSucc: column=" + column + " offset=" + offset + " themes=" + themes);
			isScrolling[currIndex] = false;
			Activity activity = getActivity();
			if (activity == null){
				return;
			}
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					NqLog.d("onGetThemeListSucc.runOnUiThread: column=" + column + " offset=" + offset + " themes=" + themes);

                    if(themes != null){
                    	if(themes.size() == 0 && mThemeArrListAdapters[column].getThemeList().size() == 0){
                    		onErr();
                    	}else {
                    		hideThemeListLoading(column, 0, false);
                    		updateThemeList(column, offset, themes);
                    		
                    		if(ThemeManager.getCount(themes) < ThemeConstants.STORE_THEME_LIST_PAGE_SIZE
                    				|| loadedFlags[column]){//视为末页
                    			loadedFlags[column] = true;
                    		} else {
                    			loadedFlags[column] = false;
                    		}
                    	}
                    }else{
                        hideThemeListLoading(column, 1, false);
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
        NqLog.d("FragmentTheme.onResume");
		super.onResume();
		if (!isHidden()) {
			mBannerUI.startAutoSlide();
		}
	}

	@Override
	public void onPause() {
        NqLog.d("FragmentTheme.onPause");
		if (!isHidden()) {
			mBannerUI.stopAutoSlide();
		}
		super.onPause();
	}

	@Override
	public void onDestroyView() {
        NqLog.d("FragmentTheme.onDestroyView");
        mBannerUI.disableAutoSlide();
        super.onDestroyView();
	}

	@Override
	public void onDestroy() {
        NqLog.d("FragmentTheme.onDestroy");
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
		if (mThemeArrListAdapters[column].getCount() <= 0) {
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
		for (int i = 0; i < ITEMS_PER_ROW; i++) {
			AsyncImageView imgView = holder.ivPreview[i];
			if (imgView == null) {
				continue;
			}
			NqLog.d("hideViewHolder: ivPreview.mUrl = " + imgView.getUrl()  + " i=" + i);
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
		int size = mThemeArrListAdapters[viewPager.getCurrentItem()].getThemeList().size();
		Theme[] tmp = mThemeArrListAdapters[viewPager.getCurrentItem()].getThemeList().get(size - 1);
		int offset = 3 * size;
		for(int i=0; i<tmp.length; i++){
			if(tmp[i] == null || TextUtils.isEmpty(tmp[i].getStrId()))
				offset --;
		}

		NqLog.d("loading... " + visibleLastIndex[currIndex] + ",offset=" + offset);
		getThemeList(currIndex, offset);
	}
	
}
