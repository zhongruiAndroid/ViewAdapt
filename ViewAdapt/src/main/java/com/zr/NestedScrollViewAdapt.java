package com.zr;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.zr.helper.BGHelper;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

public class NestedScrollViewAdapt extends NestedScrollView implements LayoutAdaptHelper.AdaptLayout {
    private LayoutAdaptHelper mHelper = new LayoutAdaptHelper( );
    /*需要判断状态栏是否隐藏*/
    public NestedScrollViewAdapt(@NonNull Context context) {
        super(context);
        mHelper.init(this,null, R.attr.LayoutAdaptAttr, R.style.LayoutAdaptStyle);
    }

    public NestedScrollViewAdapt(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mHelper.init(this,attrs, R.attr.LayoutAdaptAttr, R.style.LayoutAdaptStyle);
    }

    public NestedScrollViewAdapt(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHelper.init(this,attrs, defStyleAttr, R.style.LayoutAdaptStyle);
    }


    @Override
    public void ignoreWidth(int ignoreWidth) {
        mHelper.setIgnoreAdaptWidth(ignoreWidth);
    }

    @Override
    public void ignoreHeight(int ignoreHeight) {
        mHelper.setIgnoreAdaptHeight(ignoreHeight);
    }

    @Override
    public void setContentViewSize(int width, int height) {
        mHelper.setContentViewSize(width, height);
        BGHelper.resetDrawable(this,preDrawable);
    }

    private Drawable preDrawable;
    /*因为setBackground在构造函数的super方法中先于LayoutAdaptHelper构造之前执行，所以先保存Drawable*/
    @Override
    public void setBackgroundDrawable(Drawable background) {
        if(BGHelper.drawableAdaptEnable(mHelper)){
            Drawable drawable = BGHelper.drawableAdapt(this, background, mHelper);
            super.setBackgroundDrawable(drawable);
            preDrawable=null;
        }else{
            preDrawable=background;
            super.setBackgroundDrawable(background);
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mHelper.canUseAdapt()) {
            mHelper.setContentViewMeasureSpec(this,widthMeasureSpec,heightMeasureSpec);
            mHelper.adjustChildren(this);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHelper.canUseAdapt()) {
            adaptSelf();
        }
    }

    private void adaptSelf() {
        if (getParent() == null) {
            return;
        }
        /*如果XXXLayoutAdapt的父view是系统ViewGroup,并且自身有设置layout_adapt_width或者layout_adapt_height，则自己适配自己的宽高*/
        if (!(getParent() instanceof LayoutAdaptHelper.AdaptLayout)) {
            int selfWidth = mHelper.selfWidth;
            int selfHeight = mHelper.selfHeight;
            if (selfWidth > 0 && selfHeight > 0) {
                setMeasuredDimension(mHelper.getRealSizeInt(this, selfWidth), mHelper.getRealSizeInt(this, selfHeight));
            } else if (selfWidth > 0) {
                setMeasuredDimension(mHelper.getRealSizeInt(this, selfWidth), getMeasuredHeight());
            } else if (selfHeight > 0) {
                setMeasuredDimension(getMeasuredWidth(), mHelper.getRealSizeInt(this, selfHeight));
            }
        }
    }

    public void setPaddingAdapt(int left, int top, int right, int bottom) {
        if (mHelper.canUseAdapt()) {
            super.setPadding(mHelper.getRealSizeInt(this, left), mHelper.getRealSizeInt(this, top), mHelper.getRealSizeInt(this, right), mHelper.getRealSizeInt(this, bottom));
        }
    }

    @RequiresApi(api = JELLY_BEAN_MR1)
    public void setPaddingRelativeAdapt(int start, int top, int end, int bottom) {
        if (mHelper.canUseAdapt()) {
            super.setPaddingRelative(mHelper.getRealSizeInt(this, start), mHelper.getRealSizeInt(this, top), mHelper.getRealSizeInt(this, end), mHelper.getRealSizeInt(this, bottom));
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(NestedScrollView.LayoutParams.MATCH_PARENT, NestedScrollView.LayoutParams.MATCH_PARENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof LayoutParams) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return new LayoutParams((LayoutParams) lp);
            }
        } else if (lp instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) lp);
        }
        return new LayoutParams(lp);
    }

    public static class LayoutParams extends NestedScrollView.LayoutParams implements LayoutAdaptHelper.LayoutAdaptParams {
        private LayoutParamsInfo info;

        public LayoutParams(@NonNull Context c, @Nullable AttributeSet attrs) {
            super(c, attrs);
            getLayoutAdaptInfo().getAttributeSet(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public LayoutParams(NestedScrollView.LayoutParams source) {
            super(source);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public LayoutParams(LayoutParams source) {
            super(source);
            info = source.info;
        }

        @Override
        public LayoutParamsInfo getLayoutAdaptInfo() {
            if (info == null) {
                info = new LayoutParamsInfo();
            }
            return info;
        }
    }
}
