package com.hawk.map.observer;

import com.amap.api.maps.model.IndoorBuildingInfo;

public interface IMapIndoorBuildingActiveObserver extends IMapObserver {
	
	void onIndoorBuilding(IndoorBuildingInfo info);
}
