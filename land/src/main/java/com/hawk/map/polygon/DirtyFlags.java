package com.hawk.map.polygon;

/**
 * Created on 2018/11/23 9:18
 *
 * @author WingHawk
 */
public interface DirtyFlags {
	/**
	 * 样式改变
	 */
	int STYLE = 1;
	/**
	 * 位置改变
	 */
	int POSITION = STYLE << 1;
	/**
	 * 绘制层级改变
	 */
	int DRAW_ORDER = POSITION << 1;
	/**
	 * 可见改变
	 */
	int VISIBLE = DRAW_ORDER << 1;
}
