package com.nqmobile.livesdk.commons.mydownloadmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.commons.mydownloadmanager.table.DownloadTable;
import com.nqmobile.livesdk.commons.receiver.PackageAddedEvent;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.app.table.AppLocalTable;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.NotificationUtil;
import com.nqmobile.livesdk.utils.PackageUtils;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * Created by Rainbow on 2014/7/31.
 */
public class DownloadActivity extends BaseActvity implements IDownloadObserver{
	public static final String KEY_FROM = "from";
	public static final String KEY_VALUE = "not_install_notification";
	
    private List<DownloadItem> mDownloadingGroup = new ArrayList<DownloadItem>();
    private List<DownloadItem> mInstallGroup = new ArrayList<DownloadItem>();

    private MyDownloadManager mManager;
    private ProgressDialog mWaitingDialog;
    private ExpandableListView mList;
    private MyListAdapter mAdapter;
    private TextView mEmptyTv;
    private List<String> mGroupName = new ArrayList<String>();
    private Handler mHandler = new Handler();
    private Dialog mDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(MResource.getIdByName(this, "layout", "nq_download_activity"));

        mManager = MyDownloadManager.getInstance(this);

        mList = (ExpandableListView) findViewById(MResource.getIdByName(this,"id","list"));
        mList.setGroupIndicator(null);
        mEmptyTv = (TextView) findViewById(MResource.getIdByName(this,"id","empty_tv"));

        mGroupName.add(MResource.getString(this,"nq_label_downloading"));
        mGroupName.add(MResource.getString(this,"nq_label_wait_install"));

        showLoading();
        LoadTask task = new LoadTask();
//        task.execute(null,null,null);
        ExecutorService FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool();
        task.executeOnExecutor(FULL_TASK_EXECUTOR);
        EventBus.getDefault().register(this);

