package org.g_okuyama.transform.area;

import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.font.BitmapFont;
import org.andengine.util.HorizontalAlign;

import android.view.KeyEvent;

public class MainScene extends KeyListenScene {
	
	String mResult;
	
	public MainScene(MultiSceneActivity baseActivity, String result){
		super(baseActivity);
		mResult = result;
		init();
	}
	
	public void init(){
		//attachChild(getBaseActivity().getResourceUtil().getSprite("main_bg.png"));
		
		BitmapFont bitmapFont = 
				new BitmapFont(
						getBaseActivity().getTextureManager(),
						getBaseActivity().getAssets(),
						"font/score.fnt");
		bitmapFont.load();
		
		Text result = new Text(100, 100, bitmapFont, mResult, 20, new TextOptions(HorizontalAlign.CENTER), 
				getBaseActivity().getVertexBufferObjectManager());
		attachChild(result);
		
		result.registerEntityModifier(new LoopEntityModifier(
				new SequenceEntityModifier(
						new ScaleModifier(0.2f, 1.0f, 1.4f), 
						new ScaleModifier(0.2f, 1.4f, 1.0f)),3));
	}

	@Override
	public void prepareSoundAndMusic() {
		
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		return false;
	}
}
