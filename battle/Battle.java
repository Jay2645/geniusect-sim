
package geniusectsim.battle;

import geniusectsim.actions.Action;
import geniusectsim.bridge.Simulator;
import geniusectsim.pokemon.Pokemon;
import geniusectsim.pokemon.Tier;

/**
 * A class representing a battle.
 * Battles consist of two teams of six Pokemon each. 
 * @author TeamForretress
 * @see geniusectsim.simulator.pokemon.Pokemon
 * @see geniusectsim.simulator.battle.Team
 */
public class Battle {
	/**
	 * Creates a new Battle class.
	 * <b>THIS WILL RESET THE ACTIVE BATTLE IN SHOWDOWN.</b>
	 * All data on teams, Pokemon, etc. will be wiped.
	 * To preserve that data (i.e. this battle is being simulated), use new Battle(Battle) instead.
	 * @see #Battle(Battle)
	 */
	public Battle() {}
	
	public Battle(Battle b)
	{
		clone(b);
	}
	/**
	 * The team the USER is going to use in the battle.
	 * The first line is the name of the team (so the ShowdownHelper can find which team to select).
	 * The rest of the line is the export data from Pokemon Showdown's teambuilder.
	 */
	private String importableUs =	"Team Name: The Jungle" +
									"\nHard To Please (Ninetales) @ Leftovers" +
									"\nTrait: Drought" +
									"\nEVs: 252 HP / 252 SAtk / 4 SDef" +
									"\nModest Nature" +
									"\n- Sunny Day" +
									"\n- SolarBeam" +
									"\n- Overheat" +
									"\n- Power Swap" +
									"\n" +
									"\nAnimal (Donphan) @ Leftovers" +
									"\nTrait: Sturdy" +
									"\nEVs: 252 SDef / 28 HP / 228 Def" +
									"\nCareful Nature" +
									"\n- Rapid Spin" +
									"\n- Ice Shard" +
									"\n- Stealth Rock" +
									"\n- Earthquake" +
									"\n"+
									"\nThe Jungle (Tangrowth) @ Leftovers" +
									"\nTrait: Chlorophyll" +
									"\nEVs: 252 HP / 252 Spd / 4 Atk" +
									"\nNaive Nature" +
									"\n- Growth" +
									"\n- Power Whip" +
									"\n- Hidden Power [Ice]" +
									"\n- Earthquake" +
									"\n" +
									"\nDisease (Dugtrio) @ Focus Sash" +
									"\nTrait: Arena Trap" +
									"\nEVs: 252 Spd / 4 Def / 252 Atk" +
									"\nJolly Nature" +
									"\n- Earthquake" +
									"\n- Sucker Punch" +
									"\n- Stone Edge" +
									"\n- Reversal" +
									"\n" +
									"\nSerpentine (Heatran) @ Choice Scarf" +
									"\nTrait: Flash Fire" +
									"\nEVs: 252 Spd / 252 SAtk / 4 HP" +
									"\nModest Nature" +
									"\n- Overheat" +
									"\n- SolarBeam" +
									"\n- Earth Power" +
									"\n- Hidden Power [Ice]" +
									"\n" +
									"\nWorse Everyday (Dragonite) @ Lum Berry" +
									"\nTrait: Multiscale" +
									"\nEVs: 252 Spd / 4 HP / 252 Atk" +
									"\nAdamant Nature" +
									"\n- Dragon Dance" +
									"\n- Fire Punch" +
									"\n- ExtremeSpeed" +
									"\n- Outrage";
	
			
	private String importableEnemy =	"Team Name: Underuseless" +
										"\nCrobat @ Black Sludge" +
										"\nTrait: Inner Focus" +
										"\nEVs: 176 HP / 192 Atk / 136 Spd / 4 SDef" +
										"\nJolly Nature" +
										"\n- Roost" +
										"\n- Brave Bird" +
										"\n- Taunt" +
										"\n- U-turn" +
										"\n" +
										"\nMachamp @ Lum Berry" +
										"\nTrait: No Guard" +
										"\nEVs: 248 Atk / 8 Spd / 240 HP / 12 SDef" +
										"\nAdamant Nature" +
										"\n- DynamicPunch" +
										"\n- Ice Punch" +
										"\n- Payback" +
										"\n- Bullet Punch" +
										"\n" +
										"\nQuagsire @ Leftovers" +
										"\nTrait: Unaware" +
										"\nEVs: 252 SDef / 252 HP / 4 Def" +
										"\nSassy Nature" +
										"\nIVs: 0 Spd" +
										"\n- Curse" +
										"\n- Earthquake" +
										"\n- Waterfall" +
										"\n- Recover" +
										"\n" +
										"\nArcanine @ Leftovers" +
										"\nTrait: Intimidate" +
										"\nEVs: 248 HP / 252 Def / 8 SDef" +
										"\nBold Nature" +
										"\n- Flamethrower" +
										"\n- Hidden Power [Grass]" +
										"\n- Morning Sun" +
										"\n- Will-O-Wisp" +
										"\n" +
										"\nSigilyph @ Flame Orb" +
										"\nTrait: Magic Guard" +
										"\nEVs: 252 HP / 136 Def / 120 Spd" +
										"\nTimid Nature" +
										"\n- Cosmic Power" +
										"\n- Psycho Shift" +
										"\n- Roost" +
										"\n- Stored Power" +
										"\n" +
										"\nSuicune @ Leftovers" +
										"\nTrait: Pressure" +
										"\nEVs: 252 Def / 252 HP / 4 SDef" +
										"\nBold Nature" +
										"\n- Scald" +
										"\n- Calm Mind" +
										"\n- Rest" +
										"\n- Sleep Talk";

