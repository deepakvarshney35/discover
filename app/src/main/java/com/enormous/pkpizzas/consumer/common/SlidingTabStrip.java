package com.enormous.pkpizzas.consumer.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.enormous.pkpizzas.consumer.R;

import java.util.Locale;

/**
 * Created by Manas on 8/17/2014.
 */
public class SlidingTabStrip extends HorizontalScrollView {

    private final String TAG = "SlidingTabStrip";
    private boolean wasSetViewPagerCalled = false;
    private int divColor;
    private int indicatorColor;
    private int tabTextColorSelected;
    private int tabTextColor;
    private float tabPaddingLeftRight;
    private float tabPaddingTopBottom;
    private float indicatorHeight;
    private float divWidth;
    private float divPadding;
    private float defaultScrollOffset;
    private Paint indicatorPaint;
    private Paint divPaint;
    private ViewPager pager;
    private SlidingTabPageChangeListener pageListener;
    private ViewPager.OnPageChangeListener delegateListener;
    private int tabCount;
    private int currentPositionForDrawing = 0;
    private float currentPositionOffsetForDrawing = 0;
    private int currentPosition = 0;
    private int previousPosition = 0;
    private boolean shouldExpand = false;
    private boolean shouldShowDividers = true;
    private LinearLayout tabsContainer;
    private LinearLayout.LayoutParams containerLayoutParams;
    private LinearLayout.LayoutParams tabLayoutParams;
    private LinearLayout.LayoutParams tabLayoutParamsExpandable;
    private int lastScrollX;

    public SlidingTabStrip(Context context) {
        this(context, null, 0);
    }

    public SlidingTabStrip(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabStrip(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setWillNotDraw(false);
        setFillViewport(true);

        //set default values
        divColor = Color.parseColor("#1F000000");
        indicatorColor = Color.parseColor("#3F5CA9");
        tabTextColor = Color.parseColor("#89000000");
        tabTextColorSelected = Color.parseColor("#DD000000");
        tabPaddingLeftRight = Utils.convertDpToPixel(20);
        tabPaddingTopBottom = Utils.convertDpToPixel(15);
        indicatorHeight = Utils.convertDpToPixel(5);
        divWidth = Utils.convertDpToPixel(1);
        divPadding = Utils.convertDpToPixel(12);
        defaultScrollOffset = Utils.convertDpToPixel(50);

        //set up Paints
        indicatorPaint = new Paint();
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setColor(indicatorColor);
        indicatorPaint.setAntiAlias(true);
        divPaint = new Paint();
        divPaint.setStyle(Paint.Style.FILL);
        divPaint.setColor(divColor);
        divPaint.setAntiAlias(true);

        //init tab layout params
        tabLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        tabLayoutParamsExpandable = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);

        //set up tabs container
        containerLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        tabsContainer = new LinearLayout(context);
        tabsContainer.setLayoutParams(containerLayoutParams);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        addView(tabsContainer);

    }

