package com.hawk.map.polygon.core;

import com.hawk.map.polygon.impl.PolygonPlotter;

/**
 * 画地块所需对象的工厂
 */
public interface IPlotterFactory {

	/**
	 * 创建多边形对象
	 *
	 * @return 创建的多边形
	 */
	IPolygon createPolygon(PolygonPlotter parent);

	/**
	 * 创建多边形顶点对象
	 *
	 * @param type 顶点类型 {@link IVertex#TYPE_NORMAL}，{@link IVertex#TYPE_MIDDLE}
	 * @return 创建的多边形顶点
	 */
	IVertex createVertex(int type, IPolygon parent);

	/**
	 * 创建把手对象
	 *
	 * @return 创建的把手
	 */
	IHandle createHandle(IPolygon parent);

	/**
	 * 创建多边形历史记录管理对象
	 *
	 * @param polygon 历史记录所属的多边形
	 * @return 多边形历史记录管理对象
	 */
	IHistoryManager createHistoryManager(IPolygon polygon);
}
