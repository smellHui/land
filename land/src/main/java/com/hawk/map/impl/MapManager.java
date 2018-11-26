package com.hawk.map.impl;

import android.location.Location;
import android.view.MotionEvent;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.IndoorBuildingInfo;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MultiPointItem;
import com.amap.api.maps.model.Poi;
import com.amap.api.maps.model.Polyline;
import com.hawk.map.core.IMap;
import com.hawk.map.core.IMapFunctions;
import com.hawk.map.observer.IMapCameraChangeObserver;
import com.hawk.map.observer.IMapClickObserver;
import com.hawk.map.observer.IMapIndoorBuildingActiveObserver;
import com.hawk.map.observer.IMapInfoWindowClickObserver;
import com.hawk.map.observer.IMapLoadObserver;
import com.hawk.map.observer.IMapLongClickObserver;
import com.hawk.map.observer.IMapMarkerClickObserver;
import com.hawk.map.observer.IMapMarkerDragObserver;
import com.hawk.map.observer.IMapMultiPointClickObserver;
import com.hawk.map.observer.IMapMyLocationChangeObserver;
import com.hawk.map.observer.IMapObserver;
import com.hawk.map.observer.IMapPOIClickObserver;
import com.hawk.map.observer.IMapPolylineClickObserver;
import com.hawk.map.observer.IMapTouchObserver;
import com.hawk.map.polygon.impl.PolygonPlotter;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * 地图管理类，通过该类可获取地图功能对象和注册地图相关回调。
 * 该类实例仅可通过 {@link AmapView#getMapManager()} 获取。
 * 如果某一对象需要获取地图相关回调，那么需要让其实现 {@link com.hawk.map.observer} 包下对应的接口，并调用
 * MapManager 的 {@link #addObserver(IMapObserver)} 方法将其添加到 {@link MapManager} 中。具体可参考
 * {@link PolygonPlotter} 类中的做法，该类实现了 {@link IMapTouchObserver} 接口，并在
 * {@link IMapTouchObserver#onMapTouch(MotionEvent)} 方法中获取和处理了地图的触摸事件。该类内部维护一个
 * TreeSet 来存放所有的 IMapObserver，给定的比较器只会比较 IMapObserver 的 getPriority() 方法的返回值，
 * 因此<b>所有实现 IMapObserver 接口的对象中的 getPriority() 方法的返回值必须唯一，且在该对象被添加到 MapManager
 * 中后请勿修改 getPriority() 方法的返回值！</b>当地图上有事件产生（比如地图被点击，Marker 被拖拽等等），MapManager
 * 会根据添加到其中的 IMapObserver 对象的优先级来将事件分发到对此事件有需求的 IMapObserver 中。
 * <br/>
 * 使用示例：
 * <pre>
 * MapManager mapManager = mapView.getMapManager();
 * PolygonPlotter plotter = new PolygonPlotter(context);
 * mapManager.addObserver(plotter);
 * </pre>
 * Created on 2018/11/19 15:30
 *
 * @author WingHawk
 * @see IMapObserver
 * @see PolygonPlotter
 */
public class MapManager {

	private IMap map;
	private TreeSet<IMapObserver> mObservers = new TreeSet<>(new MapObserverComparator());

	MapManager(IMap map) {
		this.map = map;
		map.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
			@Override
			public void onMapLoaded() {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapLoadObserver) {
						((IMapLoadObserver) o).onMapLoaded();
					}
				}
			}
		});
		map.setOnMapClickListener(new AMap.OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latLng) {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapClickObserver) {
						if (((IMapClickObserver) o).onMapClick(latLng)) {
							return;
						}
					}
				}
			}
		});
		map.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapMarkerClickObserver) {
						if (((IMapMarkerClickObserver) o).onMarkerClick(marker)) {
							return true;
						}
					}
				}
				return false;
			}
		});
		map.setOnMapTouchListener(new AMap.OnMapTouchListener() {
			@Override
			public void onTouch(MotionEvent motionEvent) {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapTouchObserver) {
						if (((IMapTouchObserver) o).onMapTouch(motionEvent)) {
							return;
						}
					}
				}
			}
		});
		map.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition cameraPosition) {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapCameraChangeObserver) {
						((IMapCameraChangeObserver) o).onCameraChange(cameraPosition);
					}
				}
			}

			@Override
			public void onCameraChangeFinish(CameraPosition cameraPosition) {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapCameraChangeObserver) {
						((IMapCameraChangeObserver) o).onCameraChangeFinish(cameraPosition);
					}
				}
			}
		});
		map.setOnIndoorBuildingActiveListener(new AMap.OnIndoorBuildingActiveListener() {
			@Override
			public void OnIndoorBuilding(IndoorBuildingInfo indoorBuildingInfo) {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapIndoorBuildingActiveObserver) {
						((IMapIndoorBuildingActiveObserver) o).onIndoorBuilding(indoorBuildingInfo);
					}
				}
			}
		});
		map.setOnMapLongClickListener(new AMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng latLng) {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapLongClickObserver) {
						if (((IMapLongClickObserver) o).onMapLongClick(latLng)){
							return;
						}
					}
				}
			}
		});
		map.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapInfoWindowClickObserver) {
						((IMapInfoWindowClickObserver) o).onInfoWindowClick(marker);
					}
				}
			}
		});
		map.setOnMultiPointClickListener(new AMap.OnMultiPointClickListener() {
			@Override
			public boolean onPointClick(MultiPointItem multiPointItem) {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapMultiPointClickObserver) {
						if (((IMapMultiPointClickObserver) o).onPointClick(multiPointItem)) {
							return true;
						}
					}
				}
				return false;
			}
		});
		map.setOnMarkerDragListener(new AMap.OnMarkerDragListener() {
			@Override
			public void onMarkerDragStart(Marker marker) {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapMarkerDragObserver) {
						((IMapMarkerDragObserver) o).onMarkerDragStart(marker);
					}
				}
			}

			@Override
			public void onMarkerDrag(Marker marker) {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapMarkerDragObserver) {
						((IMapMarkerDragObserver) o).onMarkerDrag(marker);
					}
				}
			}

			@Override
			public void onMarkerDragEnd(Marker marker) {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapMarkerDragObserver) {
						((IMapMarkerDragObserver) o).onMarkerDragEnd(marker);
					}
				}
			}
		});
		map.setOnPOIClickListener(new AMap.OnPOIClickListener() {
			@Override
			public void onPOIClick(Poi poi) {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapPOIClickObserver) {
						((IMapPOIClickObserver) o).onPOIClick(poi);
					}
				}
			}
		});
		map.setOnPolylineClickListener(new AMap.OnPolylineClickListener() {
			@Override
			public void onPolylineClick(Polyline polyline) {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapPolylineClickObserver) {
						((IMapPolylineClickObserver) o).onPolylineClick(polyline);
					}
				}
			}
		});
		map.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
			@Override
			public void onMyLocationChange(Location location) {
				for (IMapObserver o : mObservers) {
					if (o instanceof IMapMyLocationChangeObserver) {
						((IMapMyLocationChangeObserver) o).onMyLocationChange(location);
					}
				}
			}
		});
	}

	/**
	 * 获取地图功能对象，通过该对象可操作地图
	 *
	 * @return 地图功能对象
	 */
	public IMapFunctions getMapFunctions() {
		return map;
	}

	private class MapObserverComparator implements Comparator<IMapObserver> {

		@Override
		public int compare(IMapObserver o1, IMapObserver o2) {
			return o2.getPriority() - o1.getPriority();
		}
	}

	/**
	 * 添加一个需要获取地图事件回调的对象到 MapManager 中。如果已添加的 observer 集合中含有与待添加的 observer 相同
	 * priority 的条目，返回添加失败。
	 * 该对象被成功添加后将立即调用它的 {@link IMapObserver#onAttach(IMapFunctions)} 方法。
	 *
	 * @param observer 需要获取地图事件回调的对象
	 * @return true 添加成功，false 添加失败
	 * @see IMapObserver
	 */
	public boolean addObserver(IMapObserver observer) {
		boolean success = mObservers.add(observer);
		if (!success) {
			return false;
		}
		observer.onAttach(map);
		return true;
	}

	/**
	 * 判断某个 IMapObserver 对象是否已被添加到 MapManager 中
	 *
	 * @param observer 待判断的对象
	 * @return true 已被添加，false 未被添加
	 */
	public boolean hasObserver(IMapObserver observer) {
		return mObservers.contains(observer);
	}

	/**
	 * 将一个已添加的 IMapObserver 对象从 MapManager 中移除，如果该对象未被添加到 MapManager 中，返回移除失败。
	 * 该对象被成功移除后将立即调用它的 {@link IMapObserver#onDetach()} 方法。
	 *
	 * @param observer 需要移除的 IMapObserver 对象
	 * @return true 移除成功，false 移除失败
	 * @see #addObserver(IMapObserver)
	 */
	public boolean removeObserver(IMapObserver observer) {
		boolean result = mObservers.remove(observer);
		if (!result) {
			return false;
		}
		observer.onDetach();
		return true;
	}
}