package com.hawk.map.utils;

import android.graphics.Point;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

public class JtsHelper {
	private static final JtsHelper mInstance = new JtsHelper();
	private GeometryFactory mGeometryFactory = new GeometryFactory();

	public static JtsHelper getInstance() {
		return mInstance;
	}

	private JtsHelper() { }

	public boolean isSelfCross(List<Point> points) {
		if (points.size() < 4) {
			return false;
		}
		Coordinate[] coordinates = convert(points);
		Polygon polygon = mGeometryFactory.createPolygon(coordinates);
		return !polygon.isSimple();
	}

	public List<Point> union(List<Point>... polygons) {
		Geometry[] geometries = new Geometry[polygons.length];
		for (int i = 0; i < polygons.length; i++) {
			List<Point> polygon = polygons[i];
			Coordinate[] coordinates = convert(polygon);
			Polygon p = mGeometryFactory.createPolygon(coordinates);
			geometries[i] = p;
		}
		Geometry union = mGeometryFactory.createGeometryCollection(geometries).union();
		Coordinate[] coordinates = union.getCoordinates();
		return convert(coordinates);
	}

	public static boolean isConcavePolygon(List<Point> points) {
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


	public static Point[] toArray(List<Point> points) {
		Point[] pointArr = new Point[points.size()];
		points.toArray(pointArr);
		return pointArr;
	}

	public static Coordinate[] convert(List<Point> points) {
		Coordinate[] coordinates = new Coordinate[points.size() + 1];
		for (int i = 0; i < points.size(); i++) {
			Point point = points.get(i);
			coordinates[i] = new Coordinate(point.x, point.y);
		}
		// the last vertex must be same as the first one in jts system
		coordinates[points.size()] = new Coordinate(points.get(0).x, points.get(0).y);
		return coordinates;
	}

	public static List<Point> convert(Coordinate[] coordinates) {
		ArrayList<Point> points = new ArrayList<>(coordinates.length);
		// the last vertex is same as the first one
		for (int i = 0; i < coordinates.length - 1; i++) {
			Coordinate coordinate = coordinates[i];
			Point point = new Point(((int) coordinate.x), ((int) coordinate.y));
			points.add(point);
		}
		return points;
	}


}
