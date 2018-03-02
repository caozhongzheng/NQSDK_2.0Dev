package com.nqmobile.livesdk.modules.apptype;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import android.util.Log;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.db.DataProvider;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.modules.app.table.AppLocalTable;
import com.nqmobile.livesdk.modules.apptype.event.AppTypeLibUpgradeEvent;
import com.nqmobile.livesdk.modules.apptype.model.AppTypeInfo;
import com.nqmobile.livesdk.modules.apptype.network.AppTypeServiceFactory;
import com.nqmobile.livesdk.modules.apptype.network.GetAppTypeProtocol.GetAppTypeFailedEvent;
import com.nqmobile.livesdk.modules.apptype.network.GetAppTypeProtocol.GetAppTypeSuccessEvent;
import com.nqmobile.livesdk.modules.apptype.table.AppCodeTable;
import com.nqmobile.livesdk.utils.CollectionUtils;
import com.nqmobile.livesdk.utils.CursorUtils;
import com.nqmobile.livesdk.utils.FileUtil;
import com.nqmobile.livesdk.utils.HttpClientUtils;
import com.nqmobile.livesdk.utils.MD5;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;

public class AppTypeManager extends AbsManager {
    // ===========================================================
    // Constants
    // ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(AppTypeModule.MODULE_NAME);
	private static final String APP_TYPE_LIB_FILE_NAME = "gamebin";
    // ===========================================================
    // Fields
    // ===========================================================
    private static AppTypeManager sInstance = new AppTypeManager();
    private HashMap<Integer,Integer> mAppTypeLib;
    private Context mContext;
    private Object mAppTypeLibLock = new Object();
    private Object mUpgradeLock = new Object();
	private AppTypePreference mPreference;

    // ===========================================================
    // Constructors
    // ===========================================================
    public static AppTypeManager getInstance(){
        return sInstance;
    }
    private AppTypeManager(){
    	mAppTypeLib = new HashMap<Integer,Integer>();
        mContext = ApplicationContext.getContext();
        mPreference = AppTypePreference.getInstance();
    }
    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================
    @Override
    public void init() {
    	EventBus.getDefault().register(this);

    	//load app lib file
        String strLibPath = getAppTypeLibPath();
        File file = new File(strLibPath);
        if (!file.exists()) {
            loadAppTypeLibFromRaw();
        } else {
            loadAppTypeLibFromFile(false);
        }
    }
    
    @Override
    public void onDisable() {
		EventBus.getDefault().unregist(this);
		mAppTypeLib = new HashMap<Integer, Integer>();//释放AppType预置库占用的内存
	}
    // ===========================================================
    // Methods
    // ===========================================================
    private String getAppTypeLibPath() {
        String result = mContext.getFilesDir() + "/" + APP_TYPE_LIB_FILE_NAME;
        return result;
    }
    
    private void loadAppTypeLibFromRaw() {
        int resourceId = MResource.getIdByName(mContext, "raw", APP_TYPE_LIB_FILE_NAME);
        InputStream in = mContext.getResources().openRawResource(resourceId);
        new Thread(new AppTypeLibLoader(in, false)).start();   
    }

    private void loadAppTypeLibFromFile(boolean isReload) {
        String gameLibPath = getAppTypeLibPath();
        File file = new File(gameLibPath);
        if (!file.exists()) {
            return;
        }
        try {
            InputStream in = new FileInputStream(file);
            new Thread(new AppTypeLibLoader(in, isReload)).start();
        } catch (Exception e) {
            NqLog.e(e);
        }
    }
    
    public void getAppTypeFromServer(List<String> packageList) {
        getAppTypeFromServer(packageList, null);        
    }
    
    public void getAppTypeFromServer(List<String> packageList, Object tag) {
        AppTypeServiceFactory.getService().getAppType(packageList, tag);
     }
    
