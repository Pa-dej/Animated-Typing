package me.padej_.animatedTyping.animation.types;

import me.padej_.animatedTyping.animation.AnimationStyle;
import me.padej_.animatedTyping.config.ConfigManager;
import org.joml.Matrix3x2fStack;

public class GrowAnimation implements AnimationStyle {
    @Override
    public void applyLiveTransform(Matrix3x2fStack matrixStack, int cx, int textY, int charHeight, float scale) {
        float baselineY = textY + charHeight;

        matrixStack.pushMatrix();
        matrixStack.translate(cx, baselineY);
        matrixStack.scale(1f, scale);
        matrixStack.translate(-cx, -baselineY);
    }

    @Override
    public void applyRemovedTransform(Matrix3x2fStack matrixStack, int leftX, int textY, int charHeight, float scale) {
        applyLiveTransform(matrixStack, leftX, textY, charHeight, scale);
    }

    @Override
    public float calculateScale(long appearTime, long currentTime) {
        float progress = Math.min((currentTime - appearTime) / ConfigManager.get.animationTime, 1f);
        return 0.5f + 0.5f * calculateEasedProgress(progress);
    }

    @Override
    public float calculateEasedProgress(float progress) {
        return (float) (1 - Math.pow(1 - progress, 3));
    }
}
