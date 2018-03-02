package com.nqmobile.livesdk.commons.prefetch;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LogLevel;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.mydownloadmanager.IDownloadObserver2;
import com.nqmobile.livesdk.commons.mydownloadmanager.MyDownloadManager;
import com.nqmobile.livesdk.commons.mydownloadmanager.MyDownloadManager.DownloadParameter;
import com.nqmobile.livesdk.commons.prefetch.event.CancelSilentDownloadEvent;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchEvent;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchRequest;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchSuccessEvent;
import com.nqmobile.livesdk.commons.prefetch.table.PrefetchTable;
import com.nqmobile.livesdk.modules.app.AppActionConstants;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.stat.StatManager;

public class PrefetchManager extends AbsManager implements IDownloadObserver2 {

	private static PrefetchManager mInstance;
	private Context mContext;
	private MyDownloadManager downloadMgr;
	private ILogger logger = LoggerFactory.getLogger(PrefetchModule.MODULE_NAME);
	private android.net.Uri uri;
	private android.content.ContentResolver contentResolver;

	private PrefetchManager(Context context) {
		super();
		this.mContext = context.getApplicationContext();
	}

	public synchronized static PrefetchManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new PrefetchManager(context.getApplicationContext());
		}
		return mInstance;
	}

	@Override
	public void init() {
		logger.d("PrefetchManager init():"+this);
		mContext = ApplicationContext.getContext();
		EventBus.getDefault().register(this);
		downloadMgr = MyDownloadManager.getInstance(mContext);
		
		uri = PrefetchTable.TABLE_URI;
		contentResolver = mContext.getContentResolver();
		logger.setLogLevel(LogLevel.DEBUG);
	}

	public void onEvent(PrefetchEvent event) {
		logger.d("PrefetchManager.onEvent(PrefetchEvent)"+event);
		if (event.mRequests == null || event.mRequests.isEmpty()) {
			logger.w("PrefetchManager.onEvent(PrefetchEvent), event.mRequests="+event.mRequests);
			return;
		}
//		if (!NetworkUtils.isWifi(mContext)) {
//			saveEvent(event);
//			logger.d("reject slient dowload when not wifi.");
//			return;
//		}
		processPrefetchs(event);
	}

	private void processPrefetchs(PrefetchEvent event) {
		// 该Event的所有 request 认为是共享Event的featureId
		List<PrefetchRequest> reqlist = event.mRequests;
//		Collection<Integer> allowSlientDownloadFeatureIds = PrefetchPreference
//				.getInstance().getAllowSlientDownloadFeatureIds();
//		// 检查此 featureId 对应的模块是否允许静默下载
//		if (allowSlientDownloadFeatureIds == null) {
//			logger.w("has no config for allowSlientDownloadFeatureIds");
//		} else if (!allowSlientDownloadFeatureIds
//				.contains(event.getFeatureId())) {
//			logger.w("featureId :" + event.getFeatureId() + " 不允许静默下载");
//			return;
//		}
		long MaxCacheSize = getMaxCacheSizeFromConfig();
		for (PrefetchRequest prefetchReq : reqlist) {
			// 第1步： 检查下载必须满足的条件
			if (ignoreDownloadRequest(prefetchReq)) {
				logger.w("ignoreDownloadRequest for PrefetchRequest:"
						+ prefetchReq);
				continue;
			}

			// 第2步： 查询状态为下载中的记录的缓存最大限制
			// if(正在下载的totalSize > MaxCacheSize) {continue;}
			{
				long downloadingSize = queryDownloadingSizeFromDB();
				if (downloadingSize > MaxCacheSize) {
					logger.w("downloadingSize ("+downloadingSize
							+ ")> MaxCacheSize("+MaxCacheSize
							+ ") ");
					continue;
				}
			}
			// 第3步： 构造下载参数
			DownloadParameter para = convertPrefetchRequestToDownloadParameter(prefetchReq);
			// 第4步：执行下载
			Long downId = downloadMgr.download(para);
			logger.d("try download :downId=" + downId + ", req=" + prefetchReq);
			if (downId != null) {
				// 第5步：插入数据 和 注册下载的回调
				try {
					downloadMgr.registerDownloadObserver(downId, this);
					insert(prefetchReq, downId);
				} catch (Exception e) {
					logger.e("fail to insert PrefetchBean with downloadId:"
							+ downId, e);
				}
			} else {
				// Log sth.
				logger.e("downloadId is null for PrefetchRequest: "
						+ prefetchReq);
			}
		}
	}
	/**
	 * 是否忽略此次下载请求
	 * 
	 * @param prefetchReq
	 * @return
	 */
	private boolean ignoreDownloadRequest(PrefetchRequest prefetchReq) {
		// 查询允许静默下载的文件最大大小maxFielSize，如15M(是否需要每条数据都查询一次配置？)
		// 如果 maxFileSize==0, 则可能是本地还没有这项配置，此时认为不限制文件大小
		// if(maxFileSize>0 && e.getSize() > maxFileSize ) {return true; }
//		long maxFileSize = getMaxFileSizeFromConfig();
//		if (maxFileSize > 0 && prefetchReq.getSize() > maxFileSize) {
//			logger.d("prefetchSize to long: " + prefetchReq.getSize() + " > "
//					+ maxFileSize);
//			return true;
//		}
		// 已经静默下载过的不再静默下载
		// 按照资源id和类型 查找记录，如果已存在（TODO 是否考虑下载过但失败的记录？），则忽略这个资源的静默下载
		if (shouldIgnoreResource(prefetchReq)) {

			return true;
		}
		// url, path 是否为空
		if (TextUtils.isEmpty(prefetchReq.getUrl())
				|| TextUtils.isEmpty(prefetchReq.getPath())) {
			return true;
		}
		// TODO 是否考虑 size 为 0 的情况
		// if (prefetchReq.getSize() == 0) {
		// return true;
		// }
		return false;
	}

	/**
	 * 是否应该忽略指定的资源
	 * <p>
	 * 按id 和 type
	 * 
	 * @param resId
	 *            - 资源id
	 * @param resType
	 *            - 资源类型
	 * @param size
	 *            - 资源大小
	 * @return
	 */
	private boolean shouldIgnoreResource(PrefetchRequest prefetchReq) {
		String resId = prefetchReq.getResId();
		int resType = prefetchReq.getType();
		long size = prefetchReq.getSize();
		String[] projection = new String[] { PrefetchTable.DownloadId,
				PrefetchTable.STATUS };
		String selection;
		String[] selectionArgs;
		selection = String.format("%s=? and %s=? and %s=?",
				PrefetchTable.ResId, PrefetchTable.ResTYPE, PrefetchTable.SIZE);
		selectionArgs = new String[] { resId, "" + resType, "" + size };
		Cursor cursor = contentResolver.query(uri, projection, selection,
				selectionArgs, null);
		boolean ignore = false;
		if (cursor.moveToNext()) {
			int statusCode = cursor.getInt(cursor
					.getColumnIndex(PrefetchTable.STATUS));
			PrefetchTable.Status status = PrefetchTable.Status
					.valueOf(statusCode);
			if (status != null) {
				switch(status){
				case DownloadFailure:{
					// 相应资源曾经下载失败过，先删除这条失败的记录
					long downloadId = cursor.getLong(cursor
							.getColumnIndex(PrefetchTable.DownloadId));
					remove(downloadId);
				}
				default:
					logger.d("downloaded res is ignore: resId=" + resId+", status="+status);
					ignore = true;break;
				}
			}
		}
		cursor.close();
		return ignore;
	}

	/**
	 * 
	 * @param e
	 * @return
	 */
	private DownloadParameter convertPrefetchRequestToDownloadParameter(
			PrefetchRequest e) {

		DownloadParameter para = new DownloadParameter();
		switch (e.getType()) {
		case PrefetchRequest.TYPE_APK: {
			para.type = MyDownloadManager.DOWNLOAD_TYPE_SLIENT;// 都使用相同的类型？
			para.resId = e.getResId();
			para.downloadUrl = e.getUrl();
			para.iconUrl = "";// icon 有啥用吗？
			para.desPath = e.getPath();
			para.name = "";
			para.packageName = e.getPackageName();
			para.totalSize = e.getSize();// 目前图片都没有size属性，无法预知大小，怎么办？
			break;
		}
		}
		para.networkflag = DownloadManager.Request.NETWORK_WIFI;
		para.order = 0;
		para.background = true;
		return para;
	}

