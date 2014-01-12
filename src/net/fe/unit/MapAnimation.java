package net.fe.unit;

import net.fe.FEResources;
import chu.engine.Game;
import chu.engine.anim.Animation;

public class MapAnimation extends Animation {
	private boolean synchro;
	private static int synchroFrame;
	private static float counter;
	public MapAnimation(String name, boolean walking){
		super(FEResources.getTexture(name), 48, 
				name.equals("swordmaster_map_selected")?60:48,
						4, 4, 17, name.equals("swordmaster_map_selected")?29:17, 0);
		synchro = !walking;
		speed = 0.15f;
	}
	public void update(){
		if(!synchro){
			super.update();
		} else {
			setFrame(synchroFrame);
		}
	}
	
	public static void updateAll(){
		counter += Game.getDeltaSeconds();
		if(counter > 0.08 && synchroFrame % 2 != 0 ||
		   counter > 0.5 && synchroFrame % 2 == 0){
			synchroFrame = (synchroFrame+1) % 4;
			counter = 0;
		}
	}
}