package com.hawk.map.polygon.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ObjectsCompat;
import android.support.v4.util.Pair;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;

import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.hawk.map.R;
import com.hawk.map.core.IMapFunctions;
import com.hawk.map.polygon.DirtyFlags;
import com.hawk.map.polygon.PlotUtils;
import com.hawk.map.polygon.Status;
import com.hawk.map.polygon.TouchType;
import com.hawk.map.polygon.callback.IPolygonHook;
import com.hawk.map.polygon.callback.OnPlotListener;
import com.hawk.map.polygon.core.IHandle;
import com.hawk.map.polygon.core.IPolygon;
import com.hawk.map.polygon.core.IVertex;
import com.hawk.map.polygon.vo.VertexStyle;
import com.hawk.map.utils.SimpleGestureDetector;
import com.hawk.map.utils.SimpleGestureListener;

import java.util.List;

/**
 * 多边形上的顶点，也可以是中间点。
 * Created on 2018/11/20 10:28
 *
 * @author WingHawk
 */
public class Vertex implements IVertex {
	private static final int DEFAULT_DRAW_ORDER = 20000;
	private static final int MIDDLE_DRAW_ORDER = 0;
	private static final int FOCUSED_DRAW_ORDER = 50000;

	private SparseArray<Object> mTags = new SparseArray<>();
	private IPolygon parent;
	private LatLng position;
	private VertexStyle style;
	private Context mContext;
	private int drawOrder;
	private int type;
	private int status = Status.DEFAULT |
			Status.VERTEX_VISIBLE |
			Status.VERTEX_TOUCHABLE |
			Status.DIRTY;
	private Marker mMarker;
	private IMapFunctions mMapFunctions;
	private BitmapDescriptor mIcon;
	private int mBgRes;
	private int mSize;
	private SimpleGestureDetector mGestureDetector = new SimpleGestureDetector(new VertexGestureListener());
	private PolygonPlotter mGrandpa;
	private static final String TAG = Vertex.class.getSimpleName();
	private TouchType mTouchType;

