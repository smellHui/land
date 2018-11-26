package com.hawk.map.polygon.impl;

import com.hawk.map.polygon.Status;
import com.hawk.map.polygon.callback.IPolygonHook;
import com.hawk.map.polygon.core.IHistoryManager;
import com.hawk.map.polygon.core.IPlotterFactory;
import com.hawk.map.polygon.core.IPolygon;
import com.hawk.map.polygon.core.IVertex;
import com.hawk.map.polygon.vo.PlotHistory;

import java.util.List;
import java.util.Stack;

public class HistoryManager implements IHistoryManager<PlotHistory> {
	private Stack<PlotHistory> mHistoryStack = new Stack<>();

	private IPolygon polygon;
	private boolean mHistoryRecovering;

	HistoryManager(IPolygon polygon) {
		this.polygon = polygon;
		polygon.registerHook(new IPolygonHook() {
			private PlotHistory removeHistory;
			private PlotHistory moveHistory;

			@Override
			public void beforeNormalVertexAdd(int index, IVertex vertex) {
				PlotHistory history = new PlotHistory(vertex, index == 0 ? 0 : index + 1, PlotHistory.TYPE_VERTEX_ADD);
				push(history);
			}

			@Override
			public void afterNormalVertexAdd(int index, IVertex vertex) {

			}

			@Override
			public void beforeNormalVertexRemove(IVertex vertex) {
				int index = vertex.getParent().getVertices().indexOf(vertex);
				removeHistory = new PlotHistory(vertex, index == 0 ? 0 : index - 1, PlotHistory.TYPE_VERTEX_REMOVE);
			}

			@Override
			public void afterNormalVertexRemove(IVertex vertex) {
				if (removeHistory != null) {
					push(removeHistory);
					removeHistory = null;
				}
			}

			@Override
			public void beforeVertexMove(IVertex vertex) {
				int index = vertex.getParent().getVertices().indexOf(vertex);
				moveHistory = new PlotHistory(vertex, index, PlotHistory.TYPE_VERTEX_MOVE);
			}

			@Override
			public void afterVertexMove(IVertex vertex) {
				if (moveHistory != null) {
					push(moveHistory);
					moveHistory = null;
				}
			}

			@Override
			public void beforeVertexTypeSwitch(IVertex vertex) {
				if ((moveHistory == null || moveHistory.target != vertex) &&
						(removeHistory == null || removeHistory.target != vertex)) {
					int index = vertex.getParent().getVertices().indexOf(vertex);
					PlotHistory history = new PlotHistory(vertex, index, PlotHistory.TYPE_VERTEX_TYPE_SWITCH);
					push(history);
				}
			}

			@Override
			public void afterVertexTypeSwitch(IVertex vertex) {

			}

			@Override
			public void beforePolygonClose(Polygone polygon) {
				PlotHistory history = new PlotHistory(polygon.getVertices().get(0), -1, PlotHistory.TYPE_POLYGON_CLOSE);
				push(history);
			}

			@Override
			public void afterPolygonClose(Polygone polygon) {
			}

			@Override
			public void beforePolygonSimplify(Polygone polygon) {
				PlotHistory history = new PlotHistory(polygon.getVertices().get(0), -1, PlotHistory.TYPE_POLYGON_SIMPLIFY);
				push(history);
			}

			@Override
			public void afterPolygonSimplify(Polygone polygone) {
			}
		});
	}

	@Override
	public void push(PlotHistory history) {
		if (mHistoryRecovering) {
			return;
		}
		mHistoryStack.push(history);
	}

	@Override
	public void pop() {
		mHistoryRecovering = true;
		if (!isEmpty()) {
			PlotHistory history = mHistoryStack.pop();
			List<IVertex> vertices = polygon.getVertices();
			IVertex focusVertex = history.focus;
			IVertex vertex = history.target;
			switch (history.changeType) {
				case PlotHistory.TYPE_VERTEX_ADD:
					if (!vertices.contains(history.target)) {
						vertex = vertices.get(history.recoverIndex);
					}
					polygon.removeNormalVertex(vertex);
					break;
				case PlotHistory.TYPE_VERTEX_REMOVE:
					boolean originPolygonClosed = (history.originPolygonStatus & Status.POLYGON_CLOSED) != 0;
					if (!vertices.contains(history.target)) {
						IPlotterFactory plotterFactory = polygon.getParent().getPlotterFactory();
						vertex = plotterFactory.createVertex(IVertex.TYPE_NORMAL, polygon);
						vertex.setPosition(history.originPos);
						polygon.addNormalVertex(history.recoverIndex, vertex);
						if (focusVertex == history.target) {
							focusVertex = vertex;
						}
					} else {
						vertex.setPosition(history.originPos);
						polygon.switchVertexType(vertex, IVertex.TYPE_NORMAL);
					}
					if (originPolygonClosed != polygon.isClosed()) {
						polygon.switchClose(true);
					}
					break;
				case PlotHistory.TYPE_VERTEX_MOVE:
					if (history.originVertexType == IVertex.TYPE_NORMAL) {
						if (!vertices.contains(vertex)) {
							vertex = vertices.get(history.recoverIndex);
						}
						polygon.moveNormalVertex(vertex, history.originPos);
					} else if (history.originVertexType == IVertex.TYPE_MIDDLE) {
						if (!vertices.contains(vertex)) {
							vertex = vertices.get(history.recoverIndex == 0 ? 0 : history.recoverIndex + 1);
						}
						polygon.moveNormalVertex(vertex, history.originPos);
						polygon.switchVertexType(vertex, IVertex.TYPE_MIDDLE);
					}
					break;
				case PlotHistory.TYPE_VERTEX_TYPE_SWITCH:
					polygon.switchVertexType(history.target, history.originVertexType);
					break;
				case PlotHistory.TYPE_POLYGON_CLOSE:
					polygon.switchClose(false);
					break;
				case PlotHistory.TYPE_POLYGON_SIMPLIFY:
					// TODO: 2018/11/26 回退
					break;
			}
			focusVertex = vertices.contains(focusVertex) ? focusVertex : null;
			polygon.switchFocusVertex(focusVertex);
		}
		mHistoryRecovering = false;
	}

	@Override
	public boolean isEmpty() {
		return mHistoryStack.isEmpty();
	}

	@Override
	public void clear() {
		mHistoryStack.clear();
	}

	@Override
	public void destroy() {
		clear();
	}
}
