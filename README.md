# Create: Cannon Plus

A small [Create](https://github.com/Creators-of-Create/Create) addon for **Minecraft 1.20.1** (Forge)
targeting **Create 6.0.8** that lets the **Schematicannon skip blocks**.

## What it does

> 📖 Looking for a store-page description or a step-by-step walkthrough? See
> [MOD_DESCRIPTION.md](MOD_DESCRIPTION.md).

Create's Schematicannon already prints the full material list of a loaded schematic onto a **clipboard**
(the list-printer slot). This mod turns that printed clipboard into a **block skip list**:

1. Insert a (blank) **clipboard** into the Schematicannon's list-printer slot while a schematic is loaded.
   The cannon writes a *Schematic Skip List* - one line per block it needs, each with the block's icon and
   required amount. Nothing is pre-checked.
2. Take the clipboard out and open it (right-click while holding it). **Click an entry's checkbox** to
   cycle it through three states: **Incomplete** (no tick) → **Complete** (green tick) → **Omitted**
   (red cross) → back to Incomplete.
3. Reinsert the marked clipboard into the list-printer slot. The cannon now treats it as the active
   skip list and **will not place** any entry marked **Omitted** while printing - it skips straight past
   those. **Complete** entries stay ticked but are still placed normally.

The skip list stays in the cannon without being consumed, so you can keep tweaking ticks and reinserting.
Removing the clipboard disables the skip behaviour again.

## How it works

A single Mixin into `SchematicannonBlockEntity`:

- Redirects the checklist→clipboard print (`createWrittenClipboard`) to a custom generator that writes a
  tickable, all-unchecked skip list (with an NBT marker) instead of Create's read-only pre-completed list.
- At the head of `tickPaperPrinter`, a clipboard carrying the marker is treated as configuration: it is
  neither consumed nor reprinted, and the checklist is recomputed once so counts already exclude skipped blocks.
- In `shouldPlace`, any block whose required item is marked **Omitted** on the active skip list is skipped.

No new items, blocks, packets or screens - it reuses Create's clipboard + checklist machinery.

## Requirements

- Minecraft **1.20.1**
- Forge **47.1.x**
- [Create 6.0.8](https://modrinth.com/mod/create) for 1.20.1 (installed separately)

## Installation

Drop `create-cannonplus-1.20.1-<version>.jar` into your `mods/` folder together with Create.

## Building

Requires a JDK 17 (auto-provisioned by Gradle).

```bash
./gradlew reobfJar
```

The playable (reobfuscated) jar is `build/libs/create-cannonplus-1.20.1-<version>.jar`.

### Dev dependency

Create is fetched from the official Create maven as a **dev-only** dependency
(`modImplementation` of `com.simibubi.create:create-1.20.1:6.0.8-289:slim` plus Ponder, Flywheel and Registrate),
per the [Create wiki](https://wiki.createmod.net/developers/depend-on-create/forge-1.20.1).
It is used to compile and run against and is **never bundled** into the jar.

## License

MIT
