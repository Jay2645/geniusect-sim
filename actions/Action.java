/*
 * A generic action class which defines what we will do in a turn.
 * @author TeamForretress
 */

package geniusectsim.actions;

import geniusectsim.battle.Battle;
import geniusectsim.bridge.Simulator;

public class Action 
{
	public String name;
	protected String sayOnSend = "";
	protected boolean crit = false;
	protected boolean sent = false;
	public int score = 0; //The minimax score for this action.
	public int attempt = 0; //How many times we've attempted to try this.
	protected Battle battle = null;
	
	public static int changeCount = 0; //How many times in a row have we changed? (Sanity check.)

	
	/*public int sendToShowdown()
	{
		if(this instanceof Change)
		{
			Change c = (Change)this;
			c.deploy();
			return Change.calculateSwitchDamagePercent(c.switchTo);
		}
		else
		{
			Attack a = (Attack) this;
			return a.deploy();
		}
	}*/
	
	public void say(String text)
	{
		//Says some text at the end of a turn.
		Simulator.print(text);
	}
	
	/*public static void main(String[] args) {
		String log = "Turn 14" +
				"\nThe foe's Cloyster used Shell Smash!" +
				"\nThe foe's Cloyster's Attack sharply rose!" +
				"\nThe foe's Cloyster's Special Attack sharply rose!" +
				"\nThe foe's Cloyster's Speed sharply rose!" +
				"\nThe foe's Cloyster's Defense fell!" +
				"\nThe foe's Cloyster's Special Defense fell!" +
				"\nThe foe's Cloyster restored its status using White Herb!" +
				"\nLatios used Draco Meteor!" +
				"\nA critical hit! The foe's Cloyster lost 75% of its health!" +
				"\nLatios's Special Attack harshly fell!" +
				"\nThe foe's Cloyster fainted!";
		
		findMove(log);
	}*/
	
	
	
	public static void onException(Action failure, Exception e, Battle battle)
	{
		//Called if everything breaks.
		if(failure.attempt == 0)
		{
			//AIHandler.print("Here's what I tried to do:");
			//AIHandler.lastTurnLogic();
			//AIHandler.print(Battle.criticalErrors);
			Battle.criticalErrors = Battle.criticalErrors + "\n" + e;
			e.printStackTrace();
		}
		else if(failure.attempt > 4)
		{
			Simulator.print("Failed!");
			Simulator.print("Sorry! Geniusect is still in early development and is very buggy.");
			Simulator.print("If I don't leave, please hit the 'Kick inactive player button.'");
			Simulator.leaveBattle();
			battle.isPlaying(false);
			return;
		}
		Simulator.print("Attempting to rectify: attempt number "+failure.attempt+" / 4");
		//Action a;
		//battle.rebuildTeams();
		/*if(battle.getTeam(0, false).getActive().isAlive())
		{
			a = GenericAI.bestMove(battle);
		}
		else
		{
			Pokemon p = Change.bestCounter(battle.getTeam(0).getPokemonTeam(),battle.getTeam(1).getActive());
			a = new Change(p, battle);
		}*/
		//a.attempt = failure.attempt + 1;
		//a.sendToShowdown(battle);
		//battle.newTurn();
	}

	/**
	 * Sends whatever message this Action is supposed to send to the chat.
	 */
	public void sendMessage() 
	{
		if(sayOnSend.isEmpty())
			return;
		say(sayOnSend);
	}
}
