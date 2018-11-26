package com.hawk.map.polygon.vo;

import android.support.annotation.DrawableRes;
import android.util.SparseArray;

import com.hawk.map.polygon.PlotUtils;

import java.util.LinkedList;

public class VertexStyle {

	private SparseArray<LinkedList<StatusValue<Integer>>> mSizeValues = new SparseArray<>();
	private SparseArray<LinkedList<StatusValue<Integer>>> mBackgroundValues = new SparseArray<>();
	private SparseArray<LinkedList<StatusValue<Integer>>> mTouchSizeValues = new SparseArray<>();

	public void setSize(int type, int status, int value) {
		LinkedList<StatusValue<Integer>> values = mSizeValues.get(type);
		if (values == null) {
			values = new LinkedList<>();
			mSizeValues.put(type, values);
		}
		PlotUtils.setStatusValue(values, status, value);
	}

	public void setBackground(int type, int status, @DrawableRes int value) {
		LinkedList<StatusValue<Integer>> values = mBackgroundValues.get(type);
		if (values == null) {
			values = new LinkedList<>();
			mBackgroundValues.put(type, values);
		}
		PlotUtils.setStatusValue(values, status, value);
	}

	public void setTouchSize(int type, int status, int value) {
		LinkedList<StatusValue<Integer>> values = mTouchSizeValues.get(type);
		if (values == null) {
			values = new LinkedList<>();
			mTouchSizeValues.put(type, values);
		}
		PlotUtils.setStatusValue(values, status, value);
	}

	public int getSize(int type, int status, int defaultValue) {
		LinkedList<StatusValue<Integer>> values = mSizeValues.get(type);
		if (values == null || values.isEmpty()) {
			return defaultValue;
		}
		return PlotUtils.getStatusValue(values, status, defaultValue);
	}

	public int getBackground(int type, int status, @DrawableRes int defaultValue) {
		LinkedList<StatusValue<Integer>> values = mBackgroundValues.get(type);
		if (values == null || values.isEmpty()) {
			return defaultValue;
		}
		return PlotUtils.getStatusValue(values, status, defaultValue);
	}

	public int getTouchSize(int type, int status, int defaultValue) {
		LinkedList<StatusValue<Integer>> values = mTouchSizeValues.get(type);
		if (values == null || values.isEmpty()) {
			return defaultValue;
		}
		return PlotUtils.getStatusValue(values, status, defaultValue);
	}
}
