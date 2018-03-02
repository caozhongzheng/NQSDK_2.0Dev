package com.nqmobile.livesdk.modules.points;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.commons.ui.MoveBackView;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.font.FontManager;
import com.nqmobile.livesdk.modules.points.model.PointsInfo;
import com.nqmobile.livesdk.modules.points.model.RewardPointsResp;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.theme.ThemeConstants;
import com.nqmobile.livesdk.modules.theme.ThemeDetailActivity;
import com.nqmobile.livesdk.modules.theme.ThemeManager;
import com.nqmobile.livesdk.utils.AnimationUtil;
import com.nqmobile.livesdk.utils.DisplayUtil;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * Created by Rainbow on 14-5-7.
 */
public class PointsCenterActivity extends BaseActvity{
	private static final ILogger NqLog = LoggerFactory.getLogger(PointModule.MODULE_NAME);

    private PointsManager mManager;
    private TextView mCurrentPoints;
    private TextView mExpidTime;
    private PointsPreference mHelper;
    private ViewPager mViewPager;
    private static final int COLUMN_COUNT = 2;
    private Context mContext;
    private ListView[] mListView;
    private AppListAdapter mAppDapter;
    private ThemeListAdaper mThemeAdapter;
    private LinearLayout[] mLoadFailedLayout;
    private MoveBackView[] mLoadingView;
    private ImageView[] mNoNetworkView;
    private ImageView[] mEmptyView;
    private TextView mPointAppTv, mPointThemeTv;
    private ImageView ivCursor;
    private int currIndex = 0;
    private int cursorPadingPx;
    private int screenWidth;
    private int cursorWidth;
    private int cursorLastX;
    private LinearLayout.LayoutParams lpCursor;
    private static final int CURSOR_ANIMATION_MILLISECONDS = 1;
    private LinearLayout mPointsDetailLayout;
    private LinearLayout mPointLayout;
    private int mDetailState;
    private ImageView expend;

    private int visibleLastIndex;
    private boolean loadedFlags;
    private boolean isScrolling;

    private ImageView mConsumeList;

    private static final int STATE_NOT_EXPEND = 0;
    private static final int STATE_EXPEND = 1;

    private static final int POSTION_APP = 0;
    private static final int POSITION_THEME = 1;

    private int mFrom;

    private ImageView mBack;

    private boolean mShowExPointTanhao;

    private boolean isFinish;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(MResource.getIdByName(this,"layout","nq_points_center"));

        mFrom = getIntent().getIntExtra("from",-1);

