
/*
 * A class representing a Pokemon.
 * Keeps track of type, name, resistances, etc.
 * Also fills itself out with data on an unknown Pokemon as the match goes on.
 * @author TeamForretress
 */

package geniusectsim.pokemon;

import geniusectsim.abilities.Ability;
import geniusectsim.actions.Attack;
import geniusectsim.actions.Change;
import geniusectsim.battle.Damage;
import geniusectsim.battle.Team;
import geniusectsim.battle.Type;
import geniusectsim.bridge.Simulator;
import geniusectsim.constants.Pokequations;
import geniusectsim.constants.SQLHandler;
import geniusectsim.items.Item;
import geniusectsim.moves.HiddenPower;
import geniusectsim.moves.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pokemon {
	protected String name;
	protected String nickName;
	private String importable;
	protected int id = -1;
	protected Item item;
	protected Team team;
	protected int level = 100;
	protected Type[] types = {Type.None, Type.None};
	protected Ability ability;
	
	protected Ability abilityZero;
	protected Ability abilityOne;
	protected Ability abilityDW;
	protected Nature nature = Nature.Hardy;
	protected Tier tier;
	
	protected ArrayList<Type> immunities = new ArrayList<Type>();
	
	protected Move[] moveset = new Move[4];
	protected String[] movesUsedSinceDeploy = new String[4];
	protected Move lastMoveUsed = null;
	
	protected int[] base = {0,0,0,0,0,0};
	protected int[] ivs = {31,31,31,31,31,31};
	protected int[] evs = {0,0,0,0,0,0};
	protected int[] stats = new int[6]; //Stats before boosts.
	protected int[] boosts = {0,0,0,0,0,0};
	protected int[] boostedStats = new int[6]; // Stats after boosts.
	protected int fullHP; //How big of an HP stat we have at full HP.
	protected int hpPercent = 100; //Our current HP percent.
	protected int evsLeft = 510; //Legality check for our EV calculations.
	
	protected boolean lead = false; //Is this the lead?
	protected boolean alive = true; //Is this Pokemon alive?
	protected boolean active = false; //Is this Pokemon active?
	protected boolean canMove = true; //Can we move?
	protected boolean canSwitch = true; //Can we switch?
	protected boolean charged = false; //Is our move recharged?
	protected Move lockedInto = null; //Are we locked into a move (i.e. Outrage, Choice, etc.)?
	protected Status status = Status.None; //What permanent status do we have (i.e. Poison, Burn, etc.)?
	protected ArrayList<VolatileStatus> effects = new ArrayList<VolatileStatus>(); //What temporary status do we have (i.e. Confused, Taunt, etc.)?
	protected Pokemon enemy;
	protected Team enemyTeam;
	protected int damageDoneLastTurn;
	public int ragequits = 0;
	protected ArrayList<String> killNames = new ArrayList<String>();
	
	public Pokemon() {}
	
	public Pokemon(Pokemon p)
	{
		clone(p);
	}
	
	public Pokemon(String n, String nick, Team t)
	{
		name = n;
		team = t;
		nickName = nick;
		enemyTeam = Team.getEnemyTeam(team.getTeamID());
		query();
	}
	
	public void query()
	{
		SQLHandler.queryPokemon(this);
		stats = Pokequations.calculateStat(this);
		boostedStats = stats;
		fullHP = boostedStats[Stat.HP.toInt()];
		if(team == null)
			team = enemy.enemyTeam;
		if(team.getTeamID() == 0)
		{
			Simulator.getMoves(this);
		}
		//if(team.getActive() == null)
			//onSendOut();
	}
	
	public void onSendOut()
	{
		//Called when this Pokemon enters the battle.
		damage(Change.calculateSwitchDamagePercent(this));
		//if(!alive)
			//return;
		active = true;
		enemyTeam = Team.getEnemyTeam(team.getTeamID());
		if(enemyTeam != null)
			enemy = enemyTeam.getActive();
		if(enemy != null)
			enemy.changeEnemy(this);
		team.setActive(this);
		if(ability != null)
			ability.onSendOut();
		else
			System.out.println("(Pokemon) "+name+"'s ability is null.");
		status.resetActive();
	}
	
	public void onWithdraw()
	{
		//Called when this Pokemon withdraws from the battle.
		active = false;
		team.setActive(null);
		resetBoosts();
		effects.clear();
		movesUsedSinceDeploy = new String[4];
		lockedInto = null;
		for(int i = 0; i < moveset.length; i++)
		{
			if(moveset[i] == null || moveset[i].pp <= 0)
				continue;
			moveset[i].disabled = false;
		}
	}
	
	public void onDie()
	{
		//Called when the Pokemon dies.
		hpPercent = 0;
		if(ability != null)
			ability.onFaint(enemy,enemy.lastMoveUsed);
		System.err.println(name+" has died!");
		alive = false;
		if(enemy != null)
			enemy.onKill(name);
		if(!active)
			onWithdraw();
	}
	
	public void onKill(String enemyName)
	{
		killNames.add(enemyName);
	}
	
	public int onNewAttack(Damage damage)
	{
		//Whereas the method below takes the moves that THIS Pokemon did, this method takes a move the OTHER Pokemon did.
		//It returns the amount of damage done in this turn.
		int preHP = hpPercent;
		damage.applyDamage();
		for(int i = 0; i < effects.size(); i++)
		{
			effects.get(i).onNewTurn();
		}
		status.onNewTurn();
		damageDoneLastTurn = preHP - hpPercent;
		System.out.println(name+" took "+damageDoneLastTurn+"% damage.");
		damage.attacker.onNewTurn(damage.attack.name, damageDoneLastTurn, false);
		
		return damageDoneLastTurn;
	}
	
	public void onNewTurn(String n, int damageDone, boolean crit)
	{
		//Called when this Pokemon uses a move.
		//Keeps track of what moves THIS Pokemon has done (if unknown) and what damage they did to the enemy.
		if(enemy == null)
			enemy = enemyTeam.getActive(); //So we can properly simulate the right team.
		Move moveUsed = addMove(n);
		if(moveUsed != null)
		{
			moveUsed.onMoveUsed(enemy, damageDone, crit);
		}
		if(ability != null)
			ability.onNewTurn();
		status.onNewTurn();
	}
	
	public int restoreHP(int restorePercent)
	{
		if(restorePercent < 0)
			restorePercent *= -1;
		int restoreAmount = restorePercent + hpPercent;
		if(restoreAmount > 100)
		{
			int difference = restoreAmount - 100;
			restoreAmount -= difference;
		}
		hpPercent = restoreAmount;
		return restoreAmount;
	}
	
	public boolean damage(int damagePercent)
	{
		hpPercent -= damagePercent;
		if(hpPercent < 0)
			hpPercent = 0;
		return isAlive();
	}
	
	public boolean damage(Attack attack)
	{
		return damage(attack.move.getProjectedPercent(this).y);
	}
	
	public void removeStatus(VolatileStatus status)
	{
		effects.remove(status);
	}
	
	/*
	 * 
	 * Query methods below here.
	 * 
	 */
	
	public int checkHP()
	{
		if(hpPercent <= 0 && alive)
		{
			onDie();
		}
		return hpPercent;
	}
	
	public boolean isAlive()
	{
		checkHP();
		return alive;
	}
	
	public boolean teamExists()
	{
		return team == null;
	}
	
	public boolean canMove()
	{
		return canMove;
	}
	
	public void canMove(boolean can)
	{
		canMove = can;
	}
	
	public boolean isFasterThan(Pokemon compare)
	{
		return compare.boostedStats[Stat.Spe.toInt()] < boostedStats[Stat.Spe.toInt()];
	}
	
	public boolean isFasterThan(int speed)
	{
		//Useful for making self faster than "Magic Numbers" when building a team.
		return speed < boostedStats[Stat.Spe.toInt()];
	}
	
	public int[] getBoostedStats()
	{
		return boostedStats;
	}
	
	public int getBoostedStat(Stat stat)
	{
		return boostedStats[stat.toInt()];
	}
	
	public void changeEnemy(Pokemon e)
	{
		enemy = e;
	}
	
	public int getHealth()
	{
		return hpPercent;
	}
	
	public void printKills()
	{
		if(killNames.isEmpty())
			return;
		System.out.println(name+" kills:");
		for(int i = 0; i < killNames.size(); i++)
		{
			System.err.println(killNames.get(i));
		}
	}
	
	/**
	 * Adds the move to the moveset. If the moveset is full, returns null. If the move is already in the moveset, returns that move.
	 * @param moveName (String): The name of the move we're trying to add.
	 * @return The move to add.
	 */
	public Move addMove(String moveName)
	{
		return addMove(moveName, false);
	}
	
	public Move[] addMove(List<String> moveNames, boolean shortname)
	{
		String[] moveArray = new String[4];
		moveArray = moveNames.toArray(moveArray);
		return addMove(moveArray, shortname);
	}
	
	public Move[] addMove(String[] moveNames, boolean shortname)
	{
		if(moveNames.length > 4)
			return null;
		for(int i = 0; i < moveNames.length; i++)
		{
			if(moveNames[i] == null || moveNames[i].toLowerCase().contains("struggle"))
				continue;
			moveset[i] = new Move(moveNames[i], this, shortname);
		}
		return moveset;
	}
	
	/**
	 * Adds the move to the moveset. If the moveset is full, returns null. If the move is already in the moveset, returns that move.
	 * @param moveName (String): The name of the move we're trying to add.
	 * @param shortname (boolean): Is this the move's short name?
	 * @return The move to add.
	 */
	public Move addMove(String moveName, boolean shortname)
	{
		System.out.println(name+" is adding "+moveName+" to its moveset.");
		for(int i = 0; i < 4; i++)
		{
			if(movesUsedSinceDeploy[i] != null && movesUsedSinceDeploy[i].equals(moveName))
				break;
			if(movesUsedSinceDeploy[i] == null)
				movesUsedSinceDeploy[i] = moveName;
		}
		for(int n = 0; n < moveset.length; n++)
		{
			//First iterate through all moves to make sure we don't already have this move.
			if(moveset[n] != null && (	moveset[n].name.toLowerCase().startsWith(moveName.toLowerCase()) && !shortname || 
										moveset[n].shortname.equals(moveName) && shortname))
			{
				System.out.println(name+" already has move "+moveName+"!");
				return moveset[n];
			}
		}
		Move move = null;
		if(team == null)
			team = Team.lookupPokemon(this);
		for(int i = 0; i < moveset.length; i++)
		{
			if(moveset[i] == null || moveset[i].name.toLowerCase().startsWith("struggle") && !moveName.toLowerCase().startsWith("struggle"))
			{
				System.err.println("Adding "+moveName+" to "+name+"'s move list.");
				moveset[i] = new Move(moveName,this,shortname);
				if(moveName.toLowerCase().startsWith("hidden power"))
				{
					HiddenPower hp = new HiddenPower(moveset[i]); 
					if(moveset[i].type == null)
					{
						//TODO: Calculate hidden power type.
					}
					else
					{
						hp.setType(moveset[i].type);
					}
					moveset[i] = hp;
				}
				move = moveset[i];
				break;
			}
		}
		return move;
	}
	
	/**
	 * Returns this Pokemon's item.
	 * @return This Pokemon's held item.
	 */
	public Item getItem()
	{
		return item;
	}
	
	/**
	 * Gets the type at the specified index.
	 * @param id - The index of the type. Can be 0 or 1.
	 * @return Type - the type at that index.
	 */
	public Type getType(int id)
	{
		return types[id];
	}
	
	/**
	 * Returns our nature.
	 * @return Nature - our Nature.
	 */
	public Nature getNature()
	{
		return nature;
	}
	
	/**
	 * Returns our ability.
	 * @return Ability - our Ability.
	 */
	public Ability getAbility()
	{
		if(ability == null)
		{
			double score = -11;
			Ability likely = null;
			if(abilityZero != null)
			{
				score = abilityZero.getScore();
				likely = abilityZero;
			}
			if(abilityOne != null)
			{
				double oneScore = abilityOne.getScore();
				if(oneScore > score)
				{
					score = oneScore;
					likely = abilityOne;
				}
			}
			//TODO: Check if DW ability is released.
			if(abilityDW != null)
			{
				double dwScore = abilityDW.getScore();
				if(dwScore > score)
				{
					score = dwScore;
					likely = abilityDW;
				}
			}
			return likely;
		}
		return ability;
	}
	
	public Type[] getImmunities()
	{
		if(immunities.isEmpty())
			return new Type[0];
		Type[] immune = new Type[1];
		immune = (Type[]) immunities.toArray(immune);
		return immune;
	}
	
	public void addImmunity(Type immunity)
	{
		immunities.add(immunity);
	}
	
	public int getDamageDone()
	{
		return damageDoneLastTurn;
	}
	
	public boolean hasMove(String moveName)
	{
		for(int i = 0; i < moveset.length; i++)
		{
			if(moveset[i] == null)
				continue;
			if(moveset[i].name.toLowerCase().startsWith(moveName))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Sets a move as the move we are forced to use.
	 * @param move (Move): The move we are forced to use.
	 */
	public void setLockedInto(Move move)
	{
		lockedInto = move;
	}
	
	/**
	 * Gets the move we are locked into, if any.
	 * @return The move we are locked into.
	 */
	public Move getLockedInto() {
		return lockedInto;
	}
	
	/**
	 * Returns the move in our moveset at index i. If we can't use that move, returns null.
	 * @param i - The index of this Pokemon, as an int.
	 * @return Move - the move in slot i.
	 */
	public Move getMove(int i)
	{
		if(moveset[i] == null || moveset[i].disabled || moveset[i].name.toLowerCase().startsWith("struggle"))
			return null;
		return moveset[i];
	}
	
	/**
	 * Sets the team to the specified team.
	 * @param t (Team): The Team to set our team to.
	 */
	public void setTeam(Team t)
	{
		team = t;
	}
	
	/**
	 * Returns all the moves in our moveset.
	 * If we have no moves in our moveset, returns an empty array.
	 * @return Move[] - The moves in our moveset.
	 */
	public Move[] getMoveset()
	{
		if(moveset[0] == null)
			return new Move[0];
		return moveset;
	}
	
	/**
	 * Returns all our EVs.
	 * @return int[] - Our EVs.
	 */
	public int[] getEVs()
	{
		return evs;
	}
	
	/**
	 * Gets the enemy of this Pokemon.
	 * @return Pokemon - The enemy of this Pokemon.
	 */
	public Pokemon getEnemy()
	{
		return enemy;
	}
	
	/**
	 * Gets the Team of this Pokemon.
	 * @return Team - The team of this Pokemon.
	 */
	public Team getTeam()
	{
		return team;
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean nameIs(String s)
	{
		return name.toLowerCase().startsWith(s.toLowerCase());
	}
	
	public void chargeMove()
	{
		inflictStatus(VolatileStatus.Charging);
		canMove = false;
		canSwitch = false;
	}
	
	public void setAbility(String a)
	{
		if(a == null || a.isEmpty() || ability != null && ability.getName().contains(a))
			return;
		ability = new Ability(a, this);
	}
	
	public void setAbility(Ability a)
	{
		ability = a;
	}
	
	public int getFullHP()
	{
		return fullHP;
	}
	
	public double getSTAB()
	{
		if(ability == null)
			return 1.5;
		return ability.getSTAB();
	}
	
	public double getAbilityModifier()
	{
		if(ability == null)
			return 1;
		return ability.getModifier();
	}
	
	public Pokemon[] getPokemonTeam()
	{
		if(team == null)
			query();
		return team.getPokemon();
	}
	
	/**
	 * Takes a stat and returns the base stat for that stat.
	 * @param stat (Stat): The Stat to get the base stat for.
	 * @return (int): The base stat for that stat.
	 */
	public int getBaseStat(Stat stat) 
	{
		return base[stat.toInt()];
	}

	/**
	 * Returns this Pokemon's level.
	 * @return (int): This Pokemon's level.
	 */
	public int getLevel() 
	{
		return level;
	}

	/**
	 * Takes a stat and returns the EV-adjusted stat for that stat.
	 * @param stat (Stat): The stat to get the adjusted stat for.
	 * @return (int): The EV-adjusted stat.
	 */
	public int getStats(Stat stat) 
	{
		return stats[stat.toInt()];
	}

	/**
	 * Takes a stat and returns the IV for that stat.
	 * @param stat (Stat): The stat to get the IV for.
	 * @return (int): The IV for that stat.
	 */
	public int getIVs(Stat stat) 
	{
		return ivs[stat.toInt()];
	}
	
	/**
	 * Takes a stat and returns the EVs for that stat.
	 * @param stat (Stat): The stat to get the EVs for.
	 * @return (int): The EVs for that stat.
	 */
	public int getEVs(Stat stat) 
	{
		return evs[stat.toInt()];
	}
	
	/**
	 * Returns what types we are.
	 * @return (Type[]): Our types.
	 */
	public Type[] getTypes() 
	{
		return types;
	}
	
	/**
	 * Sets this Pokemon's base stat.
	 * @param index (int): The index of the stat to set (use Stat.toInt() if you are unsure of a Stat's index).
	 * @param baseStat (int): The base stat to set it to.
	 * @see geniusectsim.simulator.pokemon.Stat#toInt()
	 */
	public void setBaseStat(int index, int baseStat) {
		base[index] = baseStat;
	}

	/**
	 * Sets this Pokemon's tier.
	 * @param newTier (Tier): This Pokemon's new tier.
	 */
	public void setTier(Tier newTier)
	{
		tier = newTier;
	}
	
	/**
	 * Sets this Pokemon's tier.
	 * @param string (String): This Pokemon's tier.
	 */
	public void setTier(String string) {
		tier = Tier.tierFromString(string);
	}

	/**
	 * Gets this Pokemon's tier.
	 * @return (Tier): This Pokemon's tier.
	 */
	public Tier getTier() {
		return tier;
	}

	/**
	 * Sets this Pokemon's types.
	 * @param type1 (Type): This Pokemon's primary type.
	 * @param type2 (Type): This Pokemon's secondary type (can be Type.None).
	 */
	public void setType(Type type1, Type type2) {
		types[0] = type1;
		types[1] = type2;
	}
	
	/**
	 * Gets our ID (position in the team, between 0 and 5 inclusive) and returns it.
	 * @return (int): Our ID.
	 */
	public int getID() 
	{
		return id;
	}
	
	/*
	 * 
	 * Logic methods below here.
	 * 
	 */
	
	public void adjustEVs(int[] newEVs)
	{
		//Adjust our EV spread.
		evsLeft = 510;
		evs = new int[6];
		for(int i = 0; i < evs.length; i++)
		{
			adjustEVs(i,newEVs[i]);
		}
	}
	
	public void adjustEVsNoCheck(int[] newEVs)
	{
		evs = newEVs;
		for(int index = 0; index > evs.length; index++)
		{
			evsLeft -= evs[index];
			Pokequations.calculateStat(Stat.fromInt(index),this);
			if(Stat.fromInt(index) == Stat.HP)
			{
				fullHP = stats[Stat.HP.toInt()];
				percentToHP();
			}
		}
	}
	
	public void adjustEVs(int index, int newEV)
	{
		if(Stat.fromInt(index) == Stat.HP)
		{
			stats[Stat.HP.toInt()] = fullHP;
		}
		evsLeft += (evs[index] - newEV);
		int check = evsLeft - newEV;
		System.err.println(check);
		if(check < 0)
		{
			System.err.println("EV spread is invalid!");
			return;
		}
		evs[index] = newEV;
		Pokequations.calculateStat(Stat.fromInt(index),this);
		if(Stat.fromInt(index) == Stat.HP)
		{
			fullHP = stats[Stat.HP.toInt()];
			percentToHP();
		}
	}
	
	public void inflictStatus(Status s)
	{
		if(status == Status.None || s == Status.Rest || s == Status.None)
		{
			status = s;
		}
	}
	
	public void inflictStatus(VolatileStatus s)
	{
		effects.add(s);
		s.inflict(this);
	}
	
	public void giveBoosts(Stat boost, int count)
	{
		boosts[boost.toInt()] += count;
		if(boosts[boost.toInt()] > 6)
			boosts[boost.toInt()] = 6;
		else if(boosts[boost.toInt()] < -6)
			boosts[boost.toInt()] = -6;
		boostedStats[boost.toInt()] = Pokequations.statBoost(boosts[boost.toInt()],stats[boost.toInt()]);
		System.err.println(name+"'s new "+boost+" stat is "+boosts[boost.toInt()]);
	}
	
	public int hpToPercent()
	{
		hpPercent = (int)Math.round(stats[Stat.HP.toInt()] / fullHP);
		return hpPercent;
	}
	
	public int hpToPercent(int hp)
	{
		return (int)Math.round((hp / fullHP) * 100);
	}
	
	public int percentToHP()
	{
		stats[Stat.HP.toInt()] = (int)Math.round(fullHP * (hpPercent / 100));
		return stats[Stat.HP.toInt()];
	}
	
	public void resetBoosts()
	{
		for(int i = 0; i < boosts.length; i++)
		{
			boosts[i] = 0;
		}
		boostedStats = stats;
	}
	
	public Pokemon clone(Pokemon clone)
	{
		System.out.println("Cloning "+clone.name+" ("+clone.hpPercent+" HP).");
		name = clone.name;
		id = clone.id;
		item = clone.item;
		team = clone.team;
		level = clone.level;
		types = clone.types;
		ability = clone.ability;
		nature = clone.nature;
		tier = clone.tier;
		
		immunities = clone.immunities;
		
		for(int i = 0; i < 4; i++)
		{
			if(clone.moveset[i] == null)
				continue;
			moveset[i] = new Move(clone.moveset[i]);
		}
		
		base = clone.base;
		ivs = clone.ivs;
		evs = clone.evs;
		stats = clone.stats;
		boosts = clone.boosts;
		boostedStats = clone.boostedStats;
		fullHP = clone.fullHP;
		hpPercent = clone.hpPercent;
		evsLeft = clone.evsLeft;
		
		lead = clone.lead;
		alive = clone.alive;
		canMove = clone.canMove;
		status = clone.status;
		effects = clone.effects;
		enemy = clone.enemy;
		return this;
	}
	
	
	
	/*
	 * 
	 * Static methods below here.
	 * 
	 */
	
	public static Pokemon loadFromText(String importable, Team t, int count)
	{
		if(importable.isEmpty())
			return null;
		System.out.println("Loading importable: " + importable);
		Pokemon found = null;
		Boolean evsFound = false;
		
		
		Pattern p = Pattern.compile("(.+) @ (.+)\\s+Trait: (.+)\n.+\\s+(.+)Nature\\s+", Pattern.MULTILINE);
		Matcher m = p.matcher(importable);
		if(m.find()) // only need 1 find for this
		{
			found = new Pokemon();
			found.id = count;
			found.team = t;
			// name: Hard To Please (Ninetales) (F)
			String tempname = importable.substring(m.start(1), m.end(1));
			if (tempname.contains("("))
			{
				Pattern nameP = Pattern.compile(" \\((.+?)\\)");
				Matcher nameM = nameP.matcher(tempname);
				nameM.find();
				if (nameM.end(1) - nameM.start(1) > 1)
				{
					found.name = nameM.group(1);
				}
				else
				{
					found.name = tempname.substring(0, nameM.start(1) - 2);
				}
			}
			else
			{
				found.name = tempname;
			}
			
			found.item = new Item(importable.substring(m.start(2), m.end(2)));
			found.setAbility(importable.substring(m.start(3), m.end(3)));
			found.nature = Nature.fromString(importable.substring(m.start(4), m.end(4)));
							
			System.out.println("name: " + found.name);
			System.out.println("item: " + found.item.name);
			System.out.println("trait: " + found.ability.getName());
			System.out.println("nature: " + found.nature.toString());
		}
		String[] evP = {"(\\d+) HP","(\\d+) Atk","(\\d+) Def","(\\d+) SAtk","(\\d+) SDef","(\\d+) Spd"};
		int[] evDist = new int[6];
		for (int i = 0; i < 6; ++i)
		{
			evDist[i] = 0;
			Matcher m2 = Pattern.compile(evP[i], Pattern.MULTILINE).matcher(importable);
			if (m2.find())
			{
				evDist[i] = Integer.parseInt(importable.substring(m2.start(1), m2.end(1)));
				evsFound = true;
			}
		}
		System.out.println("Stats: ");
		System.out.printf("%d hp, %d atk, %d def, %d spa, %d spd, %d spe\n", evDist[0], evDist[1], evDist[2], evDist[3], evDist[4], evDist[5]);
					
		Pattern moveP = Pattern.compile("- (.+)\\n*", Pattern.MULTILINE);
		m = moveP.matcher(importable);
		String moves[] = new String[4];
		boolean movesFound = false;
		int i = 0;
		while (m.find())
		{
			movesFound = true;
			moves[i] = importable.substring(m.start(1), m.end(1));
			Pattern hpP = Pattern.compile("Hidden Power \\[(.+)\\]");
			Matcher hpM = hpP.matcher(moves[i]);
			if(hpM.find())
			{
				moves[i] = "Hidden Power "+hpM.group(1);
			}
			i++;
		}
		if(found == null){
			System.out.format("No Pokemon was found.%n");
			return null;
		}
		if(movesFound)
		{
			found.addMove(moves, false);
		}
		if(evsFound)
		{
			found.evs = evDist;
		}
		else
		{
			System.out.format("No EVs were found.%n");
		}
		found.query();
		System.err.println(found.getMove(0).name);
		return found;
	}

	/**
	 * Resets all this Pokemon's moves and populates from a String array.
	 * @param newMoves (List<String>): The list of new moves to populate the array with.
	 */
	public void resetMoves(List<String> newMoves) 
	{
		for(int i = 0; i < newMoves.size(); i++)
		{
			addMove(newMoves.get(i));
		}
	}

	/**
	 * Gets if we can switch or not.
	 * @return TRUE if we can switch, else FALSE.
	 */
	public boolean canSwitch() 
	{
		canSwitch = Simulator.canSwitch();
		return canSwitch;
	}

	/**
	 * Returns the boosts of the specified stat.
	 * @param stat (Stat): The stat to check.
	 * @return (int): The number of boosts we have to that stat.
	 */
	public int getBoosts(Stat stat) 
	{
		return boosts[stat.toInt()];
	}

	/**
	 * Sets a Stat to a different number.
	 * @param stat (Stat): The stat to change.
	 * @param newStat (int): The number to change it to.
	 */
	public void setStat(Stat stat, int newStat) 
	{
		stats[stat.toInt()] = newStat;
		boostedStats[stat.toInt()] = Pokequations.statBoost(boosts[stat.toInt()],stats[stat.toInt()]);
	}

	/**
	 * Sets our enemy to the specified enemy.
	 * @param newEnemy (Pokemon): The new enemy to set it to.
	 */
	public void setEnemy(Pokemon newEnemy) 
	{
		enemy = newEnemy;
	}

	/**
	 * Returns our current status.
	 * @return (Status): Our current status.
	 */
	public Status getStatus() 
	{
		return status;
	}

	/**
	 * Takes a string and returns the move with that name.
	 * If there is no move with that name, returns null.
	 * @param string (String): The name of the move in question.
	 * @return (Move): The move we're going to use.
	 */
	public Move getMove(String string) 
	{
		for(int i = 0; i < moveset.length; i++)
		{
			if(moveset[i].name.toLowerCase().contains(string.toLowerCase()))
				return moveset[i];
		}
		return null;
	}

	/**
	 * Sets our HP to the specified values.
	 * @param hp (int): Our current HP value.
	 * @param maxHP (int): Our maximum HP value.
	 */
	public void setHP(int hp, int maxHP) 
	{
		if(hp > 0)
			alive = true;
		else
			alive = false;
		if(maxHP != 100)
		{
			fullHP = maxHP;
			stats[Stat.HP.toInt()] = maxHP;
		}
		double hpAmount = (hp / maxHP) * 100;
		hpPercent = (int)Math.round(hpAmount);
	}

	/**
	 * Clears our move list.
	 */
	public void clearMoves() 
	{
		moveset = new Move[4];
	}

	/**
	 * Adds a new move to our move list.
	 * @param newMove (Move): The move to add.
	 */
	public void addMove(Move newMove) 
	{
		for(int i = 0; i < moveset.length; i++)
		{
			if(moveset[i] != null)
			{
				if(moveset[i].name.equals(newMove.name))
					break;
				else continue;
			}
			moveset[i] = newMove;
			break;
		}
	}
	
	public Ability setPossibleAbilties(String abilityName, int abilityIndex)
	{
		if(abilityName == null)
			return null;
		if(abilityIndex == 0)
		{
			abilityZero = new Ability(abilityName,this);
			return abilityZero;
		}
		else if(abilityIndex == 1)
		{
			abilityOne = new Ability(abilityName, this);
			return abilityOne;
		}
		else if(abilityIndex == 2)
		{
			abilityDW = new Ability(abilityName, this);
			return abilityDW;
		}
		return null;
	}
	
	public boolean checkAbilities(String abilityName)
	{
		if(ability == null)
		{
			if(abilityZero != null && abilityZero.getName().toLowerCase().startsWith(abilityName.toLowerCase()))
				return true;
			if(abilityOne != null && abilityOne.getName().toLowerCase().startsWith(abilityName.toLowerCase()))
				return true;
			if(abilityDW != null && abilityDW.getName().toLowerCase().startsWith(abilityName.toLowerCase()))
				return true;		
		}
		else if(ability.getName().toLowerCase().startsWith(abilityName.toLowerCase()))
			return true;
		return false;
	}

	/**
	 * Returns the specified ability (0: Ability0, 1: Ability1, 2: AbilityDW). 
	 * Returns NULL if it does not have that ability or <i>i</i> is out of range.
	 * @param i (int): The index to look up.
	 * @return (Ability): The ability at this slot.
	 */
	public Ability getAbility(int i) 
	{
		if(i == 0)
			return abilityZero;
		if(i == 1)
			return abilityOne;
		if(i == 2)
			return abilityDW;
		else return null;
	}

	/**
	 * Sets this Pokemon's importable to a string.
	 * @param string (String): The importable.
	 */
	public void setImportable(String string) 
	{
		importable = string;
	}
	
	public String getImportable()
	{
		if(nickName == null || nickName.isEmpty())
		{
			importable = name;
		}
		else
		{
			importable = nickName+" ("+name+")";
		}
		if(item != null && item.name != null)
			importable = importable + " @ " + item.name;
		importable = importable + "\n";
		if(ability != null)
				importable = importable + "Trait: "+ability.getName()+"\n";
		int count = 0;
		for(int i = 0; i < evs.length; i++)
		{
			if(evs[i] > 0)
			{
				if(count == 0)
					importable = importable +"EVs: ";
				else
					importable = importable+"/ ";
				count++;
				importable = importable + evs[i]+" "+Stat.fromInt(i)+" ";
			}
		}
		if(count > 0)
			importable = importable + "\n";
		if(nature != null)
			importable = importable + nature.toString() + " Nature \n";
		count = 0;
		for(int i = 0; i < 4; i++)
		{
			if(moveset[i] == null)
				continue;
			importable = importable + "- "+ moveset[i].name +"\n";
			count++;
		}
		if(count > 0)
			importable = importable + "\n";
		return importable;
	}

	/**
	 * Sets our item to the specified item.
	 * @param itemName (String): The name of the item.
	 */
	public void setItem(String itemName) 
	{
		item = new Item(itemName);
	}

	/**
	 * Sets our level to the specified level.
	 * @param i (int): What level to set.
	 */
	public void setLevel(int i) 
	{
		level = i;
	}

	/**
	 * Disables all moves but the moves listed.
	 * @param usableMoves (List): The names of all usable moves, as a List of Strings.
	 */
	public void setUsableMoves(List<String> usableMoves) 
	{
		String[] moveArray = new String[4];
		moveArray = usableMoves.toArray(moveArray);
		setUsableMoves(moveArray);
	}
	
	private void setUsableMoves(String[] moveArray)
	{
		for(int i = 0; i < moveset.length; i++)
		{
			moveset[i].disabled = true;
			for(int n = 0; n < moveArray.length; n++)
			{
				if(moveArray[n] == null)
					continue;
				if(moveArray[n].equals(moveset[i].name) || moveArray[n].toLowerCase().contains("hidden") && moveset[i].name.toLowerCase().contains("hidden"))
					moveset[i].disabled = false;
			}
		}
	}

	/**
	 * Calculates the EVs needed to outspeed a given speed.
	 * @param boostedStat
	 */
	public void setMinSpeed(int boostedStat) 
	{
		int[] originalEVs = evs;
		int originalLeft = evsLeft;
		while(boostedStats[Stat.Spe.toInt()] > boostedStat && evsLeft > 3)
		{
			evs[Stat.Spe.toInt()] += 4;
			evsLeft -= 4;
			stats[Stat.Spe.toInt()] = Pokequations.calculateStat(Stat.Spe, this);
			boostedStats[Stat.Spe.toInt()] = Pokequations.statBoost(boosts[Stat.Spe.toInt()],stats[Stat.Spe.toInt()]);
			System.out.println("Finding min speed. Current: "+boostedStats[Stat.Spe.toInt()]+"; must be larger than "+boostedStat);
		}
		if(boostedStats[Stat.Spe.toInt()] > boostedStat && evsLeft <= 0)
		{
			Nature originalNature = nature;
			if(nature == Nature.Hardy)
			{
				nature = Nature.findPositiveNature(Stat.Spe, moveset);
				//The results were skewed, so this EV spread might be inaccurate and we need to try again.
				evs = originalEVs;
				evsLeft = originalLeft;
				stats[Stat.Spe.toInt()] = Pokequations.calculateStat(Stat.Spe, this);
				boostedStats[Stat.Spe.toInt()] = Pokequations.statBoost(boosts[Stat.Spe.toInt()],stats[Stat.Spe.toInt()]);
				setMinSpeed(boostedStat);
			}
			if(item == null)
			{
				int count = 0;
				for(int i = 0; i < 4; i++)
				{
					if(movesUsedSinceDeploy[i] == null)
						continue;
					else
						count++;
				}
				if(count == 1)
				{
					item = new Item("Choice Scarf");
					//The results were skewed, so this EV spread might be inaccurate and we need to try again.
					evs = originalEVs;
					evsLeft = originalLeft;
					nature = originalNature;
					stats[Stat.Spe.toInt()] = Pokequations.calculateStat(Stat.Spe, this);
					boostedStats[Stat.Spe.toInt()] = Pokequations.statBoost(boosts[Stat.Spe.toInt()],stats[Stat.Spe.toInt()]);
					setMinSpeed(boostedStat);
				}
			}
			else
			{
				stats[Stat.Spe.toInt()] = Pokequations.calculateStat(Stat.Spe, this);
				boostedStats[Stat.Spe.toInt()] = Pokequations.statBoost(boosts[Stat.Spe.toInt()],stats[Stat.Spe.toInt()]);
			}
		}
	}

	/**
	 * @param damageAmount
	 * @return
	 */
	public int percentToHP(int damageAmount) 
	{
		damageAmount /= 100;
		return Math.round(hpPercent * damageAmount);
	}

	/**
	 * Gets our base stats.
	 * @return (int[]): Our base stats.
	 */
	public int[] getBaseStats() 
	{
		return base;
	}

	/**
	 * Set all base stats at once.
	 * @param baseStats (int[]): The base stats to set.
	 */
	public void setBaseStats(int[] baseStats) 
	{
		for(int i = 0; i < baseStats.length; i++)
		{
			setBaseStat(i, baseStats[i]);
		}
	}

	/**
	 * Returns TRUE if the Pokemon has this nickname, else FALSE.
	 * @param nick (String): The nickname to check.
	 * @return TRUE if the Pokemon has this nickname, else FALSE.
	 */
	public boolean nicknameIs(String nick) 
	{
		if(nick == null || nick.isEmpty() || nickName == null || nickName.isEmpty())
			return false;
		return nick.toLowerCase().contains(nickName.toLowerCase());
	}

	/**
	 * Sets our nickname to a string.
	 * @param string (String): What to set our nickname to.
	 */
	public void setNickname(String string) 
	{
		nickName = string;
	}
}
