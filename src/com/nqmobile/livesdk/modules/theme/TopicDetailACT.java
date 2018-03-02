package com.nqmobile.livesdk.modules.theme;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.commons.ui.PullToRefreshListView;
import com.nqmobile.livesdk.modules.points.PointsPreference;
import com.nqmobile.livesdk.utils.MResource;

public class TopicDetailACT extends BaseActvity implements OnClickListener{
	private static final ILogger NqLog = LoggerFactory.getLogger(ThemeModule.MODULE_NAME);

	public static final String KEY_TOPIC = "com.nqmobile.live.topic";
	
	private Topic mTopic;
	private Context mContext;
	private PullToRefreshListView mListView;
	private ThemeArrListAdapter mAdapter;
	private LayoutInflater mInflater;
	private ArrayList<ThemeItem> themeItems;
	private boolean mPointCenterEnable;
	private View mBackIcon;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mContext = getApplication();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(MResource.getIdByName(getApplication(), "layout", "nq_fragment_topic"));
		
		this.mInflater = LayoutInflater.from(mContext);
		
		mPointCenterEnable = PointsPreference.getInstance().getBooleanValue(PointsPreference.KEY_POINT_CENTER_ENABLE);
        themeItems = new ArrayList<ThemeItem>(); 
        
		mTopic = (Topic) getIntent().getSerializableExtra(KEY_TOPIC);
		NqLog.d("TopicDetail " + mTopic);
		if (mTopic != null) {
			List<? extends Theme> themes = mTopic.getThemes();
			ThemeItem themeItem = null;
			int size = themes.size();
			NqLog.d("size:" + size);
			for (int i = 0; i < size; i++) {
				Theme theme = themes.get(i);
				if (themeItem == null || themeItem.themes.size() > 2) {
					themeItem = new ThemeItem();
				}
				themeItem.themes.add(theme);
				if(themeItem.themes.size() == 3 || i == size - 1){
					themeItems.add(themeItem);
				}
			}
		}
		mBackIcon = findViewById(MResource.getIdByName(mContext, "id", "icon"));
		mListView = (PullToRefreshListView) findViewById(MResource.getIdByName(mContext, "id", "lv_list"));
		mBackIcon.setOnClickListener(this);
		int count = mListView.getHeaderViewsCount();
		NqLog.d("count:" + count);
		mListView.clearHeadOneHeight();
		mAdapter = new ThemeArrListAdapter();
		mListView.setAdapter(mAdapter);
	}
	
	@Override
	public void onClick(View v) {
		if(v == mBackIcon){
			finish();
		}
	}
	
	/**
	 * 主题列表Adapter
	 */
	private class ThemeArrListAdapter extends BaseAdapter {

		public ThemeArrListAdapter() { }

		@Override
		public int getCount() {
			return themeItems.size();
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
				convertView = mInflater.inflate(MResource.getIdByName(mContext, "layout", "nq_theme_list_item3"), null);
				for (int i = 0; i < 3; i++) {
					holder.rl[i] = (LinearLayout) convertView.findViewById(MResource.getIdByName(mContext, "id", "nq_theme_list_item"+i));
					holder.ivPreview[i] = (AsyncImageView) holder.rl[i].findViewById(MResource.getIdByName(mContext, "id", "iv_preview"));
					holder.tvName[i] = (TextView) holder.rl[i].findViewById(MResource.getIdByName(mContext, "id", "tv_name"));
                    holder.tvPoint[i] = (TextView) holder.rl[i].findViewById(MResource.getIdByName(mContext,"id","tv_points"));
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// 赋值
			final ThemeItem themeItem = themeItems.get(position);
			
			ArrayList<Theme> themes = themeItem.themes;
			int size = themes.size();
			for (int i = 0; i < 3; i++) {
				if (i < size) {
					final Theme theme = themes.get(i);
					if(theme != null){
						holder.rl[i].setVisibility(View.VISIBLE);
						holder.tvName[i].setText(theme.getStrName());
						holder.ivPreview[i].setTag(theme.getStrId());
						String url = theme.getStrIconUrl();
						holder.ivPreview[i].loadImage(url, null, MResource.getIdByName(mContext, "drawable","nq_load_default"));
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
						
						if(mPointCenterEnable){
							holder.tvPoint[i].setVisibility(View.VISIBLE);
							int points = theme.getConsumePoints();
							if(points > 0){
								holder.tvPoint[i].setText(MResource.getString(mContext,"nq_theme_points", String.valueOf(points)));
							}else{
								holder.tvPoint[i].setText(MResource.getString(mContext,"nq_theme_free"));
							}
						}else
							holder.tvPoint[i].setVisibility(View.GONE);
					} else 
						holder.rl[i].setVisibility(View.INVISIBLE);
				}else 
					holder.rl[i].setVisibility(View.INVISIBLE);
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
	
	private class ThemeItem{
		public ArrayList<Theme> themes = new ArrayList<Theme>();
	}

}
