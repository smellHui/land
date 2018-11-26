package com.hawk.map.observer;

import com.amap.api.maps.model.LatLng;

public interface IMapLongClickObserver extends IMapObserver {

	boolean onMapLongClick(LatLng latLng);
}
