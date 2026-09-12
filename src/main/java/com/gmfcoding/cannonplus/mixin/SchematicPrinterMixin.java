package com.gmfcoding.cannonplus.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gmfcoding.cannonplus.schematicannon.SchematicPrinterState;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.SchematicPrinter.PrintStage;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Applies the cannon's skip list to <b>entity</b> placements too.
 * <p>
 * {@code shouldPlaceCurrent} short-circuits to {@code true} for the ENTITIES print stage without invoking the
 * cannon's placement predicate, so entity-based materials (notably Super Glue) would otherwise always be
 * placed. We gate them here, before their items are consumed, using the skip list the cannon pushes onto the
 * printer.
 */
@Mixin(value = SchematicPrinter.class, remap = false)
public class SchematicPrinterMixin implements SchematicPrinterState {

	@Unique
	private Set<Item> cannonplus$skippedItems;

	@Override
	public Set<Item> cannonplus$getSkippedItems() {
		return cannonplus$skippedItems;
	}

	@Override
	public void cannonplus$setSkippedItems(Set<Item> skippedItems) {
		cannonplus$skippedItems = skippedItems;
	}

	@Inject(
		method = "shouldPlaceCurrent(Lnet/minecraft/world/level/Level;Lcom/simibubi/create/content/schematics/SchematicPrinter$PlacementPredicate;)Z",
		at = @At("HEAD"), cancellable = true)
	private void cannonplus$skipEntities(Level world, SchematicPrinter.PlacementPredicate predicate,
		CallbackInfoReturnable<Boolean> cir) {
		Set<Item> skipped = cannonplus$skippedItems;
		if (skipped == null || skipped.isEmpty())
			return;

		SchematicPrinter self = (SchematicPrinter) (Object) this;
		if (self.getPrintStage() != PrintStage.ENTITIES)
			return;

		ItemRequirement requirement = self.getCurrentRequirement();
		if (requirement == null || requirement.isEmpty() || requirement.isInvalid())
			return;

		for (ItemRequirement.StackRequirement required : requirement.getRequiredItems()) {
			if (required.stack == null || required.stack.isEmpty())
				continue;
			if (skipped.contains(required.stack.getItem())) {
				cir.setReturnValue(false);
				return;
			}
		}
	}

	/**
	 * While the belt connector is omitted, report belt segments as needing only a shaft, so the cannon does not
	 * try to gather (or stall on) belt connectors for segments that will be placed as pulley shafts.
	 */
	@Redirect(
		method = "getCurrentRequirement",
		at = @At(value = "INVOKE",
			target = "Lcom/simibubi/create/content/schematics/requirement/ItemRequirement;of(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)Lcom/simibubi/create/content/schematics/requirement/ItemRequirement;"))
	private ItemRequirement cannonplus$shaftOnlyForSkippedBelt(BlockState state, BlockEntity blockEntity) {
		ItemRequirement requirement = ItemRequirement.of(state, blockEntity);
		Set<Item> skipped = cannonplus$skippedItems;
		if (skipped != null && !skipped.isEmpty() && AllBlocks.BELT.has(state)
			&& skipped.contains(state.getBlock()
				.asItem()))
			return ItemRequirement.of(AllBlocks.SHAFT.getDefaultState(), null);
		return requirement;
	}
}
