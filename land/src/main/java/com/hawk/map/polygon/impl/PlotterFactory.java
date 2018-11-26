package com.hawk.map.polygon.impl;

import com.hawk.map.polygon.core.IHandle;
import com.hawk.map.polygon.core.IHistoryManager;
import com.hawk.map.polygon.core.IPlotterFactory;
import com.hawk.map.polygon.core.IPolygon;
import com.hawk.map.polygon.core.IVertex;

/**
 * Created on 2018/11/20 13:07
 *
 * @author WingHawk
 */
public class PlotterFactory implements IPlotterFactory {

	@Override
	public IPolygon createPolygon(PolygonPlotter polygonPlotter) {
		Polygone polygone = new Polygone();
		polygone.setParent(polygonPlotter);
		return polygone;
	}

	@Override
	public IVertex createVertex(int type, IPolygon polygon) {
		Vertex vertex = new Vertex();
		vertex.setParent(polygon);
		vertex.setType(type);
		return vertex;
	}

	@Override
	public IHandle createHandle(IPolygon polygon) {
		Handle handle = new Handle();
		handle.setParent(polygon);
		return handle;
	}

	@Override
	public IHistoryManager createHistoryManager(IPolygon polygon) {
		return new HistoryManager(polygon);
	}
}
