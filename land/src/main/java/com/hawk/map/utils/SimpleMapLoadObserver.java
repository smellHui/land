package com.hawk.map.utils;

import com.hawk.map.core.IMapFunctions;
import com.hawk.map.observer.IMapLoadObserver;

/**
 * Created on 2018/11/19 20:57
 *
 * @author WingHawk
 */
public abstract class SimpleMapLoadObserver implements IMapLoadObserver {

	@Override
	public void onAttach(IMapFunctions map) {

	}

	@Override
	public void onDetach() {

	}

	@Override
	public int getPriority() {
		return 0;
	}
}
