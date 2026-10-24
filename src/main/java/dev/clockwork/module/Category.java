package dev.clockwork.module;

public enum Category {
    RENDER("Render"), PLAYER("Player"), WORLD("World");
    public final String label;
    Category(String l) { label = l; }
}
