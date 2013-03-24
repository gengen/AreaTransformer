package org.g_okuyama.transform.area;

import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.BitmapFont;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.Texture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

import android.graphics.Typeface;
import android.os.Handler;
import android.view.KeyEvent;

public class MainScene extends KeyListenScene implements IOnSceneTouchListener{
	
	String mResult;
	String mArea;
	MultiSceneActivity mActivity;
	Handler mHandler;
	
	public MainScene(MultiSceneActivity baseActivity, String result, String area){
		super(baseActivity);
		mActivity = baseActivity;
		mResult = result;
		mArea = area;
		init();
	}
	
	public void init(){
		//�o�b�N�O���E���h
		attachChild(getBaseActivity().getResourceUtil().getSprite("bg.png"));
		
		/*
		//�v���O���X�_�C�A���O���~�߂�
		mHandler = new Handler();
		mHandler.post(new Runnable(){
			@Override
			public void run() {
				((ResultActivity)getBaseActivity()).deleteDialog();
				//ResultActivity.sDialog.dismiss();
			}
		});
		*/
		
		float up = 10;
		//�����h�[���A�C�R��
		Sprite dome = getBaseActivity().getResourceUtil().getSprite("dome.png");
		placeToCenterX(dome, up);
		attachChild(dome);
		
		//���ʕ\���p�t�H���g
		/*
		BitmapFont bitmapFont = 
				new BitmapFont(
						getBaseActivity().getTextureManager(),
						getBaseActivity().getAssets(),
						"font/score.fnt");
		bitmapFont.load();
		*/

		//���ʂɂ���ăt�H���g�T�C�Y��ω�������
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
		
		//�t�H���g���w��
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
		
		//���ʕ\���p�e�L�X�g
		Text result = new Text(0, 0, retFont, mResult, 20, new TextOptions(HorizontalAlign.CENTER), 
				getBaseActivity().getVertexBufferObjectManager());
		//X���͐^�񒆂Ɋ񂹂�
		float x1 = ResultActivity.CAMERA_WIDTH/2f - result.getWidth()/2f;
		float y1 = up + dome.getHeight()/*�h�[���A�C�R����*/ + 10/*�}�[�W��*/;
		result.setPosition(x1, y1);
		attachChild(result);
		
		//�t�H���g���w��
		Texture texture = new BitmapTextureAtlas(getBaseActivity().getTextureManager(), 480, 800, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		Font font = new Font(getBaseActivity().getFontManager(), texture, Typeface.DEFAULT_BOLD, 20, true, Color.BLACK);
		getBaseActivity().getTextureManager().loadTexture(texture);
		getBaseActivity().getFontManager().loadFont(font);

		//�P�ʕ\��
		Text unit = new Text(0, 0, font, "��", 20, new TextOptions(HorizontalAlign.CENTER), getBaseActivity().getVertexBufferObjectManager());
		//X���ɂ��^�񒆂Ɋ񂹂�
		float x2 = ResultActivity.CAMERA_WIDTH/2f - unit.getWidth()/2f;
		float y2 = y1 + result.getHeight() + 5/*�}�[�W��*/;
		unit.setPosition(x2, y2);
		attachChild(unit);

		//�ʐϕ\��
		Text area = new Text(0, 0, font, "(" + mArea + " m2)", 20, new TextOptions(HorizontalAlign.CENTER), getBaseActivity().getVertexBufferObjectManager());
		//X���ɂ��^�񒆂Ɋ񂹂�
		float x3 = ResultActivity.CAMERA_WIDTH/2f - area.getWidth()/2f;
		float y3 = y2 + unit.getHeight() + 10/*�}�[�W��*/;
		area.setPosition(x3, y3);
		attachChild(area);
		
		//���ʕ\���ɃA�j���[�V������t����
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

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		return false;
	}
}
