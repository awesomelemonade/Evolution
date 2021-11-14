package lemon.engine.game;

import lemon.engine.toolbox.Color;

public enum Team {
    RED(Color.RED), BLUE(Color.BLUE);

    private final Color color;

    private Team(Color color) {
        this.color = color;
    }

    public Color color() {
        return color;
    }
}
