package com.nqmobile.livesdk.modules.storeentry;

import android.content.Context;
import android.content.Intent;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.ui.StoreMainActivity;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.MResource;

/**
 * 
 * @ClassName: StoreEntry 
 * @Description: 创建store入口 
 * @author handy
 * @date 2014-11-21   
 *
 */
public class StoreEntry {
	private static final ILogger NqLog = LoggerFactory.getLogger(StoreEntryModule.MODULE_NAME);

	private static StoreEntry mInstance;

	private Context mContext;
    public static final int FROM_SHORTCUT = 1002;
    public static final int FROM_CATEGORY_FOLDER = 1003;

	private StoreEntry(Context context) {
		mContext = context;
	}

	public synchronized static StoreEntry getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new StoreEntry(context);
		}

		return mInstance;
	}

	/**
	 * 
	 * @Title: createStoreEntry 
	 * @Description: 创建store入口
	 * @return void    
	 * @throws
	 */
	public void createStoreEntry() {		
		NqLog.v("createStoreEntry");
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_ENTRY_SHORTCUT");
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,MResource.getString(mContext, "nq_label_store"));
		shortcut.putExtra("duplicate", false); // 不允许重复创建
        shortcut.putExtra("mustCreate",true);
		Intent i = new Intent();
		i.setAction("com.lqlauncher.LocalTheme");
		i.putExtra("shortcutIcon", MResource.getIdByName(mContext,"drawable", "ic_launcher_localtheme"));
		i.putExtra(Intent.EXTRA_SHORTCUT_NAME,MResource.getString(mContext, "nq_label_store"));
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
		Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource
				.fromContext(mContext, MResource.getIdByName(mContext,"drawable", "ic_launcher_localtheme"));
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
		mContext.sendBroadcast(shortcut);
	}

    public void createHotAppEntry() {
        NqLog.v("createHotAppEntry");
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_ENTRY_SHORTCUT");
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,MResource.getString(mContext, "nq_hot_app_label"));
        shortcut.putExtra("duplicate", false); // 不允许重复创建
        Intent i = new Intent(mContext,StoreMainActivity.class);
        i.putExtra(StoreMainActivity.KEY_FRAGMENT_INDEX_TO_SHOW,0);
        i.putExtra(StoreMainActivity.KEY_FROM,FROM_SHORTCUT);
		i.putExtra("shortcutIcon", MResource.getIdByName(mContext,"drawable", "nq_hot_app_icon"));
		i.putExtra(Intent.EXTRA_SHORTCUT_NAME,MResource.getString(mContext, "nq_hot_app_label"));
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
        Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource
                .fromContext(mContext, MResource.getIdByName(mContext,"drawable", "nq_hot_app_icon"));
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
        mContext.sendBroadcast(shortcut);

        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,"1110","",0,"");
    }

}
