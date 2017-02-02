package pl.shareddrawboard;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by Arjan on 02.02.2017.
 */

public class CustomScrollView extends ScrollView {

	private boolean scrollEnabled;

	public CustomScrollView(Context context) {
		super(context);
	}

	public CustomScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		if (scrollEnabled) {
			return super.onInterceptTouchEvent(ev);
		} else {
			return false;
		}
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (scrollEnabled) {
			return super.onTouchEvent(ev);
		} else {
			return false;
		}
	}

	public boolean isScrollEnabled() {
		return scrollEnabled;
	}

	public void setScrollEnabled(boolean scrollEnabled) {
		this.scrollEnabled = scrollEnabled;
	}

}
