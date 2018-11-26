package com.hawk.map.impl;

import android.location.Location;
import android.util.Pair;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CustomRenderer;
import com.amap.api.maps.InfoWindowAnimationManager;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.Projection;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.Arc;
import com.amap.api.maps.model.ArcOptions;
import com.amap.api.maps.model.BuildingOverlay;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.CrossOverlay;
import com.amap.api.maps.model.CrossOverlayOptions;
import com.amap.api.maps.model.GL3DModel;
import com.amap.api.maps.model.GL3DModelOptions;
import com.amap.api.maps.model.GroundOverlay;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.IndoorBuildingInfo;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MultiPointOverlay;
import com.amap.api.maps.model.MultiPointOverlayOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.MyTrafficStyle;
import com.amap.api.maps.model.NavigateArrow;
import com.amap.api.maps.model.NavigateArrowOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.RouteOverlay;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.amap.api.maps.model.TileOverlay;
import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.maps.model.particle.ParticleOverlay;
import com.amap.api.maps.model.particle.ParticleOverlayOptions;
import com.autonavi.amap.mapcore.IPoint;
import com.hawk.map.core.IMap;

import java.util.ArrayList;
import java.util.List;

class AmapProxy implements IMap {

	private AMap map;

	AmapProxy(AMap map) {
		this.map = map;
	}

	@Override
	public Arc addArc(ArcOptions options) {
		return map.addArc(options);
	}

	@Override
	public CameraPosition getCameraPosition() {
		return map.getCameraPosition();
	}

	@Override
	public float getMaxZoomLevel() {
		return map.getMaxZoomLevel();
	}

	@Override
	public float getMinZoomLevel() {
		return map.getMinZoomLevel();
	}

	@Override
	public void moveCamera(CameraUpdate cameraUpdate) {
		map.moveCamera(cameraUpdate);
	}

	@Override
	public void animateCamera(CameraUpdate cameraUpdate) {
		map.moveCamera(cameraUpdate);
	}

	@Override
	public void animateCamera(CameraUpdate cameraUpdate, AMap.CancelableCallback callback) {
		map.animateCamera(cameraUpdate, callback);
	}

	@Override
	public void animateCamera(CameraUpdate cameraUpdate, long duration, AMap.CancelableCallback callback) {
		map.animateCamera(cameraUpdate, duration, callback);
	}

	@Override
	public void stopAnimation() {
		map.stopAnimation();
	}

	@Override
	public NavigateArrow addNavigateArrow(NavigateArrowOptions options) {
		return map.addNavigateArrow(options);
	}

	@Override
	public Polyline addPolyline(PolylineOptions options) {
		return map.addPolyline(options);
	}

	@Override
	public BuildingOverlay addBuildingOverlay() {
		return map.addBuildingOverlay();
	}

	@Override
	public Circle addCircle(CircleOptions options) {
		return map.addCircle(options);
	}

	@Override
	public Polygon addPolygon(PolygonOptions options) {
		return map.addPolygon(options);
	}

	@Override
	public GroundOverlay addGroundOverlay(GroundOverlayOptions options) {
		return map.addGroundOverlay(options);
	}

	@Override
	public Marker addMarker(MarkerOptions options) {
		return map.addMarker(options);
	}

	@Override
	public GL3DModel addGL3DModel(GL3DModelOptions options) {
		return map.addGL3DModel(options);
	}

	@Override
	public Text addText(TextOptions options) {
		return map.addText(options);
	}

	@Override
	public ArrayList<Marker> addMarkers(ArrayList<MarkerOptions> optionsList, boolean b) {
		return map.addMarkers(optionsList, b);
	}

	@Override
	public List<Marker> getMapScreenMarkers() {
		return map.getMapScreenMarkers();
	}

	@Override
	public TileOverlay addTileOverlay(TileOverlayOptions options) {
		return map.addTileOverlay(options);
	}

	@Override
	public MultiPointOverlay addMultiPointOverlay(MultiPointOverlayOptions options) {
		return map.addMultiPointOverlay(options);
	}

