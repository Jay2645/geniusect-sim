/*
 * A bunch of game constants, formulas, and the like.
 * @author TeamForretress
 */

package geniusectsim.constants;
import geniusectsim.battle.Battle;
import geniusectsim.battle.EntryHazard;
import geniusectsim.battle.Team;
import geniusectsim.battle.Type;
import geniusectsim.battle.Weather;
import geniusectsim.moves.Move;
import geniusectsim.moves.MoveType;
import geniusectsim.pokemon.Nature;
import geniusectsim.pokemon.Pokemon;
import geniusectsim.pokemon.Stat;
import geniusectsim.pokemon.Status;

import java.awt.Point;


public class Pokequations {
	
	public static Point calculateDamagePercent(Pokemon attacker, Move move, Pokemon defender)
	{
		if(move.getMoveType() == MoveType.Status)
			return new Point(0,0);
		Point percentage = calculateDamage(attacker, move, defender);
		if(defender.getFullHP() == 0)
			defender.query();
		if(defender.getFullHP() == 0) //There's something wrong with the SQL data.
			return new Point(1,1);
		double fullHP = (double)defender.getFullHP();
		double percentageX = (percentage.x / fullHP) * 100;
		double percentageY = (percentage.y / fullHP) * 100;
		percentage.x = (int)Math.round(percentageX);
		percentage.y = (int)Math.round(percentageY);
		return percentage;
	}
	
	public static Point calculateDamage(Pokemon attacker, Move move, Pokemon defender)
	{
		//System.out.println("Calculating the damage if "+attacker.name+" uses "+move.name+" on "+defender.name);
		//Returns damage dealt as a point(minValue, maxValue).
		if(move.getMoveType() == MoveType.Status)
			return new Point(0,0);
		else if(move.name.toLowerCase().startsWith("seismic toss") || move.name.toLowerCase().startsWith("night shade"))
			return new Point(attacker.getLevel(), attacker.getLevel());
		Type[] immunities = defender.getImmunities();
		for(int i = 0; i < immunities.length; i++)
		{
			if(immunities[i] == move.type)
				return new Point(0,0);
		}
		double stab = 1;
		if(attacker.getType(0) == move.type || attacker.getType(1) == move.type)
			stab = attacker.getSTAB();
		int attackPower = move.power;
		int attackStat;
		int defenseStat;
		int level = attacker.getLevel();
		Battle battle = attacker.getTeam().getBattle();
		if(move.getMoveType() == MoveType.Special)
		{
			attackStat = attacker.getBoostedStat(Stat.SpA);
			defenseStat = defender.getBoostedStat(Stat.SpD);
			if(battle.getWeather() == Weather.Sandstorm && (defender.getType(0) == Type.Rock || defender.getType(1) == Type.Rock))
				defenseStat = statBoost(attacker.getBoosts(Stat.SpD) + 1,attacker.getStats(Stat.SpD));
		}
		else
		{
			attackStat = attacker.getBoostedStat(Stat.Atk);
			defenseStat = defender.getBoostedStat(Stat.Def);
		}
		if(defender.getBoostedStat(Stat.Def) == 0)
			defenseStat = 100; //Means we could not look up this Pokemon's defense stat for some reason.
		//TODO: Convert Hidden Power to correct type.
		double multiplier = damageMultiplier(move.type, defender.getTypes());
		double modifier = attacker.getAbilityModifier();
		modifier *= weatherModifier(battle,move.type);
		
		return calculateDamage(level, attackStat, attackPower, defenseStat, stab, multiplier,modifier);
	}
	
	private static Point calculateDamage(int level, int attackStat, int attackPower, int defenseStat, double stab, double multiplier, double modifier)
	{
		if(defenseStat == 0)
			return new Point(1,1);
		//Returns damage dealt as a point(minValue, maxValue).
		
		Point p = new Point();
		p.y = (int)Math.floor(((((2 * level / 5 + 2) * attackStat * (attackPower * modifier) / defenseStat) / 50) + 2) * stab * multiplier);
		p.x = (int)Math.ceil(p.y * 0.85);
		//System.out.println("Max damage is "+p.y);
		return p;
	}
	
	public static double weatherModifier(Battle battle, Type type)
	{
		Weather weather = battle.getWeather();
		if(weather == Weather.Sun)
		{
			if(type == Type.Fire)
				return 1.5;
			if(type == Type.Water)
				return 0.5;
		}
		else if(weather == Weather.Rain)
		{
			if(type == Type.Water)
				return 1.5;
			if(type == Type.Fire)
				return 0.5;
		}
		return 1;
	}
	
