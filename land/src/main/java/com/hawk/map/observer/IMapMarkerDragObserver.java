package com.hawk.map.observer;

import com.amap.api.maps.model.Marker;

public interface IMapMarkerDragObserver extends IMapObserver {

	void onMarkerDragStart(Marker marker);

	void onMarkerDrag(Marker marker);

	void onMarkerDragEnd(Marker marker);
}
