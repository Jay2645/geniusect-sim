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

public class SQLHandler {
	
	private static Connection conn = null;
	
	public static void openConnection()
	{
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
		openConnection();
		String move = m.name;
		System.out.println("Sending SQL query for move: " + move);
		//ArrayList hiddenPower = HiddenPowerCalculator.calculateHiddenPower();
		{
			try
			{
				PreparedStatement s = conn.prepareStatement("SELECT type, power, shortname, accuracy, category, pp, target, priority, move.desc FROM move WHERE name= ? ORDER BY name ASC");
				s.setString(1, move);
				s.executeQuery();
				ResultSet rs = s.getResultSet ();
				int count = 0;
				while (rs.next ())
				{
					m.type = Type.fromSQL(rs.getString("type"));
					m.shortname = rs.getString("shortname");
					m.power = Integer.parseInt(rs.getString("power"));
					String moveCategory = rs.getString("category");
					if(moveCategory.toLowerCase().startsWith("special"))
						m.setMoveType(MoveType.Special);
					else if(moveCategory.toLowerCase().startsWith("status"))
						m.setMoveType(MoveType.Status);
					else
						m.setMoveType(MoveType.Physical);
					m.accuracy = Integer.parseInt(rs.getString("accuracy"));
					m.pp = Integer.parseInt(rs.getString("pp"));
					m.target = Target.fromString(rs.getString("target"));
					m.priority = Integer.parseInt(rs.getString("priority"));
					/*if(move.startsWith("Hidden Power")) //TODO: Hidden Power calculation.
						{
						moveType = hiddenPower.get(1).toString();
						movePower = hiddenPower.get(0).toString();
						}*/
					System.out.println (
					"Name: " + m.name +
					", Shortname: " + m.shortname +
					", Type: " + m.type +
					", Power: " + m.power +
					", Move Type: "+m.getMoveType() +
					", Accuracy: " + m.accuracy +
					", Target: " + m.target +
					", Priority: " + m.priority +
					", PP: " + m.pp);
					++count;
				}
				rs.close ();
				s.close ();
				System.out.println (count + " rows were retrieved");
			}
			catch (SQLException e)
			{
				System.err.println ("Error message: " + e.getMessage ());
				System.err.println ("Error number: " + e.getErrorCode ());
			}
		}
		closeConnection();
		return m;
	}
	
