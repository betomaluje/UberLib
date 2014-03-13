package cl.betomaluje.android.uberlib;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Listener that detects shake gesture.
 * 
 * @author {@link http://stackoverflow.com/users/590531/peceps peceps}
 * @author modified by Alberto Maluje
 * 
 *         Usage: Add this in your activity: 
 *         private SensorManager mSensorManager;
 *         private ShakeEventListener mSensorListener;
 * 
 *         in onCreate() add: 
 *         mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 *         mSensorListener = new ShakeEventListener();
 * 
 *         mSensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {
 * 
 *         		public void onShake() { 
 *         			//YOUR CODE WHEN IT SHAKES HERE!
 *         			Toast.makeText(MyActivity.this, "Shake!", Toast.LENGTH_SHORT).show();
 *         		}
 *         });
 * 
 *         Finally:
 * 		   protected void onResume() { 
 * 				super.onResume();
 *           	mSensorManager.registerListener(mSensorListener, 
 *           	mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 *           	SensorManager.SENSOR_DELAY_UI);
 *           	//or use SensorManager.SENSOR_DELAY_NORMAL
 *         }
 *           
 * 		   protected void onPause() {
 *           	mSensorManager.unregisterListener(mSensorListener);
 *           	super.onStop();
 *         }
 * 
 */
public class ShakeEventListener implements SensorEventListener {

	/** Minimum movement force to consider. */
	private int MIN_FORCE = 10;

	/**
	 * Minimum times in a shake gesture that the direction of movement needs to
	 * change.
	 */
	private int MIN_DIRECTION_CHANGE = 3;

	/** Maximum pause between movements. */
	private int MAX_PAUSE_BETWEEN_DIRECTION_CHANGE = 200;

	/** Maximum allowed time for shake gesture. */
	private int MAX_TOTAL_DURATION_OF_SHAKE = 400;

	/** Time when the gesture started. */
	private long mFirstDirectionChangeTime = 0;

	/** Time when the last movement started. */
	private long mLastDirectionChangeTime;

	/**
	 * How many movements are considered so far. The number of shakes to be
	 * done.
	 */
	private int mDirectionChangeCount = 0;

	/** The last x position. */
	private float lastX = 0;

	/** The last y position. */
	private float lastY = 0;

	/** The last z position. */
	private float lastZ = 0;

	/** OnShakeListener that is called when shake is detected. */
	private OnShakeListener mShakeListener;

	/**
	 * Interface for shake gesture.
	 */
	public interface OnShakeListener {

		/**
		 * Called when shake gesture is detected.
		 */
		void onShake();

		/**
		 * Called when shake gesture has stopped
		 */
		void onStopShake();
	}

	public void setOnShakeListener(OnShakeListener listener) {
		mShakeListener = listener;
	}

	@Override
	public void onSensorChanged(SensorEvent se) {
		// get sensor data
		float x = se.values[SensorManager.DATA_X];
		float y = se.values[SensorManager.DATA_Y];
		float z = se.values[SensorManager.DATA_Z];

		// calculate movement
		float totalMovement = Math.abs(x + y + z - lastX - lastY - lastZ);

		if (totalMovement > MIN_FORCE) {

			// get time
			long now = System.currentTimeMillis();

			// store first movement time
			if (mFirstDirectionChangeTime == 0) {
				mFirstDirectionChangeTime = now;
				mLastDirectionChangeTime = now;
			}

			// check if the last movement was not long ago
			long lastChangeWasAgo = now - mLastDirectionChangeTime;
			if (lastChangeWasAgo < MAX_PAUSE_BETWEEN_DIRECTION_CHANGE) {

				// store movement data
				mLastDirectionChangeTime = now;
				mDirectionChangeCount++;

				// store last sensor data
				lastX = x;
				lastY = y;
				lastZ = z;

				// check how many movements are so far
				if (mDirectionChangeCount >= MIN_DIRECTION_CHANGE) {

					// check total duration
					long totalDuration = now - mFirstDirectionChangeTime;
					if (totalDuration < MAX_TOTAL_DURATION_OF_SHAKE) {
						mShakeListener.onShake();
						resetShakeParameters();
					}
				}

			} else {
				resetShakeParameters();
			}
		}
	}

	/**
	 * Resets the shake parameters to their default values.
	 */
	private void resetShakeParameters() {
		mFirstDirectionChangeTime = 0;
		mDirectionChangeCount = 0;
		mLastDirectionChangeTime = 0;
		lastX = 0;
		lastY = 0;
		lastZ = 0;
		mShakeListener.onStopShake();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public int getMinForce() {
		return MIN_FORCE;
	}

	/**
	 * Sets the minimum movement force to consider on shake
	 * @param minForce: the minimum force
	 */
	public void setMinForce(int minForce) {
		MIN_FORCE = minForce;
	}

	public int getMinDirectionChange() {
		return MIN_DIRECTION_CHANGE;
	}

	/**
	 * Minimum times in a shake gesture that the direction of movement needs to
	 * change.
	 * @param minDirectionChange
	 */
	public void setMinDirectionChange(int minDirectionChange) {
		MIN_DIRECTION_CHANGE = minDirectionChange;
	}

	public int getMaxPauseBetweenDirectionChange() {
		return MAX_PAUSE_BETWEEN_DIRECTION_CHANGE;
	}

	/** Sets the number of maximum pause between shakes
	 * 
	 * @param pause: time in milliseconds 
	 */
	public void setMaxPauseBetweenDirectionChange(
			int pause) {
		MAX_PAUSE_BETWEEN_DIRECTION_CHANGE = pause;
	}

	public int getMaxTotalDurationOfShake() {
		return MAX_TOTAL_DURATION_OF_SHAKE;
	}

	public void setMaxTotalDurationOfShake(int maxTotalDurationOfShake) {
		MAX_TOTAL_DURATION_OF_SHAKE = maxTotalDurationOfShake;
	}

	public int getmDirectionChangeCount() {
		return mDirectionChangeCount;
	}

	/**
	 * How many movements are considered so far. The number of shakes to be
	 * done.
	 * @param mDirectionChangeCount
	 */
	public void setmDirectionChangeCount(int mDirectionChangeCount) {
		this.mDirectionChangeCount = mDirectionChangeCount;
	}

}
