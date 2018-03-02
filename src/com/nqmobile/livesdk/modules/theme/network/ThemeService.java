package com.nqmobile.livesdk.modules.theme.network;

import android.content.Context;

import com.nqmobile.livesdk.commons.net.AbsService;
import com.nqmobile.livesdk.modules.theme.network.ThemeDetailProtocal;

public class ThemeService extends AbsService {
	public void getThemeDetail(Context context, String id, Object tag) {
		getExecutor().submit(new ThemeDetailProtocal(context, id, tag));
	}
	
	public void getThemeList(Context context, int column, int offset, Object tag) {
		getExecutor().submit(new ThemeListProtocol(context, column, offset, tag));
	}
}
