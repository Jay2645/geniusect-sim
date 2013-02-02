/*
 * A class which hooks into an SQL server and returns the data within.
 * @author TeamForretress
 */

package geniusectsim.constants;

import geniusectsim.abilities.Ability;
import geniusectsim.battle.Battle;
import geniusectsim.battle.Type;
import geniusectsim.moves.Move;
import geniusectsim.moves.MoveType;
import geniusectsim.moves.Target;
import geniusectsim.pokemon.Pokemon;
import geniusectsim.pokemon.Stat;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class SQLHandler {
	
	private static Connection conn = null;
	private static Map<String, Pokemon> pokeCache = new HashMap<String, Pokemon>();
	private static Map<String, Move> moveCache = new HashMap<String, Move>();
	
	public static void openConnection()
	{
		Throwable t = new Throwable();
		t.printStackTrace();
		if(conn == null)
		{
			System.out.println("Opening connection to SQL server.");
			try
			{
				String userName = "root";
				String password = "securepassword";
				String url = "jdbc:mysql://localhost:3306/pokemon";
				Class.forName ("com.mysql.jdbc.Driver").newInstance ();
				conn = DriverManager.getConnection (url, userName, password);
				System.out.println ("Database connection established");
			}
			catch (Exception e)
			{
				System.err.println ("Cannot connect to database server, error: " +e);
			}
		}
	}
		
	public static Move queryMove(Move m)
	{
		String name = m.name;
		Type type = null;
		String shortname = "";
		int power = 0;
		MoveType moveType = null;
		int accuracy = 0;
		int pp = 0;
		Target target = null;
		int priority = 0;
		boolean updateCache = false;
		if(moveCache.containsKey(name))
		{
			Move cached = moveCache.get(name);
			type = cached.type;
			shortname = cached.shortname;
			power = cached.power;
			moveType = cached.getMoveType();
			accuracy = cached.accuracy;
			pp = cached.pp;
			target = cached.target;
			priority = cached.priority;
		}
		else
		{
			openConnection();
			System.out.println("Sending SQL query for move: " + name);
			//ArrayList hiddenPower = HiddenPowerCalculator.calculateHiddenPower();
			{
				try
				{
					PreparedStatement s = conn.prepareStatement("SELECT type, power, shortname, accuracy, category, pp, target, priority, move.desc FROM move WHERE name= ? ORDER BY name ASC");
					s.setString(1, name);
					s.executeQuery();
					ResultSet rs = s.getResultSet ();
					int count = 0;
					while (rs.next ())
					{
						type = Type.fromSQL(rs.getString("type"));
						shortname = rs.getString("shortname");
						power = Integer.parseInt(rs.getString("power"));
						String moveCategory = rs.getString("category");
						if(moveCategory.toLowerCase().startsWith("special"))
							moveType = MoveType.Special;
						else if(moveCategory.toLowerCase().startsWith("status"))
							moveType = MoveType.Status;
						else
							moveType = MoveType.Physical;
						accuracy = Integer.parseInt(rs.getString("accuracy"));
						pp = Integer.parseInt(rs.getString("pp"));
						target = Target.fromString(rs.getString("target"));
						priority = Integer.parseInt(rs.getString("priority"));
						/*if(move.startsWith("Hidden Power")) //TODO: Hidden Power calculation.
							{
							moveType = hiddenPower.get(1).toString();
							movePower = hiddenPower.get(0).toString();
							}*/
						++count;
					}
					rs.close ();
					s.close ();
					updateCache = true;
					System.out.println (count + " rows were retrieved");
				}
				catch (SQLException e)
				{
					System.err.println ("Error message: " + e.getMessage ());
					System.err.println ("Error number: " + e.getErrorCode ());
					closeConnection();
				}
			}
		}
		System.out.println (
			"Name: " + name +
			"\n, Shortname: " + shortname +
			"\n, Type: " + type +
			"\n, Power: " + power +
			"\n, Move Type: "+moveType +
			"\n, Accuracy: " + accuracy +
			"\n, Target: " + target +
			"\n, Priority: " + priority +
			"\n, PP: " + pp);
		m.name = name;
		m.shortname = shortname;
		m.type = type;
		m.setMoveType(moveType);
		m.accuracy = accuracy;
		m.target = target;
		m.priority = priority;
		m.pp = pp;
		if(updateCache)
		{
			closeConnection();
			moveCache.put(name, new Move(m));
			moveCache.put(shortname, new Move(m));
		}
		return m;
	}
	
	public static Move queryMoveShortname(Move m)
	{
		openConnection();
		String name = "";
		Type type = null;
		String shortname = m.shortname;
		int power = 0;
		MoveType moveType = null;
		int accuracy = 0;
		int pp = 0;
		Target target = null;
		int priority = 0;
		boolean updateCache = false;
		if(moveCache.containsKey(shortname))
		{
			Move cached = moveCache.get(shortname);
			type = cached.type;
			name = cached.name;
			power = cached.power;
			moveType = cached.getMoveType();
			accuracy = cached.accuracy;
			pp = cached.pp;
			target = cached.target;
			priority = cached.priority;
		}
		else
		{
			System.out.println("Sending SQL query for move: " + shortname);
			//ArrayList hiddenPower = HiddenPowerCalculator.calculateHiddenPower();
			try
			{
				PreparedStatement s = conn.prepareStatement("SELECT type, power, name, accuracy, category, pp, target, priority, move.desc FROM move WHERE shortname= ? ORDER BY name ASC");
				s.setString(1, shortname);
				s.executeQuery();
				ResultSet rs = s.getResultSet ();
				int count = 0;
				while (rs.next ())
				{
					name = rs.getString("name");
					type = Type.fromSQL(rs.getString("type"));
					power = Integer.parseInt(rs.getString("power"));
					String moveCategory = rs.getString("category");
					if(moveCategory.toLowerCase().startsWith("special"))
						moveType = MoveType.Special;
					else if(moveCategory.toLowerCase().startsWith("status"))
						moveType = MoveType.Status;
					else
						moveType = MoveType.Physical;
					accuracy = Integer.parseInt(rs.getString("accuracy"));
					pp = Integer.parseInt(rs.getString("pp"));
					target = Target.fromString(rs.getString("target"));
					priority = Integer.parseInt(rs.getString("priority"));
					/*if(move.startsWith("Hidden Power")) //TODO: Hidden Power calculation.
					{
						moveType = hiddenPower.get(1).toString();
						movePower = hiddenPower.get(0).toString();
					}*/
					++count;
				}
				rs.close ();
				s.close ();
				updateCache = true;
				System.out.println (count + " rows were retrieved");
			}
			catch (SQLException e)
			{
				System.err.println ("Error message: " + e.getMessage ());
				System.err.println ("Error number: " + e.getErrorCode ());
				closeConnection();
			}
		}
		System.out.println (
				"Name: " + name +
				"\nShortname: " + shortname +
				"\nType: " + type +
				"\nPower: " + power +
				"\nMove Type: "+moveType +
				"\nAccuracy: " + accuracy +
				"\nTarget: " + target +
				"\nPriority: " + priority +
				"\nPP: " + pp);
		m.name = name;
		m.shortname = shortname;
		m.type = type;
		m.setMoveType(moveType);
		m.accuracy = accuracy;
		m.target = target;
		m.priority = priority;
		m.pp = pp;
		if(updateCache)
		{
			closeConnection();
			moveCache.put(name, new Move(m));
			moveCache.put(shortname, new Move(m));
		}
		return m;
	}
	
	public static double queryDamage(Type attack, Type defender)
	{
		if(attack == null || defender == null || attack == Type.None || defender == Type.None)
			return 1;
		double multiplier = 1;
		openConnection();
		//System.out.println("Sending SQL query to determine type effectiveness where the attacker is: " + attack.toString() +" and the defender is "+defender.toString());
		try
		{
			PreparedStatement s = conn.prepareStatement("SELECT dmg_mult, typeatk, typedef FROM type_effect WHERE typeatk= ? AND typedef= ? ORDER BY dmg_mult ASC");
			s.setString(1, attack.toString());
			s.setString(2, defender.toString());
			s.executeQuery();
			ResultSet rs = s.getResultSet ();
			//int count = 0;
			while (rs.next ())
			{
				multiplier = rs.getDouble("dmg_mult");
				//System.out.println(multiplier);
				//count++;
			}
			rs.close ();
			s.close ();
			//System.out.println (count + " rows were retrieved");
		}
		catch (SQLException e)
		{
			System.err.println ("Error message: " + e.getMessage ());
			System.err.println ("Error number: " + e.getErrorCode ());
		}
		catch (NumberFormatException nfe)
		{
			System.err.println("Number Format Exception error: " + nfe.getMessage());
		}
		//System.out.println("Result: "+multiplier);
		closeConnection();
		return multiplier;
	}
	
	public static Pokemon queryPokemon(Pokemon p)
	{
		String currentPokemon = p.getName();
		String abilityZero = null;
		String abilityOne = null;
		String abilityDW = null;
		Type[] typing = new Type[2];
		int[] baseStats = new int[6];
		String tier = null;
		boolean updateCache = false;
		if(pokeCache.containsKey(currentPokemon))
		{
			Pokemon cached = pokeCache.get(currentPokemon);
			Ability zero = cached.getAbility(0);
			Ability one = cached.getAbility(1);
			Ability dw = cached.getAbility(2);
			if(zero != null)
				abilityZero = zero.getName();
			if(one != null)
				abilityOne = one.getName();
			if(dw != null)
				abilityDW = dw.getName();
			typing = cached.getTypes();
			baseStats = cached.getBaseStats();
			tier = cached.getTier().name();
		}
		else
		{
			openConnection();
			System.out.println("Sending SQL query for Pokemon: " + currentPokemon);
			try
			{
				PreparedStatement s = conn.prepareStatement("SELECT pokemon.name, atk, spa, def, spd, hp, spe, pokemon.type0, pokemon.type1, ability0, ability1, abilityDW, tier FROM pokemon WHERE pokemon.name = ? ORDER BY name ASC");
				s.setString(1, currentPokemon); // set the first '?' in the query to the currentPokemon
				s.executeQuery(); // everything else is the same from here on
				ResultSet rs = s.getResultSet();
				int count = 0;
				String[] types = new String[2];
				String[] stats = new String[6];
				while (rs.next ())
				{
					if(!rs.getString("name").toLowerCase().startsWith(currentPokemon.toLowerCase()))
						System.err.println("Could not find find "+currentPokemon+" in SQL database!");
					stats[0] = rs.getString("hp");
					stats[1] = rs.getString("atk");
					stats[2] = rs.getString("def");
					stats[3] = rs.getString("spa");
					stats[4] = rs.getString("spd");
					stats[5] = rs.getString("spe");
					tier = rs.getString("tier");
					types[0] = rs.getString("type0");
					types[1] = rs.getString("type1");
					abilityZero = rs.getString("ability0");
					abilityOne = rs.getString("ability1");
					abilityDW = rs.getString("abilityDW");
					++count;
				}
				for(int i = 0; i < stats.length; i++)
				{
					if(stats[i] == null || stats[i].isEmpty())
					{
						System.err.println("Could not determine "+currentPokemon+"'s "+Stat.fromInt(i).toString()+" stat!");
						continue;
					}
					baseStats[i] = Integer.parseInt(stats[i]);
				}
				typing[0] = Type.fromSQL(types[0]); 
				typing[1] = Type.fromSQL(types[1]);
				if(typing[0] == Type.None && baseStats[Stat.Def.toInt()] == 0 && baseStats[Stat.SpD.toInt()] == 0)
				{
					System.err.println(currentPokemon+" is not in the SQL table!");
					Battle.criticalErrors = Battle.criticalErrors + "\n"+currentPokemon+" is not in the SQL table!";
				}
				rs.close ();
				s.close ();
				updateCache = true;
				System.out.println (count + " rows were retrieved");
			}
			catch (SQLException e)
			{
				System.err.println ("Error message: " + e.getMessage ());
				System.err.println ("Error number: " + e.getErrorCode ());
				closeConnection();
			}
			catch (NumberFormatException nfe)
			{
				System.err.println("Number Format Exception error: " + nfe.getMessage());
			}
		}
		/*
		 * TODO: Immunity lookup.
		 */
		System.out.println("Pokemon name: " + 			currentPokemon);
		System.out.println("Pokemon type 1: " + 		typing[0]);
		System.out.println("Pokemon type 2: " + 		typing[1]);
		System.out.println("Tier: " + 					tier);
		if(abilityZero != null)
			System.out.println("Ability 0: " +			abilityZero);
		if(abilityOne != null)
			System.out.println("Ability 1: " +			abilityOne);
		if(abilityDW != null)
			System.out.println("Ability DW: " +			abilityDW);			
		System.out.println("Base HP: " + 				baseStats[Stat.HP.toInt()]);
		System.out.println("Base Attack: " + 			baseStats[Stat.Atk.toInt()]);
		System.out.println("Base Defense: " + 			baseStats[Stat.Def.toInt()]);
		System.out.println("Base Special Attack: " + 	baseStats[Stat.SpA.toInt()]);
		System.out.println("Base Special Defense: " +	baseStats[Stat.SpD.toInt()]);
		System.out.println("Base Speed: " +		 		baseStats[Stat.Spe.toInt()]);
		p.setType(typing[0], typing[1]);
		p.setTier(tier);
		p.setPossibleAbilties(abilityZero, 0);
		p.setPossibleAbilties(abilityOne, 1);
		p.setPossibleAbilties(abilityDW, 2);
		p.setBaseStats(baseStats);
		if(updateCache)
		{
			closeConnection();
			pokeCache.put(currentPokemon, new Pokemon(p));
		}
		return p;
	}
	
	private static void closeConnection()
	{
		/*if (conn != null)
		{
			System.out.println("Closing connection to SQL server.");
			try
			{
				conn.close ();
				System.out.println ("Database connection terminated");
			}
			catch (Exception e) { }
		}*/
	}
}
