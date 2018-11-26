package com.hawk.map.observer;

import com.amap.api.maps.model.Poi;

public interface IMapPOIClickObserver extends IMapObserver {

	void onPOIClick(Poi poi);
}
