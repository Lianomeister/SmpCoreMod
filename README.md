# SMP Core

Server-side Fabric mod for **Minecraft 1.21.11** that provides SMP rules, combat balance tweaks, and quality-of-life features in one place.

## Features (SMP Core)

- Rituals
- SMP Start command (with grace and everything)
- Damage modification (to balance PvP)
- Effect ban
- Enchantment ban
- Tipped arrow ban
- Protection limit
- Sharpness limit
- Anti restock and anti elytra in combat
- Ban anchors
- Ban bed bombing
- Ban TNT minecarts
- Ban mace
- Make mace stun shield
- Mace cooldown
- Modify shield cooldown
- Wind charge cooldown
- Riptide cooldown
- Gap cooldown
- One craft recipes
- Killing a warden drops its heart (can be used in custom recipes)
- Invisibility QOL (fully anonymous invis; kill message hides name)
- Ban breach swapping (blocks swapping; breach enchant itself stays allowed)

## Features (available via other mods/plugins)

- Item limiter
- Item ban
- One player sleep
- Clumps
- Disable/enable dimensions
- Anti health indicators
- Built-in health indicators
- Stop items from despawning
- Infinite restock
- Combat system
- No naked killing
- No AFK killing
- Anti Xaero minimap
- Ban crystals
- Ban pearls
- Custom recipes
- Anti X-Ray
- PvP toggle
- One mace
- Shield tweaks
- Pearl cooldown
- String dupers revival (1.21.4+)
- Put players into spectator after death (good for Doomsday)
- First-join kit

## Install

- Requires Fabric Loader and Fabric API for **1.21.11**
- Drop the built jar into your server’s `mods/` folder
- Config generates on first launch at `config/smpcore.json`

## Admin GUI

- Run `/smpcore` (requires admin permissions) to open the in-game **Admin Panel**
- Saving in the GUI writes `config/smpcore.json` and hot-reloads the rules

## Build

```bash
./gradlew build
```

## Notes

- More features are added every update.
- Feature suggestions are welcome, but these will not be added:
  - Click Villager
  - Built-in voice chat (unless partnering with the voice chat creator)
