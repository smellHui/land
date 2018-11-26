package com.hawk.map.observer;

import com.amap.api.maps.model.Marker;

public interface IMapMarkerClickObserver extends IMapObserver {

	boolean onMarkerClick(Marker marker);
}
