package com.mygame.engine.graphics;

import com.mygame.engine.entity.Player;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    @Setter
    @Getter
    private Vector3f position = new Vector3f();

    private final Vector3f front = new Vector3f();
    private final Vector3f up = new Vector3f(0, 1, 0);
    private final Vector3f right = new Vector3f();

    private float yaw;
    private float pitch;

    private final Matrix4f viewMatrix = new Matrix4f();

    public void update(Player player) {
        yaw = player.getYaw();
        pitch = player.getPitch();
        updateVectors();
    }

    private void updateVectors() {
        front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.normalize();

        right.set(front).cross(up).normalize();
    }

    public Matrix4f getViewMatrix() {
        Vector3f center = new Vector3f(position).add(front);
        return viewMatrix.setLookAt(position, center, up);
    }
}
