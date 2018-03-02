package com.nqmobile.livesdk.modules.appstubfolder.network;

import java.util.List;

import com.nqmobile.livesdk.commons.net.AbsService;

public class AppStubFolderService extends AbsService {
	public void getAppStubFolderList(int scene, List<String> folderIDList, Object tag) {
		getExecutor().submit(new AppStubFolderProtocol(scene, folderIDList, tag));
	}
}
