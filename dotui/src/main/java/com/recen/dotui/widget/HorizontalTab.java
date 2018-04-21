package com.recen.dotui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.recen.dotools.res.DotDisplayHelper;
import com.recen.dotools.res.DotNumHelper;
import com.recen.dotools.res.DotResHelper;
import com.recen.dotools.res.DotStringHelper;
import com.recen.dotools.res.DotViewHelper;
import com.recen.dotui.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Recen on 2018/4/20.
 */

public class HorizontalTab extends HorizontalScrollView {
    // mode: 自适应宽度+滚动 / 均分
    public static final int MODE_SCROLLABLE = 0;
    public static final int MODE_FIXED = 1;
    // icon position
    private static final int ICON_POSITION_LEFT = 0;
    private static final int ICON_POSITION_TOP = 1;
    private static final int ICON_POSITION_RIGHT = 2;
    private static final int ICON_POSITION_BOTTOM = 4;
    // status: 用于记录tab的改变状态
    private static final int STATUS_NORMAL = 0;
    private static final int STATUS_PROGRESS = 1;
    private static final int STATUS_SELECTED = 2;

    private View mIndicatorView;
    private Container mContentLayout;
    /**
     * 是否有Indicator
     */
    private boolean mHasIndicator = true;
    /**
     * Indicator高度
     */
    private int mIndicatorHeight;
    /**
     * indicator采用drawable
     */
    private Drawable mIndicatorDrawable;
    /**
     * item的默认字体大小
     */
    private int mTabTextSize;
    /**
     * indicator在顶部
     */
    private boolean mIndicatorTop = false;
    /**
     * ScrollMode下item的间隙
     */
    private int mItemSpaceInScrollMode;
    /**
     * item icon的默认位置
     */
    private int mTabIconPosition;
    /**
     * TabSegmentMode
     */
    private int mMode = MODE_FIXED;
    /**
     * item normal color
     */
    private int mDefaultNormalColor;
    /**
     * item selected color
     */
    private int mDefaultSelectedColor;
    /**
     * typeface
     */
    private TypefaceProvider mTypefaceProvider;
    private final ArrayList<OnTabSelectedListener> mSelectedListeners = new ArrayList<>();
    private boolean mIsAnimating;
    private boolean mForceIndicatorNotDoLayoutWhenParentLayout = false;

    public HorizontalTab(Context context) {
        this(context, null);
    }

    public HorizontalTab(Context context, boolean hasIndicator) {
        super(context, null);
        this.mHasIndicator = hasIndicator;
    }

