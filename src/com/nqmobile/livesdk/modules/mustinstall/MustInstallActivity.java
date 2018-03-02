package com.nqmobile.livesdk.modules.mustinstall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.commons.adapter.BaseListAdapter;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.modal.ResItem;
import com.nqmobile.livesdk.commons.modal.SectionListItem;
import com.nqmobile.livesdk.commons.mydownloadmanager.MyDownloadManager;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.commons.ui.DownloadStateView;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.apptype.model.AppTypeInfo;
import com.nqmobile.livesdk.modules.apptype.network.GetAppTypeProtocol;
import com.nqmobile.livesdk.modules.font.FontManager;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.GpUtils;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PackageUtils;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * Created by Rainbow on 14-4-3.
 */
public class MustInstallActivity extends BaseActvity implements OnClickListener{
	private static final ILogger NqLog = LoggerFactory.getLogger(MustInstallModule.MODULE_NAME);

	public static final String ACTION_REFRESH = "com.nqmobile.live.must_install_refresh";
	public static final int ADAPTER_ALL_REFRESH = 1;
    private ListView mList;

    private MustInstallAdapter mAdapter;
    private RelativeLayout mBottomLayout;
    private View mGetMore;
    private Button mInstallAll;
    private ImageView mCheckAll;
    private View mEmptyLayout;
    private TextView mEmptyTv;
    private boolean mSelectAll;

    private List<ResItem> mAlldata = new ArrayList<ResItem>(); //所有应用
	protected ArrayList<SectionListItem> sectionsMap;

    private ProgressDialog mWaitingDialog;

    private AppManager mAppManager;
    private MyDownloadManager mManager;

    public static final int FROM_NOTI = 0;
    public static final int FROM_STORE = 1;
    public static final int FROM_SHORTCUT = 2;

    private static final int TYPE_GAME = 200;
    private static final int TYPE_SOFT = 100;
    private Context mContext;
    private LayoutInflater mInflater;

    private int mFrom;
    private MustInstallPreference mHelper;
    private boolean mGpResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(MResource.getIdByName(getApplication(), "layout", "nq_must_install_activity"));
        mAppManager = AppManager.getInstance(this);
        mManager = MyDownloadManager.getInstance(this);
        mContext = getApplicationContext();
        mInflater = LayoutInflater.from(mContext);
        sectionsMap = new ArrayList<SectionListItem>();

