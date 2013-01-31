package geniusectsim.battle;

/**
 * @author Jay
 *
 */
public enum EntryHazard 
{
	Spikes,ToxicSpikes, StealthRock;
	private int level = 0;
	
	public void addLevel()
	{
		level++;
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public void clearLevel()
	{
		level = 0;
	}
}