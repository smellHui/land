package com.hawk.map.polygon.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.util.ObjectsCompat;
import android.util.Log;
import android.view.MotionEvent;

import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.hawk.map.R;
import com.hawk.map.core.IMapFunctions;
import com.hawk.map.polygon.PlotUtils;
import com.hawk.map.polygon.Status;
import com.hawk.map.polygon.core.IHandle;
import com.hawk.map.polygon.core.IPolygon;
import com.hawk.map.polygon.core.IVertex;
import com.hawk.map.polygon.vo.HandleStyle;
import com.hawk.map.utils.SimpleGestureDetector;
import com.hawk.map.utils.SimpleGestureListener;

import java.util.ArrayList;

/**
 * Created on 2018/11/20 13:07
 *
 * @author WingHawk
 */
public class Handle implements IHandle {
	private static final int HANDLE_DRAW_ORDER = 200000;
	private int status = Status.DEFAULT | Status.HANDLE_AUTO_ROTATE | Status.DIRTY;
	private int rotation;
	private int mBgRes;
	private int mSize;
	private HandleStyle style;
	private IPolygon parent;
	private Marker mMarker;
	private LatLng position;
	private Context mContext;
	private IMapFunctions mMapFunctions;
	private BitmapDescriptor mIcon;
	private Matrix mMatrix = new Matrix();
	private SimpleGestureDetector mGestureDetector = new SimpleGestureDetector(new HandleGestureListener());
	private PolygonPlotter mGrandpa;
	private static final String TAG = Handle.class.getSimpleName();

	@Override
	public void setStyle(HandleStyle style) {
		if (!ObjectsCompat.equals(this.style, style)) {
			this.style = style;
			invalidate();
		}
	}

	@Override
	public HandleStyle getStyle() {
		return style == null ? parent.getDefaultHandleStyle() : style;
	}

	@Override
	public void setRotation(int rotation) {
		if (this.rotation != rotation) {
			this.rotation = rotation;
			invalidate();
		}
	}

	@Override
	public int getRotation() {
		return rotation;
	}

	@Override
	public boolean contains(float x, float y) {
		if (mMarker == null || !isVisible()) {
			return false;
		}
		Point hp = mMapFunctions.getProjection().toScreenLocation(mMarker.getPosition());
		mMatrix.reset();
		mMatrix.setRotate(rotation, hp.x, hp.y);
		float[] p = {x, y};
		mMatrix.mapPoints(p);
		x = Math.round(p[0] - hp.x + mIcon.getWidth() / 2);
		y = Math.round(p[1] - hp.y + mIcon.getHeight());
		Bitmap bitmap = mIcon.getBitmap();
		if (x >= 0 &&
				x < bitmap.getWidth() &&
				y >= 0 &&
				y < bitmap.getHeight()) {
			int pixel = bitmap.getPixel(((int) x), ((int) y));
			int alpha = Color.alpha(pixel);
			return alpha > 250;
		}
		return false;
	}

	@Override
	public void setPosition(LatLng position) {
		if (!ObjectsCompat.equals(this.position, position)) {
			this.position = position;
			invalidate();
		}
	}

	@Override
	public LatLng getPosition() {
		return position;
	}

	@Override
	public void setVisible(boolean visible) {
		if (isVisible() != visible) {
			if (visible) {
				status |= Status.HANDLE_VISIBLE;
			} else {
				status &= ~Status.HANDLE_VISIBLE;
			}
			invalidate();
		}
	}

	@Override
	public boolean isVisible() {
		return (status & Status.HANDLE_VISIBLE) != 0 && parent.isHandleVisible();
	}

	@Override
	public void setParent(@NonNull IPolygon parent) {
		this.parent = parent;
		mGrandpa = parent.getParent();
		mMapFunctions = parent.getMapFunctions();
		mContext = mGrandpa.getContext();
		mGestureDetector.setTouchSlop(mGrandpa.getTouchSlop());
	}

	@NonNull
	@Override
	public IPolygon getParent() {
		return parent;
	}