        //如果页面是从下载未安装push提示点击进入，记录行为日志
        Intent intent = getIntent();
        if (intent != null) {
        	String value = intent.getStringExtra(KEY_FROM);
	        if (value != null && value.equals(KEY_VALUE)) {
	            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
	            		MyDownloadConstants.ACTION_LOG_1909, null, 0, null);
	        } else {
	            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
	            		MyDownloadConstants.ACTION_LOG_3201, null, 0, null);	            
	        }	        
        } 

    }

    private void showLoading(){
        mWaitingDialog = new ProgressDialog(this);
        mWaitingDialog.setMessage(getString(MResource.getIdByName(this,"string","nq_label_loading")));
        mWaitingDialog.setProgress(ProgressDialog.STYLE_HORIZONTAL);
        mWaitingDialog.setCancelable(false);
        mWaitingDialog.show();
    }

    private void dismissLoading(){
    	if (mWaitingDialog.isShowing()) {
    		mWaitingDialog.dismiss();
    	}
    }

    @Override
    public void onChange() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (DownloadItem item : mDownloadingGroup) {
                    updateState(item);
                }

                for (int i = 0; i < mDownloadingGroup.size(); i++) {
                    DownloadItem item = mDownloadingGroup.get(i);
                    if (item.state == MyDownloadManager.STATUS_SUCCESSFUL) {
                        mManager.unregisterDownloadObserver(item.downloadId);
                        if (item.type == MyDownloadManager.DOWNLOAD_TYPE_APP  && !item.resId.isEmpty()) {
                            mInstallGroup.add(item);
                        }

                        mDownloadingGroup.remove(i);
                    }
                }

                checkNoData();

                mAdapter.notifyDataSetChanged();

                if (mInstallGroup.size() > 0) {
                    mList.expandGroup(1);
                }
            }
        });
    }

    private void updateState(DownloadItem item){
        int[] bytesAndStatus = mManager.getBytesAndStatus(item.downloadId);
        if (bytesAndStatus[0] == 1) {
            item.state = bytesAndStatus[1];
            item.downloadBytes = bytesAndStatus[2];
            item.totalBytes = bytesAndStatus[3];
        }
    }

    private class LoadTask extends AsyncTask<Object,Object,Object>{

        @Override
        protected Object doInBackground(Object... objects) {
            Cursor c = null;
            try{
                c = DownloadActivity.this.getContentResolver().query(DownloadTable.TABLE_URI,null,
                		DownloadTable.DOWNLOAD_TYPE + " != ? " + " AND " +  DownloadTable.DOWNLOAD_IS_FINISH + " = ?",
                		new String[]{String.valueOf(MyDownloadManager.DOWNLOAD_TYPE_SLIENT),
                		String.valueOf(MyDownloadManager.DOWNLOAD_NOT_FINISH)},null);
                while (c != null && c.moveToNext()){
                    DownloadItem item = new DownloadItem();
                    item.downloadId = c.getLong(c.getColumnIndex(DownloadTable.DOWNLOAD_DOWNLOAD_ID));
                    item.iconUrl = c.getString(c.getColumnIndex(DownloadTable.DOwNLOAD_ICON_URL));
                    item.name = c.getString(c.getColumnIndex(DownloadTable.DOWNLOAD_NAME));
                    item.totalBytes = c.getLong(c.getColumnIndex(DownloadTable.DOWNLOAD_TOTAL_SIZE));
                    item.downloadpath = c.getString(c.getColumnIndex(DownloadTable.DOWNLOAD_DEST_PATH));
                    item.type = c.getInt(c.getColumnIndex(DownloadTable.DOWNLOAD_TYPE));
                    item.resId = c.getString(c.getColumnIndex(DownloadTable.DOWNLOAD_RES_ID));
                    int[] bytesAndStatus = mManager.getBytesAndStatus(item.downloadId);
                    if (bytesAndStatus[0] == 1 && !item.resId.isEmpty()) {
                        item.state = bytesAndStatus[1];
                        item.downloadBytes = bytesAndStatus[2];
                        item.totalBytes = bytesAndStatus[3];
                        mDownloadingGroup.add(item);
                    }
                }
            }finally {
                if(c != null){
                    c.close();
                }
            }

            Cursor cursor = null;
            try{
                cursor = DownloadActivity.this.getContentResolver().query(DownloadTable.TABLE_URI,null,
                        DownloadTable.DOWNLOAD_IS_FINISH + " = ? AND " + DownloadTable.DOWNLOAD_TYPE + "  = ? AND " + DownloadTable.DOWNLOAD_SHOWFLAG + " = ?",
                        new String[]{String.valueOf(MyDownloadManager.DOWNLOAD_FINISH),
                                String.valueOf(MyDownloadManager.DOWNLOAD_TYPE_APP),
                                String.valueOf(MyDownloadManager.SHOW_INSTALL_FLAG)},null);
                while (cursor != null && cursor.moveToNext()){
                    String resId = cursor.getString(cursor.getColumnIndex(DownloadTable.DOWNLOAD_RES_ID));
                    if(!AppManager.getInstance(DownloadActivity.this).isAppInstallByResId(resId)){
                        DownloadItem item = new DownloadItem();
                        item.downloadId = cursor.getLong(cursor.getColumnIndex(DownloadTable.DOWNLOAD_DOWNLOAD_ID));
                        item.iconUrl = cursor.getString(cursor.getColumnIndex(DownloadTable.DOwNLOAD_ICON_URL));
                        item.name = cursor.getString(cursor.getColumnIndex(DownloadTable.DOWNLOAD_NAME));
                        item.totalBytes = cursor.getLong(cursor.getColumnIndex(DownloadTable.DOWNLOAD_TOTAL_SIZE));
                        item.downloadpath = cursor.getString(cursor.getColumnIndex(DownloadTable.DOWNLOAD_DEST_PATH));
                        item.type = cursor.getInt(cursor.getColumnIndex(DownloadTable.DOWNLOAD_TYPE));
                        item.downloadBytes = item.totalBytes;
                        item.state = MyDownloadManager.STATUS_SUCCESSFUL;
                        item.resId = cursor.getString(cursor.getColumnIndex(DownloadTable.DOWNLOAD_RES_ID));

                        File file = new File(item.downloadpath);
                        if(file.exists() && !item.resId.isEmpty()){
                            mInstallGroup.add(item);
                        }
                    }
                }
            }finally {
                if(cursor != null){
                    cursor.close();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            dismissLoading();

            NqLog.i("onPostExecute mDownloadingGroup=" + mDownloadingGroup.size() + " mInstallGroup=" + mInstallGroup.size());

            mAdapter = new MyListAdapter(DownloadActivity.this);
            mList.setAdapter(mAdapter);

            if(mDownloadingGroup.size() > 0){
                mList.expandGroup(0);
            }

            if(mInstallGroup.size() > 0){
                mList.expandGroup(1);
            }

            checkNoData();

            for(DownloadItem item:mDownloadingGroup){
                mManager.registerDownloadObserver(item.downloadId,DownloadActivity.this);
            }
        }
    }

    private void checkNoData() {
        if(mDownloadingGroup.size() + mInstallGroup.size() == 0){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mList.setVisibility(View.GONE);
                    mEmptyTv.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private class DownloadItem{
        public String resId;
        public long downloadId;
        public String iconUrl;
        public String name;
        public int state;
        public long downloadBytes;
        public long totalBytes;
        public String downloadpath;
        public int type;
    }

    private void showConfirmDeleteDialog(final long downloadId,final int oldState){
        View view = LayoutInflater.from(this).inflate(MResource.getIdByName(this, "layout", "nq_confirm_cancle_dialog"), null);
        view.findViewById(MResource.getIdByName(this, "id", "cancel")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
                if(oldState == MyDownloadManager.STATUS_RUNNING){
                    mManager.resumeDownload(downloadId);
                }
                
	            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
	            		MyDownloadConstants.ACTION_LOG_3204, null, 0, null);
            }
        });

        view.findViewById(MResource.getIdByName(this, "id", "ok")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManager.cancelDownload(downloadId);
                removeDownloadingItem(downloadId);
                mDialog.dismiss();
                
	            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
	            		MyDownloadConstants.ACTION_LOG_3203, null, 0, null);
            }
        });

        mDialog = new Dialog(this, MResource.getIdByName(this,"style","Translucent_NoTitle"));
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
    }

    private void removeDownloadingItem(final long downloadId){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for(Iterator<DownloadItem> iter = mDownloadingGroup.iterator();iter.hasNext();){
                    if(iter.next().downloadId == downloadId){
                        iter.remove();
                    }
                }

                checkNoData();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private class MyListAdapter extends BaseExpandableListAdapter {

        private LayoutInflater mInflater;
        private Context mContext;

        public MyListAdapter(Context context){
            mContext = context;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getGroupCount() {
            return getGroupSize();
        }

        @Override
        public int getChildrenCount(int i) {
            if (i == 0) {
                return mDownloadingGroup.size();
            } else {
                return mInstallGroup.size();
            }
        }

        private int getGroupSize(){
            return mInstallGroup.size() > 0? 2: 1;
        }

        @Override
        public Object getGroup(int i) {
            return mGroupName.get(i);
        }

        @Override
        public Object getChild(int i, int i2) {
            if(i == 0){
                return mDownloadingGroup.get(i2);
            }else{
                return mInstallGroup.get(i2);
            }
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i2) {
            return i2;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
            if(view == null){
                view = mInflater.inflate(MResource.getIdByName(mContext,"layout","nq_download_group_view"),null);
            }

            TextView title = (TextView) view.findViewById(MResource.getIdByName(mContext, "id", "title"));
            title.setText(mGroupName.get(i));

            Button btnClear = (Button) view.findViewById(MResource.getIdByName(mContext,"id","clearBtn"));
            btnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for(DownloadItem item:mInstallGroup){
                        mManager.flagInstallShowFlag(item.downloadId,MyDownloadManager.NOT_SHOW_INSTALL_FLAG);
                    }
                    mInstallGroup.removeAll(mInstallGroup);
                    NotificationUtil.cancleNoti(DownloadActivity.this,NotificationUtil.NOTIF_ID_INSTALL_APP);
                	NotificationUtil.cancleNoti(DownloadActivity.this, NotificationUtil.NOTIF_ID_DOWNLOAD_NOTINSTALL);
                    checkNoData();
                    mAdapter.notifyDataSetChanged();
                    
                    //全部清除点击的行为日志
    	            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
    	            		MyDownloadConstants.ACTION_LOG_3208, null, 0, null);
                }
            });

            if (i == 0) {
                btnClear.setVisibility(View.INVISIBLE);
            }else{
                btnClear.setVisibility(View.VISIBLE);
            }
            return view;
        }

        private DownloadItem getDownloadItemByPosition(int group, int position) {
            List<DownloadItem> list = null;
            DownloadItem item = null;
            if (group == 0) {
                list = mDownloadingGroup;
            } else {
                list = mInstallGroup;
            }
            item = list.get(position);

            return item;
        }

        @Override
        public View getChildView(final int i, final int i2, boolean b, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if(view == null){
                holder = new ViewHolder();
                view = mInflater.inflate(MResource.getIdByName(mContext,"layout","nq_download_child_view"),null);
                holder.mIcon = (AsyncImageView) view.findViewById(MResource.getIdByName(mContext,"id","iv_icon"));
                holder.mName = (TextView) view.findViewById(MResource.getIdByName(mContext,"id","tv_name"));
                holder.mState = (TextView) view.findViewById(MResource.getIdByName(mContext,"id","tv_state"));
                holder.mProgress = (ProgressBar) view.findViewById(MResource.getIdByName(mContext,"id","progress"));
                holder.mCancle = (LinearLayout) view.findViewById(MResource.getIdByName(mContext,"id","cancle_layout"));
                holder.mPause = (ImageView) view.findViewById(MResource.getIdByName(mContext,"id","iv_download"));

                view.setTag(holder);
            }else{
                holder = (ViewHolder) view.getTag();
            }

            final DownloadItem item = getDownloadItemByPosition(i,i2);

            final long downloadId = item.downloadId;
            final ImageView mPause = holder.mPause;

            holder.mName.setText(item.name);
            //如果下载的资源无资源id，icon显示为桌面应用的logo
            if (item.resId == null || item.resId.isEmpty()) {
                holder.mIcon.loadImage(MResource.getIdByName(mContext, "drawable", "logo"));
            } else {
                holder.mIcon.loadImage(item.iconUrl, null, MResource.getIdByName(mContext, "drawable", "nq_icon_default"));
            }
            holder.mCancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int oldState = item.state;
                    if(item.state == MyDownloadManager.STATUS_RUNNING){
                        mManager.pauseDownload(downloadId);
                    }

                    showConfirmDeleteDialog(downloadId, oldState);
    	            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
    	            		MyDownloadConstants.ACTION_LOG_3202, item.resId, 0, getPackageNameByResId(item.resId));
                }
            });

            if(i == 1){
                holder.mCancle.setVisibility(View.GONE);
                holder.mProgress.setVisibility(View.GONE);
            }else{
                holder.mCancle.setVisibility(View.VISIBLE);
                holder.mProgress.setVisibility(View.VISIBLE);
            }

            if(item.state == MyDownloadManager.STATUS_PAUSED) {
                holder.mPause.setImageResource(MResource.getIdByName(mContext, "drawable", "nq_download_download_icon"));
            }else if(item.state == MyDownloadManager.STATUS_SUCCESSFUL){
                holder.mPause.setImageResource(MResource.getIdByName(mContext,"drawable","nq_store_app_install_normal"));
            }else{
                holder.mPause.setImageResource(MResource.getIdByName(mContext,"drawable","nq_download_pause_icon"));
            }

            holder.mPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DownloadItem item = getDownloadItemByPosition(i,i2);
                    if(item.state == MyDownloadManager.STATUS_PAUSED) {
                        if (!NetworkUtils.isConnected(mContext)) {
                            ToastUtils.toast(DownloadActivity.this, "nq_nonetwork");
                            return;
                        }

                        mManager.resumeDownload(downloadId);
                        item.state = MyDownloadManager.STATUS_PENDING;
                        mPause.setImageResource(MResource.getIdByName(mContext, "drawable", "nq_download_pause_icon"));
                        
                        //继续下载点击的行为日志
        	            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
        	            		MyDownloadConstants.ACTION_LOG_3206, item.resId, 0, null);
                    }else if(item.state == MyDownloadManager.STATUS_SUCCESSFUL) {
                        //点击安装的行为日志
        	            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
        	            		MyDownloadConstants.ACTION_LOG_3207, item.resId, 0, getPackageNameByResId(item.resId));
        	            
                    	PackageUtils.installApp(DownloadActivity.this, item.downloadpath);
                    }else{
                        mManager.pauseDownload(downloadId);
                        item.state = MyDownloadManager.STATUS_PAUSED;
                        mPause.setImageResource(MResource.getIdByName(mContext,"drawable","nq_download_download_icon"));
                        
                        //暂停点击的行为日志
        	            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
        	            		MyDownloadConstants.ACTION_LOG_3205, item.resId, 0, null);
                    }
                }
            });

            updateView(item,holder);

            return view;
        }

        @Override
        public boolean isChildSelectable(int i, int i2) {
            return false;
        }

        private void updateView(DownloadItem item,ViewHolder holder){
            holder.mState.setText(getStateText(item.state));
            if (item.totalBytes > 0 && item.downloadBytes >= 0
                    && item.downloadBytes <= item.totalBytes) {
                int percentage = (int) Math.floor(item.downloadBytes * 100/item.totalBytes);
                holder.mProgress.setProgress(percentage);
            }else{
                holder.mProgress.setProgress(0);
            }
        }

        private class ViewHolder{
            public AsyncImageView mIcon;
            public TextView mName;
            public TextView mState;
            public ProgressBar mProgress;
            private LinearLayout mCancle;
            private ImageView mPause;
        }
    }

    private String getStateText(int state){
        String result = "";
        switch (state){
            case MyDownloadManager.STATUS_FAILED:
            case MyDownloadManager.STATUS_NONE:
            case MyDownloadManager.STATUS_PENDING:
                result = MResource.getString(this,"nq_download_waiting");
                break;
            case MyDownloadManager.STATUS_RUNNING:
                result = MResource.getString(this,"nq_download_downloading");
                break;
            case MyDownloadManager.STATUS_PAUSED:
                result = MResource.getString(this,"nq_download_pasue");
                break;
            case MyDownloadManager.STATUS_SUCCESSFUL:
                result = MResource.getString(this,"nq_download_complete");
                break;
        }
        return result;
    }

    public void onEvent(PackageAddedEvent event){
        String packageName = event.getPackageName();
        NqLog.v("DownloadActivity:installed packagename: " + packageName);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mInstallGroup != null && mInstallGroup.size() > 0){
                    boolean needrefresh = false;
                    for(int i = 0;i < mInstallGroup.size();i++){
                        DownloadItem item = mInstallGroup.get(i);
                        if(AppManager.getInstance(DownloadActivity.this).isAppInstallByResId(item.resId)){
                            mInstallGroup.remove(i);
                            needrefresh = true;
                        }
                    }

                    checkNoData();

                    if(needrefresh){
                        mAdapter.notifyDataSetChanged();
                    }
                }
                
                if (mInstallGroup.size() <= 0) {
                	NotificationUtil.cancleNoti(DownloadActivity.this, NotificationUtil.NOTIF_ID_DOWNLOAD_NOTINSTALL);
                }
            }
        });
    }
    
    private String getPackageNameByResId(String resId) {
        Cursor c = null;
        String packagename = "";
        try{
            c = DownloadActivity.this.getContentResolver().query(DownloadTable.TABLE_URI,null,DownloadTable.DOWNLOAD_RES_ID + " = ?",new String[]{resId},null);
            if(c != null && c.moveToNext()){
                packagename = c.getString(c.getColumnIndex(DownloadTable.DOWNLOAD_PACKAGENAME));
            }
            if(TextUtils.isEmpty(packagename)){
                c = DownloadActivity.this.getContentResolver().query(AppLocalTable.LOCAL_APP_URI,null,AppLocalTable.APP_ID + " = ?",new String[]{resId},null);
                if(c != null && c.moveToNext()){
                    packagename = c.getString(c.getColumnIndex(AppLocalTable.APP_PACKAGENAME));
                }
            }
        }finally {
            if(c != null){
                c.close();
            }
        }
        return packagename;
    }
}