package com.nqmobile.livesdk.modules.installedrecommend;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.nq.interfaces.launcher.TAppResource;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.installedrecommend.network.GetInstalledRecommendProtocal.GetInstalledRecommendSuccessEvent;
import com.nqmobile.livesdk.modules.installedrecommend.network.GetInstalledRecommendServiceFactory;

public class InstalledRecommendManager extends AbsManager {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(InstalledRecommendModule.MODULE_NAME);
	// ===========================================================
	// Fields
	// ===========================================================
	private Context mContext;  
	private InstalledRecommendPreference mHelper;
    private NewPackageReceiver mReceiver;
	private static InstalledRecommendManager mInstance;
	
	// ===========================================================
	// Constructors
	// ===========================================================
    public InstalledRecommendManager(Context context) {
        super();
        mContext = context;
        mHelper = InstalledRecommendPreference.getInstance();   
		
        //注册监听安装事件
        mReceiver = new NewPackageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        mContext.registerReceiver(mReceiver,filter);

    }

    public synchronized static InstalledRecommendManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new InstalledRecommendManager(context.getApplicationContext());
        }
        return mInstance;
    }
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void init() {
		// TODO Auto-generated method stub
		EventBus.getDefault().register(this);
	}
	
	public void onEvent(GetInstalledRecommendSuccessEvent event) {
		NqLog.i("onEvent(GetInstalledRecommendSuccessEvent coming....");
		List<TAppResource> resp = event.getAppResource();
		InstalledAssociationListener listener = (InstalledAssociationListener)event.getTag();

		if (resp != null) {	
			listener.onGetInstalledAssociationSucc(resp);
		}			
	}
	
	// ===========================================================
	// Methods
	// ===========================================================
	public void getInstalledRecommendInfoFromServer(String packageName, InstalledAssociationListener listener) {
		GetInstalledRecommendServiceFactory.getService().getInstalledAssociationApp(packageName, listener);
	}
		
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
    private class NewPackageReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getDataString().substring(8);
            NqLog.i("InstalledRecommendManager-->installed packagename: " + packageName);
            
            // 检查应用是否是升级安装/替换安装/软件搬家
            boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            
            boolean enabled = mHelper.getBooleanValue(InstalledRecommendPreference.KEY_INSTALLED_RECOMMEND_ENABLE);
    		if (enabled && !replacing) {
		        Intent i = new Intent(context, InstalledRecommendActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP
						);
				i.setAction(InstalledRecommendActivity.INTENT_ACTION);
		        i.putExtra(InstalledRecommendActivity.PACKAGE_NAME, packageName);
		        context.startActivity(i);
    		}
        }
    }
    
    /**
     * 安装后关联推荐获取监听
     * @author liujiancheng
     * @time 2014-9-16
     */
    public interface InstalledAssociationListener extends Listener{
        public void onGetInstalledAssociationSucc(List<TAppResource> resp);
    }    
}
