package com.nqmobile.livesdk.modules.points;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.font.FontManager;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PackageUtils;
import com.nqmobile.livesdk.utils.ToastUtils;
import com.nqmobile.livesdk.utils.Tools;

/**
 * Created by Rainbow on 14-5-21.
 */
public class PointAppDialog extends Dialog{
	private static final ILogger NqLog = LoggerFactory.getLogger(PointModule.MODULE_NAME);
	
    private Context mContext;

    private static final int ITEM_SIZE = 3;

    private ListAdapter mListAdapter;

    private ProgressBar mLoading;
    private Handler mHandler = new Handler();
    private ListView mList;
    private Button mBtnGoPointcenter;
    private Button mBtnInstallAll;
    private TextView mEmptyTv;
    private String mResId;
    private int mNeedPoints;
    private int mOffPoints;
    private TextView mLessPoint, mGetPoint;
    private LinearLayout mLoadNoNetwork;
    private LinearLayout mBtnLayout;
    private int mDefCheckNum = 4;

    public PointAppDialog(Context context,int theme,String resId){
        super(context,theme);
        mContext = context;
        mResId = resId;
        NqLog.d("PointAppDialog mResId:" + mResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(MResource.getIdByName(mContext,"layout","nq_point_app_dialog"));

        mLoading = (ProgressBar) findViewById(MResource.getIdByName(mContext,"id","loading"));
        mList = (ListView) findViewById(MResource.getIdByName(mContext,"id","list"));
        mLessPoint = (TextView) findViewById(MResource.getIdByName(mContext,"id","less_point"));
        mGetPoint = (TextView) findViewById(MResource.getIdByName(mContext,"id","getpoint"));
        mBtnGoPointcenter = (Button) findViewById(MResource.getIdByName(mContext,"id","btn_enterPointCenter"));
        mBtnGoPointcenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, PointsCenterActivity.class);
                //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(i);
                dismiss();

                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION, PointActionConstants.ACTION_LOG_2511,null,0,null);
            }
        });
        mBtnInstallAll = (Button) findViewById(MResource.getIdByName(mContext,"id","btn_installAll"));
        mBtnInstallAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION, PointActionConstants.ACTION_LOG_2510,null,0,null);
                if (!NetworkUtils.isConnected(mContext)) {
                	ToastUtils.toast(mContext, "nq_nonetwork");
                    return;
                }

                Toast.makeText(mContext,
                        mContext.getString(MResource.getIdByName(mContext, "string", "nq_start_downloading")),
                        Toast.LENGTH_SHORT).show();

                for(App app:mListAdapter.getSelected()){
                    Long downloadId = AppManager.getInstance(mContext).downloadApp(app,
                            DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                    PointsManager.getInstance(mContext).flagPointResource(app,downloadId == null ? -1:downloadId, mResId);
                }
                dismiss();
            }
        });

        mEmptyTv = (TextView) findViewById(MResource.getIdByName(mContext,"id","empty_tv"));
        mLoadNoNetwork = (LinearLayout) findViewById(MResource.getIdByName(mContext,"id","ll_load_failed"));
        mLoadNoNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });
        mBtnLayout = (LinearLayout) findViewById(MResource.getIdByName(mContext,"id","btn_layout"));
        getNeedPoint();
        loadData();
    }

    private void loadData() {
        PointsManager.getInstance(mContext).getAppList(PointsManager.COLUMN_POINT_APP_CONSUME,0,new AppListListener() {

            @Override
            public void onGetAppListSucc(int offset,final List<App> apps) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLoading.setVisibility(View.GONE);
                        mBtnLayout.setVisibility(View.VISIBLE);
                        NqLog.i("apps.size="+apps.size());

                        if(apps != null && apps.size() > 0){
                            mList.setVisibility(View.VISIBLE);
                            filterAlreadyInstall(apps);

                            if(apps.size() > 0){
                                mListAdapter = new ListAdapter(mContext,getList(apps));
                                mList.setAdapter(mListAdapter);
                            }else{
                                mEmptyTv.setVisibility(View.VISIBLE);
                                mBtnInstallAll.setVisibility(View.GONE);
                                mGetPoint.setVisibility(View.GONE);
                                mLessPoint.setVisibility(View.VISIBLE);
                                mLessPoint.setText(MResource.getString(mContext, "nq_less_point", String.valueOf(mNeedPoints)));
                                mBtnGoPointcenter.setBackgroundResource(MResource.getIdByName(mContext,"drawable","nq_navigation_bg"));
                                mBtnGoPointcenter.setTextColor(mContext.getResources().getColor(MResource.getIdByName(mContext, "color", "nq_white")));
                            }
                        }else{
                            mEmptyTv.setVisibility(View.VISIBLE);
                            mBtnInstallAll.setVisibility(View.GONE);
                            mGetPoint.setVisibility(View.GONE);
                            mLessPoint.setVisibility(View.VISIBLE);
                            mLessPoint.setText(MResource.getString(mContext, "nq_less_point", String.valueOf(mNeedPoints)));
                            mBtnGoPointcenter.setBackgroundResource(MResource.getIdByName(mContext,"drawable","nq_navigation_bg"));
                            mBtnGoPointcenter.setTextColor(mContext.getResources().getColor(MResource.getIdByName(mContext, "color", "nq_white")));
                        }
                    }
                });
            }

            @Override
            public void onErr() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLoading.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void filterAlreadyInstall(List<App> list){
    	AppManager appManager = AppManager.getInstance(mContext);
        for(Iterator<App> iter = list.iterator();iter.hasNext();){
            App app = iter.next();
            int statusCode = appManager.getStatus(app).statusCode;
            if(statusCode == AppManager.STATUS_INSTALLED ||
            		statusCode == AppManager.STATUS_DOWNLOADING ||
            		statusCode == AppManager.STATUS_PAUSED){
                iter.remove();
            }else if(!Tools.isEmpty(app.getStrPackageName()) && PackageUtils.isAppInstalled(mContext, app.getStrPackageName())){
            	iter.remove();
            }
        }
    }


    private void getNeedPoint(){
        int themePoints = PointsManager.getInstance(mContext).getThemePoints(mResId);
        int currentPoints = PointsPreference.getInstance().getIntValue(PointsPreference.KEY_USER_POINTS);
        NqLog.i("themePoints="+themePoints+" currentPoints="+currentPoints);
        mNeedPoints = themePoints - currentPoints;
        if(mNeedPoints < 0){
            mNeedPoints = 0;
        }
        mLessPoint.setVisibility(View.VISIBLE);
        mLessPoint.setText(MResource.getString(mContext, "nq_less_point", String.valueOf(mNeedPoints)));
    }

    private List<App[]> getList(List<App> list){
        List<App[]> result = new ArrayList<App[]>();
        App[] arrApp = null;
        for (int i = 0; i < list.size(); i++) {
            if(i % ITEM_SIZE == 0)
                arrApp = new App[ITEM_SIZE];
            arrApp[i % ITEM_SIZE] = list.get(i);
            if(i % ITEM_SIZE == 2)
                result.add(arrApp);
        }
        if(list.size() % ITEM_SIZE != 0){
            result.add(arrApp);
        }
        return result;
    }

    private class ListAdapter extends BaseAdapter{

        private List<App[]> mList;

        private Context mContext;
        private LayoutInflater mInflater;
        private HashSet<App> mCheckSet = new HashSet<App>();

        public ListAdapter(Context context,List<App[]> list){
            mContext = context;
            mList = list;
            mInflater = LayoutInflater.from(mContext);
            getCheckState();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }
          
        public void getCheckState(){
            int sum = 0;
            boolean enough = false;
            for(App[] apps: mList){
                for(int i=0;i<apps.length;i++){
                    if(apps[i] != null){
                        sum+=apps[i].getRewardPoints();
                        mCheckSet.add(apps[i]);
                    }
                    if(mCheckSet.size() == mDefCheckNum){
                        enough = true;
                        break;
                    }
                }

                if(enough){
                    break;
                }
            }

            updateBtnShow();
        }

        public HashSet<App> getSelected(){
            return mCheckSet;
        }

        private void updateBtnShow(){
        	int sum = 0;
        	for(App app : mCheckSet){
        		sum+=app.getRewardPoints();
        		NqLog.i("" + app);
        	}
        	
        	NqLog.i("updateBtnShow mNeedPoint="+mNeedPoints+" sum="+sum + ", size:" + mCheckSet.size());
        	mGetPoint.setVisibility(View.VISIBLE);
        	mGetPoint.setText(String.format(MResource.getString(mContext, "nq_label_sel_app_point"), String.valueOf(sum)));
        	mBtnInstallAll.setEnabled(mCheckSet.size() > 0);
        	if(mCheckSet.size() > 0) {
        		mBtnInstallAll.setBackgroundDrawable(
        				mContext.getResources().getDrawable(MResource.getIdByName(mContext, "drawable", "nq_point_dialog_but_right")));
        	} else {
        		mBtnInstallAll.setBackgroundDrawable(
        				mContext.getResources().getDrawable(MResource.getIdByName(mContext, "drawable", "nq_navigation_bg_grey")));
        	}
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if(view == null){
                holder = new ViewHolder();
                view = mInflater.inflate(MResource.getIdByName(mContext, "layout", "nq_point_app_dialog_list_item3"), null);
                for (int i = 0; i < ITEM_SIZE; i++) {
                    holder.mLayout[i] = view.findViewById(MResource.getIdByName(mContext,"id","nq_point_list_dialog_item" + i));
                    holder.mCheck[i] = (ImageView) holder.mLayout[i].findViewById(MResource.getIdByName(mContext,"id","check"));
                    holder.mIcon[i] = (AsyncImageView) holder.mLayout[i].findViewById(MResource.getIdByName(mContext,"id","icon"));
                    holder.mName[i] = (TextView) holder.mLayout[i].findViewById(MResource.getIdByName(mContext,"id","name"));
                    holder.mPoints[i] = (TextView) holder.mLayout[i].findViewById(MResource.getIdByName(mContext,"id","point"));
                    holder.vLine1[i] = view.findViewById(MResource.getIdByName(mContext,"id","v_line1"));
                }
                view.setTag(holder);
            }else{
                holder = (ViewHolder) view.getTag();
            }

            holder.vLine1[0].setVisibility(View.GONE);
            final App[] apps = mList.get(position);
            for(int i=0;i < ITEM_SIZE;i++){
                final App app = apps[i];
                if(app != null){
                    holder.mLayout[i].setVisibility(View.VISIBLE);
                    holder.mLayout[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(mCheckSet.contains(app)){
                                mCheckSet.remove(app);
                                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                                        PointActionConstants.ACTION_LOG_2509,app.getStrId(),1,app.getStrPackageName()+"_1");
                            }else{
                                mCheckSet.add(app);
                                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                                        PointActionConstants.ACTION_LOG_2509,app.getStrId(),0,app.getStrPackageName()+"_0");
                            }
                            updateBtnShow();
                            notifyDataSetChanged();

                        }
                    });
                    holder.mName[i].setText(app.getStrName());
                    holder.mPoints[i].setText("+" + String.valueOf(app.getRewardPoints()));
                    String url = app.getStrIconUrl();
                    holder.mIcon[i].loadImage(url, null, MResource.getIdByName(mContext, "drawable","nq_load_default"));
                    if(mCheckSet.contains(app)){
                        holder.mCheck[i].setVisibility(View.VISIBLE);
                    }else{
                        holder.mCheck[i].setVisibility(View.GONE);
                    }
                }else{
                    holder.mLayout[i].setVisibility(View.INVISIBLE);
                }
            }

            return view;
        }
    }

    private static class ViewHolder {
        View[] mLayout = new View[ITEM_SIZE];
        ImageView[] mCheck = new ImageView[ITEM_SIZE];
        AsyncImageView[] mIcon = new AsyncImageView[ITEM_SIZE];
        TextView[] mName = new TextView[ITEM_SIZE];
        TextView[] mPoints = new TextView[ITEM_SIZE];
        View[] vLine1 = new View[ITEM_SIZE];
    }
}
