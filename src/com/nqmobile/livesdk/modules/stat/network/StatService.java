package com.nqmobile.livesdk.modules.stat.network;

import com.nqmobile.livesdk.commons.net.AbsService;
import com.nqmobile.livesdk.modules.stat.*;

/**
 * Created by Rainbow on 2014/11/22.
 */
public class StatService extends AbsService{

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

    public void uploadAllPackage(){
        getExecutor().submit(new UploadPackageActionProtocol());
    }

    public void uploadNewPackage(){
        getExecutor().submit(new UploadNewPackageProtocol());
    }

    public void uploadAdStat(UploadAdStatTag tag){
        getExecutor().submit(new UploadAdStatProtocol(tag));
    }

    public void uploadLog(){
        getExecutor().submit(new UploadLogProtocol());
    }

    public void uploadCrashLog(){
        getExecutor().submit(new UploadCrashLogProtocol());
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
