package me.padej_.animatedTyping.animation.types;

import me.padej_.animatedTyping.animation.AnimationStyle;
import net.minecraft.client.util.math.MatrixStack;

public class FadeAnimation implements AnimationStyle {

    @Override
    public void applyLiveTransform(MatrixStack matrixStack, int leftX, int textY, int charHeight, float scale) {
    }

    @Override
    public void applyRemovedTransform(MatrixStack matrixStack, int leftX, int textY, int charHeight, float scale) {
    }

    @Override
    public float calculateScale(long appearTime, long currentTime) {
        return 1f; // Масштаб не меняется в fade
    }

    @Override
    public float calculateEasedProgress(float progress) {
        return (float) (1 - Math.pow(1 - progress, 3));
    }

    public int calculateAlphaColor(int baseColor, float alpha) {
        alpha = Math.max(alpha, 0.03f);
        int originalAlpha = (baseColor >> 24) & 0xFF;
        if (originalAlpha == 0) originalAlpha = 255;

        int newAlpha = Math.round(originalAlpha * alpha);
        return (baseColor & 0x00FFFFFF) | (newAlpha << 24);
    }
}
