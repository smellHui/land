package com.hawk.map.polygon.impl;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.hawk.map.R;
import com.hawk.map.core.IMapFunctions;
import com.hawk.map.observer.IMapTouchObserver;
import com.hawk.map.polygon.PlotUtils;
import com.hawk.map.polygon.Status;
import com.hawk.map.polygon.TouchType;
import com.hawk.map.polygon.callback.OnPlotListener;
import com.hawk.map.polygon.core.IPlotterFactory;
import com.hawk.map.polygon.core.IPolygon;
import com.hawk.map.polygon.core.IVertex;
import com.hawk.map.polygon.vo.HandleStyle;
import com.hawk.map.polygon.vo.PolygonStyle;
import com.hawk.map.polygon.vo.VertexStyle;
import com.hawk.map.utils.SimpleGestureDetector;
import com.hawk.map.utils.SimpleGestureListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 多边形绘制类。
 * <br/>
 * Created on 2018/11/19 21:51
 *
 * @author WingHawk
 */
public class PolygonPlotter implements IMapTouchObserver {

	public static final int PLOTTER_PRIORITY = -1;
	// 用来让多边形获得焦点的顺序影响其绘制层级
	static int POLYGON_COUNTER;
	// 用来让顶点获得焦点的顺序影响其绘制层级
	static int VERTEX_COUNTER;
	private final SimpleGestureDetector mGestureDetector;
	private LinkedList<IPolygon> polygons = new LinkedList<>();
	private IPlotterFactory plotterFactory = new PlotterFactory();
	private IMapFunctions map;
	private IPolygon focusedPolygon;
	private PolygonStyle polygonStyle;
	private VertexStyle vertexStyle;
	private HandleStyle handleStyle;
	private OnPlotListener onPlotListener;
	private final Context context;
	private IPolygon mDownPolygon;
	private TouchType mDownTouchType;
	private boolean handleVisible = true;
	private boolean polygonAddable = true;
	private final int touchSlop;
	private boolean removeEmptyPolygonOnFocusLost = true;

	public PolygonPlotter(Context context) {
		this.context = context;
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop() / 3;
		mGestureDetector = new SimpleGestureDetector(new PlotGestureListener());
		mGestureDetector.setTouchSlop(touchSlop);
	}

	public Context getContext() {
		return context;
	}

	public void setPlotterFactory(@NonNull IPlotterFactory factory) {
		this.plotterFactory = factory;
	}

	/**
	 * 分发地图触摸事件。
	 * <ol>
	 * <li>在 down 事件中查找触摸的多边形，如果找到，禁止地图缩放和拖动，将 down 事件及其之后的事件都交给这个多边形处理</li>
	 * <li>如果 down 事件中找到了触摸的多边形，在 move 事件中将其设为焦点多边形</li>
	 * <li>如果 down 事件中没有找到触摸的多边形，在 up 事件后判断是否是点击事件，如果是，判断是否可添加新的多边形以及是否要切换多边形焦点。</li>
	 * </ol>
	 *
	 * @param event 触摸事件
	 * @return 是否消费此事件
	 */
	@Override
	public boolean onMapTouch(MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		if (mDownPolygon != null) {
			return mDownPolygon.dispatchTouchEvent(event);
		}
		return true;
	}

	int getTouchSlop() {
		return touchSlop;
	}

	/**
	 * 切换焦点多边形
	 *
	 * @param polygon 焦点多边形
	 */
	public void switchFocusPolygon(IPolygon polygon) {
		if (polygon != focusedPolygon) {
			if (focusedPolygon != null) {
				focusedPolygon.setFocused(false);
				// 判断是否要将多边形移除
				if (removeEmptyPolygonOnFocusLost &&
						focusedPolygon.getVertices().isEmpty() &&
						polygons.contains(focusedPolygon)) {
					removePolygon(focusedPolygon);
				}
			}
			if (polygon != null) {
				polygon.setFocused(true);
			}
			focusedPolygon = polygon;
		}
	}

	/**
	 * 移除一个多边形
	 *
	 * @param polygon 待移除的多边形
	 */
	public void removePolygon(IPolygon polygon) {
		polygons.remove(polygon);
		if (focusedPolygon == polygon) {
			switchFocusPolygon(null);
		}
		polygon.destroy();
	}

	OnPlotListener getOnPlotListener() {
		return onPlotListener;
	}

