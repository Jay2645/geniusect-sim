package geniusectsim.abilities;

import geniusectsim.battle.Weather;

/**
 * @author Jay
 *
 */
public class AbilitySnowWarning extends Ability 
{
	public AbilitySnowWarning()
	{
		rating = 4.5;
	}
	public void onSendOut()
	{
		if(!user.isAlive())
			return;
		System.out.println("Changing weather.");
		Weather hail = Weather.Hail;
		hail.setDuration(-1);
		battle.setWeather(hail);
	}
}
