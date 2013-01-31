package geniusectsim.abilities;

import geniusectsim.battle.Type;
import geniusectsim.pokemon.Pokemon;

/**
 * Class for the "Levitate" ability.
 * @author TeamForretress
 */
public class AbilityLevitate extends Ability
{
	public AbilityLevitate()
	{
		rating = 3.5;
	}
	public void setUser(Pokemon u)
	{
		user = u;
		user.addImmunity(Type.Ground);
	}
}
