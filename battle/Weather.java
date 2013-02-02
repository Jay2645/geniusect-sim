package geniusectsim.battle;

public enum Weather {
	Rain, Hail, Sandstorm, Sun, None;
	private int duration = 0; //-1: Until the weather is changed.
	
	public void setDuration(int turnNumber)
	{
		duration = turnNumber;
	}
	
	public Weather onNewTurn()
	{
		if(this == Weather.None)
			return this;
		duration--;
		if(duration == 0)
			return Weather.None;
		else return this;
	}
}
