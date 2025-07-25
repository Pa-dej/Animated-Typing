package me.padej_.animatedTyping.animation.types;

import me.padej_.animatedTyping.animation.AnimationStyle;
import org.joml.Matrix3x2fStack;

public class FadeAnimation implements AnimationStyle {

    @Override
    public void applyLiveTransform(Matrix3x2fStack matrixStack, int leftX, int textY, int charHeight, float scale) {
        matrixStack.pushMatrix();
    }

    @Override
    public void applyRemovedTransform(Matrix3x2fStack matrixStack, int leftX, int textY, int charHeight, float scale) {
        matrixStack.pushMatrix();
    }

    @Override
    public float calculateScale(long appearTime, long currentTime) {
        return 1f;
    }

    @Override
    public float calculateEasedProgress(float progress) {
        return (float)(1 - Math.pow(1 - progress, 3));
    }

//    public int calculateAlphaColor(int baseColor, float alpha) {
//        alpha = Math.min(Math.max(alpha, 0f), 1f);
//        int originalAlpha = (baseColor >> 24) & 0xFF;
//        int newAlpha = (int)(originalAlpha * alpha);
//        return (baseColor & 0x00FFFFFF) | (newAlpha << 24);
//    }

    public int calculateAlphaColor(int baseColor, float alpha) {
        alpha = Math.max(alpha, 0.03f);
        int originalAlpha = (baseColor >> 24) & 0xFF;
        if (originalAlpha == 0) originalAlpha = 255;

        int newAlpha = Math.round(originalAlpha * alpha);
        return (baseColor & 0x00FFFFFF) | (newAlpha << 24);
    }
}

