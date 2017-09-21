package com.rain.library;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by HwanJ.Choi on 2017-9-21.
 */

public class CustomProgressBar extends View {

    private static final float DOUGHNUT_WIDTH_PROPORTION = 0.12f;//圆环宽度占view的比
    private static final float INNER_RADUIS_PROPORTION = 0.65f;//圆环半径占比
    private static final int DEFUALT_SWEEP_COLOR = Color.parseColor("#FF0E87D2");

    private int mSize;
    private float mRadius;

    private Paint mPaint;
    private Paint mDotPaint;
    private Paint mRipplePaint;

    private int mSweepColor;

    private Matrix mMatrix;

    private RectF mOveralRectF;

    private float mAnimValue;

    private SparseArray<Ripple> mRipples = new SparseArray<>();

    public CustomProgressBar(Context context) {
        this(context, null);
    }

    public CustomProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = getResources().obtainAttributes(attrs, R.styleable.CustomProgressBar);
        mSweepColor = typedArray.getColor(R.styleable.CustomProgressBar_sweepColor, DEFUALT_SWEEP_COLOR);
        typedArray.recycle();
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);

        mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDotPaint.setColor(mSweepColor);
        mDotPaint.setStyle(Paint.Style.FILL);

        mRipplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRipplePaint.setColor(mSweepColor);
        mRipplePaint.setStyle(Paint.Style.STROKE);
        mRipplePaint.setStrokeWidth(10);

        mMatrix = new Matrix();
        ValueAnimator rotateAnimator = ValueAnimator.ofInt(0, 360);
        rotateAnimator.setDuration(2500);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimator.setRepeatMode(ValueAnimator.RESTART);
        rotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int degrees = (int) animation.getAnimatedValue();
                mMatrix.reset();
                mMatrix.preTranslate(mSize / 2, mSize / 2);
                mMatrix.preRotate(-degrees);
                postInvalidate();
            }
        });

        ValueAnimator rippleAnimator = ValueAnimator.ofFloat(0, 1);
        rippleAnimator.setDuration(3000);
        rippleAnimator.setInterpolator(new LinearInterpolator());
        rippleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rippleAnimator.setRepeatMode(ValueAnimator.RESTART);
        rippleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimValue = (float) animation.getAnimatedValue();
                for (int i = 0; i < mRipples.size(); i++) {
                    int key = mRipples.keyAt(i);
                    final Ripple ripple = mRipples.get(key);
                    ripple.update(mAnimValue);
                }
            }
        });
        rippleAnimator.start();
        rotateAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.concat(mMatrix);
        float doughnutWidth = mSize * DOUGHNUT_WIDTH_PROPORTION;//圆环宽度
        canvas.drawArc(mOveralRectF, 0, 360, false, mPaint);//绘制圆环
        //绘制圆环头
        canvas.drawCircle(mRadius - doughnutWidth / 2, 0, doughnutWidth / 2, mDotPaint);
        drawRipple(canvas);//绘制波纹
    }

    private void drawRipple(Canvas canvas) {
        final int saveCount = canvas.getSaveCount();
        canvas.save();
        for (int i = 0; i < mRipples.size(); i++) {
            int key = mRipples.keyAt(i);
            final Ripple ripple = mRipples.get(key);
            ripple.draw(canvas);

        }
        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));
        setMeasuredDimension(size, size);
    }

    private int measure(int measureSpec) {
        int size = MeasureSpec.getSize(measureSpec);
        int mode = MeasureSpec.getMode(measureSpec);
        int result = 200;
        switch (mode) {
            case MeasureSpec.EXACTLY:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(size, result);
                break;
        }
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mSize = w;
        mRadius = INNER_RADUIS_PROPORTION * mSize / 2;
        float doughnutWidth = mSize * DOUGHNUT_WIDTH_PROPORTION;
        Shader shader = new SweepGradient(0, 0, getSweepColors(), null);
        mPaint.setShader(shader);
        mPaint.setStrokeWidth(doughnutWidth);//圆环宽度
        float innerR = mRadius - doughnutWidth / 2;
        mOveralRectF = new RectF(-innerR, -innerR, innerR, innerR);

        mRipples.clear();
        float distance = (mSize / 2 - mRadius);
        mRipples.put(0, new Ripple(mRadius, mSweepColor, distance, 0));
        mRipples.put(1, new Ripple(mRadius, mSweepColor, distance, 0.25f));
        mRipples.put(2, new Ripple(mRadius, mSweepColor, distance, 0.5f));
        mRipples.put(2, new Ripple(mRadius, mSweepColor, distance, 0.75f));
    }

    private int[] getSweepColors() {
        final int red = Color.red(mSweepColor);
        final int green = Color.green(mSweepColor);
        final int blue = Color.blue(mSweepColor);
        int[] result = new int[3];
        final int alpha = Color.alpha(mSweepColor);
        int minAlpha = (int) (alpha * 0.1);
        int midAlpha = (int) (alpha * 0.5);
        int colorMid = Color.argb(midAlpha, red, green, blue);
        int colorMin = Color.argb(minAlpha, red, green, blue);
        result[0] = mSweepColor;
        result[1] = colorMid;
        result[2] = colorMin;
        return result;
    }

    static class Ripple {

        private final float origin;//初始值
        private float mRadius;//当前的半径
        private Paint mPaint;
        private float distance;//最大半径到初始值
        private final float offset;//动画偏移值

        Ripple(float radius, int color, float distance, float startAnimOff) {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(5);
            this.distance = distance;
            this.origin = radius;
            mPaint.setColor(color);
            offset = startAnimOff;
        }

        public void draw(Canvas canvas) {
            canvas.drawCircle(0, 0, mRadius, mPaint);
        }

        public void update(float animValue) {//0~1
            //更新透明度,半径,画笔
            animValue = offset + animValue;
            if (animValue > 1) {
                animValue -= 1;
            }
            mPaint.setAlpha((int) ((1 - animValue) * 100));
            mRadius = origin + animValue * distance;
        }

    }
}
