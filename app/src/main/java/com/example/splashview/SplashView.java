package com.example.splashview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;

public class SplashView extends View {
    /**
     * |
     * 旋转圆的画笔
     */
    private Paint mPaint;
    //扩散圆的画笔
    private Paint mHolePaint;
    //属性动画
    private ValueAnimator mValueAnimator;
    //背景色
    private int mBackgroundColor = Color.WHITE;
    private int[] mCircleColors;

    //表示旋转圆的中心
    private float mCenterX;
    private float mCenterY;
    //表示斜对角线长度的一半，扩散圆最大半径
    private float mDistance;

    //6个小圆的半径
    private float mCircleRadius = 18;
    //旋转大圆的半径
    private float mRotateRadius = 90;
    //当前大圆的旋转角度
    private float mCurrentRotateAngle = 0f;
    //当前大圆的半径
    private float mCurrentRotateRadius = mRotateRadius;
    //扩散圆的半径
    private float mCurrentHoleRadius = 0f;
    //旋转动画的时长
    private int mRotateDuration = 1200;

    private SplashState mState;

    public SplashView(Context context) {
        this(context, null);
    }

    public SplashView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SplashView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHolePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHolePaint.setStyle(Paint.Style.STROKE);
        mHolePaint.setColor(mBackgroundColor);

        mCircleColors = context.getResources().getIntArray(R.array.splash_circle_colors);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mCenterX = w * 1f / 2;
        this.mCenterY = h * 1f / 2;
        this.mDistance = (float) (Math.hypot(w, h) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mState == null) {
            mState = new RotateState();
        }
        mState.drawSate(canvas);
    }

    private abstract class SplashState {
        abstract void drawSate(Canvas canvas);
    }

    //1旋转
    private class RotateState extends SplashState {

        private RotateState() {
            mValueAnimator = ValueAnimator.ofFloat(0, (float) (Math.PI * 2));
            mValueAnimator.setRepeatCount(2);
            mValueAnimator.setDuration(mRotateDuration);
            mValueAnimator.setInterpolator(new LinearInterpolator());
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Log.e("xkff", animation.getAnimatedValue() + "===");
                    mCurrentRotateAngle = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mValueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mState = new MerginState();
                }
            });
            mValueAnimator.start();
        }

        @Override
        void drawSate(Canvas canvas) {
            //绘制背景
            drawBackground(canvas);
            //绘制6个小球
            drawCircles(canvas);
        }
    }

    private void drawCircles(Canvas canvas) {
        float rotateAngle = (float) (Math.PI * 2 / mCircleColors.length);
        for (int i = 0; i < mCircleColors.length; i++) {
            //x=r*cos(a)+centX
            //y=r*sin(a)+centY
            float angle = i * rotateAngle + mCurrentRotateAngle;
            float cx = (float) (Math.cos(angle) * mCurrentRotateRadius + mCenterX);
            float cy = (float) (Math.sin(angle) * mCurrentRotateRadius + mCenterY);
            mPaint.setColor(mCircleColors[i]);
            canvas.drawCircle(cx, cy, mCircleRadius, mPaint);

        }
    }

    private void drawBackground(Canvas canvas) {
        if (mCurrentHoleRadius > 0) {
            //绘制一个空心圆
            float strokeWidth = mDistance - mCurrentHoleRadius;
            float radius = strokeWidth / 2 + mCurrentHoleRadius;
            mHolePaint.setStrokeWidth(strokeWidth);
            canvas.drawCircle(mCenterX,mCenterY,radius,mHolePaint);
        } else {
            canvas.drawColor(mBackgroundColor);
        }
    }

    //2扩散聚合

    private class MerginState extends SplashState {
        private MerginState() {
            mValueAnimator = ValueAnimator.ofFloat(mCircleRadius, mRotateRadius);
            //mValueAnimator.setRepeatCount(2);
            mValueAnimator.setDuration(mRotateDuration);
            mValueAnimator.setInterpolator(new OvershootInterpolator(10f));
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Log.e("xkff", animation.getAnimatedValue() + "===");
                    mCurrentRotateRadius = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mValueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mState = new ExpandState();
                }
            });
            //mValueAnimator.start();
            //反向执行
            mValueAnimator.reverse();
        }

        @Override
        void drawSate(Canvas canvas) {
            drawBackground(canvas);
            drawCircles(canvas);
        }
    }

    //3水波纹效果

    private class ExpandState extends SplashState {
        public ExpandState() {
            mValueAnimator = ValueAnimator.ofFloat(mCircleRadius, mDistance);
            //mValueAnimator.setRepeatCount(2);
            mValueAnimator.setDuration(mRotateDuration);
            mValueAnimator.setInterpolator(new LinearInterpolator());
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Log.e("xkff", animation.getAnimatedValue() + "===");
                    mCurrentHoleRadius = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });

            mValueAnimator.start();
        }

        @Override
        void drawSate(Canvas canvas) {
            drawBackground(canvas);

        }
    }
}
