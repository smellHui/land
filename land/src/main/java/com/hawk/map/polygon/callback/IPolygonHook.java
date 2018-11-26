package com.hawk.map.polygon.callback;

import com.hawk.map.polygon.core.IVertex;
import com.hawk.map.polygon.impl.Polygone;

/**
 * Created on 2018/11/23 12:38
 *
 * @author WingHawk
 */
public interface IPolygonHook {

	void beforeNormalVertexAdd(int index, IVertex vertex);

	void afterNormalVertexAdd(int index, IVertex vertex);

	void beforeNormalVertexRemove(IVertex vertex);

	void afterNormalVertexRemove(IVertex vertex);

	void beforeVertexMove(IVertex vertex);

	void afterVertexMove(IVertex vertex);

	void beforeVertexTypeSwitch(IVertex vertex);

	void afterVertexTypeSwitch(IVertex vertex);

	void beforePolygonClose(Polygone polygon);

	void afterPolygonClose(Polygone polygon);
}