        mContext = this;
        mHelper = PointsPreference.getInstance();
        cursorPadingPx = DisplayUtil.dip2px(this, 6);
        mManager = PointsManager.getInstance(this);
        findView();
        if(mManager.haveGetPointsHistory()){
            mManager.rewardPoints(null,false,"",0,new RewardPointsListener() {
                @Override
                public void onRewardSucc(RewardPointsResp resp) {
                    updatePoints(resp.pointInfo);
                }

                @Override
                public void onErr() {

                }
            });
        }else{
            mManager.getPoints(new GetPointsListener() {

                @Override
                public void onGetPointsSucc(PointsInfo info) {
                    updatePoints(info);
                }

                @Override
                public void onErr() {

                }
            });
        }
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	isFinish = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPoints();
        if(currIndex == 0){
            if(mAppDapter != null){
                List<App> list = mAppDapter.getAppList();
                for(Iterator i = list.iterator();i.hasNext();){
                    App app = (App) i.next();
                    if(mManager.getRewardedState(app.getStrId()) != PointsManager.FLAG_NOT_REWARD){
                        i.remove();
                    }
                }

                mAppDapter.notifyDataSetChanged();
            }
        }
    }

    private void refreshPoints(){
        mCurrentPoints.setText(getString(MResource.getIdByName(mContext, "string", "nq_current_points"),
                mHelper.getIntValue(PointsPreference.KEY_USER_POINTS)));
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date();
        date.setTime(mHelper.getLongValue(PointsPreference.KEY_EXPERID_TIME));

        int expoint = mHelper.getIntValue(PointsPreference.KEY_EXPERID_POINTS);
        if(expoint > 0){
            mExpidTime.setVisibility(View.VISIBLE);
        }else{
            mExpidTime.setVisibility(View.GONE);
        }

        mExpidTime.setText(getString(MResource.getIdByName(mContext, "string", "nq_ex_points_tv"),
                mHelper.getIntValue(PointsPreference.KEY_EXPERID_POINTS), f.format(date)));
    }

    private void updatePoints(final PointsInfo info){
    	if(!isFinish){
    		runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCurrentPoints.setText(getString(MResource.getIdByName(mContext,"string","nq_current_points"),info.totalPoints));
                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
                    f.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = new Date();
                    date.setTime(info.expireTime);
                    if(info.expirePoints > 0){
                        mExpidTime.setVisibility(View.VISIBLE);
                    }else{
                        mExpidTime.setVisibility(View.GONE);
                    }
                    if(info.expirePoints > 0 && info.expireTime - System.currentTimeMillis() <= 7 * DateUtils.DAY_IN_MILLIS){
                        if(mHelper.getBooleanValue(PointsPreference.KEY_NEED_SHOW_EXPOINT_TIP)){
                            mShowExPointTanhao = true;
                            expend.setImageResource(MResource.getIdByName(PointsCenterActivity.this,"drawable","nq_expend_two"));
                        }
                    }

                    mExpidTime.setText(getString(MResource.getIdByName(mContext,"string","nq_ex_points_tv"),
                            info.expirePoints,f.format(date)));

                    PointsManager.getInstance(PointsCenterActivity.this).checkShowExPointNoti();
                }
            });
    	}
    }

    private void showLoading(int column){
        MoveBackView loadingView = mLoadingView[column];
        if (loadingView == null){
            return;
        }
        loadingView.startAnim(column == POSTION_APP ? mAppDapter.getCount() > 0 : mThemeAdapter.getCount() > 0);
    }

    private void findView(){
        mViewPager = (ViewPager) findViewById(MResource.getIdByName(this, "id", "vp_list"));
        mViewPager.setAdapter(new PointsPagerAdapter());
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(new PointsPageChangeAdapter());

        mPointAppTv = (TextView) findViewById(MResource.getIdByName(this,"id","tv_points_app"));
        mPointAppTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(0);
            }
        });
        mPointThemeTv = (TextView) findViewById(MResource.getIdByName(this,"id","tv_points_theme"));
        mPointThemeTv.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(1);
            }
        });
        mPointLayout = (LinearLayout) findViewById(MResource.getIdByName(this,"id","point_layout"));

        mLoadFailedLayout = new LinearLayout[COLUMN_COUNT];
        mLoadingView = new MoveBackView[COLUMN_COUNT];
        mNoNetworkView = new ImageView[COLUMN_COUNT];
        mEmptyView = new ImageView[COLUMN_COUNT];
        mListView = new ListView[COLUMN_COUNT];

        mCurrentPoints = (TextView) findViewById(MResource.getIdByName(this,"id","points"));
        mExpidTime = (TextView) findViewById(MResource.getIdByName(this,"id","experiod_points"));

        ivCursor = (ImageView) findViewById(MResource.getIdByName(this, "id", "tv_cursor"));
        screenWidth = getResources().getDisplayMetrics().widthPixels;// 获取分辨率宽度
        cursorWidth = (int) (screenWidth / 2 + 0.5f);
        lpCursor = new LinearLayout.LayoutParams(cursorWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        ivCursor.setPadding(2 * cursorPadingPx, 0, cursorPadingPx, 0);
        ivCursor.setLayoutParams(lpCursor);

        mPointsDetailLayout = (LinearLayout) findViewById(MResource.getIdByName(this,"id","points_detail"));

        expend = (ImageView) findViewById(MResource.getIdByName(this,"id","expend"));

        int expoints = mHelper.getIntValue(PointsPreference.KEY_EXPERID_POINTS);
        long extime = mHelper.getLongValue(PointsPreference.KEY_EXPERID_TIME);
        if(expoints > 0){
            long time = extime - System.currentTimeMillis();
            if(time <= 7 * DateUtils.DAY_IN_MILLIS && time >= 0){
                NqLog.i("need show tanhao!");
                if(mHelper.getBooleanValue(PointsPreference.KEY_NEED_SHOW_EXPOINT_TIP)){
                    mShowExPointTanhao = true;
                    expend.setImageResource(MResource.getIdByName(this,"drawable","nq_expend_two"));
                }
            }
        }

        if(expoints == 0){
            mExpidTime.setVisibility(View.GONE);
        }

        boolean needshowtip = mHelper.getBooleanValue(PointsPreference.KEY_SHOW_POINT_TIP);
        NqLog.i("needshowtip="+needshowtip);
        if(needshowtip){
            mHelper.setBooleanValue(PointsPreference.KEY_SHOW_POINT_TIP,false);
            mPointsDetailLayout.setVisibility(View.VISIBLE);
            expend.setImageResource(MResource.getIdByName(this,"drawable","nq_expend_three"));
            mDetailState = STATE_EXPEND;
        }else{
            mPointsDetailLayout.setVisibility(View.GONE);
            mDetailState = STATE_NOT_EXPEND;
        }

        mPointLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mDetailState == STATE_NOT_EXPEND){
                    if(mShowExPointTanhao){
                        mHelper.setBooleanValue(PointsPreference.KEY_NEED_SHOW_EXPOINT_TIP,false);
                    }
                    expend.setImageResource(MResource.getIdByName(PointsCenterActivity.this,"drawable","nq_expend_three"));
                    mPointsDetailLayout.setVisibility(View.VISIBLE);
                    mDetailState = STATE_EXPEND;
                }else{
                    expend.setImageResource(MResource.getIdByName(PointsCenterActivity.this,"drawable","nq_expend_one"));
                    mPointsDetailLayout.setVisibility(View.GONE);
                    mDetailState = STATE_NOT_EXPEND;
                }
            }
        });

        mBack = (ImageView) findViewById(MResource.getIdByName(this,"id","iv_back"));
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mFrom == PointsManager.FROM_NOTI){
                    LauncherSDK.getInstance(PointsCenterActivity.this).gotoStore(0);
                    finish();
                }else{
                    finish();
                }
            }
        });

        mConsumeList = (ImageView) findViewById(MResource.getIdByName(this,"id","consume_list"));
        mConsumeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext,ConsumeListActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(i);

                StatManager.getInstance().onAction(
                        StatManager.TYPE_STORE_ACTION,PointActionConstants.ACTION_LOG_2512,null,0,null);
            }
        });
    }

    /**
     * @param column 0:积分应用，1:积分主题
     * @param type 0:获取成功，1:失败，2:无网络
     * */
    private void hideListLoading(final int column, final int type) {
        NqLog.d("hideListLoading: column=" + column+" type="+type);
        MoveBackView loadingView = mLoadingView[column];
        if (loadingView == null){
            return;
        }

        loadingView.stopAnim();

        mLoadFailedLayout[column].setVisibility(type == 0 ? View.GONE : View.VISIBLE);
        switch(type){
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

    private class PointsPagerAdapter extends PagerAdapter{

        @Override
        public int getCount() {
            return COLUMN_COUNT;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view.equals(o);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final LinearLayout layout;
            if(position == POSTION_APP){
                layout = (LinearLayout) LayoutInflater.from(mContext).inflate(
                        MResource.getIdByName(mContext, "layout", "nq_points_app_list"), null);
                LinearLayout loadingLayout = (LinearLayout) layout.findViewById(MResource.getIdByName(mContext, "id", "ll_load_failed"));
                mLoadFailedLayout[position] = loadingLayout;

                MoveBackView moveBackView = (MoveBackView) layout.findViewById(MResource.getIdByName(mContext, "id", "nq_moveback"));
    			mLoadingView[position] = moveBackView;

                mNoNetworkView[position] = (ImageView) layout.findViewById(MResource.getIdByName(mContext, "id", "iv_nonetwork"));
                mEmptyView[position] = (ImageView) layout.findViewById(MResource.getIdByName(mContext, "id", "iv_empty"));
                final int column = position;
                View.OnClickListener clickListener = new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        getAppList();
                    }

                };
                mLoadFailedLayout[position].setOnClickListener(clickListener);
                mNoNetworkView[position].setOnClickListener(clickListener);
                mEmptyView[position].setOnClickListener(clickListener);

                ListView listView = (ListView) layout.findViewById(MResource.getIdByName(mContext, "id", "lv_list"));
                mAppDapter = new AppListAdapter();
                listView.setAdapter(mAppDapter);
                mListView[position] = listView;
                listView.setRecyclerListener(new AbsListView.RecyclerListener() {
                    @Override
                    public void onMovedToScrapHeap(View view) {
                        ViewHolder holder = (ViewHolder)view.getTag();
                        hideAppViewHolder(holder);
                    }
                });
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        App app = mAppDapter.getAppList().get(i);
                        if(app != null){
                            AppManager.getInstance(mContext).viewAppDetail(mViewPager.getCurrentItem(),app);
                        }
                    }
                });

                getAppList();
            }else{
                layout = (LinearLayout) LayoutInflater.from(mContext).inflate(
                        MResource.getIdByName(mContext, "layout", "nq_points_theme_list"), null);
                LinearLayout loadingLayout = (LinearLayout) layout.findViewById(MResource.getIdByName(mContext, "id", "ll_load_failed"));
                mLoadFailedLayout[position] = loadingLayout;

                mNoNetworkView[position] = (ImageView) layout.findViewById(MResource.getIdByName(mContext, "id", "iv_nonetwork"));
                mEmptyView[position] = (ImageView) layout.findViewById(MResource.getIdByName(mContext, "id", "iv_empty"));
                final int column = position;
                View.OnClickListener clickListener = new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        getThemeList(0);
                    }

                };
                mLoadFailedLayout[position].setOnClickListener(clickListener);
                mNoNetworkView[position].setOnClickListener(clickListener);
                mEmptyView[position].setOnClickListener(clickListener);

                MoveBackView moveBackView = (MoveBackView) layout.findViewById(MResource.getIdByName(mContext, "id", "nq_moveback"));
    			mLoadingView[position] = moveBackView;

                ListView listView = (ListView) layout.findViewById(MResource.getIdByName(mContext, "id", "lv_list"));
                mListView[position] = listView;
                mThemeAdapter = new ThemeListAdaper();
                listView.setAdapter(mThemeAdapter);
                listView.setRecyclerListener(new AbsListView.RecyclerListener() {
                    @Override
                    public void onMovedToScrapHeap(View view) {
                        ThemeViewHolder holder = (ThemeViewHolder)view.getTag();
                        hideThemeViewHolder(holder);
                    }
                });
                listView.setOnScrollListener(new ThemeListScrollListener());
            }

            container.addView(layout);
            return layout;
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
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                NqLog.d("column=" + currIndex
                        + ", scrolling... visible=" + visibleLastIndex
                        + ", Count=" + view.getCount()
                        + " loadedFlags[" + currIndex + "]=" + loadedFlags);

                if(visibleLastIndex == view.getCount() - 1 && loadedFlags)
                	ToastUtils.toast(mContext, "nq_list_end");
                if(!isScrolling && visibleLastIndex == view.getCount() - 1
                        && !loadedFlags){ //加上底部的loadMoreView项
                    //如果是自动加载,可以在这里放置异步加载数据的代码
                    isScrolling = true;
                    int size = mThemeAdapter.getThemeList().size();
                    Theme[] tmp = mThemeAdapter.getThemeList().get(size - 1);
                    int offset = 3 * size;
                    for(int i=0; i<tmp.length; i++){
                        if(tmp[i] == null || TextUtils.isEmpty(tmp[i].getStrId()))
                            offset --;
                    }

                    NqLog.d("loading... " + visibleLastIndex + ",offset=" + offset);
                    getThemeList(offset);
                }
            }
        }

        /*
         * 当列表的滚动已经完成时调用的回调函数。会在滚动完成后调用。
         */
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
        }
    }

    private void hideAppViewHolder(ViewHolder holder){
        if (holder == null) {
            return;
        }
        NqLog.d("hideAppViewHolder: ivIcon.mUrl = " + holder.ivIcon.getUrl());
        holder.ivIcon.onHide();
    }

    private void hideThemeViewHolder(ThemeViewHolder holder){
        if (holder == null) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            AsyncImageView imgView = holder.ivPreview[i];
            if (imgView == null) {
                continue;
            }
            NqLog.d("hideViewHolder: ivPreview.mUrl = " + imgView.getUrl()  + " i=" + i);
            imgView.onHide();
        }
    }

    private void updateAppList(int column, int offset, List<App> apps) {
        NqLog.d("updateAppList: column=" + column + " offset=" + offset + " apps=" + apps);
        if (apps == null || apps.size() == 0){
            return;
        }

        if (offset == 0) {
            mAppDapter.getAppList().clear();
        }
        mAppDapter.getAppList().addAll(apps);
        mAppDapter.notifyDataSetChanged();
    }

    /** 刷新主题列表视图*/
    private void updateThemeList(int column, int offset, List<Theme[]> themes) {
        NqLog.d("updateThemeList: column=" + column + " offset=" + offset + " themes=" + themes);
        if (themes == null || themes.size() == 0){
            return;
        }

        if (offset == 0) {
            mThemeAdapter.getThemeList().clear();
        }

        mThemeAdapter.getThemeList().addAll(themes);
        mThemeAdapter.notifyDataSetChanged();
    }

    private void getAppList(){
       showLoading(POSTION_APP);
       mManager.getAppList(PointsManager.COLUMN_POINT_APP,0,new BasicAppListListener());
    }

    private void getThemeList(int offset){
        showLoading(POSITION_THEME);
        mManager.getThemeList(offset,new BasicThemeListListener());
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
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(MResource.getIdByName(mContext, "layout", "nq_point_app_list_item"), null);
                holder.ivIcon = (AsyncImageView) convertView.findViewById(MResource.getIdByName(mContext, "id", "iv_icon"));
                holder.ivDownload = (PointsDownloadImageView) convertView.findViewById(MResource.getIdByName(mContext, "id", "down_layout"));
                holder.ivDownload.setPos(position);
                holder.tvName = (TextView) convertView.findViewById(MResource.getIdByName(mContext, "id", "tv_name"));
                holder.rbRate = (RatingBar) convertView.findViewById(MResource.getIdByName(mContext, "id", "rb_rate"));
                holder.tvSize = (TextView) convertView.findViewById(MResource.getIdByName(mContext, "id", "tv_size"));
                holder.tvState = (TextView) convertView.findViewById(MResource.getIdByName(mContext,"id","state_tv"));
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
            return convertView;
        }
    }

    private static class ViewHolder {
        AsyncImageView ivIcon;
        PointsDownloadImageView ivDownload;
        TextView tvName;
        RatingBar rbRate;
        TextView tvSize;
        TextView tvState;
    }

    /**
     * 主题列表Adapter
     */
    private class ThemeListAdaper extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<Theme[]> mThemeArrList;
        private int mLast = -1;

        public ThemeListAdaper() {
            this.mInflater = LayoutInflater.from(mContext);
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
            final ThemeViewHolder holder;
            if (convertView == null) {
                holder = new ThemeViewHolder();
                convertView = mInflater.inflate(MResource.getIdByName(mContext, "layout", "nq_theme_list_item3"), null);
                for (int i = 0; i < 3; i++) {
                    holder.rl[i] = (LinearLayout) convertView.findViewById(MResource.getIdByName(mContext, "id", "nq_theme_list_item"+i));
                    holder.ivPreview[i] = (AsyncImageView) holder.rl[i].findViewById(MResource.getIdByName(mContext, "id", "iv_preview"));
                    holder.tvName[i] = (TextView) holder.rl[i].findViewById(MResource.getIdByName(mContext, "id", "tv_name"));
                    holder.tvPoint[i] = (TextView) holder.rl[i].findViewById(MResource.getIdByName(mContext,"id","tv_points"));
                }
                convertView.setTag(holder);
            } else {
                holder = (ThemeViewHolder) convertView.getTag();
            }

            // 赋值
            final Theme[] themes = mThemeArrList.get(position);
            for (int i = 0; i < 3; i++) {
                if(themes[i] != null){
                    final String resId = themes[i].getStrId();
                    holder.rl[i].setVisibility(View.VISIBLE);
                    holder.tvName[i].setText(themes[i].getStrName());
                    holder.ivPreview[i].setTag(themes[i].getStrId());
                    String url = themes[i].getStrIconUrl();
                    holder.ivPreview[i].loadImage(url, null, MResource.getIdByName(mContext, "drawable", "nq_load_default"));
                    final int index = i;
                    /**主题列表item点击事件*/
                    holder.ivPreview[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            Intent intent = new Intent(mContext, ThemeDetailActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(ThemeDetailActivity.KEY_THEME, themes[index]);
                            mContext.startActivity(intent);

                            StatManager.getInstance().onAction(
                                    StatManager.TYPE_STORE_ACTION,PointActionConstants.ACTION_LOG_2507,resId,0,null);
                        }
                    });

                    int flag = themes[i].getPointsflag();
                    holder.tvPoint[i].setVisibility(View.VISIBLE);
                    NqLog.d("" + themes[i]);
                    if(flag == ThemeManager.POINT_FLAG_CONSUME){
                        holder.tvPoint[i].setText(MResource.getString(mContext,"nq_already_consume"));
                    }else{
                        holder.tvPoint[i].setText(MResource.getString(mContext,"nq_theme_points",String.valueOf(themes[i].getConsumePoints())));
                    }
                } else {
                    holder.rl[i].setVisibility(View.INVISIBLE);
                }
            }

            return convertView;
        }

    }

    private static class ThemeViewHolder {
        LinearLayout[] rl = new LinearLayout[3];
        AsyncImageView[] ivPreview = new AsyncImageView[3];
        TextView[] tvName = new TextView[3];
        TextView[] tvPoint = new TextView[3];
    }

    private class BasicAppListListener implements AppListListener {

        public BasicAppListListener() {
        }

        @Override
        public void onErr() {
            NqLog.i("PointCenterActivity onErr");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAppDapter.getAppList().size() > 0) {
                        hideListLoading(POSTION_APP, 0);
                    } else {
                        hideListLoading(POSTION_APP, 1);
                    }
                }
            });
        }

        @Override
        public void onGetAppListSucc(final int offset,final List<App> apps) {
            NqLog.i("PointCenterActivity onGetAppListSucc apps.size="+apps.size());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (apps != null) {
                        hideListLoading(POSTION_APP, 0);
                        updateAppList(POSTION_APP, offset, apps);
                    } else {
                        hideListLoading(POSTION_APP, 1);
                    }
                }
            });
        }
    }

    private class BasicThemeListListener implements ThemeListListener {

        @Override
        public void onErr() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mThemeAdapter.getThemeList().size() > 0) {
                        hideListLoading(POSITION_THEME, 0);
                    } else {
                        hideListLoading(POSITION_THEME, 1);
                    }
                }
            });
        }

        @Override
        public void onGetThemeListSucc(final int offset, final List<Theme[]> themes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(themes != null){
                        hideListLoading(POSITION_THEME, 0);
                        updateThemeList(POSITION_THEME, offset, themes);

                        if(ThemeManager.getCount(themes) < ThemeConstants.STORE_THEME_LIST_PAGE_SIZE
                                || loadedFlags){//视为末页
                            loadedFlags = true;
                        } else {
                            loadedFlags = false;
                        }
                    }else{
                        hideListLoading(POSITION_THEME, 1);
                    }
                }
            });
        }
    }

    private class PointsPageChangeAdapter implements ViewPager.OnPageChangeListener{

        @Override
        public void onPageScrolled(int i, float v, int i2) {
        }

        @Override
        public void onPageSelected(int i) {
            currIndex = i;
            mPointAppTv.setTextColor(getResources().getColor(MResource.getIdByName(mContext, "color", "nq_text_store_column_title")));
            mPointThemeTv.setTextColor(getResources().getColor(MResource.getIdByName(mContext, "color", "nq_text_store_column_title")));
            switch (i) {
                case 0:
                    mPointAppTv.setTextColor(getResources().getColor(MResource.getIdByName(mContext, "color", "nq_text_store_column_title_selected")));
                    ivCursor.setPadding(2 * cursorPadingPx, 0, cursorPadingPx, 0);
                    AnimationUtil.moveTo(ivCursor, CURSOR_ANIMATION_MILLISECONDS, cursorLastX, 0, 0, 0);
                    cursorLastX = 0;

                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,PointActionConstants.ACTION_LOG_2503,null,0,null);
                    break;
                case 1:
                    mPointThemeTv.setTextColor(getResources().getColor(MResource.getIdByName(mContext, "color", "nq_text_store_column_title_selected")));
                    ivCursor.setPadding(cursorPadingPx, 0, 2 * cursorPadingPx, 0);
                    AnimationUtil.moveTo(ivCursor, CURSOR_ANIMATION_MILLISECONDS, cursorLastX, cursorWidth, 0, 0);
                    cursorLastX = cursorWidth;

                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,PointActionConstants.ACTION_LOG_2502,null,0,null);
                    break;
                default:
                    break;
            }

            if(i == POSTION_APP){
            	int offset = 0;
            	if(mAppDapter.getAppList() != null)
            		offset = mAppDapter.getAppList().size();
                if (offset == 0) {//当前listview没数据
                    getAppList();
                }
            }else if(i == POSITION_THEME){
            	int offset = 0;
            	if(mThemeAdapter != null && mThemeAdapter.getThemeList() != null)
            		offset = mThemeAdapter.getThemeList().size();
                if (offset == 0) {//当前listview没数据
                    getThemeList(0);
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }
    }
}