package geniusectsim.battle;

import geniusectsim.moves.Move;
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
		defender = victim;
		this.attacker = attacker;
		attack = move;
	}
	public Move attack = null;
	public Pokemon attacker = null;
	public Pokemon defender = null;
	
	public int applyDamage()
	{
		if(attacker == null)
			return 0;
		int damage = attack.useMove(false, attacker, defender);
		if(damage < 0)
		{
			return -attacker.restoreHP(damage);
		}
		if(defender == null || damage == 0)
			return 0;
		defender.damage(damage);
		return damage;
	}
}
