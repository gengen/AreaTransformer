package org.g_okuyama.transform.area;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class ResultActivity extends MultiSceneActivity {
	private int CAMERA_WIDTH = 480;
	private int CAMERA_HEIGHT = 800;

    float mArea = 0.0f;
    String mResult;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setContentView(R.layout.activity_display);
        
        Bundle extras = getIntent().getExtras();
        mArea = extras.getFloat("area", 0.0f);
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
        
        float unit = mArea / 46755; //�����h�[��(m2)
        //�傫���ɂ���ĕ\������ς���
        if(unit >= 100.0){
            //�����_�͕\�����Ȃ�
            int i = Math.round(unit);
            setStr = String.valueOf(i);
        }
        else if(unit >= 1.0){
            //�����_1���܂ŕ\��
            float tmp = unit * 10;
            int i = Math.round(tmp);
            float ret = (float)i / 10;
            setStr = String.valueOf(ret);
        }
        else if(unit < 1.0 && unit >= 0.01){
            //�����_2���܂ŕ\��
            float tmp = unit * 100;
            int i = Math.round(tmp);
            float ret = (float)i / 100;
            setStr = String.valueOf(ret);            
        }
        else if(unit < 0.01 && unit >= 0.001){
            //�����_3���܂ŕ\��
            float tmp = unit * 1000;
            int i = Math.round(tmp);
            float ret = (float)i / 1000;
            setStr = String.valueOf(ret);
        }
        else{
            //����s�\�Ƃ���
            Log.d(AreaTransformActivity.TAG, "can\'t calcurate");
            //text.setVisibility(View.INVISIBLE);
            //area.setVisibility(View.INVISIBLE);
            
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
        //text.append("��");        
        //area.setText("(" + String.valueOf(Math.round(mArea)) + " m2)");
        mResult = setStr;
    }
    
    @Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		EngineOptions eo = new EngineOptions(
				true,
				ScreenOrientation.PORTRAIT_FIXED,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
				camera);
		return eo;
	}

	@Override
	protected Scene onCreateScene() {
		MainScene  mainScene = new MainScene(this, mResult);
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
