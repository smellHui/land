package com.hawk.map.polygon;

/**
 * 触摸类型
 * Created on 2018/11/21 13:43
 *
 * @author WingHawk
 */
public enum TouchType {
	/**
	 * 触摸点在多边形的把手上
	 */
	HANDLE(1000000),
	/**
	 * 触摸点在多边形顶点的可见范围中
	 */
	INSIGHT_VERTEX(1000000),
	/**
	 * 触摸点在多边形的顶点上
	 */
	VERTEX(600000),
	/**
	 * 触摸点在多边形中
	 */
	POLYGON(300000),
	/**
	 * 触摸点不在多边形上
	 */
	NONE(-1);

	private int priority;

	TouchType(int priority) {
		this.priority = priority;
	}

	/**
	 * 获取触摸类型的优先级
	 */
	public int getPriority() {
		return priority;
	}
}
