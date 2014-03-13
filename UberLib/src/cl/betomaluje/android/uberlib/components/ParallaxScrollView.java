package cl.betomaluje.android.uberlib.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import com.nineoldandroids.view.ViewHelper;

/**
 * @author Alberto Maluje
 */
public class ParallaxScrollView extends ScrollView {

	private boolean mIsOverScrollEnabled = true;

	private int topViewHeight = 0;
	private float offsetFactor = 0.5f;
	private View topView;

	public interface OnScrollChangedListener {
		/**
		 * 
		 * @param who
		 * @param currHorizontal
		 * @param currVertical
		 * @param prevHorizontal
		 * @param prevVertical
		 * @param ratio
		 *            : the ratio given between the current vertical position
		 *            and the top view height
		 */
		void onScrollChanged(ScrollView who, int currHorizontal,
				int currVertical, int prevHorizontal, int prevVertical,
				float ratio);
	}

	private OnScrollChangedListener mOnScrollChangedListener;

	public ParallaxScrollView(Context context) {
		super(context);
	}

	public ParallaxScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ParallaxScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Sets the top view
	 * 
	 * @param v
	 *            : the view
	 */
	public void setTopView(View v) {
		this.topView = v;
		this.topViewHeight = v.getHeight();
	}

	/**
	 * Sets the offset factor to multiply the scroll of the bottom view. the
	 * higher the value, the slowest the scroll of the top view.
	 * 
	 * @param f
	 *            : the offset (0 - 1.0)
	 */
	public void setOffsetFactor(float f) {
		if (f < 0 || f > 1.0) {
			return;
		}

		this.offsetFactor = f;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);

		float ratio = (float) Math.min(Math.max(t, 0), topViewHeight)
				/ topViewHeight;

		if (mOnScrollChangedListener != null)
			mOnScrollChangedListener.onScrollChanged(this, l, t, oldl, oldt,
					ratio);

		ViewHelper.setY(topView, (float) (offsetFactor * (t)));
	}

	public void setOnScrollChangedListener(OnScrollChangedListener listener) {
		mOnScrollChangedListener = listener;
	}

	public void setOverScrollEnabled(boolean enabled) {
		mIsOverScrollEnabled = enabled;
	}

	public boolean isOverScrollEnabled() {
		return mIsOverScrollEnabled;
	}

	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
			int scrollY, int scrollRangeX, int scrollRangeY,
			int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY,
				scrollRangeX, scrollRangeY,
				mIsOverScrollEnabled ? maxOverScrollX : 0,
				mIsOverScrollEnabled ? maxOverScrollY : 0, isTouchEvent);
	}
}