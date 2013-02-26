package org.g_okuyama.transform.area;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.location.Location;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class OverlayView extends View {
    Paint mPaint;
    
    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Path    mPath;
    private Paint   mBitmapPaint;

    private GoogleMap mMap;
    //�n�_
    private Location mStart;
    //�I�_
    private Location mEnd;
    //�O��̈ʒu
    private Location mPrev;
    private Projection mProjection;
    //����
    private float mDistance = 0.0f;
    //�ʐ�
    private float mArea = 0.0f;
    
    Handler mHandler;
    Context mContext;
    
    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);
        
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        
        mHandler = new Handler();
    }
    
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;

        //������
        mDistance = 0.0f;
        mArea = 0.0f;
        //���݂̒n�}�\���ʒu���擾
        mProjection = mMap.getProjection();
        LatLng l = mProjection.fromScreenLocation(new Point((int)x, (int)y));
        //�n�߂̈ܓx�A�y�x���Z�o
        mStart = new Location("start");
        mStart.setLatitude(l.latitude);
        mStart.setLongitude(l.longitude);
        Log.d(AreaTransformActivity.TAG, "lat = " + l.latitude + " ,long = " + l.longitude);
        mPrev = mStart;    
    }
    
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
        
        //���݂̈ܓx�A�y�x���Z�o
        LatLng l = mProjection.fromScreenLocation(new Point((int)x, (int)y));
        Location cur = new Location("current");
        cur.setLatitude(l.latitude);
        cur.setLongitude(l.longitude);
        Log.d(AreaTransformActivity.TAG, "cur_lat = " + l.latitude + " ,cur_long = " + l.longitude);
        //���������Z
        mDistance += mPrev.distanceTo(cur);
        Log.d(AreaTransformActivity.TAG, "distance = " + mDistance + "m");
        mPrev = cur;
    }
    
    private void touch_up(float x, float y) {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
        
        //�I�_�̈ܓx�E�y�x���Z�o
        LatLng l = mProjection.fromScreenLocation(new Point((int)x, (int)y));
        mEnd = new Location("end");
        mEnd.setLatitude(l.latitude);
        mEnd.setLongitude(l.longitude);
        Log.d(AreaTransformActivity.TAG, "cur_lat = " + l.latitude + " ,cur_long = " + l.longitude);
        //�ŏI�I�ȋ������Z�o
        mDistance += mPrev.distanceTo(mEnd);
        Log.d(AreaTransformActivity.TAG, "distance = " + mDistance + "m");
        //�ʐόv�Z(�b��)
        mArea = (mDistance / 4) * (mDistance / 4);
        
        //�n�_�ƏI�_�̋��������ȏ㗣��Ă���ꍇ�̓A���[�g���o��
        float dist = mStart.distanceTo(mEnd);
        if(dist > 1000){//1km
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(mContext)
                    .setTitle("�ʒm")
                    .setMessage("�͂�ł�������")
                    .setPositiveButton("�͂�", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            clearCanvas();
                        }
                    })
                    .show();
                    return;
                }
            });
        }
    }

    public boolean onTouchEvent(MotionEvent event){
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up(x, y);
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }
    
    @Override
    protected void onDraw(Canvas canvas){
    	super.onDraw(canvas);
        //canvas.drawColor(0xFFAAAAAA);
    	
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    void clearCanvas(){
        Log.d(AreaTransformActivity.TAG, "clearCanvas");
        mCanvas.drawColor(0,Mode.CLEAR);
        invalidate();
        
        //������
        mDistance = 0.0f;
        mArea = 0.0f;
    }
    
    void setMap(GoogleMap map){
        mMap = map;
    }
    
    float getArea(){
        return mArea;
    }
    
    float getDistance(){
        return mDistance;
    }
}