	private Team players[] = new Team[2];
	private int turnsToSimulate = 150; //How many turns we simulate, if Showdown is not running?
	private int turnCount = 0; //The current turn.
	private boolean playing = false; // TRUE if we have found a battle.
	private boolean firstTurn = true; // TRUE if it is the first turn.
	//private Action lastTurnUs;
	private Action lastTurnEnemy;
	//private ShowdownHelper showdown = null;
	private Weather weather = Weather.None;
	
	public static String criticalErrors = "Errors:\n";
	
	public String[] setupTeam()
	{
		players[1] = new Team(1, this);
		return setupTeam(0);
	}
	
	/**
	 * Sets up the team at the specified teamID.
	 * This includes importing it from an importable.
	 * If the teamName for the importable is invalid, it starts a Random Battle.
	 * Otherwise it returns a String array.
	 * @param teamID (int): The ID of the team to create.
	 * @return (String[]) Index 0 is the tier to play (i.e. "OU," "Random Battle," etc.), Index 1 is the name of the team ("" if Random Battle).
	 */
	public String[] setupTeam(int teamID)
	{
		String[] teamData = new String[2];
		if(teamID == 1)
			players[teamID] = Simulator.teamFromImportable(importableEnemy);
		else
			players[teamID] = Simulator.teamFromImportable(importableUs);
		teamData[1] = players[teamID].getTeamName();
		if(teamData[1] == null || teamData[1].equals(""))
			teamData[0] = "Random Battle";
		else
		{
			Pokemon[] team = players[teamID].getPokemon();
			int max = 0;
			for(int i = 0; i < team.length; i++)
			{
				Tier t = team[i].getTier();
				t = t.usableTier();
				int rank = t.tierToRank();
				max = Math.max(rank, max);
			}
			teamData[0] = Tier.tierFromInt(max).toString();
		}
		return setupTeam(teamData[0], teamID);
	}
	
	public String[] setupTeam(String tier, int teamID)
	{
		String[] teamData = new String[2];
		if(players[teamID] == null)
			players[teamID] = new Team(teamID,this);
		teamData[0] = tier;
		teamData[1] = players[teamID].getTeamName();
		if( tier.toLowerCase().startsWith("random battle") || teamData[1] == null || teamData[1].equals(""))
		{
			teamData[0] = "Random Battle";
			teamData[1] = "";
		}
		/*if(teamData[0].equals("OU"))
		{
			//zarel why must you be so troll
			teamData[0] = "OU (current)";
		}*/
		return teamData;
	}
	
	public void battleStart()
	{
		//Called when the battle begins.
		turnsToSimulate *= 2;
		playing = true;
		//Populate each team.
		String[] userName = Simulator.getPlayerNames();
		for(int i = 0; i < 2; i++)
		{
			players[i].setUserName(userName[i]);
			Simulator.teamFromShowdown(players[i]);
		}
	}
	
	public void gameOver(boolean won)
	{
		if(players[1].getAliveCount() > 1)
			players[0].getActive().ragequits++;
		System.err.println("Game over.");
		System.err.println(Battle.criticalErrors);
		turnsToSimulate = 0;
		playing = false;
		//AIHandler.gameOver(won);
	}
	
	public Weather getWeather()
	{
		return weather;
	}
	
	public void setWeather(Weather weatherType)
	{
		System.out.println("The weather is now "+weatherType+"!");
		weather = weatherType;
	}
	
	/**
	 * Returns the team at index ID (0 == user, 1 == enemy).
	 * @param id - int: The TeamID.
	 * @return Team - The Team at index id.
	 */
	public Team getTeam(int id, boolean clone)
	{
		if(clone)
			return new Team(players[id]);
		else return players[id];
	}
	
