package com.nqmobile.livesdk.commons.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.nqmobile.livesdk.utils.MResource;

/**
 * Created by Rainbow on 14-6-6.
 */
public class CircleProgressDownloadView extends View{
    private int maxProgress = 100;
    private int progress = 0;
    private int progressStrokeWidth = 8;
    //画圆所在的距形区域
    private RectF oval;
    private Paint paint;
    private Context mContext;
    //private Bitmap mDownloadBm;
    private int width;
    private int height;
	private Paint mRimPaint = new Paint();
	private Matrix matrix = new Matrix();
	private Paint mProgressTitPaint = new Paint();
    private Rect mPercentBounds = new Rect();
	private Rect mProgBounds = new Rect();

    public CircleProgressDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        oval = new RectF();
        paint = new Paint();

        //mDownloadBm = BitmapFactory.decodeResource(mContext.getResources(), MResource.getIdByName(mContext,"drawable","nq_download_icon"));//图片

		int mRimColor = context.getResources().getColor(MResource.getIdByName(getContext(), "color", "nq_default_rim_color"));
        mRimPaint = new Paint();
        mRimPaint.setColor(mRimColor);
        mRimPaint.setAntiAlias(true);
        mRimPaint.setStyle(Style.STROKE);
        mRimPaint.setStrokeWidth(progressStrokeWidth);

        paint.setAntiAlias(true);
        paint.setStrokeWidth(progressStrokeWidth); //线宽
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(context.getResources().getColor(MResource.getIdByName(getContext(), "color", "nq_circle_color")));
        
        mProgressTitPaint.setColor(mRimColor);
        mProgressTitPaint.setTextSize(26);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(!isShow)
        	return;
        width = this.getWidth();
        height = this.getHeight();

        if (width != height) {
            int min = Math.min(width, height);
            width = min;
            height = min;
        }

        /*int w = mDownloadBm.getWidth();
        int h = mDownloadBm.getHeight();
        float x = (float) width / w;
        float y = (float) height / h;
        matrix.setScale(x, y);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.drawBitmap(mDownloadBm, matrix, paint);*/

        oval.left = progressStrokeWidth / 2; // 左上角x
        oval.top = progressStrokeWidth / 2; // 左上角y
        oval.right = width - progressStrokeWidth / 2; // 左下角x
        oval.bottom = height - progressStrokeWidth / 2; // 右下角y
        
        canvas.drawArc(oval, 360, 360, false, mRimPaint);
        canvas.drawArc(oval, -90, ((float) progress / maxProgress) * 360, false, paint); // 绘制进度圆弧，这里是蓝色
        
        mProgressTitPaint.getTextBounds(progressTit, 0, progressTit.length(), mProgBounds);
        mProgressTitPaint.getTextBounds("%", 0, 1, mPercentBounds);
        float offset = 
                (mProgBounds.width() + mPercentBounds.width() + mPercentBounds.width() / 2) / 2;
       canvas.drawText(progress + "%", 
                this.getWidth() / 2 - offset, 
                this.getHeight() / 2 + mProgBounds.height() / 2, 
                mProgressTitPaint);
    }

    String progressTit = "";
    public void setProgress(int progress) {
        this.progress = progress;
        progressTit = progress > 0 ? String.valueOf(progress) : "";
        this.invalidate();
    }
    
    boolean isShow = false;
    @Override
    public void setVisibility(int visibility) {
    	super.setVisibility(visibility);
    	isShow = visibility == View.VISIBLE;
    }
}
