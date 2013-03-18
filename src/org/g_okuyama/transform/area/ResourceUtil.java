package org.g_okuyama.transform.area;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.debug.Debug;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ResourceUtil {
	private static ResourceUtil self;
	private static BaseGameActivity gameActivity;
	
	private static HashMap<String, ITextureRegion> textureRegionPool;
	private static HashMap<String, TiledTextureRegion> tiledTextureRegionPool;
	
	private ResourceUtil(){
		
	}
	
	public static ResourceUtil getInstance(BaseGameActivity gameActivity){
		if(self == null){
			self = new ResourceUtil();
			ResourceUtil.gameActivity = gameActivity;
			BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
			textureRegionPool = new HashMap<String, ITextureRegion>();
			tiledTextureRegionPool = new HashMap<String, TiledTextureRegion>();
		}
		
		return self;
	}
	
	public Sprite getSprite(String filename){
		if(textureRegionPool.containsKey(filename)){
			Sprite s = new Sprite(
					0, 
					0, 
					textureRegionPool.get(filename),
					gameActivity.getVertexBufferObjectManager());
			s.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			return s;
		}
		
		InputStream is = null;
		try{
			is = gameActivity.getResources().getAssets().open("gfx/" + filename);

		}catch(IOException e){
			e.printStackTrace();
		}
		
		Bitmap bm = BitmapFactory.decodeStream(is);
		BitmapTextureAtlas bta = new BitmapTextureAtlas(
				gameActivity.getTextureManager(),
				getTwoPowerSize(bm.getWidth()),
				getTwoPowerSize(bm.getHeight()),
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		gameActivity.getEngine().getTextureManager().loadTexture(bta);
		
		ITextureRegion btr = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bta, gameActivity, filename, 0, 0);
		Sprite s = new Sprite(0, 0, btr, gameActivity.getVertexBufferObjectManager());
		s.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		textureRegionPool.put(filename, btr);
		
		return s;
	}
	
	public AnimatedSprite getAnimatedSprite(String fileName, int column, int row) {
		if (tiledTextureRegionPool.containsKey(fileName)) {
			AnimatedSprite s = new AnimatedSprite(0, 0,
					tiledTextureRegionPool.get(fileName),
					gameActivity.getVertexBufferObjectManager());
			s.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			return s;
		}
		InputStream is = null;
		try {
			is = gameActivity.getResources().getAssets()
					.open("gfx/" + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bitmap bm = BitmapFactory.decodeStream(is);
		BitmapTextureAtlas bta = new BitmapTextureAtlas(
				gameActivity.getTextureManager(),
				getTwoPowerSize(bm.getWidth()),
				getTwoPowerSize(bm.getHeight()),
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		gameActivity.getTextureManager().loadTexture(bta);

		TiledTextureRegion ttr = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(bta, gameActivity, fileName, 0, 0,
						column, row);
		AnimatedSprite s = new AnimatedSprite(0, 0, ttr,
				gameActivity.getVertexBufferObjectManager());
		s.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		tiledTextureRegionPool.put(fileName, ttr);

		return s;
	}

	public ButtonSprite getButtonSprite(String normal, String pressed) {

		if (textureRegionPool.containsKey(normal)
				&& textureRegionPool.containsKey(pressed)) {
			ButtonSprite s = new ButtonSprite(0, 0,
					textureRegionPool.get(normal),
					textureRegionPool.get(pressed),
					gameActivity.getVertexBufferObjectManager());
			s.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			return s;
		}

		InputStream is = null;
		try {
			is = gameActivity.getResources().getAssets().open("gfx/" + normal);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bitmap bm = BitmapFactory.decodeStream(is);
		
		// ボタン生成の為のTextureRegion生成。TiledTextureRegionでなく、BuildableBitmapTextureAtlasを利用する。
		BuildableBitmapTextureAtlas bta = new BuildableBitmapTextureAtlas(
				gameActivity.getTextureManager(),
				getTwoPowerSize(bm.getWidth() * 2),
				getTwoPowerSize(bm.getHeight()));
		ITextureRegion trNormal = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(bta, gameActivity, normal);
		ITextureRegion trPressed = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(bta, gameActivity, pressed);

		try {
			bta.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(
					0, 0, 0));
			bta.load();
		} catch (TextureAtlasBuilderException e) {
			Debug.e(e);
		}

		textureRegionPool.put(normal, trNormal);
		textureRegionPool.put(pressed, trPressed);

		ButtonSprite s = new ButtonSprite(0, 0, trNormal, trPressed,
				gameActivity.getVertexBufferObjectManager());
		s.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		return s;
	}

	// プールを開放、シングルトンを削除する為の関数
	public void resetAllTexture() {
		// Activity.finish()だけだとシングルトンなクラスがnullにならない為明示的にnullを代入
		self = null;
		textureRegionPool.clear();
		tiledTextureRegionPool.clear();
	}
	
	public int getTwoPowerSize(float size){
		int value = (int)(size+1);
		int pow2value = 64;
		while(pow2value < value){
			pow2value *=2;
		}
		return pow2value;
	}
}
