package com.nqmobile.livesdk.modules.font;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nqmobile.live.R;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.utils.DisplayUtil;
import com.nqmobile.livesdk.utils.MResource;


public class FontLocalManager extends BaseActvity implements AdapterView.OnItemClickListener, View.OnClickListener{
	private static final ILogger NqLog = LoggerFactory.getLogger(FontModule.MODULE_NAME);
	
	private Context mContext;
	// ViewPager
	private ViewPager mViewPager;
//	private PullToRefreshListView[] mListView;
	private ListView mListView;
	private int scrollBy;
	// 数据
	private FontListAdapter mFontListAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		scrollBy = DisplayUtil.dip2px(mContext, 10);
		setContentView(MResource.getIdByName(getApplication(), "layout", "nq_font_local_manager"));
		
		final LinearLayout fontListLayout = (LinearLayout)LayoutInflater.from(mContext).inflate(MResource.getIdByName(mContext, "layout", "nq_font_local_manager"), null);

		ListView lv = (ListView) fontListLayout.findViewById(MResource.getIdByName(mContext, "id", "lv_list")); 
		mFontListAdapter = new  FontListAdapter();
		lv.setAdapter(mFontListAdapter);
		loadMoreData();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	public void loadMoreData() {
		//如果是自动加载,可以在这里放置异步加载数据的代码
		ArrayList<NqFont> list = (ArrayList<NqFont>) mFontListAdapter.getFontList();
		int offset = list == null ? 0 : list.size();

		NqLog.d("loading... " );
		getFontList( offset);
	}
	private void log(String tag, List<NqFont> fonts){
		if (fonts != null) {
            NqLog.d(tag + " fonts.size()" + fonts.size());
            NqFont ts = null;
            for (int i = 0; i < fonts.size(); i++) {
            	ts = fonts.get(i);
           		if(ts != null && !TextUtils.isEmpty(ts.getStrId()))
           			NqLog.d(i + " " + tag + " , ResourceId==" + ts.getStrId() + ", name=" + ts.getStrName());
            }
        } else {
            NqLog.d(tag + " fonts == null");
        }
	}
	private void updateFontList( int offset, List<NqFont> fonts) {
		NqLog.d("updateAppList:  offset=" + offset + " fonts=" + fonts);
		if (fonts== null || fonts.size() == 0){
			return;
		}
		
		log("updateFontList", fonts);
		
		if (offset == 0) {
			mFontListAdapter.getFontList().clear();
		}
		mFontListAdapter.getFontList().addAll(fonts);
		mFontListAdapter.notifyDataSetChanged();
		
		if(offset > 0) {
			mListView.scrollBy(0, scrollBy);
		}
	}
	private void getFontList( int offset){
		NqLog.d("getFontList offset=" + offset);
		FontManager fontManager = FontManager.getInstance(mContext);
		
		// 从缓存中获取字体列表
		List<NqFont> fonts = fontManager.getFontListFromCache(0);
		
		log("getFontListFromCache" , fonts);
		
		if (fonts != null && fonts.size() > 0) {
			updateFontList(offset, fonts);
		}
	}
	/**
	 * 字体列表Adapter
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
				convertView = mInflater.inflate(MResource.getIdByName(mContext, "layout", "nq_font_list_item"), null);
				holder.tvName = (TextView) convertView.findViewById(MResource.getIdByName(mContext, "id", "tv_name"));
				if (FontManager.getInstance(mContext).isCurrentFont(mFontList.get(position))){
					holder.imgDefault = (ImageView) convertView.findViewById(MResource.getIdByName(mContext, "id", "img_default"));
				}else{
					holder.imgDefault = null;
				}
				convertView.setTag(holder);				
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// 赋值
			NqFont font = mFontList.get(position);
			holder.tvName.setText(font.getStrName());
			NqLog.i("fix-me:should use the right default picture");
			holder.imgDefault.setImageResource(R.id.icon) ;
			NqLog.i("fix-me:need action log");
			return convertView;
		}
	}
	
	private static class ViewHolder {
		TextView tvName;
		ImageView imgDefault;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		NqLog.i("fix-me:list is click");
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// to show local detail activity
		
	}

}
