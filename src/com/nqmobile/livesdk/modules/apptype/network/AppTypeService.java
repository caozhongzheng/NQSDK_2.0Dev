package com.nqmobile.livesdk.modules.apptype.network;

import java.util.List;

import com.nqmobile.livesdk.commons.net.AbsService;

public class AppTypeService extends AbsService {
    public void getAppType(List<String> packageList, Object tag) {
        getExecutor().submit(new GetAppTypeProtocol(packageList, tag));
    }
}