	Vertex() {
		PolygonPlotter.VERTEX_COUNTER++;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public void setParent(@NonNull IPolygon parent) {
		this.parent = parent;
		mMapFunctions = parent.getMapFunctions();
		mGrandpa = parent.getParent();
		mContext = mGrandpa.getContext();
		mGestureDetector.setTouchSlop(mGrandpa.getTouchSlop());
	}

	@NonNull
	@Override
	public IPolygon getParent() {
		return parent;
	}

	@NonNull
	@Override
	public LatLng getPosition() {
		return position;
	}

	@NonNull
	@Override
	public Point getPoint() {
		return mMapFunctions.getProjection().toScreenLocation(getPosition());
	}

	@Override
	public void setPosition(@NonNull LatLng position) {
		if (!ObjectsCompat.equals(this.position, position)) {
			this.position = position;
			getParent().setDirty(DirtyFlags.POSITION);
			invalidate();
		}
	}

	@Override
	public void setVisible(boolean visible) {
		if (isVisible() != visible) {
			if (visible) {
				status |= Status.VERTEX_VISIBLE;
			} else {
				status &= ~Status.VERTEX_VISIBLE;
			}
			invalidate();
		}
	}

	@Override
	public boolean isVisible() {
		return (status & Status.VERTEX_VISIBLE) > 0 && parent.isEditable() && parent.isVisible();
	}

	@Override
	public void setFocused(boolean focused) {
		if (isFocused() != focused) {
			if (focused) {
				PolygonPlotter.VERTEX_COUNTER++;
				status |= Status.VERTEX_FOCUSED;
			} else {
				status &= ~Status.VERTEX_FOCUSED;
			}
			OnPlotListener onPlotListener = mGrandpa.getOnPlotListener();
			if (onPlotListener != null) {
				onPlotListener.onFocusChanged(this);
			}
			invalidate();
		}
	}

	private void resetDrawOrder() {
		int parentOrder = parent.getDrawOrder();
		if (isFocused()) {
			setDrawOrder(parentOrder + FOCUSED_DRAW_ORDER);
		} else if (getType() == IVertex.TYPE_MIDDLE) {
			setDrawOrder(parentOrder + MIDDLE_DRAW_ORDER);
		} else {
			setDrawOrder(parentOrder + DEFAULT_DRAW_ORDER);
		}
	}

	@Override
	public boolean isFocused() {
		return (status & Status.VERTEX_FOCUSED) > 0;
	}

	@Override
	public Pair<IVertex, IVertex> getSiblings() {
		List<IVertex> vertices = parent.getVertices();
		int index = vertices.indexOf(this);
		if (index < 0) {
			return new Pair<>(null, null);
		}
		IVertex preVtx = null;
		IVertex nextVtx = null;
		if (vertices.size() > 1) {
			if (index == 0) {
				if (parent.isClosed()) {
					preVtx = vertices.get(vertices.size() - 1);
				}
				nextVtx = vertices.get(index + 1);
			} else if (index == vertices.size() - 1) {
				if (parent.isClosed()) {
					nextVtx = vertices.get(0);
				}
				preVtx = vertices.get(index - 1);
			} else {
				preVtx = vertices.get(index - 1);
				nextVtx = vertices.get(index + 1);
			}
		}
		return new Pair<>(preVtx, nextVtx);
	}

	@Override
	public void setSelected(boolean selected) {
		if (isSelected() != selected) {
			if (selected) {
				status |= Status.VERTEX_SELECTED;
			} else {
				status &= ~Status.VERTEX_SELECTED;
			}
			invalidate();
		}
	}

	@Override
	public boolean isSelected() {
		return (status & Status.VERTEX_SELECTED) > 0;
	}

	@Override
	public void destroy() {
		if (mMarker != null) {
			mMarker.remove();
			mMarker = null;
		}
		status = Status.DESTROYED;
	}

	@Override
	public boolean isDestroyed() {
		return (status & Status.DESTROYED) != 0;
	}

	@Override
	public void setType(int type) {
		if (this.type != type) {
			this.type = type;
			OnPlotListener onPlotListener = mGrandpa.getOnPlotListener();
			if (onPlotListener != null) {
				onPlotListener.onChanged(this);
			}
			invalidate();
		}
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public void offset(int dx, int dy) {
		Point point = getPoint();
		point.offset(dx, dy);
		LatLng latLng = mMapFunctions.getProjection().fromScreenLocation(point);
		setPosition(latLng);
		invalidate();
	}

	@Override
	public boolean isBackgroundDirty() {
		if (mIcon == null) {
			return true;
		}
		int type = getType();
		int status = getStatus();
		VertexStyle style = getStyle();
		int bgRes = style.getBackground(type, status, R.drawable.white);
		int size = style.getSize(type, status, PlotUtils.dp2px(mContext, 30));
		return bgRes != mBgRes || size != mSize;
	}

	/**
	 * 绘制顶点。调用该方法会立即重绘该顶点。
	 * 如非必要，请调用 {@link #invalidate()} 方法。
	 */
	@Override
	public void draw() {
		createBackground();
		resetDrawOrder();
		LatLng position = getPosition();
		int drawOrder = getDrawOrder();
		boolean visible = isVisible();
		if (mMarker == null) {
			MarkerOptions options = new MarkerOptions()
					.anchor(0.5f, 0.5f)
					.position(position)
					.zIndex(drawOrder)
					.visible(visible)
					.icon(mIcon);
			mMarker = mMapFunctions.addMarker(options);
		} else {
			mMarker.setPosition(position);
			mMarker.setZIndex(drawOrder);
			mMarker.setVisible(visible);
			mMarker.setIcon(mIcon);
		}
		setDirty(false);
	}

	@Override
	public TouchType getTouchType(float x, float y) {
		if (mMarker == null || !isVisible() || !isTouchable()) {
			return TouchType.NONE;
		}
		// 计算 x,y 是否在设定的可触摸范围内
		Point o = getPoint();
		double ox = o.x;
		double oy = o.y;
		double dx = ox - x;
		double dy = oy - y;
		int r = getStyle().getTouchSize(getType(), getStatus(), PlotUtils.dp2px(mContext,30)) / 2;
		boolean touchIn = dx * dx + dy * dy <= r * r;
		if (touchIn) {
			// 计算 x,y 是否真正在顶点的背景图中
			Bitmap bitmap = mIcon.getBitmap();
			dx += bitmap.getWidth() / 2;
			dy += bitmap.getHeight() / 2;
			if (dx < 0 || dx >= bitmap.getWidth() || dy < 0 || dy >= bitmap.getHeight()) {
				return TouchType.VERTEX;
			}
			int pixel = bitmap.getPixel(((int) Math.abs(Math.round(dx))), ((int) Math.round(Math.abs(dy))));
			// 完全不透明的值为 255
			if (Color.alpha(pixel) > 250) {
				return TouchType.INSIGHT_VERTEX;
			}
			return TouchType.VERTEX;
		}
		return TouchType.NONE;
	}

	@Override
	public void setDrawOrder(int order) {
		this.drawOrder = order;
		invalidate();
	}

	@Override
	public int getDrawOrder() {
		return drawOrder + PolygonPlotter.VERTEX_COUNTER;
	}

	@Override
	public void setTouchable(boolean touchable) {
		if (isTouchable() != touchable) {
			if (touchable) {
				status |= Status.VERTEX_TOUCHABLE;
			} else {
				status &= ~Status.VERTEX_TOUCHABLE;
			}
			invalidate();
		}
	}

	@Override
	public boolean isTouchable() {
		return (status & Status.VERTEX_TOUCHABLE) > 0;
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
	public void setStyle(VertexStyle style) {
		if (!ObjectsCompat.equals(this.style, style)) {
			this.style = style;
			invalidate();
		}
	}

	@NonNull
	@Override
	public VertexStyle getStyle() {
		return style == null ? parent.getDefaultVertexStyle() : style;
	}

	@NonNull
	@Override
	public Bitmap createBackground() {
		if (isBackgroundDirty()) {
			int type = getType();
			int status = getStatus();
			VertexStyle style = getStyle();
			mBgRes = style.getBackground(type, status, R.drawable.white);
			mSize = style.getSize(type, status, PlotUtils.dp2px(mContext,30));
			Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), mBgRes);
			bitmap = PlotUtils.scale(bitmap, mSize, mSize);
			mIcon = BitmapDescriptorFactory.fromBitmap(bitmap);
			return bitmap;
		}
		return mIcon.getBitmap();
	}

	@Override
	public void invalidate() {
		if (isDestroyed()) {
			if (isDestroyed()) {
				Log.e(TAG, "请勿操作已销毁的顶点！");
				return;
			}
			return;
		}
		setDirty(true);
		parent.invalidate();
	}

	@Override
	public boolean isDirty() {
		return (status & Status.DIRTY) != 0;
	}

	@Override
	public void setDirty(boolean dirty) {
		if (isDirty() != dirty) {
			if (dirty) {
				status |= Status.DIRTY;
			} else {
				status &= ~Status.DIRTY;
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, TouchType touchType) {
		if (!isTouchable()) {
			return false;
		}
		mTouchType = touchType;
		mGestureDetector.onTouchEvent(event);
		return true;
	}

	@Override
	public boolean requestShowHandle() {
		IHandle handle = parent.getHandle();
		handle.indicate(this);
		OnPlotListener onPlotListener = mGrandpa.getOnPlotListener();
		if (onPlotListener != null) {
			onPlotListener.onFocusChanged(this);
		}
		return handle.isVisible();
	}

	private class VertexGestureListener extends SimpleGestureListener {
		private boolean isMoving;
		private float touchX;
		private float touchY;

		@Override
		public void onDown(MotionEvent event) {
			touchX = event.getX();
			touchY = event.getY();
		}

		@Override
		public void onMove(MotionEvent event) {
			if (!isMoving) {
				for (IPolygonHook hook : parent.getPolygonHooks()) {
					hook.beforeVertexMove(Vertex.this);
				}
				if (!parent.getHandle().isVisible()) {
					requestShowHandle();
				}
				isMoving = true;
			}
			if (getType() == IVertex.TYPE_MIDDLE) {
				parent.switchVertexType(Vertex.this, IVertex.TYPE_NORMAL);
			}
			float moveX = event.getX();
			float moveY = event.getY();
			int dx = (int) (moveX - touchX + 0.5);
			int dy = (int) (moveY - touchY + 0.5);
			int touchSlop = mGrandpa.getTouchSlop();
			if (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop) {
				touchX = moveX;
				touchY = moveY;
				Point point = getPoint();
				point.offset(dx, dy);
				LatLng position = mMapFunctions.getProjection().fromScreenLocation(point);
				parent.moveNormalVertex(Vertex.this, position);
			}
		}

		@Override
		public void onUp(MotionEvent event) {
			if (isMoving) {
				isMoving = false;
				for (IPolygonHook hook : parent.getPolygonHooks()) {
					hook.afterVertexMove(Vertex.this);
				}
			}
		}

		@Override
		public void onClick(MotionEvent event) {
			if (mTouchType == TouchType.VERTEX || mTouchType == TouchType.INSIGHT_VERTEX) {
				List<IVertex> vertices = parent.getVertices();
				int index = vertices.indexOf(Vertex.this);
				if (index == 0 && vertices.size() >= 5 && !parent.isClosed()) {
					// 点击第一个点，闭合
					parent.switchClose(true);
				} else {
					if (!parent.getHandle().isVisible()) {
						requestShowHandle();
					}
				}
			}
		}
	}
}
