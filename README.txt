Geniusect is (currently planned) to be a four-component project, including a data gatherer (TBA), AI (https://github.com/Jay2645/geniusect-ai), local simulator (this project), and battle automator (https://github.com/rissole/geniusect-selenium) for the Pokemon Showdown website.

Geniusect-Sim is a local simulator for the browser-based Pokemon Showdown! website. It can accurately estimate the outcome of potential events using the game's own formulae. It can also send data to Showdown and interpret the events within the simulator using the prerequisite Geniusect-Selenium project (https://github.com/rissole/geniusect-selenium). It can also reverse-engineer the events within the simulator to discover the values of unknown variables and can allow other projects access to these variables or provide them via a human-friendly GUI which gives an "Importable" as output in the following format:

Nickname (SpeciesName) @ ItemName
Trait: AbilityName
EVs: n HP / n Spd / n Atk / n Def / n SpA / n SDef
NatureName Nature
- Move1
- Move2
- Move3
- Move4

For example:
PikaPika (Pikachu) @ Light Ball
Trait: Static
EVs: 252 Spd / 252 Atk / 4 SDef
Jolly Nature
- Volt Tackle
- Thunderbolt
- Hidden Power [Ice]
- Fly