package com.nqmobile.livesdk.modules.points.network;

import java.util.ArrayList;
import java.util.List;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TThemeResource;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.points.GetThemeListTag;
import com.nqmobile.livesdk.modules.points.PointModule;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.theme.ThemeConstants;
import com.nqmobile.livesdk.utils.CollectionUtils;

/**
 * Created by Rainbow on 2014/11/20.
 */
public class GetThemeListProtocol extends AbsLauncherProtocol {

    // ===========================================================
    // Constants
    // ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(PointModule.MODULE_NAME);
	
    private static final int COLUMN = 5;

    // ===========================================================
    // Fields
    // ===========================================================

    private int offset;

    // ===========================================================
    // Constructors
    // ===========================================================

    public GetThemeListProtocol(GetThemeListTag tag){
        setTag(tag);
        offset = tag.offset;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    protected int getProtocolId() {
        return 0x03;
    }

    @Override
    protected void process() {
        try {
            TLauncherService.Iface client = TLauncherServiceClientFactory.getClient(getThriftProtocol());
            List<TThemeResource> list = client.getThemeList(getUserInfo(), COLUMN, offset, ThemeConstants.STORE_THEME_LIST_PAGE_SIZE);
            if(!CollectionUtils.isEmpty(list)){
                List<Theme[]> arrThemesList = new ArrayList<Theme[]>();
                    Theme[] arrTheme = null;
                    for (int i = 0; i < list.size(); i++) {
                        if(i % 3 == 0)
                            arrTheme = new Theme[3];
                        arrTheme[i % 3] = new Theme(list.get(i), ApplicationContext.getContext());
                        if(i % 3 == 2)
                            arrThemesList.add(arrTheme);
                    }
                    if(list.size() % 3 != 0){
                        arrThemesList.add(arrTheme);
                    }
                EventBus.getDefault().post(new GetThemeListSuccessEvent(arrThemesList,getTag()));
            }else{
                EventBus.getDefault().post(new GetThemeListFailEvent(getTag()));
            }
        } catch (Exception e) {
            NqLog.e(e);onError();
        }
    }
    @Override
	protected void onError() {
    	EventBus.getDefault().post(new GetThemeListFailEvent(getTag()));
	}
    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    public static class GetThemeListSuccessEvent extends AbsProtocolEvent {

        public List<Theme[]> list;

        public GetThemeListSuccessEvent(List<Theme[]> list,Object tag){
            setTag(tag);
            this.list = list;
        }
    }

    public static class GetThemeListFailEvent extends AbsProtocolEvent{

        public GetThemeListFailEvent(Object tag){
            setTag(tag);
        }
    }

	
}
