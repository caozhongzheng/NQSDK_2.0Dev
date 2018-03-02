package com.nqmobile.livesdk.modules.font;

import java.io.File;
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
import android.util.Log;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.MoveBackView;
import com.nqmobile.livesdk.commons.ui.PullToRefreshListView;
import com.nqmobile.livesdk.commons.ui.StoreMainActivity;
import com.nqmobile.livesdk.modules.banner.BannerConstants;
import com.nqmobile.livesdk.modules.banner.BannerUI;
import com.nqmobile.livesdk.utils.AnimationUtil;
import com.nqmobile.livesdk.utils.DisplayUtil;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;
import com.xinmei365.fontsdk.bean.Font;

/**
 * Font主页
 * @author liujiancheng
 * @time 2015-01-27
 */
public class FragmentFont extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {
	private static final ILogger NqLog = LoggerFactory.getLogger(FontModule.MODULE_NAME);
	
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
	private TextView tvColumnNewFonts, tvColumnHotFonts;
	private ImageView mLocalFont;
	private ImageView ivCursor;
	private RelativeLayout.LayoutParams lpCursor;
	private PullToRefreshListView[] mListView;
	// 数据
	private FontListAdapter[] mFontListAdapters;
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
		View fontView = inflater.inflate(MResource.getIdByName(mContext, "layout", "nq_fragment_font"), container, false);
		// 初始化Banner控件
		mBannerLayout = (LinearLayout) fontView.findViewById(MResource.getIdByName(mContext, "id", "bannerLayout"));
		mBannerUI = new BannerUI(mBannerLayout, new Handler(), BannerConstants.STORE_PLATE_TYPE_APP);
		mBanner = (RelativeLayout) fontView.findViewById(MResource.getIdByName(mContext, "id", "rl_banner"));
		mBannerUI.enableAutoSlide();
		// 初始化栏目指示器
		ivCursor = (ImageView) fontView.findViewById(MResource.getIdByName(mContext, "id", "tv_cursor"));
		screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;// 获取分辨率宽度
		cursorWidth = (int) (screenWidth / 2 + 0.5f);
		lpCursor = new RelativeLayout.LayoutParams(cursorWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		ivCursor.setPadding(2 * cursorPadingPx, 0, cursorPadingPx, 0);
		ivCursor.setLayoutParams(lpCursor);
		// 初始化栏目
		tvColumnNewFonts = (TextView) fontView.findViewById(MResource.getIdByName(mContext, "id", "tv_column_new_fonts"));
		tvColumnNewFonts.setTextColor(getResources().getColor(MResource.getIdByName(mContext, "color", "nq_text_store_column_title_selected")));
		tvColumnHotFonts = (TextView) fontView.findViewById(MResource.getIdByName(mContext, "id", "tv_column_hot_fonts"));
		// 初始化栏目的列表
		mViewPager = (ViewPager) fontView.findViewById(MResource.getIdByName(mContext, "id", "vp_list"));
		mViewPager.setAdapter(new FontColumnsPagerAdapter());
		mViewPager.setCurrentItem(0);
		mViewPager.setOnPageChangeListener(new AppColumnsOnPageChangeListener());
		
		mFontListAdapters = new FontListAdapter[COLUMN_COUNT];
		mListView = new PullToRefreshListView[COLUMN_COUNT];
		for(int i=0; i<COLUMN_COUNT; i++){
			mFontListAdapters[i] = new FontListAdapter();
		}
		mLoadFailedLayout = new LinearLayout[COLUMN_COUNT];
		mLoadingView = new MoveBackView[COLUMN_COUNT];
		mNoNetworkView = new ImageView[COLUMN_COUNT];
		mEmptyView = new ImageView[COLUMN_COUNT];
		
		loadedFlags = new boolean[2];
		isScrolling = new boolean[2];
		tvColumnNewFonts.setOnClickListener(this);
		tvColumnHotFonts.setOnClickListener(this);

        mTitle = (RelativeLayout) fontView.findViewById(MResource.getIdByName(mContext,"id","title_layout"));
        mTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mListView[currIndex].setSelection(1);
            }
        });
        
        mLocalFont = (ImageView) fontView.findViewById(MResource.getIdByName(mContext,"id","local_font"));
        mLocalFont.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("ljc", "mLocalFont is onclick");
                Intent intent = null;

                intent = new Intent(mContext, FontLocalManagerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                mContext.startActivity(intent);
                
            }
        });
		return fontView;
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
	public class FontColumnsPagerAdapter extends PagerAdapter {
		
		public FontColumnsPagerAdapter() { }
		
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
					getFontList(column, 0);
				}
			};
			mLoadFailedLayout[position].setOnClickListener(clickListener);
			mNoNetworkView[position].setOnClickListener(clickListener);
			mEmptyView[position].setOnClickListener(clickListener);

			PullToRefreshListView listView = (PullToRefreshListView) appListLayout.findViewById(MResource.getIdByName(mContext, "id", "lv_list"));
			mListView[position] = listView;
			listView.setAdapter(mFontListAdapters[position]);
            listView.setOnScrollListener(new FontListScrollListener());
			listView.setOnItemClickListener(FragmentFont.this);
			//listView.setFastScrollEnabled(true);
			listView.setRecyclerListener(new RecyclerListener() {
				@Override
				public void onMovedToScrapHeap(View view) {
					ViewHolder holder = (ViewHolder)view.getTag();
//					hideViewHolder(holder);			
				}
			});
            listView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadedFlags[column] = true;//每次都是true?
                    FontManager.getInstance(mContext).getFontListFromServer(column, new BasicFontListListener(column));
                }

				@Override
				public void onLoadMore() {
					loadMoreData();
				}
            });

			
			container.addView(appListLayout, 0);
			
			int offset = mFontListAdapters[position].getFontList().size();
			if (mViewPager.getCurrentItem() == position) {//只加载当前栏目
				getFontList(position, offset);
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
		}
		
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}
		
		@Override
		public void onPageSelected(int position) {
			NqLog.d("FragmentFont.MyOnPageChangeListener.onPageSelected() currIndex=" + position);
			currIndex = position;
			tvColumnNewFonts.setTextColor(getResources().getColor(MResource.getIdByName(mContext, "color", "nq_text_store_column_title")));
			tvColumnHotFonts.setTextColor(getResources().getColor(MResource.getIdByName(mContext, "color", "nq_text_store_column_title")));
			switch (position) {
			case 0:
				tvColumnNewFonts.setTextColor(getResources().getColor(MResource.getIdByName(mContext, "color", "nq_text_store_column_title_selected")));
				AnimationUtil.moveTo(ivCursor, CURSOR_ANIMATION_MILLISECONDS, cursorLastX, 0, 0, 0);
				ivCursor.setPadding(2 * cursorPadingPx, 0, cursorPadingPx, 0);
				cursorLastX = 0;
				break;
			case 1:
				tvColumnHotFonts.setTextColor(getResources().getColor(MResource.getIdByName(mContext, "color", "nq_text_store_column_title_selected")));
				AnimationUtil.moveTo(ivCursor, CURSOR_ANIMATION_MILLISECONDS, cursorLastX, cursorWidth, 0, 0);
				ivCursor.setPadding(cursorPadingPx, 0, 2 * cursorPadingPx, 0);
				cursorLastX = cursorWidth;
				break;
			default:
				break;
			}
			
			int offset = mFontListAdapters[position].getFontList().size();
			if (offset == 0) {//当前listview没数据
				getFontList(position, offset);
			}
			showColumn(position);
		}
	}
	
	/**
	 * 应用列表Adapter
	 */
	private class FontListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<NqFont> mFontList;
		private int mLast = -1;
		
		public FontListAdapter() {
			mInflater = LayoutInflater.from(mContext);
			mFontList = new ArrayList<NqFont>();
		}
		
		public List<NqFont> getFontList(){
			return mFontList;
		}
		
		@Override
		public int getCount() {
			if (mFontList != null) {
				return mFontList.size();
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
				convertView = mInflater.inflate(MResource.getIdByName(mContext, "layout", "nq_font_online_list_item"), null);
				holder.tvName = (TextView) convertView.findViewById(MResource.getIdByName(mContext, "id", "tv_name"));
				holder.tvSize = (TextView) convertView.findViewById(MResource.getIdByName(mContext, "id", "tv_size"));
				convertView.setTag(holder);				
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// 赋值
			NqFont font = mFontList.get(position);
			holder.tvName.setText(font.getFont().getFontName());
			holder.tvSize.setText(StringUtil.formatSize(font.getFont().getFontSize()));
			
			FontManager.getInstance(mContext).setTypeface(font, holder.tvName, false);
			
			if(mLast != position){
				mLast = position;
//				String key = new StringBuilder().append(mViewPager.getCurrentItem()).append(app.getStrId()).toString();
//				if((null == mActionLogs.get(key) || !mActionLogs.get(key))){
//					mActionLogs.put(key, true);					
//				}

			}
						
			return convertView;
		}
		
	}
	
	private static class ViewHolder {
		TextView tvName;
		TextView tvSize;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		List<NqFont> list = mFontListAdapters[mViewPager.getCurrentItem()].getFontList();
		int pos = (int)parent.getAdapter().getItemId(position);//由于加了header，position会有偏差，因此需要先getAdapter获取内部包装过的Adapter
		if (list!=null && pos >=0 && pos < list.size()) {
			NqFont font = list.get(pos);
			FontManager.getInstance(mContext).viewFontDetail(mViewPager.getCurrentItem(), font);
		}
	}
	
	/**
	 * 监听滚动条滚动。
	 */
	private class FontListScrollListener implements AbsListView.OnScrollListener {
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
		if (id == MResource.getIdByName(mContext, "id", "tv_column_new_fonts")) {
			mViewPager.setCurrentItem(0, true);
		} else if (id == MResource.getIdByName(mContext, "id", "tv_column_hot_fonts")) {
			mViewPager.setCurrentItem(1, true);
		}
	}
	
	@Override
	public void onResume() {
		NqLog.d("FragmentFont.onResume");
		super.onResume();
		if (!isHidden()) {
			mBannerUI.startAutoSlide();
			mFontListAdapters[currIndex].notifyDataSetInvalidated();
		}
	}
	
	@Override
	public void onPause() {
		NqLog.d("FragmentFont.onPause");
		if (!isHidden()) {
			mBannerUI.stopAutoSlide();
		}
		super.onPause();
	}
	
	@Override
	public void onDestroyView() {
		NqLog.d("FragmentFont.onDestroyView");
		mBannerUI.disableAutoSlide();
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		NqLog.d("FragmentFont.onDestroy");
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
	

	private class BasicFontListListener implements FontListStoreListener {
		private int mColumn;

		public BasicFontListListener(int column) {
			mColumn = column;
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
					if (mFontListAdapters[mColumn].getFontList().size() > 0){
						hideFontListLoading(mColumn, 0, true);
					} else {
						// 显示下载失败，请重试的视图
						toast("nq_connection_failed");
						hideFontListLoading(mColumn, 1, true);
					}
//                    if(mPullrefresh){
//                        mListView[currIndex].onRefreshComplete();
//                    }
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
					hideFontListLoading(mColumn, 2, true);
//                    if(mPullrefresh){
//                        mListView[currIndex].onRefreshComplete();
//                    }
				}
			});
			
			toast("nq_nonetwork");
		}

		@Override
		public void onGetFontListSucc(final int column, final List<NqFont> fonts) {
			// TODO Auto-generated method stub
			NqLog.d("onGetAppListSucc: fonts=" + fonts);
			isScrolling[currIndex] = false;
			Activity activity = getActivity();
			if (activity == null){
				return;
			}
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
                    if(fonts != null){
                    	if(fonts.size() == 0 && mFontListAdapters[column].getFontList().size() == 0){
                    		onErr();
                    	}else {
                    		hideFontListLoading(column, 0, false);
                    		updateFontList(column, 0, fonts);
						}
                    }else {
                    	hideFontListLoading(column, 0, false);
					}
//
//                    if(mPullrefresh){
//                        mListView[currIndex].onRefreshComplete();
//                    }
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
	private void hideFontListLoading(final int column, final int type, boolean isErr) {
		MoveBackView loadingView = mLoadingView[column];
		NqLog.d("hideAppListLoading: column=" + column + " loadingView:" + loadingView);
		if (loadingView == null){
			return;
		}
		loadingView.stopAnim();

		if(isErr)
			mListView[column].onLoadErr(mFontListAdapters[column].getCount() > 0);
		else {
			mListView[column].onLoadMoreComplete();
		}
		int newType = 0;//AppManager.getInstance(mContext).hadAvailableAppListCashe(column) ? 0 : type;
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

	private void showFontListLoading(final int column) {
		NqLog.d("showAppListLoading: column=" + column);
		MoveBackView loadingView = mLoadingView[column];
		if (loadingView == null){
			return;
		}
		boolean isMore = mFontListAdapters[column].getCount() > 0;
		loadingView.startAnim(isMore);
		if(isMore)
			mListView[column].onLoadMore();
	}
	
	private void updateFontList(int column, int offset, List<NqFont> fonts) {
		NqLog.d("updateFontList: column=" + column + " offset=" + offset + " apps=" + fonts);
		if (fonts == null || fonts.size() == 0){
			return;
		}
			
		if (offset == 0) {
			mFontListAdapters[column].getFontList().clear();
		}
		mFontListAdapters[column].getFontList().addAll(fonts);
		mFontListAdapters[column].notifyDataSetChanged();
		
		if(offset > 0) {
			mListView[column].scrollBy(0, scrollBy);
		}
	}
	
	private void getFontList(int column, int offset){
		NqLog.d("getAppList: column=" + column + " offset=" + offset);
		FontManager fontManager = FontManager.getInstance(mContext);
		
		// 先从本地数据库获取
		List<NqFont> list = fontManager.getOnlineFontListFromLocal(column);
		if (list != null && list.size() > 0) {
    		hideFontListLoading(column, 0, false);
    		updateFontList(column, 0, list);
			return;
		}
		
		// 缓存过期，或者初始进入时网络是wifi情况下，需要联网获取
		if (fontManager.isCacheExpired(column) && NetworkUtils.isWifi(mContext)) {
			showFontListLoading(column);
			fontManager.getFontListFromServer(column, new BasicFontListListener(column));
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
//			hideViewHolder(holder);
		}
	}
	
	public void showColumn(int column) {
		if (mFontListAdapters[column].getCount() <= 0) {
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
//			showViewHolder(holder);
		}
	}
	
//	private void hideViewHolder(ViewHolder holder){
//		if (holder == null) {
//			return;
//		}
//		NqLog.d("hideViewHolder: ivIcon.mUrl = " + holder.ivIcon.getUrl());
//		holder.ivIcon.onHide();
//	}
//	
//	private void showViewHolder(ViewHolder holder){
//		if (holder == null) {
//			return;
//		}
//		NqLog.d("showViewHolder: ivIcon.mUrl = " + holder.ivIcon.getUrl());
//		holder.ivIcon.onShow();
//	}
	
	public void loadMoreData() {
		//如果是自动加载,可以在这里放置异步加载数据的代码
		isScrolling[currIndex] = true;
		ArrayList<NqFont> list = (ArrayList<NqFont>) mFontListAdapters[mViewPager.getCurrentItem()].getFontList();
		int offset = list == null ? 0 : list.size();

		NqLog.d("loading... " + visibleLastIndex[currIndex] + ",offset=" + offset);
		getFontList(currIndex, offset);
	}
}
