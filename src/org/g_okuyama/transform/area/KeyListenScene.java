package org.g_okuyama.transform.area;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;

import android.view.KeyEvent;

public abstract class KeyListenScene extends Scene {
	
	private MultiSceneActivity baseActivity;

	// コンストラクタ
	public KeyListenScene(MultiSceneActivity baseActivity) {
		setTouchAreaBindingOnActionDownEnabled(true);
		this.baseActivity = baseActivity;
		prepareSoundAndMusic();
	}

	public MultiSceneActivity getBaseActivity() {
		return baseActivity;
	}

	// イニシャライザ
	public abstract void init();

	// サウンドの準備
	public abstract void prepareSoundAndMusic();

	// KeyEventのリスナー
	public abstract boolean dispatchKeyEvent(KeyEvent e);

	// Spriteの座標を画面中央に設定する（Spriteの中央が画面中央に）
	public Sprite placeToCenter(Sprite sp) {
		sp.setPosition(baseActivity.getEngine().getCamera().getWidth() / 2.0f
				- sp.getWidth() / 2.0f, baseActivity.getEngine().getCamera()
				.getHeight()
				/ 2.0f - sp.getHeight() / 2.0f);
		return sp;
	}

	// Spriteのx座標を画面中央に設定する（Spriteのx座標の中心が画面のx座標の中心に）。y座標は任意の値
	public Sprite placeToCenterX(Sprite sp, float y) {
		sp.setPosition(baseActivity.getEngine().getCamera().getWidth() / 2.0f
				- sp.getWidth() / 2.0f, y);
		return sp;
	}

	// Spriteのy座標を画面中央に設定する（Spriteのy座標の中心が画面のy座標の中心に）。x座標は任意の値
	public Sprite placeToCenterY(Sprite sp, float x) {
		sp.setPosition(x, baseActivity.getEngine().getCamera().getHeight()
				/ 2.0f - sp.getHeight() / 2.0f);
		return sp;
	}
}
