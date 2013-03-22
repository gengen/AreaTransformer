package org.g_okuyama.transform.area;

import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.font.BitmapFont;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.Texture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

import android.graphics.Typeface;
import android.view.KeyEvent;

public class MainScene extends KeyListenScene {
	
	String mResult;
	String mArea;
	
	public MainScene(MultiSceneActivity baseActivity, String result, String area){
		super(baseActivity);
		mResult = result;
		mArea = area;
		init();
	}
	
	public void init(){
		//バックグラウンド
		attachChild(getBaseActivity().getResourceUtil().getSprite("bg.png"));
		
		//プログレスダイアログを止める
		((ResultActivity)getBaseActivity()).dismissDialog();
		
		//東京ドームアイコン
		Sprite dome = getBaseActivity().getResourceUtil().getSprite("dome.png");
		placeToCenterX(dome, 0);
		attachChild(dome);
		
		//結果表示用フォント
		/*
		BitmapFont bitmapFont = 
				new BitmapFont(
						getBaseActivity().getTextureManager(),
						getBaseActivity().getAssets(),
						"font/score.fnt");
		bitmapFont.load();
		*/

		//結果によってフォントサイズを変化させる
		int size = 25;
		float ret = Float.valueOf(mResult);
		if(ret < 10000){
		    size = 40;
		}
		else if(ret < 100000){
		    size = 35;
		}
		else if(ret < 1000000){
		    size = 30;
		}
		
		//フォントを指定
        Texture retTexture = new BitmapTextureAtlas(getBaseActivity().getTextureManager(), 480, 800, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        Font retFont = new Font(
                getBaseActivity().getFontManager(),
                retTexture,
                Typeface.createFromAsset(getBaseActivity().getAssets(), "font/BMcube.TTF"),
                size,
                true,
                Color.BLACK);
        getBaseActivity().getTextureManager().loadTexture(retTexture);
        getBaseActivity().getFontManager().loadFont(retFont);
		
		//結果表示用テキスト
		Text result = new Text(0, 0, retFont, mResult, 20, new TextOptions(HorizontalAlign.CENTER), 
				getBaseActivity().getVertexBufferObjectManager());
		//X軸は真ん中に寄せる
		float x1 = 150 - result.getWidth()/2f;
		float y1 = 0 + dome.getHeight()/*ドームアイコン分*/ + 0/*マージン*/;
		result.setPosition(x1, y1);
		attachChild(result);
		
		//フォントを指定
		Texture texture = new BitmapTextureAtlas(getBaseActivity().getTextureManager(), 480, 800, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Font font = new Font(getBaseActivity().getFontManager(), texture, Typeface.DEFAULT_BOLD, 20, true, Color.BLACK);
		getBaseActivity().getTextureManager().loadTexture(texture);
		getBaseActivity().getFontManager().loadFont(font);

		//単位表示
		Text unit = new Text(0, 0, font, "個分", 20, new TextOptions(HorizontalAlign.CENTER), getBaseActivity().getVertexBufferObjectManager());
		//X軸につき真ん中に寄せる
		float x2 = 150 - unit.getWidth()/2f;
		float y2 = y1 + result.getHeight() + 5/*マージン*/;
		unit.setPosition(x2, y2);
		attachChild(unit);

		//面積表示
		Text area = new Text(0, 0, font, "(" + mArea + " m2)", 20, new TextOptions(HorizontalAlign.CENTER), getBaseActivity().getVertexBufferObjectManager());
		//X軸につき真ん中に寄せる
		float x3 = 150 - area.getWidth()/2f;
		float y3 = y2 + unit.getHeight() + 10/*マージン*/;
		area.setPosition(x3, y3);
		attachChild(area);
		
		//結果表示にアニメーションを付ける
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
