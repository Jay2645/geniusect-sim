package geniusectsim;

import geniusectsim.actions.Action;
import geniusectsim.actions.Attack;
import geniusectsim.actions.Change;
import geniusectsim.battle.Battle;
import geniusectsim.battle.Team;
import geniusectsim.bridge.Simulator;
import geniusectsim.moves.Move;
import geniusectsim.pokemon.Pokemon;
import geniusectsim.pokemon.Stat;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * @author Jay
 *
 */
public class SimulatorGUI extends JApplet implements ActionListener 
{
	Battle battle;
	Action toDo;
	Pokemon active = null;
	Pokemon enemy = null;
	boolean showdownActive = false;
	Container content;
	ActionListener pokeListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			Team user = battle.getTeam(teamID, false);
			Pokemon switchTo = user.getPokemon(((JButton)e.getSource()).getText());
			Change change = new Change();
			change.changeTo(switchTo);
			toDo = Simulator.newTurn(change);
			Pokemon active = user.getActive();
			if(active == null)
			{
				System.out.println("None");
				activePokemon.setText("null");
			}
			else
			{
				System.out.println(active.getName());
				activePokemon.setText(active.getName());
			}
			swapSides();
			populateMoveButtons();
			populatePokemonButtons();
			//startGUI();
		}
	};
	ActionListener moveListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			Move move = new Move(((JButton)e.getSource()).getText(),active, false);
			Attack attack = new Attack();
			attack.setMove(move, active, enemy, battle);
			toDo = Simulator.newTurn(attack);
			swapSides();
			populateMoveButtons();
			populatePokemonButtons();
			//startGUI();
		}
	};
	ActionListener showdownListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			toggleShowdown();
		}
	};
	int teamID = 0;
	JButton[] moveButtons = new JButton[4];
	JButton[] pokeButtons = new JButton[6];
	JLabel activePokemon = new JLabel("None");
	JLabel activeEnemy = new JLabel("None");
	JLabel[] usStatLabels = {	new JLabel("0"),new JLabel("0"),new JLabel("0"),
								new JLabel("0"),new JLabel("0"),new JLabel("0")};
	JLabel[] enemyStatLabels = {new JLabel("0"),new JLabel("0"),new JLabel("0"),
								new JLabel("0"),new JLabel("0"),new JLabel("0")};
	JLabel turnStats = new JLabel("Turn 0, Team 0");
	JTextArea activeText = new JTextArea();
	JTextArea enemyText = new JTextArea();
	String[] moves = {"Move 1", "Move 2", "Move 3", "Move 4"};
	String[] pokemon = {"Pokemon 1", "Pokemon 2", "Pokemon 3", "Pokemon 4", "Pokemon 5", "Pokemon 6"};
	int[] usStats = {0,0,0,0,0,0};
	int[] enemyStats = {0,0,0,0,0,0};
	JButton launchShowdown = new JButton("Launch Showdown");
	
	
	
	public void init()
	{
		startBattle();
		startGUI();
	}
	
	private void toggleShowdown()
	{
		Simulator.isLocal(showdownActive);
		showdownActive = !showdownActive;	//Yes, after we do the above.
		startBattle();
	}
	
	private void startBattle()
	{
		try
		{
			battle = Simulator.onNewBattle();
		}
		catch(Exception e)
		{
			toggleShowdown();
			System.err.println(e.getMessage());
		}
		toDo = Simulator.battleStartAction();
		populateMoveButtons();
		populatePokemonButtons();
	}
	
	private void startGUI()
	{
		content = getContentPane();
		content.setBackground(Color.white);
		content.setLayout(new BorderLayout());
		JPanel movePanel = new JPanel();
		JPanel pokemonPanel = new JPanel();
		JPanel infoPanel = new JPanel();
		JPanel usPanel = new JPanel();
		JPanel enemyPanel = new JPanel();
		JPanel usStatPanel = new JPanel();
		JPanel enemyStatPanel = new JPanel();
		infoPanel.setLayout(new BorderLayout());
		usPanel.setLayout(new BorderLayout());
		enemyPanel.setLayout(new BorderLayout());
		usStatPanel.setLayout(new GridLayout(2, 6));
		enemyStatPanel.setLayout(new GridLayout(2, 6));
		for(int i = 0; i < 6; i++)
		{
			usStatPanel.add(new JLabel(Stat.fromInt(i)+":"));
			usStatPanel.add(usStatLabels[i]);
		}
		for(int i = 0; i < 6; i++)
		{
			enemyStatPanel.add(new JLabel(Stat.fromInt(i)+":"));
			enemyStatPanel.add(enemyStatLabels[i]);
		}
		usPanel.add(activePokemon, BorderLayout.NORTH);
		usPanel.add(activeText, BorderLayout.WEST);
		usPanel.add(usStatPanel, BorderLayout.EAST);
		enemyPanel.add(activeEnemy, BorderLayout.NORTH);
		enemyPanel.add(enemyText, BorderLayout.WEST);
		enemyPanel.add(enemyStatPanel, BorderLayout.EAST);
		infoPanel.add(turnStats, BorderLayout.NORTH);
		infoPanel.add(usPanel, BorderLayout.WEST);
		infoPanel.add(enemyPanel, BorderLayout.EAST);
		content.add(infoPanel, BorderLayout.NORTH);
		movePanel.setLayout(new GridLayout(2,2));
		pokemonPanel.setLayout(new GridLayout(2,3));
		for(int i = 0; i < 4; i++)
			moveButtons[i].addActionListener(moveListener);
		for(int i = 0; i < 6; i++)
			pokeButtons[i].addActionListener(pokeListener);
		movePanel.add(moveButtons[0]);
		movePanel.add(moveButtons[1]);
		movePanel.add(moveButtons[2]);
		movePanel.add(moveButtons[3]);
		pokemonPanel.add(pokeButtons[0]);
		pokemonPanel.add(pokeButtons[1]);
		pokemonPanel.add(pokeButtons[2]);
		pokemonPanel.add(pokeButtons[3]);
		pokemonPanel.add(pokeButtons[4]);
		pokemonPanel.add(pokeButtons[5]);
		content.add(movePanel, BorderLayout.WEST);
		content.add(pokemonPanel, BorderLayout.EAST);
		launchShowdown.addActionListener(showdownListener);
		content.add(launchShowdown, BorderLayout.SOUTH);
	}
	
	 public void actionPerformed(ActionEvent e) {}
	
	private void populateMoveButtons()
	{
		for(int i = 0; i < 6; i++)
		{
			enemyStatLabels[i].setText(enemyStats[i]+"");
			usStatLabels[i].setText(usStats[i]+"");
		}
		if(toDo instanceof Change)
		{
			for(int i = 0; i < moveButtons.length; i++)
			{
				moveButtons[i] = new JButton("Move "+(i+1));
				moveButtons[i].setEnabled(false);
			}
			return;
		}
		if(active == null)
		{
			for(int i = 0; i < moveButtons.length; i++)
			{
				moveButtons[i].setText("Move "+(i+1));
				moveButtons[i].setEnabled(false);
			}
			return;
		}
		usStats = active.getBoostedStats();
		for(int i = 0; i < 6; i++)
			usStatLabels[i].setText(usStats[i]+"");
		Move[] moveset = active.getMoveset();
		for(int i = 0; i < moves.length; i++)
		{
			if(moveset[i] != null && !moveset[i].disabled)
			{
				moveButtons[i].setEnabled(true);
				moves[i] = moveset[i].name;
				moveButtons[i].setText(moves[i]);
			}
			else
				moveButtons[i].setEnabled(false);
		}
	}
	
	private void swapSides()
	{
		int enemyID = 1;
		if(showdownActive)
			teamID = 0;
		else
		{
			enemyID = teamID;
			if(teamID == 0)
				teamID = 1;
			else
				teamID = 0;
		}
		Team user = battle.getTeam(teamID, false);
		active = user.getActive();
		Team enemyTeam = battle.getTeam(enemyID, false);
		enemy = enemyTeam.getActive();
		if(active == null)
		{
			activeText.setText("No Pokemon.");
			activePokemon.setText("None");
			for(int i = 0; i < 6; i++)
				usStats[i] = 0;
		}
		else
		{
			activeText.setText(active.getImportable());
			activePokemon.setText(active.getName()+" ("+active.getHealth()+"% HP)");
			usStats = active.getBoostedStats();
		}
		if(enemy == null)
		{
			enemyText.setText("No Enemy.");
			activeEnemy.setText("None");
			for(int i = 0; i < 6; i++)
				enemyStats[i] = 0;
		}
		else
		{
			enemyText.setText(enemy.getImportable());
			activeEnemy.setText(enemy.getName()+" ("+enemy.getHealth()+"% HP)");
			enemyStats = enemy.getBoostedStats();
		}
		turnStats.setText("Turn "+battle.getTurnCount()+", Team "+teamID);
	}
	
	private void populatePokemonButtons()
	{
		Team user = battle.getTeam(teamID, false);
		Pokemon[] team = user.getPokemonTeam();
		for(int i = 0; i < pokemon.length; i++)
		{
			if(team[i] != null)
			{
				pokemon[i] = team[i].getName();
				if(pokeButtons[i] == null)
					pokeButtons[i] = new JButton(pokemon[i]);
				else
					pokeButtons[i].setText(pokemon[i]);
				if(team[i].isAlive() && team[i] != active)
					pokeButtons[i].setEnabled(true);
				else
					pokeButtons[i].setEnabled(false);
			}
		}
	}
}
