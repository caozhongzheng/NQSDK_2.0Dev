package com.nqmobile.livesdk.modules.stat.network;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TPackageAction;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.stat.StatModule;
import com.nqmobile.livesdk.modules.stat.StatPreference;

/**
 * Created by Rainbow on 14-2-18.
 */
public class UploadPackageActionProtocol extends AbsLauncherProtocol{
	private static final ILogger NqLog = LoggerFactory.getLogger(StatModule.MODULE_NAME);

    @Override
    protected int getProtocolId() {
        return 0x16;
    }

    @Override
    protected void process() {
        NqLog.i("UploadPackageActionProtocol process!");
        try {
            TLauncherService.Iface client = TLauncherServiceClientFactory.getClient(getThriftProtocol());
            client.uploadPackageAction(getUserInfo(),getAllPackage());
            StatPreference.getInstance().setUploadPackageFinish(true);
            NqLog.i("UploadPackageActionProtocol success!");
        }catch(Exception e) {
            e.printStackTrace();onError() ;
        }
    }
    @Override
	protected void onError() {
	}
    private TPackageAction getAllPackage(){
        // 设置可以被启动的条件
        Intent it = new Intent(Intent.ACTION_MAIN);
        it.addCategory(Intent.CATEGORY_LAUNCHER);

        ArrayList<String> appLauncheredList = new ArrayList<String>();
        List<ResolveInfo> app = ApplicationContext.getContext().getPackageManager().queryIntentActivities(it,0);
        for(int i = 0; i < app.size(); i++){
            ActivityInfo ai = app.get(i).activityInfo;
            appLauncheredList.add(ai.applicationInfo.packageName);
        }

        TPackageAction mPackageAction = new TPackageAction();
        mPackageAction.actionType = 0;
        mPackageAction.setInstalls(appLauncheredList);
        return mPackageAction;
    }

	
}
