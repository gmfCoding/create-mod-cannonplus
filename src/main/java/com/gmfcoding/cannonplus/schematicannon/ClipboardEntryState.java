package com.gmfcoding.cannonplus.schematicannon;

/**
 * Duck interface mixed into Create's {@code ClipboardEntry}, adding a third checklist state
 * ("omitted") on top of Create's built-in checked / unchecked.
 * <p>
 * Create only stores a boolean ({@code Checked}); the third state is persisted separately as a
 * {@code CannonPlusState} NBT int by {@code ClipboardEntryMixin}, while {@code checked} is kept in
 * sync ({@code checked == COMPLETE}) so Create's own rendering and "clear checked" button are unaffected.
 */
public interface ClipboardEntryState {

	/** No tick - the cannon places this block normally. */
	int INCOMPLETE = 0;
	/** Green tick - marked as handled; still placed normally. */
	int COMPLETE = 1;
	/** Red cross - deliberately omitted; not placed. */
	int OMITTED = 2;

	int cannonplus$getState();

	void cannonplus$setState(int state);

	/** {@code Incomplete -> Complete -> Omitted -> Incomplete}. */
	static int next(int state) {
		return state >= OMITTED ? INCOMPLETE : state + 1;
	}
}
