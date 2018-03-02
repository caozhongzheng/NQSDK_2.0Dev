package com.nqmobile.livesdk.modules.points;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.commons.ui.MoveBackView;
import com.nqmobile.livesdk.modules.font.FontManager;
import com.nqmobile.livesdk.modules.points.model.ConsumeListResp;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.theme.ThemeDetailActivity;
import com.nqmobile.livesdk.utils.CollectionUtils;
import com.nqmobile.livesdk.utils.MResource;

/**
 * Created by Rainbow on 14-5-22.
 */
public class ConsumeListActivity extends BaseActvity{
	private static final ILogger NqLog = LoggerFactory.getLogger(PointModule.MODULE_NAME);

    private ListView mList;
    private ImageView mBack;
    private Context mContext;
    private ListAdapter mAdapter;
    private TextView mTitle;
    private TextView mEmptyTv;
    private MoveBackView mLoadingView;
    private ImageView mNoNetworkView;
    private LinearLayout mLoadFailedLayout;
    private TextView mClickLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(MResource.getIdByName(this,"layout","nq_consume_list_activity"));
        mContext = this;
        findView();
        getData();
    }

    private void getData() {
        showAppLoading();
        PointsManager.getInstance(this).getConsumeList(new ConsumeListListener() {
            @Override
            public void onConsumeListSucc(final ConsumeListResp resp) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!CollectionUtils.isEmpty(resp.widgetResourceList)) {
                            List<Theme[]> list = PointsManager.getInstance(mContext).getConsumThemeList(resp);
                            dismissAppLoading(0);
                            mAdapter = new ListAdapter(list);
                            mList.setAdapter(mAdapter);

                            StatManager.getInstance().onAction(
                                    StatManager.TYPE_STORE_ACTION, PointActionConstants.ACTION_LOG_2513,null,0,null);
                        } else {
                            NqLog.i("ConsumeListActivity onConsumeListSucc list.size=0");
                            dismissAppLoading(1);
                        }
                    };
                });
            }

            @Override
            public void onErr() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissAppLoading(2);
                    }
                });
            }
        });
    }

    private void findView(){
        mList = (ListView) findViewById(MResource.getIdByName(this,"id","list"));
        mBack = (ImageView) findViewById(MResource.getIdByName(this,"id","iv_back"));
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mTitle = (TextView) findViewById(MResource.getIdByName(this,"id","activity_name"));
        mTitle.setText(MResource.getString(this,"nq_consume_history"));
        mEmptyTv = (TextView) findViewById(MResource.getIdByName(this,"id","iv_empty_text"));
        mEmptyTv.setText(MResource.getString(this,"nq_no_consume_history"));
        //mLoadingView = (ImageView) findViewById(MResource.getIdByName(this,"id","iv_loading_anim"));
        mLoadingView = (MoveBackView) findViewById(MResource.getIdByName(this, "id", "nq_moveback"));
		mNoNetworkView = (ImageView) findViewById(MResource.getIdByName(this,"id","iv_nonetwork"));
        mLoadFailedLayout = (LinearLayout) findViewById(MResource.getIdByName(this,"id","ll_load_failed"));
        mLoadFailedLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
            }
        });
        mClickLoad = (TextView) findViewById(MResource.getIdByName(this,"id","click_to_load"));
    }

    private void showAppLoading(){
    	MoveBackView loadingView = mLoadingView;
        if (loadingView == null){
            return;
        }
		if (mAdapter != null) {
			loadingView.startAnim(mAdapter.getCount() > 0);
		}
    }

    private void dismissAppLoading(int type){
        MoveBackView loadingView = mLoadingView;
        if (loadingView == null){
            return;
        }
        loadingView.stopAnim();

        mLoadFailedLayout.setVisibility(type == 0 ? View.GONE : View.VISIBLE);
        switch(type){
            case 0:
                mEmptyTv.setVisibility(View.GONE);
                mNoNetworkView.setVisibility(View.GONE);
                mList.setVisibility(View.VISIBLE);
                break;
            case 1:
                mEmptyTv.setVisibility(View.VISIBLE);
                mNoNetworkView.setVisibility(View.GONE);
                mList.setVisibility(View.GONE);
                mClickLoad.setVisibility(View.GONE);
                break;
            case 2:
                mEmptyTv.setVisibility(View.GONE);
                mNoNetworkView.setVisibility(View.VISIBLE);
                mList.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * 主题列表Adapter
     */
    private class ListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<Theme[]> mThemeArrList;

        public ListAdapter(List<Theme[]> list) {
            this.mInflater = LayoutInflater.from(mContext);
            this.mThemeArrList = list;
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
                final Theme theme = themes[i];
                if(theme != null){
                    final LinearLayout layout = holder.rl[i];
                    layout.setVisibility(View.VISIBLE);
                    holder.tvName[i].setText(themes[i].getStrName());
                    holder.ivPreview[i].setTag(themes[i].getStrId());
                    String url = themes[i].getStrIconUrl();
                    holder.ivPreview[i].loadImage(url, null, MResource.getIdByName(mContext, "drawable", "nq_load_default"));
                    /**主题列表item点击事件*/
                    holder.ivPreview[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            Intent intent = new Intent(mContext, ThemeDetailActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(ThemeDetailActivity.KEY_THEME, theme);
                            mContext.startActivity(intent);
                        }
                    });
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
}
