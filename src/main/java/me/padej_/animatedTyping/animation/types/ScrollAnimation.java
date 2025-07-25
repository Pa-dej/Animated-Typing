package me.padej_.animatedTyping.animation.types;

import me.padej_.animatedTyping.animation.AnimationStyle;
import me.padej_.animatedTyping.config.ConfigManager;
import org.joml.Matrix3x2fStack;

public class ScrollAnimation implements AnimationStyle {
    @Override
    public void applyLiveTransform(Matrix3x2fStack matrixStack, int leftX, int baselineY, int charHeight, float scale) {

        matrixStack.pushMatrix();
        matrixStack.translate(leftX, baselineY);
        matrixStack.scale(1f, scale);
        matrixStack.translate(-leftX, -baselineY);
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
