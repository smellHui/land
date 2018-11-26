package com.hawk.map.polygon.vo;

import android.support.annotation.DrawableRes;

import com.hawk.map.polygon.PlotUtils;

import java.util.LinkedList;

public class HandleStyle {

	private LinkedList<StatusValue<Integer>> mSize = new LinkedList<>();
	private LinkedList<StatusValue<Integer>> mBackground = new LinkedList<>();

	public void setSize(int status, int value) {
		PlotUtils.setStatusValue(mSize, status, value);
	}

	public void setBackground(int status, @DrawableRes int value) {
		PlotUtils.setStatusValue(mBackground, status, value);
	}

	public int getSize(int status, int defaultValue) {
		return PlotUtils.getStatusValue(mSize, status, defaultValue);
	}

	public int getBackground(int status, @DrawableRes int defaultValue) {
		return PlotUtils.getStatusValue(mBackground, status, defaultValue);
	}
}
