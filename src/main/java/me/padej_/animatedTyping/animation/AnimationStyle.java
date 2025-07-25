package me.padej_.animatedTyping.animation;

import org.joml.Matrix3x2fStack;

public interface AnimationStyle {
    void applyLiveTransform(Matrix3x2fStack matrixStack, int leftX, int textY, int charHeight, float scale);
    void applyRemovedTransform(Matrix3x2fStack matrixStack, int leftX, int textY, int charHeight, float scale);

    float calculateScale(long appearTime, long currentTime);
    float calculateEasedProgress(float progress);
}
