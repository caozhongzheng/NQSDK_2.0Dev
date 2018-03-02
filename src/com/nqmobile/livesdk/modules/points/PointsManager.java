package com.nqmobile.livesdk.modules.points;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.nq.interfaces.userinfo.TPointsChangeInfo;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.receiver.PackageAddedEvent;
import com.nqmobile.livesdk.commons.service.PeriodCheckEvent;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.points.model.ConsumeListResp;
import com.nqmobile.livesdk.modules.points.model.PointsInfo;
import com.nqmobile.livesdk.modules.points.model.RewardPointsResp;
import com.nqmobile.livesdk.modules.points.model.WidgetResource;
import com.nqmobile.livesdk.modules.points.network.ConsumeListProtocol;
import com.nqmobile.livesdk.modules.points.network.ConsumePointsProtocol;
import com.nqmobile.livesdk.modules.points.network.GetAppListProtocol;
import com.nqmobile.livesdk.modules.points.network.GetPointsProtocol;
import com.nqmobile.livesdk.modules.points.network.GetThemeListProtocol;
import com.nqmobile.livesdk.modules.points.network.PointServiceFactory;
import com.nqmobile.livesdk.modules.points.network.RewardPointsProtocol;
import com.nqmobile.livesdk.modules.points.table.PointResourceTable;
import com.nqmobile.livesdk.modules.points.table.RewardPointsTable;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.theme.ThemeDetailActivity;
import com.nqmobile.livesdk.modules.theme.ThemeManager;
import com.nqmobile.livesdk.modules.theme.table.ThemeCacheTable;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NotificationUtil;
import com.nqmobile.livesdk.utils.PackageUtils;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;


/**
 * Created by Rainbow on 14-5-7.
 */
public class PointsManager extends AbsManager{
	private static final ILogger NqLog = LoggerFactory.getLogger(PointModule.MODULE_NAME);

    private Context context;
    private static PointsManager mInstance;
    private PreferenceDataHelper mHelper;
    private static final int CODE_SUCC = 0;

    private static final int STATE_OPEN = 0; //打开获取积分
    private static final int STATE_INSTALL = 1; //已下载未安装
    private static final int STATE_DOWNLOADING = 2; //下载中
    private static final int STATE_DOWNLOAD = 3; //未下载
    private static final int STATE_GET_POINT = 4;   //已获取积分
    
    public static final int FLAG_NOT_REWARD = 0;
    public static final int FLAG_IS_REWARD = 1;
    public static final int FLAG_REWARD_FAIL = 2;
    
    private static final int FLAG_DOWNLOAD_FROM_POINT_CENTER = 1;
    
    public static final int FROM_NOTI = 1;
    
    public static final int COLUMN_POINT_APP = 110;
    public static final int COLUMN_POINT_THEME = 5;
    public static final int COLUMN_POINT_APP_CONSUME = 111;

    public static final int CONSUME_STATE_SUCC = 0;
    public static final int CONSUME_STATE_ALREADY_HAVE = 1;
    public static final int CONSUME_STATE_NOT_ENOUGH = 2;
    public static final int CONSUME_STATE_FAIL = 3;

    public static final int CONSUME_HISTORY_STATE_FREE_DOWNLOAD = 0;
    public static final int CONSUME_HISTORY_STATE_APPLY = 1;

    public static final int POINT_FLAG_NOT_POINT_THEME = 0;
    public static final int POINT_FLAG_CONSUME = 1;
    public static final int POINT_FLAG_NOT_CONSUME = 2;
    
    private static final String SCENE_POINT_CENTER = "1000";

    private PointsManager(Context context) {
        this.context = context;
        mHelper = PreferenceDataHelper.getInstance(context);

        EventBus.getDefault().register(this);
    }

