/*
 * A special class for the move Hidden Power.
 * Used to determine the type and power of Hidden Power used.
 * @author Team Forretress
 */

package geniusectsim.moves;


import geniusectsim.battle.Type;
import geniusectsim.constants.SQLHandler;

import java.util.ArrayList;

public class HiddenPower extends Move {
	public HiddenPower(Move clone)
	{
		if(clone instanceof HiddenPower && ((HiddenPower)clone).type != null && ((HiddenPower)clone).type != Type.None)
			type = ((HiddenPower)clone).type;
		name = "Hidden Power";
		user = clone.user;
		pp = clone.pp;
		accuracy = 100;
		target = Target.Normal;
		moveType = MoveType.Special;
		power = 70;
		boostChance = 0;
		recoilPercent = 0;
		disabled = clone.disabled;
		projectedDamage = clone.projectedDamage;
		projectedPercent = clone.projectedPercent;
		recalculateType();
	}
	
	private ArrayList<Type> resists = new ArrayList<Type>(); //What we know this resists this.
	private ArrayList<Type> strengths = new ArrayList<Type>(); //What we know this is super-effective against.
	
	private ArrayList<Type> possibles = new ArrayList<Type>();
	
	public void setType(Type t)
	{
		type = t;
	}
	
	public void setPower(int p)
	{
		power = p;
	}
	
	/**
	 * Add a type that is known to resist this Hidden Power.
	 * @param t (Type): The type that resisted.
	 */
	public void addResist(Type t)
	{
		if(resists.contains(t))
			return;
		resists.add(t);
		recalculateType();
	}
	
	/**
	 * Gets this Hidden Power type.
	 * @return (Type): The predicted (or known) Hidden Power type.
	 */
	public Type getType()
	{
		if(type == null || type == Type.None)
		{
			recalculateType();
			Type potential = Type.None;
			for(int i = 0; i < possibles.size(); i++)
			{
				Type possible = possibles.get(i);
				if(		possible == Type.Ice || 
						possible == Type.Fire || 
						possible == Type.Fighting ||
						possible == Type.Grass || 
						possible == Type.Electric ||
						possible == Type.Ground)
					potential = possible;
			}
			if(potential == Type.None)
				potential = Type.Normal;
			return potential;
		}
		return type;
	}
	
	/**
	 * Add a type this Hidden Power is known to be good against.
	 * @param t (Type): The type it is good against.
	 */
	public void addStrength(Type t)
	{
		if(strengths.contains(t))
			return;
		strengths.add(t);
		recalculateType();
	}
	
	public void recalculateType()
	{
		ArrayList<Type> typeCombos = new ArrayList<Type>();
		for(Type type : Type.values())
		{
			if(type == Type.Normal)
				continue;
			boolean next = false;
			int neutralCount = 0;
			for(int i = 0; i < strengths.size(); i++)
			{
				Type strength = strengths.get(i);
				double effectiveness = SQLHandler.queryDamage(type, strength);
				if(effectiveness == 1) //Sometimes there might be neutral types that got caught up in there, so we have to let them slip by.
					neutralCount++;
				else if(effectiveness < 1)
				{
					next = true;
					break;
				}
			}
			if(next || neutralCount / 2 > strengths.size() && strengths.size() > 0)	//No more than half of things listed as strengths should have been neutral.
				continue;
			neutralCount = 0;
			for(int i = 0; i < resists.size(); i++)
			{
				Type resist = resists.get(i);
				double effectiveness = SQLHandler.queryDamage(type, resist);
				if(effectiveness == 1)
					neutralCount++;
				else if(effectiveness > 1)
				{
					next = true;
					break;
				}
			}
			if(next || neutralCount / 2 > resists.size() && resists.size() > 0)
				continue;
			typeCombos.add(type);
			System.out.println("Potential HP Type: "+type.name());
		}
		if(typeCombos.size() == 1)
			setType(typeCombos.get(0));
		else
			possibles = typeCombos;
	}
}
