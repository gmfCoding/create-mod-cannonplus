package com.gmfcoding.cannonplus.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gmfcoding.cannonplus.schematicannon.ClipboardEntryState;
import com.gmfcoding.cannonplus.schematicannon.SkipListClipboards;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.clipboard.ClipboardScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;

/**
 * Client-side UI for the tri-state skip list: clicking an entry's checkbox cycles it
 * {@code Incomplete -> Complete -> Omitted}, and omitted entries are painted with a red cross.
 * <p>
 * Scoped to clipboards carrying the cannon skip-list marker, so ordinary Create clipboards keep their
 * standard two-state checkboxes.
 */
@Mixin(value = ClipboardScreen.class, remap = false)
public class ClipboardScreenMixin {

	@Shadow
	public ItemStack item;

	@Shadow
	List<ClipboardEntry> currentEntries;

	@Shadow
	int hoveredEntry;

	@Shadow
	boolean hoveredCheck;

	@Shadow
	int editingIndex;

	@Shadow
	private void sendIfEditingBlock() {
		throw new AssertionError();
	}

	/**
	 * Cycles the hovered checkbox instead of Create's plain checked/unchecked toggle.
	 * <p>
	 * {@code mouseClicked} overrides {@code Screen#mouseClicked}, so its name is obfuscated in production.
	 * Both the dev and production names are listed as aliases (with {@code remap = false}) so the injection
	 * resolves in either environment.
	 */
	@Inject(method = { "mouseClicked(DDI)Z", "m_6375_(DDI)Z" }, remap = false,
		at = @At("HEAD"), cancellable = true)
	private void cannonplus$cycleState(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (button != 0)
			return;
		if (!SkipListClipboards.isSkipListClipboard(item))
			return;
		if (!hoveredCheck || hoveredEntry < 0 || hoveredEntry >= currentEntries.size())
			return;

		ClipboardEntry entry = currentEntries.get(hoveredEntry);
		if (!(entry instanceof ClipboardEntryState state))
			return;

		editingIndex = -1;
		int next = ClipboardEntryState.next(state.cannonplus$getState());
		state.cannonplus$setState(next);
		cannonplus$playToggleSound(next);
		sendIfEditingBlock();
		cir.setReturnValue(true);
	}

	/**
	 * Redirects the checkbox glyph draw (the first {@code drawString} of each entry) so an omitted entry
	 * also gets a red cross. {@code drawString} is a vanilla method, so the target is remapped.
	 */
	@Redirect(method = "renderWindow", remap = false,
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I",
			ordinal = 0, remap = true))
	private int cannonplus$paintOmitted(GuiGraphics graphics, Font font, String text, int x, int y, int color,
		boolean dropShadow, @Local ClipboardEntry entry) {
		int width = graphics.drawString(font, text, x, y, color, dropShadow);
		if (entry instanceof ClipboardEntryState state && state.cannonplus$getState() == ClipboardEntryState.OMITTED)
			graphics.drawString(font, "\u2718", x, y - 1, 0xFFE04B4B, false);
		return width;
	}

	private static void cannonplus$playToggleSound(int state) {
		SoundEvent event = (state == ClipboardEntryState.COMPLETE
			? AllSoundEvents.CLIPBOARD_CHECKMARK
			: AllSoundEvents.CLIPBOARD_ERASE).getMainEvent();
		float pitch = 0.95f + (float) Math.random() * 0.05f;
		Minecraft.getInstance()
			.getSoundManager()
			.play(SimpleSoundInstance.forUI(event, pitch));
	}
}
