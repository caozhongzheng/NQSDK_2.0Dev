package com.nqmobile.livesdk.modules.mustinstall.network;

/**
 * Created by Rainbow on 2014/11/19.
 */
public class MustInstallServiceFactory {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private static MustInstallService sMock;

    // ===========================================================
    // Constructors
    // ===========================================================

    public static MustInstallService getService() {
        if (sMock != null){
            return sMock;
        } else {
            return new MustInstallService();
        }
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    public static void setMock(MustInstallService mock) {
        sMock = mock;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
