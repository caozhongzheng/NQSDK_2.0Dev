package com.nqmobile.livesdk.modules.points.model;

import com.nq.interfaces.launcher.TConsumeListResp;
import com.nq.interfaces.launcher.TWidgetResource;
import com.nqmobile.livesdk.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rainbow on 2014/11/18.
 */
public class ConsumeListResp {

    public List<WidgetResource> widgetResourceList;

    public ConsumeListResp(TConsumeListResp resp){
        if(resp != null){
            List<TWidgetResource> list = resp.widgetRes;
            if(!CollectionUtils.isEmpty(list)){
                widgetResourceList = new ArrayList<WidgetResource>();
                for(TWidgetResource t : list){
                    widgetResourceList.add(new WidgetResource(t));
                }
            }
        }
    }
}
