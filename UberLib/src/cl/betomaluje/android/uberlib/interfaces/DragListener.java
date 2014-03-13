package cl.betomaluje.android.uberlib.interfaces;

/**
 * Interface to receive notifications when a drag starts or stops
 */
public interface DragListener {

	/**
	 * Called when a DragDropTouchView starts to be dragged
	 * @param initialX:
	 * 					the initial X position of the finger's tap
	 * @param initialY:
	 * 					the initial Y position of the finger's tap
	 */
	void onDragStart(int initialX, int initialY);
	
	/**
	 * Called when a DragDropTouchView is being dragged
	 * @param currentX
	 * 					the current X position of the finger's tap
	 * @param currentY
	 * 					the current Y position of the finger's tap
	 */
	void isDragging(int currentX, int currentY);

	/**
	 * Called when a DragDropTouchView is being dropped
	 * @param finalX
	 * 					the final X position of the view
	 * @param finalY
	 * 					the final Y position of the view
	 */
	void onDrop(int finalX, int finalY);
}
