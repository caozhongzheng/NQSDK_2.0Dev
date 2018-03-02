package com.nqmobile.livesdk.modules.categoryfolder.network;

import com.nqmobile.livesdk.commons.net.AbsService;
import com.nqmobile.livesdk.modules.categoryfolder.GetRecommendAppTag;

import java.util.List;

/**
 * Created by Rainbow on 2014/12/11.
 */
public class CategoryFolderService extends AbsService{

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    public void getRecommendAppsByCategory(GetRecommendAppTag tag){
        getExecutor().submit(new GetRecommendAppsProtocol(tag));
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