        MustInstallManager.getInstance(this);
        mHelper = MustInstallPreference.getInstance();
        mHelper.setBooleanValue(MustInstallPreference.KEY_MUST_INSTALL_ENTERED, true);
        createShortCut();
        mFrom = getIntent().getIntExtra("from",-1);
        NqLog.i("from="+mFrom);
        switch (mFrom){
            case FROM_NOTI:
                StatManager.getInstance().
                        onAction(StatManager.TYPE_STORE_ACTION, MustInstallActionConstants.ACTION_LOG_1802, null, 0, "0");
                break;
            case FROM_STORE:
                StatManager.getInstance().
                        onAction(StatManager.TYPE_STORE_ACTION, MustInstallActionConstants.ACTION_LOG_1802, null, 0, "1");
                break;
            case FROM_SHORTCUT:
                StatManager.getInstance().
                        onAction(StatManager.TYPE_STORE_ACTION, MustInstallActionConstants.ACTION_LOG_1805, null, 0, null);
                break;
        }
        findViews();
        registDownFinishReceiver();
    }

	public void doInstallButton(boolean enable) {
		mCheckAll.setEnabled(enable);
		mInstallAll.setEnabled(enable);
		mInstallAll.setBackgroundResource(enable ? MResource.getIdByName(getApplication(), "color", "nq_must_install_def") :
				MResource.getIdByName(getApplication(), "color", "nq_must_install_disable"));
	}

    private void createShortCut(){
        boolean created = mHelper.getBooleanValue(MustInstallPreference.KEY_MUSI_INSTALL_ICON_CREATE);
        NqLog.i("IconCreated="+created);
        if(!created){
            Intent shortcut = new Intent("com.android.launcher.action.INSTALL_ENTRY_SHORTCUT");
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(MResource.getIdByName(this, "string", "nq_label_mustinstall")));
            shortcut.putExtra("duplicate", false); // 不允许重复创建
            Intent i = new Intent();
            i.setAction(Intent.ACTION_MAIN);
            i.setClassName(this.getPackageName(), this.getClass().getName());
            i.putExtra("from",FROM_SHORTCUT);
            i.putExtra("shortcutIcon", MResource.getIdByName(mContext,"drawable", "nq_mustinstall_icon"));
            i.putExtra(Intent.EXTRA_SHORTCUT_NAME,MResource.getString(mContext, "nq_label_mustinstall"));
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
            Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(this,
                    MResource.getIdByName(this,"drawable","nq_mustinstall_icon"));
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
            sendBroadcast(shortcut);
            mHelper.setBooleanValue(MustInstallPreference.KEY_MUSI_INSTALL_ICON_CREATE,true);

            Toast.makeText(MustInstallActivity.this,
                    MustInstallActivity.this.getString(MResource.getIdByName(MustInstallActivity.this,"string","nq_create_mustinstall_icon")),
                    Toast.LENGTH_SHORT).show();

            StatManager.getInstance().
                    onAction(StatManager.TYPE_STORE_ACTION, MustInstallActionConstants.ACTION_LOG_1803, null, 0, null);
        }
    }

    private void downloadAll(int netflag){
        NqLog.i("downloadAll");
        if (!NetworkUtils.isConnected(this)) {
        	ToastUtils.toast(this, "nq_nonetwork");
            return;
        }

        Toast.makeText(MustInstallActivity.this,
                MustInstallActivity.this.getString(MResource.getIdByName(MustInstallActivity.this,"string","nq_start_downloading")),
                Toast.LENGTH_SHORT).show();
        List<App> apps = mAdapter.getSelected();
        Collections.sort(apps, new Comparator<ResItem>() {
            @Override
            public int compare(ResItem lhs, ResItem rhs) {
                return (int) (rhs.order - lhs.order);
            }
        });

    	NqLog.d("mustinstall order:" + apps.size());
        for(ResItem resItem : apps){
        	App app = (App) resItem;
        	int status = mAppManager.getStatus(app).statusCode;
        	NqLog.d("mustinstall order:" + app.order + " status:" + status + " " + app);
            if(status == AppManager.STATUS_PAUSED){
                Long downloadId = mManager.getDownloadIdByResID(app.getStrId());
                if(downloadId != null){
                    mManager.resumeDownload(downloadId);
                }
            }else if(status != AppManager.STATUS_INSTALLED && status != AppManager.STATUS_DOWNLOADING){
	            mAppManager.downloadApp(app, netflag);
                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION, MustInstallActionConstants.ACTION_LOG_1807,
                        app.getStrId(), app.getStrId().startsWith("AD_")?2:0, null);
            }
        }
        
        mAdapter.notifyDataSetChanged();
        doInstallButton(false);
        finish();
    }

    private void findViews(){
        mList = (ListView) findViewById(MResource.getIdByName(this,"id","list"));

        mBottomLayout = (RelativeLayout) findViewById(MResource.getIdByName(this,"id","bottom_layout"));
        boolean appEnable = PreferenceDataHelper.getInstance(this).getBooleanValue(PreferenceDataHelper.KEY_APP_ENABLE);
        NqLog.i("appEnable="+appEnable+" from="+mFrom);
        if(appEnable && mFrom == FROM_SHORTCUT){
            mGetMore = findViewById(MResource.getIdByName(this,"id","getMoreLayout"));
            mGetMore.setVisibility(View.VISIBLE);
            mGetMore.setOnClickListener(this);
        }

        mEmptyLayout = findViewById(MResource.getIdByName(this,"id","empty_layout"));
        mEmptyTv = (TextView) findViewById(MResource.getIdByName(this,"id","empty_tv"));
        mInstallAll = (Button) findViewById(MResource.getIdByName(this,"id","install"));
        mInstallAll.setOnClickListener(this);

        mCheckAll = (ImageView) findViewById(MResource.getIdByName(this,"id","checkAll"));
        mCheckAll.setImageResource(MResource.getIdByName(this,"drawable","nq_select_on"));
        mSelectAll = true;
        mCheckAll.setOnClickListener(this);
        mAdapter = new MustInstallAdapter(mContext, mList, new ArrayList<SectionListItem>(), 0);

        loadData();
    }

    private int mColumn;
    private List<App> mApps;
    private void loadData(){
    	NqLog.d("loadData");
        if(NetworkUtils.isConnected(this)){
            showLoading();
            MustInstallManager.getInstance(this).getAppList(new AppListListener() {
                @Override
                public void onGetAppListSucc(List<App> apps) {
                    NqLog.i("onGetAppListSucc apps.size="+apps.size());
                    dismissLoading();
                    mApps = apps;
                    handerApps(apps);
                }

                @Override
                public void onErr() {
                    NqLog.i("getAppList onErr");
                    dismissLoading();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onLoadFaild();
                        }
                    });
                }
            });
        }else{
        	NqLog.d("loadData else");
            onLoadFaild();
        }
    }
    
    private void handerApps(List<App> apps) {
		if (apps != null && apps.size() > 0) {
            filterInstall(apps);
            processList(apps);
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onAllInstall();
                }
            });
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mList.setVisibility(View.VISIBLE);
                if(mGpResource){
                    mBottomLayout.setVisibility(View.GONE);
                }else{
                    mBottomLayout.setVisibility(View.VISIBLE);
                }

                mEmptyLayout.setVisibility(View.GONE);

                //全部被安装了
                if (mAlldata.size() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onAllInstall();
                        }
                    });
                } else {
                    mList.setAdapter(mAdapter);
                    mAdapter.selectAll();
                }
            }
        });
	}

    private void onAllInstall() {
        NqLog.i("onAllInstall");
        mList.setVisibility(View.GONE);
        mEmptyLayout.setVisibility(View.VISIBLE);
        mEmptyTv.setText(getString(MResource.getIdByName(this, "string", "nq_no_mustinstall_content")));
        mBottomLayout.setVisibility(View.GONE);
    }

    private void onLoadFaild() {
    	NqLog.d("onLoadFaild");
        mList.setVisibility(View.GONE);
        mBottomLayout.setVisibility(View.GONE);
        mEmptyLayout.setVisibility(View.VISIBLE);
        mEmptyTv.setText(getString(MResource.getIdByName(this, "string", "nq_connection_failed")));
        mEmptyLayout.setOnClickListener(this);
    }

    private void sendGameEvent(List<App> list){
        List<AppTypeInfo> appTypeList = new ArrayList<AppTypeInfo>();
        for (App app : list) {
            String pkgName = app.getStrPackageName();
            AppTypeInfo appType = new AppTypeInfo();
            appType.setPackageName(pkgName);
            appType.setCode(200);
            appTypeList.add(appType);
        }

        EventBus.getDefault().post(new GetAppTypeProtocol.GetAppTypeSuccessEvent(appTypeList, null));
    }

    private void processList(List<App> list){
        NqLog.i("processList list.size="+list.size());
        mAlldata.clear();

        if(list.size() == 0){
            return;
        }

        //只要资源中有一个gp资源就认为所有的都是gp资源
        App a = list.get(0);
        mGpResource = a.isGpApp();

        NqLog.i("mGpResource="+mGpResource+" a.clickActionType="+a.getIntClickActionType());
        
        List<App> soft = new ArrayList<App>();
        List<App> game = new ArrayList<App>();
        int order = 0;
        for(App app:list){
        	app.order = order;
            if(app.getType() == TYPE_SOFT){
            	app.setGroupName(getString(MResource.getIdByName(MustInstallActivity.this,"string","nq_mustinstall_soft")));
                soft.add(app);
            }
            if(app.getType() == TYPE_GAME){
            	app.setGroupName(getString(MResource.getIdByName(MustInstallActivity.this,"string","nq_mustinstall_game")));
                game.add(app);
            }
            ++order;
        }

        boolean isEnable = false;
        NqLog.i("soft.size="+soft.size()+" game.size="+game.size());
        if(soft.size() > 0){
            for(int i=0;i<soft.size();i++){
                ResItem item = soft.get(i);
                item.setGroupName(getString(MResource.getIdByName(MustInstallActivity.this,"string","nq_mustinstall_soft")));
                item.mStatus = mAppManager.getStatus(soft.get(i)).statusCode;
                mAlldata.add(item);

                if(!mGpResource){
                    if(!isEnable && (item.mStatus == AppManager.STATUS_DOWNLOADING ||
                            item.mStatus == AppManager.STATUS_PAUSED))
                        isEnable = false;
                    else
                        isEnable = true;
                }
            }
        }
        
        if(game.size() > 0){
            for(int i=0;i<game.size();i++){
            	ResItem item = game.get(i);
                item.setGroupName(getString(MResource.getIdByName(MustInstallActivity.this,"string","nq_mustinstall_game")));
                item = game.get(i);
                item.mStatus = mAppManager.getStatus(game.get(i)).statusCode;
                mAlldata.add(item);

                if(!mGpResource){
                    if(!isEnable && (item.mStatus == AppManager.STATUS_DOWNLOADING ||
                            item.mStatus == AppManager.STATUS_PAUSED))
                        isEnable = false;
                    else
                        isEnable = true;
                }
            }

            sendGameEvent(game);
        }

        NqLog.i("All Ddata="+mAlldata.size());
        mAdapter.setItems(mAlldata, true, true);
        final boolean enable = isEnable;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!mGpResource){
                    doInstallButton(enable);
                }
            }
        });
    }


    private void showLoading(){
        mWaitingDialog = new ProgressDialog(this);
        mWaitingDialog.setMessage(getString(MResource.getIdByName(this,"string","nq_label_loading")));
        mWaitingDialog.setProgress(ProgressDialog.STYLE_HORIZONTAL);
        mWaitingDialog.setCancelable(false);
        mWaitingDialog.show();
    }

    private void dismissLoading(){
        mWaitingDialog.dismiss();
    }

    private void filterInstall(List<App> apps){
        NqLog.i("filterInstall");
    	boolean isDel;
        for(Iterator iter = apps.iterator();iter.hasNext();){
        	isDel = false;
        	App app = (App)iter.next();
            int state = mAppManager.getStatus(app).statusCode;
            if(state == AppManager.STATUS_INSTALLED){
                iter.remove();
                isDel = true;
            }
            if(!isDel){
            	if(PackageUtils.isAppInstalled(this, app.getStrPackageName()))
                    iter.remove();
            }
        }
    }

    class MustInstallAdapter extends BaseListAdapter {
    	private List<App> mSelected = new ArrayList<App>();
    	
    	public MustInstallAdapter(Context context, ListView listView,
    			ArrayList<SectionListItem> items, int currIndex) {
    		super(context, listView, items, currIndex);
    	}

    	@Override
    	protected int getLayoutBySection(int section) {
    		return MResource.getIdByName(mContext,"layout","nq_mustinstall_list_item4");
    	}

    	@Override
    	protected int getResourceNumPerRow(int section) {
    		return 4;
    	}

    	@Override
    	protected void setData(View view, int layoutType, ResItem resItem,
    			int index, int pos) {
    		final App app = (App) resItem;
    		if(app != null){
                view.setVisibility(View.VISIBLE);
    			AsyncImageView icon = (AsyncImageView) view.findViewById(MResource.getIdByName(mContext, "id", "icon"));
    			String url = app.getStrIconUrl();
    			icon.loadImage(url,null,MResource.getIdByName(mContext,"drawable","nq_icon_default"));
    			TextView name = (TextView) view.findViewById(MResource.getIdByName(mContext, "id", "name"));
    			name.setText(app.getStrName());
    			final ImageView checkItem = (ImageView) view.findViewById(MResource.getIdByName(mContext,"id","checkState"));
				doCheckItem(app, checkItem);
    			DownloadStateView downloadStateView = (DownloadStateView) view.findViewById(MResource.getIdByName(mContext,"id","download_layout"));
    			downloadStateView.setResItem(app, DownloadStateView.TYPE_APP, app.mStatus != AppManager.STATUS_DOWNLOADED ? mHandler : null, checkItem);
                view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
                        if(!mGpResource){
                            int status = mAppManager.getStatus(app).statusCode;
                            if(status != AppManager.STATUS_DOWNLOADING){
                                if (mSelected.contains(app)) {
                                    mSelected.remove(app);
                                    mSelectAll = false;
                                    mCheckAll.setImageResource(MResource.getIdByName(MustInstallActivity.this, "drawable", "nq_select_off"));
                                } else {
                                    mSelected.add(app);
                                    if (mSelected.size() == mAlldata.size()) {
                                        mSelectAll = true;
                                        mCheckAll.setImageResource(MResource.getIdByName(MustInstallActivity.this, "drawable", "nq_select_on"));
                                    }
                                }

                                doCheckItem(app, checkItem);
                                doInstallButton(true);
                                NqLog.d("check:" + mSelected.size());
                            }
                        }else{
                            GpUtils.viewDetail(MustInstallActivity.this,app.getStrAppUrl());
                        }
					}
				});
    			
                StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                        MustInstallActionConstants.ACTION_LOG_1806, app.getStrId(), 0, null);
            }
    	}
    	
    	public void doCheckItem(final App app, final ImageView check) {
    		boolean checked = mSelected.contains(app);
    		NqLog.d("checked:" + checked + ", gname:" + app.getGroupName() + ", " + app);
            if(mGpResource){
                check.setVisibility(View.GONE);
            }else{
                if(checked){
                    check.setVisibility(View.VISIBLE);
                    check.setImageResource(MResource.getIdByName(MustInstallActivity.this,"drawable","nq_item_selected"));
                }else{
                    check.setVisibility(View.VISIBLE);
                    check.setImageResource(MResource.getIdByName(MustInstallActivity.this,"drawable","nq_item_not_selected"));
                }
            }

    	}
    	
        public void selectAll(){
            mSelected.removeAll(mSelected);
            for(ResItem item: mAlldata){
                mSelected.add((App)item);
            }
            notifyDataSetChanged();
        }

        public void deselectAll(){
            mSelected.removeAll(mSelected);
            notifyDataSetChanged();
        }

        public List<App> getSelected(){
            return mSelected;
        }
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if(isNeedInstallHandler)
    		handerApps(mApps);
    	isNeedInstallHandler = false;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mWaitingDialog != null && mWaitingDialog.isShowing()){
            mWaitingDialog.dismiss();
        }
        unregisterReceiver(mAllAppDownFinishReceiver);
    }

    public void registDownFinishReceiver() {
		IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REFRESH);
        registerReceiver(mAllAppDownFinishReceiver, filter);
	}

    BroadcastReceiver mAllAppDownFinishReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (mHandler != null)
                mHandler.sendEmptyMessage(ADAPTER_ALL_REFRESH);
        }
    };
    
    boolean isNeedInstallHandler = false;
    Handler mHandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    		super.handleMessage(msg);
    		if (msg.what == ADAPTER_ALL_REFRESH) {
    			isNeedInstallHandler = true;
    			handerApps(mApps);
			}
    	}
    };

	@Override
	public void onClick(View v) {
		if(v == mGetMore){
			 doGetMoreClick();
		}else if(v == mCheckAll){
			doCheckAllClick();
		} else if(v == mInstallAll){
			doInstallClick();
		}else if(v == mEmptyLayout){
            loadData();
		}
	}

	public void doGetMoreClick() {
        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                MustInstallActionConstants.ACTION_LOG_1808, null, 0, null);
		 LauncherSDK.getInstance(MustInstallActivity.this).gotoStore(0);
		 finish();
	}

	public void doCheckAllClick() {
		if(!mSelectAll){
		    mSelectAll = !mSelectAll;
		    mCheckAll.setImageResource(MResource.getIdByName(MustInstallActivity.this,"drawable","nq_select_on"));
		    mAdapter.selectAll();
		}else{
		    mSelectAll = !mSelectAll;
		    mCheckAll.setImageResource(MResource.getIdByName(MustInstallActivity.this,"drawable","nq_select_off"));
		    mAdapter.deselectAll();
		}
	}

	public void doInstallClick() {
		if(mAdapter.getSelected().size() == 0){
		    Toast.makeText(MustInstallActivity.this,
		            MustInstallActivity.this.getString(MResource.getIdByName(MustInstallActivity.this,"string","nq_select_one")),
		            Toast.LENGTH_SHORT).show();
		    return;
		}

        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                MustInstallActionConstants.ACTION_LOG_1804, null, 0, mAlldata.size() + "_" + mAdapter.getSelected().size());

		if(NetworkUtils.isConnected(MustInstallActivity.this)){
		    if(NetworkUtils.isWifi(MustInstallActivity.this)){
		        downloadAll(DownloadManager.Request.NETWORK_WIFI);
		    }else{
		        AlertDialog.Builder builder = new AlertDialog.Builder(MustInstallActivity.this);
		        builder.setMessage(MResource.getIdByName(MustInstallActivity.this,"string","nq_download_with_gprs"));
		        builder.setTitle(MResource.getIdByName(MustInstallActivity.this,"string","nq_label_mustinstall"));
		        builder.setPositiveButton(MResource.getIdByName(MustInstallActivity.this,"string","nq_text_ok"),new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface dialogInterface, int i) {
		                downloadAll(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
		            }
		        });
		        builder.setNegativeButton(MResource.getIdByName(MustInstallActivity.this,"string","nq_label_cancel"), new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface dialog, int which) {
		                dialog.dismiss();
		            }
		        });
		        builder.create().show();
		    }
		}else{
			ToastUtils.toast(MustInstallActivity.this, "nq_nonetwork");
		}
	}
}
