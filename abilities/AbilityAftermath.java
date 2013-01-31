package geniusectsim.abilities;

import geniusectsim.battle.Damage;
import geniusectsim.moves.Move;
import geniusectsim.moves.MoveType;
import geniusectsim.pokemon.Pokemon;

public class AbilityAftermath extends Ability {
	public AbilityAftermath()
	{
		rating = 3;
	}
	public void onFaint(Pokemon attacker, Move move)
	{
		if (move.getMoveType() != MoveType.Status && move.isContact) {
			onFaintDamage = new Damage(user.getFullHP()/4, attacker);
		}
	}
}
