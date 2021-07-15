package ru.baronessdev.lib.common.cloud.update;

@SuppressWarnings("unused")
public enum UpdateHandlerPriority {
    DEFAULT(1),
    STANDARD(2),
    MEDIUM(3),
    HIGH(4),
    MAXIMUM(5);

    private final int power;

    UpdateHandlerPriority(int power) {
        this.power = power;
    }

    public int getPower() {
        return power;
    }

    public boolean higher(UpdateHandlerPriority other) {
        return other.getPower() >= power;
    }
}
