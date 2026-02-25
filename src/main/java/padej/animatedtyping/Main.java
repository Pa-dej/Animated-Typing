package padej.animatedtyping;

import padej.animatedtyping.config.ConfigManager;
import net.fabricmc.api.ModInitializer;

public class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        ConfigManager.load();
    }
}
