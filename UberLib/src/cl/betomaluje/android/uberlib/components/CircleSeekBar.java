package cl.betomaluje.android.uberlib.components;

import cl.betomaluje.android.uberlib.R;
import cl.betomaluje.android.uberlib.R.styleable;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Displays a holo-themed color picker.
 * 
 * <p>
 * Use {@link #getColor()} to retrieve the selected color.
 * </p>
 */
public class CircleSeekBar extends View {
	/*
	 * Constants used to save/restore the instance state.
	 */
	private static final String STATE_PARENT = "parent";
	private static final String STATE_ANGLE = "angle";

	private OnCircleSeekBarChangeListener mOnCircleSeekBarChangeListener;

	/**
	 * {@code Paint} instance used to draw the color wheel.
	 */
	private Paint mColorWheelPaint;

	/**
	 * {@code Paint} instance used to draw the pointer's "halo".
	 */
	private Paint mPointerHaloPaint;

	/**
	 * {@code Paint} instance used to draw the pointer (the selected color).
	 */
	private Paint mPointerColor;

	/**
	 * The stroke width used to paint the color wheel (in pixels).
	 */
	private int mColorWheelStrokeWidth;

	/**
	 * The radius of the pointer (in pixels).
	 */
	private int mPointerRadius;

	/**
	 * The rectangle enclosing the color wheel.
	 */
	private RectF mColorWheelRectangle = new RectF();

	/**
	 * {@code true} if the user clicked on the pointer to start the move mode.
	 * {@code false} once the user stops touching the screen.
	 * 
	 * @see #onTouchEvent(MotionEvent)
	 */
	private boolean mUserIsMovingPointer = false;

	/**
	 * Number of pixels the origin of this view is moved in X- and Y-direction.
	 * 
	 * <p>
	 * We use the center of this (quadratic) View as origin of our internal
	 * coordinate system. Android uses the upper left corner as origin for the
	 * View-specific coordinate system. So this is the value we use to translate
	 * from one coordinate system to the other.
	 * </p>
	 * 
	 * <p>
	 * Note: (Re)calculated in {@link #onMeasure(int, int)}.
	 * </p>
	 * 
	 * @see #onDraw(Canvas)
	 */
	private float mTranslationOffset;

	/**
	 * Radius of the color wheel in pixels.
	 * 
	 * <p>
	 * Note: (Re)calculated in {@link #onMeasure(int, int)}.
	 * </p>
	 */
	private float mColorWheelRadius;

	private int INTERVAL = 0, newProgressWithInterval = 0;

	private boolean CALLED_FROM_ANGLE = false;

	/**
	 * The pointer's position expressed as angle (in rad).
	 */
	private float mAngle;
	private Paint textPaint;
	private String text;
	private int conversion = 0;
	private int max = 0;
	private int progress = 0;
	private SweepGradient s;
	private Paint mArcColor;
	private String wheel_color_attr, wheel_unactive_color_attr,
			pointer_color_attr, pointer_halo_color_attr, text_color_attr;
	private int wheel_color, unactive_wheel_color, pointer_color,
			pointer_halo_color, text_size, text_color, init_position;
	private boolean block_end = false;
	private float lastX;
	private int last_radians = 0;
	private boolean block_start = false;

	private int arc_finish_radians = 360;
	private int start_angle = 270;

	private float[] pointerPosition;
	private Paint mColorCenterHalo;
	private RectF mColorCenterHaloRectangle = new RectF();
	private Paint mCircleTextColor;
	private int end_angle;

	private boolean show_text = true;

	public CircleSeekBar(Context context) {
		super(context);
		init();
	}

	public CircleSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.CircleSeekBar, 0, 0);

		initAttributes(a);

		a.recycle();
		init();
	}

	public CircleSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.CircleSeekBar, defStyle, 0);

		initAttributes(a);

		a.recycle();
		init();
	}

	private void init() {

		last_radians = end_angle;

		if (init_position < start_angle)
			init_position = calculateTextFromStartAngle(start_angle);

		if (wheel_color_attr != null) {
			try {
				wheel_color = Color.parseColor(wheel_color_attr);
			} catch (IllegalArgumentException e) {
				wheel_color = Color.DKGRAY;
			}

		} else {
			wheel_color = Color.DKGRAY;
		}
		if (wheel_unactive_color_attr != null) {
			try {
				unactive_wheel_color = Color
						.parseColor(wheel_unactive_color_attr);
			} catch (IllegalArgumentException e) {
				unactive_wheel_color = Color.CYAN;
			}

		} else {
			unactive_wheel_color = Color.CYAN;
		}

		if (pointer_color_attr != null) {
			try {
				pointer_color = Color.parseColor(pointer_color_attr);
			} catch (IllegalArgumentException e) {
				pointer_color = Color.CYAN;
			}

		} else {
			pointer_color = Color.CYAN;
		}

		if (pointer_halo_color_attr != null) {
			try {
				pointer_halo_color = Color.parseColor(pointer_halo_color_attr);
			} catch (IllegalArgumentException e) {
				pointer_halo_color = Color.CYAN;
			}

		} else {
			pointer_halo_color = Color.DKGRAY;
		}

		if (text_color_attr != null) {
			try {
				text_color = Color.parseColor(text_color_attr);
			} catch (IllegalArgumentException e) {
				text_color = Color.CYAN;
			}
		} else {
			text_color = Color.CYAN;
		}

		mColorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		// puts a little gradient to the wheel
		mColorWheelPaint.setShader(s);
		mColorWheelPaint.setColor(unactive_wheel_color);
		mColorWheelPaint.setStyle(Paint.Style.STROKE);
		mColorWheelPaint.setStrokeWidth(mColorWheelStrokeWidth);
		mColorWheelPaint.setAntiAlias(true);

		mColorCenterHalo = new Paint(Paint.ANTI_ALIAS_FLAG);
		mColorCenterHalo.setColor(Color.CYAN);
		mColorCenterHalo.setAlpha(0xCC);
		mColorCenterHalo.setAntiAlias(true);

		mPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointerHaloPaint.setColor(pointer_halo_color);
		mPointerHaloPaint.setStrokeWidth(mPointerRadius + 10);
		mPointerHaloPaint.setAntiAlias(true);
		mPointerHaloPaint.setAlpha(150);

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
		textPaint.setColor(text_color);
		textPaint.setStyle(Style.FILL);
		textPaint.setTextAlign(Align.LEFT);
		textPaint.setTextSize(text_size);
		textPaint.setAntiAlias(true);

		mPointerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointerColor.setStrokeWidth(mPointerRadius);
		mPointerColor.setAntiAlias(true);
		mPointerColor.setColor(pointer_color);

		mArcColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mArcColor.setColor(wheel_color);
		mArcColor.setStyle(Paint.Style.STROKE);
		mArcColor.setStrokeWidth(mColorWheelStrokeWidth);
		mArcColor.setAntiAlias(true);

		mCircleTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCircleTextColor.setColor(Color.WHITE);
		mCircleTextColor.setStyle(Paint.Style.FILL);
		mCircleTextColor.setAntiAlias(true);

		if (mOnCircleSeekBarChangeListener != null) {

			int progress = Integer.parseInt(text);

			mOnCircleSeekBarChangeListener.onProgressChanged(this, progress,
					true);

			if (INTERVAL != 0) {
				progress = progress / INTERVAL;
				progress = progress * INTERVAL;
			}

			newProgressWithInterval = progress;

			mOnCircleSeekBarChangeListener.onIntervalProgressChanged(this,
					newProgressWithInterval, true);
		}

		arc_finish_radians = (int) calculateAngleFromText(init_position) - 90;

		if (arc_finish_radians > end_angle)
			arc_finish_radians = end_angle;
		mAngle = calculateAngleFromRadians(arc_finish_radians > end_angle ? end_angle
				: arc_finish_radians);

		updatePointer(mAngle,
				String.valueOf(calculateTextFromAngle(arc_finish_radians)));

		invalidate();
		requestLayout();
	}

	private void initAttributes(TypedArray a) {
		mColorWheelStrokeWidth = a.getInteger(
				R.styleable.CircleSeekBar_wheel_size, 16);
		mPointerRadius = a.getInteger(
				R.styleable.CircleSeekBar_pointer_size, 48);
		max = a.getInteger(R.styleable.CircleSeekBar_max, 100);

		wheel_color_attr = a
				.getString(R.styleable.CircleSeekBar_wheel_active_color);
		wheel_unactive_color_attr = a
				.getString(R.styleable.CircleSeekBar_wheel_unactive_color);
		pointer_color_attr = a
				.getString(R.styleable.CircleSeekBar_pointer_color);
		pointer_halo_color_attr = a
				.getString(R.styleable.CircleSeekBar_pointer_halo_color);

		text_color_attr = a.getString(R.styleable.CircleSeekBar_text_color);

		text_size = a.getInteger(R.styleable.CircleSeekBar_text_size, 95);

		init_position = a.getInteger(
				R.styleable.CircleSeekBar_init_position, 0);

		start_angle = a
				.getInteger(R.styleable.CircleSeekBar_start_angle, 0);
		end_angle = a.getInteger(R.styleable.CircleSeekBar_end_angle, 360);

		show_text = a.getBoolean(R.styleable.CircleSeekBar_show_text, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// All of our positions are using our internal coordinate system.
		// Instead of translating them we let Canvas do the work for us.

		canvas.translate(mTranslationOffset, mTranslationOffset);

		// Draw the color wheel.
		// canvas.drawOval(mColorWheelRectangle, mColorWheelPaint);
		canvas.drawArc(mColorWheelRectangle, start_angle + 270, end_angle
				- (start_angle), false, mColorWheelPaint);

		canvas.drawArc(mColorWheelRectangle, start_angle + 270,
				(arc_finish_radians) > (end_angle) ? end_angle - (start_angle)
						: arc_finish_radians - start_angle, false, mArcColor);

		// Draw the pointer's "halo"
		canvas.drawCircle(pointerPosition[0], pointerPosition[1],
				mPointerRadius, mPointerHaloPaint);

		// Draw the pointer (the currently selected color) slightly smaller on
		// top.
		canvas.drawCircle(pointerPosition[0], pointerPosition[1],
				(float) (mPointerRadius / 1.2), mPointerColor);

		Rect bounds = new Rect();

		if (INTERVAL == 0)
			text = String.valueOf(newProgressWithInterval);

		textPaint.getTextBounds(text, 0, text.length(), bounds);

		if (show_text)
			canvas.drawText(
					text,
					(mColorWheelRectangle.centerX())
							- (textPaint.measureText(text) / 2),
					mColorWheelRectangle.centerY() + bounds.height() / 2,
					textPaint);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = getDefaultSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		int min = Math.min(width, height);
		setMeasuredDimension(min, min);

		mTranslationOffset = min * 0.5f;
		mColorWheelRadius = mTranslationOffset - mPointerRadius;

		mColorWheelRectangle.set(-mColorWheelRadius, -mColorWheelRadius,
				mColorWheelRadius, mColorWheelRadius);

		mColorCenterHaloRectangle.set(-mColorWheelRadius / 2,
				-mColorWheelRadius / 2, mColorWheelRadius / 2,
				mColorWheelRadius / 2);

		pointerPosition = calculatePointerPosition(mAngle);
	}

	/**
	 * Change the colors of the component
	 * 
	 * @param highlight
	 *            the color of the highlighted parts (ex. Color.GREEN)
	 * @param other
	 *            the color of the other parts (ex. Color.DKGRAY)
	 */
	public void changeCombinationOfColors(int highlight, int other) {
		mColorWheelPaint.setColor(other);
		mColorCenterHalo.setColor(other);
		mPointerColor.setColor(other);

		textPaint.setColor(highlight);
		mArcColor.setColor(highlight);
		mPointerHaloPaint.setColor(highlight);

		mCircleTextColor.setColor(Color.WHITE);

		// invalidate();
		// requestLayout();
	}

	/**
	 * Change the progress color
	 * 
	 * @param color
	 *            the color you want the progress to be
	 * 
	 */
	public void changeProgressColor(int color) {
		mArcColor.setColor(color);
		// invalidate();
		// requestLayout();
	}

	/**
	 * Change the background color
	 * 
	 * @param color
	 *            the color you want the background to be
	 * 
	 */
	public void setBackGroundColor(int color) {
		mColorWheelPaint.setColor(color);
		// invalidate();
		// requestLayout();
	}

	/**
	 * Change the text color
	 * 
	 * @param color
	 *            the color you want the text to be
	 * 
	 */
	public void setTextColor(int color) {
		textPaint.setColor(color);
	}

	/**
	 * Change the pointers size
	 * 
	 * @param radius
	 *            radius of the pointer
	 * 
	 */
	public void setPointerRadius(int radius) {
		mPointerColor.setStrokeWidth(radius);
		mPointerHaloPaint.setStrokeWidth(radius + 10);
		// invalidate();
		// requestLayout();
	}

	/**
	 * Changes the view width and height
	 * 
	 * @param width
	 *            the width of the view (ex. LayoutParams.FILL_PARENT)
	 * @param height
	 *            the height of the view (ex. LayoutParams.WARP_CONTENT)
	 */
	public void changeSize(int width, int height) {
		this.setLayoutParams(new LinearLayout.LayoutParams(width, height));
	}

	/**
	 * Change the backgrounds circle's stroke width
	 * 
	 * @param stroke_width
	 *            the width of the circle
	 */
	public void setBackgroundCircleStrokeWidth(int stroke_width) {
		mColorWheelPaint.setStrokeWidth(stroke_width);
		// invalidate();
		// requestLayout();
	}

	/**
	 * Change the foreground circle's stroke width
	 * 
	 * @param stroke_width
	 *            the width of the circle
	 * 
	 */
	public void setForegroundCircleStrokeWidth(int stroke_width) {
		mArcColor.setStrokeWidth(stroke_width);
		// invalidate();
		// requestLayout();
	}

	/**
	 * Change the pointer's border color
	 * 
	 * @param color
	 *            the color you want the pointer border to be
	 * 
	 */
	public void setPointerBorderColor(int color) {
		mPointerHaloPaint.setColor(color);
		// invalidate();
		// requestLayout();
	}

	/**
	 * Change the pointer color
	 * 
	 * @param color
	 *            the color you want the pointer to be
	 * 
	 */
	public void setPointerColor(int color) {
		mPointerColor.setColor(color);
		// invalidate();
		// requestLayout();
	}

	/**
	 * Change the pointer's center color
	 * 
	 * @param color
	 *            the color you want the pointer's center to be
	 * 
	 */
	public void setPointerCenterColor(int color) {
		mColorCenterHalo.setColor(color);
		// invalidate();
		// requestLayout();
	}

	/**
	 * Moves the pointer to an initial position
	 * 
	 * @param position
	 *            position inside the range (min. - máx.) to move the pointer
	 * 
	 */
	public void initPointerFromPosition(int position) {
		if (position == 0 || position >= max)
			return;
		arc_finish_radians = (int) calculateAngleFromText(position) - 90;

		if (arc_finish_radians > end_angle)
			arc_finish_radians = end_angle;
		mAngle = calculateAngleFromRadians(arc_finish_radians > end_angle ? end_angle
				: arc_finish_radians);

		updatePointer(mAngle,
				String.valueOf(calculateTextFromAngle(arc_finish_radians)));

		invalidate();
		requestLayout();
	}

	/**
	 * Gets the max progress.
	 * 
	 * @return the max progress
	 */
	public int getMaxProgress() {
		return max;
	}

	/**
	 * Sets the max progress.
	 * 
	 * @param maxProgress
	 *            the new max progress
	 */
	public void setMaxProgress(int maxProgress) {
		this.max = maxProgress;
	}

	/**
	 * Gets the progress.
	 * 
	 * @return the progress
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * Sets the progress.
	 * 
	 * @param progress
	 *            the new progress
	 */
	public void setProgress(int progress) {
		if (this.progress != progress) {
			this.progress = progress;
			if (!CALLED_FROM_ANGLE) {
				int newPercent = (this.progress / this.max) * 100;
				int newAngle = (newPercent / 100) * 360;
				this.setAngle(newAngle);
			}

			CALLED_FROM_ANGLE = false;

			if (mOnCircleSeekBarChangeListener != null) {

				int newProgress = this.getProgress();

				mOnCircleSeekBarChangeListener.onProgressChanged(this,
						newProgress, true);

				newProgress = newProgress / INTERVAL;
				newProgress = newProgress * INTERVAL;

				newProgressWithInterval = newProgress;

				mOnCircleSeekBarChangeListener.onIntervalProgressChanged(this,
						newProgressWithInterval, true);
			}
		}
	}

	/**
	 * Gets the progress with the interval.
	 * 
	 * @return the progress with the given interval
	 */
	public int getProgressWithInterval() {
		return newProgressWithInterval;
	}

	/**
	 * Gets the text size
	 * 
	 * @return the text size
	 */
	public int getTextSize() {
		return text_size;
	}

	/**
	 * Sets the text size.
	 * 
	 * @param size
	 *            the new text size
	 */
	public void setTextSize(int size) {
		this.text_size = size;
	}

	/**
	 * Gets the initial position.
	 * 
	 * @return the initial position
	 */
	public int getInitPosition() {
		return init_position;
	}

	/**
	 * Sets the initial position of the pointer.
	 * 
	 * @param init_position
	 *            the initial position for the pointer (must be inside the range
	 *            (min. - max.)
	 */
	public void setInitPosition(int init_position) {
		this.init_position = init_position;
	}

	/**
	 * Gets the starting angle of the pointer
	 * 
	 * @return the starting angle
	 */
	public int getStartAngle() {
		return start_angle;
	}

	/**
	 * Sets the starting angle.
	 * 
	 * @param start_angle
	 *            the starting angle for the pointer
	 */
	public void setStartAngle(int start_angle) {
		this.start_angle = start_angle;
	}

	/**
	 * Gets the ending angle of the pointer
	 * 
	 * @return the ending angle
	 */
	public int getEndAngle() {
		return end_angle;
	}

	/**
	 * Sets the ending angle.
	 * 
	 * @param end_angle
	 *            the ending angle for the pointer
	 */
	public void setEndAngle(int end_angle) {
		this.end_angle = end_angle;
	}

	/**
	 * Gets if the centered text is shown
	 * 
	 * @return a boolean indicating if the text is shown
	 */
	public boolean isShowText() {
		return show_text;
	}

	/**
	 * Sets if the centered text is shown
	 * 
	 * @param show_text
	 *            a boolean indicating if the text is shown
	 */
	public void setShowText(boolean show_text) {
		this.show_text = show_text;
	}

	/**
	 * Get the angle.
	 * 
	 * @return the angle
	 */
	public float getAngle() {
		return this.mAngle;
	}

	/**
	 * Set the angle.
	 * 
	 * @param angle
	 *            the new angle
	 */
	public void setAngle(float angle) {
		this.mAngle = angle;
		float donePercent = (((float) this.mAngle) / 360) * 100;
		float progress = (donePercent / 100) * getMaxProgress();
		CALLED_FROM_ANGLE = true;
		setProgress(Math.round(progress));
	}

	/**
	 * Calculates the text from a given angle
	 * 
	 * @param angle
	 *            the angle of the pointer
	 */
	private int calculateTextFromAngle(float angle) {
		float m = angle - start_angle;

		float f = (float) ((end_angle - start_angle) / m);

		return (int) (max / f);
	}

	public int calculateTextFromStartAngle(float angle) {
		float m = angle;

		float f = (float) ((end_angle - start_angle) / m);

		return (int) (max / f);
	}

	public double calculateAngleFromText(int position) {
		if (position == 0 || position >= max)
			return (float) 90;

		double f = (double) max / (double) position;

		double f_r = 360 / f;

		double ang = f_r + 90;

		return ang;

	}

	private int calculateRadiansFromAngle(float angle) {
		float unit = (float) (angle / (2 * Math.PI));
		if (unit < 0) {
			unit += 1;
		}
		int radians = (int) ((unit * 360) - ((360 / 4) * 3));
		if (radians < 0)
			radians += 360;
		return radians;
	}

	private float calculateAngleFromRadians(int radians) {
		return (float) (((radians + 270) * (2 * Math.PI)) / 360);
	}

	/**
	 * Get the selected value
	 * 
	 * @return the value between 0 and max
	 */
	public int getValue() {
		return conversion;
	}

	/**
	 * Sets the interval for the progress
	 * 
	 * @param interval
	 *            integer for the interval (ex. 10 by 10 would be... 10)
	 */
	public void setInterval(int interval) {
		this.INTERVAL = interval;
	}

	/**
	 * Gets the interval for the progress
	 * 
	 * @return the interval used
	 */
	public int getInterval() {
		return this.INTERVAL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Convert coordinates to our internal coordinate system
		float x = event.getX() - mTranslationOffset;
		float y = event.getY() - mTranslationOffset;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// Check whether the user pressed on (or near) the pointer
			mAngle = (float) java.lang.Math.atan2(y, x);

			block_end = false;
			block_start = false;
			mUserIsMovingPointer = true;

			arc_finish_radians = calculateRadiansFromAngle(mAngle);

			if (arc_finish_radians > end_angle) {
				arc_finish_radians = end_angle;
				block_end = true;
			}

			if (!block_end && !block_start) {

				updatePointer(
						mAngle,
						String.valueOf(calculateTextFromAngle(arc_finish_radians)));
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mUserIsMovingPointer) {
				mAngle = (float) java.lang.Math.atan2(y, x);

				int radians = calculateRadiansFromAngle(mAngle);

				if (last_radians > radians && radians < (360 / 6) && x > lastX
						&& last_radians > (360 / 6)) {

					if (!block_end && !block_start)
						block_end = true;

				} else if (last_radians >= start_angle
						&& last_radians <= (360 / 4) && radians <= (360 - 1)
						&& radians >= ((360 / 4) * 3) && x < lastX) {
					if (!block_start && !block_end)
						block_start = true;

				} else if (radians >= end_angle && !block_start
						&& last_radians < radians) {
					block_end = true;
				} else if (radians < end_angle && block_end
						&& last_radians > end_angle) {
					block_end = false;
				} else if (radians < start_angle && last_radians > radians
						&& !block_end) {
					block_start = true;
				} else if (block_start && last_radians < radians
						&& radians > start_angle && radians < end_angle) {
					block_start = false;
				}

				if (block_end) {

					arc_finish_radians = end_angle - 1;

					mAngle = calculateAngleFromRadians(arc_finish_radians);

					updatePointer(mAngle, String.valueOf(max));

				} else if (block_start) {

					arc_finish_radians = start_angle;
					mAngle = calculateAngleFromRadians(arc_finish_radians);

					updatePointer(
							mAngle,
							String.valueOf(calculateTextFromAngle(arc_finish_radians)));
				} else {
					arc_finish_radians = calculateRadiansFromAngle(mAngle);

					updatePointer(
							mAngle,
							String.valueOf(calculateTextFromAngle(arc_finish_radians)));
				}

				if (mOnCircleSeekBarChangeListener != null) {

					int progress = Integer.parseInt(text);

					mOnCircleSeekBarChangeListener.onProgressChanged(this,
							progress, true);

					if(INTERVAL != 0){
						progress = progress / INTERVAL;
						progress = progress * INTERVAL;
					}

					newProgressWithInterval = progress;

					mOnCircleSeekBarChangeListener.onIntervalProgressChanged(
							this, newProgressWithInterval, true);
				}

				last_radians = radians;

			}
			break;
		case MotionEvent.ACTION_UP:
			mUserIsMovingPointer = false;
			break;
		}
		// Fix scrolling
		if (event.getAction() == MotionEvent.ACTION_MOVE && getParent() != null) {
			getParent().requestDisallowInterceptTouchEvent(true);
		}
		lastX = x;

		return true;
	}

	/**
	 * Updates the pointer's position from a angle
	 * 
	 * @param angle
	 *            the angle of the pointer
	 * @param texto
	 *            the text to display (using internal calculateTextFromAngle
	 *            method)
	 * @see calculateTextFromAngle
	 */
	private void updatePointer(float angle, String texto) {
		setAngle(angle);

		if (INTERVAL != 0) {
			int progress = Integer.parseInt(texto);
			progress = progress / INTERVAL;
			progress = progress * INTERVAL;

			newProgressWithInterval = progress;
			text = String.valueOf(newProgressWithInterval);
		} else {
			text = texto;
		}

		pointerPosition = calculatePointerPosition(this.mAngle);
		invalidate();
		requestLayout();
	}

	/**
	 * Calculate the pointer's coordinates on the color wheel using the supplied
	 * angle.
	 * 
	 * @param angle
	 *            The position of the pointer expressed as angle (in rad).
	 * 
	 * @return The coordinates of the pointer's center in our internal
	 *         coordinate system.
	 */
	private float[] calculatePointerPosition(float angle) {
		// if (calculateRadiansFromAngle(angle) > end_angle)
		// angle = calculateAngleFromRadians(end_angle);
		float x = (float) (mColorWheelRadius * Math.cos(angle));
		float y = (float) (mColorWheelRadius * Math.sin(angle));

		return new float[] { x, y };
	}

	@Override
	protected Parcelable onSaveInstanceState() {

		Bundle bundle = new Bundle();
		bundle.putParcelable(STATE_PARENT, super.onSaveInstanceState());
		bundle.putFloat(STATE_ANGLE, mAngle);

		return bundle;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {

		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			setAngle(bundle.getFloat(STATE_ANGLE));
			super.onRestoreInstanceState(bundle.getParcelable(STATE_PARENT));

			mAngle = bundle.getFloat(STATE_ANGLE);

			arc_finish_radians = calculateRadiansFromAngle(mAngle);
			updatePointer(mAngle,
					String.valueOf(calculateTextFromAngle(arc_finish_radians)));
			return;
		}

		super.onRestoreInstanceState(state);
	}

	public void setOnSeekBarChangeListener(OnCircleSeekBarChangeListener l) {
		mOnCircleSeekBarChangeListener = l;
	}

	/**
	 * The listener interface for receiving onSeekChange events. The class that
	 * is interested in processing a onSeekChange event implements this
	 * interface, and the object created with that class is registered with a
	 * component using the component's
	 * <code>setOnSeekBarChangeListener(OnCircleSeekBarChangeListener)<code> method. When
	 * the onSeekChange event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see OnSeekChangeEvent
	 */
	public interface OnCircleSeekBarChangeListener {

		public void onProgressChanged(CircleSeekBar seekBar, int progress,
				boolean fromUser);

		public void onIntervalProgressChanged(CircleSeekBar seekBar,
				int progressWithInterval, boolean fromUser);

	}

	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		destroyDrawingCache();
	}

}