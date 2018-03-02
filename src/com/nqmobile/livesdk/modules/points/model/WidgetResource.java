package com.nqmobile.livesdk.modules.points.model;

import com.nq.interfaces.launcher.TWidgetResource;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.modules.theme.Theme;

/**
 * Created by Rainbow on 2014/11/18.
 */
public class WidgetResource {

    public String resourceId;

    public int type;

    public Theme themeResource;

    public WidgetResource(TWidgetResource tw){
        resourceId = tw.resourceId;
        type = tw.type;
        themeResource = new Theme(tw.themeResource, ApplicationContext.getContext());
    }
}
