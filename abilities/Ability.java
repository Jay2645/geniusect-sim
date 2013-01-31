/*
 * A Pokemon's ability.
 * Eventually will give proper immunities (Levitiate, Wonder Guard) or give boosts to weather (Rain, Sand, etc.)
 * Right now is just placeholder.
 * @author TeamForretress
 */

package geniusectsim.abilities;

import java.util.HashMap;
import java.util.Map;

import geniusectsim.battle.Battle;
import geniusectsim.battle.Damage;
import geniusectsim.moves.Move;
import geniusectsim.pokemon.Pokemon;

public class Ability {
	public Ability() {}
	
	public Ability(String n, Pokemon u)
	{
		name = n;
		user = u;
		battle = user.getTeam().getBattle();
		initializeDictionary();
		n = n.toLowerCase();
		if(abilityDex.containsKey(n))
		{
			Ability able = abilityDex.get(n);
			able.name = name;
			able.setUser(u);
			able.setBattle(battle);
			ability = able;
			u.setAbility(able);
			System.err.println(u.getName()+" has ability "+able.getName());
		}
	}
	protected String name;
	protected Pokemon user;
	protected double rating;
	protected double stab = 1.5;
	protected double powerBoost = 1;
	protected Damage onFaintDamage;
	protected Battle battle;
	protected Ability ability;
	protected static Map<String, Ability> abilityDex;
	
	/**
	 * Gets this ability's name.
	 * @return String - our name.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Sets the battle to the specified battle.
	 * @param b - The Battle to set it to.
	 */
	public void setBattle(Battle b)
	{
		battle = b;
		if(ability != null)
			ability.setBattle(b);
	}
	
	/*

	Ratings and how they work:

	-2: Extremely detrimental
		  The sort of ability that relegates Pokemon with Uber-level BSTs
		  into NU.
		ex. Slow Start, Truant

	-1: Detrimental
		  An ability that does more harm than good.
		ex. Defeatist, Klutz

	 0: Useless
		  An ability with no net effect on a Pokemon during a battle.
		ex. Pickup, Illuminate

	 1: Ineffective
		  An ability that has a minimal effect. Should never be chosen over
		  any other ability.
		ex. Pressure, Damp

	 2: Situationally useful
		  An ability that can be useful in certain situations.
		ex. Blaze, Insomnia

	 3: Useful
		  An ability that is generally useful.
		ex. Volt Absorb, Iron Fist

	 4: Very useful
		  One of the most popular abilities. The difference between 3 and 4
		  can be ambiguous.
		ex. Technician, Intimidate

	 5: Essential
		  The sort of ability that defines metagames.
		ex. Drizzle, Magnet Pull

	*/

	public void onSendOut()
	{
		if(ability != null)
			ability.onSendOut();
	}
	
	public void onFaint(Pokemon attacker, Move move)
	{
		if(ability != null)
			ability.onFaint(attacker, move);	
	}
	
	public void onMoveUsed(Pokemon attacker, Move move)
	{
		if(ability != null)
			ability.onMoveUsed(attacker, move);
	}
	
	public void onNewTurn()
	{
		if(ability != null)
			ability.onNewTurn();
	}
	
	public void onWithdraw()
	{
		if(ability != null)
			ability.onWithdraw();
	}
	
	public double getSTAB()
	{
		return stab;
	}
	
	public void setUser(Pokemon u)
	{
		user = u;
		if(ability != null)
			ability.setUser(u);
	}

