/**
 * 
 */
package com.nqmobile.livesdk.modules.push;

import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_BIGICON_URL;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_DESKTOPTYPE;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_DOWNURL;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_DOWN_TEXT;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_ENDTIME;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_EXPIRTIME;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_ICON_URL;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_ID;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_JUMPTYPE;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_LINK_RESOURCEID;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_NETTYPE;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_PACKAGENAME;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_RESTYPE;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_STARTTIME;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_STYLE;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_SUBTITLE;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_TITLE;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_UNLIKE_TEXT;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_USEBTN;
import static com.nqmobile.livesdk.modules.push.table.PushCacheTable.PUSH_VALIDTIME;
import static com.nqmobile.livesdk.utils.StringUtil.nullToEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.nq.interfaces.launcher.TPush;
import com.nqmobile.livesdk.commons.AppConstants;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.concurrent.PriorityExecutor;
import com.nqmobile.livesdk.commons.db.DataProvider;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.image.ImageListener;
import com.nqmobile.livesdk.commons.image.ImageLoader;
import com.nqmobile.livesdk.commons.image.ImageManager;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchEvent;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchRequest;
import com.nqmobile.livesdk.commons.receiver.DownloadReceiver;
import com.nqmobile.livesdk.commons.service.PeriodCheckEvent;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.modules.app.AppConstant;
import com.nqmobile.livesdk.modules.push.features.PushFeature;
import com.nqmobile.livesdk.modules.push.model.Push;
import com.nqmobile.livesdk.modules.push.network.PushResultEvent;
import com.nqmobile.livesdk.modules.push.network.PushServiceFactory;
import com.nqmobile.livesdk.modules.push.table.PushCacheTable;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.FileUtil;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PackageUtils;
import com.nqmobile.livesdk.utils.StringUtil;

/**
 * @author HouKangxi
 *
 */
public class PushManager extends AbsManager {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(PushModule.MODULE_NAME);
	
	public static final int TYPE_ALL = 0;// 任何情况下都可以展示消息
	public static final int TYPE_DESKTOP = 1;// 仅回到桌面下展示消息
	private static final String PUSH_QUERY_ALL = PushCacheTable.PUSH_DESKTOPTYPE
			+ "=" + TYPE_ALL;
	private static final String PUSH_QUERY_DESKTOP = PushCacheTable.PUSH_DESKTOPTYPE
			+ " in (" + TYPE_ALL + "," + TYPE_DESKTOP + ")";

	// ===========================================================
	// Fields
	// ===========================================================
	private static PushManager mInstance;

	// ===========================================================
	// Constructors
	// ===========================================================
	private Context context;

