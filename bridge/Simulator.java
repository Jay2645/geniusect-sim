package geniusectsim.bridge;

import geniusectsim.abilities.Ability;
import geniusectsim.actions.Action;
import geniusectsim.actions.Attack;
import geniusectsim.actions.Change;
import geniusectsim.battle.Battle;
import geniusectsim.battle.Damage;
import geniusectsim.battle.Team;
import geniusectsim.moves.Move;
import geniusectsim.pokemon.Pokemon;

/**
 * Handles the battle simulation. 
 * Where ShowdownHandler assumes we are not playing a local game, this checks to see if the game is local or not before continuing.
 * This is also where the Simulator GUI comes from
 * @author TeamForretress
 */
public class Simulator
{
	
	private static boolean isLocal = true;
	private static Action userAction = null;
	private static Action enemyAction = null;
	private static Battle battle = null;
	private static int teamCount = 0;
	
	public static void isLocal(boolean local)
	{
		if(local)
			ShowdownHandler.closeShowdown();
		else
			ShowdownHandler.launchShowdown();
		isLocal = local;
	}
	
	public static Battle onNewBattle()
	{
		battle = new Battle();
		if(isLocal)
		{
			battle.setupTeam(0);
			battle.setupTeam(1);
		}
		else
		{
			battle = ShowdownHandler.onNewBattle(battle);
		}
		return battle;
	}
	
	public static String[] getPlayerNames()
	{
		String[] userName = new String[2];
		if(isLocal)
		{
			userName[0] = "Player 0";
			userName[1] = "Player 1";
		}
		else
		{
			userName = ShowdownHandler.getPlayerNames(userName);
		}
		return userName;
	}
	
	public static Action battleStartAction()
	{
		if(isLocal)
		{
			battle.battleStart();
			return new Change();
		}
		return ShowdownHandler.battleStartAction();
	}
	
	public static void updateLastTurn()
	{
		if(!isLocal)
		{
			ShowdownHandler.updateLastTurn();
		}
	}
	
	public static void print(String text)
	{
		if(isLocal)
			System.out.println("BATTLE CHAT: "+text);
		else
		{
			ShowdownHandler.print(text);
		}
	}
	
	public static Team teamFromImportable(String importable)
	{
		Team team = new Team(teamCount, battle);
		team.importImportable(importable);
		if(teamCount == 0 && !isLocal)
		{
			ShowdownHandler.teamFromImportable(team);
		}
		teamCount++;
		return team;
	}
	
	public static Action newTurn(Action a)
	{
		if(isLocal)
		{
			if(userAction == null)
				userAction = a;
			else
				enemyAction = a;
			simulateTurn(userAction, enemyAction, battle);
			if(userAction != null && enemyAction != null)
			{
				userAction = null;
				enemyAction= null;
			}
			return new Attack();
		}
		else
		{
			Pokemon playerActive = battle.getTeam(0, false).getActive();
			Pokemon enemyActive = battle.getTeam(1, false).getActive();
			if(playerActive != null)
				playerActive.setEnemy(enemyActive);
			if(enemyActive != null)
				enemyActive.setEnemy(playerActive);
			battle.setTurnCount(ShowdownHandler.getCurrentTurnNumber());
			userAction = a;
			return ShowdownHandler.doAction(userAction);
		}
	}
	
	/**
	 * Simulates a turn in the battle.
	 * @param nextTurnPlayer (Action): The action the player is taking.
	 * @param nextTurnEnemy (Action): The action the enemy is taking.
	 */
	public static void simulateTurn(Action nextTurnPlayer, Action nextTurnEnemy, Battle b)
	{
		if(nextTurnPlayer == null || nextTurnEnemy == null)
		{
			System.out.println("Player 1 has completed their move.");
			return;
		}
		System.out.println("Player 2 has completed their move.");
		b.setTurnCount(b.getTurnCount() + 1);
		boolean playerMoved = false;
		boolean enemyMoved = false;
		Team player = b.getTeam(0, false);
		Team opponent = b.getTeam(1, false);
		Pokemon playerPoke = player.getActive();
		Pokemon enemyPoke = opponent.getActive();
		if(playerPoke == null)
		{
			if(nextTurnPlayer instanceof Change)
				playerPoke = ((Change)nextTurnPlayer).switchTo;
			else
			{
				System.err.println("The player Pokemon is null, but the player did not provide a switch.");
				return;
			}
		}
		if(enemyPoke == null)
		{
			if(nextTurnEnemy instanceof Change && ((Change)nextTurnEnemy).switchTo != null)
				enemyPoke = ((Change)nextTurnEnemy).switchTo;
			else
			{
				System.err.println("The enemy Pokemon is null, but the enemy did not provide a switch.");
				return;
			}
		}
		playerPoke.setEnemy(enemyPoke);
		enemyPoke.setEnemy(playerPoke);
		System.err.println(playerPoke+", "+enemyPoke);
		int playerHealth = playerPoke.getHealth();
		int enemyHealth = enemyPoke.getHealth();
		if(nextTurnPlayer instanceof Change) //Always change first.
		{
			playerPoke = ((Change)nextTurnPlayer).switchTo;
			playerHealth = playerPoke.getHealth();
			playerHealth -= doAction(nextTurnPlayer);
			playerMoved = true;
			if(nextTurnEnemy instanceof Attack)
			{
				Attack a = (Attack)nextTurnEnemy;
				a.defenderSwap(playerPoke);
			}
		}
		if(nextTurnEnemy instanceof Change)
		{
			enemyPoke = ((Change)nextTurnEnemy).switchTo;
			enemyHealth = enemyPoke.getHealth();
			enemyHealth -= doAction(nextTurnEnemy);
			enemyMoved = true;
			if(nextTurnPlayer instanceof Attack)
			{
				Attack a = (Attack)nextTurnPlayer;
				a.defenderSwap(enemyPoke);
			}
		}
		if(	player.getActive().isFasterThan(opponent.getActive()) && //Check who is faster
				nextTurnEnemy instanceof Attack && nextTurnPlayer instanceof Attack && 
				((Attack)nextTurnPlayer).move.priority >= ((Attack)nextTurnEnemy).move.priority) //Check if we have priority.
		{
			if(!playerMoved)
			{
				enemyHealth -= doAction(nextTurnPlayer);
				playerMoved = true;
			}
			if(!enemyMoved && enemyHealth > 0)
			{
				playerHealth -= doAction(nextTurnEnemy);
			}
		}
		else
		{
			if(!enemyMoved)
			{
				playerHealth -= doAction(nextTurnEnemy);
			}
			if(!playerMoved && playerHealth > 0)
			{
				enemyHealth -= doAction(nextTurnPlayer);
			}
		}
		if(playerHealth <= 0)
			playerPoke.onDie();
		if(enemyHealth <= 0)
			enemyPoke.onDie();
	}
	
