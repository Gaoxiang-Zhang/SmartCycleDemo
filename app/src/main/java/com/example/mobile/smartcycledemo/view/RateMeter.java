package com.example.mobile.smartcycledemo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.example.mobile.smartcycledemo.R;

/**
 * RateMeter: gauge the heart rate
 */
public class RateMeter extends View {

    private float currentValue = 0, minBound = 100, maxBound = 150;

    private float centerX, centerY;

    private RectF mBound;
    private Paint mPaint;

    private int mRadius, arcPrimaryColor, arcSecondaryColor, textColor, valueColor, mWidth, padding;

    private final int meterStartAngle = 160;
    // max heart rate
    private final int MAX_VALUE = 220;
    // scale unit
    private final int SCALE_UNIT = 20;

    public RateMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RateMeter(Context context){
        this(context, null);
    }

    public RateMeter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomRateView, defStyle, 0);
        int n = array.getIndexCount();

        for(int i = 0; i < n; i++){
            int attr = array.getIndex(i);
            switch (attr){
                case R.styleable.CustomRateView_arcRadius:
                    mRadius = array.getDimensionPixelOffset(attr, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                            100, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.CustomRateView_arcPrimaryColor:
                    arcPrimaryColor = array.getColor(attr, getResources().getColor(R.color.primary));
                    break;
                case R.styleable.CustomRateView_arcSecondaryColor:
                    arcSecondaryColor = array.getColor(attr, getResources().getColor(R.color.primary_light));
                    break;
                case R.styleable.CustomRateView_textColor:
                    textColor = array.getColor(attr, getResources().getColor(R.color.primary_text));
                    break;
                case R.styleable.CustomRateView_valueColor:
                    valueColor = array.getColor(attr, getResources().getColor(R.color.primary_text));
                    break;
                case R.styleable.CustomRateView_arcWidth:
                    mWidth = array.getDimensionPixelOffset(attr, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                            10, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.CustomRateView_padding:
                    padding = array.getDimensionPixelOffset(attr, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                            0, getResources().getDisplayMetrics()));
            }
        }
        array.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mBound = new RectF(padding, padding, mRadius + padding, mRadius + padding);

        centerX = mRadius / 2 + padding;
        centerY = mRadius / 2 + padding;
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        // draw the scale
        drawScale(canvas);
        drawCurrentValue(canvas);
    }

    /**
     * drawScale: draw the scale
     */
    private void drawScale(Canvas canvas){
        float scaleSize = getResources().getDisplayMetrics().density * 16;
        float textSize = getResources().getDisplayMetrics().scaledDensity * 10;
        float titleOffset = mRadius /  6;
        // set the color of the scale line and text
        mPaint.setColor(textColor);
        // set the stroke width of scale line
        mPaint.setStrokeWidth(2);
        // set the text size of scale text
        mPaint.setTextSize(textSize);
        float textX, textY;
        int radius = mRadius / 2;
        // mark every SCALE_UNIT
        for(int i = 0 ; i <= MAX_VALUE; i += SCALE_UNIT){
            // calculate the angle
            double angle = Math.toRadians(meterStartAngle + i + 90);
            // draw the scale line
            float startX = (float)(centerX + (radius - scaleSize) * Math.sin(angle)), startY = (float)(centerY - (radius - scaleSize) * Math.cos(angle));
            float endX = (float)(centerX + (radius - scaleSize + mWidth) * Math.sin(angle)),
                    endY = (float)(centerY - (radius - scaleSize +mWidth) * Math.cos(angle));
            canvas.drawLine(startX, startY, endX, endY, mPaint);
            // draw the text
            String string = i+"";
            textX = startX - mPaint.measureText(string, 0, string.length()) / 2;
            textY = startY - (mPaint.descent() + mPaint.ascent()) / 2;
            canvas.drawText(i + "", textX, textY, mPaint);
        }
        // draw the gauge title
        String title = getResources().getString(R.string.current_rate);
        textSize = getResources().getDisplayMetrics().scaledDensity * 12;
        mPaint.setTextSize(textSize);
        mPaint.setColor(getResources().getColor(R.color.primary_text));
        canvas.drawText(title, centerX - mPaint.measureText(title, 0, title.length()) / 2, centerY - titleOffset, mPaint);
    }

    /**
     * drawPointer: draw the pointer and static text of gauge meter
     */
    private void drawCurrentValue(Canvas canvas){

        int accent_color = getResources().getColor(R.color.background);
        float textSize = getResources().getDisplayMetrics().scaledDensity * 32;

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mWidth);

        // draw the current value indicated by meter color
        if(currentValue < minBound){
            // first part is primary
            mPaint.setColor(arcPrimaryColor);
            canvas.drawArc(mBound, meterStartAngle, currentValue, false, mPaint );
            // second part is accent
            mPaint.setColor(accent_color);
            canvas.drawArc(mBound, meterStartAngle + currentValue, minBound - currentValue, false, mPaint );
            // third part is second primary
            mPaint.setColor(arcSecondaryColor);
            canvas.drawArc(mBound, meterStartAngle + minBound, (maxBound - minBound), false, mPaint);
            // draw the third part o graph
            mPaint.setColor(accent_color);
            canvas.drawArc(mBound, meterStartAngle + maxBound, (MAX_VALUE - maxBound), false, mPaint);
        }
        else if(currentValue >= minBound && currentValue <= maxBound){
            // first part is primary
            mPaint.setColor(arcPrimaryColor);
            canvas.drawArc(mBound, meterStartAngle, currentValue, false, mPaint );
            // third part is second primary
            mPaint.setColor(arcSecondaryColor);
            canvas.drawArc(mBound, meterStartAngle + currentValue, (maxBound - minBound), false, mPaint);
            // draw the third part o graph
            mPaint.setColor(accent_color);
            canvas.drawArc(mBound, meterStartAngle + maxBound, (MAX_VALUE - maxBound), false, mPaint);
        }
        else if(currentValue <= MAX_VALUE){
            // first part is primary
            mPaint.setColor(arcPrimaryColor);
            canvas.drawArc(mBound, meterStartAngle, currentValue, false, mPaint );
            // draw the third part o graph
            mPaint.setColor(accent_color);
            canvas.drawArc(mBound, meterStartAngle + currentValue, (MAX_VALUE - currentValue), false, mPaint);
        }
        else{
            // first part is primary
            mPaint.setColor(arcPrimaryColor);
            canvas.drawArc(mBound, meterStartAngle, MAX_VALUE, false, mPaint );
        }

        // draw the current value indicated by Text
        String result;
        mPaint.setColor(valueColor);
        mPaint.setTextSize(textSize);
        mPaint.setStyle(Paint.Style.FILL);
        result = currentValue + "";
        canvas.drawText(currentValue + "", centerX - mPaint.measureText(result, 0, result.length()) / 2 ,
                centerY - (mPaint.descent() + mPaint.ascent()) / 2, mPaint);

    }


    /**
     * onMeasure: to fit the param of "wrap_content"
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width, height;
        if(widthMode == MeasureSpec.EXACTLY){
            width = widthSize;
        }
        else{
            width = padding + mRadius + padding;
        }
        if(heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        }
        else{
            float angle = (float)(Math.toRadians((MAX_VALUE - 180) / 2));
            float extra_height = (float)Math.sin(angle) * mRadius / 2;
            height = (int)((padding + mRadius + padding) / 2 + extra_height);
        }
        setMeasuredDimension(width, height);
    }

    /**
     * setInitValue: set the upper and lower bound of heart rate
     */
    public void setInitValue(int lowerBound, int upperBound){
        minBound = lowerBound;
        maxBound = upperBound;
        invalidate();
    }

    /**
     * setCurrentValue: set current heart rate
     */
    public void setCurrentValue(int value){
        currentValue = value;
        invalidate();
    }



}
