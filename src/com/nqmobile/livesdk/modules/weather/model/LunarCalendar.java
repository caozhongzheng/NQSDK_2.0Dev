package com.nqmobile.livesdk.modules.weather.model;

import com.nq.interfaces.weather.TLunarCalendar;

/**
 * Created by Rainbow on 14-2-24.
 */
public class LunarCalendar {

    public String day; //农历日期
    public String gz; //天干地支纪年
    public String sx; //生肖年
    public String jq; //24节气
    public String jr; //农历节日
    public String suit; //宜
    public String avoid; //忌

    public LunarCalendar(){

    }

    public LunarCalendar(TLunarCalendar calendar){
        day = calendar.day;
        gz = calendar.gz;
        sx = calendar.sx;
        jq = calendar.jq;
        jr = calendar.jr;
        suit = calendar.suit;
        avoid = calendar.avoid;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getGz() {
        return gz;
    }

    public void setGz(String gz) {
        this.gz = gz;
    }

    public String getSx() {
        return sx;
    }

    public void setSx(String sx) {
        this.sx = sx;
    }

    public String getJq() {
        return jq;
    }

    public void setJq(String jq) {
        this.jq = jq;
    }

    public String getJr() {
        return jr;
    }

    public void setJr(String jr) {
        this.jr = jr;
    }

	public String getSuit() {
		return suit;
	}

	public void setSuit(String suit) {
		this.suit = suit;
	}

	public String getAvoid() {
		return avoid;
	}

	public void setAvoid(String avoid) {
		this.avoid = avoid;
	}
}
