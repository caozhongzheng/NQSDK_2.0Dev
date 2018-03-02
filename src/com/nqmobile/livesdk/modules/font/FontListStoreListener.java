package com.nqmobile.livesdk.modules.font;

import java.util.List;

import com.nqmobile.livesdk.commons.net.NetworkingListener;

/**
 * 获取Font列表监听
 * @author liujiancheng
 * @time 2015-01-27
 */
public interface FontListStoreListener extends NetworkingListener {
	public void onGetFontListSucc(int column, List<NqFont> apps);
}