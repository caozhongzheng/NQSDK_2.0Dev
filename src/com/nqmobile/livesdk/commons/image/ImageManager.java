package com.nqmobile.livesdk.commons.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;

import com.nqmobile.livesdk.commons.AppConstants;
import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.appstubfolder.model.AppStubFolder;
import com.nqmobile.livesdk.modules.daily.DailyManager;
import com.nqmobile.livesdk.modules.daily.Web;
import com.nqmobile.livesdk.modules.locker.Locker;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.wallpaper.Wallpaper;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.FileUtil;
import com.nqmobile.livesdk.utils.ImagUtil;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.StringUtil;

/**
 * 图片业务类
 *
 * @author changxiaofei
 * @time 2013-12-7 下午5:15:23
 */
public class ImageManager {
    private Context context;
    private static ImageManager mInstance;

    public ImageManager(Context context) {
        super();
        this.context = context;
    }

    public synchronized static ImageManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ImageManager(context.getApplicationContext());
        }
        return mInstance;
    }

    public boolean isSDCardOk(){
    	return CommonMethod.getSDcardPath(context) != null;
    }
    /**
     * 缓存图片。
     *
     * @param path
     * @param bmp
     * @return
     */
    public boolean saveBitmapFile(String path, Bitmap bmp) {
        // TqLog.d(TAG, "ImagManager.saveBitmapFile path=" + path);
        if (!isSDCardOk() || TextUtils.isEmpty(path) || bmp == null) {
            return false;
        }
        try {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                imgFile.delete();
            }
            FileUtil.writeBmpToFile(bmp, imgFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 根据路径取图片
     *
     * @param path
     * @return
     */
    public Bitmap getSavedWallpaper(String path) {
        if (!isSDCardOk() || TextUtils.isEmpty(path)) {
            return null;
        }
        Bitmap bmp = null;
        File imgFile = new File(path);
        if (imgFile.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(path);
                bmp = BitmapFactory.decodeStream(fis);
            } catch (Exception e) {
                NqLog.e(e + "\n" + path);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                        fis = null;
                    } catch (IOException e) {
                        NqLog.e(e);
                    }
                }
            }
        } else {
            NqLog.d("ImagManager.getSavedWallpaper. file not exist. path=" + path);
        }
        return bmp;
    }
    
    /**
     * 根据路径取图片
     *
     * @param path
     * @return
     */
    public InputStream getSavedWallpaper2(String path) {
        if (!isSDCardOk() || TextUtils.isEmpty(path)) {
            return null;
        }
        FileInputStream fis = null;
        File imgFile = new File(path);
        if (imgFile.exists()) {
            try {
                fis = new FileInputStream(path);
            } catch (Exception e) {
                NqLog.e(e + "\n" + path);
            }
        } else {
            NqLog.d("ImagManager.getSavedWallpaper2. file not exist. path=" + path);
        }
        return fis;
    }

    /**
     * 联网下载icon图片
     *
     * @param url
     * @return Bitmap
     */
    private Bitmap loadImageFromUrl(String url) {
        URL tempUrl;
        InputStream inputStream = null;
        Bitmap myBitmap = null;
        try {
            NqLog.d("loadImageFromUrl url=" + url);
            tempUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) tempUrl.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoInput(true);
            conn.connect();
            inputStream = conn.getInputStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            myBitmap = BitmapFactory.decodeStream(inputStream, null, options);
        } catch (Exception e) {
            e.printStackTrace();
            NqLog.d("loadImageFromUrl error " + e.toString());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }
            } catch (Exception e) {
            }
        }
        return myBitmap;
    }

    public Bitmap getDefaultIcon() {
        return BitmapFactory.decodeResource(context.getResources(),
                MResource.getIdByName(context, "drawable", "nq_icon_default"));
    }

    public BitmapDrawable loadImageFromMemory(String url) {
        return ImageLoader.getInstance(context).getImageFromMemoryCache(url);
    }

    public BitmapDrawable loadImageFromMemoryOrDisk(String url) {
        return loadImageFromMemoryOrDisk(url, null);
    }
    
    public BitmapDrawable loadImageFromMemoryOrDisk(String url, int[] wh) {
    	return ImageLoader.getInstance(context).getImageFromMemoryCacheOrDisk(url, wh);
    }

    public void loadImage(String url, ImageListener l) {
    	ImageLoader.getInstance(context).getImage(url, l);
    }

    public class IconMaker {
        private String mUrl;
        private LoadIconListener mListener;

        public IconMaker(String url, LoadIconListener imageLoadListener) {
            mUrl = url;
            mListener = imageLoadListener;
        }

        public void makeIcon(final int width, final int height) {
            loadImage(mUrl, new ImageListener() {
                @Override
                public void onErr() {
                    mListener.onErr();
                }

                @Override
                public void getImageSucc(String url, BitmapDrawable drawable) {
                    Bitmap result = drawable.getBitmap();
                    Bitmap r = result.copy(result.getConfig(),false);
                    if (r == null) {
                        r = getDefaultIcon();
                    }

                    if(width > 0 || height > 0) {
                        r = ImagUtil.scaleBitmap(r, width, height);
                    }
                    mListener.onLoadComplete(r);
                }

            });

        }
    }

    /**
     * 获取app的icon图片。
     *
     * @param sourceType        功能模块来源编号：0：store列表；1：banner；2每日推荐
     * @param app
     * @param imageLoadListener
     */
    public void getIconBitmap(int sourceType, App app, LoadIconListener imageLoadListener) {
        try {
            if (sourceType == DailyManager.STORE_MODULE_TYPE_DAILY) {
                IconMaker maker = new IconMaker(app.getStrImageUrl(), imageLoadListener);
                maker.makeIcon(268, 268);
            } else {
            	Bitmap bmp = null;
            	String iconPath = app.getStrIconPath();
            	if(!StringUtil.isNullOrEmpty(iconPath)){
            		File f = new File(iconPath);
            		if(f.exists()){
            			bmp = BitmapFactory.decodeFile(iconPath);
            			if(bmp != null){
            				imageLoadListener.onLoadComplete(bmp);
            			}
            		}
            	}
            	if(bmp == null) {
            		IconMaker maker = new IconMaker(app.getStrIconUrl(), imageLoadListener);
            		maker.makeIcon(206, 206);
            	}
            }
        } catch (Exception e) {
        	e.printStackTrace();
        	NqLog.e(e);
        }
    }
    
    /**
     * 获取app的preview图片。
     *
     * @param sourceType   功能模块来源编号：5游戏文件夹
     * @param index 第几张预览图
     * @param app
     * @param imageLoadListener
     */
    public void getPreviewBitmap(int sourceType, int index, App app, LoadIconListener imageLoadListener) {
    	if(index >= app.getArrPreviewPath().size())
    		return;
        try {
            if (sourceType == AppConstants.STORE_MODULE_TYPE_GAME_AD) {
            	Bitmap bmp = null;
            	String previewPath = app.getArrPreviewPath().get(index);
            	if(!StringUtil.isNullOrEmpty(previewPath)){
            		File f = new File(previewPath);
            		if(f.exists()){
            			bmp = BitmapFactory.decodeFile(previewPath);
            			if(bmp != null){
            				imageLoadListener.onLoadComplete(bmp);
            			}
            		}
            	}
            	if(bmp == null) {
            		IconMaker maker = new IconMaker(app.getStrIconUrl(), imageLoadListener);
            		maker.makeIcon(480, 800);
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Theme的icon图片。
     *
     * @param sourceType        功能模块来源编号：0：store列表；1：banner；2每日推荐
     * @param theme
     * @param imageLoadListener
     */
    public void getIconBitmap(int sourceType, Theme theme, LoadIconListener imageLoadListener) {
        getIconBitmap(sourceType, theme, imageLoadListener, false, 0, 0);
    }

    /**
     * 获取Theme的icon图片。
     *
     * @param sourceType        功能模块来源编号：0：store列表；1：banner；2每日推荐
     * @param theme
     * @param imageLoadListener
     * @param customize true:定制大小(customize为true时width和height有效)
     * @param width 定制宽度(width和height如果有任何一个不大于0都无效)
     * @param height 定制高度(width和height如果有任何一个不大于0都无效)
     */
    public void getIconBitmap(int sourceType, Theme theme, LoadIconListener imageLoadListener,
            boolean customize, int width, int height) {
        try {
            if (sourceType == DailyManager.STORE_MODULE_TYPE_DAILY) {
                IconMaker maker = new IconMaker(theme.getDailyIcon(), imageLoadListener);
                maker.makeIcon(268, 268);
            } else if(!customize) {
                IconMaker maker = new IconMaker(theme.getStrIconUrl(), imageLoadListener);
                maker.makeIcon(0, 0);
            } else {
                boolean scale = (width > 0 && height > 0);
                Bitmap bmp = getIconBitmapFromCache(theme.getStrIconUrl());
                if(bmp != null){
                    imageLoadListener.onLoadComplete(scale ? ImagUtil.scaleBitmap(bmp, width, height)
                            : ImagUtil.scaleBitmap(bmp, bmp.getWidth(),bmp.getHeight()));
                } else {
                    bmp = getIconBitmapFromLocal(theme.getStrIconPath());
                    if(bmp != null){
                        imageLoadListener.onLoadComplete(scale ? ImagUtil.scaleBitmap(bmp, width, height)
                                : ImagUtil.scaleBitmap(bmp, bmp.getWidth(),bmp.getHeight()));
                    } else {
                        IconMaker maker = new IconMaker(theme.getStrIconUrl(), imageLoadListener);
                        maker.makeIcon(scale ? width : 0, scale ? height : 0);
                    }
                }
            }
        } catch (Exception e) {
            NqLog.e(e);
        }
    }

    /**
     * 获取Wallpaper的icon图片。
     *
     * @param sourceType        功能模块来源编号：0：store列表；1：banner；2每日推荐
     * @param wallpaper
     * @param imageLoadListener
     */
    public void getIconBitmap(int sourceType, Wallpaper wallpaper, LoadIconListener imageLoadListener) {
        try {
            if (sourceType == DailyManager.STORE_MODULE_TYPE_DAILY) {
                IconMaker maker = new IconMaker(wallpaper.getDailyIcon(), imageLoadListener);
                maker.makeIcon(268, 268);
            } else {
            	Bitmap bitmap = getIconBitmapFromCache(wallpaper.getStrIconUrl());
                if(bitmap != null){
                	imageLoadListener.onLoadComplete(ImagUtil.scaleBitmap(bitmap, 88, 88));
                } else {
                	bitmap = getIconBitmapFromLocal(wallpaper.getStrIconPath());
                	if(bitmap != null){
                		imageLoadListener.onLoadComplete(ImagUtil.scaleBitmap(bitmap, 88, 88));
                	} else {
                		IconMaker maker = new IconMaker(wallpaper.getStrIconUrl(), imageLoadListener);
                		maker.makeIcon(88, 88);
                	}
                }
            }
        } catch (Exception e) {
            NqLog.e(e);
        }
    }
    
    private Bitmap getIconBitmapFromLocal(String strIconPath) {
		// TODO Auto-generated method stub
		return getSavedWallpaper(strIconPath);
	}

	private Bitmap getIconBitmapFromCache(String strIconUrl) {
		// TODO Auto-generated method stub
		if (TextUtils.isEmpty(strIconUrl)) {
            return null;
        }
    	Bitmap result = null;
    	ImageManager imgMgr = ImageManager.getInstance(context);
    	BitmapDrawable bd = imgMgr.loadImageFromMemoryOrDisk(strIconUrl);
        if(bd != null){
			try {
				result = bd.getBitmap();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        return result;
	}

	/**
     * 获取Web的icon图片。
     *
     * @param sourceType        功能模块来源编号：0：store列表；1：banner；2每日推荐
     * @param Web
     * @param imageLoadListener
     */
    public void getIconBitmap(int sourceType, Web web,
			LoadIconListener imageLoadListener) {
		// TODO Auto-generated method stub
    	try {
            if (sourceType == DailyManager.STORE_MODULE_TYPE_DAILY) {
                IconMaker maker = new IconMaker(web.getStrDailyIconUrl(), imageLoadListener);
                maker.makeIcon(268, 268);
            } else {
                IconMaker maker = new IconMaker(web.getStrIconUrl(), imageLoadListener);
                maker.makeIcon(88, 88);
            }
        } catch (Exception e) {
            NqLog.e(e);
        }
	}

    /**
    * 获取Locker的icon图片。
    *
    * @param sourceType        功能模块来源编号：0：store列表；1：banner；2每日推荐
    * @param Locker
    * @param imageLoadListener
    */
	public void getIconBitmap(int sourceType, Locker locker,
			LoadIconListener imageLoadListener) {
		// TODO Auto-generated method stub
		try {
            if (sourceType == DailyManager.STORE_MODULE_TYPE_DAILY) {
                IconMaker maker = new IconMaker(locker.getStrDailyIconUrl(), imageLoadListener);
                maker.makeIcon(268, 268);
            } else {
                IconMaker maker = new IconMaker(locker.getStrIconUrl(), imageLoadListener);
                maker.makeIcon(88, 88);
            }
        } catch (Exception e) {
            NqLog.e(e);
        }
	}

    /**
     * 获取game的icon图片。
     *
     * @param sourceType        功能模块来源编号：0：store列表；1：banner；2每日推荐; 5:游戏广告
     * @param app
     * @param imageLoadListener
     */
	public void getIconBitmap(int storeModuleTypeGameAd, App app,
			ImageListener imageListener) {
		// TODO Auto-generated method stub
		try {
            if (storeModuleTypeGameAd == AppConstants.STORE_MODULE_TYPE_GAME_AD) {
            	loadImage(app.getStrIconUrl(), imageListener);
            }
        } catch (Exception e) {
            NqLog.e(e);
        }
	}
	
	/**
     * 获取虚框文件夹的icon图片。
     *
     * @param sourceType   功能模块来源编号：
     * @param index 第几张预览图
     * @param app
     * @param imageLoadListener
     */
    public void getIconBitmap(int sourceType, AppStubFolder asf, LoadIconListener imageLoadListener) {
        try {
//            if (sourceType == ThemeConstants.STORE_MODULE_TYPE_GAME_AD) {
            	Bitmap bmp = null;
            	String iconPath = asf.getIconPath();
            	if(!StringUtil.isNullOrEmpty(iconPath)){
            		File f = new File(iconPath);
            		if(f.exists()){
            			bmp = BitmapFactory.decodeFile(iconPath);
            			if(bmp != null){
            				imageLoadListener.onLoadComplete(bmp);
            			}
            		}
            	}
            	if(bmp == null) {
            		IconMaker maker = new IconMaker(asf.getIconUrl(), imageLoadListener);
            		maker.makeIcon(206, 206);
            	}
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
