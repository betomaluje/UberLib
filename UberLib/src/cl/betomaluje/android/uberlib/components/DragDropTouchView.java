package cl.betomaluje.android.uberlib.components;

import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import cl.betomaluje.android.uberlib.interfaces.DragListener;

public class DragDropTouchView implements OnTouchListener {

	private final static int START_DRAGGING = 0;
	private final static int STOP_DRAGGING = 1;

	private int status = STOP_DRAGGING, screenHeight = 0, screenWidth = 0,
			marginTop = 0, topY, leftX, rightX, bottomY, VIBRATE_DURATION = 35,
			crashX, crashY, x, y, offset_x = 0, offset_y = 0, lastPosition = 0, yOffset = 0, initialPosition = 0;

	private boolean canDrag = true;

	private View selected_item = null;

	private RelativeLayout.LayoutParams lp;

	private Vibrator mVibrator;

	private DragListener dragListener;

	/**
	 * Allows a view to be draggable
	 * 
	 * @param container
	 *            : the container of all the views
	 * @param target
	 *            : the target view to be dragged
	 * @param allowHorizontal
	 *            : true if also horizontal drag and drop is allowed, false
	 *            otherwise
	 */
	public void init(ViewGroup container, final View target,
			final boolean allowHorizontal) {

		final int height = target.getHeight() + yOffset;
		final int width = target.getWidth();

		lp = new RelativeLayout.LayoutParams(new ViewGroup.MarginLayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT));

		container.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (status == START_DRAGGING) {

					crashX = (int) event.getRawX();
					crashY = (int) event.getRawY();

					switch (event.getActionMasked()) {
					case MotionEvent.ACTION_DOWN:

						// we get the initial values of the view, just in case
						// we need to use it afterwards
						topY = target.getTop();
						leftX = target.getLeft();
						rightX = target.getRight();
						bottomY = target.getBottom();

						if (dragListener != null)
							dragListener.onDragStart(crashX, crashY);

						break;
					case MotionEvent.ACTION_MOVE:

						x = crashX - offset_x;
						y = crashY - offset_y;

						if (y >= marginTop) {
							if (x > screenWidth)
								x = screenWidth;
							if (y > screenHeight)
								y = screenHeight;

							lp.height = height;
							lp.width = width;

							if (allowHorizontal) {
								lp.setMargins(x, y, 0, 0);
							} else {

								if (y >= lastPosition - marginTop) {
									lp.setMargins(0, y, 0, -y);
								}
							}

							selected_item.setLayoutParams(lp);
						}

						if (dragListener != null)
							dragListener.isDragging(x, y);

						break;
					case MotionEvent.ACTION_UP:

						x = crashX - offset_x;
						y = crashY - offset_y;

						lastPosition = y;

						status = STOP_DRAGGING;
						
						//lp.height = height;
						//lp.width = width;

						if (y <= lastPosition + marginTop) {
							// restore to initial position
							lp.setMargins(0, initialPosition, 0, 0);
							selected_item.setLayoutParams(lp);
						}

						if (dragListener != null)
							dragListener.onDrop(x, lastPosition);

						break;
					default:
						break;
					}
				}
				return true;
			}
		});

		target.setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub

		if (canDrag) {
			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				status = START_DRAGGING;

				if (mVibrator != null)
					mVibrator.vibrate(VIBRATE_DURATION);

				offset_x = (int) event.getRawX();
				offset_y = (int) event.getRawY();

				selected_item = v;

				break;
			case MotionEvent.ACTION_UP:
				status = STOP_DRAGGING;

				selected_item = null;
				break;
			default:
				break;
			}
		} else {
			status = STOP_DRAGGING;
		}

		return false;
	}

	public void setDragListener(DragListener dl) {
		this.dragListener = dl;
	}

	public void removeDragListener() {
		this.dragListener = null;
	}

	public void canDrag(boolean drag) {
		this.canDrag = drag;
	}

	public void setTopMargin(int margin) {
		this.marginTop = margin;
	}

	public void setScreenSizes(int width, int height) {
		this.screenWidth = width;
		this.screenHeight = height;
	}

	public void setYOffset(int offset) { 
		this.yOffset = offset;
	}
	
	public void setVibrator(Vibrator v) {
		this.mVibrator = v;
	}

	public void setVibrationDuration(int duration) {
		this.VIBRATE_DURATION = duration;
	}

	public void setTopDelimeter(int top) {
		this.lastPosition = top;
	}
	
	public void setInitialPosition (int pos) {
		this.initialPosition = pos;
	}
}
