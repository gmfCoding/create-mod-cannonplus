package com.gmfcoding.cannonplus.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gmfcoding.cannonplus.schematicannon.ClipboardEntryState;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;

import net.minecraft.nbt.CompoundTag;

/**
 * Adds the cannon's third clipboard state and persists it.
 * <p>
 * {@code Checked} is kept in sync with the state (only {@link #COMPLETE} counts as checked) so Create's
 * own logic is unaffected; the omitted state rides along in a {@code CannonPlusState} tag.
 */
@Mixin(value = ClipboardEntry.class, remap = false)
public class ClipboardEntryMixin implements ClipboardEntryState {

	@Shadow
	public boolean checked;

	@Unique
	private int cannonplus$state = INCOMPLETE;

	@Override
	public int cannonplus$getState() {
		return cannonplus$state;
	}

	@Override
	public void cannonplus$setState(int state) {
		cannonplus$state = state;
		checked = state == COMPLETE;
	}

	@Inject(method = "writeNBT", at = @At("TAIL"))
	private void cannonplus$writeState(CallbackInfoReturnable<CompoundTag> cir) {
		if (cannonplus$state != INCOMPLETE)
			cir.getReturnValue()
				.putInt("CannonPlusState", cannonplus$state);
	}

	@Inject(method = "readNBT", at = @At("RETURN"))
	private static void cannonplus$readState(CompoundTag tag, CallbackInfoReturnable<ClipboardEntry> cir) {
		ClipboardEntry entry = cir.getReturnValue();
		if (entry == null)
			return;
		int state = tag.contains("CannonPlusState")
			? tag.getInt("CannonPlusState")
			: (entry.checked ? COMPLETE : INCOMPLETE);
		((ClipboardEntryState) entry).cannonplus$setState(state);
	}
}
