package com.hawk.map.polygon;

public interface Status {
	/**
	 * 默认状态
	 */
	int DEFAULT = 1;
	/**
	 * 多边形闭合
	 */
	int POLYGON_CLOSED = DEFAULT << 1;
	/**
	 * 多边形自相交
	 */
	int POLYGON_CROSSED = POLYGON_CLOSED << 1;
	/**
	 * 多边形可编辑
	 */
	int POLYGON_EDITABLE = POLYGON_CROSSED << 1;
	/**
	 * 多边形已选中
	 */
	int POLYGON_SELECTED = POLYGON_EDITABLE << 1;
	/**
	 * 多边形获取到焦点
	 */
	int POLYGON_FOCUSED = POLYGON_SELECTED << 1;
	/**
	 * 多边形可触摸
	 */
	int POLYGON_TOUCHABLE = POLYGON_FOCUSED << 1;
	/**
	 * 多边形可见
	 */
	int POLYGON_VISIBLE = POLYGON_TOUCHABLE << 1;

	/**
	 * 顶点已选中
	 */
	int VERTEX_SELECTED = POLYGON_VISIBLE << 1;
	/**
	 * 顶点获取到焦点
	 */
	int VERTEX_FOCUSED = VERTEX_SELECTED << 1;
	/**
	 * 顶点可见
	 */
	int VERTEX_VISIBLE = VERTEX_FOCUSED << 1;
	/**
	 * 顶点可触摸
	 */
	int VERTEX_TOUCHABLE = VERTEX_VISIBLE << 1;
	/**
	 * 需要重绘
	 */
	int DIRTY = VERTEX_TOUCHABLE << 1;
	/**
	 * 把手可见
	 */
	int HANDLE_VISIBLE = DIRTY << 1;
	/**
	 * 把手自动旋转
	 */
	int HANDLE_AUTO_ROTATE = HANDLE_VISIBLE << 1;
	/**
	 * 已从地图移除并销毁
	 */
	int DESTROYED = HANDLE_AUTO_ROTATE << 1;
}
