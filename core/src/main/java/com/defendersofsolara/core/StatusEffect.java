package com.defendersofsolara.core;

public class StatusEffect {
    public String type;
    public int value;
    public int duration;

    public StatusEffect(String type, int value, int duration) {
        this.type = type;
        this.value = value;
        this.duration = duration;
    }
}
