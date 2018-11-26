package com.hawk.map.core;

import com.amap.api.maps.AMap;

/**
 * 地图监听接口
 * <br/>
 * Created on 2018/11/19 15:55
 *
 * @author WingHawk
 */
public interface IMapListeners {

	void setOnMapLoadedListener(AMap.OnMapLoadedListener listener);

	void setOnMapClickListener(AMap.OnMapClickListener listener);

	void setOnMarkerClickListener(AMap.OnMarkerClickListener listener);

	void setOnMapTouchListener(AMap.OnMapTouchListener listener);

	void setOnCameraChangeListener(AMap.OnCameraChangeListener listener);

	void setOnMapLongClickListener(AMap.OnMapLongClickListener listener);

	void setOnMultiPointClickListener(AMap.OnMultiPointClickListener listener);

	void setOnIndoorBuildingActiveListener(AMap.OnIndoorBuildingActiveListener listener);

	void setOnPOIClickListener(AMap.OnPOIClickListener listener);

	void setOnPolylineClickListener(AMap.OnPolylineClickListener listener);

	void setOnMarkerDragListener(AMap.OnMarkerDragListener listener);

	void setOnInfoWindowClickListener(AMap.OnInfoWindowClickListener listener);

	void setOnMyLocationChangeListener(AMap.OnMyLocationChangeListener listener);
}
