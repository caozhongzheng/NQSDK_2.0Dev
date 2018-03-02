package com.nqmobile.livesdk.commons.init;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.db.DataProvider;
import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.commons.info.ProcessInfo;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.commons.mydownloadmanager.MyDownloadModule;
import com.nqmobile.livesdk.commons.preference.CommonsPreference;
import com.nqmobile.livesdk.commons.preference.PreferenceServiceFactory;
import com.nqmobile.livesdk.commons.prefetch.PrefetchModule;
import com.nqmobile.livesdk.commons.receiver.LiveReceiver;
import com.nqmobile.livesdk.commons.service.BackgroundService;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.modules.activation.ActiveModule;
import com.nqmobile.livesdk.modules.activation.ActivePreference;
import com.nqmobile.livesdk.modules.app.AppModule;
import com.nqmobile.livesdk.modules.appactive.AppActiveModule;
import com.nqmobile.livesdk.modules.apprate.AppRateModule;
import com.nqmobile.livesdk.modules.appstub.AppStubModule;
import com.nqmobile.livesdk.modules.appstubfolder.AppStubFolderModule;
import com.nqmobile.livesdk.modules.apptype.AppTypeModule;
import com.nqmobile.livesdk.modules.association.AssociationModule;
import com.nqmobile.livesdk.modules.banner.BannerModule;
import com.nqmobile.livesdk.modules.batterypush.BatteryPushModule;
import com.nqmobile.livesdk.modules.browserbandge.BrowserBandgeModule;
import com.nqmobile.livesdk.modules.categoryfolder.CategoryFolderModule;
import com.nqmobile.livesdk.modules.daily.DailyModule;
import com.nqmobile.livesdk.modules.defaultlauncher.DefaultLauncherModule;
import com.nqmobile.livesdk.modules.feedback.FeedbackModule;
import com.nqmobile.livesdk.modules.font.FontModule;
import com.nqmobile.livesdk.modules.gamefolder_v2.GameFolderV2Module;
import com.nqmobile.livesdk.modules.incrementupdate.IncrementUpdateModule;
import com.nqmobile.livesdk.modules.installedrecommend.InstalledRecommendModule;
import com.nqmobile.livesdk.modules.lqwidget.LqWidgetModule;
import com.nqmobile.livesdk.modules.mustinstall.MustInstallModule;
import com.nqmobile.livesdk.modules.points.PointModule;
import com.nqmobile.livesdk.modules.push.PushModule;
import com.nqmobile.livesdk.modules.regularupdate.RegularUpdateModule;
import com.nqmobile.livesdk.modules.search.SearcherModule;
import com.nqmobile.livesdk.modules.stat.StatModule;
import com.nqmobile.livesdk.modules.storeentry.StoreEntryModule;
import com.nqmobile.livesdk.modules.theme.ThemeModule;
import com.nqmobile.livesdk.modules.update.UpdateModule;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperModule;
import com.nqmobile.livesdk.modules.weather.WeatherModule;
import com.nqmobile.livesdk.utils.CollectionUtils;
import com.nqmobile.livesdk.utils.FileUtil;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class InitManager {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger("init");
	
    public static final String TRUSTSTORE_FILE_NAME = "truststore.bks";
    public static final String TABLE_VERSION_PREFIX = "table_version_";
	// ===========================================================
	// Fields
	// ===========================================================
	private ModuleContainer mModules;
	private InitPreference mPreference;
    private Map<IModule, Boolean> mModuleDefaults;
	private Context mContext;

	// ===========================================================
	// Constructors
	// ===========================================================
	public InitManager(Context context) {
		mContext = context;
		ApplicationContext.setContext(context);
		mPreference = InitPreference.getInstance();
		mModules = ModuleContainer.getInstance();	
		mModuleDefaults = new LinkedHashMap<IModule, Boolean>();
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
    public void registerModules() {
        // 前几个为StoreMainActivity 下的 Tab，顺序固定为应用 主题 壁纸
        addModule(new AppModule(), true);
        addModule(new ThemeModule(), true);
        addModule(new WallpaperModule(), true);
        // mModules.addModule(new LockerModule(), true);

        addModule(new GameFolderV2Module());
        addModule(new WeatherModule(), true);
        addModule(new PointModule());
        addModule(new MustInstallModule());
        addModule(new ActiveModule(), true);
        addModule(new UpdateModule());
        addModule(new RegularUpdateModule(), true);

        addModule(new BannerModule(), true);
        addModule(new DailyModule());
        addModule(new AssociationModule());
        addModule(new IncrementUpdateModule(), true);
        addModule(new PushModule());
        addModule(new AppStubModule());
        addModule(new AppStubFolderModule());
        addModule(new BrowserBandgeModule());
        addModule(new MyDownloadModule(), true);
        addModule(new FeedbackModule(), true);
        addModule(new StatModule(), true);
        addModule(new StoreEntryModule());
        addModule(new AppTypeModule(), true);
        addModule(new InstalledRecommendModule());
        addModule(new AppActiveModule(),true);
        addModule(new BatteryPushModule());
        addModule(new SearcherModule());
        addModule(new PrefetchModule());
        addModule(new LqWidgetModule());

		addModule(new CategoryFolderModule(),false);
		addModule(new FontModule());
		addModule(new AppRateModule());
		addModule(new DefaultLauncherModule());

        overrideModuleDefaults();
        
        dump();
    }

	private void overrideModuleDefaults() {
		Map<String, Boolean> overrides = ClientInfo.overrideModuleDefaults();
        if (overrides != null){
        	for (Entry<String, Boolean> entry : overrides.entrySet()){
        		String moduleName = entry.getKey();
        		IModule module = mModules.getModuleByName(moduleName);
        		boolean enabled = entry.getValue();
        		mModuleDefaults.put(module, enabled);
        	}
        }
	}
    
    private void addModule(IModule module) {
    	addModule(module, false);
    }
    private void addModule(IModule module, boolean enabled){
        mModuleDefaults.put(module, enabled);
        mModules.addModule(module);
    }
	
    public void init() {
    	NqLog.i("CurrentProcessName: " + ApplicationContext.getCurrentProcessName());
		if (ProcessInfo.isProviderProcess(mContext)) {
			return;
		} else if (ProcessInfo.isStoreProcess(mContext)) {
			initModules();
		} else if (ProcessInfo.isLauncherProcess(mContext)) {// Launcher Process
			checkVersionUpgrade();
			registerTables();
			initModules();
			
			registerReceiver();
			startBackgroundService();
		}
    }
    
    private void registerTables() {
		List<IDataTable> tables = getAllTables();

		for (IDataTable table : tables) {
			String tableName = table.getName();
			mPreference.setIntValue(TABLE_VERSION_PREFIX + tableName,
					table.getVersion());
		}
	}

	private List<IDataTable> getAllTables() {
		Collection<IModule> modules = ModuleContainer.getInstance().getModules();
		List<IDataTable> tables = new ArrayList<IDataTable>();
		for (IModule module : modules) {
			List<IDataTable> list = module.getTables();
			if (list != null && !list.isEmpty()){	
				tables.addAll(list);
			}
		}
		return tables;
	}

	private void initModules() {
		String[] projection = new String[]{PreferenceServiceFactory.LIVE_SDK_SETTINGS_PREFERENCE, "isAppInitDone", "isAppInitDone"};
    	Cursor cursor = mContext.getContentResolver().query(DataProvider.TABLE_PREF_SERVICE_URI, projection, null, null, null);
		boolean isAppInitDone = cursor.getExtras().getBoolean("isAppInitDone");
		cursor.close();
		if (isAppInitDone) {
			for (IModule module : mModules.getModules()) {
				if(module.canEnabled()){
					module.init();
				}
			}
		}else{
			copyTrustStoreFile();
			for (Entry<IModule, Boolean> entry : mModuleDefaults.entrySet()) {
				IModule module = entry.getKey();
				boolean enabled = entry.getValue();
				module.onAppFirstInit(enabled);
				initModule(module, enabled);
			}
		}
//		if (isAppInitDone) {
//			for (IModule module : mModules.getModules()) {
//				boolean enabled = module.canEnabled();
//				initModule(module, enabled);
//			}
//		}
	}
	
	private void checkVersionUpgrade() {
		int ver = mPreference.getCurrentVersion();
        if(ver != ClientInfo.getEditionId()){
        	ActivePreference.getInstance().setOverrideInstall(true);//设置覆盖安装 标志
        	mPreference.setLastVersion(ver);
        	mPreference.setCurrentVersion(ClientInfo.getEditionId());
        	
        	
        	//升级后，重置渠道号，新渠道号从新包里获取
        	CommonsPreference pref = CommonsPreference.getInstance();
        	pref.setChannelId("");
        	
        	//升级后，需要重新走一下激活
        	pref.setForegroundActiveSuccess(false);
        	pref.setNeedForegroundActive(true);

			ClientInfo.onUpgrade(ver);
        }else{
        	ActivePreference.getInstance().setOverrideInstall(false);//设置覆盖安装 标志
        }
	}
	
	private void startBackgroundService() {
		Context context = ApplicationContext.getContext();
        Intent i = new Intent(context,BackgroundService.class);
        i.setAction(BackgroundService.IMMEDIATE_PERIOD_CHECK_ACTION);
        context.startService(i);
	}

	private void initModule(IModule module, boolean enabled) {
		try {
			long start = SystemFacadeFactory.getSystem().currentTimeMillis();
			module.setEnabled(enabled);
			long end = SystemFacadeFactory.getSystem().currentTimeMillis();
			NqLog.i("Init: module=" + module.getName() + ", enabled=" + enabled
					+ ", time=" + (end - start));
		} catch (Exception e) {
			NqLog.e(e);
		}
    }


    private void copyTrustStoreFile() {
    	InputStream input = null;
        try {
            Context context = ApplicationContext.getContext();
            input = context.getAssets().open(TRUSTSTORE_FILE_NAME);
        	String outFileName = context.getFilesDir() + "/" + TRUSTSTORE_FILE_NAME;
        	FileUtil.writeStreamToFile(outFileName, input);
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
        	FileUtil.closeStream(input);
        }
    }
    
    private void dump(){
    	int size = mModules.getModules().size();
		NqLog.d("InitManagerNew size:" + size + " mModules:" + mModules);
		int i = 0;
		for (IModule module : mModules.getModules()) {
			List<IDataTable> tables = module.getTables();
			NqLog.d("InitManagerNew i:" + i + " module.name:"
					+ module.getName() + " tables:" + tables);
			if (CollectionUtils.isNotEmpty(tables)) {
				for (IDataTable iDataTable : tables) {
					NqLog.d("    InitManagerNew  iDataTable.name:"
							+ iDataTable.getName());
				}
			}
			i++;
		}
    }

	public void upgradeDb(SQLiteDatabase db, int oldVersion, int newVersion) {
		List<IDataTable> tables = getAllTables();
		
		for (IDataTable table : tables) {
			String tableName = table.getName();
			int oldTableVersion = 2;//mPreference.getIntValue(TABLE_VERSION_PREFIX+tableName);
			table.upgrade(db, oldTableVersion, table.getVersion());
		}
	}
	
	private void registerReceiver(){
		LiveReceiver receiver = new LiveReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(LiveReceiver.ACTION_REGULAR_UPDATE);
		filter.addAction(LiveReceiver.ACTION_WALLPAPER_DOWNLOAD);
		filter.addAction(LiveReceiver.ACTION_THEME_DOWNLOAD);
		filter.addAction(LiveReceiver.ACTION_APPLY_THEME);
		filter.addAction(LiveReceiver.ACTION_LOCKER_DOWNLOAD);
		filter.addAction(LiveReceiver.ACTION_LOCKER_DELETED);
		filter.addAction(LiveReceiver.ACTION_APPLY_LOCKER);
		filter.addAction(LiveReceiver.ACTION_PREVIEW_LOCKER);
		filter.addAction(LiveReceiver.ACTION_APPSTUB_UPDATE);
		filter.addAction(LiveReceiver.ACTION_APPSTUB_ADD);
		filter.addAction(LiveReceiver.ACTION_APP_UPDATE);
        filter.addAction(LiveReceiver.ACTION_APPSTUB_FOLDER_ADD);
        filter.addAction(LiveReceiver.ACTION_SILENT_INSTALL);
		mContext.registerReceiver(receiver, filter);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