	private void setMapGestureEnable(boolean enable) {
		UiSettings uiSettings = getMapFunctions().getUiSettings();
		uiSettings.setScrollGesturesEnabled(enable);
		uiSettings.setZoomGesturesEnabled(enable);
	}

	private IVertex findTouchVertex(MotionEvent event) {
		IVertex preferVertex = null;
		TouchType preferTouchType = TouchType.NONE;
		ArrayList<IPolygon> polygons = new ArrayList<>();
		for (IPolygon p : polygons) {
			if (!p.isDestroyed() &&
					p.isVisible() &&
					p.isEditable() &&
					p.isTouchable() &&
					!p.getVertices().isEmpty()) {
				polygons.add(p);
			}
		}
		for (IPolygon polygon : polygons) {
			Pair<TouchType, IVertex> pair = polygon.findTouchVertex(event.getX(), event.getY());
			TouchType touchType = pair.first;
			IVertex touchVertex = pair.second;
			if (touchType == TouchType.VERTEX || touchType == TouchType.INSIGHT_VERTEX) {
				int o1 = preferTouchType.getPriority() + (preferVertex == null ? 0 : preferVertex.getDrawOrder());
				int o2 = touchType.getPriority() + (touchVertex == null ? 0 : touchVertex.getDrawOrder());
				if (o2 > o1) {
					preferVertex = touchVertex;
					preferTouchType = touchType;
				}
			}
		}
		return preferVertex;
	}

	public void setOnPlotListener(OnPlotListener onPlotListener) {
		this.onPlotListener = onPlotListener;
	}

	/**
	 * 查找触摸的多边形。查找顺序为：
	 * <ol>
	 * <li>查找当前未闭合且可编辑的多边形，如果存在则返回该多边形和触摸在该多边形上的类型</li>
	 * <li>遍历所有多边形，查找触摸类型，返回优先级最高的多边形。触摸在把手上或者点在顶点可视区域上的优先级最大，
	 * 其次是在顶点上，再次是在多边形中</li>
	 * <li>如果没有找到，返回 <code>new Pair<>(TouchType.NONE, null)</code> </li>
	 * </ol>
	 *
	 * @param event 触摸事件
	 * @return 找到的多边形和触摸类型
	 */
	private Pair<TouchType, IPolygon> findTouchPolygon(MotionEvent event) {
		// 查找当前未闭合且可编辑的多边形
		IPolygon polygon = findVertexAddablePolygon();
		if (polygon != null) {
			TouchType touchType = polygon.getTouchType(event);
			return new Pair<>(touchType, polygon);
		}
		IPolygon preferPolygon = null;
		TouchType preferTouchType = TouchType.NONE;
		for (IPolygon p : polygons) {
			// 取优先级最大的多边形
			TouchType touchType = p.getTouchType(event);
			if (touchType != TouchType.NONE) {
				// 加上多边形已有的 drawOrder，保证已获取焦点的多边形的优先级高于其它多边形
				int o1 = preferTouchType.getPriority() + (preferPolygon == null ? 0 : preferPolygon.getDrawOrder());
				int o2 = touchType.getPriority() + p.getDrawOrder();
				if (o2 > o1) {
					preferPolygon = p;
					preferTouchType = touchType;
				}
			}
		}
		return preferPolygon == null
				? new Pair<TouchType, IPolygon>(TouchType.NONE, null)
				: new Pair<>(preferTouchType, preferPolygon);
	}

	/**
	 * 设置是否可以新增多边形
	 *
	 * @param polygonAddable true 可以，false 不可
	 */
	public void setPolygonAddable(boolean polygonAddable) {
		this.polygonAddable = polygonAddable;
	}

	/**
	 * 是否可以新增多边形
	 *
	 * @return true 可以，false 不可
	 */
	public boolean isPolygonAddable() {
		return polygonAddable;
	}

	/**
	 * 设置把手是否可以显示。注意，调用该方法传入 true 并不意味着把手会立即显示，它只代表把手可以显示。
	 *
	 * @param handleVisible true 可见，false 不可见
	 * @see IPolygon#setHandleVisible(boolean)
	 */
	public void setHandleVisible(boolean handleVisible) {
		this.handleVisible = handleVisible;
		if (focusedPolygon != null) {
			focusedPolygon.setHandleVisible(handleVisible);
		}
	}

	/**
	 * 把手是否可以显示。该方法返回 true 并不意味着此时把手正在显示。
	 *
	 * @return true 可见，false 不可见
	 * @see IPolygon#isHandleVisible()
	 */
	boolean isHandleVisible() {
		return handleVisible;
	}

