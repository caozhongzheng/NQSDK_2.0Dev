/**
 * 
 */
package com.nqmobile.livesdk.modules.search;

import com.nqmobile.livesdk.commons.net.Listener;

/**
 * @author HouKangxi
 *
 */
public interface SearchHotwordsCallback extends Listener {
    void onGetHotwords(String[] hotwords);
}
