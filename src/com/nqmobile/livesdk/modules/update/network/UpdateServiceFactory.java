package com.nqmobile.livesdk.modules.update.network;

/**
 * Created by Rainbow on 2014/11/20.
 */
public class UpdateServiceFactory {

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private static UpdateService sMock;

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

    public static void setMock(UpdateService mock) {
        sMock = mock;
    }

    public static UpdateService getService() {
        if (sMock != null){
            return sMock;
        } else {
            return new UpdateService();
        }
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}