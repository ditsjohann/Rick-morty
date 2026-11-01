package com.ditsjohann.mayhem;

import com.ditsjohann.mayhem.event.ModEvents;
import com.ditsjohann.mayhem.registry.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Multiverse Mayhem — an interdimensional mad-science mod.
 * Portal guns, sentient toasters, bouncy jelly worlds, a dimension made of meat,
 * bureaucrats who audit your portal usage and a very large, very angry eyeball.
 */
public class MultiverseMayhem implements ModInitializer {
    public static final String MOD_ID = "mayhem";
    public static final Logger LOGGER = LoggerFactory.getLogger("Multiverse Mayhem");

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModSounds.init();
        ModComponents.init();
        ModEffects.init();
        ModBlocks.init();
        ModEntities.init();
        ModItems.init();
        ModItemGroups.init();
        ModFeatures.init();
        ModAttachments.init();
        ModEvents.init();
        LOGGER.info("Multiverse Mayhem loaded. Reality is now optional.");
    }
}
