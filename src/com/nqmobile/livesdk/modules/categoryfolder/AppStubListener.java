package com.nqmobile.livesdk.modules.categoryfolder;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.appstub.model.AppStub;

import java.util.List;

/**
 * @author liujiancheng
 */
public interface AppStubListener extends Listener{

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

    public void onSuccess(List<AppStub> apps);

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}