package com.nqmobile.livesdk.modules.theme.network;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TApplicationException;

import android.content.Context;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TThemeResource;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.theme.ThemeConstants;
import com.nqmobile.livesdk.modules.theme.ThemeManager;
import com.nqmobile.livesdk.modules.theme.ThemeModule;
import com.nqmobile.livesdk.utils.CollectionUtils;

public class ThemeListProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(ThemeModule.MODULE_NAME);

	private Context mContext;
    private int mColumn;
    private int mOffset;

    public ThemeListProtocol(Context context, int column, int offset, Object tag) {
        setTag(tag);
        mContext = context;
        mColumn = column;
        mOffset = offset;
    }
    
	@Override
	protected int getProtocolId() {
		return 0x03;
	}

	@Override
	protected void process() {
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());

			List<TThemeResource> resources = client.getThemeList(getUserInfo(), mColumn, mOffset, ThemeConstants.STORE_THEME_LIST_PAGE_SIZE);
            
			ArrayList<Theme[]> arrThemesList = new ArrayList<Theme[]>();
			if (CollectionUtils.isNotEmpty(resources)) {
				arrThemesList = new ArrayList<Theme[]>();
                Theme[] arrTheme = null;
                for (int i = 0; i < resources.size(); i++) {
                	if(i%3 == 0)
						arrTheme = new Theme[3];
					arrTheme[i%3] = new Theme(resources.get(i), mContext);
					if(i%3 == 2)
						arrThemesList.add(arrTheme);
                }
                if(resources.size()%3 != 0){
					arrThemesList.add(arrTheme);
				}
                if (arrThemesList != null && arrThemesList.size() > 0) {//存入缓存
                    //mListener.onGetThemeListSucc(column, offset, arrThemesList);
                    ThemeManager.getInstance(mContext).saveThemeCache(mColumn, mOffset, arrThemesList);
                }
			}
			EventBus.getDefault().post(new GetThemeListSuccessEvent(arrThemesList, mColumn, mOffset, true, getTag()));
        } catch (TApplicationException e) {//服务器端无数据
            NqLog.d("BannerListProtocol process() server is empty");
            onError();
        } catch (Exception e) {
        	NqLog.e(e);
        	onError();
        }
	}
	@Override
	protected void onError() {
		EventBus.getDefault().post(new GetThemeListSuccessEvent(null, mColumn, mOffset, false, getTag()));
	}
	public static class GetThemeListSuccessEvent extends AbsProtocolEvent {
    	private ArrayList<Theme[]> mResource;
    	private boolean isSuccess = false;
        private int mColumn;
        private int mOffset;
        
    	public GetThemeListSuccessEvent(ArrayList<Theme[]> resource, int column, int offset, boolean success, Object tag){
    		setTag(tag);
    		mResource = resource;
    		isSuccess = success;
    		mColumn = column;
    		mOffset = offset;
    	}

    	public ArrayList<Theme[]> getThemes() {
    		NqLog.d("getThemes mColumn:" + mColumn + " offset:" + mOffset + " resource:" + mResource);
    		return mResource;
    	}
    	
    	public boolean isSuccess(){
    		return isSuccess;
    	}
    	
    	public int getColumn() {
			return mColumn;
		}
    	
    	public int getOffset() {
    		return mOffset;
    	}
    }

	
    
}
