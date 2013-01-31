/*
 * A Volatile Status is a status condition which is non-permanent; i.e. it does not remain active if you switch and/or wears off after a few turns. 
 * @author Team Forretress
 */

package geniusectsim.pokemon;


public enum VolatileStatus {
	Confused, Curse, Embargo, Encore, Flinch, HealBlock, 
	Identified, Nightmare, Trapped, Perish, Leeched, Taunt, Levitated, 
	Torment, Infatuated, AquaRing, Ingrained, Bracing, DrawingAttention,
	DefenseCurl, Charging, Protected, None;
	
	private Pokemon victim;
	private int turnsActive;
	
	public void inflict(Pokemon v)
	{
		victim = v;
	}
	
	public void onNewTurn()
	{
		turnsActive++;
		if(turnsActive == 2)
		{
			if(	this == VolatileStatus.Flinch || 
				this == VolatileStatus.Bracing || 
				this == VolatileStatus.Protected || 
				this == VolatileStatus.Charging)
				victim.removeStatus(this);
		}
	}
}
