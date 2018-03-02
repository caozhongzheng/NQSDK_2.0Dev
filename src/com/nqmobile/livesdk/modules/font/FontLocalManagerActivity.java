package com.nqmobile.livesdk.modules.font;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nqmobile.live.R;
import com.nqmobile.live.store.ui.StoreMainActivity;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.DisplayUtil;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.StringUtil;


public class FontLocalManagerActivity<MainActivity> extends BaseActvity implements AdapterView.OnItemClickListener, View.OnClickListener{
	private static final ILogger NqLog = LoggerFactory.getLogger(FontModule.MODULE_NAME);
	
	private Context mContext ;

	private ListView mListView;
	private TextView mOnlineFont;
	private Typeface mDefaultTypeface;
	private int scrollBy;
	// 数据
	private FontListAdapter mFontListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		NqLog.i("on FontLocalManagerActivity onCrete Function.");
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();
		scrollBy = DisplayUtil.dip2px(mContext, 10);
		
		
		setContentView(MResource.getIdByName(getApplication(), "layout","nq_font_local_manager"));

		mListView = (ListView) findViewById(MResource.getIdByName(mContext,"id", "ldFont"));


		mFontListAdapter = new FontListAdapter();
		mListView.setAdapter(mFontListAdapter);

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parentAdapter, View view,
					int position, long id) {
				ViewHolder holder = (ViewHolder) view.getTag();
				NqLog.i("the text :" + holder.tvName.getText());
				NqLog.i("fix-me: we should start local font detail activity");
				
				// to show local detail activity
				List<NqFont> list = mFontListAdapter.getFontList();
				if (list!=null && position >=0 && position < list.size()) {
					NqFont font = list.get(position);
					FontManager.getInstance(mContext).viewFontLocalDetail( font);
					finish();
				}				
			}
		});
		
		mOnlineFont = (TextView) findViewById(MResource.getIdByName(mContext,"id","moudle_name_more"));
		mDefaultTypeface = mOnlineFont.getTypeface();
		mOnlineFont.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("FontClick", "mOnlineFont is onclick");
                Intent intent = new Intent(mContext, StoreMainActivity.class);
                intent.putExtra(StoreMainActivity.KEY_FRAGMENT_INDEX_TO_SHOW, 3);
                intent.putExtra(StoreMainActivity.KEY_FRAGMENT_COLUMN_TO_SHOW, 0);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		NqLog.i("on FontLocalManagerActivity onResume Function.");

		loadMoreData();
		mFontListAdapter.notifyDataSetChanged();

	}
	public void loadMoreData() {
		//如果是自动加载,可以在这里放置异步加载数据的代码
		List<NqFont> list =  mFontListAdapter.getFontList();

		NqLog.d("loading... " );
		getFontList( 0);
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
		NqLog.d("updateFontList:  offset=" + offset + " fonts=" + fonts + " fonts.size() " + fonts.size());
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
		List<NqFont> fonts = fontManager.getFontListFromLocal(0);
		
		log("getFontListFromLocal" , fonts);
		
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
				convertView = mInflater.inflate(MResource.getIdByName(mContext, "layout", "nq_font_local_list_item"), null);
				holder.tvName = (TextView) convertView.findViewById(MResource.getIdByName(mContext, "id", "tv_name"));
				holder.imgDefault = (ImageView) convertView.findViewById(MResource.getIdByName(mContext, "id", "imgDefault"));
				convertView.setTag(holder);				
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// 赋值
			NqFont font = mFontList.get(position);
			NqLog.i("set listview item;font.getStrName: " + font.getStrName());
			holder.tvName.setText(font.getStrName());
			FontManager.getInstance(mContext).setTypeface(font, holder.tvName, false);
			if (position == 0){
				holder.tvName.setTypeface(Typeface.DEFAULT);
			}
			if (FontManager.getInstance(mContext).isCurrentFont(mFontList.get(position))){
				holder.imgDefault.setImageResource(R.drawable.nq_pop_check_mark_select);
			}
			return convertView;
		}
	}
	
	private static class ViewHolder {
		TextView tvName;
		ImageView imgDefault;
	}
	
	@Override
	public void onClick(View v) {
		NqLog.i("fix-me:list is click");
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		// to show local detail activity
		List<NqFont> list = mFontListAdapter.getFontList();
		int pos = (int)parent.getAdapter().getItemId(position);
		if (list!=null && pos >=0 && pos < list.size()) {
			NqFont font = list.get(pos);
			FontManager.getInstance(mContext).viewFontLocalDetail( font);
			finish();
		}		
	}

}