	public static Move queryMoveShortname(Move m)
	{
		openConnection();
		String move = m.shortname;
		System.out.println("Sending SQL query for move: " + move);
		//ArrayList hiddenPower = HiddenPowerCalculator.calculateHiddenPower();
		{
			try
			{
				PreparedStatement s = conn.prepareStatement("SELECT type, power, name, accuracy, category, pp, target, priority, move.desc FROM move WHERE shortname= ? ORDER BY name ASC");
				s.setString(1, move);
				s.executeQuery();
				ResultSet rs = s.getResultSet ();
				int count = 0;
				while (rs.next ())
				{
					m.name = rs.getString("name");
					m.type = Type.fromSQL(rs.getString("type"));
					m.power = Integer.parseInt(rs.getString("power"));
					String moveCategory = rs.getString("category");
					if(moveCategory.toLowerCase().startsWith("special"))
						m.setMoveType(MoveType.Special);
					else if(moveCategory.toLowerCase().startsWith("status"))
						m.setMoveType(MoveType.Status);
					else
						m.setMoveType(MoveType.Physical);
					m.accuracy = Integer.parseInt(rs.getString("accuracy"));
					m.pp = Integer.parseInt(rs.getString("pp"));
					m.target = Target.fromString(rs.getString("target"));
					m.priority = Integer.parseInt(rs.getString("priority"));
					/*if(move.startsWith("Hidden Power")) //TODO: Hidden Power calculation.
						{
						moveType = hiddenPower.get(1).toString();
						movePower = hiddenPower.get(0).toString();
						}*/
					System.out.println (
						"Name: " + m.name +
						", Shortname: " + m.shortname +
						", Type: " + m.type +
						", Power: " + m.power +
						", Move Type: "+m.getMoveType() +
						", Accuracy: " + m.accuracy +
						", Target: " + m.target +
						", Priority: " + m.priority +
						", PP: " + m.pp);
					++count;
				}
				rs.close ();
				s.close ();
				System.out.println (count + " rows were retrieved");
			}
			catch (SQLException e)
			{
				System.err.println ("Error message: " + e.getMessage ());
				System.err.println ("Error number: " + e.getErrorCode ());
			}
		}
		closeConnection();
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
		openConnection();
		String currentPokemon = p.getName();
		System.out.println("Sending SQL query for Pokemon: " + currentPokemon);
		try
		{
			PreparedStatement s = conn.prepareStatement("SELECT pokemon.name, atk, spa, def, spd, hp, spe, pokemon.type0, pokemon.type1, ability0, ability1, abilityDW, tier FROM pokemon WHERE pokemon.name = ? ORDER BY name ASC");
			s.setString(1, currentPokemon); // set the first '?' in the query to the currentPokemon
			s.executeQuery(); // everything else is the same from here on
			ResultSet rs = s.getResultSet();
			int count = 0;
			String[] types = new String[2];
			Ability abilityZero = null;
			Ability abilityOne = null;
			Ability abilityDW = null;
			while (rs.next ())
			{
				if(!rs.getString("name").toLowerCase().startsWith(currentPokemon.toLowerCase()))
					System.err.println("Could not find find "+currentPokemon+" in SQL database!");
				String[] stats = new String[6];
				stats[0] = rs.getString("hp");
				stats[1] = rs.getString("atk");
				stats[2] = rs.getString("def");
				stats[3] = rs.getString("spa");
				stats[4] = rs.getString("spd");
				stats[5] = rs.getString("spe");
				for(int i = 0; i < stats.length; i++)
				{
					if(p.getBaseStat(Stat.fromInt(i)) != 0)
						continue;
					if(stats[i] == null || stats[i].isEmpty())
					{
						System.err.println("Could not determine "+currentPokemon+"'s "+Stat.fromInt(i).toString()+" stat!");
						continue;
					}
					p.setBaseStat(i, Integer.parseInt(stats[i]));
				}
				p.setTier(rs.getString("tier"));
				types[0] = rs.getString("type0");
				types[1] = rs.getString("type1");
				abilityZero = p.setPossibleAbilties(rs.getString("ability0"), 0);
				abilityOne = p.setPossibleAbilties(rs.getString("ability1"), 1);
				abilityDW = p.setPossibleAbilties(rs.getString("abilityDW"), 2);
				++count;
			}
			p.setType(Type.fromSQL(types[0]), Type.fromSQL(types[1]));
			if(p.getType(0) == Type.None && p.getBaseStat(Stat.Def) == 0 && p.getBaseStat(Stat.SpD) == 0)
			{
				System.err.println(currentPokemon+" is not in the SQL table!");
				Battle.criticalErrors = Battle.criticalErrors + "\n"+currentPokemon+" is not in the SQL table!";
			}
			System.out.println("Pokemon name: " + 			currentPokemon);
			System.out.println("Pokemon type 1: " + 		p.getType(0));
			System.out.println("Pokemon type 2: " + 		p.getType(1));
			System.out.println("Tier: " + 					p.getTier());
			if(abilityZero != null)
				System.out.println("Ability 0: " +			abilityZero.getName());
			if(abilityOne != null)
				System.out.println("Ability 1: " +			abilityOne.getName());
			if(abilityDW != null)
				System.out.println("Ability DW: " +			abilityDW.getName());			
			System.out.println("Base HP: " + 				p.getBaseStat(Stat.HP));
			System.out.println("Base Attack: " + 			p.getBaseStat(Stat.Atk));
			System.out.println("Base Defense: " + 			p.getBaseStat(Stat.Def));
			System.out.println("Base Special Attack: " + 	p.getBaseStat(Stat.SpA));
			System.out.println("Base Special Defense: " +	p.getBaseStat(Stat.SpD));
			System.out.println("Base Speed: " +		 		p.getBaseStat(Stat.Spe));
			rs.close ();
			s.close ();
			System.out.println (count + " rows were retrieved");
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
		/*
		 * TODO: Resistance lookup (old one was too Jaycode-y to decipher).
		p.hyperEffective = quadrupleDamageLookup(p.types);
		p.superEffective = doubleDamageLookup(p.types);
		p.resistances = halfDamageLookup(p.types);
		p.doubleResistances = quarterDamageLookup(p.types);
		*/
		closeConnection();
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
