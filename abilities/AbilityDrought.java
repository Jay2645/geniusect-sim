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
		if(!user.isAlive())
			return;
		System.out.println("Changing weather.");
		Weather sunshine = Weather.Sun;
		sunshine.setDuration(-1);
		battle.setWeather(sunshine);
	}
}
