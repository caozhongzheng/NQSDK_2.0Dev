package com.nqmobile.livesdk.modules.weather.model;

import com.nq.interfaces.weather.TLifeExponent;

/**
 * Created by Rainbow on 14-2-24.
 */
public class LifeExponent {

    public String type; //指数类型:感冒指数
    public String desc; //指数描述:较易发
    public String tips; //指数提示:天凉，湿度大，较易感冒。

    public LifeExponent(){

    }

    public LifeExponent(TLifeExponent lifeExponent){
        type = lifeExponent.type;
        desc = lifeExponent.desc;
        tips = lifeExponent.tips;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}
