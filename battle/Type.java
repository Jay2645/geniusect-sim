/*
 * A list of all types in the game.
 * Can be cast as a string.
 * @author TeamForretress
 */

package geniusectsim.battle;

public enum Type {
	Normal("Normal", 0, 17), Fighting("Fighting", 6, 1), Ghost("Ghost", 13, 7), Electric("Electric", 3, 12), Fire("Fire", 1, 9), Water("Water", 2, 10),
	Grass("Grass", 4, 11), Dark("Dark", 15, 16), Psychic("Psychic", 10, 13), Steel("Steel", 16, 8), Ground("Ground", 8, 4), 
	Rock("Rock", 12, 5), Dragon("Dragon", 14, 15), Ice("Ice", 5, 14), Bug("Bug", 11, 6), Flying("Flying", 9, 2), Poison("Poison", 7, 3), None("None", -1, -1);
	private String name;
	private int sqlID;
	private int gameID;
	
	private Type(String s, int sql, int g)
	{
		name = s;
		sqlID = sql;
		gameID = g;
	}
	
	public String toString()
	{
		return name;
	}
	
	public static Type fromSQL(String typeString)
	{
		if(typeString == null)
			return Type.None;
		if(typeString.toLowerCase().startsWith("fire"))
			return Type.Fire;
		if(typeString.toLowerCase().startsWith("water"))
			return Type.Water;
		if(typeString.toLowerCase().startsWith("grass"))
			return Type.Grass;
		if(typeString.toLowerCase().startsWith("normal"))
			return Type.Normal;
		if(typeString.toLowerCase().startsWith("fighting"))
			return Type.Fighting;
		if(typeString.toLowerCase().startsWith("poison"))
			return Type.Poison;
		if(typeString.toLowerCase().startsWith("steel"))
			return Type.Steel;
		if(typeString.toLowerCase().startsWith("dragon"))
			return Type.Dragon;
		if(typeString.toLowerCase().startsWith("ghost"))
			return Type.Ghost;
		if(typeString.toLowerCase().startsWith("electric"))
			return Type.Electric;
		if(typeString.toLowerCase().startsWith("flying"))
			return Type.Flying;
		if(typeString.toLowerCase().startsWith("dark"))
			return Type.Dark;
		if(typeString.toLowerCase().startsWith("psychic"))
			return Type.Psychic;
		if(typeString.toLowerCase().startsWith("ground"))
			return Type.Ground;
		if(typeString.toLowerCase().startsWith("rock"))
			return Type.Rock;
		if(typeString.toLowerCase().startsWith("ice"))
			return Type.Ice;
		if(typeString.toLowerCase().startsWith("bug"))
			return Type.Bug;
		else return Type.None;
	}
	
	public int toGameID()
	{
		return gameID;
	}
}
