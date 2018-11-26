package com.hawk.map.impl;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.amap.api.maps.MapView;

/**
 * 高德地图 MapView 的包装类，用来屏蔽直接调用高德地图 api。该类提供 {@link #getMapManager()} 方法，返回一个地图管理对象
 * {@link MapManager}，通过该对象可获取地图事件监听对象的回调和操作地图。使用该对象的方式与高德地图的 {@link MapView} 一致，
 * 必须在生命周期感知组件（例如 Activity，Fragment）的各个生命周期方法中调用该对象对应的生命周期方法。
 * <br/>
 * 使用示例：
 * <pre>
 * public class A extends Activity {
 *      private AmapView mapView;
 *
 *      protected void onCreate(@Nullable Bundle savedInstanceState) {
 *          super.onCreate(savedInstanceState);
 *          ...
 *          mapView.onCreate(savedInstanceState);
 *      }
 *
 *      protected void onSaveInstanceState(Bundle outState) {
 *          super.onSaveInstanceState(outState);
 *          mapView.onSaveInstanceState(outState);
 *      }
 *
 *      protected void onPause() {
 *          super.onPause();
 *          mapView.onPause();
 *      }
 *
 *      protected void onDestroy() {
 *          super.onDestroy();
 *          mapView.onDestroy();
 *      }
 *
 *      public void onLowMemory() {
 *          super.onLowMemory();
 *          mapView.onLowMemory();
 *      }
 * }
 * </pre>
 * Created on 2018/11/19 13:30
 *
 * @author WingHawk
 * @see MapManager
 */
public class AmapView extends FrameLayout {

	private MapManager mapManager;
	private MapView mMapView;

	public AmapView(Context context) {
		super(context);
		init();
	}

	public AmapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AmapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		Context context = getContext();
		mMapView = new MapView(context);
		addView(mMapView);
		AmapProxy amapProxy = new AmapProxy(mMapView.getMap());
		mapManager = new MapManager(amapProxy);
	}

	public MapManager getMapManager() {
		return mapManager;
	}

	public void onCreate(Bundle bundle) {
		mMapView.onCreate(bundle);
	}

	public void onLowMemory() {
		mMapView.onLowMemory();
	}

	public void onSaveInstanceState(Bundle bundle) {
		mMapView.onSaveInstanceState(bundle);
	}

	public void onResume() {
		mMapView.onResume();
	}

	public void onPause() {
		mMapView.onPause();
	}

	public void onDestroy() {
		mMapView.onDestroy();
	}
}
