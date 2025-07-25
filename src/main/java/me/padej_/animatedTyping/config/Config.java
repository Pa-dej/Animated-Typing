package me.padej_.animatedTyping.config;

import me.padej_.animatedTyping.animation.AnimationType;

public class Config {
    public boolean enabled = true;
    public float animationTime = 180f;
    public AnimationType animationType = AnimationType.SCALING;

    public static Config of() {
        return new Config();
    }
}
