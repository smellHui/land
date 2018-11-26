package com.hawk.map.polygon.vo;


import com.hawk.map.polygon.PlotUtils;

import java.util.LinkedList;

public class PolygonStyle {

	private LinkedList<StatusValue<Integer>> mFillColor = new LinkedList<>();
	private LinkedList<StatusValue<Integer>> mStrokeColor = new LinkedList<>();
	private LinkedList<StatusValue<Integer>> mStrokeWidth = new LinkedList<>();
	private LinkedList<StatusValue<Boolean>> mDashLine = new LinkedList<>();

	public void setFillColor(int status, int value) {
		PlotUtils.setStatusValue(mFillColor, status, value);
	}

	public void setStrokeColor(int status, int value) {
		PlotUtils.setStatusValue(mStrokeColor, status, value);
	}

	public void setStrokeWidth(int status, int value) {
		PlotUtils.setStatusValue(mStrokeWidth, status, value);
	}

	public void setDashLine(int status, boolean value) {
		PlotUtils.setStatusValue(mDashLine, status, value);
	}

	public int getFillColor(int status, int defaultValue) {
		return PlotUtils.getStatusValue(mFillColor, status, defaultValue);
	}

	public int getStrokeColor(int status, int defaultValue) {
		return PlotUtils.getStatusValue(mStrokeColor, status, defaultValue);
	}

	public int getStrokeWidth(int status, int defaultValue) {
		return PlotUtils.getStatusValue(mStrokeWidth, status, defaultValue);
	}

	public boolean isDashLine(int status, boolean defaultValue) {
		return PlotUtils.getStatusValue(mDashLine, status, defaultValue);
	}
}