    public void getAppType(List<String> packageList, Object tag) {
    	if (packageList == null || packageList.isEmpty()){
    		return;
    	}

        boolean ignore = packageList.size() > 1;
    	
		try {
			List<String> askServerList = new ArrayList<String>();
			List<AppTypeInfo> appTypeLocalList = new ArrayList<AppTypeInfo>();
			for (String pkgName : packageList) {
				int code = getAppCodeFromLocal(pkgName,ignore);
				if (code == AppTypeInfo.CODE_UNKNOWN) {
					askServerList.add(pkgName);
				} else {
					appTypeLocalList.add(new AppTypeInfo(pkgName, code, "", ""));
				}
			}
			
			// 查询服务器识别应用类别
			if (askServerList.size() > 0) {
				if(NetworkUtils.isConnected(mContext)) {
					getAppTypeFromServer(askServerList, tag);
				} else {
					for(String pkgName : askServerList){
						appTypeLocalList.add(new AppTypeInfo(pkgName, AppTypeInfo.CODE_UNKNOWN, "", ""));
					}
				}
			}
			// 回调本地识别出的应用类别
			if (appTypeLocalList.size() > 0) {
				if (tag instanceof AppTypeListener) {
					AppTypeListener listener = (AppTypeListener) tag;
					listener.getAppTypeSucc(appTypeLocalList);
				}
			}
		} catch (Exception e) {
			NqLog.e(e);
		}
	}
    
    /** 获取应用类别编号: 应用:100~199, 游戏:200~255 */
    public int getAppCodeFromLocal(String packageName) {
        return getAppCodeFromLocal(packageName,false);
    }

    private int getAppCodeFromLocal(String packageName, boolean ignoreLocalTable){
//        NqLog.i("getAppCodeFromLocal packageName="+packageName+" ignoreLocalTable="+ignoreLocalTable);
        int code = AppTypeInfo.CODE_UNKNOWN;
        if (TextUtils.isEmpty(packageName)){
            return code;
        }

        try {
            if(!ignoreLocalTable){
                code = getAppCodeFromTable(packageName);
            }

            if(code == AppTypeInfo.CODE_UNKNOWN){
                int hash = hashCode(packageName);
                if(mAppTypeLib != null){
                    Integer value = mAppTypeLib.get(hash);
                    if (value != null) {
                        code = value;
                    }
                }
            }
        } catch (Exception e) {
            NqLog.e(e);
        }

//        NqLog.i("getAppCodeFromLocal packageName="+packageName+" code="+code);

        return code;
    }

    /**
     * 获取字符串的hashcode，代码来自JDK 7，保证和服务端用相同的求字符串hashcode方法
     */
    private int hashCode(String s) {
        s = s.toLowerCase(Locale.US);
        // following source code is from JDK hashCode
        int hash = 0;
        int count = s.length();
        int offset = 0;
        if (hash == 0) {
            if (count == 0) {
                return 0;
            }
            final int end = count + offset;
            final char[] chars = s.toCharArray();
            for (int i = offset; i < end; ++i) {
                hash = 31 * hash + chars[i];
            }
        }
        
        return hash;
    }
    private int getAppCodeFromTable(String packageName) {
        int result = AppTypeInfo.CODE_UNKNOWN;
        if (TextUtils.isEmpty(packageName)) {
            return result;
        }

        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(AppCodeTable.TABLE_URI,
                    new String[] { AppCodeTable.COLUMN_APPCODE }, AppCodeTable.COLUMN_PACKAGENAME
                            + " = ?", new String[] { packageName }, AppCodeTable._ID + " desc");
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToNext();
                result = cursor.getInt(cursor.getColumnIndex(AppCodeTable.COLUMN_APPCODE));
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
            CursorUtils.closeCursor(cursor);
        }

