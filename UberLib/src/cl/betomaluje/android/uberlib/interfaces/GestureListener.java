package cl.betomaluje.android.uberlib.interfaces;

public interface GestureListener {

	public enum Direction {
		   RIGHT,
		   LEFT
		}
	
	public void onSwipe(Direction direction);
	
}
