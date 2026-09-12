package com.gmfcoding.cannonplus.schematicannon;

import java.util.Set;

import net.minecraft.world.item.Item;

/**
 * Duck interface mixed into Create's {@code SchematicPrinter} so the cannon can hand its active skip list to
 * the printer.
 * <p>
 * Needed because Create's entity print stage (super glue, item frames, armour stands, ...) bypasses the
 * cannon's {@code shouldPlace} predicate entirely, so those placements can only be filtered inside the printer.
 */
public interface SchematicPrinterState {

	Set<Item> cannonplus$getSkippedItems();

	void cannonplus$setSkippedItems(Set<Item> skippedItems);
}
