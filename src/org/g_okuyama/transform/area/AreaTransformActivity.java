
package org.g_okuyama.transform.area;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AreaTransformActivity extends FragmentActivity {
    public static final String TAG = "AreaTransformer";
    private static final LatLng TOKYO = new LatLng(35.681382, 139.766084);

    private SupportMapFragment mMapFragment = null;
    private GoogleMap mMap = null;
    OverlayView mOverlay = null;
    Handler mHandler;

    //button
    Button mSearchBtn;
    Button mStartBtn;
    Button mCalBtn;
    Button mClearBtn;
    
    LinearLayout mSearchLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //�L�[�{�[�h�������ŕ\�������Ȃ�
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_area_transform);

        mHandler = new Handler();
        
        //�}�b�v�\���ݒ�
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentById(R.id.map);
        mMapFragment = (SupportMapFragment)f;

        if (mMapFragment == null) {
            mMapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
            .add(android.R.id.content, mMapFragment, "map_fragment")
            .commit();
        }

        //���O����ܓx�E�y�x�����p�̃��C�A�E�g
        mSearchLayout = (LinearLayout)findViewById(R.id.search_layout);
        mSearchBtn = (Button)findViewById(R.id.search);
        mSearchBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                //�ܓx�E�y�x���������ړ�
                searchMap();
            }
        });
        
        mStartBtn = (Button)findViewById(R.id.start);
        mStartBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                //�`��J�n
                startDrawing();
            }
        });
        
        mCalBtn = (Button)findViewById(R.id.cal);
        mCalBtn.setEnabled(false);
        mCalBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                //�`��ʐς̌v�Z
                calcurateArea();
            }
        });
        
        mClearBtn = (Button)findViewById(R.id.clear);
        mClearBtn.setEnabled(false);
        mClearBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mOverlay != null){
                    mOverlay.clearCanvas();
                }
            }
        });
    }
    
    /*�����񂩂�ܓx�E�y�x���������A�n�}����ړ�*/
    private void searchMap(){
        EditText view = (EditText)findViewById(R.id.area);
        String text = view.getText().toString();
        if(text.length() == 0){
            //0�����̓G���[
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(AreaTransformActivity.this)
                    .setTitle(R.string.notify_search_title)
                    .setMessage(R.string.notify_search_message)
                    .setPositiveButton(R.string.notify_search_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //�������Ȃ�
                            return;
                        }
                    })                            
                    .show();
                }
            });
            return;
        }
        
        //�\�t�g�L�[�{�[�h������
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        
        //�����񂩂�ܓx�E�y�x���Z�o
        Geocoder geocoder = new Geocoder(AreaTransformActivity.this, Locale.getDefault());
        try{
             List<Address> addressList = geocoder.getFromLocationName(text, 1);
             boolean isFound = true;
             if(addressList == null){
                 isFound = false;
             }
             else if(addressList.size() == 0){
                 isFound = false;
             }
             
             if(!isFound){
                 //������Ȃ������ꍇ
                 mHandler.post(new Runnable() {
                     @Override
                     public void run() {
                         Toast.makeText(
                                 AreaTransformActivity.this,
                                 R.string.toast_search_not_found,
                                 Toast.LENGTH_LONG)
                                 .show();
                     }
                 });
             }

             Address address = addressList.get(0);      
             double lat = address.getLatitude();
             double lng = address.getLongitude();

             //�Z�o�����ܓx�E�y�x�Ɉړ�
             CameraUpdate camera = CameraUpdateFactory
                     .newCameraPosition(new CameraPosition.Builder()
                     .target(new LatLng(lat, lng))
                     .zoom(15.0f).build());
             mMap.moveCamera(camera);
             
        }catch(IOException e){
            e.printStackTrace();
        }        
    }

    /*�`��J�n*/
    private void startDrawing(){
        mStartBtn.setEnabled(false);
        mCalBtn.setEnabled(true);
        mClearBtn.setEnabled(true);
        mSearchLayout.setVisibility(View.INVISIBLE);
        
        //�`��pView��ǉ�
        mOverlay = new OverlayView(this);
        mOverlay.setMap(mMap);
            
        FrameLayout frame = (FrameLayout)findViewById(R.id.frame);
        frame.addView(mOverlay, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    /*�`��ʐς̌v�Z*/
    private void calcurateArea(){
        //�͂�ł��Ȃ���΃G���[
        if(!mOverlay.isCircled()){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(AreaTransformActivity.this)
                    .setTitle(R.string.notify_circle_title)
                    .setMessage(R.string.notify_circle_message)
                    .setPositiveButton(R.string.notify_circle_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mOverlay.clearCanvas();
                        }
                    })
                    .show();
                    return;
                }
            });
            return;
        }
        
        mStartBtn.setEnabled(true);
        mCalBtn.setEnabled(false);
        mClearBtn.setEnabled(false);
        mSearchLayout.setVisibility(View.VISIBLE);

        //�ʐς��v�Z�A�\��
        calcurate();
        
        //�`����N���A
        mOverlay.clearCanvas();
        //�`��p��View���폜
        FrameLayout frame = (FrameLayout)findViewById(R.id.frame);
        frame.removeView(mOverlay);
    }
    
    private void calcurate(){
        //�ʐ�
        float area = mOverlay.getArea();
        Log.d(TAG, "area = " + area + "m2");
        float unit = area / 46755/*�����h�[��(m2)*/;
        TextView text = (TextView)findViewById(R.id.text);
        text.setText("�����h�[��" + unit + "��");

        float d = mOverlay.getDistance();
        TextView dist = (TextView)findViewById(R.id.dist);
        dist.setText("     ����" + d + "m");
        
        Intent intent = new Intent(this, DisplayActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void onResume(){
        super.onResume();
        
        if(mMap == null){
            mMap = mMapFragment.getMap();
            if(mMap == null){
                try {
                    MapsInitializer.initialize(this);
                } catch (GooglePlayServicesNotAvailableException e) {
                    return;
                }
            }

            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            UiSettings settings = mMap.getUiSettings();
            settings.setCompassEnabled(true);
            //settings.setScrollGesturesEnabled(false);
            //settings.setZoomControlsEnabled(false);
            //settings.setZoomGesturesEnabled(false);
            setDefaultLocation();
            //�}�[�J�����ݒn�Ɏ����Ă������Ƃ��͐ݒ肷��
            //mMap.setMyLocationEnabled(true);
            
            if(mOverlay != null){
                mOverlay.setMap(mMap);
            }
        }
    }
    
    /*�}�b�v�̃f�t�H���g�\���ʒu��ݒ�*/
    private void setDefaultLocation(){
        CameraUpdate camera = CameraUpdateFactory
                .newCameraPosition(new CameraPosition.Builder()
                .target(TOKYO)
                .zoom(15.0f).build());
        mMap.moveCamera(camera);
    }
}
