package org.g_okuyama.transform.area;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FixedResolutionPolicy;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class ResultActivity extends MultiSceneActivity {
    //private int CAMERA_WIDTH = 480;
    //private int CAMERA_HEIGHT = 800;
    private int CAMERA_WIDTH = 300;
	private int CAMERA_HEIGHT = 370;
	
	private ProgressDialog mDialog; 

    float mArea = 0.0f;
    String mResult = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setContentView(R.layout.activity_display);
        
        Bundle extras = getIntent().getExtras();
        mArea = extras.getFloat("area", 0.0f);

        showDialog();
    }
    
    private void showDialog(){
        //プログレスダイアログ表示
        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getString(R.string.notify_progress_message));
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.show(); 
    }
    
    public void dismissDialog(){
        if(mDialog != null){
            mDialog.dismiss();
            mDialog = null;
        }
    }
    
    @Override
    protected void onResume(){
        super.onResume();
        
        calcurate();
    }
    
    void calcurate(){
        String setStr = "";
        
        //TextView text = (TextView)findViewById(R.id.unit);
        //TextView area = (TextView)findViewById(R.id.area);
        
        float unit = mArea / 46755; //東京ドーム(m2)
        //大きさによって表示桁を変える
        if(unit >= 100.0){
            //小数点は表示しない
            int i = Math.round(unit);
            setStr = String.valueOf(i);
        }
        else if(unit >= 1.0){
            //小数点1桁まで表示
            float tmp = unit * 10;
            int i = Math.round(tmp);
            float ret = (float)i / 10;
            setStr = String.valueOf(ret);
        }
        else if(unit < 1.0 && unit >= 0.01){
            //小数点2桁まで表示
            float tmp = unit * 100;
            int i = Math.round(tmp);
            float ret = (float)i / 100;
            setStr = String.valueOf(ret);            
        }
        else if(unit < 0.01 && unit >= 0.001){
            //小数点3桁まで表示
            float tmp = unit * 1000;
            int i = Math.round(tmp);
            float ret = (float)i / 1000;
            setStr = String.valueOf(ret);
        }
        else{
            //測定不能とする
            Log.d(AreaTransformActivity.TAG, "can\'t calcurate");
            //text.setVisibility(View.INVISIBLE);
            //area.setVisibility(View.INVISIBLE);
            
            dismissDialog();
            
            new AlertDialog.Builder(this)
            .setTitle(R.string.notify_calc_title)
            .setMessage(R.string.notify_calc_message)
            .setPositiveButton(R.string.notify_calc_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    return;
                }
            })                            
            .show();
            
            return;
        }
        
        //text.setText(setStr);
        //text.append("個分");        
        //area.setText("(" + String.valueOf(Math.round(mArea)) + " m2)");
        mResult = setStr;
    }
    
    @Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		//現在の端末の向きによってオリエンテーションを変える
		ScreenOrientation so = ScreenOrientation.PORTRAIT_FIXED;
		Configuration config = getResources().getConfiguration();
		if(config.orientation == Configuration.ORIENTATION_LANDSCAPE) { 
		    so = ScreenOrientation.LANDSCAPE_FIXED;
		} 

        EngineOptions eo = new EngineOptions(
                true,
                so,
                //new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
                new FixedResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
                camera);

        /*
        EngineOptions eo = new EngineOptions(
                true,
                ScreenOrientation.PORTRAIT_FIXED,
                new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
                camera);
                */

        return eo;
	}

	@Override
	protected Scene onCreateScene() {
	    if(mResult == null){
	        return null;
	    }
	    
		MainScene  mainScene = new MainScene(this, mResult, String.valueOf(Math.round(mArea)));
		return mainScene;
	}

	@Override
	protected int getLayoutID() {
		return R.layout.activity_result;
	}

	@Override
	protected int getRenderSurfaceViewID() {
		return R.id.renderview;
	}

	@Override
	public void appendScene(KeyListenScene scene) {
		
	}

	@Override
	public void backToInitial() {
		
	}

	@Override
	public void refreshRunningScene(KeyListenScene scene) {
		
	}
}
