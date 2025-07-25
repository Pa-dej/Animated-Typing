package me.padej_.animatedTyping.animation;

import me.padej_.animatedTyping.animation.types.*;
import me.padej_.animatedTyping.config.ConfigManager;
import me.padej_.animatedTyping.util.RemovedChar;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import org.joml.Matrix3x2fStack;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;

public class AnimationHandler {

    public void updateCharacters(String visibleText, String lastVisibleText,
                                 Map<Integer, Long> charTimestamps,
                                 Map<Integer, RemovedChar> removedChars,
                                 TextRenderer textRenderer, int textX) {
        long now = System.currentTimeMillis();
        int maxLen = Math.max(visibleText.length(), lastVisibleText.length());

        for (int i = 0; i < maxLen; i++) {
            boolean inVisible = i < visibleText.length();
            boolean inLast = i < lastVisibleText.length();

            char newChar = inVisible ? visibleText.charAt(i) : '\0';
            char oldChar = inLast ? lastVisibleText.charAt(i) : '\0';

            if (inVisible && (!inLast || newChar != oldChar)) {
                charTimestamps.put(i, now);
            }

            if (inLast && (!inVisible || newChar != oldChar)) {
                int widthUpTo = textRenderer.getWidth(lastVisibleText.substring(0, i));
                removedChars.put(i, new RemovedChar(oldChar, now, textX + widthUpTo));
            }
        }
    }

    public int renderLiveCharacters(DrawContext context, Matrix3x2fStack matrixStack, String visibleText,
                                    TextRenderer textRenderer, BiFunction<String, Integer, OrderedText> renderTextProvider,
                                    int firstCharacterIndex, Map<Integer, Long> charTimestamps, int textX, int textY,
                                    int textColor, boolean textShadow, int selectionStartRel) {
        int charX = textX;
        long now = System.currentTimeMillis();
        int newCursorX = textX;

        int charHeight = textRenderer.fontHeight;

        for (int i = 0; i < visibleText.length(); i++) {
            char c = visibleText.charAt(i);
            OrderedText ordered = renderTextProvider.apply(String.valueOf(c), firstCharacterIndex + i);

            long appear = charTimestamps.getOrDefault(i, now);
            float elapsed = (now - appear) / ConfigManager.get.animationTime;
            elapsed = Math.min(elapsed, 1f);

            if (elapsed < 1e-4f) continue;

            float scale = 1f;
            int colorToUse = textColor;

            AnimationStyle style = animationStyle();

            if (style instanceof FadeAnimation fade) {
                float alpha = fade.calculateEasedProgress(elapsed);
                colorToUse = fade.calculateAlphaColor(textColor, alpha);
            } else {
                scale = style.calculateScale(appear, now);
            }

            style.applyLiveTransform(matrixStack, charX, textY, charHeight, scale);
            context.drawText(textRenderer, ordered, charX, textY, colorToUse, textShadow);
            matrixStack.popMatrix();

            if (i < selectionStartRel) {
                newCursorX += textRenderer.getWidth(ordered);
            }

            charX += textRenderer.getWidth(ordered);
        }

        return newCursorX;
    }


    public void renderRemovedCharacters(DrawContext context, Matrix3x2fStack matrixStack,
                                        Map<Integer, RemovedChar> removedChars, TextRenderer textRenderer,
                                        BiFunction<String, Integer, OrderedText> renderTextProvider,
                                        int textY, int textColor, boolean textShadow) {
        long now = System.currentTimeMillis();
        int charHeight = textRenderer.fontHeight;

        Iterator<Map.Entry<Integer, RemovedChar>> iter = removedChars.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<Integer, RemovedChar> entry = iter.next();
            RemovedChar rc = entry.getValue();

            float t = (now - rc.timestamp()) / ConfigManager.get.animationTime;
            if (t >= 1.0f) {
                iter.remove();
                continue;
            }

            float progress = 1.0f - animationStyle().calculateEasedProgress(t);
            if (progress <= 0.01f) continue;

            OrderedText ordered = renderTextProvider.apply(String.valueOf(rc.ch()), entry.getKey());

            if (animationStyle() instanceof FadeAnimation) {
                int colorToUse = ((FadeAnimation) animationStyle()).calculateAlphaColor(textColor, progress);
                animationStyle().applyRemovedTransform(matrixStack, rc.x(), textY, charHeight, 1f);
                context.drawText(textRenderer, ordered, rc.x(), textY, colorToUse, textShadow);
                matrixStack.popMatrix();
            } else {
                animationStyle().applyRemovedTransform(matrixStack, rc.x(), textY, charHeight, progress);
                context.drawText(textRenderer, ordered, rc.x(), textY, textColor, textShadow);
                matrixStack.popMatrix();
            }
        }
    }

    private AnimationStyle animationStyle() {
        AnimationType at = ConfigManager.get.animationType;
        return switch (at) {
            case SCALING -> new ScalingAnimation();
            case GROW -> new GrowAnimation();
            case SCROLL -> new ScrollAnimation();
            case STRETCH -> new StretchAnimation();
            case FADE -> new FadeAnimation();
        };
    }
}