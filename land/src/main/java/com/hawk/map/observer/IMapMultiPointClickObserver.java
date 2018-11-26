package com.hawk.map.observer;

import com.amap.api.maps.model.MultiPointItem;

public interface IMapMultiPointClickObserver extends IMapObserver {

	boolean onPointClick(MultiPointItem item);
}
