/*
 * A class representing a team.
 * @author TeamForretress
 */

package geniusectsim.battle;

import geniusectsim.moves.Move;
import geniusectsim.pokemon.Pokemon;

public class Team {
	public Team(Team clone)
	{
		clone(clone);
	}
	
	public Team(int id, Battle b)
	{
		teamID = id;
		if(id == 0)
			enemyID = 1;
		else
			enemyID = 0;
		battle = b;
	}
	
	public void importImportable(String input)
	{
		String imports = input;
		if(input.startsWith("Team Name: "))
		{
			int start = input.indexOf("Team Name: ") + 11;
			int end = input.indexOf("\n");
			String t = input.substring(start, end);
			teamName = t;
			//System.err.println(teamName);
			imports = input.substring(end + 1);
		}
		String[] importable = imports.split("\n\n", 6);
		for(int i = 0; i < importable.length; i++)
		{
			Pokemon spread = Pokemon.loadFromText(importable[i], this,i);
			spread.setImportable(importable[i]);
			//TODO: Lookup GA data using specified Spread.
			addPokemon(spread);
		}
	}
	
	public String exportImportable()
	{
		String importable = "";
		for(int i = 0; i < team.length; i++)
		{
			if(team[i] == null)
				continue;
			importable = importable+team[i].getImportable()+"\n\n";
		}
		return importable;
	}
	
	public static Team getEnemyTeam(int id)
	{	//Gets the enemy of team "id".
		if(id == 0)
			return battle.getTeam(1, false);
		else return battle.getTeam(0, false);
	}
	
	private Pokemon[] team = new Pokemon[6];
	private Pokemon active = null;
	private int teamID = -1;
	private int enemyID = -1;
	private String teamName;
	private String userName = "";
	private boolean hasInitialized = false;
	private boolean reflect = false;
	private boolean lightScreen = false;
	private static Battle battle;
	private EntryHazard stealthRocks = EntryHazard.StealthRock;
	private EntryHazard spikes = EntryHazard.Spikes;
	private EntryHazard toxicSpikes = EntryHazard.ToxicSpikes;
	
	public Pokemon addPokemon(String s)
	{
		Pokemon found = getPokemon(s);
		if(found == null)
			return addPokemon(new Pokemon(s,"",this));
		else
			return found;
	}
	public Pokemon addPokemon(Pokemon p)
	{
		//Adds Pokemon to a team. If a Pokemon has already been added, returns that Pokemon.
		for(int i = 0; i < team.length; i++)
		{
			if(team[i] == null)
			{
				team[i] = p;
				break;
			}
			else if(team[i].nameIs(p.getName()))
			{
				team[i].setTeam(this);
				p = team[i];
				break;
			}
		}
		hasInitialized = true;
		return p;
	}
	
	
	public void updateEnemyTeam()
	{
		 battle.updateTeamActive(enemyID,active);
	}
	
	public void updateEnemy(Pokemon p)
	{
		if(active != null)
			active.changeEnemy(p);
	}
	
	/**
	 * Changes our Pokemon to the specified Pokemon.
	 * If a Pokemon of that species is already out, returns the active Pokemon.
	 * @param p (Pokemon): The Pokemon to change to.
	 * @return (Pokemon): The new active Pokemon.
	 */
	public Pokemon changePokemon(Pokemon p)
	{
		if(active != null)
		{
			if(p.nameIs(active.getName()))
				return active;
			active.onWithdraw();
		}
		active = p;
		active.onSendOut();
		updateEnemyTeam();
		return active;
	}
	
	public void setUserName(String s)
	{
		userName = s;
	}
	
	public Pokemon getPokemon(String name)
	{
		Pokemon p = null;
		for(int i = 0; i < team.length; i++)
		{
			if(team[i] == null)
				continue;
			if(team[i].nameIs(name) || team[i].nicknameIs(name))
			{
				p = team[i];
				break;
			}
		}
		return p;
	}
	
