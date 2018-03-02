package com.nqmobile.livesdk.modules.gamefolder_v2;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.text.TextUtils;

import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.FileUtil;

public class AudioManager {
	static final String TAG = "AudioManager";
	Context context;
	private static AudioManager mInstance;

	private AudioManager(Context context) {
		this.context = context.getApplicationContext();
	}

	public synchronized static AudioManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new AudioManager(context.getApplicationContext());
		}
		return mInstance;
	}

	public void getAudio(String path, String url, AudioListener listener) {
		if (url == null) {
			listener.onErr();
			return;
		}

		new Thread(new AudioFetcher(context, url, path, listener)).start();
	}

	public class AudioFetcher implements Runnable {
		private static final int CONNECT_TIMEOUT = 10*1000;
	    private static final int READ_TIMEOUT = 10*1000;

	    private Context mContext;
		private String mUrl;
		private String mPath;
		private AudioListener mListener;

		public AudioFetcher(Context mContext, String mUrl, String mPath,
				AudioListener mListener) {
			this.mContext = mContext.getApplicationContext();;
			this.mUrl = mUrl;
			this.mPath = mPath;
			this.mListener = mListener;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			boolean succ = download(mUrl, mPath);
			if (succ) {
				mListener.getAudioSucc(mUrl, mPath);
			} else {
				mListener.onErr();
			}
		}

		private boolean download(String url, String path) {
			boolean result = false;
			InputStream is = null;

			HttpURLConnection conn = null;
			try {
				URL audioUrl = new URL(url);
				conn = (HttpURLConnection) audioUrl.openConnection();
				conn.setConnectTimeout(CONNECT_TIMEOUT);
				conn.setReadTimeout(READ_TIMEOUT);
				conn.setInstanceFollowRedirects(true);
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					is = conn.getInputStream();
				}
				if (is == null) {
					throw new RuntimeException("stream is null");
				} else {
					result = saveAudioFile(path, is);
	                is.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}

			return result;
		}
	}

	/**
	 * 得到字节流 数组大小
	 * */
	public static byte[] readStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		outStream.close();
		inStream.close();
		return outStream.toByteArray();
	}

	/**
	 * 缓存音频。
	 * 
	 * @param path
	 * @param bmp
	 * @return
	 */
	public boolean saveAudioFile(String path, InputStream inStream) {
		if (!CommonMethod.isSDCardOK(context) || TextUtils.isEmpty(path)
				|| inStream == null) {
			return false;
		}
		boolean result = false;
		try {
			result = FileUtil.writeStreamToFile(path, inStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
}