	@Override
	public void onAttach(IMapFunctions map) {
		this.map = map;
		UiSettings uiSettings = map.getUiSettings();
		uiSettings.setRotateGesturesEnabled(false);
		uiSettings.setTiltGesturesEnabled(false);
	}

	@Override
	public void onDetach() {
		while (!polygons.isEmpty()) {
			polygons.pollFirst().destroy();
		}
		switchFocusPolygon(null);
	}

	IPlotterFactory getPlotterFactory() {
		return plotterFactory;
	}

	IMapFunctions getMapFunctions() {
		return map;
	}

	/**
	 * 添加一个多边形
	 *
	 * @param points   多边形的普通顶点坐标点
	 * @param isClosed 多边形是否闭合
	 * @return 添加的多边形
	 */
	public IPolygon addPolygon(List<LatLng> points, boolean isClosed) {
		IPolygon polygon = plotterFactory.createPolygon(this);
		polygon.setPositions(points, isClosed);
		polygons.add(polygon);
		return polygon;
	}

	/**
	 * 设置地图上闭合的多边形。
	 *
	 * @param polygons 多边形
	 */
	public void setPolygons(List<List<LatLng>> polygons) {
		clear();
		for (List<LatLng> list : polygons) {
			addPolygon(list, true);
		}
	}

	/**
	 * 获取焦点多边形
	 *
	 * @return 焦点多边形
	 */
	@Nullable
	public IPolygon getFocusedPolygon() {
		return focusedPolygon;
	}

	/**
	 * 获取所有多边形。该方法返回的是一个不可变的集合，请勿直接在该方法的返回值中添加或者删除多边形，
	 * 应该调用{@link #addPolygon(List, boolean)} 或者 {@link #removePolygon(IPolygon)}。
	 *
	 * @return 所有多边形
	 */
	public List<IPolygon> getPolygons() {
		return Collections.unmodifiableList(polygons);
	}

	@Override
	public int getPriority() {
		return PLOTTER_PRIORITY;
	}

	PolygonStyle getDefaultPolygonStyle() {
		if (polygonStyle == null) {
			polygonStyle = createDefaultPolygonStyle();
		}
		return polygonStyle;
	}

	/**
	 * 设置默认的多边形样式
	 *
	 * @param polygonStyle 默认的多边形样式
	 */
	public void setDefaultPolygonStyle(PolygonStyle polygonStyle) {
		this.polygonStyle = polygonStyle;
	}

	VertexStyle getDefaultVertexStyle() {
		if (vertexStyle == null) {
			vertexStyle = createDefaultVertexStyle();
		}
		return vertexStyle;
	}

	/**
	 * 设置在在多边形丢失焦点时检查其是否没有顶点，如果没有则将其移除。
	 * 该值默认为 true。
	 * <p>
	 * 注意：
	 * <li>该方法并不能保证 {@link #getPolygons()} 方法返回的多边形都非空，因为当前的焦点多边形可能会没有顶点（多边形顶点被移除）。</li>
	 * <li>如果该值为 true，在外部调用多边形的方法时，可能多边形已被移除。</li>
	 * <li>如果该值为 false，调用 {@link #getPolygons()} 的返回值可能包含多个没有顶点的多边形（多边形顶点被移除）。</li>
	 *
	 * @param remove true 移除，false 不移除
	 */
	public void setRemoveEmptyPolygonOnFocusLost(boolean remove) {
		this.removeEmptyPolygonOnFocusLost = remove;
	}

	/**
	 * 设置默认的顶点样式
	 *
	 * @param vertexStyle 默认的顶点样式
	 */
	public void setDefalutVertexStyle(VertexStyle vertexStyle) {
		this.vertexStyle = vertexStyle;
	}

	private VertexStyle createDefaultVertexStyle() {
		VertexStyle style = new VertexStyle();
		style.setSize(IVertex.TYPE_NORMAL, Status.DEFAULT, PlotUtils.dp2px(context, 30));
		style.setSize(IVertex.TYPE_MIDDLE, Status.DEFAULT, PlotUtils.dp2px(context, 15));
		style.setBackground(IVertex.TYPE_NORMAL, Status.DEFAULT, R.drawable.white);
		style.setBackground(IVertex.TYPE_MIDDLE, Status.DEFAULT, R.drawable.white);
		style.setTouchSize(IVertex.TYPE_NORMAL, Status.DEFAULT, PlotUtils.dp2px(context, 30));
		style.setTouchSize(IVertex.TYPE_MIDDLE, Status.DEFAULT, PlotUtils.dp2px(context, 30));
		return style;
	}

