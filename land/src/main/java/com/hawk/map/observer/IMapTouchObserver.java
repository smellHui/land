package com.hawk.map.observer;

import android.view.MotionEvent;

public interface IMapTouchObserver extends IMapObserver {

	boolean onMapTouch(MotionEvent event);
}
