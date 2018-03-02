package com.nqmobile.livesdk.modules.font;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ContentProviderOperation.Builder;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.commons.db.DataProvider;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.image.ImageListener;
import com.nqmobile.livesdk.commons.image.ImageLoader;
import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.mydownloadmanager.IDownloadObserver;
import com.nqmobile.livesdk.commons.mydownloadmanager.MyDownloadManager;
import com.nqmobile.livesdk.commons.mydownloadmanager.table.DownloadTable;
import com.nqmobile.livesdk.commons.receiver.DownloadCompleteEvent;
import com.nqmobile.livesdk.commons.receiver.LiveReceiver;
import com.nqmobile.livesdk.commons.ui.FontFactory;
import com.nqmobile.livesdk.modules.app.AppActionConstants;
import com.nqmobile.livesdk.modules.font.table.FontCacheTable;
import com.nqmobile.livesdk.modules.font.table.FontLocalTable;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.modules.theme.ThemePreference;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.CursorUtils;
import com.nqmobile.livesdk.utils.FileUtil;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;
import com.xinmei365.fontsdk.FontCenter;
import com.xinmei365.fontsdk.bean.Font;
import com.xinmei365.fontsdk.callback.ThumbnailCallBack;
import com.xinmei365.fontsdk.net.IHttpCallBack;
import com.xinmei365.fontsdk.util.JsonUtil;

public class FontManager extends AbsManager {
    public static final int STATUS_UNKNOWN = -1;//未知
    public static final int STATUS_NONE = 0;//未下载
    public static final int STATUS_DOWNLOADING = 1;//下载中
    public static final int STATUS_PAUSED = 2;//下载暂停中
    public static final int STATUS_DOWNLOADED = 3;//已下载
    public static final int STATUS_CURRENT_FONT = 4;//当前字体
    
    public static final String DEFAULT_FONT = "default";
    public static final String DEFAULT_FONT_ID="defaultId";
    public static String DEFAULT_FONT_NAME="默认字体";

    private Context mContext;
    public final static String CURRENT_FONT_BACKUP = "current_font_backup";
	private static FontManager mInstance;
	private Typeface    mCustomTypeface;
    private static final String STORE_FONT_QUERY_BY_COLUMN = FontLocalTable.FONT_SOURCE_TYPE + "="
            + FontConstants.STORE_MODULE_TYPE_MAIN + " AND "
            + FontLocalTable.FONT_COLUMN + "=?";
    public FontManager(Context context) {
        super();
        this.mContext = context.getApplicationContext();
        DEFAULT_FONT_NAME = MResource.getString(mContext, "nq_default_font_name");
    }

