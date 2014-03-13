package cl.betomaluje.android.uberlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class CircleProgressBar extends ProgressBar {

	/**
	 * The Translation offset which gives us the ability to use our own
	 * coordinates system.
	 */
	private float mTranslationOffset;

	private float mRadius;

	/** The start angle (12 O'clock) */
	private float startAngle = 270;
	/** The angle of progress */
	private float angle = 0;

	// text
	private boolean show_text = true;
	private Paint textPaint;
	private String text;

	private int mWidth;
	private int mHeight;

	private int textSize = 20;

	/** The progress percent */
	private int maxProgress = 100;
	private int progress = 0;
	private int progressColor, textColor, backgroundColor;

	// for attr.xml color attributes
	private String progressColor_attr, backgroundColor_attr, textColor_attr;

	// background
	private Paint backgroundPaint;
	private float backgroundStrokeWidth = 5;

	// progress
	private Paint progressPaint;
	private float progressStrokeWidth = 8;
	private boolean roundCorners = false;

	/**
	 * The rectangle enclosing the circle.
	 */
	private final RectF mCircleBounds = new RectF();
	private Rect bounds = new Rect();

	private String extras = "";

	public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.CircleProgressBar, defStyle, 0);

		initAttributes(a);

		a.recycle();
		init();
	}

	public CircleProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.CircleProgressBar, 0, 0);

		initAttributes(a);

		a.recycle();
		init();
	}

	public CircleProgressBar(Context context) {
		super(context);
		init();
	}

	private void init() {
		if (progressColor_attr != null) {
			try {
				progressColor = Color.parseColor(progressColor_attr);
			} catch (IllegalArgumentException e) {
				progressColor = Color.WHITE;
			}

		} else {
			progressColor = Color.WHITE;
		}

		if (backgroundColor_attr != null) {
			try {
				backgroundColor = Color.parseColor(backgroundColor_attr);
			} catch (IllegalArgumentException e) {
				backgroundColor = Color.LTGRAY;
			}

		} else {
			backgroundColor = Color.LTGRAY;
		}

		if (textColor_attr != null) {
			try {
				textColor = Color.parseColor(textColor_attr);
			} catch (IllegalArgumentException e) {
				textColor = Color.WHITE;
			}

		} else {
			textColor = Color.WHITE;
		}

		textPaint = new Paint(Paint.LINEAR_TEXT_FLAG);
		textPaint.setColor(textColor);
		textPaint.setStyle(Style.FILL);
		textPaint.setTextAlign(Align.LEFT);
		textPaint.setTextSize(textSize);
		textPaint.setAntiAlias(true);

		backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backgroundPaint.setColor(backgroundColor);
		backgroundPaint.setStyle(Paint.Style.STROKE);
		backgroundPaint.setStrokeWidth(backgroundStrokeWidth);

		progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		progressPaint.setColor(progressColor);
		progressPaint.setStrokeWidth(progressStrokeWidth);
		progressPaint.setAntiAlias(true);
		progressPaint.setStyle(Paint.Style.STROKE);

		if (roundCorners)
			progressPaint.setStrokeCap(Paint.Cap.ROUND);

		invalidate();
		requestLayout();
	}

	private void initAttributes(TypedArray a) {
		mRadius = a.getInteger(R.styleable.CircleProgressBar_radius, 20);

		setProgress(a.getInteger(R.styleable.CircleProgressBar_progress, 0));

		textSize = a.getInteger(R.styleable.CircleProgressBar_center_text_size,
				20);

		extras = a.getString(R.styleable.CircleProgressBar_extra_text);

		if(extras == null)
			extras = "";
			
		text = String.valueOf(progress) + extras;

		show_text = a.getBoolean(
				R.styleable.CircleProgressBar_show_progress_text, true);

		roundCorners = a.getBoolean(R.styleable.CircleProgressBar_round_border,
				false);

		maxProgress = a.getInt(R.styleable.CircleProgressBar_max_progress, 100);

		// colors to use
		progressColor_attr = a
				.getString(R.styleable.CircleProgressBar_progress_color);
		backgroundColor_attr = a
				.getString(R.styleable.CircleProgressBar_background_color);
		textColor_attr = a
				.getString(R.styleable.CircleProgressBar_text_progress_color);

		// stroke widths
		progressStrokeWidth = a.getFloat(
				R.styleable.CircleProgressBar_progress_stroke, 8f);
		backgroundStrokeWidth = a.getFloat(
				R.styleable.CircleProgressBar_background_stroke, 5f);
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

		// draw the background
		canvas.drawArc(mCircleBounds, 0, 360, false, backgroundPaint);
		
		// draw the progress
		canvas.drawArc(mCircleBounds, startAngle, angle, false, progressPaint);

		textPaint.getTextBounds(text, 0, text.length(), bounds);

		if (show_text)
			canvas.drawText(text,
					(mCircleBounds.centerX())
							- (textPaint.measureText(text) / 2),
					mCircleBounds.centerY() + bounds.height() / 2, textPaint);

		canvas.save();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mHeight = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
		mWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		int min = Math.min(mWidth, mHeight);
		setMeasuredDimension(min, min);

		mCircleBounds.set(-mRadius, -mRadius, mRadius, mRadius);

		mTranslationOffset = min * 0.5f;
	}

	/**
	 * Converts the given progress to degrees to display on the circle
	 * 
	 * @param progress
	 */
	private void convertProgressToDegrees(float progress) {
		float floatProgress = progress / maxProgress;
		this.angle = 360 * floatProgress;
	}

	/**
	 * Sets the current progress to the progress bar
	 * 
	 * @param progress
	 *            : the new progress to display
	 */
	public void setProgress(int progress) {
		if (this.progress != progress) {
			this.progress = progress;
			// calculate the degrees from progress
			convertProgressToDegrees(progress);
			text = String.valueOf(progress) + extras;

			invalidate();
			requestLayout();
		}
	}

	public void setMaxProgress(int max) {
		this.maxProgress = max;
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

	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		destroyDrawingCache();
	}

	/**
	 * Sets an extra string to put beside the progress value
	 * 
	 * @param e
	 *            : the extra string to put (ex. %, $, etc)
	 */
	public void setExtraString(String e) {
		this.extras = e;
	}
}
