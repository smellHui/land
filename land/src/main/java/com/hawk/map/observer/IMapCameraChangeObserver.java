package com.hawk.map.observer;

import com.amap.api.maps.model.CameraPosition;

public interface IMapCameraChangeObserver extends IMapObserver {

	void onCameraChange(CameraPosition cameraPosition);

	void onCameraChangeFinish(CameraPosition cameraPosition);
}