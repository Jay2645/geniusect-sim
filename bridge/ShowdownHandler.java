package geniusectsim.bridge;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import geniusectsim.abilities.Ability;
import geniusectsim.actions.Action;
import geniusectsim.actions.Attack;
import geniusectsim.actions.Change;
import geniusectsim.battle.Battle;
import geniusectsim.battle.Damage;
import geniusectsim.battle.Team;
import geniusectsim.moves.Move;
import geniusectsim.pokemon.Pokemon;
import geniusectsim.pokemon.Stat;
import geniusectsim.pokemon.Status;

import com.seleniumhelper.ShowdownHelper;
import com.seleniumhelper.ShowdownHelper.TurnEndStatus;
/**
 * All methods which interface with Showdown directly go through here.
 * @author TeamForretress
 */
public class ShowdownHandler 
{
	/**
	 * Sets up the Showdown hookup.
	 * If <code>local == FALSE</code>, opens a Firefox window and navigates to Showdown.
	 * @param local (boolean): TRUE if this is a local game (internal simulation; do not connect to Showdown).
	 * If you want to load Showdown and run the AI against other players rather than itself, set to FALSE.
	 */
	private static WebDriver driver = null;
	private static ShowdownHelper helper;
	private static TurnEndStatus whatDo = TurnEndStatus.UNKNOWN;
	private static Battle battle = null;
	
	protected static void launchShowdown()
	{
		if(driver != null)
			return;
		driver = new FirefoxDriver();
		// wait up to 1 second for elements to load
	    //driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		helper = new ShowdownHelper(driver);
		helper.open();
		driver.manage().window().maximize();
		try
		{
			helper.login("Geniusect2645","");
		}
		catch(Exception e)
		{
			System.err.println("Could not log in! Exception data: \n"+e);
			closeShowdown();
		}
	}
	
	protected static void closeShowdown()
	{
		if(driver == null)
			return;
		driver.close();
		helper = null;
		driver = null;
	}
	
	/**
	 * Starts a new battle.
	 */
	protected static Battle onNewBattle(Battle b)
	{
		battle = b;
		String[] battleStrings = battle.setupTeam();
		try
		{
			helper.findBattle(battleStrings[0], battleStrings[1]);
		}
		catch(Exception e)
		{
			try
			{
				teamFromImportable(battle.getTeam(0, false));
				helper.findBattle(battleStrings[0], battleStrings[1]);
			}
			catch(Exception x)
			{
				System.err.println(x.getMessage());
			}
		}
		return battle;
	}
	
	protected static String[] getPlayerNames(String[] userName)
	{	
		userName[0] = helper.getUserName();
		userName[1] = helper.getOpponentName();
		return userName;
	}
	
	protected static void teamFromImportable(Team team)
	{
		String importable = team.exportImportable();
		String teamName = team.getTeamName();
		helper.createTeam(importable, teamName);
	}
	
	protected static void teamFromShowdown(Team user)
	{
		String userName = user.getUsername();
		List<String> ourPokes= helper.getTeam(userName);
		for(int n = 0; n < ourPokes.size(); n++)
		{
			Pokemon addition = user.addPokemon(ourPokes.get(n));
			List<String> ourMoves = helper.getMoves(ourPokes.get(n), true);
			addition.addMove(ourMoves, true);
			addition.setAbility(helper.getAbility(ourPokes.get(n), userName));
			addition.setItem(helper.getItem(ourPokes.get(n)));
			addition.setLevel(helper.getLevel(ourPokes.get(n), userName));
			System.out.println(ourPokes.get(n)+", "+userName);
		}
	}
	
	protected static void updateLastTurn()
	{
		String lastTurn = helper.getBattleLog().getLastTurnText();
		findPokemon(lastTurn);
		findMove(lastTurn);
		for(int t = 0; t < 2; t++)
		{
			Team team = battle.getTeam(t, false);
			if(team.getActive() != null)
			{
				String owner = team.getUsername();
				Status status = Status.statusFromString(helper.getStatus(team.getActive().getName(), owner));
				if(status != null)
					team.getActive().inflictStatus(status);
			}
		}
	}
	
