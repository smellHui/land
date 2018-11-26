package com.hawk.map.core;

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

import java.util.ArrayList;
import java.util.List;

/**
 * 地图功能接口
 * <br/>
 * Created on 2018/11/19 15:44
 *
 * @author WingHawk
 */
public interface IMapFunctions {

	Arc addArc(ArcOptions options);

	CameraPosition getCameraPosition();

	float getMaxZoomLevel();

	float getMinZoomLevel();

	void moveCamera(CameraUpdate cameraUpdate);

	void animateCamera(CameraUpdate cameraUpdate);

	void animateCamera(CameraUpdate cameraUpdate, AMap.CancelableCallback callback);

	void animateCamera(CameraUpdate cameraUpdate, long duration, AMap.CancelableCallback callback);

	void stopAnimation();

	NavigateArrow addNavigateArrow(NavigateArrowOptions options);

	Polyline addPolyline(PolylineOptions options);

	BuildingOverlay addBuildingOverlay();

	Circle addCircle(CircleOptions options);

	Polygon addPolygon(PolygonOptions options);

	GroundOverlay addGroundOverlay(GroundOverlayOptions options);

	Marker addMarker(MarkerOptions options);

	GL3DModel addGL3DModel(GL3DModelOptions options);

	Text addText(TextOptions options);

	ArrayList<Marker> addMarkers(ArrayList<MarkerOptions> optionsList, boolean b);

	List<Marker> getMapScreenMarkers();

	TileOverlay addTileOverlay(TileOverlayOptions options);

	MultiPointOverlay addMultiPointOverlay(MultiPointOverlayOptions options);

	ParticleOverlay addParticleOverlay(ParticleOverlayOptions options);

	void clear();

	void clear(boolean b);

	int getMapType();

	void setMapType(int mapType);

	boolean isTrafficEnabled();

	void setTrafficEnabled(boolean trafficEnabled);

	void showMapText(boolean show);

	void showIndoorMap(boolean show);

	void showBuildings(boolean show);

	void setMyTrafficStyle(MyTrafficStyle style);

	MyTrafficStyle getMyTrafficStyle();

	boolean isMyLocationEnabled();

	void setMyLocationEnabled(boolean enable);

	Location getMyLocation();

	void setLocationSource(LocationSource source);

	void setMyLocationStyle(MyLocationStyle style);

	MyLocationStyle getMyLocationStyle();

	UiSettings getUiSettings();

	Projection getProjection();

	void setInfoWindowAdapter(AMap.InfoWindowAdapter adapter);

	void setCommonInfoWindowAdapter(AMap.CommonInfoWindowAdapter adapter);

	void getMapScreenShot(AMap.OnMapScreenShotListener listener);

	float getScalePerPixel();

	void runOnDrawFrame();

	void removeCache();

	void removeCache(AMap.OnCacheRemoveListener listener);

	void setCustomRenderer(CustomRenderer renderer);

	void setPointToCenter(int x, int y);

	void setMapTextZIndex(int zIndex);

	void setLoadOfflineData(boolean enable);

	int getMapTextZIndex();

	void reloadMap();

	void setRenderFps(int fps);

	void setIndoorBuildingInfo(IndoorBuildingInfo info);

	void setAMapGestureListener(AMapGestureListener listener);

	float getZoomToSpanLevel(LatLng var1, LatLng var2);

	Pair<Float, LatLng> calculateZoomToSpanLevel(int var1, int var2, int var3, int var4, LatLng var5, LatLng var6);

	InfoWindowAnimationManager getInfoWindowAnimationManager();

	void setMaskLayerParams(int var1, int var2, int var3, int var4, int var5, long var6);

	void setMaxZoomLevel(float maxZoomLevel);

	void setMinZoomLevel(float minZoomLevel);

	void resetMinMaxZoomPreference();

	void setMapStatusLimits(LatLngBounds bounds);

	CrossOverlay addCrossOverlay(CrossOverlayOptions options);

	RouteOverlay addRouteOverlay();

	float[] getViewMatrix();

	float[] getProjectionMatrix();

	void setMapCustomEnable(boolean enable);

	void setCustomMapStylePath(String path);

	void setCustomMapStyleID(String id);

	void setCustomTextureResourcePath(String path);

	void setRenderMode(int renderMode);

	void getP20MapCenter(IPoint point);

	String getMapContentApprovalNumber();

	String getSatelliteImageApprovalNumber();

	void setMapLanguage(String language);
}
