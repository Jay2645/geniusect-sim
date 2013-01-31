
package geniusectsim.abilities;

import geniusectsim.battle.Weather;

/**
 * @author Team Forretress
 * The ability subclass for when the abilities "Airlock" or "Cloud Nine" are in play.
 *
 */
public class AbilityRemoveWeather extends Ability {
	public AbilityRemoveWeather()
	{
		rating = 3;
	}
	private Weather weather;
	
	public void onSendOut()
	{
		battle.setWeather(Weather.None);
	}
	
	public void onWithdraw()
	{
		battle.setWeather(weather);
	}
}