        return result;
    }
    
    public void onEvent(GetAppTypeSuccessEvent event) {
        List<AppTypeInfo> apps = event.getAppTypeInfos();
        
		try {
			Object tag = event.getTag();
			if (tag instanceof AppTypeListener) {
				AppTypeListener listener = (AppTypeListener) tag;
				if (CollectionUtils.isEmpty(apps)) {
					listener.onErr();
				} else {
					listener.getAppTypeSucc(apps);
				}
			}
		} catch (Exception e) {
			NqLog.e(e);
		}
		
        saveAppTypes(apps);
    }
    
    public void onEvent(GetAppTypeFailedEvent event){
    	List<AppTypeInfo> apps = event.getAppTypeInfos();
    	
    	Object tag = event.getTag();
		if (tag instanceof AppTypeListener) {
			AppTypeListener listener = (AppTypeListener) tag;
			if (CollectionUtils.isEmpty(apps)) {
				listener.onErr();
			} else {
				listener.getAppTypeSucc(apps);
			}
		}
    }
    
    private void saveAppTypes(List<AppTypeInfo> apps) {
    	if (CollectionUtils.isEmpty(apps)) {
            return;
        }
    	
        try {
        	
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            
			for (AppTypeInfo info : apps) {
				String packageName = info.getPackageName();
				int code = info.getCode();
				if (code == AppTypeInfo.CODE_UNKNOWN){
					continue;
				}
				
				String selection = AppLocalTable.APP_PACKAGENAME + " = ?";
				Builder deleteOp = ContentProviderOperation.newDelete(
						AppCodeTable.TABLE_URI).withSelection(selection,
						new String[] { packageName });
				ops.add(deleteOp.build());

				ContentValues values = new ContentValues();
				values.put(AppLocalTable.APP_PACKAGENAME, packageName);
				values.put(AppCodeTable.COLUMN_APPCODE, code);

				Builder insertOp = ContentProviderOperation.newInsert(
						AppCodeTable.TABLE_URI).withValues(values);
				ops.add(insertOp.build());
			}

            mContext.getContentResolver().applyBatch(DataProvider.DATA_AUTHORITY, ops);
        } catch (Exception e) {
            NqLog.e(e);
        }
    }
    
    public boolean upgradeLibFile() {
        if (!needUpgrade()) {
            return false;
        }
        
        boolean result = false;
		synchronized (mUpgradeLock) {
			if (needUpgrade()) {
				if (downloadLibFile()) {
					loadAppTypeLibFromFile(true);
					String newVer = mPreference.getAppLibServerVersion();
					mPreference.setAppLibVersion(newVer);
					result = true;
				}
			}
		}
        
        return result;
    }

    private boolean needUpgrade() {
        String serVer = mPreference.getAppLibServerVersion();
        String localVer = mPreference.getAppLibVersion();

        if (TextUtils.isEmpty(serVer)) {
            return false;
        } else {
            return !serVer.equals(localVer);
        }
    }
    
    /**
     * @param type
     * 游戏应用库更新
     * */
    private boolean downloadLibFile() {
        if (!NetworkUtils.isWifi(mContext)) {
            return false;
        }
        
        String url = mPreference.getAppLibServerUrl();
        String md5 = mPreference.getAppLibServerMd5();
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(md5)) {
            return false;
        }

        boolean result = false;
        try {
            String gameLibPath = getAppTypeLibPath();
            String tmpFileName = gameLibPath + ".tmp";
            boolean downloaded = HttpClientUtils.downloadUrl(url, tmpFileName);

            if (downloaded) {
                File tmpFile = new File(tmpFileName);
                if (MD5.checkMD5(md5, tmpFile)) {
                    File file = new File(gameLibPath);
                    if (file.exists()) {
                        file.delete();
                    }
                    result = tmpFile.renameTo(file);
                } else {
                    tmpFile.delete();
                }
            }
        } catch (Exception e) {
            NqLog.e(e);
        }

        return result;
    }
    
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
    private class AppTypeLibLoader implements Runnable {
        private InputStream mInput;
        private boolean mIsReload;
        
        public AppTypeLibLoader(InputStream input, boolean isReload){
            mInput = input;
            mIsReload = isReload;
        }
        
        @Override
        public void run() {
            if (mInput == null){
                return;
            }
            
            DataInputStream dis = null;
            HashMap<Integer,Integer> tmpMap;
            try {
                dis = new DataInputStream(mInput);
                long start = SystemFacadeFactory.getSystem().currentTimeMillis();
                
                int capacity = dis.available()/5;
                if (capacity > 0) {
                	tmpMap = new HashMap<Integer,Integer>(capacity);
                } else {
                	tmpMap = new HashMap<Integer,Integer>();
                }
                while (dis.available() != 0) {
                    int code = dis.readInt();
                    int type = dis.readByte() & 0xFF;
                    tmpMap.put(code, type);
                }
                long end = SystemFacadeFactory.getSystem().currentTimeMillis();
                NqLog.d("Load app type lib: time=" + (end-start) + ", size=" + tmpMap.size()
                		+ ", mIsReload=" + mIsReload);
                
                if (tmpMap.size() > 0) {
					synchronized (mAppTypeLibLock) {
						if (mAppTypeLib != null) {
							mAppTypeLib.clear();
						}
						mAppTypeLib = tmpMap;
					}
					if (mIsReload) {
						EventBus.getDefault().post(new AppTypeLibUpgradeEvent());
					}
                }
            } catch (Exception e) {
                NqLog.e(e);
            } finally {
                FileUtil.closeStream(dis);
            }
        }
    }
    
}
