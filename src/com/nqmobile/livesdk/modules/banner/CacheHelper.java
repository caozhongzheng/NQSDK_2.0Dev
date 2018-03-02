package com.nqmobile.livesdk.modules.banner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import android.content.Context;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.utils.FileUtil;

public class CacheHelper {
	private static final ILogger NqLog = LoggerFactory.getLogger(BannerModule.MODULE_NAME);
	
	private String cache_data = "/data/data/";
	private String cache_path = "";
	private String cache_banner = ""; 
	private Context mContext;
	private static CacheHelper instance;
	
	private CacheHelper(Context context){
		mContext = context;
		cache_path = cache_data + context.getPackageName() + "/cache/banner";
		cache_banner = cache_path + "/banner_";
	}
	
	public synchronized static CacheHelper getInstance(Context context){
		if (instance == null) {
			instance = new CacheHelper(context);
		}
		return instance;
	}
	
	/**
	 * 缓存内容汇总
	 * @param groupMainData
	 * @param uid
	 */
	public void saveBannerListDataObjCache(ArrayList<Banner> banners, int uid) {
		ObjectOutputStream out = null;
		try {
			NqLog.d("saveBannerListDataObjCache uid:" + uid);
			if (banners == null || banners.size() == 0) 
				return;
			String cachePath = cache_banner + uid + ".obj";
			File file = new File(cache_path);
			if (!file.exists()) {
				file.mkdirs();
			}
			out = new ObjectOutputStream(new FileOutputStream(cachePath));
			out.writeObject(banners);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			NqLog.e("saveBannerListDataObjCache uid:" + uid + ", FileNotFoundException err:" + e);
		} catch (Exception e) {
			e.printStackTrace();
			NqLog.e("saveBannerListDataObjCache uid:" + uid + ", err:" + e);
		} finally {
			FileUtil.closeStream(out);
		}
	}
	
	/**
	 * 获取内容汇总缓存
	 * @param dataCreator
	 * @param uid
	 */
	public ArrayList<Banner> getBannerListFromCache(int uid) {
		ObjectInputStream in = null;
		ArrayList<Banner> banners = new ArrayList<Banner>();
		try {
			String cachePath = cache_banner + uid + ".obj";
			File file = new File(cachePath);
			if (file.exists() && file.length() > 0) {
				in = new ObjectInputStream(new FileInputStream(cachePath));
				banners = (ArrayList<Banner>) in.readObject();
			}
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			FileUtil.closeStream(in);
		}
		NqLog.d("getBannerListFromCache uid:" + uid + ", " + banners);
		return banners;
	}
}
