/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nqmobile.livesdk.commons.downloadmanager;

import java.util.Collection;
import java.util.HashMap;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.nqmobile.live.R;
import com.nqmobile.livesdk.utils.MResource;

/**
 * This class handles the updating of the Notification Manager for the
 * cases where there is an ongoing download. Once the download is complete
 * (be it successful or unsuccessful) it is no longer the responsibility
 * of this component to show the download in the notification manager.
 *
 */
class DownloadNotification {

    Context mContext;
    HashMap <String, NotificationItem> mNotifications;
    private SystemFacade mSystemFacade;
    
    //最多一行显示的标题个数
    private static final int MAX_TITLE_ONELINE = 10000;
    
    //显示暂停和正在下载  TODO：要移到res
    //private static final String DOWNLOADING = "正在下载";
    //private static final String DOWNLOAD_PAUSED = "暂停下载";
    
    static final String LOGTAG = "DownloadNotification";
    static final String WHERE_RUNNING =
        "(" + Downloads.COLUMN_STATUS + " >= '100') AND (" +
        Downloads.COLUMN_STATUS + " <= '199') AND (" +
        Downloads.COLUMN_VISIBILITY + " IS NULL OR " +
        Downloads.COLUMN_VISIBILITY + " == '" + Downloads.VISIBILITY_VISIBLE + "' OR " +
        Downloads.COLUMN_VISIBILITY +
            " == '" + Downloads.VISIBILITY_VISIBLE_NOTIFY_COMPLETED + "')";
    static final String WHERE_COMPLETED =
        Downloads.COLUMN_STATUS + " >= '200' AND " +
        Downloads.COLUMN_VISIBILITY +
            " == '" + Downloads.VISIBILITY_VISIBLE_NOTIFY_COMPLETED + "'";


    /**
     * This inner class is used to collate downloads that are owned by
     * the same application. This is so that only one notification line
     * item is used for all downloads of a given application.
     *
     */
    static class NotificationItem {
        int mId;  // This first db _id for the download for the app
        long mTotalCurrent = 0;
        long mTotalTotal = 0;
        int mTitleCount = 0;
        String mPackageName;  // App package name
        String mDescription;
        String[] mTitles = new String[MAX_TITLE_ONELINE]; // download titles.
        String mPausedText = null;

        /*
         * Add a second download to this notification item.
         */
        void addItem(String title, long currentBytes, long totalBytes) {
            mTotalCurrent += currentBytes;
            if (totalBytes <= 0 || mTotalTotal == -1) {
                mTotalTotal = -1;
            } else {
                mTotalTotal += totalBytes;
            }
            if (mTitleCount < MAX_TITLE_ONELINE) {
                mTitles[mTitleCount] = title;
            }
            mTitleCount++;
        }
    }


    /**
     * Constructor
     * @param ctx The context to use to obtain access to the
     *            Notification Service
     */
    DownloadNotification(Context ctx, SystemFacade systemFacade) {
        mContext = ctx;
        mSystemFacade = systemFacade;
        mNotifications = new HashMap<String, NotificationItem>();
    }

