/**
 * 
 */
package com.nqmobile.livesdk.modules.lqwidget;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.nq.interfaces.launcher.TLqWidget;
import com.nqmobile.livesdk.commons.AppConstants;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppConstant;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.lqwidget.model.LqWidgetInfo;
import com.nqmobile.livesdk.modules.lqwidget.network.LqWidgetDetailProtocol.LqWidgetDetailErrorEvent;
import com.nqmobile.livesdk.modules.lqwidget.network.LqWidgetDetailProtocol.LqWidgetDetailSuccessEvent;
import com.nqmobile.livesdk.modules.lqwidget.network.LqWidgetListProtocol.LqWidgetListErrorEvent;
import com.nqmobile.livesdk.modules.lqwidget.network.LqWidgetListProtocol.LqWidgetListSuccessEvent;
import com.nqmobile.livesdk.modules.lqwidget.network.LqWidgetServiceFactory;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.GpUtils;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * @author nq
 *
 */
public class LqWidgetManager extends AbsManager {

	private static LqWidgetManager instance = new LqWidgetManager();

	private Context getContext() {
		return ApplicationContext.getContext();
	}
	private LqWidgetManager() {
	}

	public static LqWidgetManager getInstance() {
		return instance;
	}

	@Override
	public void init() {
		EventBus.getDefault().register(this);
	}

	public LqWidgetInfo tResource2local(TLqWidget res) {
		if (res == null) {
			return null;
		}
		LqWidgetInfo info = new LqWidgetInfo();
		//
		info.setWidgetId(res.widgetId);
		info.setResourceId(res.resourceId);

		info.setDescription(res.description);
		info.setDownloadNum(res.downloadNum);
		info.setDownloadWay(res.downloadWay);
		info.setIcon(res.icon);
		info.setLinkUrl(res.linkUrl);
		info.setName(res.name);
		info.setPackageName(res.packageName);
		info.setPublishTime(res.publishTime);
		info.setSize(res.size);
		info.setSpanx(res.spanx);
		info.setSpany(res.spany);
		info.setVersion(res.version);
		info.setVersionCode(res.versionCode);
		return info;
	}

	public List<LqWidgetInfo> tResource2local(List<TLqWidget> resourceList) {
		if (resourceList == null || resourceList.isEmpty()) {
			return null;
		}
		List<LqWidgetInfo> rslist = new ArrayList<LqWidgetInfo>(
				resourceList.size());
		//
		for (TLqWidget w : resourceList) {
			LqWidgetInfo info = tResource2local(w);
			rslist.add(info);
		}
		return rslist;
	}
    private volatile Activity activity;
    private volatile boolean invokingData = false;
    
