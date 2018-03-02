package com.nqmobile.livesdk.commons.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nqmobile.livesdk.commons.modal.ResData;
import com.nqmobile.livesdk.commons.modal.ResItem;
import com.nqmobile.livesdk.commons.modal.SectionListItem;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.Tools;

public abstract class BaseListAdapter extends BaseAdapter {
	private static final String TAG = "BaseListAdapter";
	
	private List<SectionListItem> mItems = new ArrayList<SectionListItem>();
	private Map<String, Integer> mSectionPositions = new HashMap<String, Integer>();
	private List<String> mSectionNames = new ArrayList<String>();
	private List<ResItem> mRemaining = new ArrayList<ResItem>();
    private int mItemSum;

	LayoutInflater mInflater;
	private Context mContext;
	protected int mCurrIndex;
	
	public BaseListAdapter(Context context, ListView listView, ArrayList<SectionListItem> items, int currIndex) {
		this.mInflater = LayoutInflater.from(context);
		mContext = context;
		mItems = items;
		mCurrIndex = currIndex;
	}
	
	 public List<SectionListItem> getItems(){
         return mItems;
     }
	
	@Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
		ViewHolder vHolder = null;
        View view = convertView;

        int layoutId = getLayoutBySection(getSectionForPosition(position));
    	//NqLog.i("cym", "position=" + position + ",section=" +  getSectionForPosition(position) + ",size=" + linkedAdapter.items.size());
        if (view != null){
        	vHolder = (ViewHolder) view.getTag();
        	if (layoutId != vHolder.layoutId){
        		vHolder = null;
        	}
        }
        if (vHolder == null) {
            view = mInflater.inflate(layoutId, null);
            vHolder = new ViewHolder();
            vHolder.header_index = (TextView) view.findViewById(MResource.getIdByName(mContext, "id", "title"));
            vHolder.item0 = view.findViewById(MResource.getIdByName(mContext, "id", "itemOne"));
            vHolder.item1 = view.findViewById(MResource.getIdByName(mContext, "id", "itemTwo"));
            vHolder.item2 = view.findViewById(MResource.getIdByName(mContext, "id", "itemThree"));
            vHolder.item3 = view.findViewById(MResource.getIdByName(mContext, "id", "itemFour"));
            view.setTag(vHolder);
        }
        
        final SectionListItem currentItem = mItems.get(position);
        if (currentItem != null) {
            ResData resData = (ResData) currentItem.item;
            ArrayList<ResItem> resItems = resData.mResItems;
            int size = resItems.size();
            
            int layoutType = groupMap.get(resItems.get(0).getGroupName()); 
            vHolder.item1.setVisibility(size > 1 ? View.VISIBLE : layoutType < 2 ? View.GONE : View.INVISIBLE);
            vHolder.item2.setVisibility(size > 2 ? View.VISIBLE : layoutType < 3 ? View.GONE : View.INVISIBLE);
            vHolder.item3.setVisibility(size > 3 ? View.VISIBLE : layoutType < 4 ? View.GONE : View.INVISIBLE);
            
        	setData(vHolder.item0, layoutType, resItems.get(0), 0, position);
            if (size > 1) {
            	setData(vHolder.item1, layoutType, resItems.get(1), 1, position);
            }
            if (size > 2) {
            	setData(vHolder.item2, layoutType, resItems.get(2), 2, position);
            }
            if (size > 3) {
            	setData(vHolder.item3, layoutType, resItems.get(3), 3, position);
            }
            
            if (vHolder.header_index != null) {
            	vHolder.header_index.setText(currentItem.section);
            }
			int section = getSectionForPosition(position);
			if (Tools.isEmpty(currentItem.section)) {
				vHolder.header_index.setVisibility(View.GONE);
			}else if (getPositionForSection(section) == position){
				vHolder.header_index.setVisibility(View.VISIBLE);
				setHeaderIndexPadding(vHolder.header_index, position);
        	} else {
        		vHolder.header_index.setVisibility(View.GONE);
        	}
        }
        return view;
    }
	protected void setHeaderIndexPadding(View headView, int pos){}
	
	protected abstract int getLayoutBySection(int section);
	
	protected abstract int getResourceNumPerRow(int section);

	protected abstract void setData(View view, int layoutType, final ResItem resItem, final int index, final int pos);
	
	public class ViewHolder{
		public TextView header_index;
		public View item0, item1, item2, item3;
		public int layoutId;
	}

	public int getSectionForPosition(int position){
		SectionListItem item = mItems.get(position);
		return item.seq;
	}
	
	public int getPositionForSection(int section){
		if (section < 0 || section >= mSectionNames.size()){
			return -1;
		}
		
		String sectionName = mSectionNames.get(section);
		if (mSectionPositions.containsKey(sectionName)){
			return mSectionPositions.get(sectionName);
		} else {
			return -1;
		}		
	}
	
	public void setItems(List<ResItem> resList, boolean clearAll,boolean loadFlag) {
        if (resList == null || resList.size() == 0){
            return;
        }

        if(clearAll){
            mItemSum = resList.size();
            mItems.clear();
            mSectionPositions.clear();
            mRemaining.clear();
            groupMap.clear();
            mSectionNames.clear();
        }else{
            mItemSum += resList.size();
        }

        resList.addAll(0, mRemaining);
        mRemaining.clear();
        List<SectionListItem> list = getListByGroupName(resList,loadFlag);

        int position = mItems.size();
        mItems.addAll(list);
        for(int i=position; i<mItems.size(); i++){
        	String sectionName = mItems.get(i).section;
        	if (!mSectionPositions.containsKey(sectionName)){
        		mSectionPositions.put(sectionName, i);
        		mSectionNames.add(sectionName);
        	}
        }

        notifyDataSetChanged();
    }

    public int getItemSum(){
        return mItemSum;
    }
	
	private List<SectionListItem> getListByGroupName(List<ResItem> resources,boolean loadFlag){
        List<SectionListItem> result = new ArrayList<SectionListItem>();
		if (resources == null || resources.size() == 0){
			return result;
		}		
        
        int section = mSectionPositions.size() - 1;
		String lastGroupName = section == -1 ? "" : mItems.get(mItems.size() - 1).section;
        int resNumPerRow = 0;
        int resNum = 0;
        ResData rowData = null;
        for(ResItem res : resources){
        	String groupName = res.getGroupName();
        	if (TextUtils.isEmpty(groupName)){
        		continue;
        	}
        	
        	if (groupName.equals(lastGroupName)){
        		resNumPerRow = getResourceNumPerRow(section);
        	} else {
        		section = section + 1;
        		resNumPerRow = getResourceNumPerRow(section);        		
        		lastGroupName = groupName;
        		resNum = 0;
        	}
        	if (!groupMap.containsKey(groupName)) {
        		int numPerRow = getResourceNumPerRow(groupMap.size());
        		groupMap.put(groupName, numPerRow);
			}
        	resNumPerRow = groupMap.get(groupName);
        	
        	if (resNum % resNumPerRow == 0) {
        		rowData = new ResData();
        		rowData.mLayoutType = resNumPerRow;
        		result.add(new SectionListItem(rowData, groupName, section));
        		resNum = 0;
        	}

            rowData.mResItems.add(res);
    		resNum = resNum + 1;
    		
        }

        if(!loadFlag){
            if (resNum != resNumPerRow && rowData != null && rowData.mResItems.size() > 0){
                result.remove(result.size() - 1);
                mRemaining = rowData.mResItems;
            }
        }

        return result;
    }
	
	protected Map<String, Integer> groupMap = new HashMap<String, Integer>();

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}	
	
}
