package me.padej_.animatedTyping.animation.types;

import me.padej_.animatedTyping.animation.AnimationStyle;
import me.padej_.animatedTyping.config.ConfigManager;
import net.minecraft.client.util.math.MatrixStack;

public class ScalingAnimation implements AnimationStyle {
    @Override
    public void applyLiveTransform(MatrixStack matrixStack, int cx, int textY, int charHeight, float scale) {
        float cy = textY + charHeight / 2f;

        matrixStack.push();
        matrixStack.translate(cx, cy, 0);
        matrixStack.scale(scale, scale, 1f);
        matrixStack.translate(-cx, -cy, 0);
    }

    @Override
    public void applyRemovedTransform(MatrixStack matrixStack, int leftX, int textY, int charHeight, float scale) {
        applyLiveTransform(matrixStack, leftX, textY, charHeight, scale);
    }

    @Override
    public float calculateScale(long appearTime, long currentTime) {
        float progress = Math.min((currentTime - appearTime) / ConfigManager.get.animationTime, 1f);
        return 0.5f + 0.5f * calculateEasedProgress(progress);
    }

    @Override
    public float calculateEasedProgress(float progress) {
        return (float)(1 - Math.pow(1 - progress, 3));
    }
}
