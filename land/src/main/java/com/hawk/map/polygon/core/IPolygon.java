package com.hawk.map.polygon.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.MotionEvent;

import com.amap.api.maps.model.LatLng;
import com.hawk.map.core.IMapFunctions;
import com.hawk.map.polygon.DirtyFlags;
import com.hawk.map.polygon.TouchType;
import com.hawk.map.polygon.callback.IPolygonHook;
import com.hawk.map.polygon.impl.PolygonPlotter;
import com.hawk.map.polygon.impl.Polygone;
import com.hawk.map.polygon.vo.HandleStyle;
import com.hawk.map.polygon.vo.PolygonStyle;
import com.hawk.map.polygon.vo.VertexStyle;

import java.util.List;

/**
 * 多边形对象
 */
public interface IPolygon {
	/**
	 * 设置多边形所在的绘制对象
	 *
	 * @param parent 多边形所在的绘制对象
	 */
	void setParent(PolygonPlotter parent);

	/**
	 * 添加一个 tag 到该对象中
	 *
	 * @param key 待添加 tag 的 key，通过该 key 可用取出 tag
	 * @param tag 待添加的 tag
	 */
	void setTag(int key, @NonNull Object tag);

	/**
	 * 获取添加的 tag
	 *
	 * @param key 已添加的 tag 的 key
	 * @return 已添加的 tag，如果没有则返回 null
	 */
	@Nullable
	Object getTag(int key);

	/**
	 * 获取多边形所在的地图对象
	 *
	 * @return 多边形所在的地图功能对象
	 */
	@NonNull
	IMapFunctions getMapFunctions();

	/**
	 * 将经纬度集合作为顶点的坐标创建顶点并设置给多边形，创建的顶点会自动插入中间点。
	 *
	 * @param points   多边形顶点坐标
	 * @param isClosed 多边形是否闭合，如果顶点数小于三，该参数无效
	 */
	void setPositions(List<LatLng> points, boolean isClosed);

	/**
	 * 在两个普通顶点之间插入中间点
	 *
	 * @param preVtx  上一个普通顶点
	 * @param nextVtx 下一个普通顶点
	 */
	void addMiddleVertex(IVertex preVtx, IVertex nextVtx);

	/**
	 * 获取多边形顶点经纬度集合，<b>不包括中间点</b>。如果暂无顶点，返回空集合
	 *
	 * @return 多边形顶点经纬度集合
	 */
	@NonNull
	List<LatLng> getPositions();

	/**
	 * 获取多边形顶点对象集合。如果暂无顶点，返回空集合。
	 * 注意，默认实现中，为确保操作入口一致性，<b>该集合是只读的，任何对该集合的写入操作都会抛出异常</b>，
	 * 如需类似操作，请调用其它方法，比如 {@link #addVertex(int, IVertex)}，{@link #removeVertex(IVertex)} 和
	 * {@link #setVertex(int, IVertex)}
	 *
	 * @return 多边形顶点对象集合
	 * @see Polygone#getVertices()
	 */
	@NonNull
	List<IVertex> getVertices();

	/**
	 * 多边形分发触摸事件。注意，该方法默认的实现会根据最近一次调用 {@link #getTouchType(MotionEvent)} 方法的返回值
	 * 进行事件分发，请慎重调用 {@link #getTouchType(MotionEvent)} 方法。
	 *
	 * @param event 触摸事件
	 * @return true 消费此事件，false 不消费
	 */
	boolean dispatchTouchEvent(@NonNull MotionEvent event);

	void registerHook(IPolygonHook hook);

	void unregisterHook(IPolygonHook hook);

	/**
	 * 多边形处理触摸事件
	 *
	 * @param event 触摸事件
	 * @return true 消费此事件，false 不消费
	 */
	boolean onTouchEvent(@NonNull MotionEvent event);

	/**
	 * 获取在屏幕座标点处的顶点
	 *
	 * @param x 屏幕坐标 x
	 * @param y 屏幕坐标 y
	 * @return 触摸类型和触摸点的 Pair
	 */
	Pair<TouchType, IVertex> findTouchVertex(float x, float y);

	/**
	 * 设置多边形的样式
	 *
	 * @param style 多边形的样式
	 */
	void setStyle(@NonNull PolygonStyle style);

	/**
	 * 设置是否需要重绘
	 *
	 * @param dirtyFlag 需要重绘的类型
	 * @see DirtyFlags
	 */
	void setDirty(int dirtyFlag);

	/**
	 * 获取多边形的样式
	 *
	 * @return 多边形的样式
	 */
	@NonNull
	PolygonStyle getStyle();

	/**
	 * 设置是否可编辑
	 *
	 * @param editable true 可编辑，false 不可
	 */
	void setEditable(boolean editable);

	/**
	 * 获取多边形面积，如果多边形未闭合，返回 0
	 *
	 * @return 多边形面积
	 */
	double getArea();

	/**
	 * 获取多边形周长，如果多边形只有一个顶点，返回 0
	 *
	 * @return 多边形周长
	 */
	double getPerimeter();

