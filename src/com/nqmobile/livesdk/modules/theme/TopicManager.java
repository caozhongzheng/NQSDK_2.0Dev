package com.nqmobile.livesdk.modules.theme;

import android.content.Context;
import android.content.Intent;

/**
 * 壁纸业务类
 *
 * @author changxiaofei
 * @time 2013-12-5 下午3:19:07
 */
public class TopicManager{
    public static final int COLUMN_PICKS = 0;
    public static final int COLUMN_TOP_GAMES = 1;
    public static final int COLUMN_TOP_APPS = 2;
    public static final long CACHE_MAX_TIME = 24L * 60 * 60 * 1000;//24小时
    private Context mContext;
    private static TopicManager mInstance;

    public static final int STATUS_UNKNOWN = -1;//未知
    public static final int STATUS_NONE = 0;//未下载
    public static final int STATUS_DOWNLOADING = 1;//下载中
    public static final int STATUS_PAUSED = 2;//下载暂停中
    public static final int STATUS_DOWNLOADED = 3;//已下载
    public static final int STATUS_CURRENT_WALLPAPER = 4;//当前壁纸


	public TopicManager(Context context) {
        super();
        this.mContext = context;
    }

    public synchronized static TopicManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TopicManager(context);
        }
        return mInstance;
    }

    
	public void goTopicDetail(Topic topic) {
		if(topic == null)
			return;
		Intent intent = new Intent(mContext, TopicDetailACT.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(TopicDetailACT.KEY_TOPIC, topic);
		mContext.startActivity(intent);
	}
	
}
