/**
 * 
 */
package com.nqmobile.livesdk.commons.image;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nqmobile.livesdk.commons.concurrent.Priority;
import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.utils.Tools;

/**
 * @author chenyanmin
 * @time 2014-2-16 上午1:15:54
 */
public class ImageFetcher implements Runnable, Priority {
    private static final int CONNECT_TIMEOUT = 10*1000;
    private static final int READ_TIMEOUT = 10*1000;
    
    private Context mContext;
	private String mUrl;    	
	private ImageListener mListener;	
	private ImageCache mImageCache;
	
	public ImageFetcher(Context context, String url, ImageCache imageCache, ImageListener listener){
		mContext = context.getApplicationContext();
		mUrl = url;
		mListener = listener;
		mImageCache = imageCache;
	}
	
	@Override
	public void run() {
		try {
			int[] mWH = null;
			Bitmap bmp = mImageCache.getBitmapFromDiskCache(mUrl, mWH);
			if (bmp == null) {
				bmp = download(mUrl);
			}
			if (bmp != null) {
				RecyclingBitmapDrawable drawable = new RecyclingBitmapDrawable(
						mContext.getResources(), bmp);
				drawable.setIsCached(true);//先计数加1，保证调用addBitmapToCache前，图片不被回收
				try {
					mListener.getImageSucc(mUrl, drawable);
					mImageCache.addBitmapToCache(mUrl, drawable);
				} finally {
					drawable.setIsCached(false);
				}
			} else {
				mListener.onErr();
			}
		} catch(Exception e) {
			NqLog.e(e);
		}
		finally {
			mListener = null;
		}
	}
	
	private Bitmap download(String url) {
		// add by renys start for java.net.MalformedURLException:
		if(Tools.isEmpty(url)){
			return null;
		}
		// add by renys end 
		Bitmap result = null;
		
		HttpURLConnection conn = null;  
		InputStream is = null;
        try {  
            URL imageUrl = new URL(url);  
            conn = (HttpURLConnection) imageUrl.openConnection();  
            conn.setConnectTimeout(CONNECT_TIMEOUT);  
            conn.setReadTimeout(READ_TIMEOUT);  
            conn.setInstanceFollowRedirects(true);  
            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
            } else {
                is = null;
            }
            if (is == null){
                throw new RuntimeException("stream is null");
            } else {
                try {
                    byte[] data = readStream(is);
                    if(data != null){
                    	result = BitmapFactory.decodeByteArray(data, 0, data.length);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally{
                    is.close();
                }
            }
//            is = conn.getInputStream(); 
//            BufferedInputStream bis = new BufferedInputStream(is);
//            result = decodeStream(bis);
//            bis.close();
        } catch (Exception ex) {  
            ex.printStackTrace();  
        } finally{
        	if (conn != null){
        		conn.disconnect();
        	}
        }

        return result;
	}
	
	/**
     * 得到图片字节流 数组大小
     * */
    public static byte[] readStream(InputStream inStream) throws Exception{      
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();      
        byte[] buffer = new byte[1024];      
        int len = 0;      
        while( (len=inStream.read(buffer)) != -1){      
            outStream.write(buffer, 0, len);      
        }    
        
        byte[] result = outStream.toByteArray();  
        outStream.close();
        return result;      
    }
    
	private Bitmap decodeStream(InputStream stream) {
		return BitmapFactory.decodeStream(stream);
	}

	/* (non-Javadoc)
	 * @see com.nqmobile.live.common.concurrent.Priority#getPriority()
	 */
	@Override
	public int getPriority() {
		return Priority.PRIORITY_DEFAULT;
	}  

}