	public static int calculateAtkStat(Pokemon attacker, Move move, Pokemon defender, int percentageLost)
	{
		int attackPower = move.power;
		int level = attacker.getLevel();
		int defenseStat;
		if(move.getMoveType() == MoveType.Special)
			defenseStat = defender.getBoostedStat(Stat.SpD);
		else
			defenseStat = defender.getBoostedStat(Stat.Def);
		double multiplier = damageMultiplier(move.type, defender.getTypes());
		double bonus = 1;
		if(attacker.getType(0) == move.type || attacker.getType(1) == move.type)
			bonus = attacker.getSTAB();
		int damage = calculateHPDamage(percentageLost,defender.getBoostedStat(Stat.HP));
		
		return (int)Math.floor(50 * damage * defenseStat / (bonus * multiplier * attackPower * (2 * level / 5 + 2)) - 100 * defenseStat / (attackPower * (2 * level / 5 + 2)));
	}
	
	public static int calculateDefStat(Pokemon attacker, Move move, Pokemon defender, int percentageLost)
	{
		/*int attackPower = move.power;
		int level = attacker.level;
		int defenseStat;
		if(move.special)
			defenseStat = defender.stats[4];
		else
			defenseStat = defender.stats[2];
		double multiplier = damageMultiplier(move.type, defender.types);
		double bonus = 1;
		if(attacker.types[0] == move.type || attacker.types[1] == move.type)
			bonus = 1.5;
		int damage = calculateHPDamage(percentageLost,defender.stats[0]);
		return (int)Math.floor(50 * damage * defenseStat / (bonus * multiplier * attackPower * (2 * level / 5 + 2)) - 100 * defenseStat / (attackPower * (2 * level / 5 + 2)));
		*/
		//TODO: Work out what to do about this.
		return 0;
	}
	
	public static int calculateHPDamage(int percentage, int hp)
	{
		//Returns the amount of HP lost based upon our known HP value and a percentage of lost HP.
		return Math.round((percentage / 100) * hp);
	}
	
	public static double damageMultiplier(Type move, Type[] enemy)
	{
		if(move == Type.None || move == null)
			return 1;
		//Returns the damage multiplier value for a type matchup.
		double first = SQLHandler.queryDamage(move, enemy[0]);
		if(enemy[1] == Type.None)
			return first;
		double second = SQLHandler.queryDamage(move, enemy[1]);
		return first * second;
	}
	
	public static int[] calculateStat(Pokemon pokemon)
	{
		int[] stats = new int[6];
		for(int i = 0; i < 6; i++)
		{
			stats[i] = calculateStat(Stat.fromInt(i),pokemon);
		}
		return stats;
	}
	
	public static int calculateStat(Stat type, Pokemon pokemon)
	{
		if(pokemon.getBaseStat(type) == 0)
			return 0;
		return calculateStat(type, pokemon.getNature(), pokemon.getBaseStat(type), pokemon.getIVs(type),pokemon.getEVs(type), pokemon.getLevel());
	}
	
	public static int calculateStat(Stat type, Nature nature, int base, int iv, int ev, int level)
	{
		return calculateStat(type,nature.multiplier(type),base,iv,ev,level);
	}
	
	public static int calculateStat(Stat type, double natureValue, int base, int iv, int ev, int level)
	{
		//Returns any non-HP stat as an int.
		if(type == Stat.HP)
			return calculateHP(base,iv,ev,level);
		else return (int) Math.ceil((((iv + 2 * base + (ev/4) ) * level/100 ) + 5) * natureValue);
	}
	public static int calculateHP(int base, int iv, int ev, int level)
	{
		//Returns the HP stat as an int.
		return (int) Math.ceil(((iv + 2 * base + (ev/4) ) * level/100 ) + 10 + level);
	}
	
	public static int calculateEVs(Stat stat, Pokemon pokemon)
	{
		return calculateEVs(pokemon.getNature(),stat,pokemon.getBaseStat(stat),pokemon.getLevel(),pokemon.getStats(stat),pokemon.getIVs(stat));
	}
	
	public static int calculateEVs(Nature nature, Stat stat, int base, int level, int statValue, int iv)
	{
		double natureValue = nature.multiplier(stat);
		return (int)Math.ceil(-(4 *(natureValue * (2 * base*level+level * iv+500)-100 * statValue))/(natureValue * level));
	}
	
