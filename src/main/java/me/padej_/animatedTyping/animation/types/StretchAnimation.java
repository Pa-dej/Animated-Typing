package me.padej_.animatedTyping.animation.types;

import me.padej_.animatedTyping.animation.AnimationStyle;
import me.padej_.animatedTyping.config.ConfigManager;
import org.joml.Matrix3x2fStack;


public class StretchAnimation implements AnimationStyle {
    @Override
    public void applyLiveTransform(Matrix3x2fStack matrixStack, int leftX, int textY, int charHeight, float scale) {
        matrixStack.pushMatrix();
        matrixStack.translate(leftX, textY);
        matrixStack.scale(scale, 1f);
        matrixStack.translate(-leftX, -textY);
    }

    @Override
    public void applyRemovedTransform(Matrix3x2fStack matrixStack, int leftX, int textY, int charHeight, float scale) {
        matrixStack.pushMatrix();
        matrixStack.translate(leftX, textY);
        matrixStack.scale(scale, 1f);
        matrixStack.translate(-leftX, -textY);
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