//	public void onEvent(ConnectivityChangeEvent _event) {
//		if (!NetworkUtils.isWifi(mContext)) {
//			return;
//		}
//		List<PrefetchEvent> events = readEvents();
//		if (events == null || events.isEmpty()) {
//			return;
//		}
//		for (PrefetchEvent event : events) {
//			processPrefetchs(event);
//		}
//	}

	@Override
	public void onChange(long downloadId, long bytesSoFar, int status) {
		// 先查询这个downloadId在预取表里是否存在，不存在则return
		// record = db.queryById(downloadId); if(record==null) {return ;}
		switch (status) {
		case DownloadManager.STATUS_RUNNING: {
			logger.d("downloading :" + downloadId);
			// 更改数据库表里相应状态由 待下载->下载中 （是否需要防止重复写入数据库相同的值?)
			updateStatus(downloadId, PrefetchTable.Status.Downloading);
			break;
		}
		case DownloadManager.STATUS_SUCCESSFUL: {
			logger.d("download Success :" + downloadId);
			// 更改数据库表里相应状态由 下载中 -> 下载完成
			updateStatus(downloadId, PrefetchTable.Status.DownloadSuccess);
			// while( 已经下载完成的 totalSize > MaxCacheSize ){
			// 清理最旧的文件;
			// 相应记录的状态改为已删除;
			// updateStatus(_downId, PrefetchTable.Status.Removed);
			// }
			final long MaxCacheSize = getMaxCacheSizeFromConfig();
			if (MaxCacheSize > 0) {
				clearOldCaches(MaxCacheSize);
			}
			// 通知静默下载完成，由数据表的一行记录 record 转换为 PrefetchSuccessEvent 发送出去
			PrefetchSuccessEvent evt = notifyDownloadFinish(downloadId);
			uploadActionLog(evt);
			break;
		}
		case DownloadManager.STATUS_FAILED: {
			logger.d("download Failed :" + downloadId);
			// 更改数据库表里相应状态由 下载中 -> 下载失败
			updateStatus(downloadId, PrefetchTable.Status.DownloadFailure);
			break;
		}
		case DownloadManager.STATUS_PAUSED: {
			logger.d("download PAUSED :" + downloadId);
			updateStatus(downloadId, PrefetchTable.Status.DownloadPaused);
			break;
		}
		case DownloadManager.STATUS_PENDING: {
			logger.d("download PENDING :" + downloadId);
			break;
		}
		}
	}

	private void uploadActionLog(PrefetchSuccessEvent evt) {
		if(evt == null) {
			return;
		}
//		if(TextUtils.isEmpty(evt.getPackageName())){
//			logger.w("ignore actionLog when pkg isEmpty");
//			return;
//		}
		AppManager.getInstance(mContext).OnDownloadComplete(evt.getResId());
    	StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
    			AppActionConstants.ACTION_LOG_1502, evt.getResId(), StatManager.ACTION_DOWNLOAD,
    			"0_" + AppManager.convertType(evt.getSourceType())
    			+ "_" + evt.getPackageName() + "_0");
	}

	private PrefetchSuccessEvent notifyDownloadFinish(long downloadId) {
		PrefetchSuccessEvent evt = null;
		String projection[] = { PrefetchTable.ResId, PrefetchTable.PATH,
				PrefetchTable.FEATURE, PrefetchTable.SOURCETYPE, PrefetchTable.URL,
				PrefetchTable.ResTYPE, PrefetchTable.SIZE,
				PrefetchTable.PACKAGE };
		String selection = String.format("%s=?", PrefetchTable.DownloadId);
		String[] selectionArgs = new String[] { "" + downloadId };
		Cursor cursor = contentResolver.query(uri, projection, selection,
				selectionArgs, null);
		if (cursor.moveToNext()) {
			evt = new PrefetchSuccessEvent()
					.setFeatureId(
							cursor.getInt(cursor
									.getColumnIndex(PrefetchTable.FEATURE)))
					.setSourceType(
							cursor.getInt(cursor
									.getColumnIndex(PrefetchTable.SOURCETYPE)))
					.setPath(
							cursor.getString(cursor
									.getColumnIndex(PrefetchTable.PATH)))
					.setResId(
							cursor.getString(cursor
									.getColumnIndex(PrefetchTable.ResId)))
					.setUrl(cursor.getString(cursor
							.getColumnIndex(PrefetchTable.URL)))
					.setType(
							cursor.getInt(cursor
									.getColumnIndex(PrefetchTable.ResTYPE)))
					.setSize(
							cursor.getLong(cursor
									.getColumnIndex(PrefetchTable.SIZE)))
					.setPackageName(
							cursor.getString(cursor
									.getColumnIndex(PrefetchTable.PACKAGE)));
			EventBus.getDefault().post(evt);
		}
		cursor.close();
		return evt;
	}

	private void clearOldCaches(final long maxCacheSize) {
		List<Object[]> resList = queryDownloaded();
		if (resList == null || resList.isEmpty()) {
			return;
		}
		long totalCacheSize = 0;

		for (Iterator<Object[]> itr = resList.iterator(); itr.hasNext();) {
			Object[] data = itr.next();
			long _downId = (Long) data[0];
			File cacheFile = new File((String) data[1]);
			if (cacheFile.exists()) {
				totalCacheSize += cacheFile.length();
			} else {
				updateStatus(_downId, PrefetchTable.Status.Removed);
				itr.remove();
			}
		}
		for (Iterator<Object[]> itr = resList.iterator(); totalCacheSize > maxCacheSize
				&& itr.hasNext();) {
			Object[] data = itr.next();
			long _downId = (Long) data[0];
			updateStatus(_downId, PrefetchTable.Status.Removed);
			File cacheFile = new File((String) data[1]);
			totalCacheSize -= cacheFile.length();
			cacheFile.delete();
		}
	}

	private long getMaxCacheSizeFromConfig() {
		return PrefetchPreference.getInstance().getMaxCacheSize();
	}

	@Override
	public void onChange() {
	}

	// 桌面删除相应icon时发广播给liveReceiver,再由receive发送Event到这里
	public void onEvent(CancelSilentDownloadEvent cancelEvt) {
		String resourceId = cancelEvt.getResourceId();
		int resType = PrefetchRequest.TYPE_APK;
		Object[] queryRs = queryDownloadIdAndLocalFilePath(resourceId, resType);
		if (queryRs == null || queryRs.length < 2) {
			logger.d("record not found when download cancel by resourceId:"
					+ resourceId);
			return;
		}
		Long downloadId = (Long) queryRs[0];
		logger.d("Cancel silent download");
		downloadMgr.cancelDownload(downloadId);

		// 删除残留文件
		String localPath = (String) queryRs[1];
		if (localPath != null) {
			File cacheFile = new File(localPath);
			if (cacheFile.exists()) {
				cacheFile.delete();
			}
		}
		// 数据库中的下载状态改为已删除? 还是把记录也删除？
		remove(downloadId);
//		updateStatus(downloadId, PrefetchTable.Status.Removed);
	}

	private Object[] queryDownloadIdAndLocalFilePath(String resourceId,
			int resType) {
		String projection[] = { PrefetchTable.DownloadId, PrefetchTable.PATH };
		String selection = String.format("%s=? and %s=?", PrefetchTable.ResId,
				PrefetchTable.ResTYPE);
		String[] selectionArgs = new String[] { resourceId, "" + resType };
		Cursor cur = contentResolver.query(uri, projection, selection,
				selectionArgs, null);
		Object[] rs = null;
		if (cur.moveToNext()) {
			rs = new Object[2];
			rs[0] = cur.getLong(cur.getColumnIndex(PrefetchTable.DownloadId));
			rs[1] = cur.getString(cur.getColumnIndex(PrefetchTable.PATH));
		}
		cur.close();
		return rs;
	}

	private long queryDownloadingSizeFromDB() {
		String[] projection = new String[] { String.format(
				"sum(%s) AS SUM_SIZE", PrefetchTable.SIZE) };
		String selection = String.format("%s=?", PrefetchTable.STATUS);
		String[] selectionArgs = new String[] { ""
				+ PrefetchTable.Status.Downloading.code() };
		Cursor cursor = contentResolver.query(uri, projection, selection,
				selectionArgs, null);
		cursor.moveToFirst();
		long count = cursor.getLong(0);
		cursor.close();
		return count;
	}

	private void insert(PrefetchRequest prefetchReq, long downId) {
		ContentValues values = beanToContentValues(prefetchReq, downId);
		contentResolver.insert(uri, values);
	}

	private boolean remove(long downId) {
		int rowsDeleted = 0;
		try {
			String where = String.format("%s=?", PrefetchTable.DownloadId);
			String[] selectionArgs = new String[] { "" + downId };
			rowsDeleted = contentResolver.delete(uri, where, selectionArgs);
		} catch (Exception e) {
			logger.e("fail to remove with id:" + downId);
		}
		return rowsDeleted > 0;
	}

	private boolean updateStatus(long downId, PrefetchTable.Status status) {
		logger.d("updateStatus(downId=" + downId + ", status=" + status + ")");
		ContentValues values = new ContentValues();
		values.put(PrefetchTable.STATUS, status.code());
		String where = String.format("%s=?", PrefetchTable.DownloadId);
		String[] selectionArgs = new String[] { "" + downId };
		int upd = contentResolver.update(uri, values, where, selectionArgs);
		return upd > 0;
	}

	private ContentValues beanToContentValues(PrefetchRequest prefetchReq,
			long downId) {
		ContentValues values = new ContentValues();
		values.put(PrefetchTable.DownloadId, downId);
		values.put(PrefetchTable.ResId, prefetchReq.getResId());
		values.put(PrefetchTable.ResTYPE, prefetchReq.getType());
		values.put(PrefetchTable.PACKAGE, prefetchReq.getPackageName());
		values.put(PrefetchTable.FEATURE, prefetchReq.getFeatureId());
		values.put(PrefetchTable.SOURCETYPE, prefetchReq.getPrefetchEventEvent().getSourceType());
		values.put(PrefetchTable.PATH, prefetchReq.getPath());
		values.put(PrefetchTable.URL, prefetchReq.getUrl());
		values.put(PrefetchTable.SIZE, prefetchReq.getSize());
		values.put(PrefetchTable.STATUS,
				PrefetchTable.Status.waitDownLoad.code());
		values.put(PrefetchTable.CREATE_TIME, getSystem().currentTimeMillis());
		return values;
	}

	/***
	 * 查询已下载的资源
	 * 
	 * @return -- List &lt; Object[] -- 0-downloadId, 1-filePath &gt;
	 */
	private List<Object[]> queryDownloaded() {
		List<Object[]> rs = null;
		String projection[] = { PrefetchTable.DownloadId, PrefetchTable.PATH,
				PrefetchTable.SIZE };
		String selection = String.format("%s=?", PrefetchTable.STATUS);
		String[] selectionArgs = new String[] { ""
				+ PrefetchTable.Status.DownloadSuccess.code() };
		String sortOrder = PrefetchTable.CREATE_TIME + " ASC";// 按创建时间从小到大排列
		Cursor cur = contentResolver.query(uri, projection, selection,
				selectionArgs, sortOrder);

		rs = new ArrayList<Object[]>(cur.getCount());
		while (cur.moveToNext()) {
			Object[] arr = new Object[3];
			arr[0] = cur.getLong(cur.getColumnIndex(PrefetchTable.DownloadId));
			arr[1] = cur.getString(cur.getColumnIndex(PrefetchTable.PATH));
			// arr[2] = cur.getLong(cur.getColumnIndex(PrefetchTable.SIZE));
			rs.add(arr);
		}
		cur.close();
		return rs;
	}
}
