package geniusectsim.abilities;

import geniusectsim.battle.Weather;

/**
 * A class for the "Drought" ability.
 * @author Jay
 *
 */
public class AbilityDrought extends Ability
{
	public AbilityDrought()
	{
		rating = 5;
	}
	public void onSendOut()
	{
		System.out.println("Changing weather.");
		battle.setWeather(Weather.Sun);
	}
}
