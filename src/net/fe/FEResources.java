package net.fe;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;
import org.newdawn.slick.opengl.renderer.Renderer;

import chu.engine.AnimationData;
import chu.engine.anim.BitmapFont;

public class FEResources {
	private static String[] searchFolders = 
		{"battle_anim", "battle_anim/static", "map_mugshots", "gui", "map_anim"};
	
	
	private static HashMap<String, Audio> audio;
	private static HashMap<String, AnimationData> textures;
	private static HashMap<String, BitmapFont> bitmapFonts;
	
	static {
		audio = new HashMap<String, Audio>();
		textures = new HashMap<String, AnimationData>() {
			private static final long serialVersionUID = 1L;

			@Override
			public AnimationData put(String string, AnimationData data) {
				System.out.println(string + "(texture) loaded.");
				return super.put(string, data);
			}
		};
		bitmapFonts = new HashMap<String, BitmapFont>();
	}

	public static Texture getTexture(String string) {
		return getTextureData(string).texture;
	}
	
	public static boolean hasTexture(String string){
		return textures.containsKey(string);
	}
	
	public static void loadResources() {
		try {
			//Load bitmap fonts
			loadBitmapFonts();
			
			// Textures
			loadTextures();
			textures.put("unit_colors", new AnimationData(TextureLoader.getTexture("PNG", 
					ResourceLoader.getResourceAsStream("res/palette/unit_colors_condensed.png"))));

			
			
			//load audio
			audio.put("miss", AudioLoader.getAudio("WAV",
					ResourceLoader.getResourceAsStream("res/sfx/miss.wav")));
			
		} catch (IOException e) {
			int max = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
			System.out.println(max);
			e.printStackTrace();
		}
	}
	public static AnimationData getMapTexture(String name){
		AnimationData t = textures.get(name);
		if(t!=null) return t;
		System.out.println("Warn:" + name + " not explicitly defined");
		for(String loc: searchFolders){
			try{
				AnimationData txt = new AnimationData(TextureLoader.getTexture("PNG",
						ResourceLoader.getResourceAsStream(
							"res/" + loc + "/" + name + ".png"
						)), 96, 24, 4, 4, 4, 4, 0, null, null);
				textures.put(name, txt);
				return txt;
			} catch (Exception e){
				
			}
		}
		return textures.get("whoops");
	}

