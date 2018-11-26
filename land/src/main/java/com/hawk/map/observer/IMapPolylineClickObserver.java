package com.hawk.map.observer;

import com.amap.api.maps.model.Polyline;

public interface IMapPolylineClickObserver extends IMapObserver {

	void onPolylineClick(Polyline polyline);
}
