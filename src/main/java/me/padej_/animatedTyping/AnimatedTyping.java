package me.padej_.animatedTyping;

import me.padej_.animatedTyping.config.ConfigManager;
import net.fabricmc.api.ModInitializer;

public class AnimatedTyping implements ModInitializer {
    @Override
    public void onInitialize() {
        ConfigManager.load();
    }
}