	private static void loadTextures() {
		long startTime = System.nanoTime();
		// TODO Load textures from JSON
		InputStream file = ResourceLoader.getResourceAsStream("res/resources.json");
		Scanner in = new Scanner(file);
		StringBuilder sb = new StringBuilder();
		while(in.hasNextLine()) {
			sb.append(in.nextLine());
		}
		String json = sb.toString();
		
		JSONObject resources = null;
		try {
			resources = (JSONObject) JSONValue.parseWithException(json);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JSONArray txArray = (JSONArray) resources.get("textures");
		LoadStage.setMaximum(txArray.size());
		for(Object obj : txArray) {
			JSONObject texture = (JSONObject) obj;
			String name = (String)texture.get("name");
			String path = (String)texture.get("path");
			Number width = (Number)texture.get("width");
			Number height = (Number)texture.get("height");
			Number frames = (Number)texture.get("frames");
			Number columns = (Number)texture.get("columns");
			Number freeze = (Number)texture.get("freeze");
			Number offsetX = (Number)texture.get("offsetX");
			Number offsetY = (Number)texture.get("offsetY");
			Number speed = (Number)texture.get("speed");
			Number shakeFrames = (Number)texture.get("shakeFrames");
			JSONArray hitArray = (JSONArray) texture.get("hitframes");
			JSONArray audioArray = (JSONArray) texture.get("soundMap");
			HashMap<Integer, String> audioMap = new HashMap<Integer, String>();
			
			int[] hitframes;
			if(hitArray != null) {
				hitframes = new int[hitArray.size()];
				for(int i=0; i<hitframes.length; i++) {
					hitframes[i] = ((Number)hitArray.get(i)).intValue();
				}
			} else {
				hitframes = new int[0];
			}
			
			if(audioArray != null) {
				for(Object obj2 : audioArray) {
					JSONObject audio = (JSONObject) obj2; 
					audioMap.put(((Number)audio.get("frame")).intValue(), 
							(String)audio.get("sound"));
				}
			}
			try {
				AnimationData data;
				if(width == null) {
					data = new AnimationData(TextureLoader.getTexture("PNG",
							ResourceLoader.getResourceAsStream(path)));
				} else {
					data = new AnimationData(TextureLoader.getTexture("PNG",
							ResourceLoader.getResourceAsStream(path)),
							width.intValue(),
							height.intValue(),
							frames.intValue(),
							columns.intValue(),
							offsetX.intValue(),
							offsetY.intValue(),
							freeze.intValue(),
							hitframes,
							audioMap);
					System.out.println(" - " + name + ": " + path + "\n - "
							+ width + " " + height + " " + frames + " "
							+ columns + " " + freeze);
				}
				if(speed != null)
					data.speed = speed.floatValue();
				if(shakeFrames != null)
					data.shakeFrames = shakeFrames.intValue();
				textures.put(name, data);
				if((System.nanoTime() - startTime)/1000000.0f > 100){
					LoadStage.update(textures.size());
					LoadStage.render();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		in.close();
	}

	public static BitmapFont getBitmapFont(String name) {
		return bitmapFonts.get(name);
	}
	
	private static void loadBitmapFonts() {
		Scanner in = new Scanner(ResourceLoader.getResourceAsStream("res/fonts/fonts.txt"));
		while(in.hasNextLine()) {
			String line = in.nextLine();
			if(line.startsWith("#"))
				continue;
			if(line.startsWith("define")) {
				String name = line.split(":")[1];
				String texName = in.nextLine();
				char[] chars = in.nextLine().toCharArray();
				int height = Integer.parseInt(in.nextLine());
				int spacing = Integer.parseInt(in.nextLine());
				char[] widths = in.nextLine().toCharArray();
				
				BitmapFont font = new BitmapFont(texName);
				font.setHeight(height);
				font.setSpacing(spacing);
				int pos = 0;
				for(int i=0; i<chars.length; i++) {
					int width = Integer.parseInt(widths[i]+"");
					font.put(chars[i], pos, width);
					pos += width;
				}
				bitmapFonts.put(name, font);
				System.out.println(name+"(bitmap font) loaded");
			}
		}
		in.close();
	}

	public static AnimationData getTextureData(String string) {
		AnimationData t = textures.get(string);
		if(t != null) {
			return t;
		} else {
			//try to get it, in case we forgot
			System.err.println("Warn: " + string + " not explicitly defined");
			for(String loc: searchFolders){
				try{
					AnimationData txt = new AnimationData(TextureLoader.getTexture("PNG",
							ResourceLoader.getResourceAsStream(
								"res/" + loc + "/" + string + ".png"
							)));
					textures.put(string, txt);
					return txt;
				} catch (Exception e){
					
				}
			}
			return textures.get("whoops");
		}
	}
	
	public static TrueTypeFont createFont(String name, int type, int size) {
		Font awtfont = new Font(name, type, size);
		return new TrueTypeFont(awtfont, false);
	}
	
	public static TrueTypeFont createFontFromFile(String name, int size) {
		try {
			Font awtfont = Font.createFont(Font.TRUETYPE_FONT, ResourceLoader.getResourceAsStream("res/"+name+".ttf"));
			awtfont = awtfont.deriveFont(Font.TRUETYPE_FONT, size);
			TrueTypeFont ttf = new TrueTypeFont(awtfont, false);
			return ttf;
		} catch (FontFormatException e) {
			System.err.println("Font "+name+" could not be created!");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			System.err.println("Font "+name+" could not be found!");
			e.printStackTrace();
			return null;
		}
	}

	public static Audio getAudio(String name) {
		Audio a = audio.get(name);
		if(a == null) {
//			System.err.println("Warn: " + name + " not explicitly defined");
			try{
				Audio b = AudioLoader.getAudio("WAV",
						ResourceLoader.getResourceAsStream("res/sfx/"+name+".wav"));
				audio.put(name, b);
				return b;
			} catch (Exception e){
				return null;
			}
		} else {
			return a;
		}
	}

}
