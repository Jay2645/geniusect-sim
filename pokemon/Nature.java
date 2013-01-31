/*
 * A list of all natures in the game, along with what they modify.
 * The modifier can be returned in multiple ways, or the enum can modify the stat directly.
 * @author TeamForretress
 */

package geniusectsim.pokemon;


public enum Nature {
	Hardy("Hardy", Stat.Atk,Stat.Atk), Docile("Docile", Stat.Def,Stat.Def), Serious("Serious", Stat.Spe,Stat.Spe), 
	Bashful("Bashful", Stat.SpA,Stat.SpA),Quirky("Quirky",Stat.SpD,Stat.SpD), Lonely("Lonely", Stat.Atk,Stat.Def), 
	Brave("Brave", Stat.Atk,Stat.Spe), Adamant("Adamant", Stat.Atk,Stat.SpA), Naughty("Naughty", Stat.Atk,Stat.SpD), 
	Bold("Bold", Stat.Def,Stat.Atk), Relaxed("Relaxed", Stat.Def,Stat.Spe), Impish("Impish", Stat.Def,Stat.SpA), 
	Lax("Lax", Stat.Def,Stat.SpD), Modest("Modest", Stat.SpA, Stat.Atk), Mild("Mild", Stat.SpA, Stat.Def), Quiet("Quiet", Stat.SpA,Stat.Spe),
	Rash("Rash", Stat.SpA,Stat.SpD), Calm("Calm", Stat.SpD, Stat.Atk), Gentle("Gentle", Stat.SpD, Stat.Def), 
	Sassy("Sassy", Stat.SpD, Stat.Spe), Careful("Careful", Stat.SpD, Stat.SpA), Timid("Timid", Stat.Spe,Stat.Atk), 
	Hasty("Hasty", Stat.Spe,Stat.Def), Jolly("Jolly", Stat.Spe,Stat.SpA), Naive("Naive", Stat.Spe,Stat.SpD);
	private Stat[] mod = new Stat[2];
	private String name;
	private Nature(String name, Stat boost, Stat reduce)
	{
		mod[0] = boost;
		mod[1] = reduce;
	}
	
	public Stat[] multiplier()
	{
		return mod;
	}
	
	public double multiplier(Stat type)
	{
		if(type == mod[0] && type == mod[1])
			return 1;
		else if(type == mod[0])
			return 1.1;
		else if(type == mod[1])
			return 0.9;
		else return 1;
	}
	
	public int multiplier(int type, int stat)
	{
		return multiplier(Stat.fromInt(type), stat);
	}
	
	public int multiplier(Stat type, int stat)
	{
		if(mod[0] == type && mod[1] == type)
			return stat;
		else if(mod[0] == type)
			return (int)Math.round(stat * 1.1);
		else if(mod[1] == type)
			return (int)Math.round(stat * 0.9);
		else return stat;
	}
	
	public String toString()
	{
		return name;
	}
	
	public static Nature fromString(String n)
	{
		//I didn't want to have to write 50000 if statements because jaycode but oh well.
			if(n.toLowerCase().startsWith("har"))
				return Nature.Hardy;
			else if(n.toLowerCase().startsWith("d"))
				return Nature.Docile;
			else if(n.toLowerCase().startsWith("se"))
				return Nature.Serious;
			else if(n.toLowerCase().startsWith("ba"))
				return Nature.Bashful;
			else if(n.toLowerCase().startsWith("quir"))
				return Nature.Quirky;
			else if(n.toLowerCase().startsWith("l"))
				return Nature.Lonely;
			else if(n.toLowerCase().startsWith("br"))
				return Nature.Brave;
			else if(n.toLowerCase().startsWith("a"))
				return Nature.Adamant;
			else if(n.toLowerCase().startsWith("nau"))
				return Nature.Naughty;
			else if(n.toLowerCase().startsWith("bo"))
				return Nature.Bold;
			else if(n.toLowerCase().startsWith("r"))
				return Nature.Relaxed;
			else if(n.toLowerCase().startsWith("i"))
				return Nature.Impish;
			else if(n.toLowerCase().startsWith("l"))
				return Nature.Lax;
			else if(n.toLowerCase().startsWith("mo"))
				return Nature.Modest;
			else if(n.toLowerCase().startsWith("mi"))
				return Nature.Mild;
			else if(n.toLowerCase().startsWith("qui"))
				return Nature.Quiet;
			else if(n.toLowerCase().startsWith("r"))
				return Nature.Rash;
			else if(n.toLowerCase().startsWith("cal"))
				return Nature.Calm;
			else if(n.toLowerCase().startsWith("g"))
				return Nature.Gentle;
			else if(n.toLowerCase().startsWith("s"))
				return Nature.Sassy;
			else if(n.toLowerCase().startsWith("c"))
				return Nature.Careful;
			else if(n.toLowerCase().startsWith("t"))
				return Nature.Timid;
			else if(n.toLowerCase().startsWith("h"))
				return Nature.Hasty;
			else if(n.toLowerCase().startsWith("j"))
				return Nature.Jolly;
			else if(n.toLowerCase().startsWith("n"))
				return Nature.Naive;
		else return Nature.Hardy;
	}
}
