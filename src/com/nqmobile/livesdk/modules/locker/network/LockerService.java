/**
 * 
 */
package com.nqmobile.livesdk.modules.locker.network;

import com.nqmobile.livesdk.commons.net.AbsService;
import com.nqmobile.livesdk.modules.locker.NewLockerEngineListener;

/**
 * @author HouKangxi
 *
 */
public class LockerService extends AbsService {
	public void getLockerList(int column, int offset, int length, Object tag) {
		getExecutor().submit(
				new LockerListProtocol(column, offset, length, tag));
	}

	public void getLockerDetail(String id, Object tag) {
		getExecutor().submit(new LockerDetailProtocol(id, tag));
	}
	
	public void getNewLockerEngine(int type, int version, Object tag){
		getExecutor().submit(new GetNewLockerEngineProtocol(type, version, tag));
		
	}
}