    public void setViewPager(ViewPager pager) {
        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have an adapter set on it");
        }
        else {
            this.pager = pager;
            pageListener = new SlidingTabPageChangeListener();
            pager.setOnPageChangeListener(pageListener);
            notifyDataSetChanged();
            wasSetViewPagerCalled = true;
        }
    }

    public void setDelegatePageChangeListener(ViewPager.OnPageChangeListener delegateListener) {
        this.delegateListener = delegateListener;
    }

    public void shouldExpand(boolean shouldExpand) {
        if (!wasSetViewPagerCalled) {
            this.shouldExpand = shouldExpand;
        }
        else {
            throw new IllegalStateException("shouldExpand must be called before setting ViewPager");
        }
    }

    public void showDividers(boolean shouldShowDividers) {
        if (pager != null) {
            throw new IllegalStateException("showDividers must be called before setting ViewPager");
        }
        else {
            this.shouldShowDividers = shouldShowDividers;
        }
    }

    private void addTextTab(int position) {
        TextView tab = new TextView(getContext());
        tab.setPadding((int) tabPaddingLeftRight, (int) tabPaddingTopBottom, (int) tabPaddingLeftRight, (int) tabPaddingTopBottom);
        tab.setText(pager.getAdapter().getPageTitle(position).toString().toUpperCase(Locale.ENGLISH));
        tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tab.setTextColor(tabTextColor);
        tab.setTypeface(Typeface.create("sans-serif-regular", Typeface.BOLD));
        tab.setGravity(Gravity.CENTER);
        tab.setSingleLine();
        if (shouldExpand) {
            tab.setLayoutParams(tabLayoutParamsExpandable);
        }
        else {
            tab.setLayoutParams(tabLayoutParams);
        }
        addTab(tab, position);
    }

    private void addTab(View tab, final int position) {
        tab.setFocusable(true);
        tab.setClickable(true);
        tab.setBackgroundResource(R.drawable.sts_tab_background_selector);
        tab.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                pager.setCurrentItem(position);
            }
        });
        tabsContainer.addView(tab, position);
    }

    public void notifyDataSetChanged() {
        tabCount = pager.getAdapter().getCount();
        updatePaintColors();

        //add all tabs to container
        tabsContainer.removeAllViews();
        for (int i = 0; i < tabCount; i++) {
            addTextTab(i);
        }

        //set default default selection AFTER the view is ready
        tabsContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                currentPositionForDrawing = pager.getCurrentItem();
                scrollToChild(currentPositionForDrawing, (int) defaultScrollOffset);
                tabsContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                View currentTab = tabsContainer.getChildAt(currentPosition);
                if (currentTab instanceof TextView) {
                    ((TextView) currentTab).setTextColor(tabTextColorSelected);
                }
            }
        });
    }

    public void updatePaintColors() {
        divPaint.setColor(divColor);
        indicatorPaint.setColor(indicatorColor);
    }

    private void scrollToChild(int position, int offset) {
        int newScrollX = tabsContainer.getChildAt(position).getLeft() - offset/2;
        if (lastScrollX != newScrollX) {
            smoothScrollTo(newScrollX, 0);
        }
        lastScrollX = newScrollX;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float height = getHeight();

        float lineLeft = tabsContainer.getChildAt(currentPositionForDrawing).getLeft();
        float lineRight = tabsContainer.getChildAt(currentPositionForDrawing).getRight();

        //interpolate between the x coordinates of the edges of consecutive tabs if offset > 0
        if (currentPositionOffsetForDrawing > 0f && currentPositionForDrawing < tabCount - 1) {
            View nextTab = tabsContainer.getChildAt(currentPositionForDrawing + 1);
            float nextLineLeft = nextTab.getLeft();
            float nextLineRight = nextTab.getRight();

            lineLeft = lerp(lineLeft, nextLineLeft, currentPositionOffsetForDrawing);
            lineRight = lerp(lineRight, nextLineRight, currentPositionOffsetForDrawing);
        }

        //draw indicator
        canvas.drawRect(lineLeft, height - indicatorHeight, lineRight, height, indicatorPaint);

        //draw dividers
        if (shouldShowDividers) {
            for (int i = 0; i < tabCount - 1 ; i++) {
                View tab = tabsContainer.getChildAt(i);
                canvas.drawRect(tab.getRight(), divPadding, tab.getRight() + divWidth, height - divPadding, divPaint);
            }
        }
    }

    private float lerp(float v0, float v1, float t) {
        return (1-t)*v0 + t*v1;
    }

    private class SlidingTabPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int pos, float offset, int offsetPixels) {
            currentPositionForDrawing = pos;
            currentPositionOffsetForDrawing = offset;
            invalidate();

            if (delegateListener != null) {
                delegateListener.onPageScrolled(pos, offset, offsetPixels);
            }
        }

        @Override
        public void onPageSelected(int pos) {
            currentPosition = pos;
            scrollToChild(currentPosition, tabsContainer.getChildAt(currentPosition).getWidth());

            //set selected tab's text color to black
            View currentTab = tabsContainer.getChildAt(currentPosition);
            View previousTab = tabsContainer.getChildAt(previousPosition);
            if (currentTab instanceof TextView && previousTab instanceof TextView) {
                ((TextView) currentTab).setTextColor(tabTextColorSelected);
                ((TextView) previousTab).setTextColor(tabTextColor);
            }

            if (delegateListener != null) {
                delegateListener.onPageSelected(pos);
            }

            previousPosition = currentPosition;
        }

        @Override
        public void onPageScrollStateChanged(int scrollState) {
            if (delegateListener != null) {
                delegateListener.onPageScrollStateChanged(scrollState);
            }
        }
    }


    public void setDivColor(int divColor) {
        if (!wasSetViewPagerCalled) {
            this.divColor = divColor;
        }
        else {
            throw new IllegalStateException("setDivColor must be called before setting ViewPager");
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

    public void setTabTextColor(int tabTextColor) {
        if (!wasSetViewPagerCalled) {
            this.tabTextColor = tabTextColor;
        }
        else {
            throw new IllegalStateException("setTabTextColor must be called before setting ViewPager");
        }
    }

    public void setTabTextColorSelected(int tabTextColorSelected) {
        if (!wasSetViewPagerCalled) {
            this.tabTextColorSelected = tabTextColorSelected;
        }
        else {
            throw new IllegalStateException("setTabTextColorSelected must be called before setting ViewPager");
        }
    }

    public void setTabPaddingLeftRight(int dp) {
        if (!wasSetViewPagerCalled) {
            this.tabPaddingLeftRight = Utils.convertDpToPixel(dp);
        }
        else {
            throw new IllegalStateException("setTabPaddingLeftRight must be called before setting ViewPager");
        }
    }

    public void setTabPaddingTopBottom(int dp) {
        if (!wasSetViewPagerCalled) {
            this.tabPaddingTopBottom = Utils.convertDpToPixel(dp);
        }
        else {
            throw new IllegalStateException("setTabPaddingTopBottom must be called before setting ViewPager");
        }
    }

    public void setIndicatorHeight(int dp) {
        if (!wasSetViewPagerCalled) {
            this.indicatorHeight = Utils.convertDpToPixel(dp);
        }
        else {
            throw new IllegalStateException("setIndicatorHeight must be called before setting ViewPager");
        }
    }

    public void setDivWidth(int dp) {
        if (!wasSetViewPagerCalled) {
            this.divWidth = Utils.convertDpToPixel(dp);
        }
        else {
            throw new IllegalStateException("setDivWidth must be called before setting ViewPager");
        }
    }

    public void setDivPadding(int dp) {
        if (!wasSetViewPagerCalled) {
            this.divPadding = Utils.convertDpToPixel(dp);
        }
        else {
            throw new IllegalStateException("setDivPadding must be called before setting ViewPager");
        }
    }

}