	private PolygonStyle createDefaultPolygonStyle() {
		PolygonStyle style = new PolygonStyle();
		style.setFillColor(Status.DEFAULT, 0x66000000);
		style.setFillColor(Status.POLYGON_CROSSED, 0x66ff0000);
		style.setStrokeColor(Status.DEFAULT, Color.WHITE);
		style.setDashLine(Status.DEFAULT, true);
		style.setDashLine(Status.POLYGON_CLOSED, false);
		style.setStrokeWidth(Status.DEFAULT, PlotUtils.dp2px(context, 1));
		style.setStrokeWidth(Status.POLYGON_SELECTED + Status.POLYGON_CLOSED, PlotUtils.dp2px(context, 3));
		style.setStrokeWidth(Status.POLYGON_EDITABLE, PlotUtils.dp2px(context, 1));
		return style;
	}

	private HandleStyle createDefaultHandleStyle() {
		HandleStyle style = new HandleStyle();
		style.setBackground(Status.DEFAULT, R.drawable.handle);
		style.setSize(Status.DEFAULT, PlotUtils.dp2px(context, 52));
		return style;
	}

	/**
	 * 设置默认的把手样式
	 *
	 * @param style 默认的把手样式
	 */
	public void setDefaultHandleStyle(HandleStyle style) {
		this.handleStyle = style;
	}

	HandleStyle getDefaultHandleStyle() {
		if (handleStyle == null) {
			handleStyle = createDefaultHandleStyle();
		}
		return handleStyle;
	}

	public void clear() {
		while (!polygons.isEmpty()) {
			IPolygon polygon = polygons.getFirst();
			removePolygon(polygon);
		}
	}

	private class PlotGestureListener extends SimpleGestureListener {

		private float downX;
		private float downY;

		@Override
		public void onDown(MotionEvent event) {
			downX = event.getX();
			downY = event.getY();
			Pair<TouchType, IPolygon> pair = findTouchPolygon(event);
			mDownTouchType = pair.first;
			mDownPolygon = pair.second;
			setMapGestureEnable(!interceptMapTouchEvent());
		}

		@Override
		public void onMove(MotionEvent event) {
			if (interceptMapTouchEvent()) {
				switchFocusPolygon(mDownPolygon);
			}
		}

		@Override
		public void onUp(MotionEvent event) {
			setMapGestureEnable(true);
		}

		@Override
		public void onClick(MotionEvent event) {
			// 检查是否有可编辑且未闭合的多边形
			IPolygon polygon = findVertexAddablePolygon();
			if (polygon == null) {
				// 点击在顶点上则让顶点所在的多边形获取焦点
				IVertex vertex = findTouchVertex(event);
				if (vertex != null) {
					polygon = vertex.getParent();
					switchFocusPolygon(polygon);
					polygon.switchFocusVertex(vertex);
					vertex.requestShowHandle();
				} else {
					switchFocusPolygon(mDownPolygon);
				}
			}
			// 如果没有点在任何多边形上且允许新增多边形
			if ((focusedPolygon == null || focusedPolygon.getVertices().size() == 0) && polygonAddable) {
				if (focusedPolygon == null) {
					// 新增多边形
					polygon = plotterFactory.createPolygon(PolygonPlotter.this);
					switchFocusPolygon(polygon);
					polygons.add(polygon);
				}
				// 新增多边形顶点
				LatLng latLng = PlotUtils.toLocation(map, ((int) downX), ((int) downY));
				IVertex vertex = plotterFactory.createVertex(IVertex.TYPE_NORMAL, focusedPolygon);
				vertex.setPosition(latLng);
				focusedPolygon.addNormalVertex(focusedPolygon.getVertices().size(), vertex);
			}
		}
	}

	private IPolygon findVertexAddablePolygon() {
		for (IPolygon p : polygons) {
			if (!p.isDestroyed() &&
					p.isEditable() &&
					p.isVisible() &&
					!p.isClosed() &&
					p.isTouchable() &&
					!p.getVertices().isEmpty()) {
				return p;
			}
		}
		return null;
	}

	private boolean interceptMapTouchEvent() {
		return mDownPolygon != null &&
				mDownTouchType != TouchType.NONE &&
				mDownTouchType != TouchType.POLYGON;
	}
}
