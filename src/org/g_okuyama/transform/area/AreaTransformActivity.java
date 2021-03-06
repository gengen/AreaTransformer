
package org.g_okuyama.transform.area;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.ad_stir.AdstirTerminate;
import com.ad_stir.AdstirView;
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
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AreaTransformActivity extends FragmentActivity {
    public static final String TAG = "AreaTransformer";
    private static final boolean DEBUG = false;
    private static final LatLng TOKYO = new LatLng(35.681382, 139.766084);

    private SupportMapFragment mMapFragment = null;
    private GoogleMap mMap = null;
    OverlayView mOverlay = null;
    Handler mHandler;

    //button
    Button mSearchBtn;
    Button mStartCalcBtn;
    ImageButton mClearBtn;
    ImageButton mBackBtn;
    
    //検索用レイアウト
    LinearLayout mSearchLayout;
    //モードフラグ(0:地図モード、1:描画モード)
    int mMode = 0;
    
    AdstirView mAdstirView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //キーボードを自動で表示させない
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_area_transform);

        mHandler = new Handler();
        
        //マップ表示設定
        FragmentManager fm = getSupportFragmentManager();
        Fragment f = fm.findFragmentById(R.id.map);
        mMapFragment = (SupportMapFragment)f;

        if (mMapFragment == null) {
            mMapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
            .add(android.R.id.content, mMapFragment, "map_fragment")
            .commit();
        }

        //名前から緯度・軽度検索用のレイアウト
        mSearchLayout = (LinearLayout)findViewById(R.id.search_layout);
        mSearchBtn = (Button)findViewById(R.id.search);
        mSearchBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                //緯度・軽度を検索し移動
                searchMap();
            }
        });
        
        mStartCalcBtn = (Button)findViewById(R.id.start_and_cal);
        mStartCalcBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mMode == 0){
                    //描画開始
                    startDrawing();
                    mMode = 1;
                }
                else{
                    //描画面積の計算
                    calcurateArea();
                    mMode = 0;
                }
            }
        });
        
        mClearBtn = (ImageButton)findViewById(R.id.clear);
        //mClearBtn.setVisibility(View.INVISIBLE);
        mClearBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mOverlay != null){
                    mOverlay.clearCanvas();
                }
            }
        });
        
        mBackBtn = (ImageButton)findViewById(R.id.back);
        //mBackBtn.setVisibility(View.INVISIBLE);
        mBackBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mOverlay != null){
                    mOverlay.clearCanvas();
                    //描画用のViewを削除
                    FrameLayout frame = (FrameLayout)findViewById(R.id.frame);
                    frame.removeView(mOverlay);
                    mOverlay = null;
                }
                
                mStartCalcBtn.setText(R.string.button_circle);
                //ボタンのアイコンを設定する
                Drawable icon = getResources().getDrawable(android.R.drawable.ic_menu_edit);
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                mStartCalcBtn.setCompoundDrawables(icon, null, null, null);
                mClearBtn.setVisibility(View.INVISIBLE);
                mBackBtn.setVisibility(View.INVISIBLE);
                mSearchLayout.setVisibility(View.VISIBLE);
                mAdstirView.setVisibility(View.VISIBLE);
                mMode = 0;
            }
        });
    }
    
    /*文字列から緯度・軽度を検索し、地図上を移動*/
    private void searchMap(){
        EditText view = (EditText)findViewById(R.id.area);
        String text = view.getText().toString();
        if(text.length() == 0){
            //0文字はエラー
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(AreaTransformActivity.this)
                    .setTitle(R.string.notify_search_title)
                    .setMessage(R.string.notify_search_message)
                    .setPositiveButton(R.string.notify_search_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //何もしない
                            return;
                        }
                    })                            
                    .show();
                }
            });
            return;
        }
        
        //ソフトキーボードを消す
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        
        //文字列から緯度・軽度を算出
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
                 //見つからなかった場合
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

             //算出した緯度・軽度に移動
             CameraUpdate camera = CameraUpdateFactory
                     .newCameraPosition(new CameraPosition.Builder()
                     .target(new LatLng(lat, lng))
                     .zoom(15.0f).build());
             mMap.moveCamera(camera);
             
        }catch(IOException e){
            e.printStackTrace();
        }        
    }

    /*描画開始*/
    private void startDrawing(){
        mStartCalcBtn.setText(R.string.button_calc);
        //ボタンのアイコンを消す
        //mStartCalcBtn.setCompoundDrawables(null, null, null, null);
        //ボタンのアイコンを設定する
        Drawable icon = getResources().getDrawable(android.R.drawable.ic_menu_set_as);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        mStartCalcBtn.setCompoundDrawables(icon, null, null, null);
        mClearBtn.setVisibility(View.VISIBLE);
        mBackBtn.setVisibility(View.VISIBLE);
        mSearchLayout.setVisibility(View.INVISIBLE);
        mAdstirView.setVisibility(View.INVISIBLE);
        
        //描画用Viewを追加
        mOverlay = new OverlayView(mMap, this);
            
        FrameLayout frame = (FrameLayout)findViewById(R.id.frame);
        frame.addView(mOverlay, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    /*描画面積の計算*/
    private void calcurateArea(){
        //面積を計算、表示
        double area = mOverlay.getArea();
        if(DEBUG){
            Log.d(TAG, "area = " + area + "m2");
        }
        
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("area", area);
        startActivity(intent);

        //move to onResume
        /*
        mStartCalcBtn.setText(R.string.button_circle);
        Drawable icon = getResources().getDrawable(android.R.drawable.ic_menu_edit);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        mStartCalcBtn.setCompoundDrawables(icon, null, null, null);
        mClearBtn.setVisibility(View.INVISIBLE);
        mBackBtn.setVisibility(View.INVISIBLE);
        mSearchLayout.setVisibility(View.VISIBLE);
        mAdstirView.setVisibility(View.VISIBLE);
        */
    }
    
    @Override
    protected void onResume(){
        super.onResume();
        
        if(mOverlay != null){
            mOverlay.clearCanvas();
            //描画用のViewを削除
            FrameLayout frame = (FrameLayout)findViewById(R.id.frame);
            frame.removeView(mOverlay);
            mOverlay = null;
        }
        
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
            settings.setZoomControlsEnabled(false);
            setDefaultLocation();
            //マーカを現在地に持ってきたいときは設定する
            //mMap.setMyLocationEnabled(true);
        }
        
        //広告設定
        if(mAdstirView == null){
            mAdstirView = new AdstirView(this, "MEDIA-d9327c32", 1);
            LinearLayout layout = (LinearLayout)findViewById(R.id.adspace);
            layout.addView(mAdstirView);
        }
        
        mStartCalcBtn.setText(R.string.button_circle);
        Drawable icon = getResources().getDrawable(android.R.drawable.ic_menu_edit);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        mStartCalcBtn.setCompoundDrawables(icon, null, null, null);
        mClearBtn.setVisibility(View.INVISIBLE);
        mBackBtn.setVisibility(View.INVISIBLE);
        mSearchLayout.setVisibility(View.VISIBLE);
        mAdstirView.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void onPause(){
        super.onPause();
        
        AdstirTerminate.init(this);
        
        //アプリのキャッシュ削除
        deleteCache(getCacheDir());
    }
    
    public void onConfigurationChanged(Configuration newConfig){
    	super.onConfigurationChanged(newConfig);
    	
    	if(DEBUG){
    		Log.d(TAG, "onConfigurationChanged");
    	}
    	
    	if(mOverlay != null){
    		mOverlay.clearCanvas();
    	}
    }
    
    /*マップのデフォルト表示位置を設定*/
    private void setDefaultLocation(){
        CameraUpdate camera = CameraUpdateFactory
                .newCameraPosition(new CameraPosition.Builder()
                .target(TOKYO)
                .zoom(15.0f).build());
        mMap.moveCamera(camera);
    }
    
    /*囲んでないときのエラー表示*/
    void displayCircleError(){
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
    }

    /*1筆で囲んでないときのエラー表示*/
    void displayCircleError2(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(AreaTransformActivity.this)
                .setTitle(R.string.notify_circle_title)
                .setMessage(R.string.notify_circle_message2)
                .setPositiveButton(R.string.notify_circle_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mOverlay.clearCanvas();
                    }
                })
                .show();
                return;
            }
        });
    }
    
    //キャッシュの削除
    public boolean deleteCache(File dir) {
        if(dir==null) {
            return false;
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteCache(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
    
    public void finish(){
    	System.exit(RESULT_OK);
    }
}