	public static int statBoost(int level, int stat)
	{
		//Adjusts a stat for a certain number of boosts, then returns the adjusted stat.
		double adjust = 1;
		if(level < -6)
			level = -6;
		else if(level > 6)
			level = 6;
		switch(level) {
			case -6:	adjust = 0.25;
						break;
			case -5:	adjust = 0.285;
						break;
			case -4:	adjust = 0.33;
						break;
			case -3:	adjust = 0.4;
						break;
			case -2:	adjust = 0.5;
						break;
			case -1:	adjust = 0.66;
						break;
			case 0:		adjust = 1;
						break;
			case 1:		adjust = 1.5;
						break;
			case 2:		adjust = 2;
						break;
			case 3:		adjust = 2.5;
						break;
			case 4:		adjust = 3;
						break;
			case 5:		adjust = 3.5;
						break;
			case 6:		adjust = 4;
						break;
		}
		return (int)Math.round(stat*adjust);
	}
	
	public static Move bestMove(Pokemon attacker, Pokemon defender, Move enemyMove)
	{
		//Proper calculation for Wobbuffet, Wynaut, etc.
		boolean foundMove = false;
		Move[] moveset = attacker.getMoveset();
		for(int i = 0; i < moveset.length; i++)
		{
			if(moveset[i] == null || moveset[i].disabled)
				continue;
			foundMove = true;
			int projectedDamageFromEnemy = 0;
			if(enemyMove.getProjectedPercent(attacker) == null)
				projectedDamageFromEnemy = calculateDamagePercent(attacker,enemyMove,defender).y;
			else
				projectedDamageFromEnemy = enemyMove.getProjectedPercent(attacker).y;
			int turnsUntilDead = turnsToKill(attacker.getHealth(), projectedDamageFromEnemy + moveset[i].recoilPercent);
			if(turnsUntilDead > 1)
			{
				if(	moveset[i].name.toLowerCase().startsWith("counter") && enemyMove.getMoveType() == MoveType.Physical || 
					moveset[i].name.toLowerCase().startsWith("mirror coat") && enemyMove.getMoveType() == MoveType.Special)
				{
					int projectedDamageLower = enemyMove.getProjectedDamage(attacker).x * 2;
					int projectedDamageUpper = enemyMove.getProjectedDamage(attacker).y * 2;
					moveset[i].getProjectedDamage(defender).x = projectedDamageLower;
					moveset[i].getProjectedDamage(defender).y = projectedDamageUpper;
					moveset[i].getProjectedPercent(defender).x = defender.hpToPercent(projectedDamageLower);
					moveset[i].getProjectedPercent(defender).y = defender.hpToPercent(projectedDamageUpper);
				}
				else if(moveset[i].name.toLowerCase().startsWith("counter") && enemyMove.getMoveType() != MoveType.Physical || 
						moveset[i].name.toLowerCase().startsWith("mirror coat") && enemyMove.getMoveType() != MoveType.Special)
				{
					moveset[i].getProjectedDamage(defender).x = 0;
					moveset[i].getProjectedDamage(defender).y = 0;
					moveset[i].getProjectedPercent(defender).x = 0;
					moveset[i].getProjectedPercent(defender).y = 0;
				}
				if(turnsUntilDead == 2 && !attacker.isFasterThan(defender))
				{
					if(moveset[i].name.toLowerCase().startsWith("destiny bond"))
					{
						moveset[i].getProjectedDamage(defender).x = Integer.MAX_VALUE - 1;
						moveset[i].getProjectedDamage(defender).y = Integer.MAX_VALUE;
						moveset[i].getProjectedPercent(defender).x = 99;
						moveset[i].getProjectedPercent(defender).y = 100;
					}
				}
				else if(moveset[i].name.toLowerCase().startsWith("destiny bond"))
				{
					moveset[i].getProjectedDamage(defender).x = 0;
					moveset[i].getProjectedDamage(defender).y = 0;
					moveset[i].getProjectedPercent(defender).x = 0;
					moveset[i].getProjectedPercent(defender).y = 0;
				}
			}
			else
			{
				if(moveset[i].name.toLowerCase().startsWith("counter")|| moveset[i].name.toLowerCase().startsWith("mirror coat"))
				{
					moveset[i].getProjectedDamage(defender).x = 0;
					moveset[i].getProjectedDamage(defender).y = 0;
					moveset[i].getProjectedPercent(defender).x = 0;
					moveset[i].getProjectedPercent(defender).y = 0;
				}
				else if(turnsUntilDead == 1 && attacker.isFasterThan(defender) && moveset[i].name.toLowerCase().startsWith("destiny bond"))
				{
					moveset[i].getProjectedDamage(defender).x = Integer.MAX_VALUE - 1;
					moveset[i].getProjectedDamage(defender).y = Integer.MAX_VALUE;
					moveset[i].getProjectedPercent(defender).x = 99;
					moveset[i].getProjectedPercent(defender).y = 100;
				}
			}
		}
		if(foundMove)
			return bestMove(attacker,defender);
		else return new Move("Struggle", attacker, false);
	}
	
