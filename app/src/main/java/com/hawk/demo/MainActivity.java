package com.hawk.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hawk.map.impl.AmapView;
import com.hawk.map.impl.MapManager;
import com.hawk.map.polygon.PlotUtils;
import com.hawk.map.polygon.callback.OnPlotListener;
import com.hawk.map.polygon.core.IHandle;
import com.hawk.map.polygon.core.IHistoryManager;
import com.hawk.map.polygon.core.IPolygon;
import com.hawk.map.polygon.core.IVertex;
import com.hawk.map.polygon.impl.PolygonPlotter;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	private AmapView mMapView;
	private View mBtnAdd;
	private View mBtnDel;
	private View mBtnRevert;
	private View mBtnNext;
	private View mBtnPre;
	private IPolygon mFocusedPolygon;
	private View mBtnCompress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mMapView = findViewById(R.id.mapView);
		mBtnAdd = findViewById(R.id.btnAdd);
		mBtnDel = findViewById(R.id.btnDel);
		mBtnRevert = findViewById(R.id.btnRevert);
		mBtnPre = findViewById(R.id.btnPre);
		mBtnNext = findViewById(R.id.btnNext);
		mBtnCompress = findViewById(R.id.btnCompress);
		mBtnAdd.setOnClickListener(this);
		mBtnDel.setOnClickListener(this);
		mBtnRevert.setOnClickListener(this);
		mBtnPre.setOnClickListener(this);
		mBtnNext.setOnClickListener(this);
		mBtnCompress.setOnClickListener(this);

		mMapView.onCreate(savedInstanceState);
		final PolygonPlotter polygonPlotter = new PolygonPlotter(this);
		MapManager mapManager = mMapView.getMapManager();
		mapManager.addObserver(polygonPlotter);
		mapManager.addObserver(new SimpleMapMarkerClickObserver());
		polygonPlotter.setOnPlotListener(new OnPlotListener() {
			@Override
			public void onChanged(IPolygon polygon) {
				// 只允许添加一个多边形
				polygonPlotter.setPolygonAddable(!polygon.isClosed());
				changeButtonsEnable(polygon);

			}

			@Override
			public void onChanged(IVertex vertex) {
				changeButtonsEnable(vertex.getParent());
			}

			@Override
			public void onFocusChanged(IPolygon polygon) {
				if (polygon.isFocused()) {
					mFocusedPolygon = polygon;
				}
				changeButtonsEnable(polygon);
			}

			@Override
			public void onFocusChanged(IVertex vertex) {
				changeButtonsEnable(vertex.getParent());
			}

			@Override
			public void onClick(IPolygon polygon) {
				changeButtonsEnable(polygon);
			}
		});
	}

	private void changeButtonsEnable(IPolygon polygon) {
		IVertex vertex = polygon.getFocusedVertex();
		IHandle handle = polygon.getHandle();
		mBtnAdd.setEnabled(handle.isVisible() && vertex != null && vertex.getType() == IVertex.TYPE_MIDDLE);
		mBtnDel.setEnabled(handle.isVisible() && vertex != null && vertex.getType() == IVertex.TYPE_NORMAL);
		mBtnRevert.setEnabled(!polygon.getHistoryManager().isEmpty());
		mBtnPre.setEnabled(handle.isVisible() && vertex != null && vertex.getType() == IVertex.TYPE_NORMAL && (polygon.isClosed()
				|| polygon.getVertices().indexOf(vertex) != 0));
		mBtnNext.setEnabled(handle.isVisible() && vertex != null && vertex.getType() == IVertex.TYPE_NORMAL && (polygon.isClosed()
				|| polygon.getVertices().indexOf(vertex) != polygon.getVertices().size() - 1));
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnAdd:
				IVertex vertex = mFocusedPolygon.getFocusedVertex();
				mFocusedPolygon.switchVertexType(vertex, IVertex.TYPE_NORMAL);
				break;
			case R.id.btnDel:
				vertex = mFocusedPolygon.getFocusedVertex();
				mFocusedPolygon.removeNormalVertex(vertex);
				break;
			case R.id.btnRevert:
				IHistoryManager historyManager = mFocusedPolygon.getHistoryManager();
				historyManager.pop();
				break;
			case R.id.btnPre:
				vertex = mFocusedPolygon.getFocusedVertex();
				List<IVertex> vertices = mFocusedPolygon.getVertices();
				int index = vertices.indexOf(vertex);
				IVertex preVertex = vertices.get((index - 2 + vertices.size()) % vertices.size());
				mFocusedPolygon.switchFocusVertex(preVertex);
				break;
			case R.id.btnNext:
				vertex = mFocusedPolygon.getFocusedVertex();
				vertices = mFocusedPolygon.getVertices();
				index = vertices.indexOf(vertex);
				IVertex nextVertex = vertices.get((index + 2) % vertices.size());
				mFocusedPolygon.switchFocusVertex(nextVertex);
				break;
			case R.id.btnCompress:
				mFocusedPolygon.compress(PlotUtils.dp2px(this, 15));
				break;
		}
	}
}
