package com.nqmobile.livesdk.modules.regularupdate.processor;

import java.util.Map;

import com.nqmobile.livesdk.commons.moduleframework.IUpdateActionHandler;
import com.nqmobile.livesdk.modules.regularupdate.model.UpdateAction;

public interface IUpdateActionProcessor {

	public void process(Map<Integer, IUpdateActionHandler> handlers, UpdateAction action);

}
