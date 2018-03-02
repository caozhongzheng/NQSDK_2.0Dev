package com.nqmobile.livesdk.modules.mustinstall;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.text.format.DateUtils;

import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.image.ImageListener;
import com.nqmobile.livesdk.commons.image.ImageManager;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.receiver.LauncherResumeEvent;
import com.nqmobile.livesdk.commons.receiver.PackageAddedEvent;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.mustinstall.network.GetAppListProtocol;
import com.nqmobile.livesdk.modules.mustinstall.network.MustInstallServiceFactory;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.NotificationUtil;

/**
 * Created by Rainbow on 2014/11/19.
 */
public class MustInstallManager extends AbsManager {

	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(MustInstallModule.MODULE_NAME);

	// ===========================================================
	// Fields
	// ===========================================================

	private Context context;

	private static MustInstallManager mInstance;

	// ===========================================================
	// Constructors
	// ===========================================================

	private MustInstallManager(Context context) {
		this.context = context;
		EventBus.getDefault().register(this);
	}

	public synchronized static MustInstallManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new MustInstallManager(context.getApplicationContext());
		}
		return mInstance;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public void getAppList(AppListListener listener) {
        NqLog.i("MustInstallManager getAppList");
		MustInstallServiceFactory.getService().getAppList(listener);
	}

	public void onEvent(GetAppListProtocol.GetAppListSuccessEvent event) {
        NqLog.i("onEvent GetAppListSuccessEvent list.size="+event.list.size());
		((AppListListener) event.getTag()).onGetAppListSucc(event.list);
	}

	public void onEvent(GetAppListProtocol.GetAppListFailEvent event) {
        NqLog.i("onEvent GetAppListFailEvent");
		((AppListListener) event.getTag()).onErr();
	}

	public void onEvent(PackageAddedEvent event) {
        checkShowMustInstall(false);

		Intent i = new Intent();
		i.setAction(MustInstallActivity.ACTION_REFRESH);
		context.sendBroadcast(i);
	}

    public void onEvent(LauncherResumeEvent event){
        checkShowMustInstall(true);
    }

    private void createShortCut(){
        boolean created = MustInstallPreference.getInstance().getBooleanValue(MustInstallPreference.KEY_MUSI_INSTALL_ICON_CREATE);
        NqLog.i("MustInstallManager IconCreated=" + created);
        if(!created){
            Intent shortcut = new Intent("com.android.launcher.action.INSTALL_ENTRY_SHORTCUT");
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(MResource.getIdByName(context, "string", "nq_label_mustinstall")));
            shortcut.putExtra("duplicate", false); // 不允许重复创建
            Intent i = new Intent();
            i.setAction(Intent.ACTION_MAIN);
            i.setClassName(context.getPackageName(), MustInstallActivity.class.getName());
            i.putExtra("from", 2);
            i.putExtra("shortcutIcon", MResource.getIdByName(context,"drawable", "nq_mustinstall_icon"));
            i.putExtra(Intent.EXTRA_SHORTCUT_NAME,MResource.getString(context, "nq_label_mustinstall"));
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
            Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(context,
                    MResource.getIdByName(context,"drawable","nq_mustinstall_icon"));
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
            context.sendBroadcast(shortcut);
            MustInstallPreference.getInstance().setBooleanValue(MustInstallPreference.KEY_MUSI_INSTALL_ICON_CREATE, true);

            StatManager.getInstance().
                    onAction(StatManager.TYPE_STORE_ACTION, MustInstallActionConstants.ACTION_LOG_1803, null, 0, null);
        }
    }

    private void checkShowMustInstall(boolean fromOnResume) {
        MustInstallPreference helper = MustInstallPreference.getInstance();
        boolean mustInstallEnable = helper.getBooleanValue(MustInstallPreference.KEY_MUST_INSTALL_ENABLE);
        NqLog.i("checkShowMustInstall fromOnResume="+fromOnResume+" mustInstallEnable="+mustInstallEnable);
        if (mustInstallEnable && NetworkUtils.isWifi(context)) {
            String key = fromOnResume ? MustInstallPreference.KEY_MUST_INSTALL_TIP_SHOW
                    : MustInstallPreference.KEY_MUST_INSTALL_TIP_INSTALLAPP_SHOW;
            boolean show = helper.getBooleanValue(key);
            long lastTime = helper.getLongValue(MustInstallPreference.KEY_MUST_INSTALL_PUSH_TIME);
            int showCount = helper.getIntValue(MustInstallPreference.KEY_MUST_INSTALL_PUSH_COUNT);
            NqLog.i("handy showCount = "+showCount+ " true ? false = "+((System.currentTimeMillis() - lastTime) > DateUtils.DAY_IN_MILLIS * (showCount<3?14:30)));
            if (!show || (System.currentTimeMillis() - lastTime) > DateUtils.DAY_IN_MILLIS * (showCount<3?14:30)) {
                Intent i = new Intent(context, MustInstallActivity.class);
                i.putExtra("from", MustInstallActivity.FROM_NOTI);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                NotificationUtil.showNoti(context, MResource.getIdByName(
                                context, "drawable", "nq_mustinstall_icon"),
                        MResource.getIdByName(context, "string", "nq_label_mustinstall"),
                        MResource.getIdByName(context, "string", "nq_mustinstall_tip"), i,
                        NotificationUtil.NOTIF_ID_MUSTINSTALL);

                helper.setBooleanValue(key, true);
                helper.setLongValue(MustInstallPreference.KEY_MUST_INSTALL_PUSH_TIME,System.currentTimeMillis());
                helper.setIntValue(MustInstallPreference.KEY_MUST_INSTALL_PUSH_COUNT,showCount++);

                StatManager.getInstance().onAction(
                        StatManager.TYPE_STORE_ACTION,
                        MustInstallActionConstants.ACTION_LOG_1801, null,
                        0, fromOnResume ? "0" : "1");
            }
        }
    }

    private void preloadIcon(){
        getAppList(new AppListListener() {
            @Override
            public void onGetAppListSucc(List<App> apps) {
                NqLog.i("init onGetAppListSucc apps.size="+apps.size());
                for(App app :apps){
                    ImageManager.getInstance(context).loadImage(app.getStrIconUrl(),new ImageListener() {
                        @Override
                        public void getImageSucc(String url, BitmapDrawable drawable) {
                            NqLog.i("getImageSucc url="+url);
                        }

                        @Override
                        public void onErr() {

                        }
                    });
                }
            }

            @Override
            public void onErr() {

            }
        });
    }

	@Override
	public void init() {
        NqLog.i("MustInstallManager init!");
        EventBus.getDefault().register(this);

        if(MustInstallPreference.getInstance().getBooleanValue(MustInstallPreference.KEY_MUST_INSTALL_ENABLE)){
            createShortCut();
            preloadIcon();
        }
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
