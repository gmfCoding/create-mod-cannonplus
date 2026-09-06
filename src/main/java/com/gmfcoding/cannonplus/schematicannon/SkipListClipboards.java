package com.gmfcoding.cannonplus.schematicannon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.content.schematics.cannon.MaterialChecklist;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.StackRequirement;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Helpers for the Schematicannon "block skip list", which lives on a Create clipboard item.
 * <p>
 * The cannon prints one line per needed block onto a clipboard (each with the block's item as icon).
 * The player ticks (checks) the lines they want skipped; the cannon then ignores those blocks while printing.
 */
public final class SkipListClipboards {

	/** NBT marker that flags a clipboard as the cannon's active skip list. */
	public static final String MARKER = "CannonSkipList";
	public static final String READONLY = "Readonly";
	public static final int MAX_ENTRIES_PER_PAGE = 7;

	private SkipListClipboards() {
	}

	/** A skip list is a Create clipboard printed by the Schematicannon (carries our marker). */
	public static boolean isSkipListClipboard(ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return false;
		if (!AllBlocks.CLIPBOARD.isIn(stack))
			return false;
		CompoundTag tag = stack.getTag();
		return tag != null && tag.getBoolean(MARKER);
	}

	/** Collects the items the player has ticked on the given skip-list clipboard. */
	public static Set<Item> collectSkipped(ItemStack clipboard) {
		Set<Item> skipped = new HashSet<>();
		if (!isSkipListClipboard(clipboard))
			return skipped;

		List<List<ClipboardEntry>> pages = ClipboardEntry.readAll(clipboard);
		for (List<ClipboardEntry> page : pages) {
			if (page == null)
				continue;
			for (ClipboardEntry entry : page) {
				if (entry == null || !entry.checked)
					continue;
				if (entry.icon == null || entry.icon.isEmpty())
					continue;
				skipped.add(entry.icon.getItem());
			}
		}
		return skipped;
	}

	/**
	 * Returns true when the block about to be placed requires an item that is ticked on the skip list.
	 * Uses the same {@link ItemRequirement} mapping the cannon uses to gather materials, so ticks line up
	 * exactly with the icons printed on the clipboard.
	 */
	public static boolean isBlockSkipped(Set<Item> skipped, BlockState state, @Nullable BlockEntity blockEntity) {
		if (skipped.isEmpty())
			return false;

		ItemRequirement requirement = ItemRequirement.of(state, blockEntity);
		if (requirement.isEmpty() || requirement.isInvalid())
			return false;

		for (StackRequirement stackRequirement : requirement.getRequiredItems()) {
			if (stackRequirement == null || stackRequirement.stack == null || stackRequirement.stack.isEmpty())
				continue;
			if (skipped.contains(stackRequirement.stack.getItem()))
				return true;
		}
		return false;
	}

	/**
	 * Prints a fresh skip-list clipboard for the given material checklist.
	 * <p>
	 * Unlike Create's normal read-only material list (which pre-checks already-gathered blocks), every entry here
	 * starts <b>unchecked</b> so the player can tick exactly the blocks the cannon should skip.
	 */
	public static ItemStack createSkipListClipboard(MaterialChecklist checklist) {
		ItemStack clipboard = AllBlocks.CLIPBOARD.asStack();

		// Unique set of every item the schematic needs (consumed + damaged).
		Set<Item> items = new LinkedHashSet<>();
		items.addAll(checklist.required.keySet());
		items.addAll(checklist.damageRequired.keySet());

		List<Item> sorted = new ArrayList<>(items);
		sorted.sort(Comparator.comparing(item -> item.getDescription()
			.getString()
			.toLowerCase(Locale.ENGLISH)));

		List<List<ClipboardEntry>> pages = new ArrayList<>();
		List<ClipboardEntry> currentPage = new ArrayList<>();
		int entriesOnPage = 0;

		for (Item item : sorted) {
			if (entriesOnPage == MAX_ENTRIES_PER_PAGE) {
				pages.add(currentPage);
				currentPage = new ArrayList<>();
				entriesOnPage = 0;
			}

			int amount = checklist.getRequiredAmount(item);

			MutableComponent text = Component.translatable(item.getDescriptionId());
			if (amount > 1)
				text.append(Component.literal("\n")
					.append(Component.literal("x" + amount).withStyle(ChatFormatting.GRAY)));

			currentPage.add(new ClipboardEntry(false, text).displayItem(new ItemStack(item), amount));
			entriesOnPage++;
		}

		pages.add(currentPage);
		ClipboardEntry.saveAll(pages, clipboard);
		ClipboardOverrides.switchTo(ClipboardType.WRITTEN, clipboard);

		CompoundTag tag = clipboard.getOrCreateTag();
		tag.putBoolean(MARKER, true);
		tag.putBoolean(READONLY, true);

		clipboard.getOrCreateTagElement("display")
			.putString("Name", Component.Serializer.toJson(Component
				.translatable("item.create_cannon_plus.schematic_skip_list")
				.setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)
					.withItalic(false))));
		return clipboard;
	}

}