    public HorizontalTab(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.HorizontalTabStyle);
    }

    public HorizontalTab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        String typefaceProviderName;
        mDefaultSelectedColor = ContextCompat.getColor(context, R.color.dot_color_blue);
        mDefaultNormalColor = ContextCompat.getColor(context, R.color.dot_color_gray_5);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.HorizontalTab, defStyleAttr, 0);
        mHasIndicator = array.getBoolean(R.styleable.HorizontalTab_dot_tab_has_indicator, true);
        mIndicatorHeight = array.getDimensionPixelSize(R.styleable.HorizontalTab_dot_tab_indicator_height,
                getResources().getDimensionPixelSize(R.dimen.dot_tab_segment_indicator_height));
        mTabTextSize = array.getDimensionPixelSize(R.styleable.HorizontalTab_android_textSize,
                getResources().getDimensionPixelSize(R.dimen.dot_tab_segment_text_size));
        mIndicatorTop = array.getBoolean(R.styleable.HorizontalTab_dot_tab_indicator_top, false);
        mTabIconPosition = array.getInt(R.styleable.HorizontalTab_dot_tab_icon_position, ICON_POSITION_LEFT);
        mMode = array.getInt(R.styleable.HorizontalTab_dot_tab_mode, MODE_FIXED);
        mItemSpaceInScrollMode = array.getDimensionPixelSize(R.styleable.HorizontalTab_dot_tab_mode, DotDisplayHelper.dp2px(context, 10));
        typefaceProviderName = array.getString(R.styleable.HorizontalTab_dot_tab_typeface_provider);
        array.recycle();

        mContentLayout = new Container(context);
        addView(mContentLayout, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (mHasIndicator) {
            createIndicatorView();
        }

    }

    private void createIndicatorView() {
        if (mIndicatorView == null) {
            mIndicatorView = new View(getContext());
            mIndicatorView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, mIndicatorHeight));
            if (mIndicatorDrawable != null) {
                DotViewHelper.setBackgroundKeepingPadding(mIndicatorView, mIndicatorDrawable);
            } else {
                mIndicatorView.setBackgroundColor(mDefaultSelectedColor);
            }
            mContentLayout.addView(mIndicatorView);
        }
    }

    private void setTextViewTypeface(TextView tv, boolean selected) {
        if (mTypefaceProvider == null || tv == null) {
            return;
        }
        boolean isBold = selected ? mTypefaceProvider.isSelectedTabBold() : mTypefaceProvider.isNormalTabBold();
        tv.setTypeface(null, isBold ? Typeface.BOLD : Typeface.NORMAL);
    }


    public static class Tab {
        private Drawable normalIcon = null;
        private Drawable selectedIcon = null;
        public static final int USE_TAB_SEGMENT = Integer.MIN_VALUE;
        private int textSize = USE_TAB_SEGMENT;
        private int normalColor = USE_TAB_SEGMENT;
        private int selectedColor = USE_TAB_SEGMENT;
        private int iconPosition = USE_TAB_SEGMENT;
        private int gravity = Gravity.CENTER;
        private List<View> mCustomViews;
        private int contentWidth = 0;
        private int contentLeft = 0;
        private CharSequence text;
        private int mSignCountDigits = 2;
        private TextView mSignCountTextView;
        private int mSignCountMarginRight = 0;
        private int mSignCountMarginTop = 0;
        /**
         * 是否动态更改icon颜色，如果为true, selectedIcon将失效
         */
        private boolean dynamicChangeIconColor = true;


        public Tab(CharSequence text) {
            this.text = text;
        }

        public Tab(Drawable normalIcon, Drawable selectedIcon, CharSequence text, boolean dynamicChangeIconColor) {
            this.normalIcon = normalIcon;
            if (this.normalIcon != null) {
                this.normalIcon.setBounds(0, 0, normalIcon.getIntrinsicWidth(), normalIcon.getIntrinsicHeight());
            }
            this.selectedIcon = selectedIcon;
            if (this.selectedIcon != null) {
                this.selectedIcon.setBounds(0, 0, selectedIcon.getIntrinsicWidth(), selectedIcon.getIntrinsicHeight());
            }
            this.text = text;
            this.dynamicChangeIconColor = dynamicChangeIconColor;
        }

        /**
         * 设置红点中数字显示的最大位数，默认值为 2，超过这个位数以 99+ 这种形式显示。如：110 -> 99+，98 -> 98
         *
         * @param digit 数字显示的最大位数
         */
        public void setmSignCountDigits(int digit) {
            mSignCountDigits = digit;
        }

        public void setTextColor(@ColorInt int normalColor, @ColorInt int selectedColor) {
            this.normalColor = normalColor;
            this.selectedColor = selectedColor;
        }

        public int getTextSize() {
            return textSize;
        }

        public void setTextSize(int textSize) {
            this.textSize = textSize;
        }

        public CharSequence getText() {
            return text;
        }

        public void setText(CharSequence text) {
            this.text = text;
        }

        public Drawable getNormalIcon() {
            return normalIcon;
        }

        public void setNormalIcon(Drawable normalIcon) {
            this.normalIcon = normalIcon;
        }

        public Drawable getSelectedIcon() {
            return selectedIcon;
        }

        public void setSelectedIcon(Drawable selectedIcon) {
            this.selectedIcon = selectedIcon;
        }

        public int getContentWidth() {
            return contentWidth;
        }

        public void setContentWidth(int contentWidth) {
            this.contentWidth = contentWidth;
        }

        public int getContentLeft() {
            return contentLeft;
        }

        public void setContentLeft(int contentLeft) {
            this.contentLeft = contentLeft;
        }

        public int getNormalColor() {
            return normalColor;
        }

        public void setNormalColor(int normalColor) {
            this.normalColor = normalColor;
        }

        public int getSelectedColor() {
            return selectedColor;
        }

        public void setSelectedColor(int selectedColor) {
            this.selectedColor = selectedColor;
        }

        public int getIconPosition() {
            return iconPosition;
        }

        public void setIconPosition(int iconPosition) {
            this.iconPosition = iconPosition;
        }

        public int getGravity() {
            return gravity;
        }

        public void setGravity(int gravity) {
            this.gravity = gravity;
        }

        public boolean isDynamicChangeIconColor() {
            return dynamicChangeIconColor;
        }

        public void setDynamicChangeIconColor(boolean dynamicChangeIconColor) {
            this.dynamicChangeIconColor = dynamicChangeIconColor;
        }

        private RelativeLayout.LayoutParams getDefaultCustomLayoutParam() {
            return new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }

        public void addCustomView(@NotNull View view) {
            if (mCustomViews == null) {
                mCustomViews = new ArrayList<>();
            }

            if (view.getLayoutParams() == null) {
                view.setLayoutParams(getDefaultCustomLayoutParam());
            }
            mCustomViews.add(view);
        }

        public List<View> getmCustomViews() {
            return mCustomViews;
        }

        /**
         * 设置红点的位置, 注意红点的默认位置是在内容的右侧并顶对齐
         *
         * @param marginRight 在红点默认位置的基础上添加的 marginRight
         * @param marginTop   在红点默认位置的基础上添加的 marginTop
         */
        public void setSignCountMargin(int marginRight, int marginTop) {
            mSignCountMarginRight = marginRight;
            mSignCountMarginTop = marginTop;
            if (mSignCountTextView != null && mSignCountTextView.getLayoutParams() != null) {
                ((MarginLayoutParams) mSignCountTextView.getLayoutParams()).rightMargin = marginRight;
                ((MarginLayoutParams) mSignCountTextView.getLayoutParams()).topMargin = marginTop;
            }
        }

        private TextView ensureSignCountView(Context context) {
            if (mSignCountTextView == null) {
                mSignCountTextView = new TextView(context, null, R.attr.dot_tab_sign_count_view);
                RelativeLayout.LayoutParams signCountLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, DotResHelper.getAttrDimen(context, R.attr.dot_tab_sign_count_view_minSize));
                signCountLp.addRule(RelativeLayout.ALIGN_TOP, R.id.dot_tab_segment_item_id);
                signCountLp.addRule(RelativeLayout.RIGHT_OF, R.id.dot_tab_segment_item_id);
                mSignCountTextView.setLayoutParams(signCountLp);
                addCustomView(mSignCountTextView);
            }
            // 确保在先 setMargin 后 create 的情况下 margin 会生效
            setSignCountMargin(mSignCountMarginRight, mSignCountMarginTop);
            return mSignCountTextView;
        }

        /**
         * 显示 Tab 上的未读数或红点
         *
         * @param count 不为0时红点会显示该数字作为未读数,为0时只会显示一个小红点
         */
        public void showSignCountView(Context context, int count) {
            ensureSignCountView(context);
            mSignCountTextView.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams signCountLp = (RelativeLayout.LayoutParams) mSignCountTextView.getLayoutParams();
            if (count != 0) {
                // 显示未读数
                signCountLp.height = DotResHelper.getAttrDimen(mSignCountTextView.getContext(), R.attr.dot_tab_sign_count_view_minSize_with_text);
                mSignCountTextView.setLayoutParams(signCountLp);
                mSignCountTextView.setMinHeight(DotResHelper.getAttrDimen(mSignCountTextView.getContext(), R.attr.dot_tab_sign_count_view_minSize_with_text));
                mSignCountTextView.setMinWidth(DotResHelper.getAttrDimen(mSignCountTextView.getContext(), R.attr.dot_tab_sign_count_view_minSize_with_text));
                mSignCountTextView.setText(getNumberDigitsFormattingValue(count));
            } else {
                // 显示红点
                signCountLp.height = DotResHelper.getAttrDimen(mSignCountTextView.getContext(), R.attr.dot_tab_sign_count_view_minSize);
                mSignCountTextView.setLayoutParams(signCountLp);
                mSignCountTextView.setMinHeight(DotResHelper.getAttrDimen(mSignCountTextView.getContext(), R.attr.dot_tab_sign_count_view_minSize));
                mSignCountTextView.setMinWidth(DotResHelper.getAttrDimen(mSignCountTextView.getContext(), R.attr.dot_tab_sign_count_view_minSize));
                mSignCountTextView.setText(null);
            }
        }

        /**
         * 隐藏 Tab 上的未读数或红点
         */
        public void hideSignCountView() {
            if (mSignCountTextView != null) {
                mSignCountTextView.setVisibility(View.GONE);
            }
        }


        private String getNumberDigitsFormattingValue(int number) {
            if (DotNumHelper.getNumberDigits(number) > mSignCountDigits) {
                String result = "";
                for (int digit = 1; digit <= mSignCountDigits; digit++) {
                    result += "9";
                }
                result += "+";
                return result;
            } else {
                return String.valueOf(number);
            }
        }

        /**
         * 获取该 Tab 的未读数
         */
        public int getSignCount() {
            if (mSignCountTextView != null && !DotStringHelper.isNotNullOrEmpty(mSignCountTextView.getText())) {
                return Integer.parseInt(mSignCountTextView.getText().toString());
            } else {
                return 0;
            }
        }

    }

    public class InnerTextView extends AppCompatTextView {

        public InnerTextView(Context context) {
            super(context);
        }

        public InnerTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void requestLayout() {
            if (mForceIndicatorNotDoLayoutWhenParentLayout) {
                return;
            }
            super.requestLayout();
        }
    }

    public class TabItemView extends RelativeLayout {
        private InnerTextView mTextView;
        private GestureDetector mGestureDetector = null;

        public TabItemView(Context context) {
            super(context);
            mTextView = new InnerTextView(getContext());
            mTextView.setSingleLine(true);
            mTextView.setGravity(Gravity.CENTER);
            mTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            // 用于提供给customView布局用
            mTextView.setId(R.id.dot_tab_segment_item_id);
            RelativeLayout.LayoutParams tvLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvLp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            addView(mTextView, tvLp);

            mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (mSelectedListeners == null) {
                        return false;
                    } else {
                        if (mIsAnimating) {
                            return false;
                        }
                        int index = (int) TabItemView.this.getTag();
                        Tab model = getAda
                    }
                    return super.onDoubleTap(e);
                }
            });
        }

        public TextView getTextView() {
            return mTextView;
        }
    }

    public class TabAdapter extends DotItemViewsAdapter<Tab, TabItemView> {

        public TabAdapter(ViewGroup mParentView) {
            super(mParentView);
        }

        @Override
        protected TabItemView createView(ViewGroup viewGroup) {
            return new TabItemView(getContext());
        }

        @Override
        protected void bind(Tab item, TabItemView view, int position) {
            TextView tv = view.getTextView();
            setTextViewTypeface(tv,false);

            List<View> mCustomViews = item.getmCustomViews();
            if (mCustomViews != null && mCustomViews.size()>0){
                view.setTag(R.id.dot_tab_segment_item_id,true);
                for (View v :mCustomViews){
                    if (v.getParent() == null){
                        view.addView(v);
                    }
                }
            }

            if (mMode == MODE_FIXED){
                int gravity = item.getGravity();
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tv.getLayoutParams();
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, (gravity & Gravity.LEFT) == Gravity.LEFT ? RelativeLayout.TRUE : 0);
                lp.addRule(RelativeLayout.CENTER_HORIZONTAL, (gravity & Gravity.CENTER) == Gravity.CENTER ? RelativeLayout.TRUE : 0);
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, (gravity & Gravity.RIGHT) == Gravity.RIGHT ? RelativeLayout.TRUE : 0);
                tv.setLayoutParams(lp);
            }

            tv.setText(item.getText());

            if (item.getNormalIcon() == null){
                tv.setCompoundDrawablePadding(0);
                tv.setCompoundDrawables(null, null, null, null);
            }else {
                Drawable drawable = item.getNormalIcon();
                if (drawable != null) {
                    drawable = drawable.mutate();
                    setDrawable(tv, drawable, getTabIconPosition(item));
                    tv.setCompoundDrawablePadding(QMUIDisplayHelper.dp2px(getContext(), 4));
                } else {
                    tv.setCompoundDrawables(null, null, null, null);
                }
            }
        }
    }

    private final class Container extends ViewGroup {
        private int mLastSelectedIndex = -1;

        public Container(Context context) {
            super(context);
        }

        public Container(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public Container(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {

        }
    }


    public interface TypefaceProvider {

        boolean isNormalTabBold();

        boolean isSelectedTabBold();
    }

    public interface OnTabSelectedListener {
        /**
         * 当某个 Tab 被选中时会触发
         *
         * @param index 被选中的 Tab 下标
         */
        void onTabSelected(int index);

        /**
         * 当某个 Tab 被取消选中时会触发
         *
         * @param index 被取消选中的 Tab 下标
         */
        void onTabUnselected(int index);

        /**
         * 当某个 Tab 处于被选中状态下再次被点击时会触发
         *
         * @param index 被再次点击的 Tab 下标
         */
        void onTabReselected(int index);

        /**
         * 当某个 Tab 被双击时会触发
         *
         * @param index 被双击的 Tab 下标
         */
        void onDoubleTap(int index);
    }


}
