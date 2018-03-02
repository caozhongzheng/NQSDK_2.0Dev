package com.nqmobile.livesdk.modules.installedrecommend;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nq.interfaces.launcher.TAppResource;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.AsyncImageView;
import com.nqmobile.livesdk.commons.ui.BaseActvity;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.font.FontManager;
import com.nqmobile.livesdk.modules.installedrecommend.InstalledRecommendManager.InstalledAssociationListener;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.GpUtils;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;

public class InstalledRecommendActivity extends BaseActvity implements OnClickListener {
	private static final ILogger NqLog = LoggerFactory.getLogger(InstalledRecommendModule.MODULE_NAME);
	
	private Context mContext;
	private InstalledRecommendPreference mPreference = InstalledRecommendPreference.getInstance();
	
	private String mInstalledPackageName;
	public static final String INTENT_ACTION = "com.nqmobile.live.installedRecommend";
	public static final String PACKAGE_NAME = "package_name";
	public static final int RANDOM_MIN = 100;
	public static final int RANDOM_MAX = 2000;
	public static final int SHOW_ICON_NUM = 2;
	
	private String mInstalledName;
	private Drawable mInstalledIcon;
	private PackageManager mPkgManager;
	private List<App> mAppList;
	
	private ImageView mInstalledIconView;
	private TextView mInstalledNameView;
	private TextView mInstallTitle;
	private TextView mInstallDetails;
	private View mItem0;
	private View mItem1;
//	private View mItem2;
//	private View mItem3;
	private Button mInstallComplete;
	private Button mInstallOpen;
	private LinearLayout mLineOne;
	private LinearLayout mLineTwo;
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		
        if (INTENT_ACTION.equals(intent.getAction())) {
			mInstalledPackageName = intent.getStringExtra(PACKAGE_NAME);
			NqLog.i("onNewIntent: mInstalledPackageName="+mInstalledPackageName);
	        //开启任务获取安装后关联推荐app信息
			initTask();
	        mAppList.clear();
        }        
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mContext = InstalledRecommendActivity.this;
		mPkgManager = mContext.getPackageManager();
		mAppList = new ArrayList<App>();
		
        if (INTENT_ACTION.equals(getIntent().getAction())) {
			mInstalledPackageName = getIntent().getStringExtra(PACKAGE_NAME);
			NqLog.i("OnCreate: mInstalledPackageName="+mInstalledPackageName);
			initTask();
        }
        
