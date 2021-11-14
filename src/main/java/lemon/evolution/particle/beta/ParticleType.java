package lemon.evolution.particle.beta;

public enum ParticleType {
    FIRE("fire_01.png"),
    SMOKE("smoke_01.png");

    private final String filename;

    private ParticleType(String filename) {
        this.filename = filename;
    }

    public String filename() {
        return filename;
    }
}
