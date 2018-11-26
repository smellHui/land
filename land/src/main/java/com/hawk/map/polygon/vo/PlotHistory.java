package com.hawk.map.polygon.vo;

import com.amap.api.maps.model.LatLng;
import com.hawk.map.polygon.core.IPolygon;
import com.hawk.map.polygon.core.IVertex;

/**
 * Created on 2018/11/22 10:26
 *
 * @author WingHawk
 */
public class PlotHistory {
	public static final int TYPE_VERTEX_ADD = 1;
	public static final int TYPE_VERTEX_REMOVE = 2;
	public static final int TYPE_VERTEX_MOVE = 3;
	public static final int TYPE_VERTEX_TYPE_SWITCH = 4;
	public static final int TYPE_POLYGON_CLOSE = 5;

	public final LatLng originPos;
	public final IVertex target;
	public final IVertex focus;
	public final int changeType;
	public final int recoverIndex;
	public final int originVertexType;
	public final int originPolygonStatus;

	public PlotHistory(IVertex target,int recoverIndex, int changeType) {
		this.target = target;
		IPolygon parent = target.getParent();
		this.focus = parent.getFocusedVertex();
		this.originPolygonStatus = parent.getStatus();
		this.originPos = target.getPosition();
		this.originVertexType = target.getType();
		this.recoverIndex = recoverIndex;
		this.changeType = changeType;
	}
}