    /*
     * Update the notification ui.
     */
    public void updateNotification(Collection<DownloadInfo> downloads) {
        updateActiveNotification(downloads);
        updateCompletedNotification(downloads);
    }

   
    private void updateActiveNotification(Collection<DownloadInfo> downloads) {       
        // Collate the notifications
        mNotifications.clear();
        
        //缺省设置有任务为暂停
        boolean hasPausedText = true;
        
        for (DownloadInfo download : downloads) {       	
        	//如果是静默下载，不在通知栏显示
            if (!isActiveAndVisible(download)) {
                continue;
            }
            String packageName = download.mPackage;
            long max = download.mTotalBytes;
            long progress = download.mCurrentBytes;
            long id = download.mId;
            String title = download.mTitle;
            if (title == null || title.length() == 0) {
                title = mContext.getResources().getString(
                        R.string.download_unknown_title);
            }

            NotificationItem item;
            if (mNotifications.containsKey(packageName)) {
                item = mNotifications.get(packageName);
                
                //永远都取download list中id最小的作为post notification的id，修复有时候通知栏会出现多个下载通知的bug
                if (item.mId > download.mId)
                	item.mId = (int)download.mId;

                item.addItem(title, progress, max);
            } else {
                item = new NotificationItem();
                item.mId = (int) id;
                item.mPackageName = packageName;
                item.mDescription = download.mDescription;
                item.addItem(title, progress, max);
                mNotifications.put(packageName, item);
            }
            /*
            if (download.mStatus == Downloads.STATUS_QUEUED_FOR_WIFI
                    && item.mPausedText == null) {
                item.mPausedText = mContext.getResources().getString(
                        R.string.notification_need_wifi_for_size);
            }*/
            
            //单个资源暂停下载时，副标题显示 已暂停；多任务下载时，所有任务都暂停时，副标题显示 已暂停
            //缺省认为所有下载任务都是暂停的，有一个不为暂停状态，则设置状态为非暂停
            if ((download.mStatus != Downloads.STATUS_QUEUED_FOR_WIFI) 
            		&& (download.mStatus != Downloads.STATUS_PAUSED_BY_APP)
            		&& (download.mStatus != Downloads.STATUS_WAITING_FOR_NETWORK)){
            	hasPausedText = false;
            }
        }

        // Add the notifications
        for (NotificationItem item : mNotifications.values()) {
            // Build the notification object
            Notification n = new Notification();

            //boolean hasPausedText = (item.mPausedText != null);
            int iconResource = android.R.drawable.stat_sys_download;
            if (hasPausedText) {
                iconResource = android.R.drawable.stat_sys_warning;
            }
            n.icon = iconResource;

            n.flags |= Notification.FLAG_ONGOING_EVENT;
            //用户不可清除
            n.flags |= Notification.FLAG_NO_CLEAR;
            
            /*
            // Build the RemoteView object
            RemoteViews expandedView = new RemoteViews(mContext.getPackageName(),
                    R.layout.status_bar_ongoing_event_progress_bar);
            if (item.mTitleCount > 1) {
                title.append(mContext.getString(R.string.notification_filename_separator));
                title.append(item.mTitles[1]);
                n.number = item.mTitleCount;
                if (item.mTitleCount > 2) {
                    title.append(mContext.getString(R.string.notification_filename_extras,
                            new Object[] { Integer.valueOf(item.mTitleCount - 2) }));
                }
            } else {
                expandedView.setTextViewText(R.id.description,
                        item.mDescription);
            }
             */
            
            //要显示的标题
            StringBuilder title = new StringBuilder(item.mTitles[0]);            
            for (int i = 1; i < item.mTitleCount; i++){
            	title.append(",");
            	title.append(item.mTitles[i]);
            }
            if (hasPausedText) {
                item.mPausedText = MResource.getString(mContext,"nq_downloadmanager_pasue");;
            }else{
            	item.mPausedText = MResource.getString(mContext,"nq_downloadmanager_downloading");
            }

            /*
            expandedView.setTextViewText(R.id.title, title);

            if (hasPausedText) {
                expandedView.setViewVisibility(R.id.progress_bar, View.GONE);
                expandedView.setTextViewText(R.id.paused_text, item.mPausedText);
            } else {
                expandedView.setViewVisibility(R.id.paused_text, View.GONE);
                expandedView.setProgressBar(R.id.progress_bar,
                        (int) item.mTotalTotal,
                        (int) item.mTotalCurrent,
                        item.mTotalTotal == -1);
            }
            
            expandedView.setTextViewText(R.id.progress_text,
                    getDownloadingText(item.mTotalTotal, item.mTotalCurrent));
            expandedView.setImageViewResource(R.id.appIcon, iconResource);
            n.contentView = expandedView;
			*/
            
            Intent intent = new Intent(Constants.ACTION_LIST);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.setClassName(mContext.getPackageName(),
//                    DownloadReceiver.class.getName());
//            intent.setData(
//                    ContentUris.withAppendedId(Downloads.ALL_DOWNLOADS_CONTENT_URI, item.mId));
//            intent.putExtra("multiple", item.mTitleCount > 1);
            
            n.contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

            // normal style
            Notification notification = new NotificationCompat.Builder(mContext)
                    .setSmallIcon(R.drawable.nq_download_notfi_icon)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setAutoCancel(false)
                    .setContentIntent(PendingIntent.getActivity(mContext, 0, intent, 0))
                    .setContentTitle(title)                
                    .setContentInfo(getDownloadingText(item.mTotalTotal, item.mTotalCurrent))
                    .setContentText(item.mPausedText)
                    .setProgress((int) item.mTotalTotal, (int) item.mTotalCurrent, item.mTotalTotal == -1).build();
            
            notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;            
            notification.icon = iconResource;
          
            mSystemFacade.postNotification(item.mId, notification);

        }
    }

    private void updateCompletedNotification(Collection<DownloadInfo> downloads) {
        for (DownloadInfo download : downloads) {
            if (!isCompleteAndVisible(download)) {
                continue;
            }
            // Add the notifications
            Notification n = new Notification();
            n.icon = android.R.drawable.stat_sys_download_done;

            long id = download.mId;
            String title = download.mTitle;
            if (title == null || title.length() == 0) {
                title = mContext.getResources().getString(
                        R.string.download_unknown_title);
            }
            Uri contentUri =
                ContentUris.withAppendedId(Downloads.ALL_DOWNLOADS_CONTENT_URI, id);
            String caption;
            Intent intent;
            if (Downloads.isStatusError(download.mStatus)) {
                caption = mContext.getResources()
                        .getString(R.string.notification_download_failed);
                intent = new Intent(Constants.ACTION_LIST);
            } else {
                caption = mContext.getResources()
                        .getString(R.string.notification_download_complete);
                if (download.mDestination == Downloads.DESTINATION_EXTERNAL) {
                    intent = new Intent(Constants.ACTION_OPEN);
                } else {
                    intent = new Intent(Constants.ACTION_LIST);
                }
            }
            intent.setClassName(mContext.getPackageName(),
                    DownloadReceiver.class.getName());
            intent.setData(contentUri);

            n.when = download.mLastMod;
            n.setLatestEventInfo(mContext, title, caption,
                    PendingIntent.getBroadcast(mContext, 0, intent, 0));

            intent = new Intent(Constants.ACTION_HIDE);
            intent.setClassName(mContext.getPackageName(),
                    DownloadReceiver.class.getName());
            intent.setData(contentUri);
            n.deleteIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

            mSystemFacade.postNotification(download.mId, n);
        }
    }

    private boolean isActiveAndVisible(DownloadInfo download) {
        return 100 <= download.mStatus && download.mStatus < 200
                && download.mVisibility != Downloads.VISIBILITY_HIDDEN;
    }

    private boolean isCompleteAndVisible(DownloadInfo download) {
        return download.mStatus >= 200
                && download.mVisibility == Downloads.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
    }

    /*
     * Helper function to build the downloading text.
     */
    private String getDownloadingText(long totalBytes, long currentBytes) {
        if (totalBytes <= 0) {
            return "";
        }
        long progress = currentBytes * 100 / totalBytes;
        StringBuilder sb = new StringBuilder();
        sb.append(progress);
        sb.append('%');
        return sb.toString();
    }

}
