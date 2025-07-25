package me.padej_.animatedTyping.animation;

import net.minecraft.client.util.math.MatrixStack;

public interface AnimationStyle {
    void applyLiveTransform(MatrixStack matrices, int leftX, int textY, int charHeight, float scale);
    void applyRemovedTransform(MatrixStack matrices, int leftX, int textY, int charHeight, float scale);

    float calculateScale(long appearTime, long currentTime);
    float calculateEasedProgress(float progress);
}