    public synchronized static PointsManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PointsManager(context.getApplicationContext());
        }
        return mInstance;
    }

    public void getPoints(GetPointsListener listener){
        NqLog.i("getPoints");
        PointServiceFactory.getService().getPoints(listener);
    }

    private void showGetPointsNoti(int points){
        Intent i = new Intent(context, PointsCenterActivity.class);
        i.putExtra("from", FROM_NOTI);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        NotificationUtil.showNoti(context, MResource.getIdByName(context, "drawable", "nq_noti_get_point"),
                context.getString(MResource.getIdByName(context, "string", "nq_point_get_point")),
                context.getString(MResource.getIdByName(context, "string", "nq_get_points"), String.valueOf(points)),
                i,
                NotificationUtil.NOTIF_ID_GET_POINT);
    }

    public void checkShowExPointNoti(){
        long extime = mHelper.getLongValue(PointsPreference.KEY_EXPERID_TIME);
        long pointextime = extime - System.currentTimeMillis();
        if(pointextime <= 7 * DateUtils.DAY_IN_MILLIS && pointextime >= 0){
            if(mHelper.getBooleanValue(PointsPreference.KEY_SHOW_EX_POINT_TIP)){
                long expoints = mHelper.getIntValue(PointsPreference.KEY_EXPERID_POINTS);
                Intent i = new Intent(context, PointsCenterActivity.class);
                i.putExtra("from", PointsManager.FROM_NOTI);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                mHelper.setBooleanValue(PointsPreference.KEY_SHOW_EX_POINT_TIP, false);

                NotificationUtil.showNoti(context, MResource.getIdByName(context, "drawable", "nq_noti_ex_point"),
                        context.getString(MResource.getIdByName(context, "string", "nq_ex_points")),
                        context.getString(MResource.getIdByName(context, "string", "nq_ex_points_tip"), String.valueOf(expoints)),
                        i,
                        NotificationUtil.NOTIF_ID_EX_POINT);
            }
        }
    }
    
    private void checkShowIntoThemeNoti(int points, String themeid, int downid){
    	Intent i = new Intent(context, ThemeDetailActivity.class);
    	i.putExtra(ThemeDetailActivity.KEY_THEME_ID, themeid);
    	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	i.setAction(ThemeDetailActivity.INTENT_ACTION);
    	
    	NotificationUtil.showNoti(context, MResource.getIdByName(context, "drawable", "nq_noti_get_point"),
                context.getString(MResource.getIdByName(context, "string", "nq_point_get_point")),
                context.getString(MResource.getIdByName(context, "string", "nq_get_points"), String.valueOf(points)),
                i,
                NotificationUtil.NOTIF_ID_GET_POINT);
    }

    private void processPointInfo(PointsInfo info){
        if(info == null){
            return;
        }

        NqLog.i("processPointInfo info.point="+info.totalPoints);
        long nowextime = mHelper.getLongValue(PointsPreference.KEY_EXPERID_TIME);
        if(info.expireTime != nowextime){
            mHelper.setBooleanValue(PointsPreference.KEY_NEED_SHOW_EXPOINT_TIP,true);
            mHelper.setBooleanValue(PointsPreference.KEY_SHOW_EX_POINT_TIP,true);
        }

        mHelper.setIntValue(PointsPreference.KEY_USER_POINTS, info.totalPoints);
        mHelper.setIntValue(PointsPreference.KEY_EXPERID_POINTS, info.expirePoints);
        mHelper.setLongValue(PointsPreference.KEY_EXPERID_TIME,info.expireTime);
    }

    public void rewardPoints(final String trackid,final boolean fore,String themeId,int downid,final RewardPointsListener listener){
        NqLog.i("rewardPoints trackid="+trackid);
        RewardPointsTag tag = new RewardPointsTag();
        tag.downid = downid;
        tag.theme = themeId;
        tag.fore = fore;
        tag.trackId = trackid;
        tag.listener = listener;
        PointServiceFactory.getService().rewardPoints(tag);
    }
    
    public TPointsChangeInfo getPointInfo(String trackid){
        NqLog.i("getPointInfo trackid="+trackid);
        TPointsChangeInfo info = null;
        Cursor c = null;
        try{
        	c = context.getContentResolver().query(PointResourceTable.TABLE_URI, null,
                    PointResourceTable.POINT_APP_TRACKID + " = ?", new String[]{trackid}, "_id desc");
            if(c != null && c.moveToNext()){
                info = new TPointsChangeInfo();
                int points = c.getInt(c.getColumnIndex(PointResourceTable.POINT_APP_POINTS));
                info.trackId = trackid;
                info.clientTime = System.currentTimeMillis();
                info.points = points;
                info.resourceId = c.getString(c.getColumnIndex(PointResourceTable.POINT_APP_ID));
                info.scene = SCENE_POINT_CENTER;
            }
        }finally{
        	if(c != null){
                c.close();
            }
        }

        return info;
    }

    public boolean haveGetPointsHistory(){
        Cursor c = context.getContentResolver().query(RewardPointsTable.TABLE_URI,null,null,null,null);
        if(c != null && c.getCount() > 0){
            return true;
        }
        return false;
    }

    /**
     * 标记积分资源获取到积分
     * @param trackid
     */
    public void flagPointResourceGetPoints(String trackid,boolean success){
        ContentValues value = new ContentValues();
        value.put(PointResourceTable.POINT_APP_REWARD_STATE,success ? FLAG_IS_REWARD : FLAG_REWARD_FAIL);
        context.getContentResolver().update(PointResourceTable.TABLE_URI,value,PointResourceTable.POINT_APP_TRACKID + " = ?",new String[]{trackid});
    }

    public void flagPointThemeConsume(String resId){
        ContentValues value = new ContentValues();
        value.put(ThemeCacheTable.THEME_POINTSFLAG,CONSUME_STATE_ALREADY_HAVE);
        context.getContentResolver().update(ThemeCacheTable.THEME_CACHE_URI,value,ThemeCacheTable.THEME_ID + " = ?",new String[]{resId});
    }

    public void checkShowInstallTip(String packagename){
        NqLog.i("PointsManager checkShowInstallTip packageName="+packagename);
        Cursor c = context.getContentResolver().query(PointResourceTable.TABLE_URI,null,
                PointResourceTable.POINT_APP_PACKAGENAME +" = ?",new String[]{packagename},null);
        if(c != null && c.moveToNext()){
            int download = c.getInt(c.getColumnIndex(PointResourceTable.POINT_APP_DOWNLOAD));
            int downloadid = c.getInt(c.getColumnIndex(PointResourceTable.POINT_APP_DOWNLOADID));
            String themeid = c.getString(c.getColumnIndex(PointResourceTable.POINT_APP_FROM_THEME_ID));
            String trackid = c.getString(c.getColumnIndex(PointResourceTable.POINT_APP_TRACKID));
            NqLog.i("download="+download);
            if(download == FLAG_DOWNLOAD_FROM_POINT_CENTER){
                Intent i = new Intent(context, PointOpenNoitifiAppACT.class);
                i.putExtra("packagename", packagename);
                i.putExtra("themeid", themeid);
                i.putExtra("downloadid", downloadid);
                i.putExtra("trackid", trackid);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                NotificationUtil.showNoti(context, MResource.getIdByName(context, "drawable", "nq_noti_open"),
                        context.getString(MResource.getIdByName(context, "string", "nq_point_app_install_complele")),
                        context.getString(MResource.getIdByName(context, "string", "nq_install_complete_tip")),
                        i,
                        downloadid);
            }
        }
    }
    
    /**
     * 获取积分应用列表
     */
    public void getAppList(int column,int offset,AppListListener listener) {
        NqLog.i("getAppList");
        //检查参数
        if (listener == null){
            return;
        }

        GetAppListTag tag = new GetAppListTag();
        tag.column = column;
        tag.listener = listener;
        tag.offset = offset;

        PointServiceFactory.getService().getAppList(tag);
    }

    private List<App> getCachePointResource(){
        List<App> list = new ArrayList<App>();
        App app;
        Cursor c = null;
        try{
        	c = context.getContentResolver().query(PointResourceTable.TABLE_URI,null,PointResourceTable.POINT_APP_REWARD_STATE +" = 0",null,null);
            while(c != null && c.moveToNext()){
                app = AppManager.getInstance(context).cursorToApp(c);
                list.add(app);
            }
        }finally{
        	if(c != null){
                c.close();
            }
        }
        return list;
    }

    private boolean haveRepeat(List<App> list,App app){
        boolean result = false;
        for(App a:list){
            if(a.getStrId().equals(app.getStrId())){
                result = true;
                break;
            }
        }

        return result;
    }

    public int getRewardedState(String resId){
        int result = 0;
        Cursor c = null;
        try{
        	c = context.getContentResolver().query(PointResourceTable.TABLE_URI,null,PointResourceTable.POINT_APP_ID + " = ?",new String[]{resId},null);
            if(c != null && c.moveToNext()){
                result = c.getInt(c.getColumnIndex(PointResourceTable.POINT_APP_REWARD_STATE));
            }
        }finally{
        	if(c != null){
                c.close();
            }
        }

        return result;
    }

    public void launchApp(App app){
    	PackageUtils.launchApp(context, app.getStrPackageName());
        rewardPoints(app.getTrackId(), true, "", 0, null);
    }

    /**
     * 调整得到的列表顺序按照 打开 安装 下载中 未下载的顺序
     * @param list
     */
    private List<App> sortAppList(List<App> list){
        if(list != null){
            NqLog.i("sortAppList size="+list.size());
        }

        ArrayList<AppListItem> applist = new ArrayList<AppListItem>();
        List<App> cacheList = getCachePointResource();
        NqLog.i("cacheList size="+cacheList.size());
        ArrayList<App> result = new ArrayList<App>();
        ArrayList<App> newAddList = new ArrayList<App>();
        for(App app :cacheList){
            AppListItem item = new AppListItem();
            item.mApp = app;
            item.mState = getAppState(app);
            if(item.mState != STATE_GET_POINT){
                applist.add(item);
            }
        }

        Collections.sort(applist, new Comparator<AppListItem>() {
            @Override
            public int compare(AppListItem appListItem, AppListItem appListItem2) {
                if (appListItem.mState > appListItem2.mState) {
                    return 1;
                } else if (appListItem.mState < appListItem2.mState) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        for(AppListItem item:applist){
            result.add(item.mApp);
        }

        if(list != null){
            if(result.size() > 0){
                for(App app:list){
                    if(!haveRepeat(result,app)){
                        newAddList.add(app);
                    }
                }

                NqLog.i("newAddList size="+newAddList.size());
                result.addAll(newAddList);
            }else{
                result.addAll(list);
            }
        }

        for(Iterator<App> i = result.iterator();i.hasNext();){
            App app = i.next();
            if(isInRewardHistory(app.getStrId())){
                i.remove();
            }
        }

        NqLog.i("result size="+result.size());

        return result;
    }

    private boolean isInRewardHistory(String resId){
        boolean result = false;
        Cursor c = null;
        try{
            c = context.getContentResolver().query(RewardPointsTable.TABLE_URI,null,
                    RewardPointsTable.REWARD_POINTS_HISTORY_RESID + " = ?" ,new String[]{resId},null);
            if(c != null && c.getCount() > 0){
                result = true;
            }
        }finally {
            if(c != null){
                c.close();
            }
        }
        return result;
    }

    public int getThemePoints(String resId){
        int result = -1;
        Cursor c = null;
        try{
            c = context.getContentResolver().query(ThemeCacheTable.THEME_CACHE_URI,null,
            		ThemeCacheTable.THEME_ID + " = ?" ,new String[]{resId},null);
            if(c != null && c.moveToNext()){
                result = c.getInt(c.getColumnIndex(ThemeCacheTable.THEME_COSUMEPOINTS));
            }
        }finally {
            if(c != null){
                c.close();
            }
        }
        return result;
    }

    private class AppListItem{
        public App mApp;
        public int mState;
    }

    private int getAppState(App app){
        int result = STATE_GET_POINT;
        Cursor c = null;
        try{
        	c = context.getContentResolver().query(PointResourceTable.TABLE_URI,null,
                    PointResourceTable.POINT_APP_ID + " = ? AND " + PointResourceTable.POINT_APP_TRACKID +" = ?",
                    new String[]{app.getStrId(),String.valueOf(app.getTrackId())},null);
            while(c != null && c.moveToNext()){
                int download = c.getInt(c.getColumnIndex(PointResourceTable.POINT_APP_DOWNLOAD));
                if(download == FLAG_DOWNLOAD_FROM_POINT_CENTER){
                    int getPoint = c.getInt(c.getColumnIndex(PointResourceTable.POINT_APP_REWARD_STATE));
                    if(getPoint == FLAG_NOT_REWARD){
                        int statusCode = AppManager.getInstance(context).getStatus(app).statusCode;
                        switch (statusCode) {
                            case AppManager.STATUS_NONE:
                            case AppManager.STATUS_UNKNOWN:
                                break;
                            case AppManager.STATUS_PAUSED:
                            case AppManager.STATUS_DOWNLOADING:
                                result = STATE_DOWNLOADING;
                                break;
                            case AppManager.STATUS_DOWNLOADED:
                                result = STATE_INSTALL;
                                break;
                            case AppManager.STATUS_INSTALLED:
                                result = STATE_OPEN;
                                break;
                        }
                    }
                }else{
                    result = STATE_DOWNLOAD;
                }
            }
        }finally{
        	if(c != null){
                c.close();
            }
        }

        NqLog.i("getAppState appId="+app.getStrId()+" state="+result);

        return result;
    }

    /**
     * 缓存点击下载的积分应用信息
     */
    public void flagPointResource(App app,long downloadId, String themeID){
        NqLog.i("flagPointResource app.name="+app.getStrName()+" downloadId="+downloadId);
        context.getContentResolver().delete(PointResourceTable.TABLE_URI,PointResourceTable.POINT_APP_ID + " = ?",new String[]{app.getStrId()});

        ContentValues value = AppManager.getInstance(context).appToContentValues(COLUMN_POINT_APP,app);
        value.put(PointResourceTable.POINT_APP_TIME,System.currentTimeMillis());
        value.put(PointResourceTable.POINT_APP_DOWNLOAD,FLAG_DOWNLOAD_FROM_POINT_CENTER);
        value.put(PointResourceTable.POINT_APP_DOWNLOADID,downloadId);
        value.put(PointResourceTable.POINT_APP_FROM_THEME_ID, themeID);
        Uri uri = context.getContentResolver().insert(PointResourceTable.TABLE_URI,value);
        NqLog.i("uri="+uri);
    }

    private void clearExPointResource() {
        context.getContentResolver().delete(PointResourceTable.TABLE_URI,
                PointResourceTable.POINT_APP_TIME + " <= ?", new String[]{String.valueOf(System.currentTimeMillis() - 7 * DateUtils.DAY_IN_MILLIS)});
    }

    public void consumePoints(String resId,final ConsumePointsListener listener){
       if(listener == null){
           return;
       }

       PointServiceFactory.getService().consumePoints(resId,listener);
    }

    public List<Theme[]> getConsumThemeList(ConsumeListResp resp){
        List<Theme[]> result = new ArrayList<Theme[]>();
        List<WidgetResource> list = resp.widgetResourceList;
        Theme[] arrTheme = null;
        for (int i = 0; i < list.size(); i++) {
            if(i % 3 == 0)
                arrTheme = new Theme[3];
            arrTheme[i % 3] = list.get(i).themeResource;
            if(i % 3 == 2)
                result.add(arrTheme);
        }
        if(list.size() % 3 != 0){
            result.add(arrTheme);
        }
        return result;
    }

    public void getConsumeList(ConsumeListListener listener){
        NqLog.i("getConsumeList");
        PointServiceFactory.getService().getConsumeList(listener);
    }

    public void getThemeList(int offset,ThemeListListener listener){
        GetThemeListTag tag = new GetThemeListTag();
        tag.column = COLUMN_POINT_THEME;
        tag.offset = offset;
        tag.listener = listener;
        PointServiceFactory.getService().getThemeList(tag);
    }
    
    public boolean isPointsCenterResource(String packageName){
        boolean result = false;
        Cursor c = null;
        try{
			c = context.getContentResolver().query(
					PointResourceTable.TABLE_URI, null,
                    PointResourceTable.POINT_APP_PACKAGENAME + " = ?",
					new String[] { packageName }, null);
			if (c != null && c.moveToNext()) {
				result = true;
			}
        }finally {
            if(c != null){
                c.close();
            }
        }

        NqLog.i("isPointsCenterResource packagName="+packageName+" result="+result);
        return result;
    }
    
    public String getDownloadPointsAppNameByPackName(String packageName){
		String result = "";
		Cursor c = null;
		try {
			c = context.getContentResolver().query(
                    PointResourceTable.TABLE_URI,
					null,
                    PointResourceTable.POINT_APP_PACKAGENAME + " = ? AND "
							+ PointResourceTable.POINT_APP_DOWNLOAD + "=1",
					new String[] { packageName }, null);
			if (c != null && c.moveToNext()) {
				result = c.getString(c
						.getColumnIndex(PointResourceTable.POINT_APP_NAME));
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return result;
    }
    
    public String getDownloadPointsAppNameByResId(String resId){
		String result = "";
		Cursor c = null;
		try {
			c = context.getContentResolver().query(
					PointResourceTable.TABLE_URI,
					null,
                    PointResourceTable.POINT_APP_ID + " = ? AND "
							+ PointResourceTable.POINT_APP_DOWNLOAD + "=1",
					new String[] { resId }, null);
			if (c != null && c.moveToNext()) {
				result = c.getString(c
						.getColumnIndex(PointResourceTable.POINT_APP_NAME));
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return result;
    }


    public void onEvent(ConsumeListProtocol.ConsumeListSuccessEvent event){
        NqLog.i("onEvent ConsumeListSuccessEvent");
       ((ConsumeListListener) event.getTag()).onConsumeListSucc(event.mResp);
    }

    public void onEvent(ConsumeListProtocol.ConsumeListFailedEvent event){
        NqLog.i("onEven ConsumeListFailedEvent");
        ((ConsumeListListener) event.getTag()).onErr();
    }

    public void onEvent(RewardPointsProtocol.RewardPointsSuccessEvent event){
        RewardPointsResp resp = event.resp;
        Map<String,Integer> resultCode = resp.respCodes;
        RewardPointsTag tag = (RewardPointsTag) event.getTag();
        NqLog.i("rewardPoints resultCode="+resultCode+" fore="+tag.fore);

        String id = tag.trackId;
        if(!tag.fore){
            for (Iterator<String> i = resultCode.keySet().iterator(); i.hasNext();) {
                String trackid = i.next();
                int code = resultCode.get(trackid);
                NqLog.i("trackId="+trackid+" code="+code);
                flagPointResourceGetPoints(String.valueOf(trackid), code == CODE_SUCC);
                context.getContentResolver().delete(RewardPointsTable.TABLE_URI,
                        RewardPointsTable.REWARD_POINTS_HISTORY_TRACKID + " = ?",new String[]{trackid});
            }
        }else{
            int code = resultCode.get(id);
            NqLog.i("foregroud rewardPoints code="+code);
            if(code == CODE_SUCC){
                showGetPointsNoti(getPointInfo(id).points);
            }
            flagPointResourceGetPoints(String.valueOf(id), code == CODE_SUCC);
            context.getContentResolver().delete(RewardPointsTable.TABLE_URI,
                    RewardPointsTable.REWARD_POINTS_HISTORY_TRACKID + " = ?",new String[]{id});
        }

        processPointInfo(resp.pointInfo);

        if (!TextUtils.isEmpty(tag.theme)) {
            checkShowIntoThemeNoti(getPointInfo(id).points,tag.theme, tag.downid);
        }
    }

    public void onEvent(RewardPointsProtocol.RewardPointsFailEvent event){
        NqLog.i("onErr foregroud rewardPoints");
        if(event.trackId != null){
            TPointsChangeInfo info = getPointInfo(event.trackId);
            ContentValues value = new ContentValues();
            value.put(RewardPointsTable.REWARD_POINTS_HISTORY_RESID,info.getResourceId());
            value.put(RewardPointsTable.REWARD_POINTS_HISTORY_POINTS,info.getPoints());
            value.put(RewardPointsTable.REWARD_POINTS_HISTORY_SCENE,info.getScene());
            value.put(RewardPointsTable.REWARD_POINTS_HISTORY_TIME,info.getClientTime());
            value.put(RewardPointsTable.REWARD_POINTS_HISTORY_TRACKID,event.trackId);
            context.getContentResolver().insert(RewardPointsTable.TABLE_URI,value);
        }
    }

    public void onEvent(GetPointsProtocol.GetPointsSuccessEvent event){
        NqLog.i("onEvent GetPointsSuccessEvent info.point="+event.info.totalPoints);
        processPointInfo(event.info);
        ((GetPointsListener)event.getTag()).onGetPointsSucc(event.info);
    }

    public void onEvent(GetPointsProtocol.GetPointsFailEvent event){
        NqLog.i("onEvent GetPointsFailEvent");
        ((GetPointsListener)event.getTag()).onErr();
    }

    public void onEvent(ConsumePointsProtocol.ConsumePointsSucessEvent event){
        processPointInfo(event.resp.pointsInfo);
        ((ConsumePointsListener)event.getTag()).onComsumeSucc(event.resp);
    }

    public void onEvent(ConsumePointsProtocol.ConsumePointsFailEvent event){
        ((ConsumePointsListener)event.getTag()).onErr();
    }

    public void onEvent(PeriodCheckEvent event){
        checkShowExPointNoti();
    }

    public void onEvent(PackageAddedEvent event){
        NqLog.i("PointsManager onEvent!");
        if(isPointsCenterResource(event.getPackageName())){
            NotificationUtil.cancleNoti(context,NotificationUtil.NOTIF_ID_INSTALL_APP);
        }

        checkShowInstallTip(event.getPackageName());
    }

    public void onEvent(GetAppListProtocol.GetAppListSuccessEvent event){
        NqLog.i("onEvent GetAppListSuccessEvent list.size="+event.list.size());
        clearExPointResource();
        GetAppListTag tag  = (GetAppListTag) event.getTag();
        tag.listener.onGetAppListSucc(tag.offset,sortAppList(event.list));
    }

    public void onEvent(GetAppListProtocol.GetAppListFailEvent event){
        NqLog.i("onEvent GetAppListFailEvent");
        ((GetAppListTag)(event.getTag())).listener.onErr();
    }

    public void onEvent(GetThemeListProtocol.GetThemeListSuccessEvent event){
        GetThemeListTag tag = (GetThemeListTag)event.getTag();
        tag.listener.onGetThemeListSucc(tag.offset,event.list);
        ThemeManager.getInstance(ApplicationContext.getContext()).saveThemeCache(COLUMN_POINT_THEME, tag.offset, event.list);
    }

    public void onEvent(GetThemeListProtocol.GetThemeListFailEvent event) {
        GetThemeListTag tag = (GetThemeListTag) event.getTag();
        tag.listener.onErr();
    }

	@Override
	public void init() {
		EventBus.getDefault().register(this);
	}
}
