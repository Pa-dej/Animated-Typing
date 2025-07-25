package me.padej_.animatedTyping.mixin;

import me.padej_.animatedTyping.animation.AnimationHandler;
import me.padej_.animatedTyping.config.ConfigManager;
import me.padej_.animatedTyping.util.RemovedChar;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
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
    @Shadow private int textX;
    @Shadow private int selectionEnd;
    @Shadow private long lastSwitchFocusTime;
    @Shadow private BiFunction<String, Integer, OrderedText> renderTextProvider;
    @Shadow private int textY;
    @Shadow private boolean textShadow;
    @Shadow protected abstract int getMaxLength();
    @Shadow @Nullable private Text placeholder;
    @Shadow @Nullable private String suggestion;

    @Unique private final Map<Integer, Long> charTimestamps = new HashMap<>();
    @Unique private String lastVisibleText = "";
    @Unique private final Map<Integer, RemovedChar> removedChars = new HashMap<>();
    @Unique private final AnimationHandler animationHandler = new AnimationHandler();

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void renderWidgetRecode(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (!ConfigManager.get.enabled) return;
        if (!isVisible()) return;

        Matrix3x2fStack matrixStack = context.getMatrices();

        if (drawsBackground()) {
            Identifier identifier = TEXTURES.get(this.isNarratable(), this.isFocused());
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }

        int textColor = this.editable ? this.editableColor : this.uneditableColor;
        int selectionStartRel = this.selectionStart - this.firstCharacterIndex;
        String visibleText = this.textRenderer.trimToWidth(this.text.substring(this.firstCharacterIndex), this.getInnerWidth());
        boolean isCursorWithInText = selectionStartRel >= 0 && selectionStartRel <= visibleText.length();
        boolean shouldDrawCursor = this.isFocused() && (Util.getMeasuringTimeMs() - this.lastSwitchFocusTime) / 300L % 2L == 0L && isCursorWithInText;
        int newCursorX;
        int selectionEndRel = MathHelper.clamp(this.selectionEnd - this.firstCharacterIndex, 0, visibleText.length());

        animationHandler.updateCharacters(visibleText, lastVisibleText, charTimestamps, removedChars, textRenderer, textX);

        newCursorX = animationHandler.renderLiveCharacters(context, matrixStack, visibleText, textRenderer, renderTextProvider,
                firstCharacterIndex, charTimestamps, textX, textY, textColor, textShadow, selectionStartRel);

        animationHandler.renderRemovedCharacters(context, matrixStack, removedChars, textRenderer, renderTextProvider,
                textY, textColor, textShadow);

        lastVisibleText = visibleText;

        boolean hasMoreTextOrFull = this.selectionStart < this.text.length() || this.text.length() >= this.getMaxLength();
        int suggestionX = newCursorX;

        if (!isCursorWithInText) {
            suggestionX = selectionStartRel > 0 ? this.textX + this.width : this.textX;
        }

        if (this.placeholder != null && visibleText.isEmpty() && !this.isFocused()) {
            context.drawTextWithShadow(this.textRenderer, this.placeholder, newCursorX, this.textY, textColor);
        }

        if (!hasMoreTextOrFull && this.suggestion != null) {
            context.drawText(this.textRenderer, this.suggestion, suggestionX - 1, this.textY, -8355712, this.textShadow);
        }

        if (selectionEndRel != selectionStartRel) {
            int selectionX = this.textX + this.textRenderer.getWidth(visibleText.substring(0, selectionEndRel));
            int selectionX1 = Math.min(suggestionX, this.getX() + this.width);
            int selectionY1 = this.textY - 1;
            int selectionX2 = Math.min(selectionX - 1, this.getX() + this.width);
            int selectionY2 = this.textY + 1;
            context.drawSelection(selectionX1, selectionY1, selectionX2, selectionY2 + 9);
        }

        if (shouldDrawCursor) {
            if (hasMoreTextOrFull) {
                int cursorY1 = this.textY - 1;
                int cursorX2 = suggestionX + 1;
                int cursorY2 = this.textY + 9;
                context.fill(suggestionX, cursorY1, cursorX2, cursorY2, -3092272);
            } else {
                context.drawText(this.textRenderer, "_", suggestionX, this.textY, textColor, this.textShadow);
            }
        }

        ci.cancel();
    }
}