	/**
	 * 当前是否可编辑
	 *
	 * @return true 可编辑，false 不可
	 */
	boolean isEditable();

	/**
	 * 设置是否接收触摸事件。
	 *
	 * @param touchable true 接收触摸事件，false 不接收
	 */
	void setTouchable(boolean touchable);

	/**
	 * 当前是否可响应触摸事件
	 *
	 * @return true 可响应，false 不可
	 */
	boolean isTouchable();

	/**
	 * 设置是否闭合
	 *
	 * @param closed true 闭合，false 不闭合
	 */
	void setClosed(boolean closed);

	/**
	 * 当前是否已闭合
	 *
	 * @return true 已闭合，false 未闭合
	 */
	boolean isClosed();

	/**
	 * 当前是否为一个有效的多边形。通常一个多边形已闭合且不自相交则认为它是有效的。
	 *
	 * @return true 是，false 否
	 * @see #isClosed()
	 * @see #isCrossed()
	 */
	boolean isValid();

	/**
	 * 当前多边形是否自相交
	 *
	 * @return true 自相交，false 不相交
	 */
	boolean isCrossed();

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
	 * 添加顶点
	 *
	 * @param index  添加的位置
	 * @param vertex 要添加的顶点
	 */
	void addVertex(int index, @NonNull IVertex vertex);

	/**
	 * 修改多边形给定位置的顶点为新的顶点
	 *
	 * @param index  修改的位置
	 * @param vertex 新的顶点
	 */
	void setVertex(int index, @NonNull IVertex vertex);

	/**
	 * 删除顶点。
	 *
	 * @param vertex 要删除的顶点
	 */
	void removeVertex(@Nullable IVertex vertex);

