package me.padej_.animatedTyping.mixin;

import me.padej_.animatedTyping.animation.AnimationHandler;
import me.padej_.animatedTyping.config.ConfigManager;
import me.padej_.animatedTyping.util.RemovedChar;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin extends ClickableWidget {

    public TextFieldWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Shadow public abstract boolean isVisible();
    @Shadow public abstract boolean drawsBackground();
    @Shadow @Final private static ButtonTextures TEXTURES;
    @Shadow private boolean editable;
    @Shadow private int editableColor;
    @Shadow private int uneditableColor;
    @Shadow private int selectionStart;
    @Shadow private int firstCharacterIndex;
    @Shadow @Final private TextRenderer textRenderer;
    @Shadow private String text;
    @Shadow public abstract int getInnerWidth();
    @Shadow private int selectionEnd;
    @Shadow private long lastSwitchFocusTime;
    @Shadow private BiFunction<String, Integer, OrderedText> renderTextProvider;
    @Shadow protected abstract int getMaxLength();
    @Shadow @Nullable private Text placeholder;
    @Shadow @Nullable private String suggestion;

    @Shadow
    protected abstract void drawSelectionHighlight(DrawContext context, int x1, int y1, int x2, int y2);

    @Unique private final Map<Integer, Long> charTimestamps = new HashMap<>();
    @Unique private String lastVisibleText = "";
    @Unique private final Map<Integer, RemovedChar> removedChars = new HashMap<>();
    @Unique private final AnimationHandler animationHandler = new AnimationHandler();

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void renderWidgetRecode(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (!ConfigManager.get.enabled) return;
        if (!isVisible()) return;

        MatrixStack matrixStack = context.getMatrices();

        if (this.drawsBackground()) {
            Identifier backgroundTexture = TEXTURES.get(this.isNarratable(), this.isFocused());
            context.drawGuiTexture(RenderLayer::getGuiTextured, backgroundTexture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }

        int textColor = this.editable ? this.editableColor : this.uneditableColor;
        int cursorIndexRelative = this.selectionStart - this.firstCharacterIndex;
        String visibleText = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());

        boolean isCursorVisibleInBounds = cursorIndexRelative >= 0 && cursorIndexRelative <= visibleText.length();
        boolean showCursor = this.isFocused() && (Util.getMeasuringTimeMs() - this.lastSwitchFocusTime) / 300L % 2L == 0L && isCursorVisibleInBounds;

        int textX = this.drawsBackground() ? this.getX() + 4 : this.getX();
        int textY = this.drawsBackground() ? this.getY() + (this.getHeight() - 8) / 2 : this.getY();
        int selectionEndRelative = MathHelper.clamp(this.selectionEnd - this.firstCharacterIndex, 0, visibleText.length());

        // Обновляем анимации новых и удалённых символов
        animationHandler.updateCharacters(visibleText, lastVisibleText, charTimestamps, removedChars, textRenderer, textX);

        // Отрисовываем появляющиеся символы
        int newCursorX = animationHandler.renderLiveCharacters(context, matrixStack, visibleText, textRenderer, renderTextProvider,
                firstCharacterIndex, charTimestamps, textX, textY, textColor, true, cursorIndexRelative);

        // Отрисовываем исчезающие символы
        animationHandler.renderRemovedCharacters(context, matrixStack, removedChars, textRenderer, renderTextProvider,
                textY, textColor, true);

        lastVisibleText = visibleText;

        boolean hasMoreText = this.selectionStart < this.text.length() || this.text.length() >= this.getMaxLength();
        int cursorX = newCursorX;

        if (!isCursorVisibleInBounds) {
            cursorX = cursorIndexRelative > 0 ? textX + this.width : textY;
        }

        if (this.placeholder != null && visibleText.isEmpty() && !this.isFocused()) {
            context.drawTextWithShadow(this.textRenderer, this.placeholder, cursorX, textY, textColor);
        }

        if (!hasMoreText && this.suggestion != null) {
            context.drawTextWithShadow(this.textRenderer, this.suggestion, cursorX - 1, textY, -8355712);
        }

        if (selectionEndRelative != cursorIndexRelative) {
            int selectionX = textX + this.textRenderer.getWidth(visibleText.substring(0, selectionEndRelative));
            int selectionTop = textY - 1;
            int selectionRight = selectionX - 1;
            int selectionBottom = textY + 1;
            this.drawSelectionHighlight(context, cursorX, selectionTop, selectionRight, selectionBottom + 9);
        }

        if (showCursor) {
            if (hasMoreText) {
                RenderLayer overlayLayer = RenderLayer.getGuiOverlay();
                int cursorTop = textY - 1;
                int cursorRight = cursorX + 1;
                int cursorBottom = textY + 1;
                context.fill(overlayLayer, cursorX, cursorTop, cursorRight, cursorBottom + 9, -3092272);
            } else {
                context.drawTextWithShadow(this.textRenderer, "_", cursorX, textY, textColor);
            }
        }

        ci.cancel();
    }

}