        setContentView(MResource.getIdByName(this, "layout", "nq_installed_recommend"));           
        findViews();
	}

	@Override  
	protected void onStart() {  
	    super.onStart();  // Always call the superclass method first  
	}  
	
    
    @Override
    protected void onResume() {
    	super.onResume();
		NqLog.i("onResume is coming..");
		updateview();
    }
    
    private void updateview() {
        if (mContext.getPackageManager().getLaunchIntentForPackage(mInstalledPackageName) == null) {
        	mInstallOpen.setVisibility(View.GONE);
        }
        //获取安装信息的icon和应用名称
        loadInstalledInfo();    
    	setRecommendAppInfo();
    }
    
	private void initTask() {
		String packageName = mPreference.getStringValue(InstalledRecommendPreference.KEY_LAST_INSTALLED_RECOMMEND_PACKAGE);
		long lastRequestTime = mPreference.getLongValue(InstalledRecommendPreference.KEY_LAST_INSTALLED_RECOMMEND_TIME);
	
		//　有时候会发现initTask方法会在几十毫秒内被调用两次，onCreate()和onNewIntent()各走一次，在这里防御一下
		long current = System.currentTimeMillis();
		if (packageName.equals(mInstalledPackageName) && (current - lastRequestTime) < 3*1000) {
	        NqLog.i("ljc1234:initTask() is called twice!!.........");
			return;
		}
		mPreference.setStringValue(InstalledRecommendPreference.KEY_LAST_INSTALLED_RECOMMEND_PACKAGE, mInstalledPackageName);
		mPreference.setLongValue(InstalledRecommendPreference.KEY_LAST_INSTALLED_RECOMMEND_TIME, current);
		
        //开启任务获取安装后关联推荐app信息
        LoadRecommendInfoTask task = new LoadRecommendInfoTask();
        task.execute(null,null,null);
        
        //安装界面成功弹出，记录行为日志
        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
        		InstalledRecommendConstants.ACTION_LOG_2801, null, 0, null);
	}
	
	private void loadInstalledInfo() {
        getInstalledPackageInfo();

        mInstalledIconView.setImageDrawable(mInstalledIcon);
        mInstalledNameView.setText(mInstalledName);
	}
	
	private void getInstalledPackageInfo() {
        try {  
    		PackageInfo packageInfo = mPkgManager.getPackageInfo(mInstalledPackageName,0); 
    		mInstalledName = packageInfo.applicationInfo.loadLabel(mPkgManager).toString();
    		mInstalledIcon = packageInfo.applicationInfo.loadIcon(mPkgManager);
        } catch (NameNotFoundException e) {  
            e.printStackTrace();
        }	

	}
	
	private void findViews() {
		// TODO Auto-generated method stub        
		mInstalledIconView = (ImageView)findViewById(MResource.getIdByName(this, "id", "installed_icon"));
        mInstalledNameView = (TextView)findViewById(MResource.getIdByName(this, "id", "installed_name"));

        mItem0 = findViewById(MResource.getIdByName(this, "id", "itemOne"));
        mItem1 = findViewById(MResource.getIdByName(this, "id", "itemTwo"));
//        mItem2 = findViewById(MResource.getIdByName(this, "id", "itemThree"));
//        mItem3 = findViewById(MResource.getIdByName(this, "id", "itemFour"));

        mLineOne = (LinearLayout)findViewById(MResource.getIdByName(this, "id", "lineOne"));
        mLineTwo = (LinearLayout)findViewById(MResource.getIdByName(this, "id", "lineTwo"));	
        
        mInstallComplete = (Button)findViewById(MResource.getIdByName(this, "id", "install_complete"));
        mInstallOpen = (Button)findViewById(MResource.getIdByName(this, "id", "install_open"));
        mInstallTitle = (TextView)findViewById(MResource.getIdByName(this, "id", "recommend_install"));
        int number = (int)(Math.random() *(RANDOM_MAX-RANDOM_MIN) + RANDOM_MIN);
        NqLog.i("number = " + number);
        mInstallTitle.setText(MResource.getString(getApplication(), "nq_recommend_install", number));
        setAllIconGone();
        
        mInstallComplete.setOnClickListener(this);
        mInstallOpen.setOnClickListener(this);
	}
	
	private void setAllIconGone() {
        mItem0.setVisibility(View.GONE);
        mItem1.setVisibility(View.GONE);
//        mItem2.setVisibility(View.GONE);
//        mItem3.setVisibility(View.GONE);
        mInstallTitle.setVisibility(View.GONE);
        mLineOne.setVisibility(View.GONE);
        mLineTwo.setVisibility(View.GONE);        
	}
	
	private void setViewVisible() {
        mInstallTitle.setVisibility(View.VISIBLE);
        mLineOne.setVisibility(View.VISIBLE);
        mLineTwo.setVisibility(View.VISIBLE); 
	}
	
	private void getInstalledRecommendInfo() {
		if (NetworkUtils.isWifi(mContext) == false) {
			return;
		}
		
		InstalledRecommendManager.getInstance(mContext).getInstalledRecommendInfoFromServer(mInstalledPackageName, 
				new InstalledAssociationListener() {
					
			@Override
			public void onErr() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onGetInstalledAssociationSucc(List<TAppResource> resp) {
				// TODO Auto-generated method stub
				if (resp != null) {
					NqLog.i("getRecommendAppList.size = " + resp.size());
					
			        mAppList.clear();
	                for (TAppResource tApp : resp) {
	        	        //推荐资源展示，记录行为日志
	        	        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
	        	        		InstalledRecommendConstants.ACTION_LOG_2802, tApp.getResourceId(), 0, tApp.getPackageName());
	        	        
						App app = new App(tApp, mContext);
						if (app != null) {
							mAppList.add(app);
							if (mAppList.size() >= SHOW_ICON_NUM) {
								break;
							}
						}						
	                }
	                
	                runOnUiThread(new Runnable() {
	                    @Override
	                    public void run() {
	                    	setRecommendAppInfo();
	                    }
	                });
				}
			}					
		});
	}

	private void setRecommendAppInfo() {
		setAllIconGone();	
		
		int size = mAppList.size();
		if (size == 0) {
			return;
		}
		
		setViewVisible();
		
    	setData(mItem0, mAppList.get(0));
        if (mAppList.size() > 1) {
        	setData(mItem1, mAppList.get(1));
        } else {
        	setData(mItem1, null);
        }
        
//        if (mAppList.size() > 2) {
//        	setData(mItem2, mAppList.get(2));
//        } else {
//        	setData(mItem2, null);
//        }
//        
//        if (mAppList.size() > 3) {
//        	setData(mItem3, mAppList.get(3));
//        } else {
//        	setData(mItem3, null);
//        }
	}
	
	private void setData(View view, App app) {
		LinearLayout layoutView = (LinearLayout)view;
		AsyncImageView icon;
		TextView name;
		TextView details;
				
		view.setVisibility(View.VISIBLE);
		icon = (AsyncImageView) layoutView.findViewById(MResource.getIdByName(mContext, "id", "icon"));
		name = (TextView) layoutView.findViewById(MResource.getIdByName(mContext, "id", "name"));
		details = (TextView) layoutView.findViewById(MResource.getIdByName(mContext, "id", "details"));
		if (app == null) {
			icon.setImageDrawable(null);
			name.setText(null);
			details.setText(null);
			return;
		} else {
			icon.loadImage(app.getStrIconUrl(),null,MResource.getIdByName(mContext,"drawable","nq_icon_default"));
			name.setText(app.getStrName());
			details.setText(MResource.getString(mContext, "nq_install_details"));
		}		

		final App clickApp = app;
		layoutView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		        //推荐资源icon点击，记录行为日志
		        StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
		        		InstalledRecommendConstants.ACTION_LOG_2804, clickApp.getStrId(), 1, clickApp.getStrPackageName());
		        
		    	int actionType = clickApp.getIntDownloadActionType();		    	
		    	if (actionType == InstalledRecommendConstants.GP_DOWNLOAD_TYPE
		    			|| actionType == InstalledRecommendConstants.GP_OPEN_TYPE) {
		    		GpUtils.viewDetail(mContext, clickApp.getStrAppUrl());
		    	} else {
		    		// 非GP资源下载，弹出应用详情介绍
		    		showAppDetail(clickApp);
		    	}   	
			}
			
		});
	}
	
	private void showAppDetail(App app) {
		if (app == null)
			return;
		Intent intent = new Intent(InstalledRecommendActivity.this, InstalledRecommendDetailActivity.class);
		intent.setAction(InstalledRecommendDetailActivity.INTENT_ACTION);
		intent.putExtra(InstalledRecommendDetailActivity.KEY_APP, app);
		intent.putExtra(InstalledRecommendDetailActivity.KEY_APP_ID, app.getStrId());
		InstalledRecommendActivity.this.startActivityForResult(intent, 3);
	}
	
    @Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  
    {  
//        super.onActivityResult(requestCode, resultCode, data);  
    	String packageName;
    	if (resultCode == RESULT_OK) {
	        if (data != null) {
	        	packageName = data.getStringExtra(InstalledRecommendDetailActivity.KEY_RESULT);
	        	NqLog.i("data =" + data.getStringExtra("packagename"));
	        	
	        	for (App app: mAppList) {
	        		if (packageName.equals(app.getStrPackageName())) {
	        			mAppList.remove(app);
	        			break;
	        		}
	        	}
	        }
	        
    	}
    }  
    
	private class LoadRecommendInfoTask extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... objects) {    
			getInstalledRecommendInfo();

//			//找到系统installer，kill其进程
//			String sys_packagename;
//			Intent i = new Intent(Intent.ACTION_VIEW);
//			i.setDataAndType(Uri.fromFile(new File("abc.apk")), "application/vnd.android.package-archive");
//			List<ResolveInfo> list = mPkgManager.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
//			if (list != null && list.size() > 0){
//				sys_packagename = list.get(0).activityInfo.packageName;
//				NqLog.i("ljctest","system installer package is:" + sys_packagename);
////				ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
////				am.killBackgroundProcesses(sys_packagename);
//				
//		        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);  
//		        // 获得系统里所有正在运行的进程  
//		        List<ActivityManager.RunningAppProcessInfo> appProcessList = mActivityManager  
//		                .getRunningAppProcesses();  
//				ComponentName cn = mActivityManager.getRunningTasks(1).get(0).topActivity;
//				if (sys_packagename.equals(cn.getPackageName())){
//
//				}
//		        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {  
//		            int pid = appProcess.pid;
//		            String processName = appProcess.processName; // 进程名  		  
//		            String[] pkgNameList = appProcess.pkgList; // 获得运行在该进程里的所有应用程序包  
//		  
//		            // 输出所有应用程序的包名  
//		            for (int j = 0; j < pkgNameList.length; j++) {  
//		            	if (sys_packagename.equals(pkgNameList[j])) {
//		            		NqLog.i("ljctest", "mactched! pid = " + pid + " process name = " + processName);	
//		            		android.os.Process.killProcess(pid);
//		            		break;
//		            	}
//		            }  
//		        } 
//			}			

			return null;
		}
	}
    
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mInstallComplete) {
	        //完成按钮点击，记录行为日志
	        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
	        		InstalledRecommendConstants.ACTION_LOG_2805, null, 1, mInstalledPackageName);
			this.finish();
		} else if (v == mInstallOpen) {
	        //打开按钮点击，记录行为日志
	        StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
	        		InstalledRecommendConstants.ACTION_LOG_2806, null, 1, mInstalledPackageName);
	        
			this.finish();
			
	        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(    
	                mInstalledPackageName);   
	        if (intent != null) {
		        startActivity(intent);
	        }
		}
	}

}
