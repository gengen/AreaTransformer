
package org.g_okuyama.transform.area;

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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AreaTransformActivity extends FragmentActivity {
    public static final String TAG = "AreaTransformer";
    private static final LatLng TOKYO = new LatLng(35.681382, 139.766084);

    private SupportMapFragment mMapFragment = null;
    private GoogleMap mMap = null;
    Context mContext = null;
    OverlayView mOverlay = null;
    
    //button
    Button mStartBtn;
    Button mCalBtn;
    Button mClearBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_area_transform);
        mContext = getApplicationContext();
        
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentById(R.id.map);
        mMapFragment = (SupportMapFragment)f;

        if (mMapFragment == null) {
            mMapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
            .add(android.R.id.content, mMapFragment, "map_fragment")
            .commit();
        }
        
        //オーバレイビューがイベントを受け取らないようにする
        mOverlay = (OverlayView)findViewById(R.id.view);
        mOverlay.setVisibility(View.GONE);
        
        mStartBtn = (Button)findViewById(R.id.start);
        mStartBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                mStartBtn.setEnabled(false);
                mCalBtn.setEnabled(true);
                mClearBtn.setEnabled(true);
                mOverlay.setVisibility(View.VISIBLE);
                /*これを入れてXperiaでどうか。。*/
                mOverlay.bringToFront();
            }
        });
        
        mCalBtn = (Button)findViewById(R.id.cal);
        mCalBtn.setEnabled(false);
        mCalBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                mStartBtn.setEnabled(true);
                mCalBtn.setEnabled(false);
                mClearBtn.setEnabled(false);
                mOverlay.setVisibility(View.GONE);
                OverlayView view = (OverlayView)findViewById(R.id.view);
                
                //面積
                float area = view.getArea();
                Log.d(TAG, "area = " + area + "m2");
                float unit = area / 46755/*東京ドーム(m2)*/;
                TextView text = (TextView)findViewById(R.id.text);
                text.setText("東京ドーム" + unit + "個分");

                float d = view.getDistance();
                TextView dist = (TextView)findViewById(R.id.dist);
                dist.setText("     距離" + d + "m");
                
                //描画をクリア
                view.clearCanvas();
            }
        });
        
        mClearBtn = (Button)findViewById(R.id.clear);
        mClearBtn.setEnabled(false);
        mClearBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                OverlayView view = (OverlayView)findViewById(R.id.view);
                view.clearCanvas();
            }
        });
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
            //マーカを現在地に持ってきたいときは設定する
            //mMap.setMyLocationEnabled(true);
            
            if(mOverlay != null){
                mOverlay.setMap(mMap);
            }
        }
    }
    
    private void setDefaultLocation(){
        CameraUpdate camera = CameraUpdateFactory
                .newCameraPosition(new CameraPosition.Builder()
                .target(TOKYO)
                .zoom(15.0f).build());
        mMap.moveCamera(camera);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_area_transform, menu);
        return true;
    }

}
