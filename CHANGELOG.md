# Changelog

## 1.1.0

Polish, bugfixes, and a wider fishing loop. Still NeoForge 1.21.1.

### Screens

- Upgrade Station, Auto-Fisher, and Breeding Tank share one slot, arrow, and progress-bar helper. Slots use the same 18×18 footprint as the menus, with vanilla-style rims.
- Each screen has its own panel texture: a light inventory field, a teal work well, and a brass rule. The old upgrade texture no longer draws slots in the wrong place.
- Upgrade Station names the rod, material, and result slots, shows a ghost when they are empty, and says what the current pair still needs (including short stacks).
- Auto-Fisher shows filter, reel, and bait ghosts, a countdown, and whether it is waiting on water, powered, filtered, baited, or full.
- Breeding Tank shows a fill arrow, a progress bar, and a status line for a missing pair, a mismatch, breeding, a full output, or missing water.
- The Fishing Journal opens on right-click. It lists every catalog species, dims ones you have not caught, and tracks four milestones.

### Graphics

- Item and block textures were redrawn as small tackle-shop sprites: tiered rods, distinct fish, bait, the net, the reel, the journal, and the four machines. Trophy fish scale with size and rarity and sparkle for epic, boss, and record catches. An empty plaque says so.

### Fixes

- Shift-clicking an upgrade result consumes the inputs only when the whole result fits, so a partial move cannot duplicate a craft.
- Breaking an Upgrade Station drops the inputs and not the preview result.
- Breaking an Auto-Fisher drops the catch bin and the filter, reel, and bait.
- Ghost Bait is applied after the biome swap, so a biome fish no longer replaces the guaranteed treasure.
- Reef Shark and Ancient Leviathan keep their identity across a restart (scoreboard tag, not only an in-memory id) and still drop teeth or scales.
- The breeding tank no longer resets progress or deletes a finished offspring when the output is full, and it no longer marks the chunk dirty every tick.
- The journal shows translated species names and ignores junk. Shark teeth and leviathan scales are recorded when you take the kill.
- Pipes can pull breeding-tank offspring and auto-fisher catches. They cannot pull the upgrade preview or the breeding pair.

### Additions

- Three biome fish: Sunscale (warm ocean and beaches), Frost Char (cold and frozen water), and Cave Loach (lush and dripstone caves), with loot tables and handheld flavor.
- Auto-Fisher bait slot. One bait is spent per cycle that actually stores a catch. Reel upgrades still roll twice.
- Journal milestones at 1, 5, 10, and all 14 species. Completing a journal grants an advancement, 4 Shiny Lures, and a Reel Upgrade, once per journal.
- Sixteen advancements, from the first rod through the leviathan.
- Trophy tiers: mounted fish bob, grow with their size roll, and use a rarity-colored nameplate.
- Reef sharks and the leviathan glow when they rise, with a splash, an action-bar line, and (for the leviathan) a chat line. Whale sightings use the same water burst. Only one encounter fires per catch.
- Common config for encounter rates, biome-flavor chance, shiny-lure chance, Leviathan Rod bonus, weather bonuses, auto-fisher intervals, and breeding time.

Handheld biome flavor defaults to 25% (was 15%) so the new waters show up on a rod. Auto-fishers use the loot tables instead. Bait roles are unchanged: junk to fish, 20% treasure, double catch, guaranteed treasure.

## 1.0.0

Initial release: rod tiers, four baits, Upgrade Station, Auto-Fisher, Fishing Journal, seven species, weight and rarity, Fish Trophy, Breeding Tank, Reef Shark, Ancient Leviathan, and whale sightings.
