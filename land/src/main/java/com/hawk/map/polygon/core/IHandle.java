package com.hawk.map.polygon.core;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.amap.api.maps.model.LatLng;
import com.hawk.map.polygon.vo.HandleStyle;

/**
 * 编辑多边形时辅助拖拽顶点的把手。把手永远只会指向有焦点的顶点。
 */
public interface IHandle {

	/**
	 * 设置把手样式
	 *
	 * @param style 把手样式
	 */
	void setStyle(HandleStyle style);

	/**
	 * 获取把手当前样式
	 *
	 * @return 把手当前样式
	 */
	HandleStyle getStyle();

	/**
	 * 设置把手旋转角度，注意，高德地图的 marker 旋转角度以 12 点钟方向为起点逆时针计算。
	 *
	 * @param rotation 旋转角度
	 */
	void setRotation(int rotation);

	/**
	 * 获取把手当前旋转角度，注意，高德地图的 marker 旋转角度以 12 点钟方向为起点逆时针计算。
	 *
	 * @return 旋转角度
	 */
	int getRotation();

	/**
	 * 给定屏幕坐标点是否在把手上
	 *
	 * @param x 屏幕坐标点 x
	 * @param y 屏幕坐标点 y
	 * @return true 代表包含，false 不包含
	 */
	boolean contains(float x, float y);

	/**
	 * 设置把手在地图上的经纬度位置
	 *
	 * @param position 把手在地图上的经纬度位置
	 */
	void setPosition(LatLng position);

	/**
	 * 获取把手在地图上的经纬度位置
	 *
	 * @return 把手在地图上的经纬度位置
	 */
	LatLng getPosition();

	/**
	 * 设置把手显示或隐藏
	 *
	 * @param visible true 显示把手，false 隐藏把手
	 */
	void setVisible(boolean visible);

	/**
	 * 获取把手当前可见状态
	 *
	 * @return true 可见，false 隐藏
	 */
	boolean isVisible();

	/**
	 * 设置把手所在的多边形
	 *
	 * @param parent 把手所在的多边形
	 */
	void setParent(@NonNull IPolygon parent);

	/**
	 * 获取把手所在的多边形
	 *
	 * @return 把手所在的多边形
	 * @see #setParent(IPolygon)
	 */
	@NonNull
	IPolygon getParent();

	/**
	 * 刷新把手。默认实现的该方法并不会导致该把手立即重新绘制，而是设置自身需要重绘，并请求多边形绘制，在其下一次绘制中重绘
	 */
	void invalidate();

	/**
	 * 把手处理触摸事件
	 *
	 * @param event       触摸事件
	 * @return true 消费事件，false 不消费
	 */
	boolean onTouchEvent(MotionEvent event);

	/**
	 * 显示把手并指向给定顶点
	 *
	 * @param vertex 顶点
	 */
	void indicate(IVertex vertex);

	/**
	 * 是否需要重绘
	 *
	 * @return true 需要，false 不需要
	 */
	boolean isDirty();

	/**
	 * 设置是否需要重绘
	 *
	 * @param dirty true 需要，false 不需要
	 */
	void setDirty(boolean dirty);

	/**
	 * 获取把手当前状态
	 *
	 * @return 把手当前状态
	 */
	int getStatus();

	/**
	 * 生成背景图。
	 *
	 * @return 背景图
	 */
	@NonNull
	Bitmap createBackground();

	/**
	 * 是否需要重新生成背景。
	 *
	 * @return true 调用 {@link #createBackground()} 方法时重新生成背景图片，false 使用上一次生成的背景
	 */
	boolean isBackgroundDirty();

	/**
	 * 获取绘制层级
	 *
	 * @return 绘制层级
	 */
	int getDrawOrder();

	/**
	 * 设置绘制层级
	 *
	 * @param drawOrder 绘制层级
	 */
	void setDrawOrder(int drawOrder);

	/**
	 * 以给定屏幕像素距离偏移把手
	 *
	 * @param dx 屏幕 x 轴距离
	 * @param dy 屏幕 y 轴距离
	 */
	void offset(int dx, int dy);

	/**
	 * 设置把手是否自动修正角度。
	 * 该方法目前未实现。默认自动修正角度。
	 *
	 * @param autoRotate true 自动修正角度，false 不自动修正
	 */
	void setAutoRotate(boolean autoRotate);

	/**
	 * 把手是否自动修正角度
	 *
	 * @return true 自动修正角度，false 不自动修正
	 */
	boolean isAutoRotate();

	/**
	 * 是否已从地图移除并销毁
	 *
	 * @return true 已销毁，false 未销毁
	 */
	boolean isDestroyed();

	/**
	 * 绘制把手
	 */
	void draw();

	/**
	 * 移除该把手
	 */
	void destroy();
}