package com.nqmobile.livesdk.modules.categoryfolder.network;

import com.nqmobile.livesdk.modules.points.network.PointService;

/**
 * Created by Rainbow on 2014/12/11.
 */
public class CategoryFolderFactory {

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

    private static CategoryFolderService sMock;

    public static void setMock(CategoryFolderService mock) {
        sMock = mock;
    }

    public static CategoryFolderService getService() {
        if (sMock != null){
            return sMock;
        } else {
            return new CategoryFolderService();
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