	public void viewWidget(final Activity activity, final String widgetId, final String widgetName) {
		LqWidgetModule.Nqlog.d("viewWidget (widgetId=" + widgetId
				+ ",widgetName=" + widgetName + "), invoking ? " + invokingData+", time:"+System.currentTimeMillis()
				);
		if (invokingData) {
			return;
		}
		invokingData = true;
		
		this.activity = activity;
		Context context =getContext();
		String SDCardPath = CommonMethod.getSDcardPath(context);
		if (SDCardPath == null)
			SDCardPath = CommonMethod.getSDcardPathFromPref(context);
		String path = new StringBuilder(SDCardPath)
				.append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP)
				.append(widgetId).append(".apk").toString();// path & id 统一使用
															// WidgetId
															// 而不是resourceId,
															// 避免不必要的服务器查询
		App app = new App();
		app.setStrId(widgetId);//
		app.setStrAppUrl("");
		app.setStrAppPath(path);
		app.setStrName(widgetName);
		app.setIntSourceType(AppConstants.LQ_MODULE_TYPE_LqWidget);
		app.setIntDownloadActionType(AppManager.DOWNLOAD_ACTION_TYPE_DIRECT);
		AppManager appMgr =  AppManager.getInstance(context);
		AppManager.Status status =appMgr.getStatus(app);
		if (status != null && status.statusCode == AppManager.STATUS_DOWNLOADED) {
			appMgr.OnDownloadComplete(app);// 如果已经下载完成则直接调取安装
			invokingData = false;
		} else {
			// 弹出对话框，对话框上有俩按钮，一个取消，一个下载，点击取消则关闭，
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showDialog(activity, widgetId, widgetName);
				}
			});
		}
		
	}
	public void getLqWidgetList(LqWidgetListener listener) {
		LqWidgetServiceFactory.getService().getLqWidgetList(0, 20, listener);
	}

	public void onEvent(LqWidgetDetailErrorEvent evt) throws Exception {
		
	}
	private final Runnable toastDownloadingTask=new Runnable() {
		@Override
		public void run() {
			ToastUtils.toast(ApplicationContext.getContext(), "nq_in_downloading");			
		}
	};
	private final Runnable serverExceptionTask = new Runnable() {
		@Override
		public void run() {
			ToastUtils.toast(ApplicationContext.getContext(),"nq_serverdata_exception");
		}
	};
	public void onEvent(LqWidgetDetailSuccessEvent evt) throws Exception {
		LqWidgetInfo widget = evt.getWidgetInfo();
		LqWidgetModule.Nqlog.d("onEvent:LqWidgetDetailSuccessEvent widget="
				+ widget);
		if (widget == null) {
			return;
		}
		//如果是gp资源则跳转gp，否则调MyDownloadManager下载
		Context context = ApplicationContext.getContext();
		if (widget.getDownloadWay() == 1) {
			GpUtils.viewDetail(context, widget.getLinkUrl());
			return;
		}
		if (widget.getDownloadWay() == 0) {
			if(TextUtils.isEmpty(widget.getLinkUrl())){
				LqWidgetModule.Nqlog.e("widget 下载地址为空,忽略此次下载");
				if (activity != null) {
					activity.runOnUiThread(serverExceptionTask);
				}
				return;
			}
			
			if(TextUtils.isEmpty(widget.getResourceId())){
				LqWidgetModule.Nqlog.e("widget resouceId为空, 忽略此次下载");
				return;
			}
			// Toast 提示下载中
			if (activity != null) {
				activity.runOnUiThread(toastDownloadingTask);
			}
			
			String SDCardPath = CommonMethod.getSDcardPath(context);
	        if(SDCardPath == null)
	        	SDCardPath = CommonMethod.getSDcardPathFromPref(context);
	        String path = new StringBuilder(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).append(widget.getWidgetId())
	                .append(".apk").toString();// path & id 统一使用 WidgetId 而不是resourceId, 避免不必要的服务器查询
	        App app = new App();
			app.setStrId(widget.getWidgetId());//
			app.setStrAppUrl( widget.getLinkUrl());
	        app.setStrAppPath(path);
			app.setStrName(evt.getWidgetName());
			app.setLongSize(widget.getSize());
			app.setIntSourceType(AppConstants.LQ_MODULE_TYPE_LqWidget);
			app.setIntDownloadActionType(AppManager.DOWNLOAD_ACTION_TYPE_DIRECT);
			app.setStrIconUrl(widget.getIcon());
	        app.setStrPackageName(widget.getPackageName());
				
			Long downloadId = AppManager.getInstance(context).downloadApp(app);
			
			LqWidgetModule.Nqlog.d("download: "+downloadId);
		}
	}
	private void showDialog(final Context mContext,final String widgetId,final String widgetName) {
		LqWidgetModule.Nqlog.d("showDialog");
		final Dialog dialog = new Dialog(mContext, MResource.getIdByName(mContext,"style","translucent"));
		dialog.setCanceledOnTouchOutside(true);
		LayoutInflater inflate = LayoutInflater.from(mContext);
		View lqwidgetDialogLyt = inflate
				.inflate(MResource.getIdByName(mContext, "layout",
						"nq_lqwidget_dialog"), null);
		
		dialog.setContentView(lqwidgetDialogLyt);
		
		TextView lqwiget_dialog_title = (TextView) lqwidgetDialogLyt
				.findViewById(MResource.getIdByName(mContext, "id",
						"lqwiget_dialog_title"));
		lqwiget_dialog_title.setText(widgetName);
		View btn_cancel = (TextView) lqwidgetDialogLyt.findViewById(MResource
				.getIdByName(mContext, "id", "btn_cancel"));
		View btn_download = (TextView) lqwidgetDialogLyt.findViewById(MResource
				.getIdByName(mContext, "id", "btn_download"));
		btn_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		btn_download.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();// 先关闭对话框
				if(NetworkUtils.isConnected(mContext)){
					// --------- 点击下载，去服务端查询资源信息 LqWidgetInfo
					LqWidgetServiceFactory.getService().getLqWidgetDetail(
							null, widgetId,widgetName,null);
				}else{
					// 弹出toast 提示 无网络
					ToastUtils.toast(ApplicationContext.getContext(), "nq_nonetwork");			
				}
			}
		});
		dialog.setOnDismissListener(new android.content.DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				LqWidgetModule.Nqlog.d("dialog onDismiss");
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						LqWidgetModule.Nqlog.d("** "+System.currentTimeMillis()
								+ " set invokingData: false");
						invokingData = false;				
					}
				}).start();
			}
		});
		dialog.show();
		
		dialog.setOnKeyListener(new android.content.DialogInterface.OnKeyListener(){  
		    @Override  
		    public boolean onKey(android.content.DialogInterface dialog, int keyCode,KeyEvent event) {  
		        switch (keyCode) {  
		            case KeyEvent.KEYCODE_BACK:  
		            	LqWidgetModule.Nqlog.d("KEYCODE_BACK");
		            	dialog.dismiss();	
		            case KeyEvent.KEYCODE_HOME:  
		            	LqWidgetModule.Nqlog.d("KEYCODE_HOME");
		            	dialog.dismiss();	
		            return true;  
		        }  
		        return false;  
		    }  
		}); 
	}

	public void onEvent(LqWidgetListSuccessEvent evt) {
		LqWidgetModule.Nqlog.d("onEvent(LqWidgetListSuccessEvent)");
		if (evt.getTag() instanceof LqWidgetListener) {
			LqWidgetListener l = (LqWidgetListener) evt.getTag();
			List<LqWidgetInfo> widgets = evt.getWidgetList();
			l.onSuccess(widgets);
		}
	}

	public void onEvent(LqWidgetListErrorEvent evt) {
		if (evt.getTag() instanceof LqWidgetListener) {
			LqWidgetListener l = (LqWidgetListener) evt.getTag();
			l.onErr();
		}
	}
	// public void onEvent(LqWidgetDetailErrorEvent wlistErrEvent) {
	// }

}
