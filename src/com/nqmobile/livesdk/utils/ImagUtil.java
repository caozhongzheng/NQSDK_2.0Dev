package com.nqmobile.livesdk.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * 图片工具类
 * @author changxiaofei
 * @time 2013-12-7 下午6:23:20
 */
public class ImagUtil {
	private static final String TAG = "ImagUtil";
	
	/**
	 * Drawable转Bitmap
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = null;
		try {
			if (drawable.getIntrinsicWidth() > 0 && drawable.getIntrinsicHeight() > 0) {
				bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
				Canvas canvas = new Canvas(bitmap);
				drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
				drawable.draw(canvas);
			}
		} catch (Exception e) {
			Log.e(TAG, "drawableToBitmap error " + e.toString());
		}
		return bitmap;
	}
	
	/**
	 * Bitmap转byte[]
	 * @param bm
	 * @return
	 */
	public static byte[] Bitmap2Bytes(Bitmap bm) {
		byte[] mByte = null;
		try {
			if (bm != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
				mByte = baos.toByteArray();
			}
		} catch (Exception e) {
			Log.e(TAG, "Bitmap2Bytes error " + e.toString());
		}
		return mByte;
	}
	
	/**
	 * byte[]转Bitmap
	 * @param b
	 * @return
	 */
	public static Bitmap Bytes2Bimap(byte[] b) {
		try {
			if (b.length != 0) {
				return BitmapFactory.decodeByteArray(b, 0, b.length);
			} else {
				return null;
			}
		} catch (Exception e) {
			Log.e(TAG, "Bytes2Bimap error " + e.toString());
			return null;
		}
	}
	
	/**
	 * Bitmap转Drawable
	 * @param bimap
	 * @return
	 */
	public static Drawable bimap2Drawable(Resources res, Bitmap bimap) {
		Drawable drawable = null;
		try {
			if (bimap != null) {
				drawable = new BitmapDrawable(res, bimap);
			}
		} catch (Exception e) {
			Log.e(TAG, "bimap2Drawable error " + e.toString());
			return null;
		}
		return drawable;
	}
	
	/**
	 * 按正方形裁切图片。测试
	 * @param bitmap
	 * @return
	 */
	public static Bitmap ImageCrop(Bitmap bitmap) {
		int w = bitmap.getWidth(); // 得到图片的宽，高
		int h = bitmap.getHeight();
		int wh = w > h ? h : w;// 裁切后所取的正方形区域边长
		int retX = w > h ? (w - h) / 2 : 0;// 基于原图，取正方形左上角x坐标
		int retY = w > h ? 0 : (h - w) / 2;
		// 下面这句是关键
		return Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null, false);
	}
	
	/**
	 * 质量压缩
	 * @param image
	 * @return
	 */
	public static Bitmap compressImage(Bitmap image) {
		Bitmap bitmap = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
			int options = 100;
			while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
				baos.reset();// 重置baos即清空baos
				image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
				options -= 10;// 每次都减少10
			}
			ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
			bitmap = BitmapFactory.decodeStream(isBm, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	/**
	 * 缩放Bitmap
	 * @param image
	 * @param newW
	 * @param newH
	 * @return
	 */
	public static Bitmap zoomBitmap(Bitmap image, float newW, float newH) {
		Bitmap bitmap = null;
		ByteArrayInputStream isBm = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			if (baos.toByteArray().length / 1024 > 1024) {// 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
				baos.reset();// 重置baos即清空baos
				image.compress(Bitmap.CompressFormat.JPEG, 50, baos);// 这里压缩50%，把压缩后的数据存放到baos中
			}
			isBm = new ByteArrayInputStream(baos.toByteArray());
			BitmapFactory.Options newOpts = new BitmapFactory.Options();
			// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
			newOpts.inJustDecodeBounds = true;
			bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
			isBm.close();
			newOpts.inJustDecodeBounds = false;
			int w = newOpts.outWidth;
			int h = newOpts.outHeight;
			float hh = 800f;// 这里设置高度为800f
			float ww = 480f;// 这里设置宽度为480f
			// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
			int be = 1;// be=1表示不缩放
			if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
				be = (int) (newOpts.outWidth / ww);
			} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
				be = (int) (newOpts.outHeight / hh);
			}
			if (be <= 0) {
				be = 1;
			}
			newOpts.inSampleSize = be;// 设置缩放比例
			// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
			isBm = new ByteArrayInputStream(baos.toByteArray());
			bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
			bitmap = compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (isBm != null){
				try {
					isBm.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}
	
	/**
	 * 缩放
	 * @param bm
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	public static Bitmap scaleBitmap(Bitmap bm, int newWidth, int newHeight) {
		Bitmap tempBitmap = cropAsTargetScale(bm, newWidth, newHeight);
		
		int width = tempBitmap.getWidth();
		int height = tempBitmap.getHeight();
		// 设置想要的大小
		int newWidth1 = newWidth;
		int newHeight1 = newHeight;
		// 计算缩放比例
		float scaleWidth = ((float) newWidth1) / width;
		float scaleHeight = ((float) newHeight1) / height;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// 得到新的图片
		Bitmap result = Bitmap.createBitmap(tempBitmap, 0, 0, width, height, matrix, true);
		tempBitmap.recycle();
		
		return result;
	}
	
	/**
	 * 按目标宽高比裁切图片
	 * @param bitmap
	 * @return
	 */
	private static Bitmap cropAsTargetScale(Bitmap bitmap, int newWidth,
			int newHeight) {
		Bitmap result = null;

		int src_width = bitmap.getWidth();
		int src_height = bitmap.getHeight();
		// 裁剪，裁剪左右两边或上下两边，保留中间
		int sub_width = Math.min(src_width,
				Math.round(newWidth * src_height / newHeight));
		int sub_height = Math.min(src_height,
				Math.round(newHeight * src_width / newWidth));
		result = Bitmap.createBitmap(bitmap,
				Math.round((src_width - sub_width) / 2),
				Math.round((src_height - sub_height) / 2), sub_width,
				sub_height);
		
		return result;
	}
}