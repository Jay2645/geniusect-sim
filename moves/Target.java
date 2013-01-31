/*
 * A list of all possible targets for a move.
 * @author TeamForretress
 */

package geniusectsim.moves;

public enum Target {
	Adjacent ("Adjacent"), All("All"), Allies("Allies"), Ally("Ally"),
	AllySide("AllySide"), Any("Any"), AnyFoe("AnyFoe"), Foes("Foes"), 
	FoeSide("FoeSide"), Normal("Normal"), Self("Self");
	private String name;
	
	private Target(String n)
	{
		n = name;
	}
	
	public static Target fromString(String name)
	{
		Target target = Target.Normal;
		
		return target;
	}
}
