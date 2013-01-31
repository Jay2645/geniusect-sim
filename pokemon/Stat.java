/*
 * A list of all possible stats.
 * Used to easily grab indexes from arrays.
 * @author TeamForretress
 */

package geniusectsim.pokemon;

public enum Stat {
	HP(0),Atk(1),Def(2),SpA(3),SpD(4),Spe(5);
	private int value;
	
	private Stat(int value)
	{
		this.value = value;
	}
	
	public int toInt()
	{
		return value;
	}
	
	public static Stat fromInt(int i)
	{
		Stat stat = null;
		switch(i)
		{
			case 0: stat = Stat.HP;
					break;
			case 1: stat = Stat.Atk;
					break;
			case 2: stat = Stat.Def;
					break;
			case 3: stat = Stat.SpA;
					break;
			case 4: stat = Stat.SpD;
					break;
			case 5: stat = Stat.Spe;
					break;
		}
		return stat;
	}
	
	public static Stat fromString(String s)
	{
		s = s.toLowerCase();
		if(s.startsWith("hp"))
			return Stat.HP;
		if(s.startsWith("attack"))
			return Stat.Atk;
		if(s.startsWith("special attack"))
			return Stat.SpA;
		if(s.startsWith("defense"))
			return Stat.Def;
		if(s.startsWith("special defense"))
			return Stat.SpD;
		return Stat.Spe;
	}
}
