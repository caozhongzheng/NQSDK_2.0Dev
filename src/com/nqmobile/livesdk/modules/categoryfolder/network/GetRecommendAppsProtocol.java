package com.nqmobile.livesdk.modules.categoryfolder.network;

import android.content.Context;
import com.nq.interfaces.launcher.TAppResource;
import com.nq.interfaces.launcher.TClassFolderAppListReq;
import com.nq.interfaces.launcher.TClassFolderAppListResp;
import com.nq.interfaces.launcher.TLauncherService;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.categoryfolder.CategoryFolderModule;
import com.nqmobile.livesdk.modules.categoryfolder.CategoryFolderPreference;
import com.nqmobile.livesdk.modules.categoryfolder.GetRecommendAppTag;
import com.nqmobile.livesdk.modules.categoryfolder.model.RecommendApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rainbow on 2014/12/11.
 */
public class GetRecommendAppsProtocol extends AbsLauncherProtocol{

    // ===========================================================
    // Constants
    // ===========================================================

    private static final ILogger NqLog = LoggerFactory.getLogger(CategoryFolderModule.MODULE_NAME);

    // ===========================================================
    // Fields
    // ===========================================================

    private List<Integer> mCategory;
    private CategoryFolderPreference mPreference;
    private Context mContext;

    // ===========================================================
    // Constructors
    // ===========================================================

    public GetRecommendAppsProtocol(GetRecommendAppTag tag){
        mCategory = tag.category;
        mPreference = CategoryFolderPreference.getInstance();
        mContext = ApplicationContext.getContext();
        setTag(tag);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    protected int getProtocolId() {
        return 0x38;
    }

    @Override
    protected void onError() {
        EventBus.getDefault().post(new GetRecommendAppsFailEvent(getTag()));
    }

    @Override
    protected void process() {
        NqLog.i("GetRecommendAppsProtocol process");
        try {
            TLauncherService.Iface client = TLauncherServiceClientFactory.getClient(getThriftProtocol());
            TClassFolderAppListReq req = new TClassFolderAppListReq();
            req.lqAppTypeIds = mCategory;

            TClassFolderAppListResp resp = client.getClassFolderAppList(getUserInfo(), req);
            Map<Integer,List<TAppResource>> map = resp.classFolderAppList;
            mPreference.setIntValue(CategoryFolderPreference.KEY_EMPTY_FOLDER_APPSTUB_SIZE,resp.empty);
            mPreference.setIntValue(CategoryFolderPreference.KEY_RECENT_FOLDER_SIZE,resp.recently);
            Map<Integer,List<RecommendApp>> result = pareseResp(map);
            EventBus.getDefault().post(new GetRecommendAppsSuccEvent(result,getTag()));
        } catch (Exception e) {
            NqLog.e(e);
            onError();
        }finally {
            mPreference.setLongValue(CategoryFolderPreference.KEY_LAST_GET_RECOMMEND_APP_TIME, System.currentTimeMillis());
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================

    private Map<Integer,List<RecommendApp>> pareseResp(Map<Integer,List<TAppResource>> map){
        Map<Integer,List<RecommendApp>> result = new HashMap<Integer, List<RecommendApp>>();
        List<RecommendApp> recommenList;
        for(Integer key : map.keySet()){
            List<TAppResource> list = map.get(key);
            recommenList = new ArrayList<RecommendApp>();
            for(TAppResource a : list){
                RecommendApp app = new RecommendApp(new App(a,mContext));
                recommenList.add(app);
            }

            result.put(key,recommenList);
        }

        return result;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    public static class GetRecommendAppsSuccEvent extends AbsProtocolEvent{

        public Map<Integer,List<RecommendApp>> mList;

        public GetRecommendAppsSuccEvent(Map<Integer,List<RecommendApp>> list,Object tag){
            mList = list;
            setTag(tag);
        }
    }

    public static class GetRecommendAppsFailEvent extends AbsProtocolEvent{

        public GetRecommendAppsFailEvent(Object tag){
            setTag(tag);
        }
    }
}