	/**
	 * Gets the Pokemon at index i, where i is an int between 0 and 5 (inclusive).
	 * @param i (int): The index to fetch the Pokemon from.
	 * @return Pokemon: The Pokemon at the index. NULL if index is out of bounds or Pokemon is not found.
	 */
	public Pokemon getPokemon(int i)
	{
		if(i > -1 && i < 7)
			return team[i];
		else return null;
	}
	
	/**
	 * Returns this team's username.
	 * @return This team's username.
	 */
	public String getUsername()
	{
		return userName;
	}
	
	/**
	 * Sets the battle to the specified battle.
	 * @param b (Battle): The battle to set our battle to.
	 */
	public void setBattle(Battle b)
	{
		battle = b;
	}
	
	/**
	 * Gets our current battle.
	 * @return (Battle): Our current battle.
	 */
	public Battle getBattle()
	{
		return battle;
	}
	
	/**
	 * Gets our Pokemon team.
	 * @return Pokemon[] - A CLONE of our Pokemon team.
	 */
	public Pokemon[] getPokemon()
	{
		return team.clone();
	}
	
	public int getPokemonID(Pokemon p)
	{
		for(int i = 0; i < team.length; i++)
		{
			if(team[i] == null)
				break;
			if(team[i] == p)
				return i;
		}
		return -1;
	}
	
	public int getTeamID()
	{
		return teamID;
	}
	
	public Pokemon getActive()
	{
		return active;
	}
	
	/**
	 * Sets the Pokemon as active.
	 * @param p (Pokemon): The Pokemon to mark as active.
	 */
	public void setActive(Pokemon p)
	{
		if(p == null)
			active = null;
		else if(active != null)
			active.onWithdraw();
		active = p;
	}
	/**
	 * Sets the Pokemon slot at the given ID to the given Pokemon.
	 * @param id (int): The Pokemon slot index (can be 0-5, inclusive)
	 * @param pokemon (Pokemon): The Pokemon to set the teamslot to.
	 */
	public void setPokemon(int id, Pokemon pokemon) 
	{
		if(id > -1 && id < 7)
			return;
		team[id] = pokemon;
	}
	/**
	 * Looks up the team of the specified Pokemon.
	 * @param pokemon (Pokemon): The Pokemon to look up.
	 * @return (Team): That Pokemon's team.
	 */
	public static Team lookupPokemon(Pokemon pokemon) 
	{
		Team team = null;
		for(int i = 0; i < 2; i++)
		{
			if(battle.getTeam(i, false).hasPokemon(pokemon))
			{
				System.out.println("Looking up team.");
				team = battle.getTeam(i, false);
				break;
			}
		}
		return team;
	}
	/**
	 * Looks up if this team has this Pokemon.
	 * @param pokemon (Pokemon): The Pokemon to look up.
	 * @return TRUE if it does have this Pokemon, else FALSE.
	 */
	private boolean hasPokemon(Pokemon pokemon) 
	{
		for(int i = 0; i < team.length; i++)
		{
			if(team[i] == null)
				continue;
			System.err.println(team[i].getName());
			if(team[i] == pokemon)
				return true;
			if(team[i].getName().equals(pokemon.getName())) //Sometimes for whatever reason it says we have a Pokemon when we don't.
			{
				boolean hasMoves = false;
				Move[] teamMoveset = team[i].getMoveset();
				Move[] pokeMoveset = pokemon.getMoveset();
				//Make sure this isn't a false positive.
				if((teamMoveset[0] == null && pokeMoveset[0] != null || teamMoveset[0] != null && pokeMoveset[0] == null) ||
					(teamMoveset[1] == null && pokeMoveset[1] != null || teamMoveset[1] != null && pokeMoveset[1] == null) ||
					(teamMoveset[2] == null && pokeMoveset[2] != null || teamMoveset[2] != null && pokeMoveset[2] == null) ||
					(teamMoveset[3] == null && pokeMoveset[3] != null || teamMoveset[3] != null && pokeMoveset[3] == null))
					hasMoves = false;
				else if((teamMoveset[0] == null && pokeMoveset[0] == null || teamMoveset[0].name.equals(pokeMoveset[0].name)) && 
						(teamMoveset[1] == null && pokeMoveset[1] == null || teamMoveset[1].name.equals(pokeMoveset[1].name)) &&
						(teamMoveset[2] == null && pokeMoveset[2] == null || teamMoveset[1].name.equals(pokeMoveset[2].name)) &&
						(teamMoveset[3] == null && pokeMoveset[3] == null || teamMoveset[1].name.equals(pokeMoveset[3].name)))
					hasMoves = true;
				return hasMoves;
			}
		}
		return false;
	}
	
