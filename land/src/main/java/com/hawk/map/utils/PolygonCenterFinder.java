package com.hawk.map.utils;

import android.graphics.Point;

import com.amap.api.maps.AMap;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.LatLng;
import com.hawk.map.polygon.PlotUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class PolygonCenterFinder {

	private static final double PRECISION = 2e-10;
	private static final int OFFSET = 1000000;
	/**
	 * 1. find polygon gravity center point
	 * 2. if polygon is not concave, the gcp is the center point.
	 * 3. else whe check whether the gcp is in the polygon
	 * 4. we get gcp nearest cross points of the polygon, and caculate the center point.
	 */
	public static LatLng findCenter(AMap map, List<LatLng> posList) {
		Projection projection = map.getProjection();
		ArrayList<Point> points = new ArrayList<>();
		for (LatLng pos : posList) {
			Point p = projection.toScreenLocation(pos);
			points.add(p);
		}
		// gravity center point
		Point gcp = projection.toScreenLocation(PlotUtils.getCenterOfGravityPoint(posList));
		if (!isConcavePolygon(points)) {
			// if not concave polygon, the gcp is the center point
			return projection.fromScreenLocation(gcp);
		}
		// center point
		Point cp = projection.toScreenLocation(PlotUtils.getCenterPoint(posList));
		int dx = gcp.x - cp.x;
		int dy = gcp.y - cp.y;
		Point result = findCenter(gcp, points, dx, dy);
		result = result == null ? gcp : result;
		return projection.fromScreenLocation(result);
	}

	private static Point findCenter(Point gcp, ArrayList<Point> points, int dx, int dy) {
		// center point in x axis
		Point cpx = null;
		// center point in y axis
		Point cpy = null;
		// center x
		int cx;
		// center y
		int cy = -1;
		Point p0 = new Point();
		Point p1 = new Point();
		if (!isPointInPolygon(gcp, points)) {
			p0.set(gcp.x - OFFSET, gcp.y);
			p1.set(gcp.x + OFFSET, gcp.y);
			cx = getCxOut(gcp, points, dx, p0, p1);
			if (cx != -1) {
				p0.set(cx, gcp.y - OFFSET);
				p1.set(cx, gcp.y + OFFSET);
				cy = getCyIn(gcp, points, cy, p0, p1);
				if (cy != -1) {
					cpx = new Point(cx, cy);
				}
			}
			p0.set(gcp.x, gcp.y - OFFSET);
			p1.set(gcp.x, gcp.y + OFFSET);
			cy = getCyOut(gcp, points, dy, p0, p1);
			if (cy != -1) {
				p0.set(gcp.x - OFFSET, cy);
				p1.set(gcp.x + OFFSET, cy);
				cx = getCxIn(gcp, points, p0, p1);
				if (cx != -1) {
					cpy = new Point(cx, cy);
				}
			}
		} else {
			p0.set(gcp.x - OFFSET, gcp.y);
			p1.set(gcp.x + OFFSET, gcp.y);
			cx = getCxIn(gcp, points, p0, p1);
			p0.set(cx, gcp.y - OFFSET);
			p1.set(cx, gcp.y + OFFSET);
			cy = getCyIn(gcp, points, cy, p0, p1);
			cpx = new Point(cx, cy);

			p0.set(gcp.x, gcp.y - OFFSET);
			p1.set(gcp.x, gcp.y + OFFSET);
			cy = getCyIn(gcp, points, cy, p0, p1);
			p0.set(gcp.x - OFFSET, cy);
			p1.set(gcp.x + OFFSET, cy);
			cx = getCxIn(gcp, points, p0, p1);
			cpy = new Point(cx, cy);
		}

		if (Math.abs(dx) > Math.abs(dy) && cpx != null && isPointInPolygon(cpx, points)) {
			return cpx;
		} else if (cpy != null && isPointInPolygon(cpy, points)) {
			return cpy;
		} else {
			return null;
		}
	}

	private static int getCxIn(Point gcp, ArrayList<Point> points, Point p0, Point p1) {
		int cx = -1;
		TreeSet<Point> crosses = findCrossPoints(p0, p1, points, new Comparator<Point>() {
			@Override
			public int compare(Point o1, Point o2) {
				return o1.x - o2.x;
			}
		});
		Point l = crosses.lower(gcp);
		Point h = crosses.higher(gcp);
		if (l != null && h != null) {
			cx = (l.x + h.x) / 2;
		}
		return cx;
	}

	private static int getCyIn(Point gcp, ArrayList<Point> points, int cy, Point p0, Point p1) {
		TreeSet<Point> crosses = findCrossPoints(p0, p1, points, new Comparator<Point>() {
			@Override
			public int compare(Point o1, Point o2) {
				return o1.y - o2.y;
			}
		});
		Point l = crosses.lower(gcp);
		Point h = crosses.higher(gcp);
		if (l != null && h != null) {
			cy = (l.y + h.y) / 2;
		}
		return cy;
	}

	private static int getCxOut(Point gcp, ArrayList<Point> points, int dx, Point p0, Point p1) {
		TreeSet<Point> crosses = findCrossPoints(p0, p1, points, new Comparator<Point>() {
			@Override
			public int compare(Point o1, Point o2) {
				return o1.x - o2.x;
			}
		});
		int cxLeft = -1;
		int cxRight = -1;
		Point l1 = crosses.lower(gcp);
		if (l1 != null) {
			Point l2 = crosses.lower(l1);
			if (l2 != null) {
				cxLeft = (l1.x + l2.x) / 2;
			}
		}
		Point h1 = crosses.higher(gcp);
		if (h1 != null) {
			Point h2 = crosses.higher(h1);
			if (h2 != null) {
				cxRight = (h1.x + h2.x) / 2;
			}
		}
		return (dx < 0 && cxLeft != -1) ? cxLeft : cxRight;
	}

	private static int getCyOut(Point gcp, ArrayList<Point> points, int dy, Point p0, Point p1) {
		TreeSet<Point> crosses = findCrossPoints(p0, p1, points, new Comparator<Point>() {
			@Override
			public int compare(Point o1, Point o2) {
				return o1.y - o2.y;
			}
		});
		int cyTop = -1;
		int cyBottom = -1;
		Point l1 = crosses.lower(gcp);
		if (l1 != null) {
			Point l2 = crosses.lower(l1);
			if (l2 != null) {
				cyTop = (l1.y + l2.y) / 2;
			}
		}
		Point h1 = crosses.higher(gcp);
		if (h1 != null) {
			Point h2 = crosses.higher(h1);
			if (h2 != null) {
				cyBottom = (h1.y + h2.y) / 2;
			}
		}
		return (dy < 0 && cyTop != -1) ? cyTop : cyBottom;
	}

	private static boolean isPointInPolygon(Point point, List<Point> pts) {
		int n = pts.size();
		if (n < 3) {
			return false;
		}
		//cross mPoints count of x
		int intersectCount = 0;
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
					} else {
						//cross point on the left side
						double xInters = (point.x - p1.x) * (p2.y - p1.y) / (p2.x - p1.x) + p1.y;
						//cross point of y
						if (Math.abs(point.y - xInters) < PRECISION) {
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

	private static boolean isConcavePolygon(List<Point> points) {
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			Point np = points.get((i + 1) % points.size());
			Point pp = points.get(i == 0 ? points.size() - 1 : i - 1);
			int x = np.x - p.x;
			int y = np.y - p.y;
			if (y * pp.x + x * np.y - y * np.x > x * pp.y) {
				return true;
			}
		}
		return false;
	}

	private static TreeSet<Point> findCrossPoints(Point p1, Point p2, List<Point> polygon, Comparator<Point> order) {
		TreeSet<Point> crosses = new TreeSet<>(order);
		int size = polygon.size();
		for (int i = 0; i < size; i++) {
			Point p3 = polygon.get(i);
			Point p4 = polygon.get((i + 1) % size);
			Point cp = findCrossPoint(p1, p2, p3, p4);
			if (cp != null) {
				crosses.add(cp);
			}
		}
		return crosses;
	}

	private static Point findCrossPoint(Point p0, Point p1, Point p2, Point p3) {
		float x0 = p1.x - p0.x;
		float y0 = p1.y - p0.y;
		float x1 = p3.x - p2.x;
		float y1 = p3.y - p2.y;

		float det = x0 * y1 - x1 * y0;
		if (det == 0) {
			return null;
		}
		boolean detPositive = det > 0;
		float x2 = p0.x - p2.x;
		float y2 = p0.y - p2.y;
		float s = x0 * y2 - y0 * x2;
		if (s < 0 == detPositive) {
			return null;
		}
		float t = x1 * y2 - y1 * x2;
		if (t < 0 == detPositive) {
			return null;
		}
		if (Math.abs(s) > Math.abs(det) || Math.abs(t) > Math.abs(det)) {
			return null;
		}
		float k = t / det;
		float x = p0.x + k * x0;
		float y = p0.y + k * y0;
		return new Point(((int) x), ((int) y));
	}
}

