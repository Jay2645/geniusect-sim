/*
 * An attack option, to be sent to Showdown or used in our virtual modeling.
 * @author TeamForretress
 */

package geniusectsim.actions;

import geniusectsim.battle.Battle;
import geniusectsim.bridge.Simulator;
import geniusectsim.constants.Pokequations;
import geniusectsim.moves.Move;
import geniusectsim.pokemon.Pokemon;

import java.awt.Point;

public class Attack extends Action {
	public Move move;
	
	public Pokemon attacker;
	public Pokemon defender;
	
	private Action toDo;
	
	public void setMove(Move m, Pokemon attack, Pokemon defend, Battle b)
	{
		attacker = attack;
		defender = defend;
		battle = b;
		move = null;
		if(attacker.getLockedInto() != null)
		{
			move = attacker.getLockedInto();
			System.err.println(attacker.getName()+" is locked into "+move.name+"!");
			name = move.name;
			return;
		}
		if(m.disabled || m.name.toLowerCase().startsWith("struggle"))
		{
			//Double-check to make sure this is a legal move.
			Move[] newSet = new Move[4];
			Move[] currentSet = attack.getMoveset();
			boolean foundEnabledMove = false;
			for(int i = 0; i < currentSet.length; i++)
			{
				if(currentSet[i] == null || currentSet[i].disabled)
					continue;
				else
				{
					foundEnabledMove = true;
					newSet[i] = currentSet[i];
				}
			}
			if(foundEnabledMove)
				move = Pokequations.bestMove(attacker, defender, newSet);
			else if(m.name.toLowerCase().startsWith("struggle"))
				move = m;
			else
				move = new Move("struggle", attack, false);
		}
		else
			move = m;
		name = move.name;
	}
	
	public Action deploy()
	{
		//Send next Attack to Showdown.
		if(sent)
			return toDo;
		System.err.println(attacker.getName()+" used "+move.name+"!");
		sent = true;
		if(!sayOnSend.equals(""))
		{
			Simulator.print(sayOnSend);
			sayOnSend = "";
		}
		toDo = Simulator.newTurn(this);
		return toDo;
	}
	
	public void defenderSwap(Pokemon newDefend)
	{
		defender = newDefend;
	}
	
	public void resultPercent(int damageDone)
	{
		if(damageDone > move.getProjectedPercent(defender).y)
		{
			move.adjustProjectedPercent(new Point(move.getProjectedPercent(defender).x, damageDone), defender);
		}
		else if(damageDone < move.getProjectedPercent(defender).x)
		{
			move.adjustProjectedPercent(new Point(damageDone, move.getProjectedPercent(defender).y), defender);
		}
	}
}
