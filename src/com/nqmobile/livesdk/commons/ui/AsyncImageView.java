package com.nqmobile.livesdk.commons.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.nqmobile.livesdk.commons.image.ImageListener;
import com.nqmobile.livesdk.commons.image.ImageManager;
import com.nqmobile.livesdk.commons.image.RecyclingBitmapDrawable;
import com.nqmobile.livesdk.commons.log.NqLog;

public class AsyncImageView extends ImageView implements ImageListener {
	private String mPath;
	private String mUrl;
	private int mDefaultResId;
	private View mLoading;
	private Drawable mDefaultBitmap;
	private boolean isDisplayed; //目标图片是否显示出来，如果显示的是默认icon，则isDisplayed=false
	private boolean isIcon = false; // 从缓存中获取
	private Handler mHandler;
	private static int screenW, screenH;
	private Drawable mPreviousDrawable;

	public AsyncImageView(Context context) {
		this(context, null);
	}

	public AsyncImageView(Context context, AttributeSet attrs) {
		this(context, attrs, -1);
	}

	public AsyncImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mHandler = new Handler();
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		screenW = dm.widthPixels;
		screenH = dm.heightPixels;
	}
	
	public void loadImage(String path, String url, View loading, int defaultResId) {
		NqLog.d("loadImage: url=" + url + " path=" + path);
		mDefaultResId = defaultResId;
		try {
			if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(mPath) && !path.equals(mPath)){//之前的imageView已经被重复利用，需要先把默认icon显示出来
				setImage(getDefaultBitmap());
			}
			if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(mUrl) && !url.equals(mUrl)){////之前的imageView已经被重复利用，需要先把默认icon显示出来
				setImage(getDefaultBitmap());
			}
			
			mPath = path;
			mUrl = url;
			mLoading = loading;
			ImageManager imgMgr = ImageManager.getInstance(getContext());
			BitmapDrawable drawable = imgMgr.loadImageFromMemory(url);
			if (drawable == null) {
				if(!TextUtils.isEmpty(path)){
					Bitmap bitmap = ImageManager.getInstance(getContext()).getSavedWallpaper(path);
					if (bitmap != null) {
						drawable = new RecyclingBitmapDrawable(getContext().getResources(),
								bitmap);
					}
				}
			}
			
			if (drawable == null){
				if (mLoading != null) {
					mLoading.setVisibility(View.VISIBLE);
				}
				
				ImageManager.getInstance(getContext()).loadImage(url, this);
			} else {
				setImage(drawable);//直接从缓存读取，因此不需要动画
				isDisplayed = true;
				if (mLoading != null){
					mLoading.setVisibility(View.GONE);
				}
			}
		} catch (Exception e) { // OutOfMemoryError
			NqLog.d("loadImage exception: " + e);
		}
		
	}
	
	public void loadImage(String url, View loading, int defaultResId) {
		loadImage(null, url, loading, defaultResId);
	}

	@Override
	public void onErr() {
		NqLog.d("onErr: isIcon:" + isIcon + ", mUrl=" + mUrl);
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (!isIcon) 
					setImage(getDefaultBitmap());
				isDisplayed = false;
				if (mLoading != null)
					mLoading.setVisibility(View.GONE);
			}
		});

	}

	@Override
	public void getImageSucc(String url, BitmapDrawable drawable) {
		NqLog.d("getImageSucc: drawable:" + 
				(drawable != null ? ("[" +drawable.getIntrinsicWidth() +" " + drawable.getIntrinsicWidth() + "]") : " ") + 
				" mUrl=" + mUrl);
		if (url == null || !url.equals(mUrl)) {
			return;// 收到回调时，之前的imageView已经被重复利用，因此忽略回调
		}

		/*if (drawable == null) {
			drawable = getDefaultBitmap();
		}*/
		final BitmapDrawable result = drawable;

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				NqLog.d("getImageSucc.run: mUrl=" + mUrl);
				if(result != null)
					setImage(result);
				else
					setImage(getDefaultBitmap());
				showAnimation();
				isDisplayed = true;
				if (mLoading != null){
					mLoading.setVisibility(View.GONE);
				}
				recycleDefaultBitmap();
			}
		});
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//NqLog.d(TAG, "onDraw: mUrl=" + mUrl);
		Drawable drawable = getDrawable();
		if (drawable instanceof BitmapDrawable) {
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			if (bitmap == null || bitmap.isRecycled()) {
				setImage(getDefaultBitmap());
				loadImage(mUrl, mLoading, mDefaultResId);
				return;
			}
		}

		super.onDraw(canvas);
	}

	@Override
	protected void onDetachedFromWindow() {
		onHide();		
		super.onDetachedFromWindow();
	}
	
	public void loadImage(int resId) {
		Drawable drawable = getResources().getDrawable(resId);
		
		if (drawable == null) {
			setImage(getDefaultBitmap());
		} else {
			setImage(getResources().getDrawable(resId));
		}
		isDisplayed = true;
		recycleDefaultBitmap();
	}
	
	private Drawable getDefaultBitmap() {
		if (mDefaultBitmap == null && mDefaultResId != 0) {
			mDefaultBitmap = getResources().getDrawable(mDefaultResId);
		}
		return mDefaultBitmap;
	}
	
	public void onHide() {
		recycle();
		isDisplayed = false;
	}
	
	public void onShow() {
		if (!isDisplayed) {
			loadImage(mPath, mUrl, mLoading, mDefaultResId);
		}
	}
	
	private void recycle(){
		setImage(null);
		recycleDefaultBitmap();
	}
	
	private void recycleDefaultBitmap(){
		if (mDefaultBitmap != null) {
			//notifyDrawable(mDefaultBitmap, false);
			//mDefaultBitmap = null;
		}
	}
	
	public String getUrl(){
		return mUrl;
	}
	
	private void showAnimation(){
		// 添加透明渐变动画显示图片
        //AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
        //alphaAnim.setDuration(1000);
        //setAnimation(alphaAnim);
	}
    
    private void setImage(Drawable drawable) {
        // Keep hold of previous Drawable
        final Drawable previousDrawable = mPreviousDrawable;

        // Call super to set new Drawable
        if (drawable instanceof RecyclingBitmapDrawable){
        	setImageBitmap(((RecyclingBitmapDrawable) drawable).getBitmap());
        } else {
        	setImageDrawable(drawable);
        }

        // Notify new Drawable that it is being displayed
        if (drawable != null){
        	notifyDrawable(drawable, true);
        }

        // Notify old Drawable so it is no longer being displayed
        if (previousDrawable != null){
        	notifyDrawable(previousDrawable, false);
        }
        
        mPreviousDrawable = drawable;
    }

    /**
     * Notifies the drawable that it's displayed state has changed.
     *
     * @param drawable
     * @param isDisplayed
     */
    private static void notifyDrawable(Drawable drawable, final boolean isDisplayed) {
        if (drawable instanceof RecyclingBitmapDrawable) {
            // The drawable is a RecyclingBitmapDrawable, so notify it
            ((RecyclingBitmapDrawable) drawable).setIsDisplayed(isDisplayed);
        } else if (drawable instanceof LayerDrawable) {
            // The drawable is a LayerDrawable, so recurse on each layer
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            for (int i = 0, z = layerDrawable.getNumberOfLayers(); i < z; i++) {
                notifyDrawable(layerDrawable.getDrawable(i), isDisplayed);
            }
        }
    }
	
    public final static int OTHER = 0;
    public final static int ICON = OTHER + 1;
    public final static int WALLPAPER = ICON + 1;
    public final static int WALLPAPER_DETAIL = WALLPAPER + 1;
    private final static int[] ICON_WH = new int[]{80, 80};
    private final static int[] WALLPAPER_WH = new int[]{120, 190};
    public int[] mWH = null;
    
    public void setType(int type){
		mWH = getPicWH(type);
	}
    
    public static int[] getPicWH(int type){
    	if (type == ICON) {
			return ICON_WH;
		}else if(type == WALLPAPER){
			return WALLPAPER_WH;
    	}else if(type == WALLPAPER_DETAIL)
    		return new int[]{screenW >> 1, screenH >> 1};
    	return null;    			
    }
}
