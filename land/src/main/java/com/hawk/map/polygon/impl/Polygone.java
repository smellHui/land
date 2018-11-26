package com.hawk.map.polygon.impl;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ObjectsCompat;
import android.support.v4.util.Pair;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.hawk.map.core.IMapFunctions;
import com.hawk.map.polygon.DirtyFlags;
import com.hawk.map.polygon.PlotUtils;
import com.hawk.map.polygon.Status;
import com.hawk.map.polygon.TouchType;
import com.hawk.map.polygon.callback.IPolygonHook;
import com.hawk.map.polygon.callback.OnPlotListener;
import com.hawk.map.polygon.core.IHandle;
import com.hawk.map.polygon.core.IHistoryManager;
import com.hawk.map.polygon.core.IPlotterFactory;
import com.hawk.map.polygon.core.IPolygon;
import com.hawk.map.polygon.core.IVertex;
import com.hawk.map.polygon.vo.HandleStyle;
import com.hawk.map.polygon.vo.PolygonStyle;
import com.hawk.map.polygon.vo.VertexStyle;
import com.hawk.map.utils.JtsHelper;
import com.hawk.map.utils.SimpleGestureDetector;
import com.hawk.map.utils.SimpleGestureListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 多边形
 * <br/>
 * Created on 2018/11/20 9:42
 *
 * @author WingHawk
 */
public class Polygone implements IPolygon {
	private static final String TAG = Polygone.class.getSimpleName();
	private static final int DEFAULT_DRAW_ORDER = 20000;
	private static final int FOCUSED_DRAW_ORDER = 50000;
	private static final int INVALIDATE_INTERVAL = 12;
	private LinkedList<IVertex> vertices = new LinkedList<>();
	private SparseArray<Object> mTags = new SparseArray<>();
	private GestureDetector mGestureDetector = new GestureDetector(new PolygonGestureListener());
	private SimpleGestureDetector mClickGestureDetector = new SimpleGestureDetector(new ClickGestureListener());
	private TouchType mTouchType = TouchType.NONE;
	private Handler mHandler = new Handler();
	private IHistoryManager historyManager;
	private IPlotterFactory plotterFactory;
	private IVertex focusedVertex;
	private PolygonPlotter parent;
	private PolygonStyle style;
	private Polyline mPolyline;
	private Polygon mPolygon;
	private IHandle handle;
	private VertexStyle vertexStyle;
	private HandleStyle handleStyle;
	private List<IPolygonHook> polygonHooks = new ArrayList<>();
	private int drawOrder;
	private int mDirtyFlag;
	private boolean handleVisible = true;
	private Projection mProjection;
	private Context mContext;

	Polygone() {
		PolygonPlotter.POLYGON_COUNTER++;
	}

	private int status = Status.DEFAULT |
			Status.POLYGON_VISIBLE |
			Status.POLYGON_TOUCHABLE |
			Status.POLYGON_EDITABLE;

	@Override
	public void setParent(PolygonPlotter parent) {
		this.parent = parent;
		plotterFactory = parent.getPlotterFactory();
		handle = plotterFactory.createHandle(this);
		mClickGestureDetector.setTouchSlop(parent.getTouchSlop());
		mProjection = getMapFunctions().getProjection();
		mContext = parent.getContext();
		setHistoryManager(plotterFactory.createHistoryManager(this));
	}

	@Override
	public void setTag(int key, @NonNull Object tag) {
		mTags.put(key, tag);
	}

	@Nullable
	@Override
	public Object getTag(int key) {
		return mTags.get(key);
	}

	@Override
	@NonNull
	public IMapFunctions getMapFunctions() {
		return parent.getMapFunctions();
	}

