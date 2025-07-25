package me.padej_.animatedTyping.screen;

import me.padej_.animatedTyping.animation.AnimationType;
import me.padej_.animatedTyping.config.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {

    private TextFieldWidget textField;

    private ButtonWidget toggleButton;
    private ButtonWidget animationButton;

    public ConfigScreen() {
        super(Text.of("Animated Typing Options Screen"));
    }

    public static ConfigScreen open() {
        return new ConfigScreen();
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int y = 40;

        toggleButton = ButtonWidget.builder(getToggleButtonText(), button -> {
            ConfigManager.get.enabled = !ConfigManager.get.enabled;
            toggleButton.setMessage(getToggleButtonText());
            ConfigManager.save();
        }).position(centerX - 100, y).size(200, 20).build();
        addDrawableChild(toggleButton);
        y += 30;

        IntSliderWidget sliderWidget = new IntSliderWidget(centerX - 100, y, 200, 20, Text.of("Speed"),
                (int) ConfigManager.get.animationTime, 100, 1000, 50, value -> {
            ConfigManager.get.animationTime = value;
            ConfigManager.save();
        });
        addDrawableChild(sliderWidget);
        y += 30;

        animationButton = ButtonWidget.builder(Text.of("Animation: " + ConfigManager.get.animationType.name()), button -> {
            ConfigManager.get.animationType = nextAnimation(ConfigManager.get.animationType);
            animationButton.setMessage(Text.of("Animation: " + ConfigManager.get.animationType.name()));
            ConfigManager.save();
        }).position(centerX - 100, y).size(200, 20).build();
        addDrawableChild(animationButton);
        y += 30;

        textField = new TextFieldWidget(textRenderer, centerX - 100, y, 200, 20, Text.of(""));
        addSelectableChild(textField);

        ButtonWidget doneButton = ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
                .position(centerX - 100, height - 26)
                .size(200, 20)
                .build();
        addDrawableChild(doneButton);
    }

    private Text getToggleButtonText() {
        return Text.of(ConfigManager.get.enabled ? "Enabled" : "Disabled");
    }

    private AnimationType nextAnimation(AnimationType current) {
        AnimationType[] values = AnimationType.values();
        int nextOrdinal = (current.ordinal() + 1) % values.length;
        return values[nextOrdinal];
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawCenteredTextWithShadow(textRenderer, Text.of("Animated Typing Options Screen"), width / 2, 15, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
        textField.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        client.setScreen(new OptionsScreen(null, this.client.options));
    }

    private static class IntSliderWidget extends SliderWidget {
        private final int min;
        private final int max;
        private final int step;
        private final java.util.function.IntConsumer onChange;

        public IntSliderWidget(int x, int y, int width, int height, Text message,
                               int initialValue, int min, int max, int step, java.util.function.IntConsumer onChange) {
            super(x, y, width, height, message, 0);
            this.min = min;
            this.max = max;
            this.step = step;
            this.onChange = onChange;
            this.value = (double) (initialValue - min) / (max - min);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int intValue = getIntValue();
            setMessage(Text.of("Speed: " + intValue));
        }

        @Override
        protected void applyValue() {
            int intValue = getIntValue();
            onChange.accept(intValue);
        }

        public int getIntValue() {
            int intValue = (int) (min + (max - min) * this.value);
            return (intValue / step) * step;
        }
    }
}
