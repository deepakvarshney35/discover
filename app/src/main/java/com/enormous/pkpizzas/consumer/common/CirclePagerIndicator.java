package com.enormous.pkpizzas.consumer.common;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by Manas on 8/17/2014.
 */
public class CirclePagerIndicator extends LinearLayout {

    private final String TAG = "CirclePagerIndicator";
    private Resources res;
    private int indicatorColor;
    private int indicatorColorSelected;
    private float indicatorRadius;
    private Paint selectedIndicatorPaint;
    private LayoutParams containerLayoutParams;
    private float containerPadding;
    private float indicatorPadding;
    private LayoutParams indicatorLayoutParams;
    private ViewPager pager;
    private int indicatorCount;
    private int currentPosition;
    private float currentPositionOffset;
    private PagerIndicatorPageListener pageListener;
    private ShapeDrawable indicatorShape;
    boolean wasSetViewPagerCalled = false;

    public CirclePagerIndicator(Context context) {
        this(context, null, 0);
    }

    public CirclePagerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CirclePagerIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setWillNotDraw(false);

        //set default values
        indicatorColor = Color.parseColor("#33ffffff");
        indicatorColorSelected = Color.parseColor("#ffffff");
        indicatorRadius = Utils.convertDpToPixel(4);
        containerPadding = Utils.convertDpToPixel(15);
        indicatorPadding = Utils.convertDpToPixel(3);

        //set up selectedIndicatorPaint
        selectedIndicatorPaint = new Paint();
        selectedIndicatorPaint.setAntiAlias(true);
        selectedIndicatorPaint.setStyle(Paint.Style.FILL);
        selectedIndicatorPaint.setColor(indicatorColorSelected);

        //set up current lin layout that will be used as a container for indicators
        containerLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(containerLayoutParams);
        setOrientation(LinearLayout.HORIZONTAL);
        setPadding((int) containerPadding, (int) containerPadding, (int) containerPadding, (int) containerPadding);
        setGravity(Gravity.CENTER);

        //set up indicator shape
        indicatorShape = new ShapeDrawable(new OvalShape());
        indicatorShape.setIntrinsicWidth((int) (indicatorRadius*2));
        indicatorShape.setIntrinsicHeight((int) indicatorRadius*2);
        indicatorShape.getPaint().setColor(indicatorColor);
        indicatorShape.getPaint().setAntiAlias(true);
        indicatorShape.getPaint().setStyle(Paint.Style.FILL);

        indicatorLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    public void setViewPager(ViewPager pager) {
        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have an adapter attached to it");
        }
        else {
            this.pager = pager;
            pageListener = new PagerIndicatorPageListener();
            pager.setOnPageChangeListener(pageListener);
            notifyDataSetChanged();
            wasSetViewPagerCalled = true;
        }
    }

    public void notifyDataSetChanged() {
        this.indicatorCount = pager.getAdapter().getCount();
        updateStyles();

        //add indicators to container
        removeAllViews();
        for (int i = 0; i < indicatorCount; i++) {
            addIndicator(i);
        }
    }

    private void updateStyles() {
        selectedIndicatorPaint.setColor(indicatorColorSelected);
        indicatorShape.getPaint().setColor(indicatorColor);
        indicatorShape.setIntrinsicWidth((int) (indicatorRadius*2));
        indicatorShape.setIntrinsicHeight((int) indicatorRadius*2);
    }

    private void addIndicator(int position) {
        ImageView indicatorImageView = new ImageView(getContext());
        indicatorImageView.setLayoutParams(indicatorLayoutParams);
        indicatorImageView.setImageDrawable(indicatorShape);
        indicatorImageView.setPadding((int) indicatorPadding, (int) indicatorPadding, (int) indicatorPadding, (int) indicatorPadding);
        addView(indicatorImageView, position);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode() || indicatorCount == 0) {
            return;
        }

        int height = getHeight();

        View indicator = getChildAt(currentPosition);
        float cx = indicator.getLeft() + (indicatorRadius) + indicatorPadding;

        if (currentPositionOffset > 0f && currentPosition < indicatorCount - 1) {
            View nextIndicator = getChildAt(currentPosition + 1);
            float nextCx = nextIndicator.getLeft() + (indicatorRadius) + indicatorPadding;
            cx = lerp(cx, nextCx, currentPositionOffset);
        }

        canvas.drawCircle(cx, (float) height/2, (float) indicatorRadius, selectedIndicatorPaint);
    }

    float lerp(float v0, float v1, float t) {
        return (1-t)*v0 + t*v1;
    }

    private class PagerIndicatorPageListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int pos, float offset, int offsetPx) {
            currentPosition = pos;
            currentPositionOffset = offset;
            invalidate();
        }

        @Override
        public void onPageSelected(int i) {

        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }

    public void setIndicatorColor(int indicatorColor) {
        if (!wasSetViewPagerCalled) {
            this.indicatorColor = indicatorColor;
        }
        else {
            throw new IllegalStateException("setIndicatorColor must be called before setting ViewPager");
        }
    }

    public void setSelectedIndicatorColor(int indicatorColorSelected) {
        if (!wasSetViewPagerCalled) {
            this.indicatorColorSelected = indicatorColorSelected;
        }
        else {
            throw new IllegalStateException("setSelectedIndicatorColor must be called before setting ViewPager");
        }
    }

    public void setIndicatorRadius(int dp) {
        if (!wasSetViewPagerCalled) {
            this.indicatorRadius = Utils.convertDpToPixel(dp);
        }
        else {
            throw new IllegalStateException("setIndicatorRadius must be called before setting ViewPager");
        }
    }

    public void setIndicatorPadding(int dp) {
        if (!wasSetViewPagerCalled) {
            this.indicatorPadding = Utils.convertDpToPixel(dp);
        }
        else {
            throw new IllegalStateException("setIndicatorPadding must be called before setting ViewPager");
        }
    }
}
