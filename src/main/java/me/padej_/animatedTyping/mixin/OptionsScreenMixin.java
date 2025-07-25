package me.padej_.animatedTyping.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.padej_.animatedTyping.screen.ConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {

    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;",
                    ordinal = 9
            )
    )
    private void addCustomButton(CallbackInfo ci, @Local GridWidget.Adder adder) {

        adder.add(ButtonWidget.builder(
                Text.literal("Animated Typing"),
                button -> MinecraftClient.getInstance().setScreen(
                        ConfigScreen.open()
                )
        ).width(150).build());
    }
}
