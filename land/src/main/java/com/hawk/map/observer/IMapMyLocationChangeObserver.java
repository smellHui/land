package com.hawk.map.observer;

import android.location.Location;

public interface IMapMyLocationChangeObserver extends IMapObserver {
	void onMyLocationChange(Location location);
}
