package com.nqmobile.livesdk.modules.points;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.commons.net.NetworkingListener;
import com.nqmobile.livesdk.modules.theme.Theme;

import java.util.List;

/**
 * Created by Rainbow on 2014/11/20.
 */
public interface ThemeListListener extends Listener{

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

    public void onGetThemeListSucc(int offset, List<Theme[]> themes);

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
