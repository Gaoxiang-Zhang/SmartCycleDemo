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
 * DonutChart: showing the progress of cycling
 */
public class DonutChart extends View {

    private float currPerAngle = 0.0f, currAngle = 0.0f;
    private float arrPer[] = new float[]{20f, 30f, 10f, 40f};
    private final int colorVacant[] = new int []{
            R.color.green_primary_light, R.color.yellow_primary_light,
            R.color.red_primary_light, R.color.blue_primary_light
    };
    private final int colorPassed[] = new int[]{
            R.color.green_primary, R.color.yellow_primary, R.color.red_primary, R.color.blue_primary
    };

    private RectF mBound;
    private Paint mPaint;

    private int radiusOutside, radiusInside;
    private int pointerColor, pointerShadowColor;

    public DonutChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DonutChart(Context context){
        this(context, null);
    }

    public DonutChart(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomDonutView,
                defStyle, 0);
        int n = array.getIndexCount();
        for (int i = 0; i < n ; i++){
            int attr = array.getIndex(i);
            switch (attr){
                case R.styleable.CustomDonutView_outsideRadius:
                    radiusOutside = array.getDimensionPixelOffset(attr, (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.CustomDonutView_insideRadius:
                    radiusInside = array.getDimensionPixelOffset(attr, (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 90, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.CustomDonutView_pointerColor:
                    pointerColor = array.getColor(attr, getResources().getColor(R.color.primary));
                    break;
                case R.styleable.CustomDonutView_pointerShadowColor:
                    pointerColor = array.getColor(attr, getResources().getColor(R.color.primary_light));
                    break;
            }
        }
        array.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mBound = new RectF(0, 0, radiusOutside, radiusOutside);
    }

    @Override
    public void onDraw(Canvas canvas){

        super.onDraw(canvas);

        canvas.drawColor(getResources().getColor(R.color.white));

        float cirX = radiusOutside / 2;
        float cirY = radiusOutside / 2;

        float percentage;
        float currPer = 270.0f;
        for (int i = 0 ; i < arrPer.length ; i++){
            percentage = 360 * (arrPer[i] / 100);
            percentage = (float)(Math.round(percentage * 100)) / 100;
            if(currPer - 270 < currAngle && currPer + percentage - 270 < currAngle){
                mPaint.setColor(getResources().getColor(colorPassed[i]));
                canvas.drawArc(mBound, currPer, percentage, true, mPaint);
            }
            else if(currPer - 270 < currAngle && currPer + percentage - 270 >= currAngle){
                mPaint.setColor(getResources().getColor(colorPassed[i]));
                canvas.drawArc(mBound, currPer, currAngle + 270 - currPer, true, mPaint);
                mPaint.setColor(getResources().getColor(colorVacant[i]));
                canvas.drawArc(mBound, currAngle + 270, currPer+ percentage - 270 - currAngle, true, mPaint);
            }
            else{
                mPaint.setColor(getResources().getColor(colorVacant[i]));
                // outside recF, startAngle, sweepAngle, use center( in sector shape ), paint
                canvas.drawArc(mBound, currPer, percentage, true, mPaint);
            }
            currPer += percentage;
        }

        // center background
        mPaint.setColor(getResources().getColor(R.color.white));
        canvas.drawCircle(cirX, cirY, radiusInside, mPaint);

        // pointer base shadow
        mPaint.setColor(getResources().getColor(R.color.primary_light));
        canvas.drawCircle(cirX, cirY, 25, mPaint);

        // pointer base color
        mPaint.setColor(getResources().getColor(R.color.primary));
        canvas.drawCircle(cirX, cirY, 20, mPaint);

        // pointer line
        mPaint.setStrokeWidth((float)5.0);
        // the base point of rotation is (0,0)
        canvas.translate(cirX, cirY);
        canvas.rotate(currAngle);
        canvas.drawLine(0, 0, 0, -cirY, mPaint);
        //canvas.rotate(0-currAngle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width, height;
        if(widthMode == MeasureSpec.EXACTLY){
            width = widthSize;
        }
        else {
            width = (int)(getPaddingLeft() + radiusOutside + getPaddingRight());
        }
        if(heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        }
        else{
            height = (int)(getPaddingTop() + radiusOutside + getPaddingBottom());
        }
        setMeasuredDimension(width, height);
    }

    // set the quota for each part
    public void setQuota(float[] quota){
        float total = 0.0f;
        for (int i = 0 ; i < arrPer.length ; i++){
            arrPer[i] = quota[i];
            total += arrPer[i];
        }
        for (int i = 0 ; i < arrPer.length ; i++){
            arrPer[i] = arrPer[i] / total * 100;
        }
        // currPerAngle is the angle to be rotated per second
        currPerAngle = 360 / total;
        invalidate();
    }

    // update the view when receive the timer broadcast
    public void setCurrentValue(){
        currAngle += currPerAngle;
        invalidate();
    }


}