	private static int doAction(Action action)
	{
		if(action instanceof Attack)
		{
			if(isLocal)
			{
				Attack attack = (Attack)action;
				Pokemon attacker = attack.attacker;
				Pokemon defender = attack.defender;
				if(!attacker.isAlive()) //We can only attack if we're alive.
					return 0;
				Damage damageDone = new Damage(attack.move, attacker, defender);
				int damage = damageDone.applyDamage();
				if(defender.isAlive())
					System.err.println("Damage done: "+damage+"%");
				return damage;
			}
			else
			{
				ShowdownHandler.doAction(action);
			}
		}
		else if(action instanceof Change)
		{
			if(isLocal)
			{
				Change change = (Change)action;
				System.out.println("Changing to "+change.switchTo.getName());
				Team changeTo = change.switchTo.getTeam();
				changeTo.setActive(change.switchTo);
			}
			else
			{
				ShowdownHandler.doAction(action);
			}
		}
		return 0;
	}

	/**
	 * Leaves the current battle.
	 */
	public static void leaveBattle() 
	{
		if(battle.isPlaying())
			battle.gameOver(false);
		if(isLocal)
			ShowdownHandler.leaveBattle();
	}

	/**
	 * Gets the moves for a certain Pokemon.
	 * @param pokemon (Pokemon): The Pokemon to get the moves of.
	 */
	public static void getMoves(Pokemon pokemon) 
	{
		if(isLocal)
			return;
		ShowdownHandler.getMoves(pokemon);
	}

	/**
	 * Finds out if we can switch or not.
	 * @return TRUE if we can switch, else FALSE.
	 */
	public static boolean canSwitch() 
	{
		if(isLocal)
		{
			//TODO: Figure out if we can switch or not.
			return true;
		}
		else
			return ShowdownHandler.canSwitch();
	}

	/**
	 * Gets the amount of PP remaining for a move.
	 * @param move (Move): The move to get the amount of PP remaining.
	 * @param ability (Ability): The enemy Ability. NULL if not known.
	 * @return (int): The amount of PP remaining.
	 */
	public static int getMoveRemainingPP(Move move, Ability ability) 
	{
		if(isLocal || move.user.getTeam().getTeamID() == 1)
		{
			int pp = move.pp;
			if(ability == null || !ability.getName().toLowerCase().startsWith("pressure"))
				pp--;
			else
				pp -=2;
			return pp;
		}
		else
		{
			return ShowdownHandler.getMoveRemainingPP(move, ability);
		}
	}

	/**
	 * Switches to the Pokemon specified by the specified Change.
	 * @param switchTo (Change): The Change to deploy.
	 */
	public static void switchTo(Change switchAction) 
	{
		if(switchAction.switchTo == null)
		{
			System.out.println("Could not switch to a Pokemon because it was not defined in the Change class.");
			Throwable t = new Throwable();
			t.printStackTrace();
			return;
		}
		System.err.println("Is local? "+isLocal);
		if(isLocal)
		{
			Pokemon switchTo = switchAction.switchTo;
			System.err.println("This is a local game.");
			switchTo.getTeam().changePokemon(switchTo);
		}
		else
		{
			ShowdownHandler.switchTo(switchAction);
		}
	}

	/**
	 * Populates our team with the data from Showdown.
	 * @param team (Team): The team to look up.
	 */
	public static void teamFromShowdown(Team team) 
	{
		if(!isLocal)
			ShowdownHandler.teamFromShowdown(team);
	}
}
