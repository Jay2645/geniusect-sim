package geniusectsim.abilities;

import geniusectsim.battle.Weather;
import geniusectsim.pokemon.Stat;

/**
 * @author Jay
 *
 */
public class AbilityChlorophyll extends Ability
{
	public AbilityChlorophyll()
	{
		rating = 3.5;
	}
	
	private Weather weather;
	private int initialSpeed;
	private int newSpeed = 0;
	
	public void onSendOut()
	{
		weather = battle.getWeather();
		doubleSpeed();
	}
	
	public void onNewTurn()
	{
		doubleSpeed();
	}
	
	public void onWithdraw()
	{
		user.setStat(Stat.Spe, initialSpeed);
		newSpeed = 0;
	}
	
	private void doubleSpeed()
	{
		if(newSpeed == 0)
		{
			initialSpeed = user.getStats(Stat.Spe);
			newSpeed = initialSpeed * 2;
		}
		if(weather == Weather.Sun)
		{
			user.setStat(Stat.Spe, newSpeed);
		}
		else
		{
			user.setStat(Stat.Spe, initialSpeed);
			newSpeed = 0;
		}
	}
}