	/**
	 * @return
	 */
	public double getModifier() 
	{
		return powerBoost;
	}
	
	
	public static void initializeDictionary()
	{
		if(abilityDex != null)
			return;
		abilityDex = new HashMap<String, Ability>();
		abilityDex.put("adaptability", new AbilityAdaptability());
		abilityDex.put("aftermath", new AbilityAftermath());
		abilityDex.put("airlock", new AbilityRemoveWeather());
		abilityDex.put("cloud nine", new AbilityRemoveWeather());
		abilityDex.put("chlorophyll", new AbilityChlorophyll());
		abilityDex.put("levitate", new AbilityLevitate());
		abilityDex.put("flash fire", new AbilityFlashFire());
		abilityDex.put("drought", new AbilityDrought());
		abilityDex.put("magic bounce", new AbilityMagicBounce());
	}
	/*
	{
		"analytic": {
			shortDesc: "This Pokemon's attacks do 1.3x damage if it is the last to move in a turn.",
			onBasePower: function(basePower, attacker, defender, move) {
				if (!this.willMove(defender)) {
					this.debug('Analytic boost');
					return basePower * 1.3;
				}
			},
			rating = 1,
		},
		"angerpoint": {
			shortDesc: "If this Pokemon (not a Substitute) is hit by a critical hit, its Attack is boosted by 12.",
			onCriticalHit: function(target) {
				if (!target.volatiles['substitute']) {
					target.setBoost({atk: 6});
					this.add('-setboost',target,'atk',12,'[from] ability: Anger Point');
				}
			},
			rating = 2,
		},
		"anticipation": {
			shortDesc: "On switch-in, this Pokemon shudders if any foe has a super effective or OHKO move.",
			onStart: function(pokemon) {
				var targets = pokemon.side.foe.active;
				for (var i=0; i<targets.length; i++) {
					if (targets[i].fainted) return;
					for (var j=0; j<targets[i].moveset.length; j++) {
						var move = this.getMove(targets[i].moveset[j].move);
						if (move.category !== 'Status' && (this.getEffectiveness(move.type, pokemon) > 0 || move.ohko)) {
							this.add('-activate', pokemon, 'Anticipation');
							return;
						}
					}
				}
			},
			rating = 1,
		},
		"arenatrap": {
			shortDesc: "Prevents foes from switching out normally unless they have immunity to Ground.",
			onFoeModifyPokemon: function(pokemon) {
				if (pokemon.runImmunity('Ground', false)) {
					pokemon.trapped = true;
				}
			},
			rating = 5,
		},
		"baddreams": {
			shortDesc: "Causes sleeping adjacent foes to lose 1/8 of their max HP at the end of each turn.",
			onResidualOrder: 26,
			onResidualSubOrder: 1,
			onResidual: function(pokemon) {
				for (var i=0; i<pokemon.side.foe.active.length; i++) {
					var target = pokemon.side.foe.active[i];
					if (target.status === 'slp') {
						this.damage(target.maxhp/8, target);
					}
				}
			},
			rating = 2,
		},
		"battlearmor": {
			shortDesc: "This Pokemon cannot be struck by a critical hit.",
			onCriticalHit: false,
			rating = 1,
		},
		"bigpecks": {
			shortDesc: "Prevents other Pokemon from lowering this Pokemon's Defense.",
			onBoost: function(boost, target, user) {
				if (user&& target === user) return;
				if (boost['def'] && boost['def'] < 0) {
					boost['def'] = 0;
					this.add("-message", target.name+"'s Defense was not lowered! (placeholder)");
				}
			},
			rating = 1,
		},
		"blaze": {
			shortDesc: "When this Pokemon has 1/3 or less of its max HP, its Fire attacks do 1.5x damage.",
			onBasePower: function(basePower, attacker, defender, move) {
				if (move.type === 'Fire' && attacker.hp <= attacker.maxhp/3) {
					this.debug('Blaze boost');
					return basePower * 1.5;
				}
			},
			rating = 2,
		},
		"clearbody": {
			shortDesc: "Prevents other Pokemon from lowering this Pokemon's stat stages.",
			onBoost: function(boost, target, user) {
				if (user&& target === user) return;
				for (var i in boost) {
					if (boost[i] < 0) {
						delete boost[i];
						this.add("-message", target.name+"'s stats were not lowered! (placeholder)");
					}
				}
			},
			rating = 2,
		},
		"colorchange": {
			shortDesc: "This Pokemon's type changes to match the type of the last move that hit it.",
			onAfterMoveSecondary: function(target, user, effect) {
				if (effect && effect.effectType === 'Move' && effect.category !== 'Status') {
					target.addVolatile('colorchange', user, effect);
				}
			},
			effect: {
				onStart: function(target, user, effect) {
					this.effectData.type = 'Normal';
					if (effect && effect.type && effect.type !== 'Normal') {
						this.add('-start', target, 'typechange', effect.type, '[from] Color Change');
						this.effectData.type = effect.type;
					} else {
						return false;
					}
				},
				onRestart: function(target, user, effect) {
					if (effect && effect.type && effect.type !== this.effectData.type) {
						this.add('-start', target, 'typechange', effect.type, '[from] Color Change');
						this.effectData.type = effect.type;
					}
				},
				onModifyPokemon: function(target) {
					if (!this.effectData.type) this.effectData.type = 'Normal';
					target.types = [this.effectData.type];
				}
			},
			rating = 2,
		},
		"compoundeyes": {
			shortDesc: "This Pokemon's moves have their Accuracy boosted to 1.3x.",
			onModifyMove: function(move) {
				if (typeof move.accuracy !== 'number') return;
				this.debug('compoundeyes - enhancing accuracy');
				move.accuracy *= 1.3;
			},
			rating = 3.5,
		},
		"contrary": {
			shortDesc: "If this Pokemon has a stat boosted it is lowered instead, and vice versa.",
			onBoost: function(boost) {
				for (var i in boost) {
					boost[i] *= -1;
				}
			},
			rating = 4,
		},
		"cursedbody": {
			shortDesc: "If this Pokemon is hit by an attack, there is a 30% chance that move gets Disabled.",
			onAfterDamage: function(damage, target, user, move) {
				if (!user|| user.volatiles['disable']) return;
				if (user!== target) {
					if (this.random(10) < 3) {
						user.addVolatile('disable');
					}
				}
			},
			rating = 2,
		},
		"cutecharm": {
			shortDesc: "30% chance of infatuating Pokemon of the opposite gender if they make contact.",
			onAfterDamage: function(damage, target, user, move) {
				if (move && move.isContact) {
					if (this.random(10) < 3) {
						if (user.addVolatile('attract', target)) {
							this.add('-start', user, 'Attract', '[from] Cute Charm', '[of] '+target);
						}
					}
				}
			},
			rating = 2,
		},
		"damp": {
			shortDesc: "While this Pokemon is active, Selfdestruct, Explosion, and Aftermath do not work.",
			id: "damp",
			onAnyTryHit: function(target, user, effect) {
				if (effect.id === 'selfdestruct' || effect.id === 'explosion') {
					return false;
				}
			},
			onAnyDamage: function(damage, target, user, effect) {
				if (effect && effect.id === 'aftermath') {
					return false;
				}
			},
			rating = 1,
		},
		"defeatist": {
			shortDesc: "When this Pokemon has 1/2 or less of its max HP, its Attack and Sp. Atk are halved.",
			onModifyStats: function(stats, pokemon) {
				if (pokemon.hp < pokemon.maxhp/2) {
					stats.atk /= 2;
					stats.spa /= 2;
				}
			},
			onResidual: function(pokemon) {
				pokemon.update();
			},
			rating = -1,
		},
		"defiant": {
			shortDesc: "This Pokemon's Attack is boosted by 2 for each of its stats that is lowered by a foe.",
			onAfterEachBoost: function(boost, target, user) {
				if (!user|| target === user) {
					return;
				}
				var statsLowered = false;
				for (var i in boost) {
					if (boost[i] < 0) {
						statsLowered = true;
					}
				}
				if (statsLowered) {
					this.boost({atk: 2});
				}
			},
			rating = 2,
		},
		"download": {
			shortDesc: "On switch-in, Attack or Sp. Atk is boosted by 1 based on the foes' weaker Defense.",
			onStart: function (pokemon) {
				var foeactive = pokemon.side.foe.active;
				var totaldef = 0;
				var totalspd = 0;
				for (var i=0; i<foeactive.length; i++) {
					if (!foeactive[i]) continue;
					totaldef += foeactive[i].stats.def;
					totalspd += foeactive[i].stats.spd;
				}
				if (totaldef >= totalspd) {
					this.boost({spa:1});
				} else {
					this.boost({atk:1});
				}
			},
			rating = 4,
		},
		"drizzle": {
			shortDesc: "On switch-in, this Pokemon summons Rain Dance until another weather replaces it.",
			onStart: function(user) {
				this.setWeather('raindance');
				this.weatherData.duration = 0;
			},
			rating = 5,
		},
		"drought"
		"dryskin": {
			shortDesc: "This Pokemon is healed 1/4 by Water, 1/8 by Rain; is hurt 1.25x by Fire, 1/8 by Sun.",
			onImmunity: function(type, pokemon) {
				if (type === 'Water') {
					this.heal(pokemon.maxhp/4);
					return null;
				}
			},
			onFoeBasePower: function(basePower, attacker, defender, move) {
				if (move.type === 'Fire') {
					return basePower * 5/4;
				}
			},
			onWeather: function(target, user, effect) {
				if (effect.id === 'raindance') {
					this.heal(target.maxhp/8);
				} else if (effect.id === 'sunnyday') {
					this.damage(target.maxhp/8);
				}
			},
			rating = 3,
		},
		"earlybird": {
			shortDesc: "This Pokemon's sleep status lasts half as long as usual, self-induced or not.",
			isHalfSleep: true,
			rating = 2.5,
		},
		"effectspore": {
			shortDesc: "30% chance of poisoning, paralyzing, or causing sleep on Pokemon making contact.",
			onAfterDamage: function(damage, target, user, move) {
				if (move && move.isContact && !user.status) {
					var r = this.random(100);
					if (r < 11) user.setStatus('slp');
					else if (r < 21) user.setStatus('par');
					else if (r < 30) user.setStatus('psn');
				}
			},
			rating = 2,
		},
		"filter": {
			shortDesc: "This Pokemon receives 3/4 damage from super effective attacks.",
			onFoeBasePower: function(basePower, attacker, defender, move) {
				if (this.getEffectiveness(move.type, defender) > 0) {
					this.debug('Filter neutralize');
					return basePower * 3/4;
				}
			},
			rating = 3,
		},
		"flamebody": {
			shortDesc: "30% chance of burning a Pokemon making contact with this Pokemon.",
			onAfterDamage: function(damage, target, user, move) {
				if (move && move.isContact) {
					if (this.random(10) < 3) {
						user.trySetStatus('brn', target, move);
					}
				}
			},
			rating = 2,
		},
		"flareboost": {
			shortDesc: "When this Pokemon is burned, its special attacks do 1.5x damage.",
			onModifyStats: function(stats, pokemon) {
				if (pokemon.status === 'brn') {
					stats.spa *= 1.5;
				}
			},
			rating = 3,
		},
		"flowergift": {
			shortDesc: "If user is Cherrim and Sunny Day is active, it and allies' Attack and Sp. Def are 1.5x.",
			onStart: function(pokemon) {
				delete this.effectData.forme;
			},
			onModifyStats: function(stats, pokemon) {
				if (this.weather === 'sunnyday') {
					stats.atk *= 1.5;
					stats.spd *= 1.5;
					if (pokemon.isActive && pokemon.speciesid === 'cherrim' && this.effectData.forme !== 'Sunny') {
						this.effectData.forme = 'Sunny';
						this.add('-formechange', pokemon, 'Cherrim-Sunny');
						this.add('-message', pokemon.name+' transformed! (placeholder)');
					}
				} else if (pokemon.isActive && pokemon.speciesid === 'cherrim' && this.effectData.forme) {
					delete this.effectData.forme;
					this.add('-formechange', pokemon, 'Cherrim');
					this.add('-message', pokemon.name+' transformed! (placeholder)');
				}
			},
			rating = 3,
		},
		"forecast": {
			shortDesc: "Castform's type changes to the current weather condition's type, except Sandstorm.",
			onModifyPokemon: function(pokemon) {
				if (pokemon.baseTemplate.species !== 'Castform' || pokemon.transformed) return;
				var forme = null;
				switch (this.weather) {
				case 'sunnyday':
					if (pokemon.template.speciesid !== 'castformsunny') forme = 'Castform-Sunny';
					break;
				case 'raindance':
					if (pokemon.template.speciesid !== 'castformrainy') forme = 'Castform-Rainy';
					break;
				case 'hail':
					if (pokemon.template.speciesid !== 'castformsnowy') forme = 'Castform-Snowy';
					break;
				default:
					if (pokemon.template.speciesid !== 'castform') forme = 'Castform';
					break;
				}
				if (pokemon.isActive && forme) {
					pokemon.transformInto(forme);
					pokemon.transformed = false;
					this.add('-formechange', pokemon, forme);
					this.add('-message', pokemon.name+' transformed! (placeholder)');
				}
			},
			rating = 4,
		},
		"forewarn": {
			shortDesc: "On switch-in, this Pokemon is alerted to the foes' move with the highest base power.",
			onStart: function(pokemon) {
				var targets = pokemon.side.foe.active;
				var warnMoves = [];
				var warnBp = 1;
				for (var i=0; i<targets.length; i++) {
					if (targets[i].fainted) continue;
					for (var j=0; j<targets[i].moveset.length; j++) {
						var move = this.getMove(targets[i].moveset[j].move);
						var bp = move.basePower;
						if (move.ohko) bp = 160;
						if (move.id === 'counter' || move.id === 'metalburst' || move.id === 'mirrorcoat') bp = 120;
						if (!bp && move.category !== 'Status') bp = 80;
						if (bp > warnBp) {
							warnMoves = [[move, targets[i]]];
							warnBp = bp;
						} else if (bp == warnBp) {
							warnMoves.push([move, targets[i]]);
						}
					}
				}
				if (!warnMoves.length) return;
				var warnMove = warnMoves[this.random(warnMoves.length)];
				this.add('-activate', pokemon, 'ability: Forewarn', warnMove[0]);
			},
			rating = 1,
		},
		"friendguard": {
			shortDesc: "This Pokemon's allies receive 3/4 damage from other Pokemon's attacks.",
			rating = 0,
		},
		"frisk": {
			shortDesc: "On switch-in, this Pokemon identifies a random foe's held item.",
			onStart: function(pokemon) {
				var target = pokemon.side.foe.randomActive();
				if (target && target.item) {
					this.add('-item', target, target.getItem().name, '[from] ability: Frisk', '[of] '+pokemon);
				}
			},
			rating = 1.5,
		},
		"gluttony": {
			shortDesc: "When this Pokemon has 1/2 or less of its max HP, it uses certain Berries early.",
			rating = 1.5,
		},
		"guts": {
			shortDesc: "If this Pokemon is statused, its Attack is 1.5x; burn's Attack drop is ignored.",
			onModifyStats: function(stats, pokemon) {
				if (pokemon.status) {
					stats.atk *= 1.5;
				}
			},
			rating = 4,
		},
		"harvest": {
			shortDesc: "50% chance this Pokemon's Berry is restored at the end of each turn. 100% in Sun.",
			onResidualOrder: 26,
			onResidualSubOrder: 1,
			onResidual: function(pokemon) {
				if ((this.weather === 'sunnyday') || (this.random(2) === 0)) {
					if (!pokemon.item && this.getItem(pokemon.lastItem).isBerry) {
							pokemon.setItem(pokemon.lastItem);
							this.add("-item", pokemon, pokemon.item, '[from] ability: Harvest');
					}
				}
			},
			rating = 4,
		},
		"healer": {
			shortDesc: "30% chance of curing an adjacent ally's status at the end of each turn.",
			onResidualOrder: 5,
			onResidualSubOrder: 1,
			rating = 0,
		},
		"heatproof": {
			shortDesc: "This Pokemon receives half damage from Fire-type attacks and burn damage.",
			onSourceBasePower: function(basePower, attacker, defender, move) {
				if (move.type === 'Fire') {
					return basePower / 2;
				}
			},
			onDamage: function(damage, attacker, defender, effect) {
				if (effect && effect.id === 'brn') {
					return damage / 2;
				}
			},
			rating = 2.5,
		},
		"heavymetal": {
			shortDesc: "This Pokemon's weight is doubled.",
			onModifyPokemon: function(pokemon) {
				pokemon.weightkg *= 2;
			},
			rating = 0,
		},
		"honeygather": {
			rating = 0,
		},
		"hugepower": {
			shortDesc: "This Pokemon's Attack is doubled.",
			onModifyStats: function(stats) {
				stats.atk *= 2;
			},
			rating = 5,
		},
		"hustle": {
			shortDesc: "This Pokemon's Attack is 1.5x and Accuracy of its physical attacks is 0.8x.",
			onModifyStats: function(stats) {
				stats.atk *= 1.5;
			},
			onModifyMove: function(move) {
				if (move.category === 'Physical' && typeof move.accuracy === 'number') {
					move.accuracy *= 0.8;
				}
			},
			rating = 3,
		},
		"hydration": {
			shortDesc: "This Pokemon has its status cured at the end of each turn if Rain Dance is active.",
			onResidualOrder: 5,
			onResidualSubOrder: 1,
			onResidual: function(pokemon) {
				if (pokemon.status && this.weather === 'raindance') {
					this.debug('hydration');
					pokemon.cureStatus();
				}
			},
			rating = 4,
		},
		"hypercutter": {
			shortDesc: "Prevents other Pokemon from lowering this Pokemon's Attack.",
			onBoost: function(boost, target, user) {
				if (user&& target === user) return;
				if (boost['atk'] && boost['atk'] < 0) {
					boost['atk'] = 0;
					this.add("-message", target.name+"'s Attack was not lowered! (placeholder)");
				}
			},
			rating = 2,
		},
		"icebody": {
			shortDesc: "If Hail is active, this Pokemon heals 1/16 of its max HP each turn; immunity to Hail.",
			onWeather: function(target, user, effect) {
				if (effect.id === 'hail') {
					this.heal(target.maxhp/16);
				}
			},
			onImmunity: function(type, pokemon) {
				if (type === 'hail') return false;
			},
			rating = 3,
		},
		"illuminate": {
			rating = 0,
		},
		"illusion": {
			shortDesc: "This Pokemon appears as the last Pokemon in the party until it takes direct damage.",
			onBeforeSwitchIn: function(pokemon) {
				if (!pokemon.volatiles['illusion']) {
					var i;
					for (i=pokemon.side.pokemon.length-1; i>pokemon.position; i--) {
						if (!pokemon.side.pokemon[i]) continue;
						if (!pokemon.side.pokemon[i].fainted) break;
					}
					pokemon.illusion = pokemon.side.pokemon[i];
				}
			},
			onDamage: function(damage, pokemon, user, effect) {
				if (effect && effect.effectType === 'Move') {
					this.debug('illusion cleared');
					//pokemon.addVolatile('illusion');
					pokemon.setAbility('');
					pokemon.illusion = null;
					this.add('replace', pokemon, pokemon.getDetails());
				}
			},
			rating = 4.5,
		},
		"immunity": {
			shortDesc: "This Pokemon cannot be poisoned. Gaining this Ability while poisoned cures it.",
			onImmunity: function(type) {
				if (type === 'psn') return false;
			},
			rating = 1,
		},
		"imposter": {
			shortDesc: "On switch-in, this Pokemon copies the foe it's facing; stats, moves, types, Ability.",
			onStart: function(pokemon) {
				var target = pokemon.side.foe.randomActive();
				if (target && pokemon.transformInto(target)) {
					this.add('-transform', pokemon, target);
				}
			},
			rating = 5,
		},
		"infiltrator": {
			shortDesc: "This Pokemon's moves ignore the foe's Reflect, Light Screen, Safeguard, and Mist.",
			// Implemented in the corresponding effects.
			rating = 1,
		},
		"innerfocus": {
			shortDesc: "This Pokemon cannot be made to flinch.",
			onFlinch: false,
			rating = 1,
		},
		"insomnia": {
			shortDesc: "This Pokemon cannot fall asleep. Gaining this Ability while asleep cures it.",
			onImmunity: function(type, pokemon) {
				if (type === 'slp') return false;
			},
			rating = 2,
		},
		"intimidate": {
			shortDesc: "On switch-in, this Pokemon lowers adjacent foes' Attack by 1.",
			onStart: function(pokemon) {
				var foeactive = pokemon.side.foe.active;
				for (var i=0; i<foeactive.length; i++) {
					if (!foeactive[i]) continue;
					if (foeactive[i].volatiles['substitute']) {
						// does it give a message?
						this.add('-activate',foeactive[i],'Substitute','ability: Intimidate','[of] '+pokemon);
					} else {
						this.add('-ability',pokemon,'Intimidate','[of] '+foeactive[i]);
						this.boost({atk: -1}, foeactive[i], pokemon);
					}
				}
			},
			rating = 4,
		},
		"ironbarbs": {
			shortDesc: "This Pokemon causes other Pokemon making contact to lose 1/8 of their max HP.",
			onAfterDamage: function(damage, target, user, move) {
				if (user&& user!== target && move && move.isContact) {
					this.damage(user.maxhp/8, user, target);
				}
			},
			rating = 3,
		},
		"ironfist": {
			shortDesc: "This Pokemon's punch-based attacks do 1.2x damage. Sucker Punch is not boosted.",
			onBasePower: function(basePower, attacker, defender, move) {
				if (move.isPunchAttack) {
					this.debug('Iron Fist boost');
					return basePower * 12/10;
				}
			},
			rating = 3,
		},
		"justified": {
			shortDesc: "This Pokemon's Attack is boosted by 1 after it is damaged by a Dark-type attack.",
			onAfterDamage: function(damage, target, user, effect) {
				if (effect && effect.type === 'Dark') {
					this.boost({atk:1});
				}
			},
			rating = 2,
		},
		"keeneye": {
			shortDesc: "Prevents other Pokemon from lowering this Pokemon's Accuracy.",
			onBoost: function(boost, target, user) {
				if (user&& target === user) return;
				if (boost['accuracy'] && boost['accuracy'] < 0) {
					boost['accuracy'] = 0;
					this.add("-message", target.name+"'s accuracy was not lowered! (placeholder)");
				}
			},
			rating = 1,
		},
		"klutz": {
			shortDesc: "This Pokemon's held item has no effect, except Macho Brace. Fling cannot be used.",
			onModifyPokemon: function(pokemon) {
				pokemon.ignore['Item'] = true;
			},
			rating = 1.5,
		},
		"leafguard": {
			shortDesc: "If Sunny Day is active, this Pokemon cannot be statused, and Rest will fail for it.",
			onSetStatus: function(pokemon) {
				if (this.weather === 'sunnyday') {
					return false;
				}
			},
			onTryHit: function(target, user, move) {
				if (this.weather === 'sunnyday' && move && move.id === 'yawn') {
					return false;
				}
			},
			rating = 3,
		},
		"lightmetal": {
			shortDesc: "This Pokemon's weight is halved.",
			onModifyPokemon: function(pokemon) {
				pokemon.weightkg /= 2;
			},
			rating = 1,
		},
		"lightningrod": {
			shortDesc: "This Pokemon draws Electric moves to itself to boost Sp. Atk by 1; Electric immunity.",
			onImmunity: function(type, pokemon) {
				if (type === 'Electric') {
					this.boost({spa:1});
					return null;
				}
			},
			rating = 3,
		},
		"limber": {
			shortDesc: "This Pokemon cannot be paralyzed. Gaining this Ability while paralyzed cures it.",
			onImmunity: function(type, pokemon) {
				if (type === 'par') return false;
			},
			rating = 2,
		},
		"liquidooze": {
			shortDesc: "This Pokemon damages those draining HP from it for as much as they would heal.",
			id: "liquidooze",
			onSourceTryHeal: function(damage, target, user, effect) {
				this.debug("Heal is occurring: "+target+" <- "+user+" :: "+effect.id);
				var canOoze = {drain: 1, leechseed: 1};
				if (canOoze[effect.id]) {
					this.damage(damage);
					return 0;
				}
			},
			rating = 1,
		},
		"magicguard": {
			shortDesc: "This Pokemon can only be damaged by direct attacks.",
			onDamage: function(damage, target, user, effect) {
				if (effect.effectType !== 'Move') {
					return false;
				}
			},
			rating = 4.5,
		},
		"magmaarmor": {
			shortDesc: "This Pokemon cannot be frozen. Gaining this Ability while frozen cures it.",
			onImmunity: function(type, pokemon) {
				if (type === 'frz') return false;
			},
			rating = 0.5,
		},
		"magnetpull": {
			shortDesc: "Prevents Steel-type foes from switching out normally.",
			onFoeModifyPokemon: function(pokemon) {
				if (pokemon.hasType('Steel')) {
					pokemon.trapped = true;
				}
			},
			rating = 5,
		},
		"marvelscale": {
			shortDesc: "If this Pokemon is statused, its Defense is 1.5x.",
			onModifyStats: function(stats, pokemon) {
				if (pokemon.status) {
					stats.def *= 1.5;
				}
			},
			rating = 3,
		},
		"minus": {
			rating = 0,
		},
		"moldbreaker": {
			shortDesc: "This Pokemon's moves ignore the target's Ability if that Ability would modify damage.",
			onStart: function(pokemon) {
				this.add('-ability', pokemon, 'Mold Breaker');
			},
			onAllyModifyPokemonPriority: 100,
			onAllyModifyPokemon: function(pokemon) {
				if (this.activePokemon === this.effectData.target && pokemon !== this.activePokemon) {
					pokemon.ignore['Ability'] = 'A';
				}
			},
			onFoeModifyPokemonPriority: 100,
			onFoeModifyPokemon: function(pokemon) {
				if (this.activePokemon === this.effectData.target) {
					pokemon.ignore['Ability'] = 'A';
				}
			},
			rating = 3,
		},
		"moody": {
			shortDesc: "Boosts a random stat by 2 and lowers another stat by 1 at the end of each turn.",
			onResidualOrder: 26,
			onResidualSubOrder: 1,
			onResidual: function(pokemon) {
				var stats = [], i = '';
				var boost = {};
				for (var i in pokemon.boosts) {
					if (pokemon.boosts[i] < 6) {
						stats.push(i);
					}
				}
				if (stats.length) {
					i = stats[this.random(stats.length)];
					boost[i] = 2;
				}
				stats = [];
				for (var j in pokemon.boosts) {
					if (pokemon.boosts[j] > -6 && j !== i) {
						stats.push(j);
					}
				}
				if (stats.length) {
					i = stats[this.random(stats.length)];
					boost[i] = -1;
				}
				this.boost(boost);
			},
			rating = 5,
		},
		"motordrive": {
			shortDesc: "This Pokemon's Speed is boosted by 1 if hit by an Electric move; Electric immunity.",
			onImmunity: function(type, pokemon) {
				if (type === 'Electric') {
					this.boost({spe:1});
					return null;
				}
			},
			rating = 3,
		},
		"moxie": {
			shortDesc: "This Pokemon's Attack is boosted by 1 if it attacks and faints another Pokemon.",
			onSourceFaint: function(target, user, effect) {
				if (effect && effect.effectType === 'Move') {
					this.boost({atk:1}, user);
				}
			},
			rating = 4,
		},
		"multiscale": {
			shortDesc: "If this Pokemon is at full HP, it takes half damage from attacks.",
			onSourceBasePower: function(basePower, attacker, defender, move) {
				if (defender.hp >= defender.maxhp) {
					this.debug('Multiscale weaken');
					return basePower/2;
				}
			},
			rating = 4,
		},
		"multitype": {
			shortDesc: "If this Pokemon is Arceus, its type changes to match its held Plate.",
			onModifyPokemon: function(pokemon) {
				if (pokemon.baseTemplate.species !== 'Arceus') {
					return;
				}
				var type = this.runEvent('Plate', pokemon);
				if (type && type !== true) {
					pokemon.types = [type];
				}
			},
			onTakeItem: function(item) {
				if (item.onPlate) return false;
			},
			rating = 5,
		},
		"mummy": {
			shortDesc: "Pokemon making contact with this Pokemon have their Ability changed to Mummy.",
			onAfterDamage: function(damage, target, user, move) {
				if (user&& user!== target && move && move.isContact) {
					if (user.setAbility('mummy')) {
						this.add('-ability', user, 'Mummy', '[from] Mummy');
					}
				}
			},
			rating = 1,
		},
		"naturalcure": {
			shortDesc: "This Pokemon has its status cured when it switches out.",
			onSwitchOut: function(pokemon) {
				pokemon.setStatus('');
			},
			rating = 4,
		},
		"noguard": {
			shortDesc: "Every move used by or against this Pokemon will always hit.",
			onModifyMove: function(move) {
				move.accuracy = true;
				move.alwaysHit = true;
			},
			onSourceModifyMove: function(move) {
				move.accuracy = true;
				move.alwaysHit = true;
			},
			rating = 4.1,
		},
		"normalize": {
			shortDesc: "This Pokemon's moves all become Normal-typed.",
			onModifyMove: function(move) {
				if (move.id !== 'struggle') {
					move.type = 'Normal';
				}
			},
			rating = -1,
		},
		"oblivious": {
			shortDesc: "This Pokemon cannot be infatuated. Gaining this Ability while infatuated cures it.",
			onImmunity: function(type, pokemon) {
				if (type === 'attract') {
					this.add('-immune', pokemon, '[from] Oblivious');
					return false;
				}
			},
			onTryHit: function(pokemon, target, move) {
				if (move.id === 'captivate') {
					this.add('-immune', pokemon, '[msg]', '[from] Oblivious');
					return null;
				}
			},
			rating = 0.5,
		},
		"overcoat": {
			shortDesc: "This Pokemon does not take damage from Sandstorm or Hail.",
			onImmunity: function(type, pokemon) {
				if (type === 'sandstorm' || type === 'hail') return false;
			},
			rating = 1,
		},
		"overgrow": {
			shortDesc: "When this Pokemon has 1/3 or less of its max HP, its Grass attacks do 1.5x damage.",
			onBasePower: function(basePower, attacker, defender, move) {
				if (move.type === 'Grass' && attacker.hp <= attacker.maxhp/3) {
					this.debug('Overgrow boost');
					return basePower * 1.5;
				}
			},
			rating = 2,
		},
		"owntempo": {
			shortDesc: "This Pokemon cannot be confused. Gaining this Ability while confused cures it.",
			onImmunity: function(type, pokemon) {
				if (type === 'confusion') {
					this.add('-immune', pokemon, 'confusion');
					return false;
				}
			},
			rating = 1,
		},
		"pickup": {
			shortDesc: "If this Pokemon has no item, it finds one used by an adjacent Pokemon this turn.",
			onResidualOrder: 26,
			onResidualSubOrder: 1,
			onResidual: function(pokemon) {
				var foe = pokemon.side.foe.randomActive();
				if (!foe) return;
				if (!pokemon.item && foe.lastItem && foe.usedItemThisTurn && foe.lastItem !== 'airballoon' && foe.lastItem !== 'ejectbutton') {
					pokemon.setItem(foe.lastItem);
					foe.lastItem = '';
					var item = pokemon.getItem();
					this.add('-item', pokemon, item, '[from] Pickup');
					if (item.isBerry) pokemon.update();
				}
			},
			rating = 0,
		},
		"pickpocket": {
			shortDesc: "If this Pokemon has no item, it steals an item off a Pokemon making contact.",
			onAfterDamage: function(damage, target, user, move) {
				if (user&& user!== target && move && move.isContact) {
					if (target.item) {
						return;
					}
					var yourItem = user.takeItem(target);
					if (!yourItem) {
						return;
					}
					if (!target.setItem(yourItem)) {
						user.item = yourItem.id;
						return;
					}
					this.add('-item', target, yourItem, '[from] ability: Pickpocket');
				}
			},
			rating = 1,
		},
		"plus": {
			rating = 0,
		},
		"poisonheal": {
			shortDesc: "This Pokemon is healed by 1/8 of its max HP each turn when poisoned; no HP loss.",
			onDamage: function(damage, target, user, effect) {
				if (effect.id === 'psn' || effect.id === 'tox') {
					this.heal(target.maxhp/8);
					return false;
				}
			},
			rating = 4,
		},
		"poisonpoint": {
			shortDesc: "30% chance of poisoning a Pokemon making contact with this Pokemon.",
			onAfterDamage: function(damage, target, user, move) {
				if (move && move.isContact) {
					if (this.random(10) < 3) {
						user.trySetStatus('psn', target, move);
					}
				}
			},
			rating = 2,
		},
		"poisontouch": {
			shortDesc: "This Pokemon's contact moves have a 30% chance of poisoning.",
			// upokecenter says this is implemented as an added secondary effect
			onModifyMove: function(move) {
				if (!move || !move.isContact) return;
				if (!move.secondaries) {
					move.secondaries = [];
				}
				move.secondaries.push({
					chance: 30,
					status: 'psn'
				});
			},
			rating = 2,
		},
		"prankster": {
			shortDesc: "This Pokemon's non-damaging moves have their priority increased by 1.",
			onModifyPriority: function(priority, pokemon, target, move) {
				if (move && move.category === 'Status') {
					return priority + 1;
				}
				return priority;
			},
			rating = 4,
		},
		"pressure": {
			shortDesc: "If this Pokemon is the target of a move, that move loses one additional PP.",
			onStart: function(pokemon) {
				this.add('-ability', pokemon, 'Pressure');
			},
			onSourceDeductPP: function(pp, target, user) {
				if (target === user) return;
				return pp+1;
			},
			rating = 1,
		},
		"purepower": {
			shortDesc: "This Pokemon's Attack is doubled.",
			onModifyStats: function(stats) {
				stats.atk *= 2;
			},
			rating = 5,
		},
		"quickfeet": {
			shortDesc: "If this Pokemon is statused, its Speed is 1.5x; paralysis' Speed drop is ignored.",
			onModifyStats: function(stats, pokemon) {
				if (pokemon.status) {
					stats.spe *= 1.5;
				}
			},
			rating = 3,
		},
		"raindish": {
			shortDesc: "If Rain Dance is active, this Pokemon heals 1/16 of its max HP each turn.",
			onWeather: function(target, user, effect) {
				if (effect.id === 'raindance') {
					this.heal(target.maxhp/16);
				}
			},
			rating = 3,
		},
		"rattled": {
			shortDesc: "This Pokemon's Speed is boosted by 1 if hit by a Dark-, Bug-, or Ghost-type attack.",
			onAfterDamage: function(damage, target, user, effect) {
				if (effect && (effect.type === 'Dark' || effect.type === 'Bug' || effect.type === 'Ghost')) {
					this.boost({spe:1});
				}
			},
			rating = 2,
		},
		"reckless": {
			shortDesc: "This Pokemon's attacks with recoil or crash damage do 1.2x damage; not Struggle.",
			onBasePower: function(basePower, attacker, defender, move) {
				if (move.recoil || move.hasCustomRecoil) {
					this.debug('Reckless boost');
					return basePower * 12/10;
				}
			},
			rating = 3,
		},
		"regenerator": {
			shortDesc: "This Pokemon heals 1/3 of its max HP when it switches out.",
			onSwitchOut: function(pokemon) {
				pokemon.heal(pokemon.maxhp/3);
			},
			rating = 4.5,
		},
		"rivalry": {
			shortDesc: "This Pokemon's attacks do 1.25x on same gender targets; 0.75x on opposite gender.",
			onBasePower: function(basePower, attacker, defender, move) {
				if (attacker.gender && defender.gender) {
					if (attacker.gender === defender.gender) {
						this.debug('Rivalry boost');
						return basePower * 5/4;
					} else {
						this.debug('Rivalry weaken');
						return basePower * 3/4;
					}
				}
			},
			rating = 2.5,
		},
		"rockhead": {
			shortDesc: "This Pokemon does not receive recoil damage; not Struggle, Life Orb, crash damage.",
			onModifyMove: function(move) {
				delete move.recoil;
			},
			rating = 3.5,
		},
		"roughskin": {
			shortDesc: "This Pokemon causes other Pokemon making contact to lose 1/8 of their max HP.",
			onAfterDamage: function(damage, target, user, move) {
				if (user&& user!== target && move && move.isContact) {
					this.damage(user.maxhp/8, user, target);
				}
			},
			rating = 3,
		},
		"runaway": {
			rating = 0,
		},
		"sandforce": {
			shortDesc: "This Pokemon's Rock/Ground/Steel attacks do 1.3x in Sandstorm; immunity to it.",
			onBasePower: function(basePower, attacker, defender, move) {
				if (this.weather === 'sandstorm') {
					if (move.type === 'Rock' || move.type === 'Ground' || move.type === 'Steel') {
						this.debug('Sand Force boost');
						return basePower * 13/10;
					}
				}
			},
			onImmunity: function(type, pokemon) {
				if (type === 'sandstorm') return false;
			},
			rating = 3,
		},
		"sandrush": {
			shortDesc: "If Sandstorm is active, this Pokemon's Speed is doubled; immunity to Sandstorm.",
			onModifyStats: function(stats, pokemon) {
				if (this.weather === 'sandstorm') {
					stats.spe *= 2;
				}
			},
			onImmunity: function(type, pokemon) {
				if (type === 'sandstorm') return false;
			},
			rating = 3.5,
		},
		"sandstream": {
			shortDesc: "On switch-in, this Pokemon summons Sandstorm until another weather replaces it.",
			onStart: function(user) {
				this.setWeather('sandstorm');
				this.weatherData.duration = 0;
			},
			rating = 5,
		},
		"sandveil": {
			shortDesc: "If Sandstorm is active, this Pokemon's Evasion is 1.25x; immunity to Sandstorm.",
			onImmunity: function(type, pokemon) {
				if (type === 'sandstorm') return false;
			},
			onSourceModifyMove: function(move) {
				if (typeof move.accuracy !== 'number') return;
				if (this.weather === 'sandstorm') {
					this.debug('sand veil - decreasing accuracy');
					move.accuracy *= 0.8;
				}
			},
			rating = 3,
		},
		"sapsipper": {
			shortDesc: "This Pokemon's Attack is boosted by 1 if hit by any Grass move; Grass immunity.",
			onImmunity: function(type, pokemon) {
				if (type === 'Grass') {
					this.boost({atk:1});
					return null;
				}
			},
			rating = 3,
		},
		"scrappy": {
			shortDesc: "This Pokemon can hit Ghost-types with Normal- and Fighting-type moves.",
			onFoeModifyPokemon: function(pokemon) {
				if (pokemon.hasType('Ghost')) {
					pokemon.negateImmunity['Normal'] = true;
					pokemon.negateImmunity['Fighting'] = true;
				}
			},
			rating = 3,
		},
		"serenegrace": {
			shortDesc: "This Pokemon's moves have their secondary effect chance doubled.",
			onModifyMove: function(move) {
				if (move.secondaries) {
					this.debug('doubling secondary chance');
					for (var i=0; i<move.secondaries.length; i++) {
						move.secondaries[i].chance *= 2;
					}
				}
			},
			rating = 4,
		},
		"shadowtag": {
			shortDesc: "Prevents foes from switching out normally unless they also have this Ability.",
			onFoeModifyPokemon: function(pokemon) {
				if (pokemon.ability !== 'shadowtag') {
					pokemon.trapped = true;
				}
			},
			rating = 5,
		},
		"shedskin": {
			shortDesc: "This Pokemon has a 33% chance to have its status cured at the end of each turn.",
			onResidualOrder: 5,
			onResidualSubOrder: 1,
			onResidual: function(pokemon) {
				if (pokemon.status && this.random(3) === 0) {
					this.debug('shed skin');
					pokemon.cureStatus();
				}
			},
			rating = 3,
		},
		"sheerforce": {
			shortDesc: "This Pokemon's attacks with secondary effects do 1.3x damage; nullifies the effects.",
			onModifyMove: function(move) {
				if (move.secondaries) {
					if (!move.basePowerModifier) move.basePowerModifier = 1;
					move.basePowerModifier *= 13/10;
					delete move.secondaries;
					move.negateSecondary = true;
				}
			},
			rating = 4,
		},
		"shellarmor": {
			shortDesc: "This Pokemon cannot be struck by a critical hit.",
			onCriticalHit: false,
			rating = 1,
		},
		"shielddust": {
			shortDesc: "This Pokemon is not affected by the secondary effect of another Pokemon's attack.",
			onTrySecondaryHit: function() {
				this.debug('Shield Dust prevent secondary');
				return null;
			},
			rating = 2,
		},
		"simple": {
			shortDesc: "This Pokemon has its own stat boosts and drops doubled as they happen.",
			onBoost: function(boost) {
				for (var i in boost) {
					boost[i] *= 2;
				}
			},
			rating = 4,
		},
		"skilllink": {
			shortDesc: "This Pokemon's multi-hit attacks always hit the maximum number of times.",
			onModifyMove: function(move) {
				if (move.multihit && move.multihit.length) {
					move.multihit = move.multihit[1];
				}
			},
			rating = 4,
		},
		"slowstart": {
			shortDesc: "On switch-in, this Pokemon's Attack and Speed are halved for 5 turns.",
			onStart: function(pokemon) {
				pokemon.addVolatile('slowstart');
			},
			effect: {
				duration: 5,
				onStart: function(target) {
					this.add('-start', target, 'Slow Start');
				},
				onModifyStats: function(stats) {
					stats.atk /= 2;
					stats.spe /= 2;
				},
				onEnd: function(target) {
					this.add('-end', target, 'Slow Start');
				}
			},
			rating = -2,
		},
		"sniper": {
			shortDesc: "If this Pokemon strikes with a critical hit, the damage is tripled instead of doubled.",
			onModifyMove: function(move) {
				move.critModifier = 3;
			},
			rating = 1,
		},
		"snowcloak": {
			shortDesc: "If Hail is active, this Pokemon's Evasion is 1.25x; immunity to Hail.",
			onImmunity: function(type, pokemon) {
				if (type === 'hail') return false;
			},
			onSourceModifyMove: function(move) {
				if (typeof move.accuracy !== 'number') return;
				if (this.weather === 'hail') {
					this.debug('snow cloak - decreasing accuracy');
					move.accuracy *= 0.8;
				}
			},
			rating = 2,
		},
		"snowwarning": {
			shortDesc: "On switch-in, this Pokemon summons Hail until another weather replaces it.",
			onStart: function(user) {
				this.setWeather('hail');
				this.weatherData.duration = 0;
			},
			rating = 4.5,
		},
		"solarpower": {
			shortDesc: "If Sunny Day is active, this Pokemon's Sp. Atk is 1.5x and loses 1/8 max HP per turn.",
			onModifyStats: function(stats, pokemon) {
				if (this.weather === 'sunnyday') {
					stats.spa *= 1.5;
				}
			},
			onWeather: function(target, user, effect) {
				if (effect.id === 'sunnyday') {
					this.damage(target.maxhp/8);
				}
			},
			rating = 3,
		},
		"solidrock": {
			shortDesc: "This Pokemon receives 3/4 damage from super effective attacks.",
			onFoeBasePower: function(basePower, attacker, defender, move) {
				if (this.getEffectiveness(move.type, defender) > 0) {
					this.debug('Solid Rock neutralize');
					return basePower * 3/4;
				}
			},
			rating = 3,
		},
		"soundproof": {
			shortDesc: "This Pokemon is immune to sound-based moves.",
			onImmunity: function(type, pokemon) {
				if (type === 'sound') {
					this.add('-immune', pokemon.id, '[msg]');
					return null;
				}
			},
			rating = 2,
		},
		"speedboost": {
			shortDesc: "This Pokemon's Speed is boosted by 1 at the end of each turn.",
			onResidualOrder: 26,
			onResidualSubOrder: 1,
			onResidual: function(pokemon) {
				if (pokemon.activeTurns) {
					this.boost({spe:1});
				}
			},
			rating = 4.5,
		},
		"stall": {
			shortDesc: "This Pokemon attacks last among Pokemon using the same or greater priority moves.",
			onModifyMove: function(move) {
				move.priority -= 0.1;
			},
			rating = -1,
		},
		"static": {
			shortDesc: "30% chance of paralyzing a Pokemon making contact with this Pokemon.",
			onAfterDamage: function(damage, target, user, effect) {
				if (effect && effect.isContact) {
					if (this.random(10) < 3) {
						user.trySetStatus('par', target, effect);
					}
				}
			},
			rating = 2,
		},
		"steadfast": {
			shortDesc: "If this Pokemon is made to flinch, its Speed is boosted by 1.",
			onFlinch: function(pokemon) {
				this.boost({spe: 1});
			},
			rating = 1,
		},
		"stench": {
			shortDesc: "This Pokemon's attacks without a chance to flinch have a 10% chance to flinch.",
			onModifyMove: function(move) {
				if (move.category !== "Status") {
					this.debug('Adding Stench flinch');
					if (!move.secondaries) move.secondaries = [];
					for (var i=0; i<move.secondaries.length; i++) {
						if (move.secondaries[i].volatileStatus === 'flinch') return;
					}
					move.secondaries.push({
						chance: 10,
						volatileStatus: 'flinch'
					});
				}
			},
			rating = 0,
		},
		"stickyhold": {
			shortDesc: "This Pokemon cannot lose its held item due to another Pokemon's attack.",
			onTakeItem: function(item, pokemon, user) {
				if (user&& user!== pokemon) return false;
			},
			rating = 1,
		},
		"stormdrain": {
			shortDesc: "This Pokemon draws Water moves to itself to boost Sp. Atk by 1; Water immunity.",
			onImmunity: function(type, pokemon) {
				if (type === 'Water') {
					this.boost({spa:1});
					return null;
				}
			},
			rating = 3,
		},
		"sturdy": {
			shortDesc: "If this Pokemon is at full HP, it lives one hit with at least 1HP. OHKO moves fail on it.",
			onDamagePriority: -100,
			onDamage: function(damage, target, user, effect) {
				if (effect && effect.ohko) {
					this.add('-activate',target,'Sturdy');
					return 0;
				}
				if (target.hp === target.maxhp && damage >= target.hp && effect && effect.effectType === 'Move') {
					this.add('-activate',target,'Sturdy');
					return target.hp - 1;
				}
			},
			rating = 3,
		},
		"suctioncups": {
			shortDesc: "This Pokemon cannot be forced to switch out by another Pokemon's attack or item.",
			onDragOut: false,
			rating = 3,
		},
		"superluck": {
			shortDesc: "This Pokemon's critical hit ratio is boosted by 1.",
			onModifyMove: function(move) {
				move.critRatio++;
			},
			rating = 1,
		},
		"swarm": {
			shortDesc: "When this Pokemon has 1/3 or less of its max HP, its Bug attacks do 1.5x damage.",
			onBasePower: function(basePower, attacker, defender, move) {
				if (move.type === 'Bug' && attacker.hp <= attacker.maxhp/3) {
					this.debug('Swarm boost');
					return basePower * 1.5;
				}
			},
			rating = 2,
		},
		"swiftswim": {
			shortDesc: "If Rain Dance is active, this Pokemon's Speed is doubled.",
			onModifyStats: function(stats, pokemon) {
				if (this.weather === 'raindance') {
					stats.spe *= 2;
				}
			},
			rating = 3.5,
		},
		"synchronize": {
			shortDesc: "If another Pokemon burns/poisons/paralyzes this Pokemon, it also gets that status.",
			onAfterSetStatus: function(status, target, user) {
				if (!user|| user=== target) return;
				if (status.id === 'slp' || status.id === 'frz') return;
				user.trySetStatus(status);
			},
			rating = 3,
		},
		"tangledfeet": {
			shortDesc: "This Pokemon's Evasion is doubled as long as it is confused.",
			onSourceModifyMove: function(move, user, target) {
				if (target && target.volatiles['confusion'] && move.accuracy !== true) {
					move.accuracy /= 2;
				}
			},
			rating = 1,
		},
		"technician": {
			shortDesc: "This Pokemon's attacks of 60 base power or less do 1.5x damage. Includes Struggle.",
			onBasePowerPriority: 10,
			onBasePower: function(basePower, attacker, defender, move) {
				if (basePower <= 60) {
					this.debug('Technician boost');
					return basePower * 1.5;
				}
			},
			rating = 4,
		},
		"telepathy": {
			shortDesc: "This Pokemon does not take damage from its allies' attacks.",
			onTryHit: function(target, user, move) {
				if (target.side === user.side && move.target === 'allAdjacent') {
					this.add('-activate', target, 'ability: Telepathy');
					return null;
				}
			},
			rating = 0,
		},
		"teravolt": {
			shortDesc: "This Pokemon's moves ignore the target's Ability if that Ability would modify damage.",
			onStart: function(pokemon) {
				this.add('-ability', pokemon, 'Teravolt');
			},
			onAllyModifyPokemon: function(pokemon) {
				if (this.activePokemon === this.effectData.target && pokemon !== this.activePokemon) {
					pokemon.ignore['Ability'] = 'A';
				}
			},
			onFoeModifyPokemon: function(pokemon) {
				if (this.activePokemon === this.effectData.target) {
					pokemon.ignore['Ability'] = 'A';
				}
			},
			rating = 3,
		},
		"thickfat": {
			shortDesc: "This Pokemon receives half damage from Ice-type and Fire-type attacks.",
			onSourceBasePower: function(basePower, attacker, defender, move) {
				if (move.type === 'Ice' || move.type === 'Fire') {
					this.debug('Thick Fat weaken');
					return basePower / 2;
				}
			},
			rating = 3,
		},
		"tintedlens": {
			shortDesc: "This Pokemon's attacks that are not very effective on a target do double damage.",
			onBasePowerPriority: -100,
			onBasePower: function(basePower, attacker, defender, move) {
				if (this.getEffectiveness(move.type, defender) < 0) {
					this.debug('Tinted Lens boost');
					return basePower * 2;
				}
			},
			rating = 4.5,
		},
		"torrent": {
			shortDesc: "When this Pokemon has 1/3 or less of its max HP, its Water attacks do 1.5x damage.",
			onBasePower: function(basePower, attacker, defender, move) {
				if (move.type === 'Water' && attacker.hp <= attacker.maxhp/3) {
					this.debug('Torrent boost');
					return basePower * 1.5;
				}
			},
			rating = 2,
		},
		"toxicboost": {
			shortDesc: "When this Pokemon is poisoned, its physical attacks do 1.5x damage.",
			onModifyStats: function(stats, pokemon) {
				if (pokemon.status === 'psn' || pokemon.status === 'tox') {
					stats.atk *= 1.5;
				}
			},
			rating = 3,
		},
		"trace": {
			shortDesc: "On switch-in, or when it can, this Pokemon copies a random adjacent foe's Ability.",
			onUpdate: function(pokemon) {
				var target = pokemon.side.foe.randomActive();
				if (!target) return;
				var ability = this.getAbility(target.ability);
				if (ability.id === 'flowergift' || ability.id === 'forecast' || ability.id === 'illusion' || ability.id === 'imposter' || ability.id === 'multitype' || ability.id === 'trace' || ability.id === 'wonderguard' || ability.id === 'zenmode') return;
				if (pokemon.setAbility(ability)) {
					this.add('-ability',pokemon, ability,'[from] ability: Trace','[of] '+target);
				}
			},
			rating = 3.5,
		},
		"truant": {
			shortDesc: "This Pokemon skips every other turn instead of using a move.",
			onBeforeMove: function(pokemon, target, move) {
				if (pokemon.removeVolatile('truant')) {
					this.add('cant',pokemon,'ability: Truant', move);
					return false;
				}
				pokemon.addVolatile('truant');
			},
			effect: {
				duration: 2
			},
			rating = -2,
		},
		"turboblaze": {
			shortDesc: "This Pokemon's moves ignore the target's Ability if that Ability would modify damage.",
			onStart: function(pokemon) {
				this.add('-ability', pokemon, 'Turboblaze');
			},
			onAllyModifyPokemon: function(pokemon) {
				if (this.activePokemon === this.effectData.target && pokemon !== this.activePokemon) {
					pokemon.ignore['Ability'] = 'A';
				}
			},
			onFoeModifyPokemon: function(pokemon) {
				if (this.activePokemon === this.effectData.target) {
					pokemon.ignore['Ability'] = 'A';
				}
			},
			rating = 3,
		},
		"unaware": {
			shortDesc: "This Pokemon ignores other Pokemon's stat changes when taking or doing damage.",
			onModifyMove: function(move, user, target) {
				move.ignoreEvasion = true;
				move.ignoreDefensive = true;
			},
			onSourceModifyMove: function(move, user, target) {
				move.ignoreAccuracy = true;
				move.ignoreOffensive = true;
			},
			rating = 2,
		},
		"unburden": {
			shortDesc: "Speed is doubled on held item loss; boost is lost if it switches, gets new item/Ability.",
			onUseItem: function(item, pokemon) {
				pokemon.addVolatile('unburden');
			},
			onTakeItem: function(item, pokemon) {
				pokemon.addVolatile('unburden');
			},
			effect: {
				onModifyStats: function(stats, pokemon) {
					if (pokemon.ability !== 'unburden') {
						pokemon.removeVolatile('unburden');
						return;
					}
					if (!pokemon.item) {
						stats.spe *= 2;
					}
				}
			},
			rating = 3.5,
		},
		"unnerve": {
			shortDesc: "While this Pokemon is active, prevents opposing Pokemon from using their Berries.",
			onStart: function(pokemon) {
				this.add('-ability',pokemon,'Unnerve',pokemon.side.foe);
			},
			onFoeEatItem: false,
			rating = 1,
		},
		"victorystar": {
			shortDesc: "This Pokemon and its allies' moves have their Accuracy boosted to 1.1x.",
			onAllyModifyMove: function(move) {
				if (typeof move.accuracy === 'number') {
					move.accuracy *= 1.1;
				}
			},
			rating = 2,
		},
		"vitalspirit": {
			shortDesc: "This Pokemon cannot fall asleep. Gaining this Ability while asleep cures it.",
			onImmunity: function(type) {
				if (type === 'slp') return false;
			},
			rating = 1,
		},
		"voltabsorb": {
			shortDesc: "This Pokemon heals 1/4 of its max HP when hit by Electric moves; Electric immunity.",
			onImmunity: function(type, pokemon) {
				if (type === 'Electric') {
					var d = pokemon.heal(pokemon.maxhp/4);
					this.add('-heal',pokemon,d+pokemon.getHealth(),'[from] ability: Volt Absorb');
					return null;
				}
			},
			rating = 3,
		},
		"waterabsorb": {
			shortDesc: "This Pokemon heals 1/4 of its max HP when hit by Water moves; Water immunity.",
			onImmunity: function(type, pokemon) {
				if (type === 'Water') {
					var d = pokemon.heal(pokemon.maxhp/4);
					this.add('-heal',pokemon,d+pokemon.getHealth(),'[from] ability: Water Absorb');
					return null;
				}
			},
			rating = 3,
		},
		"waterveil": {
			shortDesc: "This Pokemon cannot be burned. Gaining this Ability while burned cures it.",
			onImmunity: function(type, pokemon) {
				if (type === 'brn') return false;
			},
			rating = 1.5,
		},
		"weakarmor": {
			shortDesc: "If a physical attack hits this Pokemon, Defense is lowered 1 and Speed is boosted 1.",
			onAfterDamage: function(damage, target, user, move) {
				if (move.category === 'Physical') {
					this.boost({spe:1, def:-1});
				}
			},
			rating = 0,
		},
		"whitesmoke": {
			shortDesc: "Prevents other Pokemon from lowering this Pokemon's stat stages.",
			onBoost: function(boost, target, user) {
				if (!user|| target === user) return;
				for (var i in boost) {
					if (boost[i] < 0) {
						delete boost[i];
						this.add("-message", target.name+"'s stats were not lowered! (placeholder)");
					}
				}
			},
			rating = 2,
		},
		"wonderguard": {
			shortDesc: "This Pokemon can only be damaged by super effective moves and indirect damage.",
			onDamagePriority: 10,
			onDamage: function(damage, target, user, effect) {
				if (effect.effectType !== 'Move') return;
				if (effect.type === '???' || effect.id === 'Struggle') return;
				this.debug('Wonder Guard immunity: '+effect.id);
				if (this.getEffectiveness(effect.type, target) <= 0) {
					this.add('-activate',target,'ability: Wonder Guard');
					return null;
				}
			},
			onSubDamage: function(damage, target, user, effect) {
				if (effect.effectType !== 'Move') return;
				if (target.negateImmunity[effect.type]) return;
				this.debug('Wonder Guard immunity: '+effect.id);
				if (this.getEffectiveness(effect.type, target) <= 0) {
					this.add('-activate',target,'ability: Wonder Guard');
					return null;
				}
			},
			rating = 5,
		},
		"wonderskin": {
			shortDesc: "All status moves with a set % Accuracy are 50% accurate if used on this Pokemon.",
			onSourceModifyMovePriority: 10,
			onSourceModifyMove: function(move) {
				if (move.category === 'Status' && typeof move.accuracy === 'number') {
					this.debug('setting move accuracy to 50%');
					move.accuracy = 50;
				}
			},
			rating = 1,
		},
		"zenmode": {
			shortDesc: "If this Pokemon is Darmanitan, it changes to Zen Mode whenever it is below half HP.",
			onResidualOrder: 27,
			onResidual: function(pokemon) {
				if (pokemon.baseTemplate.species !== 'Darmanitan') {
					return;
				}
				if (pokemon.hp <= pokemon.maxhp/2 && pokemon.template.speciesid==='darmanitan'){
					pokemon.addVolatile('zenmode');
				} else if (pokemon.hp > pokemon.maxhp/2 && pokemon.template.speciesid==='darmanitanzen') {
					pokemon.removeVolatile('zenmode');
				}
			},
			effect: {
				onStart: function(pokemon) {
					if (pokemon.transformInto('Darmanitan-Zen')) {
						this.add('-formechange', pokemon, 'Darmanitan-Zen');
						this.add('-message', 'Zen Mode triggered! (placeholder)');
					} else {
						return false;
					}
				},
				onEnd: function(pokemon) {
					if (pokemon.transformInto('Darmanitan')) {
						this.add('-formechange', pokemon, 'Darmanitan');
						this.add('-message', 'Zen Mode ended! (placeholder)');
					} else {
						return false;
					}
				},
				onUpdate: function(pokemon) {
					if (pokemon.ability !== 'zenmode') {
						pokemon.removeVolatile('zenmode');
					}
				}
			},
			rating = -1,
		},

		// CAP
		"mountaineer": {
			shortDesc: "On switch-in, this Pokemon avoids all Rock-type attacks and Stealth Rock.",
			onDamage: function(damage, target, user, effect) {
				if (effect && effect.id === 'stealthrock') {
					return false;
				}
			},
			onImmunity: function(type, target) {
				if (type === 'Rock' && !target.activeTurns) {
					return false;
				}
			},
			rating = 3.5,
		},
		"rebound": {
			shortDesc: "On switch-in, this Pokemon blocks certain status moves and uses the move itself.",
			onAllyTryFieldHit: function(target, user, move) {
				if (target === user) return;
				if (this.effectData.target.activeTurns) return;
				if (typeof move.isBounceable === 'undefined') {
					move.isBounceable = !!(move.status || move.forceSwitch);
				}
				if (move.target !== 'foeSide' && target !== this.effectData.target) {
					return;
				}
				if (this.pseudoWeather['magicbounce']) {
					return;
				}
				if (move.isBounceable) {
					this.addPseudoWeather('magicbounce');
					this.add('-activate', target, 'ability: Rebound', move, '[of] '+user);
					this.moveHit(user, user, move);
					return null;
				}
			},
			effect: {
				duration: 1
			},
			rating = 4.5,
		},
		"persistent": {
			shortDesc: "The duration of certain field effects is increased by 2 turns if used by this Pokemon.",
			// implemented in the corresponding move
			rating = 4,
		}
	};*/
}
