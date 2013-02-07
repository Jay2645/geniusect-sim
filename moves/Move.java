/*
 * A class representing a move.
 * Keeps track of potential damage to known enemy Pokemon.
 * @author TeamForretress
 */

package geniusectsim.moves;

import geniusectsim.battle.EntryHazard;
import geniusectsim.battle.Type;
import geniusectsim.battle.Weather;
import geniusectsim.bridge.Simulator;
import geniusectsim.constants.Pokequations;
import geniusectsim.constants.SQLHandler;
import geniusectsim.pokemon.Pokemon;
import geniusectsim.pokemon.Stat;
import geniusectsim.pokemon.Status;
import geniusectsim.pokemon.VolatileStatus;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class Move {
	public String name;
	public String shortname;
	public Pokemon user;
	public int pp;
	public int power;
	public int accuracy;
	protected Type type;
	public Target target;
	public Status condition = null; //A status condition to inflict upon a target.
	public Map<VolatileStatus, Target> vol = new HashMap<VolatileStatus, Target>(); //The volatile status condition caused and its target.
	public int boosts[] = {0,0,0,0,0,0};
	protected MoveType moveType = MoveType.Status;
	public int boostChance = 0;
	public int recoilPercent = 0;
	public int priority = 0;
	public Weather weatherChange = null;
	
	public boolean isContact = false;
	public boolean disabled = false;
	
	protected Map<Pokemon, Point> projectedDamage = new HashMap<Pokemon,Point>(6);
	protected Map<Pokemon, Point> projectedPercent = new HashMap<Pokemon, Point>(6);
	
	public Move(){}
	
	public Move(String n, Pokemon p, boolean isShortname)
	{
		user = p;
		if(isShortname)
		{
			shortname = n;
			if(shortname.contains("hiddenpower"))
			{
				name = "Hidden Power";
				pp = 24;
				power = 70;
				accuracy = 100;
				//type = Type.Normal;
				moveType = MoveType.Special;
			}
			else
				SQLHandler.queryMoveShortname(this);
			if(shortname.contains("raindance"))
				weatherChange = Weather.Rain;
			else if(shortname.contains("sunnyday"))
				weatherChange = Weather.Sun;
			else if(shortname.contains("sandstorm"))
				weatherChange = Weather.Sandstorm;
			else if(shortname.contains("hail"))
				weatherChange = Weather.Hail;
		}
		else
		{
			int i = n.indexOf("\n");
			if(i == -1)
				name = n;
			else
				name = n.substring(0, i);
			if(name.toLowerCase().startsWith("struggle"))
			{
				pp = Integer.MAX_VALUE;
				power = 50;
				accuracy = 100;
				type = Type.None;
				recoilPercent = 25;
				moveType = MoveType.Physical;
			}
			else if(name.toLowerCase().startsWith("hidden power"))
			{
				name = "Hidden Power";
				pp = 24;
				power = 70;
				accuracy = 100;
				//type = Type.Normal;
				moveType = MoveType.Special;
			}
			else
				SQLHandler.queryMove(this);
			if(name.toLowerCase().contains("rain dance"))
				weatherChange = Weather.Rain;
			else if(name.toLowerCase().contains("sunny day"))
				weatherChange = Weather.Sun;
			else if(name.toLowerCase().contains("sandstorm"))
				weatherChange = Weather.Sandstorm;
			else if(name.toLowerCase().contains("hail"))
				weatherChange = Weather.Hail;
		}
		if(	name.toLowerCase().contains("overheat") || name.toLowerCase().contains("draco meteor") || //Hardcode these for now.
			name.toLowerCase().contains("leaf storm") || name.toLowerCase().contains("psycho boost"))
		{
			boosts[Stat.SpA.toInt()] = -2;
			boostChance = 1;
		}
	}
	
	public void onMoveUsed(Pokemon enemy, int damageDone, boolean wasCrit)
	{
		//Called when this move is used.
		if(!user.isAlive() || enemy == null)
			return;
		if(user.getItem() != null && user.getItem().name != null && user.getItem().name.toLowerCase().startsWith("choice"))
		{
			user.setLockedInto(this);
			Move[] moveset = user.getMoveset();
			for(int i = 0; i < moveset.length; i++)
			{
				if(moveset[i] == null || moveset[i].name.equals(name))
					continue;
				moveset[i].disabled = true;
			}
		}
		pp = Simulator.getMoveRemainingPP(this, enemy.getAbility());
		if(0 >= pp)
		{
			disabled = true;
		}
		if(shortname != null && !shortname.isEmpty())
		{
			if(shortname.contains("stealthrock"))
			{
				enemy.getTeam().addHazard(EntryHazard.StealthRock);
				return;
			}
			else if(shortname.contains("toxicspikes"))
			{
				enemy.getTeam().addHazard(EntryHazard.ToxicSpikes);
				return;
			}
			else if(shortname.contains("spikes"))
			{
				enemy.getTeam().addHazard(EntryHazard.Spikes);
				return;
			}
			else if(shortname.contains("rapidspin"))
			{
				user.getTeam().removeHazards();
			}
			else if(shortname.contains("powerswap"))
			{
				int[] boosts = user.getBoosts();
				user.giveBoosts(enemy.getBoosts());
				enemy.giveBoosts(boosts);
			}
		}
		if(weatherChange != null)
		{
			//TODO: Adjust duration of weather moves based upon the held item.
			weatherChange.setDuration(5);
			user.getTeam().getBattle().setWeather(weatherChange);
		}
		if(!projectedPercent.containsKey(enemy));
		{
			adjustProjectedPercent(Pokequations.calculateDamagePercent(user,this,enemy),enemy);
		}
		if(!withinExpectedRange(damageDone,enemy,wasCrit))
		{
			//TODO: Work out defense stat.
		}
	}
	
	public void setType(Type newType)
	{
		type = newType;
	}
	
	public Type getType()
	{
		return type;
	}
	
	public boolean withinExpectedRange(int damage, Pokemon p, boolean wasCrit)
	{
		if(wasCrit && (user.getAbility() == null || !user.getAbility().getName().toLowerCase().startsWith("sniper")))
		{
			damage /= 2;
		}
		else if(wasCrit)
		{
			damage /= 3;
		}
		if(!projectedPercent.containsKey(p))
			adjustProjectedPercent(Pokequations.calculateDamagePercent(user,this,p),p);
		if(damage < projectedPercent.get(p).x || damage > projectedPercent.get(p).y)
			return false;
		else return true;
	}
	
	public void adjustProjectedPercent(Point newProjection, Pokemon p)
	{
		//Adjust our projected damage against a Pokemon.
		Point damage = new Point(Integer.MAX_VALUE, Integer.MIN_VALUE);
		if(projectedPercent.containsKey(p))
		{
			damage = projectedPercent.get(p);
		}
		damage.x = Math.min(newProjection.x, damage.x);
		damage.y = Math.max(damage.x, newProjection.y);
		projectedPercent.put(p, damage);
	}
	
	public int useMove(boolean bestCase,Pokemon us, Pokemon enemy)
	{
		int damage = 0;
		if(bestCase)
		{
			damage = Pokequations.calculateDamagePercent(us, this, enemy).y;
			for(int i = 0; i < 6; i++)
			{
				boostStats(Stat.fromInt(i));
			}
		}
		else
		{
			damage = Pokequations.calculateDamagePercent(us, this, enemy).x;
			if(boostChance == 1)
			{
				for(int i = 0; i < 6; i++)
				{
					boostStats(Stat.fromInt(i));
				}
			}  //It's more accurate to get stat drops straight from Showdown and apply them there.
		}
		return damage;
	}
	
	public Point getProjectedPercent(Pokemon enemy)
	{
		
		if(projectedPercent.containsKey(enemy))
		{
			return projectedPercent.get(enemy);
		}
		else
		{
			Point damage = Pokequations.calculateDamagePercent(user, this, enemy);
			projectedPercent.put(enemy, damage);
			return damage;
		}
	}
	
	public int getProjectedPercent(Pokemon enemy, boolean most)
	{
		if(most)
			return getProjectedPercent(enemy).y;
		else return getProjectedPercent(enemy).x;
	}
	
	public Point getProjectedDamage(Pokemon enemy)
	{
		
		if(projectedDamage.containsKey(enemy))
		{
			return projectedDamage.get(enemy);
		}
		else
		{
			Point damage = Pokequations.calculateDamage(user, this, enemy);
			projectedDamage.put(enemy, damage);
			return damage;
		}
	}
	
	public int getProjectedDamage(Pokemon enemy, boolean most)
	{
		if(most)
			return getProjectedDamage(enemy).y;
		else return getProjectedDamage(enemy).x;
	}
	
	public boolean getContact()
	{
		return isContact;
	}
	
	public void boostStats(Stat boost)
	{
		if(boosts[boost.toInt()] == 0)
			return;
		user.giveBoosts(boost, boosts[boost.toInt()]);
	}
	
	public MoveType getMoveType()
	{
		return moveType;
	}
	
	/**
	 * Sets this move's MoveType to the specified MoveType.
	 * @param type (MoveType): The new MoveType.
	 */
	public void setMoveType(MoveType type)
	{
		moveType = type;
	}

	/**
	 * @return
	 */
	public boolean isPhysical() 
	{
		return moveType == MoveType.Physical;
	}
	
	public boolean isSpecial()
	{
		return moveType == MoveType.Special;
	}
	
	public static Move clone(Move clone)
	{
		Move move;
		if(clone instanceof HiddenPower)
			move = new HiddenPower(clone);
		else
			move = new Move();
		move.name = clone.name;
		move.shortname = clone.shortname;
		move.user = clone.user;
		move.pp = clone.pp;
		move.power = clone.power;
		move.accuracy = clone.accuracy;
		move.type = clone.type;
		move.target = clone.target;
		move.boosts = clone.boosts;
		move.moveType = clone.getMoveType();
		move.isContact = clone.getContact();
		move.boostChance = clone.boostChance;
		move.recoilPercent = clone.recoilPercent;
		move.disabled = clone.disabled;
		move.projectedDamage = clone.projectedDamage;
		move.projectedPercent = clone.projectedPercent;
		return move;
	}
}
