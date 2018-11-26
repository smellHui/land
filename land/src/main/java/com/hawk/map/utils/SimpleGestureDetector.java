package com.hawk.map.utils;

import android.view.MotionEvent;

@SuppressWarnings("UNUSED")
public class SimpleGestureDetector {
	private static final int CHECK_CLICK_TIME = 400;
	private OnGestureListener onGestureListener;
	private int touchSlop;
	private int mDownPointerId;
	private float mDownX;
	private float mDownY;
	private long mDownTime;
	private boolean mIsMoving;

	public SimpleGestureDetector(OnGestureListener onGestureListener) {
		this.onGestureListener = onGestureListener;
	}

	public void setTouchSlop(int touchSlop) {
		this.touchSlop = touchSlop;
	}

	public void onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				mDownX = event.getX();
				mDownY = event.getY();
				mDownTime = event.getEventTime();
				mDownPointerId = event.getPointerId(event.getActionIndex());
				onGestureListener.onDown(event);
				mIsMoving = false;
				break;
			case MotionEvent.ACTION_MOVE:
				int pointerId = event.getPointerId(event.getActionIndex());
				if (pointerId == mDownPointerId) {
					float moveX = event.getX();
					float moveY = event.getY();
					float dx = moveX - mDownX;
					float dy = moveY - mDownY;
					if (mIsMoving || Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop) {
						mIsMoving = true;
						onGestureListener.onMove(event);
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				onGestureListener.onUp(event);
				if (isClick(event)) {
					onGestureListener.onClick(event);
				}
				mIsMoving = false;
				break;
			case MotionEvent.ACTION_CANCEL:
				onGestureListener.onUp(event);
				mIsMoving = false;
				break;
		}
	}

	private boolean isClick(MotionEvent event) {
		int pointerId = event.getPointerId(event.getActionIndex());
		if (pointerId == mDownPointerId) {
			long elapsedTime = event.getEventTime() - mDownTime;
			// press a short time and not move
			return elapsedTime < CHECK_CLICK_TIME && !mIsMoving;
		}
		return false;
	}

	public interface OnGestureListener {
		void onDown(MotionEvent event);

		void onMove(MotionEvent event);

		void onUp(MotionEvent event);

		void onClick(MotionEvent event);
	}
}
