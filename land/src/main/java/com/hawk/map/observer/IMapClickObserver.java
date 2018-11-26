package com.hawk.map.observer;

import com.amap.api.maps.model.LatLng;

public interface IMapClickObserver extends IMapObserver {

	boolean onMapClick(LatLng latLng);
}
