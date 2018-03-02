package com.nqmobile.livesdk.commons.preference;


/**
 *  记录首次运行过的应用包名，LiveSDKPkgs.xml
 *
 */
public class PkgsPreference extends AbsPreference {
    public PkgsPreference(){
        super(PreferenceServiceFactory.LIVE_SDK_PKGS_PREFERENCE);
	}

}