	@Override
	public void setPositions(List<LatLng> points, boolean isClosed) {
		vertices.clear();
		focusedVertex = null;
		for (LatLng point : points) {
			IVertex nextNormalVtx = plotterFactory.createVertex(IVertex.TYPE_NORMAL, this);
			nextNormalVtx.setPosition(point);
			if (vertices.size() > 0) {
				IVertex preNormalVtx = vertices.getLast();
				addMiddleVertex(preNormalVtx, nextNormalVtx);
			}
			addVertex(vertices.size(), nextNormalVtx);
		}
		isClosed = isClosed && points.size() > 3;
		if (isClosed) {
			addMiddleVertex(vertices.getLast(), vertices.getFirst());
		}
		setClosed(isClosed);
	}

	@Override
	public void addMiddleVertex(IVertex preVtx, IVertex nextVtx) {
		int preIndex = vertices.indexOf(preVtx);
		IVertex midVertex = plotterFactory.createVertex(IVertex.TYPE_MIDDLE, this);
		LatLng middlePosition = PlotUtils.getMiddlePosition(preVtx, nextVtx, mProjection);
		midVertex.setPosition(middlePosition);
		addVertex(preIndex + 1, midVertex);
	}

	@NonNull
	@Override
	public List<LatLng> getPositions() {
		ArrayList<LatLng> positions = new ArrayList<>();
		for (IVertex vertex : vertices) {
			if (vertex.getType() == IVertex.TYPE_NORMAL) {
				positions.add(vertex.getPosition());
			}
		}
		return positions;
	}

	@NonNull
	@Override
	public List<IVertex> getVertices() {
		return Collections.unmodifiableList(vertices);
	}

