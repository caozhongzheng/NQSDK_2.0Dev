package com.nqmobile.livesdk.modules.appstub;


public interface IAppStubInstall {
	/**
	 * 安装虚框时，调用系统安装的具体实现，并返回设置结果。
	 * @param appPath
	 * @return 成功/失败
	 */
	public boolean onInstallAppStub(String appPath);
}
