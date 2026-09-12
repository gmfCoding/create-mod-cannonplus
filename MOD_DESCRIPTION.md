# Create: Cannon Plus

> A tiny [Create](https://github.com/Creators-of-Create/Create) addon that lets the **Schematicannon skip blocks**.
> Minecraft 1.20.1 · Forge · Create 6.0.8

---

## Mod description

**Create: Cannon Plus** turns Create's Schematicannon material list into a **block skip list**.

When the Schematicannon prints the list of blocks its schematic needs onto a clipboard, this mod writes it
as a **Schematic Skip List** — every required block on its own line, with the block's icon and the amount
needed, and **nothing pre-checked**. Take the clipboard out, mark the blocks you don't want placed, and
reinsert it: the cannon then ignores those blocks while printing and skips straight past them.

Each entry's checkbox has **three states** — click to cycle:

| State | Looks like | Cannon behaviour |
|---|---|---|
| Incomplete | no tick | places the block normally |
| Complete | green tick | places the block normally (marked as handled) |
| Omitted | red cross | skips the block |

No new items, blocks, screens, or packets. It reuses Create's own clipboard and checklist machinery, so the
printed counts stay accurate and the skip list behaves like any other Create clipboard.

### Features

- **Print a skip list** — a blank clipboard in the list-printer slot becomes a *Schematic Skip List* listing
  every item the schematic needs (icons + amounts), sorted alphabetically.
- **Three-state checkboxes** — click an entry to cycle **Incomplete** (no tick) → **Complete** (green tick)
  → **Omitted** (red cross) → back to Incomplete. **Omitted** entries are skipped by the cannon; **Complete**
  just marks an entry as handled. Skipping is per *item*, so it covers every block and block entity variant
  that needs that item.
- **Live configuration** — while a ticked skip list sits in the list-printer slot, the cannon reads it as its
  active config every tick and won't consume or overwrite it.
- **Accurate checklist** — when the skip list is (re)inserted, the checklist is recomputed so the printed
  placed/needed counts already exclude the skipped blocks.
- **Easy to tweak** — pull the clipboard, adjust the ticks, put it back. No skip list in the slot? The cannon
  prints normally.

---

## Quick tutorial

### 1. Load a schematic into the cannon

Place a **Schematicannon**, link it to a placed schematic (or blueprint) as you normally would, and give it
power + gunpowder.

### 2. Print the skip list

Put a **blank clipboard** into the cannon's **list-printer slot** (the clipboard slot on the block).
The cannon writes a **Schematic Skip List**: one line per block the schematic needs, each with the item's
icon and required amount. Every entry starts **unchecked**.

### 3. Mark what to skip

Take the clipboard out of the cannon and **open it** (right-click while holding it).
**Click an entry's checkbox** to cycle it: **Incomplete** → **Complete** (green tick) → **Omitted**
(red cross) → **Incomplete**.

Mark the blocks you want the cannon *not* to place — e.g. decorative fills, glass, torches, or anything
you'd rather place by hand — as **Omitted** (red cross). **Complete** (green tick) simply records that an
entry is handled and does **not** skip it.

> Skipping is per *item* and skips *every* placement that needs that item, including variants and block
> entities that drop it.

### 4. Reinsert and print

Put the ticked clipboard back into the **list-printer slot**. The cannon now treats it as the active
skip list: it will **not place** any **Omitted** block, and the on-screen placed/needed counts update to match.

### 5. Change your mind?

Any time, take the clipboard out, untick/retick entries, and reinsert it. Removing the clipboard entirely
switches the cannon back to normal, place-everything behaviour.

---

## Notes

- The clipboard is renamed **Schematic Skip List** so it's easy to tell apart from Create's default material lists.
- Only the list-printer slot is used — the skip list is read as configuration there and is never consumed.
- Special placements follow the same rule: omitting **Super Glue** skips glue, and omitting **Belt Connector**
  skips belts while still placing their **pulley shafts** (omit **Shaft** as well to skip those too).
- Compatible with the rest of the Schematicannon: skipping reduces the materials you need to supply, and the
  cannon still finishes the schematic, just leaving the skipped blocks as air.

---

## Requirements & install

- Minecraft **1.20.1**, Forge **47.1.x**
- Create **6.0.8** (install separately)

Place `create-cannonplus-1.20.1-<version>.jar` into `mods/` alongside Create and restart the game.

## License

MIT
