package com.hawk.map.observer;

import com.hawk.map.core.IMapFunctions;
import com.hawk.map.impl.MapManager;

/**
 * 地图管理类 {@link MapManager} 所管理对象的接口。实现该接口的对象必须保证其实现的 {@link #getPriority()}
 * 方法的返回值唯一且不可变，{@link MapManager} 会根据该方法的返回值对所有添加到其中的 {@link IMapObserver}
 * 对象进行排序，并在地图相关事件产生时根据优先级来将事件分发到对该事件有需求的 {@link IMapObserver} 对象中。
 * <br/>
 * Created on 2018/11/19 14:55
 *
 * @author WingHawk
 * @see MapManager
 */
public interface IMapObserver {

	/**
	 * 该对象被添加到 {@link MapManager} 中。
	 * <b>Note! 该方法被调用并不意味着此时 map 对象可用，请在 {@link IMapLoadObserver#onMapLoaded()} 方法中操作地图。</b>
	 *
	 * @param map 地图功能对象，通过该对象可以操作地图
	 */
	void onAttach(IMapFunctions map);

	/**
	 * 该对象被从 {@link MapManager} 中移除。
	 */
	void onDetach();

	/**
	 * 获取该对象相对于其它同类对象的优先级，优先级越高，它会越先接受到地图上事件的回调。
	 *
	 * @return 该对象的优先级
	 */
	int getPriority();
}