    public synchronized static FontManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FontManager(context);
        }
        return mInstance;
    }
	public Typeface getCustomTypeface(){
		return mCustomTypeface;
	}
    private void backupFontToData(String fontPath){
    	
    	if (TextUtils.isEmpty(fontPath)){
    		return;
    	}

    	try {
    		FileInputStream input = new FileInputStream(fontPath);
    		FileOutputStream output = mContext.openFileOutput(CURRENT_FONT_BACKUP, 0);
    		FileUtil.copyFile(input, output);
		} catch (Exception e) {
			e.printStackTrace();
		}
    } 
	
	@Override
	public void init() {
		EventBus.getDefault().register(this);
		String currentFontID = getCurrentFontID();
		String currentFont = getCurrentFont();
		if (TextUtils.isEmpty(currentFontID) || TextUtils.isEmpty(currentFont)
				|| DEFAULT_FONT_ID.equals(currentFontID)) {
			mCustomTypeface = Typeface.DEFAULT;
		}else{
			try{
			mCustomTypeface = Typeface.createFromFile(currentFont);
			}catch(Exception ex){
				mCustomTypeface = Typeface.DEFAULT;
			}finally{
			FontFactory.setDefaultTypeface(mCustomTypeface);
		}
	}
	}
	
    /**
     * 根据id 从缓存获取Font详情。
     *
     * @param fontId
     * @return Font对象或null。null表示没有找到。
     */
    public NqFont getFontDetailFromCache(String fontId) {
        NqFont font = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(FontCacheTable.FONT_CACHE_URI, null,
            		FontCacheTable.FONT_ID + "=?", new String[]{fontId}, FontCacheTable._ID + " desc");
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToNext();
                font = cursorToFont(cursor);
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
			CursorUtils.closeCursor(cursor);
        }
        return font;
    }
    /**
     * 返回缓存的字体列表。
     *
     * @param column 0：字体列表
     * @return
     */
    public List<NqFont> getFontListFromCache(int column) {
        List<NqFont> listFonts = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;

        try {
    		cursor = contentResolver.query(FontCacheTable.FONT_CACHE_URI, null,
    				STORE_FONT_QUERY_BY_COLUMN,
    				new String[]{String.valueOf(column)}, FontCacheTable._ID
    				+ " asc");
            if (cursor != null && cursor.getCount() > 0) {
            	listFonts = new ArrayList<NqFont>();
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    NqFont font = cursorToFont(cursor);
                    listFonts.add(font);
                }
            }
        } catch (Exception e) {
            NqLog.e(" getFontListFromCache " + e.toString());
        } finally {
        	CursorUtils.closeCursor(cursor);
        }
        return listFonts;
    }
    /**
     * 返回本地的字体列表。
     *
     * @param column 0：字体列表
     * @return
     */
    public List<NqFont> getFontListFromLocal(int column) {
        List<NqFont> listFonts = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        listFonts = new ArrayList<NqFont>();
        NqFont ft = new NqFont();
        ft.setStrId(DEFAULT_FONT_ID);
        ft.setStrName(DEFAULT_FONT_NAME);
        listFonts.add(ft);
        Cursor cursor = null;
        try {
    		cursor = contentResolver.query(FontLocalTable.LOCAL_FONT_URI, null,null,null, FontLocalTable._ID + " asc");
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    NqFont font = cursorToFont(cursor);
                    listFonts.add(font);
                }
            }else{
            	NqLog.i("cursor is null");
            }
            
        } catch (Exception e) {
            NqLog.e(" getFontListFromLocal " + e.toString());
        } finally {
        	CursorUtils.closeCursor(cursor);
        }

        return listFonts;
    }
    /**
     * 将cursor对象转换Font
     *
     * @param cursor
     * @return
     */
    public NqFont cursorToFont(Cursor cursor) {
        NqFont font = new NqFont();
        font.setStrId(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(FontLocalTable.FONT_ID))));
        font.setIntSourceType(cursor.getColumnIndex(FontLocalTable.FONT_SOURCE_TYPE));
        font.setStrName(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(FontLocalTable.FONT_NAME))));
        font.setStrAuthor(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(FontLocalTable.FONT_AUTHOR))));
        font.setStrVersion(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(FontLocalTable.FONT_VERSION))));
        font.setStrSource(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(FontLocalTable.FONT_SOURCE))));
        font.setLongSize(cursor.getLong(cursor.getColumnIndex(FontLocalTable.FONT_SIZE)));
        font.setLongDownloadCount(cursor.getLong(cursor.getColumnIndex(FontLocalTable.FONT_DOWNLOAD_COUNT)));
        font.setStrIconUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(FontLocalTable.FONT_ICON_URL))));
        font.setStrIconPath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(FontLocalTable.FONT_ICON_PATH))));
        Font fd =getFontByJason( StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(FontLocalTable.FONT_JSON))));
        font.setFont(fd);
        String previewUrl = cursor.getString(cursor.getColumnIndex(FontLocalTable.FONT_PREVIEW_URL));
        if (!TextUtils.isEmpty(previewUrl)) {
            String[] s = previewUrl.split(";");
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < s.length; i++) {
                list.add(s[i]);
            }
            font.setArrPreviewUrl(list);
        }
        String previewPath = cursor.getString(cursor.getColumnIndex(FontLocalTable.FONT_PREVIEW_PATH));
        if (!TextUtils.isEmpty(previewPath)) {
            String[] s = previewPath.split(";");
            List<String> listPath = new ArrayList<String>();
            for (int i = 0; i < s.length; i++) {
                listPath.add(s[i]);
            }
            font.setArrPreviewPath(listPath);
        }
        font.setStrFontUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(FontLocalTable.FONT_URL))));
        font.setStrFontPath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(FontLocalTable.FONT_PATH))));
        font.setLongUpdateTime(cursor.getLong(cursor.getColumnIndex(FontLocalTable.FONT_UPDATETIME)));
        font.setLongLocalTime(cursor.getLong(cursor.getColumnIndex(FontLocalTable.FONT_LOCALTIME)));
        return font;
    }
    public static class FontStatus {
        public int statusCode;
        public long downloadedBytes;
        public long totalBytes;
    }
    private boolean isDefaultFont(NqFont font) {
    	Log.i("FontCurrent","DEFAULT_FONT " + DEFAULT_FONT +" font.getStrId :" + font.getStrId());
        return font != null && DEFAULT_FONT_ID.equals(font.getStrId());
    }
    private boolean isDefaultFont(String fontPath){
    	return DEFAULT_FONT.equals(fontPath);
    };
    public boolean isCurrentFont(NqFont font) {

        String currentFontId = getCurrentFontID();
        if ("".equals(currentFontId)) {
            if (isDefaultFont(font)) {// 默认字体

                return true;
            } else {

                return false;
            }
        }

        return currentFontId.equals(font.getStrId());
    }

    public boolean isCurrentFont(String fontId,String fontPath){
        String currentFontId = getCurrentFontID();
        if ("".equals(currentFontId)) {
            if (isDefaultFont(fontPath)) {// 默认字体
                return true;
            } else {
                return false;
            }
        }
        return currentFontId.equals(fontId);
    }
    
    public FontStatus getStatus(NqFont font) {
    	FontStatus result = new FontStatus();
        result.statusCode = STATUS_UNKNOWN;

        if (font == null) {
            return result;
        }

        if (isCurrentFont(font)) {
            result.statusCode = STATUS_CURRENT_FONT;
        } else {
            MyDownloadManager downloader = MyDownloadManager.getInstance(mContext);
            Long downloadId = downloader.getDownloadId(font.getStrFontUrl());
            int[] bytesAndStatus = downloader.getBytesAndStatus(downloadId);
            if (bytesAndStatus[0] == 1) {
                result.statusCode = convertStatus(bytesAndStatus[1]);
                if(result.statusCode == STATUS_DOWNLOADED) {
                	String filePath = font.getStrFontPath();
                	if (!TextUtils.isEmpty(filePath)){
                        File file = new File(filePath);
                        if (!file.exists()){
                        	result.statusCode = STATUS_NONE;
                        }
                    }
                }
                result.downloadedBytes = bytesAndStatus[2];
                result.totalBytes = bytesAndStatus[3];
            } else {
                result.statusCode = STATUS_NONE;// 未下载
            }
        }

        return result;
    }

    public String getCurrentFontID() {
        return FontPreference.getInstance().getCurrentFontId();
    }

    public void setCurrentFontID(String fontId) {
    	FontPreference.getInstance().setCurrentFontId(fontId);
    }
    private int convertStatus(int status) {
        int result = STATUS_UNKNOWN;

        switch (status) {
            case MyDownloadManager.STATUS_SUCCESSFUL:
                result = STATUS_DOWNLOADED;
                break;
            case MyDownloadManager.STATUS_RUNNING:
                result = STATUS_DOWNLOADING;
                break;
            case MyDownloadManager.STATUS_PENDING:
                result = STATUS_DOWNLOADING;
                break;
            case MyDownloadManager.STATUS_FAILED:
                result = STATUS_NONE;
                break;
            case MyDownloadManager.STATUS_NONE:
                result = STATUS_NONE;
                break;
            case MyDownloadManager.STATUS_PAUSED:
                result = STATUS_PAUSED;
                break;
            default:
                break;
        }

        return result;
    }

    public void registerDownloadObserver(NqFont font, IDownloadObserver observer) {
        if (font == null || observer == null) {
            return;
        }

        MyDownloadManager downloader = MyDownloadManager.getInstance(mContext);
        Long downloadId = downloader.getDownloadId(font.getStrFontUrl());
        downloader.registerDownloadObserver(downloadId, observer);
    }
    public void unregisterDownloadObserver(NqFont font) {
        if (font == null) {
            return;
        }

        MyDownloadManager downloader = MyDownloadManager.getInstance(mContext);
        Long downloadId = downloader.getDownloadId(font.getStrFontUrl());
        downloader.unregisterDownloadObserver(downloadId);
    }
    /**
     * 根据id 获取Font 详情。
     *
     * @param id
     * @param listener
     */
    public void getFontDetail(String id, final FontDetailListener listener) {
    	NqLog.i("fix-me:need an implementaion for service factory. ");
//    	FontServiceFactory.getService().getFontDetail(mContext, id, listener);
    }
    /**
     * 应用字体
     *
     * @param font
     */
    public boolean applyFont(NqFont font) {
		boolean result = false;
		String destPath ="";

		String fontPath = font.getStrFontPath();
		if (font == null || TextUtils.isEmpty(fontPath)) {
			//缺省字体没有fontPath
			NqLog.i("缺省字体设置");
			String currentfontbackup = mContext.getFilesDir() + "/" + CURRENT_FONT_BACKUP;
			FileUtil.delFile(currentfontbackup);
		}else{
			//从字体apk文件中解压ttf文件
	        destPath = fontPath.substring(0, fontPath.lastIndexOf(".")) + ".ttf";
			
			try {
				int ret = FileUtil.upZipSingleFile(new File(fontPath), "fonts/", destPath);
			    //复制字体
			    backupFontToData(destPath);
			} catch (IOException e) {
				NqLog.e(e);
				return false;
			}
		}

	    //设置当前字体
	    setCurrentFont(destPath);
	    setCurrentFontID(font.getStrId());

		// 发广播给桌面，重启手机应用新字体
	    Intent i = new Intent();
	    i.setAction(LiveReceiver.ACTION_CHANGE_FONT);
	    mContext.sendBroadcast(i);
	    LauncherSDK.getInstance(mContext).onPreRestoreDefault();
	    result = true;
		return result;
	}
    
    /**
     * 设置当前字体ttf文件路径
     * @param fontPath
     */
    public void setCurrentFont(String fontPath) {
    	FontPreference.getInstance().setCurrentFont(fontPath);
    }
    
    /**
     * 获取当前字体ttf文件路径
     * @param fontPath
     */
    public String getCurrentFont() {
    	String currentFont = FontPreference.getInstance().getCurrentFont();
    	if (TextUtils.isEmpty(currentFont)){
    		return "";
    	}
    	
    	File currentFontFile = new File(currentFont);
    	String currentfontbackup = mContext.getFilesDir() + "/" + CURRENT_FONT_BACKUP;
		File currentBackupFontFile = new File(currentfontbackup);
    	if (currentFontFile.exists()){
    		return currentFont;
    	}else if (currentBackupFontFile.exists()) {
			return currentfontbackup;
		} else {
			return "";
		}
    }
    
	public Long downloadFont(NqFont font) {
	    Long result = null;
	
	    if (font == null || TextUtils.isEmpty(font.getStrFontUrl())
	            || DEFAULT_FONT.equals(font.getStrFontPath())) {
	        return result;
	    }
	
	    if (!NetworkUtils.isConnected(mContext)) {
	    	ToastUtils.toast(mContext, "nq_nonetwork");
	        return result;
	    }
	
	    try {
	        Long downloadId = MyDownloadManager.getInstance(mContext).downloadFont(font);
	        if (downloadId != null) {
	            saveFont(font);
	            result = downloadId;
	        }
	    } catch (Exception e) {
	        NqLog.e(e);
	    }
	
	    return result;
	}
    private void saveFont(final NqFont font) {
        (new Thread() {
            @Override
            public void run() {
                try {
                    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                    Builder deleteOp = ContentProviderOperation.newDelete(
                            FontLocalTable.LOCAL_FONT_URI).withSelection(
                            FontLocalTable.FONT_ID + " = ?",
                            new String[]{font.getStrId()});
                    ops.add(deleteOp.build());

                    font.setLongLocalTime(System.currentTimeMillis());
                    ContentValues values = fontToContentValues(-1, font);
                    Builder insertOp = ContentProviderOperation.newInsert(
                            FontLocalTable.LOCAL_FONT_URI).withValues(values);
                    ops.add(insertOp.build());

                    mContext.getContentResolver().applyBatch(
                    		DataProvider.DATA_AUTHORITY, ops);

                    saveImages(font);
                } catch (Exception e) {
                    NqLog.e(e);
                }
            }
        }).start();
    }
    private boolean saveImages(NqFont font) {
        boolean result = false;
        if (font != null) {
            result = saveImageToFile(font.getStrIconUrl(), font.getStrIconPath());
            List<String> previewUrls = font.getArrPreviewUrl();
            List<String> previewPaths = font.getArrPreviewPath();
            if (previewUrls != null && previewPaths != null && previewUrls.size() == previewPaths.size()) {
                for (int i = 0; i < previewUrls.size(); i++) {
                    result = saveImageToFile(previewUrls.get(i), previewPaths.get(i));
                }
            }
        }
        return result;
    }
    private boolean saveImageToFile(final String srcUrl, final String destFilePath) {
        boolean result = false;
        if (TextUtils.isEmpty(srcUrl)) {
            return result;
        }

        ImageLoader.getInstance(mContext).getImage(srcUrl, new ImageListener() {
            @Override
            public void onErr() {
            }

            @Override
            public void getImageSucc(String url, BitmapDrawable drawable) {
                if (drawable != null) {
                    FileUtil.writeBmpToFile(drawable.getBitmap(), new File(destFilePath));
                }
            }
        });

        return true;
    }
    public ContentValues fontToContentValues(int column, NqFont font) {
        ContentValues values = null;
        if (font != null) {
            values = new ContentValues();
            values.put(FontLocalTable.FONT_ID, font.getStrId());
            values.put(FontLocalTable.FONT_SOURCE_TYPE, font.getIntSourceType());
            values.put(FontLocalTable.FONT_COLUMN, column);
            values.put(FontLocalTable.FONT_NAME, font.getStrName());
            values.put(FontLocalTable.FONT_AUTHOR, font.getStrAuthor());
            values.put(FontLocalTable.FONT_VERSION, font.getStrVersion());
            values.put(FontLocalTable.FONT_SOURCE, font.getStrSource());
            values.put(FontLocalTable.FONT_SIZE, font.getLongSize());
            values.put(FontLocalTable.FONT_DOWNLOAD_COUNT, font.getLongDownloadCount());
            values.put(FontLocalTable.FONT_ICON_URL, font.getStrIconUrl());

            values.put(FontLocalTable.FONT_JSON, Font.toJson(font.getFont()));

            //预览图网址
            StringBuilder previewUrl = new StringBuilder();
            List<String> previewUrls = font.getArrPreviewUrl();
            if (previewUrls != null && previewUrls.size() > 0) {
                for (int j = 0; j < previewUrls.size(); j++) {
                    previewUrl.append(previewUrls.get(j)).append(";");
                }
            }
            if (previewUrl.length() > 1) {
                values.put(FontLocalTable.FONT_PREVIEW_URL, previewUrl.substring(0, previewUrl.length() - 1));
            } else {
                values.put(FontLocalTable.FONT_PREVIEW_URL, "");
            }
            values.put(FontLocalTable.FONT_URL, font.getStrFontUrl());
            values.put(FontLocalTable.FONT_ICON_PATH, font.getStrIconPath());
            //预览图本地路径
            StringBuilder previewPath = new StringBuilder();
            List<String> previewPaths = font.getArrPreviewPath();
            if (previewPaths != null && previewPaths.size() > 0) {
                for (int j = 0; j < previewPaths.size(); j++) {
                    previewPath.append(previewPaths.get(j)).append(";");
                }
            }
            if (previewPath.length() > 1) {
                values.put(FontLocalTable.FONT_PREVIEW_PATH, previewPath.substring(0, previewPath.length() - 1));
            } else {
                values.put(FontLocalTable.FONT_PREVIEW_PATH, "");
            }
            values.put(FontLocalTable.FONT_PATH, font.getStrFontPath());
            values.put(FontLocalTable.FONT_UPDATETIME, font.getLongUpdateTime());
            values.put(FontLocalTable.FONT_LOCALTIME, font.getLongLocalTime());
        }
        return values;
    }
    
	/**
	 * 从json字符串还原Font对象
	 * @param json
	 * @return
	 */
	public Font getFontByJason(String json) {
		return JsonUtil.getFontByJsonStr(json);
	}
	
	//把各个字体名称text设置为各自的字体样式
	public boolean setTypeface(final NqFont font, final TextView tvName, final boolean getdata) {
		Font fd = font.getFont();
		if (fd == null) {
			Log.i("ljc", "font = null");
			return false;
		}
		
		File f = new File(fd.getThumbnailLocalPath());
		if (f.exists()) { // 字体已下载，直接读取typeface
			try {
				Typeface face = Typeface.createFromFile(fd.getThumbnailLocalPath());
	
				tvName.setTypeface(face);
				tvName.setText(fd.getFontName());
			} catch (Exception e) {
				e.printStackTrace();
				tvName.setTypeface(Typeface.DEFAULT);
			}
			return true;
		} else {
			tvName.setTypeface(Typeface.DEFAULT);
			FontCenter.getInstance().getThumbnail(new ThumbnailCallBack() {

				@Override
				public void onUpgrade(String id ,long progress, long total) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onSuccessed(String id ,Typeface typeface) {
					// TODO Auto-generated method stub
					setTypeface(font, tvName, false);
				}

				@Override
				public void onStart(String id ) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onFailed(String id , String msg) {
					// TODO Auto-generated method stub

				}
			}, fd);
		}
		return false;
	}
	
	public boolean isCacheExpired(int column) {
		return true;
	}
	
	public void viewFontDetail(int column, NqFont font) {
        if (font == null) {
            return;
        }

        try {
            Intent intent = null;

            intent = new Intent(mContext, FontDetailActivity.class);
            intent.putExtra(FontDetailActivity.KEY_FONT, font);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);

        } catch (Exception e) {
            NqLog.e(e);
        }
	}
	/**
	 * 初始化字体管家提供的sdk
	 * key为字体管家提供给网秦的固定key
	 */
	public void initFontManager(Application application) {
        //初始化字体管家
        FontCenter.initFontCenter(application, "715e351e5e8e069d",null);
	}
	public void viewFontLocalDetail( NqFont font) {
        if (font == null) {
            return;
        }

        try {
            Intent intent = null;

            intent = new Intent(mContext, FontLocalDetailActivity.class);
            intent.putExtra(FontLocalDetailActivity.KEY_FONT, font);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);

        } catch (Exception e) {
            NqLog.e(e);
        }
	}
	
	public List<NqFont> getOnlineFontListFromLocal(int column) {
		String field = column == 0 ? FontCacheTable.FONT_ISNEW : FontCacheTable.FONT_ISHOT;
		Log.i("ljc","field = " + field);
        Cursor c = null;
        
		List<NqFont> list = new ArrayList<NqFont>();
        try{
            c = mContext.getContentResolver().query(FontCacheTable.FONT_CACHE_URI,null,
            		field + " = 1", null, null);
            while (c != null && c.moveToNext()) {
            	String json = StringUtil.nullToEmpty(c.getString(c.getColumnIndex(FontCacheTable.FONT_JSON)));
            	Font font = getFontByJason(json);
            	if (font != null) {
            		NqFont nqfont = new NqFont(font, mContext);				
    				list.add(nqfont);
            	}
            }
        }catch (Exception e) {
			NqLog.e("getFontListFromLocal error " + e.toString());
            e.printStackTrace();
		}finally {
            if(c != null){
                c.close();
            }
        }	
        
        return list;
	}
	
	public void getFontListFromServer(int column, FontListStoreListener listener) {
		switch (column) {
		case 0:
			getNewestFontList(listener);
			break;
		case 1:
			getHotstFontList(listener);
			break;
		default:
			break;
		}
			 
	}
	
	private void getNewestFontList(FontListStoreListener listener) {
		//检查参数
		if (!isNetworkReady(listener)) {
			return;
		}
		final FontListStoreListener l = listener;
		FontCenter.getInstance().getNewestFontListFromServer(new IHttpCallBack() {
			
			@Override
			public void onSuccess(Object obj) {
				onSuceed(0, l, obj);
			}
			
			@Override
			public void onErr(String err) {
				Log.i("ljc", "getNewestFontListFromServer onErr");
				l.onErr();
			}
		});
    }
	
	private void getHotstFontList(FontListStoreListener listener) {
		//检查参数
		if (!isNetworkReady(listener)) {
			return;
		}
		
		final FontListStoreListener l = listener;
		FontCenter.getInstance().getHotFontListFromServer(new IHttpCallBack() {
			
			@Override
			public void onSuccess(Object obj) {
				onSuceed(1, l, obj);
			}
			
			@Override
			public void onErr(String err) {
				Log.i("ljc", "getHotFontListFromServer onErr");
				l.onErr();
			}
		});
    }
	
	private boolean isNetworkReady(FontListStoreListener listener) {
		if (listener == null){
			return false;
		}

		if (!NetworkUtils.isConnected(mContext)) {
			listener.onNoNetwork();
			return false;
		}
		
		return true;
	}
	
	/**
	 * @param column: 0为最新字体 1为热门字体
	 * @param listener
	 * @param obj
	 */
	private void onSuceed(final int column, FontListStoreListener listener, Object obj) {
		if (obj == null) {
			listener.onErr();
			return;
		}
		List<Font> list = FontCenter.getInstance().getFonts((List<Font>) obj);
		FontCenter.getInstance().getFontsAndLocal(list);
		if (list == null) {
			listener.onErr();
		} else {
			List<NqFont> fontList = new ArrayList<NqFont>();
			for (Font font: list) {
				Log.i("ljc", "font download image= " + font.getDownloadUr());
				NqFont nqFont = new NqFont(font, mContext);				
				fontList.add(nqFont);
			}
			listener.onGetFontListSucc(column, fontList);
			saveOnlineFont(column, fontList);
		} 
	}
	
	public void onEvent(DownloadCompleteEvent event) {
		processFontDownload(mContext, event.mRefer);
	}
	
	private void processFontDownload(Context context,long refer){
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(DownloadTable.TABLE_URI, null,
                    DownloadTable.DOWNLOAD_DOWNLOAD_ID + " = " + refer, null,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                int type = cursor.getInt(cursor.getColumnIndex(DownloadTable.DOWNLOAD_TYPE));
                String resId = cursor.getString(cursor.getColumnIndex(DownloadTable.DOWNLOAD_RES_ID));
                String destPath = cursor.getString(cursor.getColumnIndex(DownloadTable.DOWNLOAD_DEST_PATH));
                if (FileUtil.isFileExists(destPath) && !TextUtils.isEmpty(resId)){//确保文件存在
                    switch(type) {
                        case MyDownloadManager.DOWNLOAD_TYPE_FONT:
                            StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
                            		AppActionConstants.ACTION_LOG_1502, resId, StatManager.ACTION_DOWNLOAD,
                                    "4_1");
                            break;
                    }
                }
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
			CursorUtils.closeCursor(cursor);
        }
    }
	
	
	private void saveOnlineFont(int column, List<NqFont> list) {
		ContentValues values = null;
		ContentResolver contentResolver = mContext.getContentResolver();
    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
    	
    	String field = column == 0 ? FontCacheTable.FONT_ISNEW : FontCacheTable.FONT_ISHOT;
        try {
        	// 先删除本地所有在线字体
//        	Builder b = ContentProviderOperation.newDelete(FontCacheTable.FONT_CACHE_URI);
//            ops.add(b.build());
            
            // 添加新的在线字体
    		for (NqFont font : list) {
    			if (!isInFontTable(font.getStrId())) {
    	            values = new ContentValues();   
    	    		values.put(FontCacheTable.FONT_ID, font.getStrId());
    	    		values.put(FontCacheTable.FONT_NAME, font.getStrName());
    	    		values.put(field, 1);
    	    		values.put(FontCacheTable.FONT_JSON, Font.toJson(font.getFont()));
    	            Builder insert = ContentProviderOperation.newInsert(FontCacheTable.FONT_CACHE_URI).withValues(values);
    	            ops.add(insert.build());
    			} else {
    	            values = new ContentValues();   
    	    		values.put(field, 1);
	                Builder update = ContentProviderOperation.newUpdate(FontCacheTable.FONT_CACHE_URI)
		                      .withValues(values)
		                      .withSelection(FontCacheTable.FONT_ID + " =?", 
		                    		  new String[]{font.getStrId()});
			        ops.add(update.build());
            	}
    		}

            contentResolver.applyBatch(DataProvider.DATA_AUTHORITY,ops);                	
		} catch (Exception e) {
			NqLog.e("saveOnlineFont error " + e.toString());
            e.printStackTrace();
		}		
	}
	
	private boolean isInFontTable(String font_id) {
		boolean result = false;
        Cursor c = null;
        try{
            c = mContext.getContentResolver().query(FontCacheTable.FONT_CACHE_URI,null,
            		FontCacheTable.FONT_ID + " =?" , new String[]{font_id}, null);
            if (c != null && c.moveToNext()) {
            	result = true;
            }

		}catch (Exception e) {
			NqLog.e("getFontListFromLocal error " + e.toString());
	        e.printStackTrace();
		}finally {
	        if(c != null){
	            c.close();
	        }
        }
        
        return result;
    }	
}