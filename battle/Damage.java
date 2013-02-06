package geniusectsim.battle;

import geniusectsim.constants.Pokequations;
import geniusectsim.moves.Move;
import geniusectsim.moves.MoveType;
import geniusectsim.moves.Target;
import geniusectsim.pokemon.Pokemon;


public class Damage {
	
	/**
	 * Damages a victim right away, without an attack.
	 * @param damageAmount (int): The amount of damage to apply to the victim.
	 * @param victim (Pokemon): The Pokemon to damage.
	 */
	public Damage(int damageAmount, Pokemon victim)
	{
		if(damageAmount != 0)
			victim.damage(damageAmount);
	}
	public Damage(Move move, Pokemon attacker, Pokemon victim)
	{
		this.attacker = attacker;
		attack = move;
		Target target = attack.target;
		if(target == Target.Self ||target == Target.Allies || target == Target.AllySide)
			defender = this.attacker;
		else
			defender = victim;
	}
	public Damage(String move, Pokemon attacker, Pokemon victim, int damageDealt, boolean wasCrit)
	{
		this.attacker = attacker;
		defender = victim;
		damage = damageDealt;
		attack = this.attacker.onNewTurn(move, damageDealt, wasCrit);
		if(attacker.getEVsLeft() > 3 && !attack.withinExpectedRange(damageDealt, victim, wasCrit))
		{
			int targetAtkStat = Pokequations.calculateAtkStat(this);
			if(attack.getMoveType() == MoveType.Physical)
				attacker.setMinAttack(targetAtkStat);
			else if(attack.getMoveType() == MoveType.Special)
				attacker.setMinSpA(targetAtkStat);
		}
	}
	public Move attack = null;
	public Pokemon attacker = null;
	public Pokemon defender = null;
	int damage = 0;
	
	public int applyDamage()
	{
		if(attacker == null || defender == null)
			return 0;
		if(damage == 0)
			damage = attack.useMove(false, attacker, defender);
		if(damage < 0)
			return -attacker.restoreHP(damage);
		if(damage == 0)
			return 0;
		defender.damage(damage);
		return damage;
	}
	/**
	 * Gets the amount of damage this class applied to the target.
	 * @return (int): The damage amount.
	 */
	public int getDamageAmount() 
	{
		return damage;
	}
}
