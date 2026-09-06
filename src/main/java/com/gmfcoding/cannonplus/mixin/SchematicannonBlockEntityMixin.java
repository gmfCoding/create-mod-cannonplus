package com.gmfcoding.cannonplus.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gmfcoding.cannonplus.schematicannon.SkipListClipboards;
import com.simibubi.create.content.schematics.cannon.MaterialChecklist;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Gives the {@link SchematicannonBlockEntity} its block skip-list behaviour.
 * <ul>
 * <li>A clipboard printed as the material list becomes a tickable "skip list" instead of Create's read-only
 * pre-completed list.</li>
 * <li>While a skip-list clipboard sits in the list-printer slot it is not consumed/reprinted; its ticks are
 * read as the active configuration.</li>
 * <li>Blocks whose item is ticked are not placed while the cannon prints.</li>
 * </ul>
 */
// Create's own classes are not present in Minecraft's obfuscation maps, so mod-to-mod mixins
// must disable remapping (mirrors how createaddition etc. mixin into Create classes).
@Mixin(value = SchematicannonBlockEntity.class, remap = false)
public class SchematicannonBlockEntityMixin {

	/** Items ticked on the skip-list clipboard in slot 2, parsed once per server tick. */
	@Unique
	private Set<Item> cannonplus$skippedItems;

	@Unique
	private boolean cannonplus$hasSkipList;

	/**
	 * Runs every server tick at the head of {@code tickPaperPrinter}.
	 * <p>
	 * Refreshes the parsed skip list. When a skip-list clipboard occupies the list-printer slot we cancel the
	 * method so the clipboard is neither consumed nor overwritten - it is the active configuration. On first
	 * detection the checklist is recomputed so placed/counted totals already exclude skipped blocks.
	 */
	@Inject(method = "tickPaperPrinter", at = @At("HEAD"), cancellable = true)
	private void cannonplus$handleSkipListClipboard(CallbackInfo ci) {
		SchematicannonBlockEntity self = (SchematicannonBlockEntity) (Object) this;
		ItemStack clipboard = self.inventory.getStackInSlot(2);

		if (SkipListClipboards.isSkipListClipboard(clipboard)) {
			boolean firstSeen = !cannonplus$hasSkipList;
			cannonplus$hasSkipList = true;
			cannonplus$skippedItems = SkipListClipboards.collectSkipped(clipboard);

			if (firstSeen) {
				self.bookPrintingProgress = 0;
				self.dontUpdateChecklist = false;
				self.updateChecklist();
			}

			ci.cancel();
		} else {
			cannonplus$hasSkipList = false;
			cannonplus$skippedItems = null;
		}
	}

	/**
	 * Blocks whose required item is ticked on the active skip list are not placed (and are excluded from the
	 * checklist, keeping the printed counts in sync).
	 */
	@Inject(method = "shouldPlace", at = @At("HEAD"), cancellable = true)
	private void cannonplus$skipCheckedBlocks(BlockPos pos, BlockState state, BlockEntity be, BlockState toReplace,
		BlockState toReplaceOther, boolean isNormalCube, CallbackInfoReturnable<Boolean> cir) {
		if (cannonplus$skippedItems == null)
			return;
		if (SkipListClipboards.isBlockSkipped(cannonplus$skippedItems, state, be))
			cir.setReturnValue(false);
	}

	/**
	 * When the cannon prints the material list onto a clipboard, write our tickable skip list instead of Create's
	 * default one.
	 */
	@Redirect(method = "tickPaperPrinter",
		at = @At(value = "INVOKE",
			target = "Lcom/simibubi/create/content/schematics/cannon/MaterialChecklist;createWrittenClipboard()Lnet/minecraft/world/item/ItemStack;"))
	private ItemStack cannonplus$printSkipList(MaterialChecklist checklist) {
		return SkipListClipboards.createSkipListClipboard(checklist);
	}

}