	private PushManager(Context ctx) {
		context = ctx;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public static PushManager getInstance() {
		return getInstance(ApplicationContext.getContext());
	}

	public static synchronized PushManager getInstance(Context ctx) {
		if (mInstance == null) {
			mInstance = new PushManager(ctx);
		}
		if (mInstance.context == null) {
			mInstance.context = ctx;
		}
		return mInstance;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void init() {
		EventBus.getDefault().register(this);
	}

	public void getPushList() {
		PushServiceFactory.getService().getPush(null);
	}

	public void onEvent(PeriodCheckEvent event) {

		/** 定期展示不限触发场景的Push消息 */
		showPush(TYPE_ALL);

		PushPreference helper = PushPreference.getInstance();
		long now = SystemFacadeFactory.getSystem().currentTimeMillis();
		long lastGetPushTime = helper
				.getLongValue(PushPreference.KEY_LAST_GET_PUSH_TIME);
		long pushFrequency_wifi = helper
				.getLongValue(PushPreference.KEY_PUSH_FREQ_WIFI) * DateUtils.MINUTE_IN_MILLIS;
		long pushFrequency_3g = helper
				.getLongValue(PushPreference.KEY_PUSH_FREQ_3G) * DateUtils.MINUTE_IN_MILLIS;
		if (helper.getBooleanValue(PushPreference.KEY_PUSH_ENABLE)
				&& CommonMethod.hasActiveNetwork(context)) {
			if (NetworkUtils.isWifi(context)) {
				if (Math.abs(now - lastGetPushTime) >= (pushFrequency_wifi == 0L ? 2 * DateUtils.DAY_IN_MILLIS
						: pushFrequency_wifi)) {
					getPushList();
				}
			} else {
				if (Math.abs(now - lastGetPushTime) >= (pushFrequency_3g == 0L ? 7 * DateUtils.DAY_IN_MILLIS
						: pushFrequency_3g)) {
					getPushList();
				}
			}
		}
	}

	public void onEvent(PushResultEvent e) {
		// if (!e.isSuccess()) {
		// return;
		// }
		List<Push> pushs = e.getPushList();
		if (pushs == null || pushs.isEmpty()) {
			return;
		}

		savePushCache(pushs);// 存入缓存

		// 图片下载失败的 pushId 集合
		final Set<String> imageDownedFailurePushIds = new HashSet<String>(
				pushs.size() + 3);// HashSet 足矣

		// 欲下载图片的数目
		int countImages = 0;
		List<Object[]> tasks = new ArrayList<Object[]>(pushs.size() + 5);
		final ImageCountDownLatch imageCountDownLatch = new ImageCountDownLatch();
		
		
		PrefetchEvent prefetchEvent = new PrefetchEvent();
		List<PrefetchRequest> reqlist = null;
		String SDCardPath = CommonMethod.getSDcardPath(context);
        if(SDCardPath == null)
        	SDCardPath = CommonMethod.getSDcardPathFromPref(context);
        
		for (Push push : pushs) {
			final String pushId = push.getStrId();
			// 如果是应用，并且直接下载的，并且允许静默下载的
			if(//push.getIntType()==0 && push.getJumpType()==2&&
					push.getIntType()==0&&
					push.isSlientDownload() //服务端新增isSlientDownload字段
					){
				int type = PrefetchRequest.TYPE_APK;
				String url = push.getDownUrl();
				String path = new StringBuilder(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).append(push.getStrId())
		                .append(".apk").toString();
				long size = push.getSize();
				if (reqlist == null) {
					reqlist = new LinkedList<PrefetchRequest>();
				}
				PrefetchRequest req = new PrefetchRequest(prefetchEvent, pushId, type, url, path, size);
				req.setPackageName(push.getPackageName());
				reqlist.add(req);
			}
			
			String[] imageUrlsToDown;
			if (push.getPushStyle() == 1
					&& !TextUtils.isEmpty(push.getBigIcon())) {
				imageUrlsToDown = new String[2];
				imageUrlsToDown[1] = push.getBigIcon();
			} else {
				imageUrlsToDown = new String[1];
			}
			imageUrlsToDown[0] = push.getIcon();
			countImages += imageUrlsToDown.length;

			ImageListener imageDownCallback = new ImageListener() {
				@Override
				public void getImageSucc(String url, BitmapDrawable drawable) {
					imageCountDownLatch.countDown();
				}

				@Override
				public void onErr() {
					imageDownedFailurePushIds.add(pushId);
					imageCountDownLatch.countDown();
				}
			};
			for (String imgUrl : imageUrlsToDown) {
				tasks.add(new Object[] { imgUrl, imageDownCallback });
			}
		}
		if(reqlist!=null){
			prefetchEvent.mRequests = reqlist;
			prefetchEvent.setSourceType(AppConstants.STORE_MODULE_TYPE_PUSH);
			prefetchEvent.setFeatureId(PushFeature.FEATURE);
			EventBus.getDefault().post(prefetchEvent);
		}
		ImageLoader imageUtil = ImageLoader.getInstance(ApplicationContext
				.getContext());
		imageCountDownLatch.latch = new CountDownLatch(countImages);
		for (Object[] task : tasks) {
			// 执行图片下载
			imageUtil.getImage((String) task[0], (ImageListener) task[1]);
		}
		PriorityExecutor.getExecutor().submit(new Runnable() {
			public void run() {
				try {
					// 等待所有图片下载完成
					imageCountDownLatch.latch.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				showPush(TYPE_ALL, imageDownedFailurePushIds);
			}
		});
	}

	private static class ImageCountDownLatch {
		CountDownLatch latch;

		public ImageCountDownLatch() {
		}

		void countDown() {
			latch.countDown();
		}
	}

	public Push tpushToPush(TPush tp) {
		if (tp == null) {
			return null;
		}
		Push push = new Push();
		NqLog.d("tpushToPush, pushId==" + tp.getPushId() + ",title="
						+ StringUtil.nullToEmpty(tp.getTitle()) + ",subtitle="
						+ StringUtil.nullToEmpty(tp.getSubtitle()) + ",icon="
						+ StringUtil.nullToEmpty(tp.getIcon()) + ",pushStyle="
						+ tp.getPushType() + ",bigIcon="
						+ StringUtil.nullToEmpty(tp.getSpreadImage())
						+ ",useBtn=" + tp.isShowButtons() + ",unlikeText="
						+ StringUtil.nullToEmpty(tp.getUnlikeText())
						+ ",downText="
						+ StringUtil.nullToEmpty(tp.getDownloadText())
						+ ",jump=" + tp.getJumpType() + ",downUrl="
						+ StringUtil.nullToEmpty(tp.getDownUrl())
						+ ",packagename="
						+ StringUtil.nullToEmpty(tp.getPackageName())
						+ ",linkResId="
						+ StringUtil.nullToEmpty(tp.getLinkResourceId())
						+ ",resType=" + tp.getRestype() + ",net=" + tp.getNet()
						+ ",scene=" + tp.getScene() + ",longS="
						+ tp.getStartTime() + ",longE=" + tp.getExpirTime()
						+ ",dayS="
						+ StringUtil.nullToEmpty(tp.getShowStartTime())
						+ ",dayE="
						+ StringUtil.nullToEmpty(tp.getShowEndTime()));
		push.setStrId(tp.getPushId());
		push.setTitle(StringUtil.nullToEmpty(tp.getTitle()));
		push.setSubtitle(StringUtil.nullToEmpty(tp.getSubtitle()));
		push.setIcon(StringUtil.nullToEmpty(tp.getIcon()));
		push.setPushStyle(tp.getPushType());
		push.setBigIcon(StringUtil.nullToEmpty(tp.getSpreadImage()));
		push.setUseBtn(tp.isShowButtons() ? 1 : 0);
		push.setUnlikeText(StringUtil.nullToEmpty(tp.getUnlikeText()));
		push.setDownText(StringUtil.nullToEmpty(tp.getDownloadText()));
		push.setJumpType(tp.getJumpType());
		push.setDownUrl(StringUtil.nullToEmpty(tp.getDownUrl()));
		push.setPackageName(StringUtil.nullToEmpty(tp.getPackageName()));
		push.setLinkResourceId(StringUtil.nullToEmpty(tp.getLinkResourceId()));
		push.setIntType(tp.getRestype());
		push.setNetType(tp.getNet());
		push.setDesktopType(tp.getScene());
		push.setShowStartTime(StringUtil.nullToEmpty(tp.getShowStartTime()));
		push.setShowEndTime(StringUtil.nullToEmpty(tp.getShowEndTime()));
		push.setLongStartTime(tp.getStartTime());
		push.setLongExpirTime(tp.getExpirTime());
		push.setSlientDownload(tp.isSilentDownload==1);
		push.setSize(tp.getSize());
		return push;
	}

	/**
	 * 缓存Push数据
	 *
	 * @param pushs
	 */
	private boolean savePushCache(List<Push> pushs) {
		try {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Builder b = null;
			long now = System.currentTimeMillis();
			// 清除过期PUSH缓存
			b = ContentProviderOperation.newDelete(
					PushCacheTable.PUSH_CACHE_URI).withSelection(
					PushCacheTable.PUSH_EXPIRTIME + "<" + now, null);
			ops.add(b.build());

			if (pushs != null && pushs.size() > 0) {
				Push push = null;
				for (int i = 0; i < pushs.size(); i++) {
					push = pushs.get(i);
					NqLog.i("savePushCache:" + push);
					if (push != null) {
						ContentValues values = pushToContentValues(push);
						b = ContentProviderOperation.newInsert(
								PushCacheTable.PUSH_CACHE_URI).withValues(
								values);
						ops.add(b.build());
					}
				}
			}
			ApplicationContext.getContext().getContentResolver()
					.applyBatch(DataProvider.DATA_AUTHORITY, ops);
			return true;
		} catch (Exception e) {
			NqLog.e(e);
		}
		return false;
	}

	/**
	 * 将push对象转换成数据库操作的ContentValues对象
	 *
	 * @param push
	 * @return
	 */
	private ContentValues pushToContentValues(Push push) {
		ContentValues values = null;
		if (push != null) {
			Log.i("gqf","pushToContentValues push.id="+push.getStrId()+" bigIcon="+push.getBigIcon()+" icon="+push.getIcon());
			values = new ContentValues();
			values.put(PUSH_ID, push.getStrId());
			values.put(PUSH_RESTYPE, push.getIntType());
			values.put(PUSH_LINK_RESOURCEID, push.getLinkResourceId());
			values.put(PUSH_TITLE, push.getTitle());// TODO:广告平台整合到服务器后会下发此字段。
			values.put(PUSH_SUBTITLE, push.getSubtitle());
			values.put(PUSH_ICON_URL, push.getIcon());
			values.put(PUSH_STYLE, push.getPushStyle());
			values.put(PUSH_BIGICON_URL, push.getBigIcon());
			values.put(PUSH_USEBTN, push.getUseBtn());
			values.put(PUSH_UNLIKE_TEXT, push.getUnlikeText());
			values.put(PUSH_DOWN_TEXT, push.getDownText());
			values.put(PUSH_JUMPTYPE, push.getJumpType());
			values.put(PUSH_DOWNURL, push.getDownUrl());
			values.put(PUSH_PACKAGENAME, push.getPackageName());
			values.put(PUSH_DESKTOPTYPE, push.getDesktopType());
			values.put(PUSH_NETTYPE, push.getNetType());
			values.put(PUSH_VALIDTIME, push.getLongStartTime());
			values.put(PUSH_EXPIRTIME, push.getLongExpirTime());
			values.put(PUSH_STARTTIME, push.getShowStartTime());
			values.put(PUSH_ENDTIME, push.getShowEndTime());
		}
		return values;
	}

	public void showPush(int typeDesktop) {
		showPush(typeDesktop, null);
	}

	/**
	 * 展示push
	 * 
	 * @param typeDesktop
	 *            - 类型
	 * @param filterIds
	 *            - 需要过滤掉的pushId
	 */
	private void showPush(int typeDesktop, Collection<String> filterIds) {
		if (!PushPreference.getInstance().getBooleanValue(
				PushPreference.KEY_PUSH_ENABLE)) {
			NqLog.d("PushManager KEY_PUSH_ENABLE is false");
			return;
		}
		Context context = ApplicationContext.getContext();
		if (!CommonMethod.hasActiveNetwork(context)) {
			NqLog.d("PushManager no network");
			return;
		}
		List<Push> pushs = getPushListFromCache(typeDesktop);
		if (pushs != null) {
			NqLog.d("PushManager 有可用的缓存数据");
			if (pushs != null && pushs.size() > 0) {
				processShowPush(pushs);
				return;
			} else {
				NqLog.e("Push cache is in bad state!");
			}
		} else {
			NqLog.d("PushManager.showPush() 无可用的缓存数据  typeDesktop="
					+ typeDesktop);
		}
	}

	/**
	 * 返回缓存的PUSH列表。
	 *
	 * @param desktopType
	 * @return List<Push>
	 */
	public List<Push> getPushListFromCache(int desktopType) {
		List<Push> listPushs = null;
		ContentResolver contentResolver = ApplicationContext.getContext()
				.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = contentResolver.query(PushCacheTable.PUSH_CACHE_URI, null,
					desktopType == TYPE_ALL ? PUSH_QUERY_ALL
							: PUSH_QUERY_DESKTOP, null, PushCacheTable._ID
							+ " asc");
			if (cursor == null || cursor.getCount() == 0)
				return null;

			listPushs = new ArrayList<Push>();
			while (cursor.moveToNext()) {
				Push push = cursorToPush(cursor);
				if (push != null) {
					listPushs.add(push);
				}
			}
		} catch (Exception e) {
			NqLog.e(e);
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return listPushs;
	}

	/**
	 * 将cursor对象转换Push
	 *
	 * @param cursor
	 * @return Push
	 */
	private Push cursorToPush(Cursor cursor) {
		Push push = new Push();
		push.setStrId(nullToEmpty(cursor.getString(cursor
				.getColumnIndex(PUSH_ID))));
		push.setTitle(nullToEmpty(cursor.getString(cursor
				.getColumnIndex(PUSH_TITLE))));
		push.setSubtitle(nullToEmpty(cursor.getString(cursor
				.getColumnIndex(PUSH_SUBTITLE))));
		push.setIcon(nullToEmpty(cursor.getString(cursor
				.getColumnIndex(PUSH_ICON_URL))));
		push.setPushStyle(cursor.getInt(cursor.getColumnIndex(PUSH_STYLE)));
		push.setBigIcon(nullToEmpty(cursor.getString(cursor
				.getColumnIndex(PUSH_BIGICON_URL))));
		push.setUseBtn(cursor.getInt(cursor.getColumnIndex(PUSH_USEBTN)));
		push.setUnlikeText(nullToEmpty(cursor.getString(cursor
				.getColumnIndex(PUSH_UNLIKE_TEXT))));
		push.setDownText(nullToEmpty(cursor.getString(cursor
				.getColumnIndex(PUSH_DOWN_TEXT))));
		push.setJumpType(cursor.getInt(cursor.getColumnIndex(PUSH_JUMPTYPE)));
		push.setDownUrl(nullToEmpty(cursor.getString(cursor
				.getColumnIndex(PUSH_DOWNURL))));
		push.setPackageName(nullToEmpty(cursor.getString(cursor
				.getColumnIndex(PUSH_PACKAGENAME))));
		push.setLinkResourceId(nullToEmpty(cursor.getString(cursor
				.getColumnIndex(PUSH_LINK_RESOURCEID))));
		push.setIntType(cursor.getInt(cursor.getColumnIndex(PUSH_RESTYPE)));
		push.setNetType(cursor.getInt(cursor.getColumnIndex(PUSH_NETTYPE)));
		push.setDesktopType(cursor.getInt(cursor
				.getColumnIndex(PUSH_DESKTOPTYPE)));
		push.setLongStartTime(cursor.getLong(cursor
				.getColumnIndex(PUSH_VALIDTIME)));
		push.setLongExpirTime(cursor.getLong(cursor
				.getColumnIndex(PUSH_EXPIRTIME)));
		push.setShowStartTime(nullToEmpty(cursor.getString(cursor
				.getColumnIndex(PUSH_STARTTIME))));
		push.setShowEndTime(nullToEmpty(cursor.getString(cursor
				.getColumnIndex(PUSH_ENDTIME))));
		return push;
	}

	public boolean clearPushCache() {
		try {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Builder b = null;
			// 清除缓存
			b = ContentProviderOperation
					.newDelete(PushCacheTable.PUSH_CACHE_URI);
			ops.add(b.build());
			ApplicationContext.getContext().getContentResolver()
					.applyBatch(PushCacheTable.DATA_AUTHORITY, ops);

			return true;
		} catch (Exception e) {
			NqLog.e(e);
		}
		return false;
	}

	/** 删除push */
	public boolean deletePushCache(String pushId) {
		Log.i("push", "deletePushCache(pushId=" + pushId + ")");
		if (pushId == null || TextUtils.isEmpty(pushId)) {
			return false;
		}
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		Builder b = null;
		b = ContentProviderOperation.newDelete(PushCacheTable.PUSH_CACHE_URI)
				.withSelection(PushCacheTable.PUSH_ID + "='" + pushId + "'",
						null);
		ops.add(b.build());
		Context context = ApplicationContext.getContext();
		ContentResolver contentResolver = context.getContentResolver();
		try {
			contentResolver.applyBatch(PushCacheTable.DATA_AUTHORITY, ops);
			return true;
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private void processShowPush(List<Push> pushs) {
		if (pushs == null || pushs.isEmpty())
			return;
		Context context = ApplicationContext.getContext();
		boolean isWifi = NetworkUtils.isWifi(context);
		long now = System.currentTimeMillis();
		int time = getDayTime();
		for (Push push : pushs) {
			// 删除已安装的push
			if (PackageUtils.isAppInstalled(context, push.getPackageName())) {
				NqLog.i("delete installded push pack: " + push.getPackageName());
				deletePushCache(push.getStrId());
			}

			if (TextUtils.isEmpty(push.getTitle())
					|| TextUtils.isEmpty(push.getSubtitle())) {
				NqLog.i("push title or subtitle is empty:Title= "
						+ push.getTitle() + ",subTitle=" + push.getSubtitle());
			}

			/** PUSHV2 */
			if (push.getPushStyle() == 1
					&& (push.getBigIcon() == null || TextUtils.isEmpty(push
							.getBigIcon()))) {
				NqLog.i("push bigIcon is empty:Title= " + push.getTitle()
								+ ",subTitle=" + push.getSubtitle());
			}
			// 判断网络场景
			if (push.getNetType() == 1 && isWifi || push.getNetType() == 0) {
				int start = 0, end = 0;
				try {
					start = Integer.valueOf(push.getShowStartTime()
							.replaceAll(":", "").replaceAll(" ", ""));
					end = Integer.valueOf(push.getShowEndTime()
							.replaceAll(":", "").replaceAll(" ", ""));
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (push.getLongStartTime() <= now
						&& push.getLongExpirTime() >= now && start <= time
						&& end >= time) {
					final Push p = push;
					// 先从内存缓存中查找
					ImageManager imgMgr = ImageManager.getInstance(context);
					BitmapDrawable drawableIcon = imgMgr
							.loadImageFromMemoryOrDisk(push.getIcon());
					BitmapDrawable drawableBig = null;
					if (drawableIcon == null) {
						NqLog.i("push icon not fetched: " + p.getStrId()
										+ ",name=" + p.getTitle() + ",icon="
										+ p.getIcon());

						ImageLoader.getInstance(context).getImage(
								push.getIcon(), new ImageListener() {

									@Override
									public void onErr() {
									}

									@Override
									public void getImageSucc(String url,
											BitmapDrawable drawable) {
										sendPushBroadcast(p);
									}
								});
					} else {
						if (p.getPushStyle() == 1) {
							drawableBig = imgMgr.loadImageFromMemoryOrDisk(push
									.getBigIcon());
							if (drawableBig == null) {
								NqLog.i("push bigIcon not fetched: "
												+ p.getStrId() + ",name="
												+ p.getTitle() + ",bigIcon="
												+ p.getBigIcon());
								ImageLoader.getInstance(context).getImage(
										push.getBigIcon(), new ImageListener() {

											@Override
											public void onErr() {
												NqLog.e("大图下载失败:"+p.getBigIcon());
											}

											@Override
											public void getImageSucc(
													String url,
													BitmapDrawable drawable) {
												// TODO ImageListener
												// getImageSucc
												// stub
												NqLog.i("大图下载成功:"+p.getBigIcon());
												sendPushBroadcast(p);
											}
										});
							} else {
								sendPushBroadcast(p);
							}
						} else {
							sendPushBroadcast(p);
						}

					}

				} else {
					NqLog.i(push.getStrId() + ", 竟然出现失效的Push! " + push.getTitle()
									+ ",push date,time is invalid: date=["
									+ push.getLongStartTime() + ",="
									+ push.getLongExpirTime() + "] time=["
									+ push.getShowStartTime() + ","
									+ push.getShowEndTime());
				}
			}
		}
	}

	private void sendPushBroadcast(final Push push) {
		Log.i("push", "发送push通知：sendPushBroadcast(push=" + push + ")");
		BitmapDrawable drawableIcon = null;
		BitmapDrawable drawableBigIcon = null;
		Bitmap image = null;
		Bitmap bigImage = null;
		Context context = ApplicationContext.getContext();
		ImageManager imgMgr = ImageManager.getInstance(context);
		if (push.getPushStyle() == 1) {
			drawableBigIcon = imgMgr.loadImageFromMemoryOrDisk(push.getBigIcon());
			if (drawableBigIcon != null) {
				try {
					bigImage = drawableBigIcon.getBitmap();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		drawableIcon = imgMgr.loadImageFromMemoryOrDisk(push.getIcon());
		if (drawableIcon != null) {
			try {
				image = drawableIcon.getBitmap();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (image == null) {
			NqLog.i("push icon generated failed: " + push.getStrId()
					+ ",name=" + push.getTitle());
			return;
		}
		if (push.getPushStyle() == 1 && bigImage == null) {
			NqLog.i("push bigIcon generated failed1: " + push.getStrId()
							+ ",name=" + push.getTitle());

			InputStream input = null;
			try {
				input = (InputStream) new URL(push.getBigIcon()).getContent();
				bigImage = BitmapFactory.decodeStream(input);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			} finally {
				FileUtil.closeStream(input);
			}
			if( bigImage == null){
				return;
			}
		}

		int icon = MResource.getIdByName(context, "drawable",
				"nq_push_small_icon");

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(ns);
		Notification notification = createNotification(push, image, icon,
				bigImage);
		mNotificationManager.notify(getPushIdHashCode(push), notification);

		// notify 后删除缓存
		Log.i("push", "notify 后删除缓存");
		deletePushCache(push.getStrId());

		StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
				PushActionLogConstants.ACTION_LOG_1901, push.getStrId(), 0, null);
	}

	private PendingIntent getUnlikePendingIntent(Push push) {
		Intent unlike = new Intent();
		unlike.setAction(DownloadReceiver.ACTION_UNLIKE_PUSH);
		unlike.putExtra(DownloadReceiver.KEY_PUSH, push);
		Context context = ApplicationContext.getContext();
		PendingIntent contentPendingIntent = PendingIntent.getBroadcast(
				context, getPushIdHashCode(push), unlike,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return contentPendingIntent;
	}

	private PendingIntent getDownloadPendingIntent(Push push) {
		Intent unlike = new Intent();
		unlike.setAction(DownloadReceiver.ACTION_DOWNLOAD_PUSH);
		unlike.putExtra(DownloadReceiver.KEY_PUSH, push);
		Context context = ApplicationContext.getContext();
		PendingIntent contentPendingIntent = PendingIntent.getBroadcast(
				context, getPushIdHashCode(push), unlike,
				PendingIntent.FLAG_UPDATE_CURRENT);
		return contentPendingIntent;
	}

	private int getDayTime() {
		GregorianCalendar gcal = new GregorianCalendar();
		int hour = gcal.get(Calendar.HOUR_OF_DAY);
		int minu = gcal.get(Calendar.MINUTE);
		return minu + hour * 100;
	}

	private int getPushIdHashCode(final Push push) {
		return push.getStrId().hashCode();
	}

	private PendingIntent getClickPendingIntent(Push push) {
		Intent detailIntent = new Intent();
		detailIntent.setAction(DownloadReceiver.ACTION_CLICK_PUSH);
		detailIntent.putExtra(DownloadReceiver.KEY_PUSH, push);
		Context context = ApplicationContext.getContext();
		PendingIntent contentPendingIntent = PendingIntent.getBroadcast(
				context, getPushIdHashCode(push), detailIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		return contentPendingIntent;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private Notification createNotification(Push push, Bitmap largeIcon,
			int smallIcon, Bitmap bigImage) {
		PendingIntent contentPendingIntent = getClickPendingIntent(push);
		Context context = ApplicationContext.getContext();
		NotificationCompat.Builder nb = new NotificationCompat.Builder(context)
				.setContentTitle(push.getTitle())
				.setContentText(push.getSubtitle()).setAutoCancel(true)
				.setLargeIcon(largeIcon).setSmallIcon(smallIcon)
				.setTicker(push.getTitle())
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setContentIntent(contentPendingIntent);

		Notification notification = nb.build();

		if (bigImage != null && push.getPushStyle() == 1
				&& Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {// big
																		// view
			int expandedLayoutId = MResource.getIdByName(context, "layout",
					"nq_push_notification_expanded");
			RemoteViews expandedView = new RemoteViews(
					context.getPackageName(), expandedLayoutId);
			int bigImageViewId = MResource.getIdByName(context, "id",
					"notification_expanded_big_image");
			expandedView.setImageViewBitmap(bigImageViewId, bigImage);
			expandedView.setOnClickPendingIntent(bigImageViewId,
					getClickPendingIntent(push));

			int summaryViewId = MResource.getIdByName(context, "id",
					"notification_expanded_summary");
			expandedView.setTextViewText(summaryViewId, push.getSubtitle());

			int seperatorLineViewId = MResource.getIdByName(context, "id",
					"expanded_notification_seperator_line");
			int actionsViewId = MResource.getIdByName(context, "id",
					"expanded_notification_actions");

			if (push.getUseBtn() == 1) {
				expandedView.setViewVisibility(seperatorLineViewId,
						View.VISIBLE);
				expandedView.setViewVisibility(actionsViewId, View.VISIBLE);

				int unlikeActionViewId = MResource.getIdByName(context, "id",
						"notification_expanded_unlike");
				int unlikeTextViewId = MResource.getIdByName(context, "id",
						"notification_expanded_unlike_textview");
				expandedView.setTextViewText(unlikeTextViewId,
						push.getUnlikeText());
				expandedView.setOnClickPendingIntent(unlikeActionViewId,
						getUnlikePendingIntent(push));

				int downloadActionViewId = MResource.getIdByName(context, "id",
						"notification_expanded_download");
				int downloadTextViewId = MResource.getIdByName(context, "id",
						"notification_expanded_download_textview");
				expandedView.setTextViewText(downloadTextViewId,
						push.getDownText());
				expandedView.setOnClickPendingIntent(downloadActionViewId,
						getDownloadPendingIntent(push));
			} else {
				expandedView.setViewVisibility(seperatorLineViewId,
						View.INVISIBLE);
				expandedView.setViewVisibility(actionsViewId, View.INVISIBLE);
			}

			notification.bigContentView = expandedView;
		}

		notification.defaults |= Notification.DEFAULT_SOUND;

		NqLog.d("push=" + push.toString());
		return notification;
	}
}
