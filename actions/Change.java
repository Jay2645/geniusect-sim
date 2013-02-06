/*
 * A change ("switch") option, to be sent to Showdown or used in our virtual modeling.
 * @author TeamForretress
 */

package geniusectsim.actions;

import geniusectsim.battle.EntryHazard;
import geniusectsim.battle.Team;
import geniusectsim.battle.Type;
import geniusectsim.constants.Pokequations;
import geniusectsim.moves.Move;
import geniusectsim.pokemon.Pokemon;

import java.util.List;

public class Change extends Action {
	
	public Pokemon switchTo;

	public void changeTo(Pokemon changeTo)
	{
		switchTo = changeTo;
		name = switchTo.getName();
	}
	
	public static int calculateSwitchDamagePercent(Pokemon change)
	{
		//Returns the amount of entry hazard damage an incoming Pokemon will take.
		int damage = 0;
		Team team = change.getTeam();
		int rocksCount = team.getHazardCount(EntryHazard.StealthRock);
		int spikesCount = team.getHazardCount(EntryHazard.Spikes);
		//int toxicSpikesCount = team.getHazardCount(EntryHazard.ToxicSpikes);
		if(rocksCount > 0)
		{
			double effectiveness = Pokequations.damageMultiplier(Type.Rock, change.getTypes());
			if(effectiveness == 4)
				damage += 50;
			else if(effectiveness == 2)
				damage += 25;
			else if(effectiveness == 1)
				damage += 13;
			else if(effectiveness == 0.5)
				damage += 6;
			else
				damage += 3;
		}
		if(spikesCount > 0)
		{
			if(spikesCount == 1)
				damage += 13;
			else if(spikesCount == 2)
				damage += 17;
			else
				damage += 25;
		}
		//TODO: Toxic Spikes.
		return damage;
	}
	
	public static Pokemon bestCounter(Pokemon[] ourTeam, Pokemon enemy, Pokemon removeFromCalc)
	{
		//Returns the best response to a threat.
		int damage = Integer.MAX_VALUE;
		Pokemon switcher = null;
		double damageMult = 64;
		for(int i = 0; i < ourTeam.length; i++)
		{
			if(ourTeam[i] == null)
				continue;
			if(removeFromCalc != null)
			{
				if(ourTeam[i].getName().equals(removeFromCalc.getName()))
					continue;
			}
			if(ourTeam[i].isAlive())
			{
				double ourDamageMult = 	Pokequations.damageMultiplier(enemy.getType(0), ourTeam[i].getTypes())
										*Pokequations.damageMultiplier(enemy.getType(1), ourTeam[i].getTypes());
				if(switcher == null)
				{
					switcher = ourTeam[i]; //Make sure we will always return something if at least one Pokemon is alive.
					damageMult = ourDamageMult;
				}
				if(ourDamageMult < damageMult)
				{
					switcher = ourTeam[i];
					damageMult = ourDamageMult;
				}
				Move theirBestMove = Pokequations.bestMove(enemy,ourTeam[i]);
				if(damage > theirBestMove.getProjectedPercent(ourTeam[i]).y)
				{
					damage = theirBestMove.getProjectedPercent(ourTeam[i]).y;
					switcher = ourTeam[i];
					damageMult = ourDamageMult;
				}
			}
		}
		return switcher;
	}
	
	public static Pokemon bestCounter(Pokemon[] ourTeam, Pokemon enemy)
	{
		return bestCounter(ourTeam,enemy,null);
	}
	
	public static Pokemon bestCounter(List<String> pokeTeam, Team ourTeam, Pokemon enemy)
	{
		Pokemon[] team = new Pokemon[6];
		for(int i = 0; i < pokeTeam.size(); i++)
		{
			team[i] = ourTeam.getPokemon(pokeTeam.get(i));
		}
		return bestCounter(team,enemy,null);
	}
	public static Pokemon bestChange(Pokemon us, Pokemon[] ourTeam, Pokemon enemy, Move predictedMove)
	{
		if(changedRecently() || us.hasMove("Destiny Bond")) //Make sure this is a sane thing to do or try to take opponent with us if we can.
			return us;
		int damageStayIn = Pokequations.calculateDamagePercent(enemy, predictedMove, us).y;
		Pokemon change = us;
		int predictedDamage = damageStayIn;
		for(int i = 0; i < ourTeam.length; i++)
		{
			if(ourTeam[i] != null && ourTeam[i].isAlive() && ourTeam[i] != us)
			{
				if(ourTeam[i].getFullHP() == 0)
					ourTeam[i].query();
				int switchDamage = Pokequations.bestMove(enemy, ourTeam[i]).getProjectedPercent(ourTeam[i], true) + calculateSwitchDamagePercent(ourTeam[i]);
				if(ourTeam[i].getHealth() - switchDamage <= 0)
					continue;				
				if(change == us) //If we have not found someone to change to.
				{
					if(us.getHealth() - predictedDamage <= 0) 
					{
						change = ourTeam[i];
						predictedDamage = switchDamage;
					}
					else if(switchDamage < predictedDamage && sanityCheck(ourTeam[i], enemy, switchDamage))
					{	//A switch will take less damage by switching in and it's sane to come in:
						change = ourTeam[i];
						predictedDamage = switchDamage;
					}
				}
				else
				{
					if(sanityCheck(ourTeam[i], enemy, switchDamage))
					{
						change = ourTeam[i];
						predictedDamage = switchDamage;
					}
				}
			}
			else
				continue;
		}
		return change;
	}
	
	private static boolean changedRecently()
	{
		if(changeCount >= 3)
			return true;
		else return false;
	}
	
	private static boolean sanityCheck(Pokemon saneSwitch, Pokemon enemy, int switchDamage)
	{
		//Sanity check: We don't want to come in and get killed the next turn, unless we can OHKO the enemy.
		Move secondTurnBestMove = Pokequations.bestMove(enemy, saneSwitch);
		Move secondTurnOurBestMove = Pokequations.bestMove(saneSwitch, enemy);
		if(	secondTurnBestMove.getProjectedPercent(saneSwitch).y + switchDamage - saneSwitch.getHealth() > 0 || 
			secondTurnOurBestMove.getProjectedPercent(enemy).x - enemy.getHealth() <= 0 && saneSwitch.isFasterThan(enemy))
			return true;
		return false;
	}
}