	/**
	 * 移除一个普通顶点。如果这个顶点左右两边都有中间点，则将他变为中间点并移除它左右两边的中间点；
	 * 否则移除这个顶点和左右两侧存在的中间点。如果闭合的多边形移除普通顶点之后导致无法闭合，会修改闭合状态并自动移除
	 * 多边形顶点列表头部或尾部的中间点。
	 * <pre>
	 * 假设以数字作为普通顶点，小 o 作为中间点，多边形顶点列表如下：
	 * 1o2o3o4o5o
	 * 调用该方法移除第一个普通顶点后多边形的顶点列表为：
	 * 2o3o4o5o
	 * 再调用该方法移除最后一个普通顶点后多边形的顶点列表为：
	 * 2o3o4o
	 * 再调用该方法移除任意一个普通顶点后多边形的顶点列表为：
	 * 2o3 或 3o4 或 2o4
	 * </pre>
	 *
	 * @param vertex 要移除的普通顶点
	 */
	void removeNormalVertex(IVertex vertex);

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
	 * 设置是否获取焦点。通常一个用户正在操作的多边形认为是有焦点的。
	 * 同一时间仅可能有一个多边形有焦点。
	 * <b>注意，请慎重调用此方法，除非能确保当前没有其它多边形获取了焦点，否则将会违背“最多一个焦点”的原则，
	 * 因而引起难以预料的行为。通常情况下你应该调用 {@link PolygonPlotter#switchFocusPolygon(IPolygon)} 方法，
	 * 这个方法会维护“最多一个焦点”的原则。</b>
	 *
	 * @param focused true 获取焦点，false 取消焦点
	 * @see PolygonPlotter#switchFocusPolygon(IPolygon)
	 */
	void setFocused(boolean focused);

	/**
	 * 判断是否有焦点。通常一个用户正在操作的多边形认为是有焦点的。
	 * 同一时间仅可能有一个多边形有焦点。
	 *
	 * @return true 有焦点，false 没有焦点
	 */
	boolean isFocused();

	/**
	 * 获取当前有焦点的顶点。通常一个用户正在操作的顶点是有焦点的。
	 * 同一时间仅可能有一个顶点是有焦点的。
	 *
	 * @return 当前有焦点的顶点
	 */
	@Nullable
	IVertex getFocusedVertex();

	/**
	 * 设置绘制的层级，层级值越大，绘制越靠上层
	 *
	 * @param order 层级
	 */
	void setDrawOrder(int order);

	/**
	 * 获取绘制层级
	 *
	 * @return 绘制层级
	 * @see #setDrawOrder(int)
	 */
	int getDrawOrder();

	/**
	 * 获取多边形所在的绘制对象
	 *
	 * @return 多边形所在的绘制对象
	 */
	@NonNull
	PolygonPlotter getParent();

	/**
	 * 获取该多边形编辑的历史记录管理器
	 *
	 * @return 历史记录管理器
	 */
	@NonNull
	IHistoryManager getHistoryManager();

	/**
	 * 设置该多边形编辑的历史记录管理器
	 *
	 * @param historyManager 多边形编辑的历史记录管理器
	 */
	void setHistoryManager(@NonNull IHistoryManager historyManager);

	/**
	 * 获取多边形的状态
	 *
	 * @return 多边形的状态
	 */
	int getStatus();

	/**
	 * 刷新该多边形
	 */
	void invalidate();

	/**
	 * 获取多边形上的把手，如果该多边形上没有把手，返回 null
	 *
	 * @return 多边形上的把手
	 */
	IHandle getHandle();

	/**
	 * 返回多边形包含屏幕触摸点的类型。注意，{@link #dispatchTouchEvent(MotionEvent)} 方法默认的实现会根据本
	 * 方法最近一次的返回值进行事件分发，请慎重调用本方法。
	 *
	 * @param event 触摸事件
	 * @return 多边形包含屏幕触摸点的类型
	 * @see #dispatchTouchEvent(MotionEvent)
	 */
	TouchType getTouchType(MotionEvent event);

	/**
	 * 移除该多边形和它所有的顶点以及其中的把手
	 */
	void destroy();

	/**
	 * 新增一个普通顶点。
	 * 注意，默认实现的该方法与 {@link #addVertex(int, IVertex)} 不同，它不仅会新增一个顶点，也会插入必要的中间点。
	 *
	 * @param index  添加的位置
	 * @param vertex 添加的顶点
	 */
	void addNormalVertex(int index, IVertex vertex);

	/**
	 * 设置多边形上的把手是否可见。<b>注意，默认实现中，该方法设置 true 并不意味着多边形上的把手会立即显示，
	 * 仅仅代表多边形上的把手可以显示。</b>
	 * 如果设置为 false，正在显示的把手会隐藏。
	 * 如需显示把手，请保证多边形对象（IPolygon）以及多边形绘制对象（PolygonPlotter）允许把手显示，
	 * 并调用把手上的方法 {@link IHandle#setVisible(boolean)}。
	 *
	 * @param visible true 可见，false 不可见
	 */
	void setHandleVisible(boolean visible);

	/**
	 * 多边形上的把手是否可见。<b>注意，默认实现中，该方法返回 true 并不意味着多边形上有把手正在显示，仅仅代表多边形上的把手可以显示。</b>
	 * 如需知道把手是否显示，请调用把手上的方法 {@link IHandle#isVisible()}。
	 *
	 * @return true 可见，false 不可见
	 */
	boolean isHandleVisible();

	/**
	 * 设置默认顶点样式。只有在某个顶点的样式没有设置时才会采用这个值。
	 *
	 * @param style 默认顶点样式
	 */
	void setDefaultVertexStyle(VertexStyle style);

	/**
	 * 设置默认把手样式。只有在把手的样式没有设置时才会采用这个值。
	 *
	 * @param style 默认把手样式
	 */
	void setDefaultHandleStyle(HandleStyle style);

	/**
	 * 获取默认把手样式。
	 *
	 * @return 默认把手样式
	 */
	HandleStyle getDefaultHandleStyle();

	/**
	 * 获取默认顶点样式。
	 *
	 * @return 默认顶点样式
	 */
	VertexStyle getDefaultVertexStyle();

	/**
	 * 顶点抽稀。注意，该方法调用后可能会将一个已闭合的多边形变为未闭合状态（抽稀后顶点数小于三）。
	 *
	 * @param tolerance 可忍受的偏差（单位 px），这个值越大，偏差越大，点越少
	 */
	void compress(double tolerance);

	/**
	 * 绘制多边形。调用该方法会立即重绘多边形及上面的顶点和把手。
	 * 如非必要，请调用 {@link #invalidate()} 方法。
	 */
	void draw();

	/**
	 * 是否已从地图移除并销毁
	 *
	 * @return true 已销毁，false 未销毁
	 */
	boolean isDestroyed();

	/**
	 * 切换闭合多边形。注意，该方法不同与 {@link #setClosed(boolean)}，它不仅仅会改变闭合状态，也会执行一些必要的操作，
	 * 比如在闭合时插入需要的中间点，取消闭合时删除多余的中间点。
	 *
	 * @param close true 闭合，false 不闭合
	 */
	void switchClose(boolean close);

	/**
	 * 切换焦点顶点
	 *
	 * @param vertex 焦点顶点
	 */
	void switchFocusVertex(IVertex vertex);

	/**
	 * 切换顶点类型。
	 * 注意，默认实现的该方法与 {@link IVertex#setType(int)} 不同，它不仅会改变顶点类型的值，也会执行一些必要的操作。
	 * 比如将一个普通顶点变为中间点时，会自动移除它左右两边的中间点，将一个中间点变为顶点时，会自动在它两边插入新的中间点。
	 *
	 * @param vertex 顶点
	 * @param type   顶点类型
	 */
	void switchVertexType(IVertex vertex, int type);

	List<IPolygonHook> getPolygonHooks();

	/**
	 * 设置普通顶点的位置。
	 * 注意，默认实现的该方法与 {@link IVertex#setPosition(LatLng)} 不同，它不仅会移动顶点，也会同时移动它旁边的中间点。
	 *
	 * @param vertex         普通顶点
	 * @param targetPosition 目标位置
	 */
	void moveNormalVertex(IVertex vertex, LatLng targetPosition);

	/**
	 * 执行点击事件
	 */
	void performClick();
}

