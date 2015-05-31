package com.enormous.pkpizzas.consumer.models;// Created by Sanat Dutta on 1/17/2015.

import android.content.Context;
import android.view.animation.Animation;
import android.widget.ImageView;

public class ButtonViewAnimator extends ImageView {

    private Animation inAnimation;
    private Animation outAnimation;

    public ButtonViewAnimator(Context context)
    {
        super(context);
    }

    public void setInAnimation(Animation inAnimation)
    {
        this.inAnimation = inAnimation;
    }

    public void setOutAnimation(Animation outAnimation)
    {
        this.outAnimation = outAnimation;
    }

    @Override
    public void setVisibility(int visibility)
    {
        if (getVisibility() != visibility)
        {
            if (visibility == VISIBLE)
            {
                if (inAnimation != null) startAnimation(inAnimation);
            }
            else if ((visibility == INVISIBLE) || (visibility == GONE))
            {
                if (outAnimation != null) startAnimation(outAnimation);
            }
        }

        super.setVisibility(visibility);
    }
}
