package com.rain.library;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by HwanJ.Choi on 2017-9-21.
 */

public class CustomProgressBar extends View {

    private static final float DOUGHNUT_WIDTH_PROPORTION = 0.12f;

    private int mSize;
    private int mRadius;

    private Paint mPaint;
    private Paint mDotPaint;

    private int mSweepColor;

    private Matrix mMatrix;

    private RectF mOveralRectF;

    public CustomProgressBar(Context context) {
        this(context, null);
    }

    public CustomProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mSweepColor = Color.parseColor("#FF0E87D2");
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);

        mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDotPaint.setColor(mSweepColor);
        mDotPaint.setStyle(Paint.Style.FILL);
        mMatrix = new Matrix();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 360);
        valueAnimator.setDuration(2500);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int degrees = (int) animation.getAnimatedValue();
                mMatrix.reset();
                mMatrix.preTranslate(mRadius, mRadius);
                mMatrix.preRotate(-degrees);
                postInvalidate();
            }
        });
        valueAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.concat(mMatrix);
        float doughnutWidth = mSize * DOUGHNUT_WIDTH_PROPORTION;
        canvas.drawArc(mOveralRectF, 0, 360, false, mPaint);
        //
        canvas.drawCircle(mRadius - doughnutWidth / 2, 0, doughnutWidth / 2, mDotPaint);
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
        mRadius = mSize / 2;
        float doughnutWidth = mSize * DOUGHNUT_WIDTH_PROPORTION;
        Shader shader = new SweepGradient(0, 0, getSweepColors(), null);
        mPaint.setShader(shader);
        mPaint.setStrokeWidth(doughnutWidth);//圆环宽度
        float innerR = mRadius - doughnutWidth / 2;
        mOveralRectF = new RectF(-innerR, -innerR, innerR, innerR);
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

}
