package geniusectsim.abilities;

import geniusectsim.battle.Weather;

/**
 * @author Jay
 *
 */
public class AbilityDrizzle extends Ability 
{
	public AbilityDrizzle()
	{
		rating = 5;
	}
	public void onSendOut()
	{
		if(!user.isAlive())
			return;
		System.out.println("Changing weather.");
		Weather goAway = Weather.Rain;
		goAway.setDuration(-1);
		battle.setWeather(goAway);
	}
}
