package com.hawk.map.polygon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.support.annotation.NonNull;

import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.hawk.map.core.IMapFunctions;
import com.hawk.map.polygon.core.IVertex;
import com.hawk.map.polygon.vo.StatusValue;

import java.util.List;

public class PlotUtils {

	private static final double MIN_LAT = -90;
	private static final double MAX_LAT = 90;
	private static final double MIN_LNG = -180;
	private static final double MAX_LNG = 180;

	public static boolean isPointInPolygon(Point point, List<Point> pts) {
		if (point == null) {
			return false;
		}
		if (pts == null || pts.size() < 3) {
			return false;
		}
		int n = pts.size();
		//cross mPoints count of x
		int intersectCount = 0;
		double precision = 2e-10;
		//neighbour bound vertices
		Point p1, p2;
		//left vertex
		p1 = pts.get(0);
		//check all rays
		for (int i = 1; i <= n; ++i) {
			if (point.equals(p1)) {
				//p is an vertex
				return true;
			}
			//right vertex
			p2 = pts.get(i % n);
			if (point.x < Math.min(p1.x, p2.x) || point.x > Math.max(p1.x, p2.x)) {
				//ray is outside of our interests
				p1 = p2;
				//next ray left point
				continue;
			}
			//ray is crossing over by the algorithm (common part of)
			if (point.x > Math.min(p1.x, p2.x) && point.x < Math.max(p1.x, p2.x)) {
				//x is before of ray
				if (point.y <= Math.max(p1.y, p2.y)) {
					//overlies on a horizontal ray
					if (p1.x == p2.x && point.y >= Math.min(p1.y, p2.y)) {
						return true;
					}
					//ray is vertical
					if (p1.y == p2.y) {
						if (p1.y == point.y) {
							//overlies on a vertical ray
							return true;
						} else {
							//before ray
							++intersectCount;
						}
					} else {//cross point on the left side
						double xInters = (point.x - p1.x) * (p2.y - p1.y) / (p2.x - p1.x) + p1.y;
						//cross point of y
						if (Math.abs(point.y - xInters) < precision) {
							//overlies on a ray
							return true;
						}
						if (point.y < xInters) {
							//before ray
							++intersectCount;
						}
					}
				}
			} else {
				//special case when ray is crossing through the vertex
				if (point.x == p2.x && point.y <= p2.y) {
					//p crossing over p2
					Point p3 = pts.get((i + 1) % n);
					//next vertex
					if (point.x >= Math.min(p1.x, p3.x) && point.x <= Math.max(p1.x, p3.x)) {
						//p.x lies between p1.x & p3.x
						++intersectCount;
					} else {
						intersectCount += 2;
					}
				}
			}
			//next ray left point
			p1 = p2;
		}
		return intersectCount % 2 != 0;
	}

	private static double getSlopeFactor(Point p1, Point p2) {
		return (p2.y - p1.y) * 1.0 / (p2.x - p1.x);
	}

	public static <T> T getStatusValue(List<StatusValue<T>> list, int status, T defaultValue) {
		for (int i = list.size() - 1; i >= 0; i--) {
			StatusValue<T> item = list.get(i);
			if ((status & item.status) == item.status) {
				return item.value;
			}
		}
		return defaultValue;
	}

