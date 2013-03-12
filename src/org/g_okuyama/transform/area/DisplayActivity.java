package org.g_okuyama.transform.area;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

public class DisplayActivity extends Activity {
    float mArea = 0.0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_display);
        
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
        }
        
        TextView text = (TextView)findViewById(R.id.unit);
        text.setText(setStr);
        text.append("個分");
        
        TextView area = (TextView)findViewById(R.id.area);
        area.setText("(" + String.valueOf(Math.round(mArea)) + " m2)");   
    }
}
