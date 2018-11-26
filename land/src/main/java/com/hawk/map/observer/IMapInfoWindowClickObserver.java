package com.hawk.map.observer;

import com.amap.api.maps.model.Marker;

public interface IMapInfoWindowClickObserver extends IMapObserver {

	void onInfoWindowClick(Marker marker);
}
