package geniusectsim.abilities;

import geniusectsim.battle.Weather;

/**
 * @author Jay
 *
 */
public class AbilitySandStream extends Ability 
{
	public AbilitySandStream()
	{
		rating = 5;
	}
	public void onSendOut()
	{
		if(!user.isAlive())
			return;
		System.out.println("Changing weather.");
		Weather sand = Weather.Sandstorm;
		sand.setDuration(-1);
		battle.setWeather(sand);
	}
}
