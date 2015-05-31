package com.enormous.discover.consumer.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class NotifyingScrollView extends ScrollView {

    private OnScrollChangedListener onScrollChangedListener;
	
	public interface OnScrollChangedListener {
		void onScrollChanged(ScrollView view, int l, int t, int oldl, int oldt);
	}
	
	public NotifyingScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NotifyingScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NotifyingScrollView(Context context) {
		super(context);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if (onScrollChangedListener != null) {
			onScrollChangedListener.onScrollChanged(this, l, t, oldl, oldt);
		}
	}
	
	public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener) {
		this.onScrollChangedListener = onScrollChangedListener;
	}

}