	public static int dp2px(@NonNull Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static Bitmap scale(@NonNull Bitmap src, int newWidth, int newHeight) {
		return Bitmap.createScaledBitmap(src, newWidth, newHeight, true);
	}

	public static LatLng getMiddlePosition(IVertex pre, IVertex next, Projection projection) {
		Point pp = pre.getPoint();
		Point np = next.getPoint();
		Point p = new Point((pp.x + np.x) / 2, (pp.y + np.y) / 2);
		return projection.fromScreenLocation(p);
	}

	public static <T> void setStatusValue(List<StatusValue<T>> values, int status, T value) {
		for (StatusValue<T> item : values) {
			if (item.status == status) {
				item.value = value;
				return;
			}
		}
		values.add(new StatusValue<>(status, value));
	}

	public static LatLng getCenterOfGravityPoint(List<LatLng> mPoints) {
		double area = 0.0;//多边形面积
		double gx = 0.0, gy = 0.0;// 重心的x、y
		for (int i = 1; i <= mPoints.size(); i++) {
			double iLat = mPoints.get(i % mPoints.size()).latitude;
			double iLng = mPoints.get(i % mPoints.size()).longitude;
			double nextLat = mPoints.get(i - 1).latitude;
			double nextLng = mPoints.get(i - 1).longitude;
			double temp = (iLat * nextLng - iLng * nextLat) / 2.0;
			area += temp;
			gx += temp * (iLat + nextLat) / 3.0;
			gy += temp * (iLng + nextLng) / 3.0;
		}
		gx = gx / area;
		gy = gy / area;
		return new LatLng(gx, gy);
	}

	public static LatLng getCenterPoint(List<LatLng> points) {

		if (points.size() == 3) {
			double totalLat = 0;
			double totalLng = 0;
			for (LatLng point : points) {
				totalLat += point.latitude;
				totalLng += point.longitude;
			}
			double latitude = totalLat / points.size();
			double longitude = totalLng / points.size();
			return new LatLng(latitude, longitude);
		} else {
			double latitude = (getMinLatitude(points) + getMaxLatitude(points)) / 2;
			double longitude = (getMinLongitude(points) + getMaxLongitude(points)) / 2;
			return new LatLng(latitude, longitude);
		}
	}

	// 经度最小值
	private static double getMinLongitude(List<LatLng> mPoints) {
		double minLongitude = MAX_LNG;
		if (mPoints.size() > 0) {
			minLongitude = mPoints.get(0).longitude;
			for (LatLng latlng : mPoints) {
				// 经度最小值
				if (latlng.longitude < minLongitude)
					minLongitude = latlng.longitude;
			}
		}
		return minLongitude;
	}

	// 经度最大值
	private static double getMaxLongitude(List<LatLng> mPoints) {
		double maxLongitude = MIN_LNG;
		if (mPoints.size() > 0) {
			maxLongitude = mPoints.get(0).longitude;
			for (LatLng latlng : mPoints) {
				// 经度最大值
				if (latlng.longitude > maxLongitude)
					maxLongitude = latlng.longitude;
			}
		}
		return maxLongitude;
	}

	// 纬度最小值
	private static double getMinLatitude(List<LatLng> mPoints) {
		double minLatitude = MAX_LAT;
		if (mPoints.size() > 0) {
			minLatitude = mPoints.get(0).latitude;
			for (LatLng latlng : mPoints) {
				// 纬度最小值
				if (latlng.latitude < minLatitude)
					minLatitude = latlng.latitude;
			}
		}
		return minLatitude;
	}

	// 纬度最大值
	private static double getMaxLatitude(List<LatLng> mPoints) {
		double maxLatitude = MIN_LAT;
		if (mPoints.size() > 0) {
			maxLatitude = mPoints.get(0).latitude;
			for (LatLng latlng : mPoints) {
				// 纬度最大值
				if (latlng.latitude > maxLatitude)
					maxLatitude = latlng.latitude;
			}
		}
		return maxLatitude;
	}

	/**
	 * 计算把手旋转角度
	 *
	 * @param points 多边形的顶点在屏幕上的坐标点
	 * @param hp     把手在屏幕上的坐标点
	 * @param matrix 矩阵
	 * @return 把手旋转角度（高德 Marker 旋转角度）
	 */
	public static float computeHandleAngel(Context context, List<Point> points, Point hp, Matrix matrix) {
		if (points.size() <= 1) {
			// 只有一个点，无需旋转
			return 0;
		}
		// 把手指向的点在多边形中顶点中的索引
		int hi = points.indexOf(hp);
		if (hi < 0) {
			return 0;
		}
		// 把手前一个点的索引
		int pi;
		// 把手后一个点的索引
		int ni;
		// 把手前一个点在屏幕的位置
		Point pp;
		// 把手后一个点在屏幕的位置
		Point np;
		// 把手应该旋转的角度
		float a;
		// 两个普通顶点和一个中间点，组成的必定是一条直线
		if (points.size() == 3) {
			// 第一个点的索引
			pi = 0;
			// 最后一个点的索引
			ni = 2;
			pp = points.get(pi);
			np = points.get(ni);
			// 计算这条直线的斜率
			double k = PlotUtils.getSlopeFactor(pp, np);
			// 斜率转角度
			a = (float) (Math.atan(k) / Math.PI * 180);
			// 高德地图旋转 Marker 从 12 点钟方向开始逆时针计算，所以返回 -a
			return -a;
		}
		// 计算把手前一个点的索引
		pi = hi == 0 ? points.size() - 1 : hi - 1;
		// 计算把手后一个点的索引
		ni = hi == points.size() - 1 ? 0 : hi + 1;
		// 取出把手前一个点和后一个点
		pp = points.get(pi);
		np = points.get(ni);
		// 把手前一个点与把手组成直线的斜率
		double ps = PlotUtils.getSlopeFactor(pp, hp);
		// 把手后一个点与把手组成的直线的斜率
		double ns = PlotUtils.getSlopeFactor(np, hp);
		// 把手前一个点与把手组成直线的角度
		float pa = (float) (Math.atan(ps) / Math.PI * 180);
		// 把手后一个点与把手组成直线的角度
		float na = (float) (Math.atan(ns) / Math.PI * 180);
		// 额外的角度。斜率转的角度在 -90 和 90 之间，因此在某些情况下（90，-90，0）需要加上额外的角度值
		float ea = pa % 90 == 0 ? -pa : na % 90 == 0 ? -na : 0;
		// 把手在屏幕上 x 轴坐标值
		int x = hp.x;
		// 把手前一个点在屏幕上 x 轴坐标值
		int px = pp.x;
		// 把手后一个点在屏幕上 x 轴坐标值
		int nx = np.x;
		// 把手前一点、把手和把手后一点组成角的角平分线的角度。这个角度可能会有90度，-90度，180度的偏差。
		float v = (pa + na) / 2;
		a = v;
		// 修正偏差，先考虑 90 度和 -90 度的偏差
		if (x <= Math.min(px, nx)) {
			a = v - 90;
		} else if (x > Math.max(px, nx)) {
			a = v + 90;
		}
		// 加上额外的角度
		a += ea;
		// 考虑 180 度的偏差。
		// 在把手垂直中线上取一点，计算这个点在旋转 a 度后是否在多边形内部，如果是，角度就有是有偏差的
		// 第一步，在把手垂直中线上取一个点
		float[] p = {hp.x, hp.y - dp2px(context, 1)};
		// 第二步，旋转这个点
		matrix.reset();
		matrix.setRotate(a, hp.x, hp.y);
		matrix.mapPoints(p);
		hp.x = Math.round(p[0]);
		hp.y = Math.round(p[1]);
		// 第三步，检查这个点是否在多边形内部，如果是，将旋转角度加上 180
		if (PlotUtils.isPointInPolygon(hp, points)) {
			return -a + 180;
		}
		return -a;
	}

	public static double getDistance(Point p0, Point p1) {
		int dx = p0.x - p1.x;
		int dy = p0.y - p1.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public static LatLng toLocation(IMapFunctions map, int x, int y) {
		Point point = new Point(x, y);
		return map.getProjection().fromScreenLocation(point);
	}
}
