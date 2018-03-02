package com.nqmobile.livesdk.modules.appstubfolder;

import java.util.List;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.appstubfolder.model.AppStubFolder;

/**
 * 虚框文件夹获取监听
 * @author caozhongzheng
 * @time 2014/10/10 14:16:44
 */
public interface StubFolderListener extends Listener{
    public void onGetStubFolderSucc(List<AppStubFolder> stubFolders);
}
