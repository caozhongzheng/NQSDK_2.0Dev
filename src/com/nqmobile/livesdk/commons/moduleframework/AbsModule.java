package com.nqmobile.livesdk.commons.moduleframework;

import android.content.Context;
import android.os.Process;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LogLevel;
import com.nqmobile.livesdk.commons.log.LoggerFactory;

public abstract class AbsModule implements IModule {
	private Context mContext;
	private boolean mEnabled;
	ILogger NqLog = LoggerFactory.getLogger(
			getLogTag()
//			"AbsModule"
			);

	public AbsModule() {
		mContext = ApplicationContext.getContext();
	}
	
//	@Override
//	public void init() {
//	}

	@Override
	public String getLogTag() {
		return getName();
	}

	public Context getContext() {
		return mContext;
	}

	public void setContext(Context mContext) {
		this.mContext = mContext;
	}
	private String getLogSuffix(){
		Thread th = Thread.currentThread();
		String suffix = " in "+this+",pid="+Process.myPid()
				+ ", Thread[id="+th.getId()+", "+", name="+th.getName()+" ]";
		return suffix;
	}
	@Override
	public boolean isEnabled(){
		
		NqLog.v("isEnabled? "+mEnabled+getLogSuffix());
		return mEnabled;
	}
	
	/**
	 * 启用/禁用一个模块，如果调用onEnabled成功，完成模块具体的启用/禁用逻辑，则设置mEnabled状态为，否则不修改状态
	 * @param enabled
	 */
	@Override
	public final void setEnabled(boolean enabled){
		try {
			onEnabled(enabled);
			NqLog.i("setEnabled: "+enabled+getLogSuffix());
			mEnabled = enabled;
		} catch (Exception e) {
			NqLog.e(e);
		}
	}
	

	/**
	 * 启用/禁用一个模块时，模块做一下具体的启用/禁用逻辑
	 */
	protected abstract void onEnabled(boolean enabled);
	
	@Override
	public void onAppFirstInit(boolean enabled) {
		// do nothing
	}
	
}
