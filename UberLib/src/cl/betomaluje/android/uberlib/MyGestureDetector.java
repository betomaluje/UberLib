package cl.betomaluje.android.uberlib;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import cl.betomaluje.android.uberlib.interfaces.GestureListener;
import cl.betomaluje.android.uberlib.interfaces.GestureListener.Direction;

public class MyGestureDetector extends SimpleOnGestureListener {

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	private GestureListener listener;

	public MyGestureDetector(GestureListener l) {
		this.listener = l;
	}

	public void setGestureListener(GestureListener l) {
		this.listener = l;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		if (listener == null)
			throw new NullPointerException(
					"You must set a GestureListener object. Please use setGestureListener() method.");

		try {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			// right to left swipe
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				// left swipe
				listener.onSwipe(Direction.LEFT);
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				// right swipe
				listener.onSwipe(Direction.RIGHT);
			}
		} catch (Exception e) {
			// nothing
		}
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return true;
	}
}