	@Override
	public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
		if (!isTouchable()) {
			return false;
		}
		handle.setAutoRotate(event.getAction() != MotionEvent.ACTION_MOVE || mTouchType != TouchType.HANDLE);
		// 如果触摸把手，让把手处理事件
		if (mTouchType == TouchType.HANDLE) {
			focusedVertex.onTouchEvent(event, mTouchType);
			handle.setPosition(focusedVertex.getPosition());
			// 如果触摸顶点，让顶点处理事件
		} else if (mTouchType == TouchType.VERTEX || mTouchType == TouchType.INSIGHT_VERTEX) {
			focusedVertex.onTouchEvent(event, mTouchType);
			handle.setPosition(focusedVertex.getPosition());
			// 触摸在多边形上
		} else if (mTouchType == TouchType.POLYGON) {
			return onTouchEvent(event);
		} else {
			mClickGestureDetector.onTouchEvent(event);
		}
		return true;
	}

	@Override
	public void registerHook(IPolygonHook hook) {
		if (!polygonHooks.contains(hook)) {
			polygonHooks.add(hook);
		}
	}

	@Override
	public void unregisterHook(IPolygonHook hook) {
		polygonHooks.remove(hook);
	}

	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	@Override
	public Pair<TouchType, IVertex> findTouchVertex(float x, float y) {
		IVertex preferVertex = null;
		TouchType preferType = TouchType.NONE;
		double touchDistance = Integer.MAX_VALUE;
		Point touchPoint = new Point(((int) x), ((int) y));
		// 取优先级最高的顶点，同样的 TouchType 下，距离触摸点最近的优先级最大
		// 触摸点在顶点可见范围的优先级大于其在顶点触摸范围的优先级，
		for (IVertex vertex : vertices) {
			TouchType touchType = vertex.getTouchType(x, y);
			if (touchType != TouchType.NONE) {
				double distance = PlotUtils.getDistance(vertex.getPoint(), touchPoint);
				if (preferType == touchType && distance < touchDistance) {
					preferVertex = vertex;
					preferType = touchType;
					touchDistance = distance;
				} else {
					int o1 = preferType.getPriority() + (preferVertex != null ? preferVertex.getDrawOrder() : 0);
					int o2 = touchType.getPriority() + vertex.getDrawOrder();
					// 加上顶点已有的 drawOrder，保证已获取焦点的顶点的优先级高于其它顶点
					if (o2 > o1) {
						preferVertex = vertex;
						preferType = touchType;
						touchDistance = distance;
					}
				}
			}
		}
		return new Pair<>(preferType, preferVertex);
	}

	@Override
	public void setStyle(@NonNull PolygonStyle style) {
		if (!ObjectsCompat.equals(this.style, style)) {
			this.style = style;
			setDirty(DirtyFlags.STYLE);
			invalidate();
		}
	}

	@Override
	public void setDirty(int dirtyFlag) {
		mDirtyFlag |= dirtyFlag;
	}

	private void checkCross() {
		boolean crossed = false;
		if (isClosed()) {
			List<Point> points = getPoints();
			// FIXME: 2018/11/24 换一种检测是否自相交的方式。
			crossed = JtsHelper.isConcavePolygon(points) && JtsHelper.getInstance().isSelfCross(points);
		}
		if (isCrossed() != crossed) {
			if (crossed) {
				status |= Status.POLYGON_CROSSED;
			} else {
				status &= ~Status.POLYGON_CROSSED;
			}
			setDirty(DirtyFlags.STYLE);
		}
	}

	@NonNull
	@Override
	public PolygonStyle getStyle() {
		return style == null ? parent.getDefaultPolygonStyle() : style;
	}

	@Override
	public void setEditable(boolean editable) {
		if (isEditable() != editable) {
			if (editable) {
				status |= Status.POLYGON_EDITABLE;
			} else {
				status &= ~Status.POLYGON_EDITABLE;
			}
			setDirty(DirtyFlags.STYLE);
			for (IVertex vertex : vertices) {
				vertex.invalidate();
			}
			handle.invalidate();
		}
	}

	@Override
	public double getArea() {
		return isClosed() ? AMapUtils.calculateArea(getPositions()) : 0;
	}

	@Override
	public double getPerimeter() {
		List<LatLng> positions = getPositions();
		double perimeter = 0;
		if (positions.size() > 1) {
			if (isClosed()) {
				positions.add(vertices.get(0).getPosition());
			}
			for (int i = 0; i < positions.size(); i += 2) {
				LatLng lineStart = positions.get(i);
				LatLng lineEnd = positions.get(i + 1);
				perimeter += AMapUtils.calculateLineDistance(lineStart, lineEnd);
			}
		}
		return perimeter;
	}

	@Override
	public boolean isEditable() {
		return (status & Status.POLYGON_EDITABLE) != 0;
	}

	@Override
	public void setTouchable(boolean touchable) {
		if (isTouchable() != touchable) {
			if (touchable) {
				status |= Status.POLYGON_TOUCHABLE;
			} else {
				status &= ~Status.POLYGON_TOUCHABLE;
			}
			setDirty(DirtyFlags.STYLE);
			invalidate();
		}
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public void invalidate() {
		if (isDestroyed()) {
			Log.e(TAG, "请勿操作已销毁的多边形！");
			return;
		}
		// 不可见并且不是可见性改变，则不重绘
		if ((mDirtyFlag & DirtyFlags.VISIBLE) == 0 && !isVisible()) {
			return;
		}
		mHandler.removeCallbacks(mInvalidateTask);
		mHandler.postDelayed(mInvalidateTask, INVALIDATE_INTERVAL);
	}

	@Override
	public IHandle getHandle() {
		return handle;
	}

	@Override
	public TouchType getTouchType(MotionEvent event) {
		if (!isTouchable() || !isVisible()) {
			return TouchType.NONE;
		}
		float x = event.getX();
		float y = event.getY();
		TouchType touchType = TouchType.NONE;
		Point point = new Point(((int) x), ((int) y));
		// 判断是否触摸把手
		if (handle.contains(x, y)) {
			touchType = TouchType.HANDLE;
		} else {
			// 判断是否触摸顶点
			Pair<TouchType, IVertex> pair = findTouchVertex(x, y);
			IVertex touchVertex = pair.second;
			if (touchVertex != null) {
				touchType = pair.first;
				switchFocusVertex(touchVertex);
				// 判断是否触摸多边形
			} else if (isClosed() && isVisible() && PlotUtils.isPointInPolygon(point, getPoints())) {
				touchType = TouchType.POLYGON;
			}
		}
		mTouchType = touchType;
		return touchType;
	}

	@Override
	public void setClosed(boolean closed) {
		if (isClosed() != closed) {
			if (vertices.size() < 5) {
				throw new RuntimeException("多边形无法闭合：顶点数不可小于三！");
			}
			if (closed) {
				status |= Status.POLYGON_CLOSED;
			} else {
				status &= ~Status.POLYGON_CLOSED;
			}
			setDirty(DirtyFlags.VISIBLE | DirtyFlags.STYLE);
			invalidate();
		}
	}

	@Override
	public boolean isClosed() {
		return (status & Status.POLYGON_CLOSED) != 0;
	}

	@Override
	public boolean isValid() {
		return isClosed() && !isCrossed();
	}

	@Override
	public boolean isCrossed() {
		return (status & Status.POLYGON_CROSSED) != 0;
	}

	private List<Point> getPoints() {
		ArrayList<Point> points = new ArrayList<>();
		for (IVertex vertex : vertices) {
			if (vertex.getType() == IVertex.TYPE_NORMAL) {
				points.add(vertex.getPoint());
			}
		}
		return points;
	}

	@Override
	public boolean isTouchable() {
		return (status & Status.POLYGON_TOUCHABLE) != 0;
	}

	@Override
	public void setSelected(boolean selected) {
		if (isSelected() != selected) {
			if (selected) {
				status |= Status.POLYGON_SELECTED;
			} else {
				status &= ~Status.POLYGON_SELECTED;
			}
			setDirty(DirtyFlags.STYLE);
			invalidate();
		}
	}

	@Override
	public boolean isSelected() {
		return (status & Status.POLYGON_SELECTED) != 0;
	}

	@Override
	public void addVertex(int index, @NonNull IVertex vertex) {
		vertices.add(index, vertex);
		setDirty(DirtyFlags.POSITION);
		invalidate();
	}

	@Override
	public void setVertex(int index, @NonNull IVertex vertex) {
		IVertex exists = vertices.get(index);
		if (!ObjectsCompat.equals(exists, vertex)) {
			vertices.set(index, vertex);
			setDirty(DirtyFlags.POSITION);
			invalidate();
		}
	}

	@Override
	public void removeVertex(IVertex vertex) {
		if (vertex != null) {
			vertices.remove(vertex);
			vertex.destroy();
			if (focusedVertex == vertex) {
				focusedVertex = null;
				vertex.setFocused(false);
				handle.invalidate();
			}
			setDirty(DirtyFlags.POSITION);
		}
		invalidate();
	}

	@Override
	public void removeNormalVertex(IVertex vertex) {
		if (vertex.getType() != IVertex.TYPE_NORMAL) {
			throw new RuntimeException("参数只能是普通顶点");
		}
		for (IPolygonHook hook : polygonHooks) {
			hook.beforeNormalVertexRemove(vertex);
		}
		if (vertices.size() == 6 && isClosed()) {
			switchClose(false);
		}
		Pair<IVertex, IVertex> siblings = vertex.getSiblings();
		if (siblings.first != null && siblings.second != null) {
			switchVertexType(vertex, IVertex.TYPE_MIDDLE);
		} else {
			removeVertex(vertex);
			removeVertex(siblings.first);
			removeVertex(siblings.second);
		}

		for (IPolygonHook hook : polygonHooks) {
			hook.afterNormalVertexRemove(vertex);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		if (isVisible() != visible) {
			if (visible) {
				status |= Status.POLYGON_VISIBLE;
			} else {
				status &= ~Status.POLYGON_VISIBLE;
			}
			setDirty(DirtyFlags.VISIBLE);
			for (IVertex vertex : vertices) {
				vertex.invalidate();
			}
			handle.invalidate();
		}
	}

	@Override
	public boolean isVisible() {
		return (status & Status.POLYGON_VISIBLE) != 0;
	}

	@Override
	public void setFocused(boolean focused) {
		if (isFocused() != focused) {
			if (focused) {
				PolygonPlotter.POLYGON_COUNTER++;
				status |= Status.POLYGON_FOCUSED;
				setDrawOrder(FOCUSED_DRAW_ORDER);
			} else {
				status &= ~Status.POLYGON_FOCUSED;
				setDrawOrder(DEFAULT_DRAW_ORDER);
			}
			OnPlotListener onPlotListener = parent.getOnPlotListener();
			if (onPlotListener != null) {
				onPlotListener.onFocusChanged(this);
			}
			setDirty(DirtyFlags.STYLE);
			for (IVertex vertex : vertices) {
				vertex.invalidate();
			}
			handle.invalidate();
		}
	}

	@Override
	public boolean isFocused() {
		return (status & Status.POLYGON_FOCUSED) != 0;
	}

	@Override
	public IVertex getFocusedVertex() {
		if (focusedVertex != null && isFocused()) {
			return focusedVertex;
		}
		return null;
	}

	@Override
	public void setDrawOrder(int order) {
		if (this.drawOrder != order) {
			this.drawOrder = order;
			setDirty(DirtyFlags.DRAW_ORDER);
			invalidate();
		}
	}

	@Override
	public int getDrawOrder() {
		return drawOrder + PolygonPlotter.POLYGON_COUNTER;
	}

	@NonNull
	@Override
	public PolygonPlotter getParent() {
		return parent;
	}

	@NonNull
	@Override
	public IHistoryManager getHistoryManager() {
		return historyManager;
	}

	@Override
	public void setHistoryManager(@NonNull IHistoryManager historyManager) {
		this.historyManager = historyManager;
	}

	@Override
	public void destroy() {
		if (mPolygon != null) {
			mPolygon.remove();
		}
		if (mPolyline != null) {
			mPolyline.remove();
		}
		if (handle != null) {
			handle.destroy();
		}
		while (!vertices.isEmpty()) {
			IVertex vertex = vertices.poll();
			if (vertex != null) {
				vertex.destroy();
			}
		}
		vertices.clear();
		mHandler.removeCallbacks(mInvalidateTask);
		historyManager.destroy();
		status = Status.DESTROYED;
	}

	@Override
	public void addNormalVertex(int index, IVertex vertex) {
		if (vertex.getType() != IVertex.TYPE_NORMAL) {
			throw new RuntimeException("参数只能是普通顶点");
		}
		for (IPolygonHook hook : polygonHooks) {
			hook.beforeNormalVertexAdd(index, vertex);
		}
		addVertex(index, vertex);
		Pair<IVertex, IVertex> siblings = vertex.getSiblings();
		boolean setPreMid = true;
		boolean setNextMid = true;
		if (siblings.first != null && siblings.first.getType() == IVertex.TYPE_NORMAL) {
			addMiddleVertex(siblings.first, vertex);
			setPreMid = false;
		}
		if (siblings.second != null && siblings.second.getType() == IVertex.TYPE_NORMAL) {
			addMiddleVertex(vertex, siblings.second);
			setNextMid = false;
		}
		siblings = vertex.getSiblings();
		IVertex preMid = siblings.first;
		IVertex nextMid = siblings.second;
		if (setPreMid && preMid != null) {
			siblings = preMid.getSiblings();
			if (siblings.first != null && siblings.second != null) {
				LatLng midPos = PlotUtils.getMiddlePosition(siblings.first, siblings.second, getMapFunctions().getProjection());
				preMid.setPosition(midPos);
			}
		}
		if (setNextMid && nextMid != null) {
			siblings = nextMid.getSiblings();
			if (siblings.first != null && siblings.second != null) {
				LatLng midPos = PlotUtils.getMiddlePosition(siblings.first, siblings.second, getMapFunctions().getProjection());
				nextMid.setPosition(midPos);
			}
		}
		switchFocusVertex(vertex);
		for (IPolygonHook hook : polygonHooks) {
			hook.afterNormalVertexAdd(index, vertex);
		}
	}

	@Override
	public void switchFocusVertex(IVertex vertex) {
		if (vertex != focusedVertex) {
			if (focusedVertex != null) {
				focusedVertex.setFocused(false);
			}
			focusedVertex = vertex;
			if (focusedVertex != null) {
				focusedVertex.setFocused(true);
				handle.setPosition(focusedVertex.getPosition());
			} else {
				handle.invalidate();
			}
		} else if (vertex != null) {
			handle.setPosition(vertex.getPosition());
		}
	}

	@Override
	public void setHandleVisible(boolean visible) {
		if (handleVisible != visible) {
			handleVisible = visible;
			handle.invalidate();
		}
	}

	@Override
	public boolean isHandleVisible() {
		return handleVisible &&
				isTouchable() &&
				isFocused() &&
				isEditable() &&
				isVisible() &&
				focusedVertex != null &&
				parent.isHandleVisible();
	}

	@Override
	public void setDefaultVertexStyle(VertexStyle style) {
		vertexStyle = style;
		for (IVertex vertex : vertices) {
			vertex.invalidate();
		}
	}

	@Override
	public void setDefaultHandleStyle(HandleStyle style) {
		handleStyle = style;
		handle.invalidate();
	}

	@Override
	public HandleStyle getDefaultHandleStyle() {
		return handleStyle == null ? parent.getDefaultHandleStyle() : handleStyle;
	}

	@Override
	public VertexStyle getDefaultVertexStyle() {
		return vertexStyle == null ? parent.getDefaultVertexStyle() : vertexStyle;
	}

	@Override
	public void compress(double tolerance) {
		List<Integer> keepIndices = PlotUtils.compress(getPoints(), tolerance);
		// TODO: 2018/11/26
		setDirty(DirtyFlags.POSITION);
		invalidate();
	}

	@Override
	public void draw() {
		mHandler.removeCallbacks(mInvalidateTask);
		// 画顶点
		for (IVertex vertex : vertices) {
			if (vertex.isDirty()) {
				vertex.draw();
			}
		}
		// 画把手
		if (handle.isDirty()) {
			handle.draw();
		}
		// 画多边形或折线
		if (isDirty()) {
			// FIXME: 2018/11/26 在这里调用可能会有 bug
			if ((mDirtyFlag & DirtyFlags.POSITION) != 0) {
				OnPlotListener onPlotListener = parent.getOnPlotListener();
				if (onPlotListener != null) {
					onPlotListener.onChanged(this);
				}
			}
			if (isClosed()) {
				drawPolygon();
			} else {
				drawPolyline();
			}
		}
		mDirtyFlag = 0;
	}

	private boolean isDirty() {
		return mDirtyFlag != 0;
	}

	@Override
	public boolean isDestroyed() {
		return (status & Status.DESTROYED) != 0;
	}

	@Override
	public void switchClose(boolean close) {
		if (close) {
			close();
		} else {
			open();
		}
	}

	private void close() {
		if (vertices.size() < 5 || isClosed()) {
			return;
		}
		for (IPolygonHook hook : polygonHooks) {
			hook.beforePolygonClose(this);
		}
		setClosed(true);
		IVertex midVtx = plotterFactory.createVertex(IVertex.TYPE_MIDDLE, this);
		addVertex(vertices.size(), midVtx);
		Pair<IVertex, IVertex> siblings = midVtx.getSiblings();
		if (siblings.first != null && siblings.second != null) {
			LatLng midPos = PlotUtils.getMiddlePosition(siblings.first, siblings.second, mProjection);
			midVtx.setPosition(midPos);
		}
		for (IPolygonHook hook : polygonHooks) {
			hook.afterPolygonClose(this);
		}
	}

	private void open() {
		if (vertices.size() < 6) {
			setClosed(false);
			return;
		}
		if (isClosed()) {
			setClosed(false);
			// 移除额外的中间点
			IVertex midVtx = vertices.getLast();
			removeVertex(midVtx);
		}
	}

	private Runnable mInvalidateTask = new Runnable() {
		@Override
		public void run() {
			draw();
		}
	};

	private void drawPolygon() {
		if (mPolyline != null && mPolyline.isVisible()) {
			mPolyline.setVisible(false);
		}
		if ((mDirtyFlag & DirtyFlags.POSITION) != 0) {
			checkCross();
		}
		if (mPolygon == null) {
			List<LatLng> posList = getPositions();
			PolygonStyle style = getStyle();
			int status = getStatus();
			int fillColor = style.getFillColor(status, 0x66000000);
			int strokeColor = style.getStrokeColor(status, Color.WHITE);
			int strokeWidth = style.getStrokeWidth(status, PlotUtils.dp2px(mContext, 1));
			int drawOrder = getDrawOrder();
			boolean visible = isVisible();
			PolygonOptions options = new PolygonOptions()
					.addAll(posList)
					.fillColor(fillColor)
					.visible(visible)
					.strokeColor(strokeColor)
					.zIndex(drawOrder)
					.strokeWidth(strokeWidth);
			mPolygon = getMapFunctions().addPolygon(options);
		} else {
			if ((mDirtyFlag & DirtyFlags.POSITION) != 0) {
				List<LatLng> posList = getPositions();
				mPolygon.setPoints(posList);
			}
			if ((mDirtyFlag & DirtyFlags.STYLE) != 0) {
				PolygonStyle style = getStyle();
				int status = getStatus();
				int fillColor = style.getFillColor(status, 0x66000000);
				int strokeColor = style.getStrokeColor(status, Color.WHITE);
				int strokeWidth = style.getStrokeWidth(status, PlotUtils.dp2px(mContext, 1));
				mPolygon.setFillColor(fillColor);
				mPolygon.setStrokeColor(strokeColor);
				mPolygon.setStrokeWidth(strokeWidth);
			}
			if ((mDirtyFlag & DirtyFlags.VISIBLE) != 0) {
				boolean visible = isVisible();
				mPolygon.setVisible(visible);
			}
			if ((mDirtyFlag & DirtyFlags.DRAW_ORDER) != 0) {
				int drawOrder = getDrawOrder();
				mPolygon.setZIndex(drawOrder);
			}
		}
	}

	private void drawPolyline() {
		if (mPolygon != null && mPolygon.isVisible()) {
			mPolygon.setVisible(false);
		}
		if (mPolyline == null) {
			List<LatLng> posList = getPositions();
			PolygonStyle style = getStyle();
			int status = getStatus();
			int strokeColor = style.getStrokeColor(status, Color.WHITE);
			int strokeWidth = style.getStrokeWidth(status, PlotUtils.dp2px(mContext, 1));
			boolean dashLine = style.isDashLine(status, true);
			boolean visible = isVisible();
			int drawOrder = getDrawOrder();
			PolylineOptions polylineOptions = new PolylineOptions()
					.addAll(posList)
					.color(strokeColor)
					.setDottedLine(dashLine)
					.visible(visible)
					.zIndex(drawOrder)
					.width(strokeWidth);
			mPolyline = getMapFunctions().addPolyline(polylineOptions);
		} else {
			if ((mDirtyFlag & DirtyFlags.POSITION) != 0) {
				List<LatLng> posList = getPositions();
				mPolyline.setPoints(posList);
			}
			if ((mDirtyFlag & DirtyFlags.STYLE) != 0) {
				PolygonStyle style = getStyle();
				int status = getStatus();
				int strokeColor = style.getStrokeColor(status, Color.WHITE);
				int strokeWidth = style.getStrokeWidth(status, PlotUtils.dp2px(mContext, 1));
				boolean dashLine = style.isDashLine(status, true);
				mPolyline.setColor(strokeColor);
				mPolyline.setWidth(strokeWidth);
				mPolyline.setDottedLine(dashLine);
			}
			if ((mDirtyFlag & DirtyFlags.VISIBLE) != 0) {
				boolean visible = isVisible();
				mPolyline.setVisible(visible);
			}
			if ((mDirtyFlag & DirtyFlags.DRAW_ORDER) != 0) {
				int drawOrder = getDrawOrder();
				mPolyline.setZIndex(drawOrder);
			}
		}
	}

	@Override
	public void switchVertexType(IVertex vertex, int type) {
		if (vertex.getType() == type) {
			return;
		}
		for (IPolygonHook hook : polygonHooks) {
			hook.beforeVertexTypeSwitch(vertex);
		}
		Pair<IVertex, IVertex> siblings = vertex.getSiblings();
		int beforeType = vertex.getType();
		if (beforeType == IVertex.TYPE_NORMAL && type == IVertex.TYPE_MIDDLE) {
			// 把一个顶点变为中间点，需要将该点两边的中间点移除
			removeVertex(siblings.first);
			removeVertex(siblings.second);
			siblings = vertex.getSiblings();
			if (siblings.first != null && siblings.second != null) {
				LatLng midPos = PlotUtils.getMiddlePosition(siblings.first, siblings.second, mProjection);
				vertex.setPosition(midPos);
				if (focusedVertex == vertex && getHandle().isVisible()) {
					vertex.requestShowHandle();
				}
			}
			int index = vertices.indexOf(vertex);
			// 保证第一个点永远是普通顶点
			if (index == 0) {
				vertices.remove(0);
				vertices.add(vertex);
			}
		} else if (beforeType == IVertex.TYPE_MIDDLE && type == IVertex.TYPE_NORMAL) {
			// 把一个中间点变为顶点，需要在该点两边插入新的中间点
			addMiddleVertex(siblings.first, vertex);
			addMiddleVertex(vertex, siblings.second);
		}
		vertex.setType(type);
		for (IPolygonHook hook : polygonHooks) {
			hook.afterVertexTypeSwitch(vertex);
		}
	}

	@Override
	public List<IPolygonHook> getPolygonHooks() {
		return polygonHooks;
	}

	@Override
	public void moveNormalVertex(IVertex vertex, LatLng targetPosition) {
		if (vertex.getType() != IVertex.TYPE_NORMAL) {
			throw new RuntimeException("参数只能是普通顶点");
		}
		vertex.setPosition(targetPosition);
		Pair<IVertex, IVertex> siblings = vertex.getSiblings();
		IVertex preMidVtx = siblings.first;
		IVertex nextMidVtx = siblings.second;
		if (preMidVtx != null) {
			siblings = preMidVtx.getSiblings();
			if (siblings.first != null && siblings.second != null) {
				LatLng midPos = PlotUtils.getMiddlePosition(siblings.first, siblings.second, mProjection);
				preMidVtx.setPosition(midPos);
			}
		}
		if (nextMidVtx != null) {
			siblings = nextMidVtx.getSiblings();
			if (siblings.first != null && siblings.second != null) {
				LatLng midPos = PlotUtils.getMiddlePosition(siblings.first, siblings.second, mProjection);
				nextMidVtx.setPosition(midPos);
			}
		}
	}

	@Override
	public void performClick() {
		OnPlotListener onPlotListener = parent.getOnPlotListener();
		if (onPlotListener != null) {
			onPlotListener.onClick(Polygone.this);
		}
	}

	private class PolygonGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			performClick();
			return true;
		}
	}

	private class ClickGestureListener extends SimpleGestureListener {

		private float downX;
		private float downY;

		@Override
		public void onDown(MotionEvent event) {
			downX = event.getX();
			downY = event.getY();
		}

		@Override
		public void onClick(MotionEvent event) {
			if (!isClosed() && isEditable()) {
				LatLng latLng = PlotUtils.toLocation(getMapFunctions(), ((int) downX), ((int) downY));
				IVertex vertex = plotterFactory.createVertex(IVertex.TYPE_NORMAL, Polygone.this);
				vertex.setPosition(latLng);
				addNormalVertex(vertices.size(), vertex);
			}
		}
	}
}
