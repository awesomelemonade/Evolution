package lemon.evolution;

import lemon.engine.glfw.GLFWInput;
import lemon.engine.math.*;
import lemon.evolution.util.GLFWGameControls;

public class FreeCamera implements CameraHolder {
    private static final float MOUSE_SENSITIVITY = .001f;
    private final MutableCamera camera;
    private final GLFWGameControls<EvolutionControls> controls;

    public FreeCamera(Camera camera, GLFWGameControls<EvolutionControls> controls) {
        this.camera = new MutableCamera(camera);
        this.controls = controls;
        controls.addCallback(GLFWInput::cursorDeltaEvent, event -> {
            if (controls.isActivated(EvolutionControls.CAMERA_ROTATE)) {
                float deltaY = (float) (-(event.x()) * MOUSE_SENSITIVITY);
                float deltaX = (float) (-(event.y()) * MOUSE_SENSITIVITY);
                this.camera.mutableRotation().asXYVector().add(deltaX, deltaY)
                        .clampX(-MathUtil.PI / 2f, MathUtil.PI / 2f).modY(MathUtil.TAU);
            }
        });
    }

    public void update() {
        float speed = .5f;
        float angle = (camera.rotation().y() + MathUtil.PI / 2f);
        float sin = (float) Math.sin(angle);
        float cos = (float) Math.cos(angle);
        var playerHorizontalVector = Vector2D.of(speed * sin, speed * cos);
        var mutableForce = MutableVector3D.ofZero();
        if (controls.isActivated(EvolutionControls.STRAFE_LEFT)) {
            mutableForce.asXZVector().subtract(playerHorizontalVector);
        }
        if (controls.isActivated(EvolutionControls.STRAFE_RIGHT)) {
            mutableForce.asXZVector().add(playerHorizontalVector);
        }
        var playerForwardVector = Vector2D.of(speed * cos, -speed * sin);
        if (controls.isActivated(EvolutionControls.MOVE_FORWARDS)) {
            mutableForce.asXZVector().add(playerForwardVector);
        }
        if (controls.isActivated(EvolutionControls.MOVE_BACKWARDS)) {
            mutableForce.asXZVector().subtract(playerForwardVector);
        }
        if (controls.isActivated(EvolutionControls.FLY)) {
            mutableForce.addY(speed);
        }
        if (controls.isActivated(EvolutionControls.FALL)) {
            mutableForce.subtractY(speed);
        }
        camera.mutablePosition().add(mutableForce.asImmutable());
    }

    public void setPositionAndRotation(Camera camera) {
        this.camera.setPositionAndRotation(camera);
    }

    @Override
    public Camera camera() {
        return camera;
    }
}
