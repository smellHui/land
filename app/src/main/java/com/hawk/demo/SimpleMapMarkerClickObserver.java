package com.hawk.demo;

import com.amap.api.maps.model.Marker;
import com.hawk.map.core.IMapFunctions;
import com.hawk.map.observer.IMapMarkerClickObserver;

/**
 * Created on 2018/11/26 15:21
 *
 * @author WingHawk
 */
public class SimpleMapMarkerClickObserver implements IMapMarkerClickObserver {
	@Override
	public boolean onMarkerClick(Marker marker) {
		return true;
	}

	@Override
	public void onAttach(IMapFunctions iMapFunctions) {

	}

	@Override
	public void onDetach() {

	}

	@Override
	public int getPriority() {
		return 0;
	}
}
