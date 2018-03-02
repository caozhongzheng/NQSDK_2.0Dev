package com.nqmobile.livesdk.modules.stat;

/**
 * 统计事件
 * 行为日志：desc + "|" + resourceId  + "|" + scene + "|" + time
 * 广告日志：actionType、resourceId、scene
 * @author chenyanmin
 *
 */
public class StatEvent {
	
	public int type;// 统计事件的类型 
	
	public String desc;  // 行为日志ID，比如1701 (即时上传的日志，不用此字段)
	
	public String resourceId;  //事件操作对应的资源ID 
	
	public int actionType; // 操作类型：0展示 1点击 2下载 3短信购买支付成功通知   5应用安装后首次点击（此时resourceId为包名） 6 应用安装完成 
	
	public String scene; // 事件的附加参数信息

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public int getActionType() {
		return actionType;
	}

	public void setActionType(int actionType) {
		this.actionType = actionType;
	}

	public String getScene() {
		return scene;
	}

	public void setScene(String scene) {
		this.scene = scene;
	}

    @Override
	public String toString() {
		return "type="+ type +" desc="+desc+" resourceId="+resourceId+" actionType="+actionType+" scene="+scene;
	}
}
