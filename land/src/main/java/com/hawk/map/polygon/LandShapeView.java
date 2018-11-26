package com.hawk.map.polygon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.v4.util.ObjectsCompat;
import android.util.AttributeSet;
import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LandShapeView extends View {

	private Projection mProjection;
	private Paint mPaint;

	private double mLandRight;
	private double mLandBottom;
	private double mLandLeft;
	private double mLandTop;

	private double mLandWidth;
	private double mLandHeight;
	private Map<Path, ShapeStyle> mPolygonsMap;
	private Map<List<LatLng>, ShapeStyle> mPosMap;
	private boolean mNeedLayoutLand;

	public LandShapeView(Context context) {
		super(context);
		init();
	}

	public LandShapeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LandShapeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		Context context = getContext();
		MapView mapView = new MapView(context);
		AMap map = mapView.getMap();
		LatLng defaultLatLng = new LatLng(0, 0);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, map.getMaxZoomLevel()));
		mProjection = map.getProjection();
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	}

	public void setPolygon(List<LatLng> posList, ShapeStyle style) {
		HashMap<List<LatLng>, ShapeStyle> map = new HashMap<>();
		map.put(posList, style);
		setPolygons(map);
	}

	public void setPolygons(Map<List<LatLng>, ShapeStyle> posMap) {
		if (!ObjectsCompat.equals(mPosMap, posMap)) {
			mPosMap = posMap;
			if (getWidth() == 0 || getHeight() == 0) {
				mNeedLayoutLand = true;
				invalidate();
				return;
			}
			mLandRight = Long.MIN_VALUE;
			mLandBottom = Long.MIN_VALUE;
			mLandLeft = Long.MAX_VALUE;
			mLandTop = Long.MAX_VALUE;
			Map<List<Point>, ShapeStyle> pointMap = new HashMap<>();
			for (Map.Entry<List<LatLng>, ShapeStyle> entry : posMap.entrySet()) {
				List<LatLng> posList = entry.getKey();
				ShapeStyle shapeStyle = entry.getValue();
				if (posList != null && !posList.isEmpty()) {
					List<Point> points = mapToPoints(posList);
					layoutAndMeasureLand(points);
					pointMap.put(points, shapeStyle);
				}
			}
			mPolygonsMap = new HashMap<>();
			for (Map.Entry<List<Point>, ShapeStyle> entry : pointMap.entrySet()) {
				List<Point> points = entry.getKey();
				ShapeStyle shapeStyle = entry.getValue();
				Path path = mapToPath(points);
				mPolygonsMap.put(path, shapeStyle);
			}
			invalidate();
		}
	}

	private Path mapToPath(List<Point> points) {
		float usableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
		float usableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
		double viewAspectRatio = usableHeight * 1.0 / usableWidth;
		double landAspectRatio = mLandHeight / mLandWidth;
		double sizeFactor;
		double offsetTop;
		double offsetLeft;

		if (viewAspectRatio > landAspectRatio) {
			// use view with as base size
			sizeFactor = usableWidth / mLandWidth;
			offsetLeft = 0;
			offsetTop = (usableHeight - mLandHeight * sizeFactor) / 2;
		} else {
			// use view height as base size
			sizeFactor = usableHeight / mLandHeight;
			offsetTop = 0;
			offsetLeft = (usableWidth - mLandWidth * sizeFactor) / 2;
		}
		Path path = new Path();
		boolean first = true;
		int paddingLeft = getPaddingLeft();
		int paddingTop = getPaddingTop();
		for (Point pt : points) {
			double dx = pt.x - mLandLeft;
			double dy = pt.y - mLandTop;
			float x = (float) (dx * sizeFactor + paddingLeft + offsetLeft);
			float y = (float) (dy * sizeFactor + paddingTop + offsetTop);
			if (first) {
				path.moveTo(x, y);
				first = false;
			} else {
				path.lineTo(x, y);
			}
		}
		path.close();
		return path;
	}

	private void layoutAndMeasureLand(List<Point> points) {
		for (Point pt : points) {
			mLandLeft = Math.min(mLandLeft, pt.x);
			mLandRight = Math.max(mLandRight, pt.x);
			mLandTop = Math.min(mLandTop, pt.y);
			mLandBottom = Math.max(mLandBottom, pt.y);
		}
		mLandWidth = mLandRight - mLandLeft;
		mLandHeight = mLandBottom - mLandTop;
	}

	private List<Point> mapToPoints(List<LatLng> posList) {
		ArrayList<Point> points = new ArrayList<>();
		for (LatLng latLng : posList) {
			Point point = mProjection.toScreenLocation(latLng);
			points.add(point);
		}
		return points;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mNeedLayoutLand) {
			setPolygons(mPosMap);
		} else if (mPolygonsMap != null) {
			for (Map.Entry<Path, ShapeStyle> entry : mPolygonsMap.entrySet()) {
				Path path = entry.getKey();
				ShapeStyle shapeStyle = entry.getValue();
				mPaint.setStyle(Paint.Style.FILL);
				mPaint.setColor(shapeStyle.fillColor);
				canvas.drawPath(path, mPaint);
				if (shapeStyle.strokeColor != 0 && shapeStyle.strokeWidth != 0) {
					mPaint.setStyle(Paint.Style.STROKE);
					mPaint.setColor(shapeStyle.strokeColor);
					canvas.drawPath(path, mPaint);
				}
			}
		}
	}

	public static class ShapeStyle {
		public int fillColor;
		public int strokeColor;
		public int strokeWidth;

		public ShapeStyle(int fillColor, int strokeColor, int strokeWidth) {
			this.fillColor = fillColor;
			this.strokeColor = strokeColor;
			this.strokeWidth = strokeWidth;
		}

		public ShapeStyle() {
		}
	}
}
