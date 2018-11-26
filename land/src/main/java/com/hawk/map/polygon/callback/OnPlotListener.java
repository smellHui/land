package com.hawk.map.polygon.callback;

import com.hawk.map.polygon.core.IPolygon;
import com.hawk.map.polygon.core.IVertex;

/**
 * Created on 2018/11/20 17:58
 *
 * @author WingHawk
 */
// FIXME: 2018/11/22 该接口中的方法目前仅仅是为了画地块的功能调用，需要调整调用时机。
public interface OnPlotListener {

	/**
	 * 目前该方法在多边形有顶点加入、删除、拖动、以及闭合时会被调用
	 *
	 * @param polygon 有改变的多边形
	 */
	void onChanged(IPolygon polygon);

	/**
	 * 在顶点类型改变时会被调用
	 *
	 * @param vertex 改变的顶点
	 */
	void onChanged(IVertex vertex);

	/**
	 * 多边形焦点改变时被调用
	 *
	 * @param polygon 焦点改变的多边形
	 */
	void onFocusChanged(IPolygon polygon);

	/**
	 * 顶点焦点改变或者请求显示把手时被调用
	 *
	 * @param vertex 获取到焦点的顶点
	 */
	void onFocusChanged(IVertex vertex);

	/**
	 * 多边形被点击时调用
	 */
	void onClick(IPolygon polygon);
}