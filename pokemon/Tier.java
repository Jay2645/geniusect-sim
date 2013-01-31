package geniusectsim.pokemon;

import java.util.ArrayList;

/**
 * Represents a list of all possible tiers for a Pokemon to be in.
 * @author TeamForretress
 */
public enum Tier 
{
	LC(0, "LC"), NFE(1, "NFE"), NU(2, "NU"), BL3(3, "BL3"), RU(4,"RU"), BL2(5,"BL2"), 
	UU(6, "UU"), BL(7, "BL"), OU(8,"OU"), Limbo(9,"Limbo"), Uber(10,"Uber"), G4CAP(11,"G4CAP"), 
	G5CAP(12,"G5CAP"), CAP(13, "CAP"), Illegal(14,"Illegal"), Hackmons(15, "Hackmons");
	private int ranking;
	private String name;
	private Tier(int r, String n)
	{
		ranking = r;
		name = n;
	}
	
	/**
	 * Gets this tier's ranking.
	 * @return (int): This tier's ranking.
	 */
	public int tierToRank()
	{
		return ranking;
	}
	
	/**
	 * Returns the name of this Tier.
	 * @return (String): The name of this tier.
	 */
	public String toString()
	{
		return name;
	}
	
	/**
	 * Returns the closest USABLE tier.
	 * i.e. "NFE" will return NU, "G5CAP" will return CAP, "Limbo" will return Uber, etc.
	 * @return (Tier): The closest joinable tier.
	 */
	public Tier usableTier()
	{
		if(name.equals("NFE"))
			return Tier.NU;
		else if(name.equals("BL3"))
			return Tier.RU;
		else if(name.equals("BL2"))
			return Tier.UU;
		else if(name.equals("BL"))
			return Tier.OU;
		else if(name.equals("Limbo"))
			return Tier.Uber;
		else if(name.contains("CAP"))
			return Tier.CAP;
		else if(name.equals("Illegal"))
			return Tier.Hackmons;
		else return this;
	}
	
	/**
	 * Takes a String, returns the tier associated with that String.
	 * If none can be found, returns Tier.OU.
	 * @param tier (String): The tier to lookup.
	 * @return (Tier): The found Tier.
	 */
	public static Tier tierFromString(String tier)
	{
		tier = tier.toLowerCase();
		if(tier.contains("lc"))
			return Tier.LC;
		if(tier.contains("nfe"))
			return Tier.NFE;
		if(tier.contains("nu"))
			return Tier.NU;
		if(tier.contains("bl3"))
			return Tier.BL3;
		if(tier.contains("ru"))
			return Tier.RU;
		if(tier.contains("bl2"))
			return Tier.BL2;
		if(tier.contains("uu"))
			return Tier.UU;
		if(tier.contains("bl"))
			return Tier.BL;
		if(tier.contains("ou"))
			return Tier.OU;
		if(tier.contains("limbo"))
			return Tier.Limbo;
		if(tier.contains("uber"))
			return Tier.Uber;
		if(tier.contains("g4cap"))
			return Tier.G4CAP;
		if(tier.contains("g5cap"))
			return Tier.G5CAP;
		if(tier.contains("cap"))
			return Tier.CAP;
		if(tier.contains("illegal"))
			return Tier.Illegal;
		if(tier.contains("hackmons"))
			return Tier.Hackmons;
		else return Tier.OU;
	}
	
	/**
	 * Takes an int and returns a tier with that int as its rank.
	 * @param rank (int): The int to convert from.
	 * @return (Tier): The Tier with that rank. Returns OU if invalid.
	 */
	public static Tier tierFromInt(int rank)
	{
		switch(rank)
		{
			case 0: 	return Tier.LC;
			case 1: 	return Tier.NFE;
			case 2: 	return Tier.NU;
			case 3: 	return Tier.RU;
			case 4: 	return Tier.RU;
			case 5: 	return Tier.BL2;
			case 6: 	return Tier.UU;
			case 7: 	return Tier.BL;
			case 8: 	return Tier.OU;
			case 9: 	return Tier.Limbo;
			case 10:	return Tier.Uber;
			case 11:	return Tier.G4CAP;
			case 12:	return Tier.G5CAP;
			case 13:	return Tier.CAP;
			case 14:	return Tier.Illegal;
			case 15:	return Tier.Hackmons;
			default:	return Tier.OU;
		}
	}
	
	/**
	 * Returns all tiers between Tier one and Tier two. 
	 * The order in which they are listed does not matter.
	 * If Tier one and Tier two are the same, returns an array size one containing that tier.
	 * @param one (Tier): The first Tier to search between.
	 * @param two (Tier): The second Tier to search between.
	 * @return (Tier[]): An array of all tiers between the first and second (inclusive).
	 */
	public static Tier[] getBetween(Tier one, Tier two)
	{
		int rankOne = one.tierToRank();
		int rankTwo = two.tierToRank();
		int max = Math.max(rankOne, rankTwo);
		int min = Math.min(rankOne, rankTwo);
		Tier[] tierArray = new Tier[1];
		if(min == max)
		{
			tierArray[0] = tierFromInt(min);
			return tierArray;
		}
		ArrayList<Tier> tiers = new ArrayList<Tier>();
		for(int i = min; i <= max; i++)
		{
			tiers.add(tierFromInt(i));
		}
		tierArray = tiers.toArray(tierArray);
		return tierArray;
	}
}
