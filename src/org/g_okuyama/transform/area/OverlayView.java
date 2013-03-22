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
    private static final boolean DEBUG = false;
    
    Context mContext;
    
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
    private double mDistance = 0.0;
    //1�s�N�Z���̋���
    private float m1PxDist = 0.0f;
    //�ʐ�
    private double mArea = 0.0;
    //1�M�ŕ`����Ă��邩�̔���t���O
    private boolean mFlag = false;
    //�G���[�t���O(�G���[�_�C�A���O�\�������̂�OK��������Ȃ������Ƃ��p)
    private boolean mErrFlag = false;
    
    public OverlayView(GoogleMap map, Context context) {
        super(context);
        mContext = context;
        mMap = map;
        
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
        
        mProjection = mMap.getProjection();
        //1�s�N�Z���̋����𑪒肷��
        LatLng p1 = mProjection.fromScreenLocation(new Point(0, 0));
        Location l1 = new Location("p1");
        l1.setLatitude(p1.latitude);
        l1.setLongitude(p1.longitude);
        LatLng p2 = mProjection.fromScreenLocation(new Point(0, 1));
        Location l2 = new Location("p2");
        l2.setLatitude(p2.latitude);
        l2.setLongitude(p2.longitude);
        m1PxDist = l1.distanceTo(l2);
        if(DEBUG){
            Log.d(AreaTransformActivity.TAG, "distance = " + m1PxDist);
        }
    }
    
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        
        if(mFlag){
            //2�M�ȏ�ŕ`���ꂽ�ꍇ�̓G���[�Ƃ���
            ((AreaTransformActivity)mContext).displayCircleError2();
            mErrFlag = true;
        }

        //������
        mDistance = 0.0f;
        mArea = 0.0f;
        mFlag = true;
        //���݂̒n�}�\���ʒu���擾
        LatLng l = mProjection.fromScreenLocation(new Point((int)x, (int)y));
        //�n�߂̈ܓx�A�y�x���Z�o
        mStart = new Location("start");
        mStart.setLatitude(l.latitude);
        mStart.setLongitude(l.longitude);
        if(DEBUG){
            Log.d(AreaTransformActivity.TAG, "lat = " + l.latitude + " ,long = " + l.longitude);
        }
        mPrev = mStart;    
    }
    
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
            
            //���݂̈ܓx�A�y�x���Z�o
            LatLng l = mProjection.fromScreenLocation(new Point((int)x, (int)y));
            Location cur = new Location("current");
            cur.setLatitude(l.latitude);
            cur.setLongitude(l.longitude);
            if(DEBUG){
                Log.d(AreaTransformActivity.TAG, "cur_lat = " + l.latitude + " ,cur_long = " + l.longitude);
            }
            //���������Z
            mDistance += mPrev.distanceTo(cur);
            if(DEBUG){
                Log.d(AreaTransformActivity.TAG, "distance = " + mDistance + "m");
            }
            mPrev = cur;
        }        
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
        if(DEBUG){
            Log.d(AreaTransformActivity.TAG, "cur_lat = " + l.latitude + " ,cur_long = " + l.longitude);
        }
        //�ŏI�I�ȋ������Z�o
        mDistance += mPrev.distanceTo(mEnd);
        if(DEBUG){
            Log.d(AreaTransformActivity.TAG, "distance = " + mDistance + "m");
        }

        //�ʐόv�Z(�b��)
        mArea = (mDistance / 4) * (mDistance / 4);
        //->���ۂ́ABitmap�̃X�i�b�v�V���b�g����͂܂ꂽ�����̃h�b�g���𐔂��Čv�Z����B
        //Projection����ܓx�A�y�x�𓱂��A1�h�b�g�̋������Z�肷��B
        //�h�b�g�̑����Ƌ�������ʐς����߂�B
        
        //�n�_�ƏI�_�̋���
        float dist = mStart.distanceTo(mEnd);
        if(DEBUG){
            Log.d(AreaTransformActivity.TAG, "dist = " + dist);
        }
        //1�s�N�Z���̋�����100�{����Ă�����G���[�Ƃ���
        if(dist > (m1PxDist * 100)){
            ((AreaTransformActivity)mContext).displayCircleError();
            mErrFlag = true;
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
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
    }

    void clearCanvas(){
        Log.d(AreaTransformActivity.TAG, "clearCanvas");
        mCanvas.drawColor(0,Mode.CLEAR);
        invalidate();
        
        //������
        mDistance = 0.0f;
        mArea = 0.0f;
        //����t���O��߂�
        mFlag = false;
        mErrFlag = false;
    }
    
    double getArea(){
        //�G���[�̂Ƃ��͖ʐς�Ԃ��Ȃ��B���̏ꍇ�Ăяo�����Ōv���s�\�ƂȂ�B
        if(mErrFlag){
            return 0.0f;
        }

        return mArea;
    }
    
    double getDistance(){
        return mDistance;
    }
}
