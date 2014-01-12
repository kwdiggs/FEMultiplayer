package net.fe.fightStage;

import java.util.ArrayList;

import net.fe.network.FEServer;
import net.fe.unit.Unit;
import net.fe.unit.UnitIdentifier;

public class HealCalculator {
	private Unit healer, healee;

	public HealCalculator(UnitIdentifier u1, UnitIdentifier u2) {
		healer = FEServer.getUnit(u1);
		healee = FEServer.getUnit(u2);
	}

	public ArrayList<AttackRecord> getAttackQueue() {
		ArrayList<AttackRecord> q = new ArrayList<AttackRecord>();
		final int heal = Math.min(healer.get("Mag") / 2 + healer.getWeapon().mt, 
				healee.get("HP") - healee.getHp());
		System.out.println("Heal: "+heal+"("+healee.get("HP")+""+healee.getHp()+")");
		healer.use(healer.getWeapon());
		healee.setHp(healee.getHp() + heal);
		AttackRecord a = new AttackRecord();
		a.animation = "Heal";
		a.attacker = new UnitIdentifier(healer);
		a.defender = new UnitIdentifier(healee);
		a.damage = -heal;
		q.add(a);
		return q;
	}
}