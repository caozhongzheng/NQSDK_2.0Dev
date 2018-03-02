package com.nqmobile.livesdk.modules.points.network;

/**
 * Created by Rainbow on 2014/11/18.
 */
public class PointServiceFactory {

    private static PointService sMock;

    public static void setMock(PointService mock) {
        sMock = mock;
    }

    public static PointService getService() {
        if (sMock != null){
            return sMock;
        } else {
            return new PointService();
        }
    }
}