	public static Move bestMove(Pokemon attacker, Pokemon defender)
	{
		return bestMove(attacker, defender, attacker.getMoveset());
	}
	
	public static Move bestMove(Pokemon attacker, Pokemon defender, Move[] moveset)
	{
		if(attacker.getLockedInto() != null)
			return attacker.getLockedInto();
		Move use = null;
		if(attacker.getStatus() == Status.Rest || attacker.getStatus() == Status.Sleep)
		{
			if(attacker.hasMove("Sleep Talk"))
				use = attacker.getMove("Sleep Talk");
			else if(attacker.hasMove("Snore"))
				use = attacker.getMove("Snore");
			if(use != null)
				return use;
		}
		Team attackerTeam = attacker.getTeam();
		int attackerAliveCount = attackerTeam.getAliveCount();
		int defenderAliveCount = defender.getTeam().getAliveCount();
		Point damage = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE + 1);
		Weather weather = attacker.getTeam().getBattle().getWeather();
		double adjustedDamage = 0;
		int health = attacker.getHealth();
		for(int i = 0; i < moveset.length; i++)
		{
			if(moveset[i] == null || moveset[i].disabled || moveset[i].pp <= 0 || moveset[i].shortname.equals("solarbeam") && weather != Weather.Sun)
			{
				if(attacker.getTeam().getTeamID() == 0)
					System.err.println(attacker.getName()+"'s move "+moveset[i]+" is null or disabled!");
				continue;
			}
			System.out.println("This move is "+moveset[i].name);
			if(attackerAliveCount > 1 && defenderAliveCount > 2 && !defender.checkAbilities("Magic Bounce") && health > 60)
			{
				if(moveset[i].shortname.equals("stealthrock"))
				{
					if(!defender.getTeam().hasMaxHazard(EntryHazard.StealthRock))
						return moveset[i];
				}
				else if(moveset[i].shortname.equals("toxicspikes"))
				{
					if(!defender.getTeam().hasMaxHazard(EntryHazard.ToxicSpikes))
						return moveset[i];
				}
				else if(moveset[i].shortname.equals("spikes"))
				{
					if(!defender.getTeam().hasMaxHazard(EntryHazard.Spikes))
						return moveset[i];
				}
			}
			if(use == null)
			{
				use = moveset[i];
				damage = calculateDamage(attacker, moveset[i],defender);
				adjustedDamage = (damage.x * use.accuracy) / 100;
				continue;
			}
			Point moveDamage;
			if(moveset[i].name.toLowerCase().startsWith("counter") || moveset[i].name.toLowerCase().startsWith("mirror coat"))
				moveDamage = moveset[i].getProjectedDamage(defender);
			else
				moveDamage = calculateDamage(attacker, moveset[i],defender);
			//Factor in accuracy.
			int moveAdjustedDamage = (moveDamage.x * moveset[i].accuracy) / 100;
			//If we're going to kill something regardless of what move we use, use the move with the higher accuracy.
			//TODO: Also factor in stat drops and PP counts.
			if(	moveAdjustedDamage > adjustedDamage && (damage.x < defender.getHealth() || moveset[i].accuracy > use.accuracy) || 
				(moveAdjustedDamage == adjustedDamage || moveDamage.x >= defender.getHealth()) && moveset[i].accuracy > use.accuracy)
			{
				damage=moveDamage;
				use = moveset[i];
				adjustedDamage = moveAdjustedDamage;
			}
		}
		if(use == null)
		{
			System.err.println(attacker.getName()+" could not find move to use, using Struggle.");
			use = new Move("Struggle", attacker, false);
		}
		return use;
	}
	
	
	public static int turnsToKill(int health, int damage)
	{
		if(damage == 0)
			return Integer.MAX_VALUE;
		return (int)Math.floor(health / damage);
	}
}