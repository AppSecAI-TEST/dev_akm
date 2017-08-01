package com.zongsheng.drink.h17.background.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.zongsheng.drink.h17.R;

/**
 * 软件更新时的下载进度条
 * Created by goolong on 17/2/28.
 */

public class CircleProgressBar extends View {

    private Paint paint;
    private int color;
    private int lineWidth;
    private int progress;
    private int duration;
    private boolean useAnim;

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar, defStyleAttr, 0);
        color = array.getColor(R.styleable.CircleProgressBar_color, Color.BLACK);
        lineWidth = array.getDimensionPixelOffset(R.styleable.CircleProgressBar_lineWidth, 0);
        progress = array.getInt(R.styleable.CircleProgressBar_progress, 30);
        duration = array.getInt(R.styleable.CircleProgressBar_duration, 2000);
        useAnim = array.getBoolean(R.styleable.CircleProgressBar_useAnim, true);
        array.recycle();

        // 设置画笔
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setStrokeWidth(lineWidth);
    }

    private void throwException() {
        if (getWidth() != getHeight()) {
            throw new RuntimeException("width and height must be equal");
        }
        if (progress > 100) {
            throw new RuntimeException("progress can't be bigger than hundred");
        }
        if (progress < 0) {
            throw new RuntimeException("progress can't be smaller than zero");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        throwException();
        paint.setColor(Color.parseColor("#e9e9e9"));
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2 - lineWidth / 2, paint);
        paint.setColor(color);
        RectF oval = new RectF(lineWidth / 2, lineWidth / 2, getWidth() - lineWidth / 2, getHeight() - lineWidth / 2);
        canvas.drawArc(oval, -90, progress * 360 / 100, false, paint);
    }

    public void setProgress(int p) {
        this.progress = p;
        if (useAnim) {
            startAnim(p);
        } else {
            invalidate();
        }
    }

    private void startAnim(int p) {
        ValueAnimator animator = ValueAnimator.ofInt(0, p);
        animator.setDuration(duration);
        animator.start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progress = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

}
