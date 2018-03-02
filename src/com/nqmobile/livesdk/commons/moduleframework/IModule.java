package com.nqmobile.livesdk.commons.moduleframework;

import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;


public interface IModule {
	
	public void init();
	/**
	 * 返回模块的名称
	 * @return
	 */
	public String getName();
	
	/**
	 * 返回模块是否允许启用，即服务端配置该模块是否启用
	 * @return
	 */
	public boolean canEnabled();
	
	/**
	 * 返回模块是否已启用
	 * @return
	 */
	public boolean isEnabled();
	
	/**
	 * 启用模块
	 * @param enabled
	 */
	public void setEnabled(boolean enabled);
	
	/**
	 * 返回模块的Log tag
	 * @return
	 */
	public String getLogTag();
	
	/**
	 * 返回模块的Features
	 * @return
	 */
	public List<IFeature> getFeatures();
	
	/**
	 * 返回模块用到的数据库表
	 * @return
	 */
	public List<IDataTable> getTables();
	
	/**
	 * 应用安装后，首次启动的初始化事件，模块做一下自己的首次初始化
	 * @param enabled
	 */
	public void onAppFirstInit(boolean enabled);
}
