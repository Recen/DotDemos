package com.recen.dotdemos;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by Recen on 2018/4/19.
 */

public class HeaderViewPager extends LinearLayout {
    private int topOffset = 0;
    private View mHeaderView;
    private int mHeaderWidth;
    private int mHeaderHeight;

    public HeaderViewPager(Context context) {
        this(context, null);
    }

    public HeaderViewPager(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public HeaderViewPager(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.HeaderViewPager);
        topOffset = typedArray.getDimensionPixelSize(typedArray.getIndex(R.styleable.HeaderViewPager_topOffset),topOffset);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mHeaderView = getChildAt(0);
        measureChildWithMargins(mHeaderView,widthMeasureSpec,0,MeasureSpec.UNSPECIFIED,0);
        mHeaderHeight = mHeaderView.getMeasuredHeight();

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
