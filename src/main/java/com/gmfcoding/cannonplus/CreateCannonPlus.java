package com.gmfcoding.cannonplus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;

/**
 * Create: Cannon Plus.
 * <p>
 * Gives the Create Schematicannon a configurable <b>block skip list</b>.
 * Insert a clipboard into the cannon's list-printer slot to print every block the loaded schematic needs,
 * tick the entries you want skipped, then reinsert the clipboard - the cannon will leave those blocks alone.
 */
@Mod(CreateCannonPlus.MOD_ID)
public class CreateCannonPlus {

	public static final String MOD_ID = "create_cannon_plus";
	public static final String NAME = "Create: Cannon Plus";

	public static final Logger LOGGER = LogManager.getLogger(NAME);

	public CreateCannonPlus() {
		LOGGER.info("{} initialized. The Schematicannon can now skip blocks via a clipboard.", NAME);
	}

}