	/**
	 * Returns the current turn count.
	 * @return int - the current turn count.
	 */
	public int getTurnCount()
	{
		return turnCount;
	}
	
	/**
	 * Finds out if it is the first turn.
	 * @return TRUE if it is the first turn, else FALSE.
	 */
	public boolean isFirstTurn()
	{
		return firstTurn;
	}
	
	/**
	 * Sets if this is the battle's first turn.
	 * @param first (boolean) - TRUE if this is the first turn, else FALSE.
	 */
	public void isFirstTurn(boolean first)
	{
		firstTurn = first;
	}
	
	public void isPlaying(boolean play)
	{
		playing = play;
	}
	
	/**@return TRUE if the battle is active, else FALSE.*/
	public boolean isPlaying()
	{
		return playing;
	}
	
	public void clone(Battle clone)
	{
		importableUs = clone.importableUs;
		importableEnemy = clone.importableEnemy;
		firstTurn = clone.firstTurn;
		players = clone.players;
		//players[0] = new Team(clone.players[0]);
		//players[1] = new Team(clone.players[1]);
		playing = clone.playing;
		turnCount = clone.turnCount;
		turnsToSimulate = clone.turnsToSimulate;
		weather = clone.weather;
	}

	/**
	 * Returns the enemy's last turn.
	 * @return Action - What the enemy team did last.
	 */
	public Action getLastTurnEnemy() 
	{
		return lastTurnEnemy;
	}

	
	/**
	 * Updates the active Pokemon on the specified team.
	 * @param teamID (int): The ID of the Team to update.
	 * @param active (Pokemon): The Pokemon to mark as active.
	 */
	public void updateTeamActive(int teamID, Pokemon active)
	{
		updateTeamActive(players[teamID], active);
	}
	/**
	 * Updates the active Pokemon on the specified team.
	 * @param team (Team): The team to update.
	 * @param active (Pokemon): The Pokemon to mark as active.
	 */
	private void updateTeamActive(Team team, Pokemon active) 
	{
		team.updateEnemy(active);
	}

	/**
	 * Changes this battle's turn count.
	 * @param i (int): The number to set it to.
	 */
	public void setTurnCount(int i) 
	{
		turnCount = i;
	}

	/**
	 * @param usImportable
	 */
	public void setUsImportable(String usImportable) 
	{
		importableUs = usImportable;
	}

	/**
	 * Returns a list of all errors.
	 * @return (String): A list of all bad errors.
	 */
	public String getErrors() 
	{
		return criticalErrors;
	}

	/**
	 * Rebuilds each team all over again.
	 */
	/*public void rebuildTeams() 
	{
		if(showdown != null)
		{
			players[0] = new Team(0,this);
			players[1] = new Team(1,this);
			populateTeams();
			Team enemy = players[1];
			String enemyUsername = enemy.getUsername();
			String activeEnemy = showdown.getCurrentPokemon(enemyUsername,true);
			Pokemon enemyPoke = enemy.getPokemon(activeEnemy);
			if(enemyPoke != null)
				enemy.setActive(enemyPoke);
			Pokemon[] enemyTeam = enemy.getPokemon();
			for(int i = 0; i < enemyTeam.length; i++)
			{
				if(enemyTeam[i] == null)
					continue;
				boolean alive = enemyTeam[i].isAlive();
				String teamName = enemyTeam[i].getName();
				boolean actualAlive = showdown.isFainted(teamName, enemyUsername);
				if(alive && !actualAlive)
				{
					enemyTeam[i].onDie();
				}
				else if(!alive && actualAlive)
				{
					enemyTeam[i].setHP(showdown.getHP(teamName, enemyUsername), showdown.getMaxHP(teamName, enemyUsername));
				}
			}
			players[1] = enemy;
			Team us = players[0];
			String usUsername = us.getUsername();
			String activeUs = showdown.getCurrentPokemon(usUsername,true);
			Pokemon poke = us.getPokemon(activeUs);
			if(poke != null)
				us.setActive(poke);
			Pokemon[] usTeam = us.getPokemon();
			for(int i = 0; i < usTeam.length; i++)
			{
				if(usTeam[i] == null)
					continue;
				boolean alive = usTeam[i].isAlive();
				String teamName = usTeam[i].getName();
				boolean actualAlive = showdown.isFainted(teamName, usUsername);
				if(alive && !actualAlive)
				{
					usTeam[i].onDie();
				}
				else if(!alive && actualAlive)
				{
					usTeam[i].setHP(showdown.getHP(teamName, usUsername), showdown.getMaxHP(teamName, usUsername));
				}
				usTeam[i].resetMoves(showdown.getMoves(usTeam[i].getName()));
			}
			players[0] = us;
		}
	}*/
}
