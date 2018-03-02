package com.nqmobile.livesdk.commons.image;

import java.io.File;
import java.util.concurrent.ExecutorService;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.nqmobile.livesdk.commons.concurrent.PriorityExecutor;
import com.nqmobile.livesdk.commons.info.ProcessInfo;

public class ImageLoader {  
	private static ImageLoader sLoader;
	private static final String IMAGE_CACHE_DIR = "images";
	private Context mContext;
    private ImageCache mImageCache;
    private ImageCache.ImageCacheParams mImageCacheParams;
    
    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;
    // 线程池  
    private ExecutorService mExecutorService;  
    
    private ImageLoader(Context context) {  
    	mContext = context.getApplicationContext();
    	mImageCacheParams =
                new ImageCache.ImageCacheParams(context, IMAGE_CACHE_DIR);
    	mImageCacheParams.setMemCacheSizePercent(0.125f); // Set memory cache to 12.5% of app memory
    	if (ProcessInfo.isLauncherProcess(mContext)){
    		mImageCacheParams.memoryCacheEnabled = false;
    	}
    	//mImageCacheParams.diskCacheEnabled = false;
        mImageCache = ImageCache.getInstance(mImageCacheParams);
        
        mExecutorService = PriorityExecutor.getExecutor(); 
        //init disk cache async
        CacheAsyncTask task = new CacheAsyncTask(MESSAGE_INIT_DISK_CACHE);
    	new Thread(task).start();
    }  
    
    public synchronized static ImageLoader getInstance(Context context){
    	if (sLoader == null){
    		sLoader = new ImageLoader(context.getApplicationContext());
    	}
    	
    	return sLoader;
    }
    
    public BitmapDrawable getImageFromMemoryCache(String url) {  
    	return mImageCache.getBitmapFromMemCache(url); 
    }
    
    public BitmapDrawable getImageFromMemoryCacheOrDisk(String url, int[] wh) {  
    	BitmapDrawable drawable = mImageCache.getBitmapFromMemCache(url); 
    	if(drawable == null){
    		Bitmap bmp = mImageCache.getBitmapFromDiskCache(url, wh);
    		if(bmp != null){
    			drawable = new RecyclingBitmapDrawable(mContext.getResources(), bmp);
    		}
    	}
    	return drawable;
    }
    
    // 最主要的方法  
    public void getImage(String url, ImageListener listener) {  
    	if(url == null){
    		listener.onErr();
    		return;
    	}
    	
        // 先从内存缓存中查找  
        BitmapDrawable drawable = mImageCache.getBitmapFromMemCache(url);  
        if (drawable != null){
        	listener.getImageSucc(url, drawable);
        }
        else {  
            mExecutorService.submit(new ImageFetcher(mContext, url, mImageCache, listener));
        }  
    }   
    
    public void getImage(String path, String url, ImageListener listener) {
    	if(path != null){
    		File f = new File(path);
    		if(f.exists()){
    			BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), path);
    			if(drawable != null){
    				listener.getImageSucc(url, drawable);
    				return;
    			}
    		}
    	}
    	if(url == null){
    		listener.onErr();
    		return;
    	}
    	
        // 先从内存缓存中查找  
        BitmapDrawable drawable = mImageCache.getBitmapFromMemCache(url);  
        if (drawable != null){
        	listener.getImageSucc(url, drawable);
        }
        else {  
            mExecutorService.submit(new ImageFetcher(mContext, url, mImageCache, listener));
        }  
    }
    
	protected class CacheAsyncTask implements Runnable {
		private final int mMsg;

		public CacheAsyncTask(int msg) {
			mMsg = msg;
		}

		@Override
		public void run() {
			if (mImageCache != null) {
				switch (mMsg) {
				case MESSAGE_CLEAR:
					mImageCache.clearCache();
					break;
				case MESSAGE_INIT_DISK_CACHE:
					mImageCache.initDiskCache();
					break;
				case MESSAGE_FLUSH:
					mImageCache.flush();
					break;
				case MESSAGE_CLOSE:
					mImageCache.close();
					mImageCache = null;
					break;
				default:
					break;
				}
			}
		}
	}
    
	protected void clearCache() {
    	CacheAsyncTask task = new CacheAsyncTask(MESSAGE_CLEAR);
    	new Thread(task).start();
    }

	protected void flushCache() {
    	CacheAsyncTask task = new CacheAsyncTask(MESSAGE_FLUSH);
    	new Thread(task).start();
    }

	protected void closeCache() {
    	CacheAsyncTask task = new CacheAsyncTask(MESSAGE_CLOSE);
    	new Thread(task).start();
    }
}  