	protected static Action battleStartAction()
	{
		try
		{
			whatDo = helper.waitForBattleStart();
		}
		catch(Exception e)
		{
			return null;
		}
		battle.battleStart();
		if(whatDo == TurnEndStatus.SWITCH)
			return new Change();
		else
			return new Attack();
	}
	
	protected static Action doAction(Action a)
	{
		if(a instanceof Attack)
		{
			try 
			{
				if(a.name.toLowerCase().startsWith("hidden power"))
					helper.doMove("Hidden Power");
				else
					helper.doMove(a.name);
			} catch (Exception e) 
			{
				System.err.println(((Attack)a).attacker.getName()+" could not do move "+a.name+"! Exception data:\n"+e);
				Simulator.print("Exception! "+((Attack)a).attacker.getName()+" could not do move "+a.name+"!");
				Action.onException(a, e, battle);
			}
		}
		else if(a instanceof Change)
		{
			Team t = ((Change)a).switchTo.getTeam();
			if(helper.isTrapped())
			{
				Pokemon active = t.getActive();
				active.setUsableMoves(helper.getUsableMoves());
				return new Attack();
			}
			try
			{
				helper.switchTo(a.name, false);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return nextTurn();
	}
	
	/**
	 * Prints a line of text to the battle log and the console.
	 * @see seleniumhelper.ShowdownHelper#sendMessage(String)
	 * @param text - The text to print.
	 */
	protected static void print(String text)
	{
		try
		{
			helper.sendMessage(text);
		}
		catch (Exception e)
		{
			System.err.println("Could not print to chat!");
			System.err.println("Message we attempted: "+text);
			System.err.println("Exception data: "+e);
		}
	}
	
	private static Action nextTurn()
	{
		whatDo = helper.waitForNextTurn(10);
		Team player = battle.getTeam(0, false);
		Team enemy = battle.getTeam(1, false);
		String username = player.getUsername();
		String enemyUsername = enemy.getUsername();
		Pokemon usActive = player.getPokemon(helper.getCurrentPokemon(true));
		Pokemon enemyActive = enemy.addPokemon(helper.getCurrentOpponentPokemon(true));
		usActive = player.changePokemon(usActive);
		enemyActive = enemy.changePokemon(enemyActive);
		String ourPoke = "";
		String enemyPoke = "";
		if(usActive != null)
			ourPoke = usActive.getName();
		if(enemyActive != null)
			enemyPoke = enemyActive.getName();
		try
		{
			if(usActive != null)
				usActive.setUsableMoves(helper.getUsableMoves());
			List<String> aliveUsList = helper.getAliveTeam(player.getUsername());
			String[] aliveUs = new String[0];
			aliveUs = aliveUsList.toArray(aliveUs);
			for(int i = 0; i < 6; i++)
			{
				Pokemon poke = player.getPokemon(i);
				boolean found = false;
				for(int a = 0; a < aliveUs.length; a++)
				{
					if(poke.nameIs(aliveUs[a]))
					{
						found = true;
						poke.setHP(helper.getHP(aliveUs[a], player.getUsername()), poke.getFullHP());
						break;
					}
				}
				if(found)
					continue;
				poke.setHP(0, poke.getFullHP());
			}
			if(!ourPoke.isEmpty())
			{
				usActive.setHP(helper.getHP(ourPoke, username), helper.getMaxHP(ourPoke, username));
				usActive.setAbility(helper.getAbility(ourPoke, username));
				usActive.setItem(helper.getItem(ourPoke));
			}
			if(!enemyPoke.isEmpty())
			{
				enemyActive.setHP(helper.getHP(enemyPoke, enemyUsername), 100);
				enemyActive.setAbility(helper.getAbility(enemyPoke, enemyUsername));
				enemyActive.setItem(helper.getItem(enemyPoke));
			}
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}
		if(whatDo == TurnEndStatus.ATTACK)
			return new Attack();
		else if(whatDo == TurnEndStatus.SWITCH)
			return new Change();
		battle.gameOver(whatDo == TurnEndStatus.WON);
		return null;
	}

	/**
	 * Adds all a Pokemon's moves to its movelist.
	 * @param poke (Pokemon): The Pokemon to add the moves to.
	 */
	protected static void getMoves(Pokemon poke)
	{
		List<String> ourMoves = ShowdownHandler.getMoves(poke.getName(),true);
		poke.addMove(ourMoves, true);
	}
	
	/**
	 * Gets the names of a Pokemon's moves.
	 * @param name (String): The name of this Pokemon.
	 * @param b (boolean): TRUE to get the shortnames of moves, else FALSE.
	 * @return
	 */
	private static List<String> getMoves(String name, boolean b) 
	{
		return helper.getMoves(name, b);
	}

	/**
	 * Returns if the active Pokemon is trapped.
	 * @return TRUE if we are trapped and cannot switch, else FALSE.
	 */
	protected static boolean canSwitch() 
	{
		return helper.isTrapped();
	}
	
	/**
	 * Takes a string of the last turn's events, finds all moves used and their damage, then updates HP and Pokemon.
	 * <b>TEMPORARY.</b> Will be replaced by a TurnInfo class in SeleniumHelper.
	 * @param text (String): The text to search.
	 */
	private static void findMove(String text)
	{
		Pattern p = Pattern.compile("(.+) used (.+)!", Pattern.MULTILINE);
		Matcher m = p.matcher(text);
		Team t = null;
		//Team.players[0] = new Team(0);
		//Team.players[1] = new Team(1);
		while(m.find())
		{
			String tempname = text.substring(m.start(1), m.end(1));
			if(tempname.contains("The foe's"))
			{
				t = battle.getTeam(1, false);
				tempname = stripFoe(tempname);
			}
			else
				t = battle.getTeam(0, false);
			Pokemon poke = t.addPokemon(tempname);
			String tempmove = text.substring(m.start(2), m.end(2));
			String moveDamage = text.substring(m.end(2));
			boolean crit = false;
			int dmg = 0;
			Pattern dmgP = Pattern.compile("(.+) lost (.+)%");
			Matcher dmgM = dmgP.matcher(moveDamage);
			if(dmgM.find())
			{
				String damage = moveDamage.substring(dmgM.start(1),dmgM.end(2));
				if(!damage.contains(tempname))
				{
					crit = damage.contains("critical hit");
					damage = moveDamage.substring(dmgM.start(2),dmgM.end(2));
					dmg = Integer.parseInt(damage);
				}
			}
			System.out.println(tempname+" used "+tempmove+" for "+dmg+"% damage. Was it a crit? "+crit);
			poke.onNewTurn(tempmove, dmg, crit);
		}
		findDrops(text);
	}
	
	/**
	 * Takes a string of the last turn's events and finds the boosts or drops.
	 * <b>TEMPORARY.</b> Will be replaced by a TurnInfo class in SeleniumHelper.
	 * @param text (String): The text to search.
	 */
	private static void findDrops(String text)
	{
		Pattern whP = Pattern.compile("(.+) restored its status using White Herb!");
		Matcher whM = whP.matcher(text);
		String[] restore = new String[2];
		while(whM.find())
		{
			String herb = text.substring(whM.start(1),whM.end(1));
			if(herb.contains("The foe's"))
			{
				herb = stripFoe(herb);
				restore[1] = herb;
			}
			else
				restore[0] = herb;
		}
		Pattern p = Pattern.compile("(.+)'s (.+) (fell|rose)!",Pattern.MULTILINE);
		Matcher m = p.matcher(text);
		Team t = null;
		while(m.find())
		{
			String temp = text.substring(m.start(1),m.end(1));
			String herb;
			if(temp.contains("The foe's"))
			{
				t = battle.getTeam(1, false);
				temp = stripFoe(temp);
				herb = restore[1];
			}
			else
			{
				t = battle.getTeam(0, false);
				herb = restore[0];
			}
			Pokemon poke = t.addPokemon(temp);
			String stat = text.substring(m.start(2),m.end(2));
			int level = 1;
			if(stat.contains("harshly")||stat.contains("sharply"))
			{
				String twoStage;
				if(stat.contains("harshly"))
					twoStage = "harshly";
				else
					twoStage = "sharply";
				level = 2;
				stat = stat.substring(0, stat.indexOf(twoStage));
			}
			else if(stat.contains("drastically")||stat.contains("dramatically"))
			{
				String threeStage;
				if(stat.contains("drastically"))
					threeStage = "drastically";
				else
					threeStage = "dramatically";
				level = 3;
				stat = stat.substring(0,stat.indexOf(threeStage));
			}
			Stat st = Stat.fromString(stat);
			String rose = text.substring(m.start(3),m.end(3));
			if(rose.toLowerCase().startsWith("fell"))
			{
				if(herb == null)
					level *= -1;
				else
				{
					System.out.println(poke.getName()+" restored its negative stat drop!");
					level = 0;
				}
			}
			if(level != 0)
				poke.giveBoosts(st, level);
		}
	}
	
	/**
	 * Strips "The foe's " from a result to give you just the Pokemon name.
	 * <b>TEMPORARY.</b> Will be replaced by a TurnInfo class in SeleniumHelper.
	 * @param tempname (String): The text to strip.
	 */
	private static String stripFoe(String tempname)
	{	
		String f = "";
		Pattern foeP = Pattern.compile("The foe's (.+)");
		Matcher foeM = foeP.matcher(tempname);
		foeM.find();
		if (foeM.end(1) - foeM.start(1) > 1)
		{
			f = foeM.group(1);
		}
		else
		{
			f = tempname.substring(0, foeM.start(1) - 2);
		}
		return f;
	}
	
	/**
	 * Takes a string of the last turn's events, finds any Pokemon that were sent out, and marks them as active.
	 * <b>TEMPORARY.</b> Will be replaced by a TurnInfo class in SeleniumHelper.
	 * @param text (String): The text to search.
	 */
	private static boolean findPokemon(String text)
	{
		boolean switched = false;
		String[] pokemon = new String[2];
		Pattern faintP = Pattern.compile("(.+) fainted!", Pattern.MULTILINE);
		Matcher faintM = faintP.matcher(text);
		while(faintM.find())
		{
			String faint = text.substring(faintM.start(1), faintM.end(1));
			Team t;
			if(faint.contains("The foe's "))
			{
				faint = stripFoe(faint);
				t = battle.getTeam(1, false);
			}
			else
				t = battle.getTeam(0, false);
			Pokemon poke = t.getPokemon(faint);
			if(poke == null)
				System.err.println("Could not find Pokemon "+faint);
			else
				poke.onDie();
		}
		pokemon[0] = helper.getCurrentPokemon(true);
		pokemon[1] = helper.getCurrentOpponentPokemon(true);
		for(int i = 0; i < pokemon.length; i++)
		{
			if(pokemon[i] != null)
			{
				Pokemon poke = battle.getTeam(i, false).addPokemon(pokemon[i]); //Not getPokemon because we don't know if we've seen it yet.
				poke.onSendOut();
				switched = true;
			}
		}
		return switched;
	}

	/**
	 * Leaves the current battle.
	 */
	protected static void leaveBattle() 
	{
		helper.leaveBattle();
	}

	/**
	 * Gets the remaining PP for a move.
	 * @param move (Move): The move to get the remaining PP for.
	 * @param ability (Ability): The enemy ability. NULL if not known.
	 * @return (int): The amount of PP left.
	 */
	protected static int getMoveRemainingPP(Move move, Ability ability) 
	{
		int pp = move.pp;
		try 
		{
			if(move.name.toLowerCase().startsWith("hidden power"))
				pp = helper.getMoveRemainingPP("Hidden Power");
			else
				pp = helper.getMoveRemainingPP(move.name);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			if(ability == null || !ability.getName().toLowerCase().startsWith("pressure"))
				pp--;
			else
				pp -=2;
		}
		return pp;
	}

	/**
	 * @param switchAction
	 */
	public static void switchTo(Change switchAction) 
	{
		Pokemon switchTo = switchAction.switchTo;
		try
		{
			helper.switchTo(switchTo.getName(), false);
		}
		catch (Exception e)
		{
			System.err.println("Could not switch to "+switchTo.getName()+"! Exception data:\n"+e);
			print("Exception! Could not switch to "+switchTo.getName()+"!");
			Action.onException(switchAction, e, battle);
		}
	}

	/**
	 * @return
	 */
	public static int getCurrentTurnNumber() 
	{
		return helper.getBattleLog().getCurrentTurn();
	}
}
