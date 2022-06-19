package lemon.engine.math;

public enum EulerAnglesConvention {
    YAW_PITCH_ROLL,
    ROLL_PITCH_YAW;

    public EulerAnglesConvention inverse() {
        return switch (this) {
            case YAW_PITCH_ROLL -> ROLL_PITCH_YAW;
            case ROLL_PITCH_YAW -> YAW_PITCH_ROLL;
        };
    }
}
