package com.hawk.map.polygon.core;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.MotionEvent;

import com.amap.api.maps.model.LatLng;
import com.hawk.map.polygon.TouchType;
import com.hawk.map.polygon.impl.Vertex;
import com.hawk.map.polygon.vo.VertexStyle;

public interface IVertex extends Cloneable {
	/**
	 * 普通顶点
	 */
	int TYPE_NORMAL = 1;
	/**
	 * 中间点
	 */
	int TYPE_MIDDLE = 2;

	/**
	 * 获取顶点的状态
	 *
	 * @return 顶点的状态
	 */
	int getStatus();

	/**
	 * 设置顶点所在的多边形
	 *
	 * @param parent 顶点所在的多边形
	 */
	void setParent(@NonNull IPolygon parent);

	/**
	 * 获取顶点所在的多边形
	 *
	 * @return 顶点所在的多边形
	 */
	@NonNull
	IPolygon getParent();

	/**
	 * 获取顶点的经纬度
	 *
	 * @return 顶点的经纬度
	 */
	@NonNull
	LatLng getPosition();

	/**
	 * 获取顶点经纬度对应的屏幕坐标点
	 *
	 * @return 顶点经纬度对应的屏幕坐标点
	 */
	@NonNull
	Point getPoint();

	/**
	 * 设置顶点的经纬度
	 *
	 * @param position 顶点的经纬度
	 */
	void setPosition(@NonNull LatLng position);

	/**
	 * 设置是否可见
	 *
	 * @param visible true 可见，false 不可见
	 */
	void setVisible(boolean visible);

	/**
	 * 当前是否可见
	 *
	 * @return true 可见，false 不可见
	 */
	boolean isVisible();

	/**
	 * 设置是否获取焦点。通常一个用户正在操作的顶点认为是有焦点的。
	 * 同一时间仅可能有一个顶点有焦点。
	 * <b>注意，请慎重调用此方法，除非能确保当前没有其它顶点获取了焦点，否则将会违背“最多一个焦点”的原则，
	 * 因而引起难以预料的行为。通常情况下你应该调用 {@link IPolygon#switchFocusVertex(IVertex)} 方法，
	 * 这个方法会维护“最多一个焦点”的原则。</b>
	 *
	 * @param focused true 获取焦点，false 取消焦点
	 * @see IPolygon#switchFocusVertex(IVertex)
	 */
	void setFocused(boolean focused);

	/**
	 * 判断是否有焦点。通常一个用户正在操作的顶点认为是有焦点的。
	 * 同一时间仅可能有一个顶点是有焦点的。
	 *
	 * @return true 有焦点，false 没有焦点
	 */
	boolean isFocused();

	/**
	 * 获取该顶点的兄弟顶点
	 *
	 * @return 该顶点的兄弟顶点
	 */
	Pair<IVertex, IVertex> getSiblings();

	/**
	 * 设置是否被选中
	 *
	 * @param selected true 选中，false 取消选中
	 */
	void setSelected(boolean selected);

	/**
	 * 当前是否被选中
	 *
	 * @return true 被选中，false 没有选中
	 */
	boolean isSelected();

	/**
	 * 从地图移除并销毁该顶点
	 */
	void destroy();

	/**
	 * 是否已从地图移除并销毁
	 *
	 * @return true 已销毁，false 未销毁
	 */
	boolean isDestroyed();

	/**
	 * 设置顶点类型
	 *
	 * @param type 顶点类型
	 * @see #TYPE_NORMAL
	 * @see #TYPE_MIDDLE
	 */
	void setType(int type);

	/**
	 * 获取顶点类型
	 *
	 * @return {@link #TYPE_NORMAL} 普通顶点, {@link #TYPE_MIDDLE} 中间点
	 */
	int getType();

	/**
	 * 以给定屏幕像素距离偏移顶点
	 *
	 * @param dx 屏幕 x 轴距离
	 * @param dy 屏幕 y 轴距离
	 */
	void offset(int dx, int dy);

	/**
	 * 是否需要重新生成背景。注意，<b>重载默认实现的该方法时请慎重考虑，它可能会严重影响性能，请在确实需要重新生成背景图时才返回 true。
	 * 默认实现中该方法在每次调用 {@link #draw()} 时都会调用，重载时请尽量不要在该方法内部 new 对象。<b/>
	 *
	 * @return true 调用 {@link #createBackground()} 方法时重新生成背景图片，false 使用上一次生成的背景
	 * @see Vertex#isBackgroundDirty()
	 */
	boolean isBackgroundDirty();

	/**
	 * 绘制顶点。
	 *
	 * @see Vertex#draw()
	 */
	void draw();

	/**
	 * 顶点包含给定屏幕坐标类型
	 *
	 * @param x 屏幕坐标 x
	 * @param y 屏幕坐标 y
	 * @return {@link TouchType#NONE}，{@link TouchType#VERTEX}，{@link TouchType#INSIGHT_VERTEX}
	 */
	TouchType getTouchType(float x, float y);

	/**
	 * 设置绘制的层级，层级值越大，绘制越靠上层
	 *
	 * @param order 层级
	 */
	void setDrawOrder(int order);

	/**
	 * 获取绘制层级
	 *
	 * @see #setDrawOrder(int)
	 */
	int getDrawOrder();

	/**
	 * 设置是否接收触摸事件。
	 *
	 * @param touchable true 接收触摸事件，false 不接收
	 */
	void setTouchable(boolean touchable);

	/**
	 * 判断顶点是否可触摸
	 *
	 * @return 顶点是否可触摸
	 */
	boolean isTouchable();

	/**
	 * 添加一个 tag 到该对象中
	 *
	 * @param key 待添加 tag 的 key，通过该 key 可用取出 tag
	 * @param tag 待添加的 tag
	 */
	void setTag(int key, @NonNull Object tag);

	/**
	 * 获取已添加的 tag
	 *
	 * @param key 已添加的 tag 的 key
	 * @return 已添加的 tag，如果没有则返回 null
	 */
	@Nullable
	Object getTag(int key);

	/**
	 * 设置顶点样式
	 *
	 * @param style 顶点样式
	 */
	void setStyle(VertexStyle style);

	/**
	 * 获取顶点样式
	 *
	 * @return 顶点样式
	 */
	@NonNull
	VertexStyle getStyle();

	/**
	 * 生成背景图。注意，<b>重载默认实现的该方法时请慎重考虑，它可能会严重影响性能，请先调用 {@link #isBackgroundDirty()} 方法判断
	 * 是否需要重新生成背景图，请尽量使用缓存。
	 * 默认实现的该方法在每次调用 {@link #draw()} 时都会调用，重载时请尽量不要在该方法内部 new 对象。<b/>
	 *
	 * @return 背景图
	 */
	@NonNull
	Bitmap createBackground();

	/**
	 * 刷新顶点。默认实现的该方法并不会导致该顶点立即重新绘制，而是设置自身需要重绘，并请求多边形绘制，在其下一次绘制中重绘
	 */
	void invalidate();

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
	 * 顶点处理触摸事件
	 *
	 * @param event     触摸事件
	 * @param touchType 触摸类型
	 * @return true 消费事件，false 不消费
	 */
	boolean onTouchEvent(MotionEvent event, TouchType touchType);

	/**
	 * 请求显示把手在该顶点上
	 *
	 * @return 把手是否显示，true 显示，false 未显示
	 */
	boolean requestShowHandle();

	/**
	 * 克隆
	 * @return 克隆的对象
	 */
	IVertex clone();
}
