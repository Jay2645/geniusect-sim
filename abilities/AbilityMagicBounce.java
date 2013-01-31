package geniusectsim.abilities;

import geniusectsim.moves.Move;
import geniusectsim.moves.MoveType;
import geniusectsim.moves.Target;
import geniusectsim.pokemon.Pokemon;

/**
 * @author Jay
 *
 */
public class AbilityMagicBounce extends Ability 
{
	public AbilityMagicBounce()
	{
		rating = 5;
	}
	/*"magicbounce": {
	shortDesc: "This Pokemon blocks certain status moves and uses the move itself.",
	onAllyTryFieldHit: function(target, user, move) {
		if (target === user) return;
		if (typeof move.isBounceable === 'undefined') {
				move.isBounceable = !!(move.category === 'Status' && (move.status || move.boosts || move.volatileStatus === 'confusion' || move.forceSwitch));
		}
		if (move.target !== 'foeSide' && target !== this.effectData.target) {
			return;
		}
		if (move.hasBounced) {
			return;
		}
		if (move.isBounceable) {
			var newMove = this.getMoveCopy(move.id);
			newMove.hasBounced = true;
			this.add('-activate', target, 'ability: Magic Bounce', newMove, '[of] '+user);
			this.moveHit(user, target, newMove);
			return null;
		}
	},
	effect: {
		duration: 1
	},*/
	public void onMoveUsed(Pokemon attacker, Move move)
	{
		if(move.target != Target.Self && move.getMoveType() == MoveType.Status)
		{
			//TODO: Make attack bounce back to target.
		}
	}
}
