package com.enormous.pkpizzas.consumer.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.enormous.pkpizzas.consumer.R;


/**
 * Created by Manas on 8/19/2014.
 */
public class SlidingTabsMainScreen extends LinearLayout implements View.OnClickListener {

    private Resources res;
    private int titleTextColor;
    private int titleTextColorSelected;
    private int indicatorColor;
    private LayoutParams iconTabLayoutParams;
    private LayoutParams textTabLayoutParams;
    private ImageButton archiveImageButton;
    private ImageButton profileImageButton;
    private TextView titleTextView;
    private ViewPager pager;
    private PageChangeListener pageListener;
    private ViewPager.OnPageChangeListener delegateListener;
    private int tabCount = 3;
    private int currentPositionForDrawing;
    private float currentPositionOffsetForDrawing;
    private int currentPosition;
    private int previousPosition = 1;
    private Paint indicatorPaint;
    private float indicatorHeight;

    public SlidingTabsMainScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        res = getResources();

        setWillNotDraw(false);

        //get required data from resources and set default values
        titleTextColor = res.getColor(R.color.whiteTranslucent50Percent);
        titleTextColorSelected = res.getColor(R.color.white);
        indicatorColor = res.getColor(R.color.white);
        indicatorHeight = Utils.convertDpToPixel(5);

        //set up layout params
        iconTabLayoutParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2.5f);
        textTabLayoutParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 5f);

        //set up indicator paint
        indicatorPaint = new Paint();
        indicatorPaint.setColor(indicatorColor);
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setAntiAlias(true);
    }

    public SlidingTabsMainScreen(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabsMainScreen(Context context) {
        this(context, null, 0);
    }

    public void setViewPager(ViewPager pager) {
        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have an adapter attached to it");
        }
        else {
            this.pager = pager;
            pageListener = new PageChangeListener();
            pager.setOnPageChangeListener(pageListener);
            currentPositionForDrawing = pager.getCurrentItem();
            currentPosition = 0;
            addTabs();
        }
    }

    public void setDelegatePageListener(ViewPager.OnPageChangeListener delegateListener) {
        this.delegateListener = delegateListener;
    }

    private void addTabs() {
        archiveImageButton = new ImageButton(getContext());
        archiveImageButton.setLayoutParams(iconTabLayoutParams);
        archiveImageButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        archiveImageButton.setBackgroundResource(R.drawable.sts_tab_background_selector);
        archiveImageButton.setImageResource(R.drawable.ic_action_archives_2);
        archiveImageButton.setOnClickListener(this);
        archiveImageButton.setTag(0);
        addView(archiveImageButton, 0);

        titleTextView = new TextView(getContext());
        titleTextView.setLayoutParams(textTabLayoutParams);
        titleTextView.setText("pkpizzas");
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        titleTextView.setGravity(Gravity.CENTER);
        titleTextView.setBackgroundResource(R.drawable.sts_tab_background_selector);
        titleTextView.setTextColor(titleTextColor);
        titleTextView.setOnClickListener(this);
        titleTextView.setTag(1);
        addView(titleTextView, 1);

        profileImageButton = new ImageButton(getContext());
        profileImageButton.setLayoutParams(iconTabLayoutParams);
        profileImageButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        profileImageButton.setBackgroundResource(R.drawable.sts_tab_background_selector);
        profileImageButton.setImageResource(R.drawable.ic_chat_2);
        profileImageButton.setOnClickListener(this);
        profileImageButton.setTag(2);
        addView(profileImageButton, 2);

        //set default default selection AFTER the view is ready
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                currentPositionForDrawing = pager.getCurrentItem();
                currentPosition = pager.getCurrentItem();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                View currentTab = getChildAt(currentPosition);
                deselectTab(previousPosition);
                selectTab(currentPosition);
            }
        });
    }

    public void selectTab(int pos) {
        switch(pos) {
            case 0:
                archiveImageButton.setImageResource(R.drawable.ic_action_archives_selected_2);
                break;
            case 1:
                titleTextView.setTextColor(titleTextColorSelected);
                break;
            case 2:
                profileImageButton.setImageResource(R.drawable.ic_chat_white_18dp);
                break;
        }
    }

    public void deselectTab(int pos) {
        switch(pos) {
            case 0:
                archiveImageButton.setImageResource(R.drawable.ic_action_archives_2);
                break;
            case 1:
                titleTextView.setTextColor(titleTextColor);
                break;
            case 2:
                profileImageButton.setImageResource(R.drawable.ic_chat_2);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        pager.setCurrentItem((Integer) view.getTag());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (pager != null) {
            float height = getHeight();

            float lineLeft = getChildAt(currentPositionForDrawing).getLeft();
            float lineRight = getChildAt(currentPositionForDrawing).getRight();

            //interpolate between the x coordinates of the edges of consecutive tabs if offset > 0
            if (currentPositionOffsetForDrawing > 0f && currentPositionForDrawing < tabCount - 1) {
                View nextTab = getChildAt(currentPositionForDrawing + 1);
                float nextLineLeft = nextTab.getLeft();
                float nextLineRight = nextTab.getRight();

                lineLeft = lerp(lineLeft, nextLineLeft, currentPositionOffsetForDrawing);
                lineRight = lerp(lineRight, nextLineRight, currentPositionOffsetForDrawing);
            }

            //draw indicator
            canvas.drawRect(lineLeft, height - indicatorHeight, lineRight, height, indicatorPaint);
        }

    }

    private float lerp(float v0, float v1, float t) {
        return (1-t)*v0 + t*v1;
    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int pos, float offset, int offsetPx) {
            currentPositionForDrawing = pos;
            currentPositionOffsetForDrawing = offset;
            invalidate();

            if (delegateListener != null) {
                delegateListener.onPageScrolled(pos, offset, offsetPx);
            }
        }

        @Override
        public void onPageSelected(int i) {
            currentPosition = i;

            deselectTab(previousPosition);
            selectTab(currentPosition);

            previousPosition = currentPosition;

            if (delegateListener != null) {
                delegateListener.onPageSelected(i);
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {
            if (delegateListener != null) {
                delegateListener.onPageScrollStateChanged(i);
            }
        }
    }

}