	@Override
	public ParticleOverlay addParticleOverlay(ParticleOverlayOptions options) {
		return map.addParticleOverlay(options);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public void clear(boolean b) {
		map.clear(b);
	}

	@Override
	public int getMapType() {
		return map.getMapType();
	}

	@Override
	public void setMapType(int mapType) {
		map.setMapType(mapType);
	}

	@Override
	public boolean isTrafficEnabled() {
		return map.isTrafficEnabled();
	}

	@Override
	public void setTrafficEnabled(boolean trafficEnabled) {
		map.setTrafficEnabled(trafficEnabled);
	}

	@Override
	public void showMapText(boolean show) {
		map.showMapText(show);
	}

	@Override
	public void showIndoorMap(boolean show) {
		map.showIndoorMap(show);
	}

	@Override
	public void showBuildings(boolean show) {
		map.showBuildings(show);
	}

	@Override
	public void setMyTrafficStyle(MyTrafficStyle style) {
		map.setMyTrafficStyle(style);
	}

	@Override
	public MyTrafficStyle getMyTrafficStyle() {
		return map.getMyTrafficStyle();
	}

	@Override
	public boolean isMyLocationEnabled() {
		return map.isMyLocationEnabled();
	}

	@Override
	public void setMyLocationEnabled(boolean enable) {
		map.setMyLocationEnabled(enable);
	}

	@Override
	public Location getMyLocation() {
		return map.getMyLocation();
	}

	@Override
	public void setLocationSource(LocationSource source) {
		map.setLocationSource(source);
	}

	@Override
	public void setMyLocationStyle(MyLocationStyle style) {
		map.setMyLocationStyle(style);
	}

	@Override
	public MyLocationStyle getMyLocationStyle() {
		return map.getMyLocationStyle();
	}

	@Override
	public UiSettings getUiSettings() {
		return map.getUiSettings();
	}

	@Override
	public Projection getProjection() {
		return map.getProjection();
	}

	@Override
	public void setInfoWindowAdapter(AMap.InfoWindowAdapter adapter) {
		map.setInfoWindowAdapter(adapter);
	}

	@Override
	public void setCommonInfoWindowAdapter(AMap.CommonInfoWindowAdapter adapter) {
		map.setCommonInfoWindowAdapter(adapter);
	}

	@Override
	public void getMapScreenShot(AMap.OnMapScreenShotListener listener) {
		map.getMapScreenShot(listener);
	}

	@Override
	public float getScalePerPixel() {
		return map.getScalePerPixel();
	}

	@Override
	public void runOnDrawFrame() {
		map.runOnDrawFrame();
	}

	@Override
	public void removeCache() {
		map.removecache();
	}

	@Override
	public void removeCache(AMap.OnCacheRemoveListener listener) {
		map.removecache(listener);
	}

	@Override
	public void setCustomRenderer(CustomRenderer renderer) {
		map.setCustomRenderer(renderer);
	}

	@Override
	public void setPointToCenter(int x, int y) {
		map.setPointToCenter(x, y);
	}

	@Override
	public void setMapTextZIndex(int zIndex) {
		map.setMapTextZIndex(zIndex);
	}

	@Override
	public void setLoadOfflineData(boolean enable) {
		map.setLoadOfflineData(enable);
	}

	@Override
	public int getMapTextZIndex() {
		return map.getMapTextZIndex();
	}

	@Override
	public void reloadMap() {
		map.reloadMap();
	}

	@Override
	public void setRenderFps(int fps) {
		map.setRenderFps(fps);
	}

	@Override
	public void setIndoorBuildingInfo(IndoorBuildingInfo info) {
		map.setIndoorBuildingInfo(info);
	}

	@Override
	public void setAMapGestureListener(AMapGestureListener listener) {
		map.setAMapGestureListener(listener);
	}

	@Override
	public float getZoomToSpanLevel(LatLng var1, LatLng var2) {
		return map.getZoomToSpanLevel(var1, var2);
	}

	@Override
	public Pair<Float, LatLng> calculateZoomToSpanLevel(int var1, int var2, int var3, int var4, LatLng var5, LatLng var6) {
		return map.calculateZoomToSpanLevel(var1, var2, var3, var4, var5, var6);
	}

	@Override
	public InfoWindowAnimationManager getInfoWindowAnimationManager() {
		return map.getInfoWindowAnimationManager();
	}

	@Override
	public void setMaskLayerParams(int var1, int var2, int var3, int var4, int var5, long var6) {
		map.setMaskLayerParams(var1, var2, var3, var4, var5, var6);
	}

	@Override
	public void setMaxZoomLevel(float maxZoomLevel) {
		map.setMaxZoomLevel(maxZoomLevel);
	}

	@Override
	public void setMinZoomLevel(float minZoomLevel) {
		map.setMinZoomLevel(minZoomLevel);
	}

	@Override
	public void resetMinMaxZoomPreference() {
		map.resetMinMaxZoomPreference();
	}

	@Override
	public void setMapStatusLimits(LatLngBounds bounds) {
		map.setMapStatusLimits(bounds);
	}

	@Override
	public CrossOverlay addCrossOverlay(CrossOverlayOptions options) {
		return map.addCrossOverlay(options);
	}

	@Override
	public RouteOverlay addRouteOverlay() {
		return map.addRouteOverlay();
	}

	@Override
	public float[] getViewMatrix() {
		return map.getViewMatrix();
	}

	@Override
	public float[] getProjectionMatrix() {
		return map.getProjectionMatrix();
	}

	@Override
	public void setMapCustomEnable(boolean enable) {
		map.setMapCustomEnable(enable);
	}

	@Override
	public void setCustomMapStylePath(String path) {
		map.setCustomMapStylePath(path);
	}

	@Override
	public void setCustomMapStyleID(String id) {
		map.setCustomMapStyleID(id);
	}

	@Override
	public void setCustomTextureResourcePath(String path) {
		map.setCustomTextureResourcePath(path);
	}

	@Override
	public void setRenderMode(int renderMode) {
		map.setRenderMode(renderMode);
	}

	@Override
	public void getP20MapCenter(IPoint point) {
		map.getP20MapCenter(point);
	}

	@Override
	public String getMapContentApprovalNumber() {
		return map.getMapContentApprovalNumber();
	}

	@Override
	public String getSatelliteImageApprovalNumber() {
		return map.getSatelliteImageApprovalNumber();
	}

	@Override
	public void setMapLanguage(String language) {
		map.setMapLanguage(language);
	}

	@Override
	public void setOnMapLoadedListener(AMap.OnMapLoadedListener listener) {
		map.setOnMapLoadedListener(listener);
	}

	@Override
	public void setOnMapClickListener(AMap.OnMapClickListener listener) {
		map.setOnMapClickListener(listener);
	}

	@Override
	public void setOnMarkerClickListener(AMap.OnMarkerClickListener listener) {
		map.setOnMarkerClickListener(listener);
	}

	@Override
	public void setOnMapTouchListener(AMap.OnMapTouchListener listener) {
		map.setOnMapTouchListener(listener);
	}

	@Override
	public void setOnCameraChangeListener(AMap.OnCameraChangeListener listener) {
		map.setOnCameraChangeListener(listener);
	}

	@Override
	public void setOnMapLongClickListener(AMap.OnMapLongClickListener listener) {
		map.setOnMapLongClickListener(listener);
	}

	@Override
	public void setOnMultiPointClickListener(AMap.OnMultiPointClickListener listener) {
		map.setOnMultiPointClickListener(listener);
	}

	@Override
	public void setOnIndoorBuildingActiveListener(AMap.OnIndoorBuildingActiveListener listener) {
		map.setOnIndoorBuildingActiveListener(listener);
	}

	@Override
	public void setOnPOIClickListener(AMap.OnPOIClickListener listener) {
		map.setOnPOIClickListener(listener);
	}

	@Override
	public void setOnPolylineClickListener(AMap.OnPolylineClickListener listener) {
		map.setOnPolylineClickListener(listener);
	}

	@Override
	public void setOnMarkerDragListener(AMap.OnMarkerDragListener listener) {
		map.setOnMarkerDragListener(listener);
	}

	@Override
	public void setOnInfoWindowClickListener(AMap.OnInfoWindowClickListener listener) {
		map.setOnInfoWindowClickListener(listener);
	}

	@Override
	public void setOnMyLocationChangeListener(AMap.OnMyLocationChangeListener listener) {
		map.setOnMyLocationChangeListener(listener);
	}
}
