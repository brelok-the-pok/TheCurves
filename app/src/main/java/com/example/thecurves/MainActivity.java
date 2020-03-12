package com.example.thecurves;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.util.AttributeSet;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity{
    DrawView view;
    EditText rankEdit;
    EditText accuracyEdit;
    Button clearCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.drawView2);
        rankEdit = findViewById(R.id.rankEdit);
        accuracyEdit = findViewById(R.id.accuracyEdit);
        clearCanvas = findViewById(R.id.clearCanvas);

        rankEdit.setText(String.valueOf(view.rank));
        accuracyEdit.setText(String.valueOf(view.curvePointCount));

        rankEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.toString().length() > 0){
                    int rank = Integer.parseInt(s.toString());
                    if( rank > 10){
                        rankEdit.setText("10");
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length() > 0){
                    view.rank = Integer.parseInt(s.toString());
                    view.refreshCurveOptions();
                }
            }
        });
        accuracyEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
/*                if(s.toString().length() > 1){
                    if( Integer.parseInt(s.toString()) < 100){
                        accuracyEdit.setText("100");
                    }
                }*/
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length() > 1){
                    if( Integer.parseInt(s.toString()) >= 10){
                        view.curvePointCount = Integer.parseInt(s.toString());
                        view.refreshCurveOptions();
                    }
                }
            }
        });

        clearCanvas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.refreshCurveOptions();
            }
        });
    }
}


class DrawView extends View implements View.OnTouchListener{

    static {
        System.loadLibrary("native-lib");
    }
    int curvePointCount = 100;
    int rank = 3;
    int currentPoint = 0;
    int nextCurvePoint = 0;

    Point[] refPoints = new Point[rank];
    Point[] curvePoints = new Point[curvePointCount + 2];
    Paint p = new Paint();

    boolean drawRefPoints = false;
    boolean drawCurve = false;
    boolean drawing = false;

    public DrawView(Context context) {
        super(context);
        initObject();
    }
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initObject();
    }
    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initObject();
    }
    private void initObject(){
        setFocusable(false);
        setFocusableInTouchMode(false);
        this.setOnTouchListener(this);
        requestLayout();
        for (int i = 0; i < rank; i++){
            refPoints[i] = new Point();
        }
        for (int i = 0; i < curvePointCount + 1; i++){
            curvePoints[i] = new Point();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.parseColor("#D6F9FC"));

        p.setColor(Color.rgb(187, 22, 27));
        p.setStrokeWidth(10);

        if(drawRefPoints){
            for (int i = 0; i < rank; i++) {
                canvas.drawPoint(refPoints[i].x, refPoints[i].y, p);
            }
        }
        if(drawCurve){
            for (int i = 0; i < nextCurvePoint; i++) {
                canvas.drawPoint(curvePoints[i].x, curvePoints[i].y, p);
            }
            nextCurvePoint++;

            if(nextCurvePoint + 2 == curvePointCount + 2){
                drawCurve = false;
                currentPoint = 0;
                nextCurvePoint = 0;
                for (int i = 0; i < rank; i++){
                    refPoints[i] = new Point();
                }

            }else{
                invalidate();
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        float x = event.getX();
        float y = event.getY();
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(!drawing){
                refPoints[currentPoint].setPoint(x, y);
                currentPoint++;
                drawRefPoints = true;
                invalidate();

                if(currentPoint == rank){
                    drawing = true;
                    drawRefPoints = false;
                    drawCurve = true;
                    calculateCurve();
                    invalidate();
                }
            }
            else{
                return  false;
            }
        }
        return true;
    }

    public  void calculateCurve(){
        float[] refX = new float[rank];
        float[] refY = new float[rank];
        for(int i = 0; i < rank; i++){
            refX[i] = refPoints[i].x;
            refY[i] = refPoints[i].y;
        }
        for(int i = 0; i < curvePointCount + 1; i++){
            float t = (float)i / curvePointCount;
            curvePoints[i].x = calculateCurvePoint(refX , t, rank - 1);
            curvePoints[i].y = calculateCurvePoint(refY , t, rank - 1);
        }
    }

    public void refreshCurveOptions(){
        refPoints = new Point[rank];
        for (int i = 0; i < rank; i++){
            refPoints[i] = new Point();
        }
        curvePoints = new Point[curvePointCount + 2];
        for (int i = 0; i < curvePointCount + 2; i++){
            curvePoints[i] = new Point();
        }
        currentPoint = 0;
        nextCurvePoint = 0;
        drawCurve = false;
        drawing = false;
        drawRefPoints = true;

        invalidate();
    }

    public native float calculateCurvePoint(float[]  points, float t, int rank);
}

class Point{
    public float x;
    public float y;

    Point(){
        x = 0;
        y = 0;
    }
    Point(float x, float y){
        this.x = x;
        this.y = y;
    }
    public void setPoint(float x, float y){
        this.x = x;
        this.y = y;
    }
}