	private void clone(Team clone)
	{
		team = clone.team.clone();
		if(clone.active != null)
			active = team[clone.active.getID()];
		else
			active = null;
		teamID = clone.teamID;
		enemyID = clone.enemyID;
		teamName = clone.teamName;
		userName = clone.userName;
		hasInitialized = clone.hasInitialized;
		reflect = clone.reflect;
		lightScreen = clone.lightScreen;
	}

	/**
	 * Returns our Pokemon team as an array.
	 * @return (Pokemon[]): Our Pokemon team.
	 */
	public Pokemon[] getPokemonTeam() 
	{
		return team;
	}
	
	/**
	 * Adds entry hazards to our side.
	 * @param hazard (EntryHazard): The Entry Hazard to add.
	 */
	public void addHazard(EntryHazard hazard) 
	{
		if(hazard == stealthRocks && stealthRocks.getLevel() <= 1)
			stealthRocks.addLevel();
		else if(hazard == spikes && spikes.getLevel() <= 3)
			spikes.addLevel();
		else if(hazard == toxicSpikes && toxicSpikes.getLevel() <= 2)
			toxicSpikes.addLevel();
	}
	
	/**
	 * Checks if we have the maximum amount of an entry hazard on our side.
	 * @param hazard (EntryHazard): The hazard to check.
	 * @return TRUE if we've maxed it out, else FALSE.
	 */
	public boolean hasMaxHazard(EntryHazard hazard)
	{
		if(hazard == EntryHazard.StealthRock)
		{
			if(stealthRocks.getLevel() == 1)
				return true;
			else return false;
		}
		else if(hazard == EntryHazard.Spikes)
		{
			if(spikes.getLevel() == 3)
				return true;
			else return false;
		}
		else if(hazard == EntryHazard.ToxicSpikes)
		{
			if(toxicSpikes.getLevel() == 2)
				return true;
			else return false;
		}
		return true;
	}

	/**
	 * Returns the number of Pokemon still alive.
	 * @return (int): The number of still-living Pokemon.
	 */
	public int getAliveCount() 
	{
		int aliveCount = 0;
		for(int i = 0; i < team.length; i++)
		{
			if(team[i] == null || team[i].isAlive())
				aliveCount++;
		}
		return aliveCount;
	}

	/**
	 * Returns the number of entry hazards of a certain type.
	 * @param hazard (EntryHazard): The hazard to check.
	 * @return (int): The number of EntryHazards of type <i>hazard</i>.
	 */
	public int getHazardCount(EntryHazard hazard) 
	{
		if(hazard == EntryHazard.StealthRock)
			return stealthRocks.getLevel();
		else if(hazard == EntryHazard.Spikes)
			return spikes.getLevel();
		else if(hazard == EntryHazard.ToxicSpikes)
			return toxicSpikes.getLevel();
		else return 0;
	}

	/**
	 * Gets this team's name.
	 * @return (String): The name of this team.
	 */
	public String getTeamName() 
	{
		return teamName;
	}

	/**
	 * @return
	 */
	public boolean hasReflect() 
	{
		return reflect;
	}

	/**
	 * @return
	 */
	public boolean hasLightScreen() 
	{
		return lightScreen;
	}
}
