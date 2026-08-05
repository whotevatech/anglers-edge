# Angler's Edge

A small NeoForge 1.21.1 content mod: rod tiers, bait, an upgrade station, and an auto-fisher.
Mod ID `anglersedge` (easy to rename — see below).

## What's in it

**Rods** (`sturdy_rod`, `anglers_rod`, `leviathan_rod`): each is a real `FishingRodItem`
subclass with its own durability and a flat luck/lure bonus baked in, rather than read from
enchantments. Casting a line with one spawns a vanilla `FishingHook` using those numbers
directly — confirmed against the real 1.21.1 class that `luck` and `lureSpeed` are plain
constructor `int`s, so no hook subclass or mixin was needed. Reeling in is left to vanilla.

Only `sturdy_rod` has a crafting recipe (copper + string). The other two tiers only come from
the upgrade station — that's deliberate, not a missing recipe.

**Bait** (`basic_bait`, `shiny_lure`, `frenzy_bait`, `ghost_bait`): held in the off-hand, not
loaded onto the rod. `FishingEventHandler` listens for NeoForge's `ItemFishedEvent` — which
fires after vanilla has already rolled the fishing loot table but before the items reach the
player — checks the off-hand, applies the bait's effect to the drop list, and consumes one bait.
Works with vanilla rods too, not just this mod's. Effects: basic upgrades junk catches to fish,
shiny adds a 20% chance of bonus treasure, frenzy doubles the catch quantity, ghost replaces the
catch outright with guaranteed treasure. The junk/treasure/fish item sets are hand-maintained in
`BaitEffect` (matching the vanilla fishing loot pools) rather than read from the datapack — see
the note there if the vanilla pools ever drift.

**Upgrade Station** (`upgrade_station`, block + GUI): rod in one slot, material in another,
upgraded rod appears in the result slot the moment both match — same "instant preview, consume
on take" pattern vanilla crafting uses. Recipe table lives in `UpgradePath` as a flat lookup
(only two upgrades, didn't seem worth forcing onto the shaped-recipe system):
Sturdy + 4x Prismarine Crystals → Angler's, Angler's + 2x Nautilus Shell → Leviathan.

**Auto-Fisher** (`auto_fisher`, block, no GUI): needs water adjacent (checks all 6 neighbours).
Every 200 ticks (10s) it rolls the real `minecraft:gameplay/fishing` loot table directly and
drops the result into its own 9-slot inventory — any redstone signal drops the interval to 60
ticks. The block entity implements `Container` itself, so vanilla hoppers can pull from it with
no extra wiring; it also registers the `IItemHandler` capability for cross-mod pipes.

## Building

Needs JDK 21. `gradle-wrapper.jar` and the `gradlew`/`gradlew.bat` scripts aren't included —
run `gradle wrapper --gradle-version 8.10` once inside the project, or copy them from any recent
NeoForge MDK, same as you did for the SkyGrid pack.

```bash
./gradlew build          # -> build/libs/anglersedge-1.0.0.jar
./gradlew runClient       # dev world with everything in the "Angler's Edge" creative tab
```

### What's already been verified

All 20 Java source files — every item, block, block entity, menu, and the client screen —
compile with **zero errors** against the real, official-mapped 1.21.1 classes and the real
NeoForge 21.1.244 classes (not stubs). Two of NeoForge's own access-transformer entries
(`BlockEntityType$BlockEntitySupplier`, `MenuType$MenuSupplier`, `MenuScreens$ScreenConstructor`)
had to be reproduced by hand for this offline check, since applying NeoForge's full source-patch
pipeline isn't possible in this sandbox — the same RAM ceiling documented in the SkyGrid pack's
`PORTING.md`. All 24 JSON assets (recipes, loot tables, models, blockstates, lang) parse cleanly.

What this **doesn't** cover: runtime behaviour. `runClient` is still the real test — same caveat
as SkyGrid.

## Known gaps in this first pass

- Textures are flat placeholder art (rods are a coloured line + handle, baits are blobs, blocks
  are bordered squares) generated in this session, not hand-drawn or AI-art. Swap the PNGs under
  `src/main/resources/assets/anglersedge/textures/` for real art any time — nothing in the Java
  code references pixel content, just file paths.
- Breaking the upgrade station or auto-fisher drops the block but not its inventory contents
  (no `copy_state`/container loot function on the loot tables yet).
- Bait's junk/treasure/fish item lists are hand-maintained, not read from the actual datapack
  registry — see the comment in `BaitEffect`.
- No new fish species yet, per the initial scope call.

## Renaming

Mod ID `anglersedge` shows up in: `gradle.properties` (`mod_id`), the Java package
`dev.whoteva.anglersedge`, every `assets/anglersedge` and `data/anglersedge` folder name, and
the `anglersedge:` prefix inside the JSON files themselves. A project-wide find/replace on the
string `anglersedge` (plus a package rename in your IDE) covers all of it.