	@Override
	public void invalidate() {
		if (isDestroyed()) {
			Log.e(TAG, "请勿操作已销毁的把手！");
			return;
		}
		if (isDirty()) {
			return;
		}
		setDirty(true);
		parent.invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mGestureDetector.onTouchEvent(event);
				break;
			default:
				if (!isVisible()) {
					return false;
				}
				mGestureDetector.onTouchEvent(event);
				break;
		}
		return true;
	}

	@Override
	public void indicate(IVertex vertex) {
		setVisible(true);
		setPosition(vertex.getPosition());
	}

	@Override
	public boolean isDirty() {
		return (status & Status.DIRTY) != 0;
	}

	@Override
	public void setDirty(boolean dirty) {
		if (dirty) {
			status |= Status.DIRTY;
		} else {
			status &= ~Status.DIRTY;
		}
	}

	@Override
	public int getStatus() {
		return status;
	}

	@NonNull
	@Override
	public Bitmap createBackground() {
		if (isBackgroundDirty()) {
			int status = getStatus();
			HandleStyle style = getStyle();
			mBgRes = style.getBackground(status, R.drawable.handle);
			mSize = style.getSize(status, PlotUtils.dp2px(mContext, 52));
			Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), mBgRes);
			bitmap = PlotUtils.scale(bitmap, mSize, mSize);
			mIcon = BitmapDescriptorFactory.fromBitmap(bitmap);
			return bitmap;
		}
		return mIcon.getBitmap();
	}

	@Override
	public boolean isBackgroundDirty() {
		if (mIcon == null) {
			return true;
		}
		int status = getStatus();
		int bgRes = getStyle().getBackground(status, R.drawable.handle);
		int size = getStyle().getSize(status, PlotUtils.dp2px(mContext, 52));
		return bgRes != mBgRes || size != mSize;
	}

	@Override
	public int getDrawOrder() {
		return HANDLE_DRAW_ORDER;
	}

	@Override
	public void setDrawOrder(int drawOrder) {
		// ignore
	}

	@Override
	public void offset(int dx, int dy) {
		Point point = getPoint();
		point.offset(dx, dy);
		LatLng latLng = mMapFunctions.getProjection().fromScreenLocation(point);
		setPosition(latLng);
	}

	@Override
	public void setAutoRotate(boolean autoRotate) {
		if (isAutoRotate() != autoRotate) {
			if (autoRotate) {
				status |= Status.HANDLE_AUTO_ROTATE;
			} else {
				status &= ~Status.HANDLE_AUTO_ROTATE;
			}
			invalidate();
		}
	}

	@Override
	public boolean isAutoRotate() {
		return (status & Status.HANDLE_AUTO_ROTATE) != 0;
	}

	@Override
	public void draw() {
		createBackground();
		if (isAutoRotate()) {
			rotation = (int) computeRotateAngel();
		}
		LatLng position = getPosition();
		int drawOrder = getDrawOrder();
		boolean visible = isVisible();
		if (mMarker == null) {
			MarkerOptions options = new MarkerOptions()
					.position(position)
					.zIndex(drawOrder)
					.rotateAngle(rotation)
					.visible(visible)
					.icon(mIcon);
			mMarker = mMapFunctions.addMarker(options);
		} else {
			mMarker.setPosition(position);
			mMarker.setZIndex(drawOrder);
			mMarker.setRotateAngle(rotation);
			mMarker.setVisible(visible);
			mMarker.setIcon(mIcon);
		}
		setDirty(false);
	}

	private Point getPoint() {
		return mMapFunctions.getProjection().toScreenLocation(getPosition());
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

	private class HandleGestureListener extends SimpleGestureListener {
		private float touchX;
		private float touchY;

		@Override
		public void onDown(MotionEvent event) {
			super.onDown(event);
			touchX = event.getX();
			touchY = event.getY();
		}

		@Override
		public void onMove(MotionEvent event) {
			float moveX = event.getX();
			float moveY = event.getY();
			int dx = (int) (moveX - touchX + 0.5);
			int dy = (int) (moveY - touchY + 0.5);
			int touchSlop = mGrandpa.getTouchSlop();
			if (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop) {
				offset(dx, dy);
				touchX = moveX;
				touchY = moveY;
			}
		}

		@Override
		public void onUp(MotionEvent event) {
			super.onUp(event);
			setAutoRotate(true);
		}
	}

	private float computeRotateAngel() {
		ArrayList<Point> points = new ArrayList<>();
		for (IVertex vertex : parent.getVertices()) {
			points.add(vertex.getPoint());
		}
		return PlotUtils.computeHandleAngel(mContext, points, getPoint(), mMatrix);
	}
}
