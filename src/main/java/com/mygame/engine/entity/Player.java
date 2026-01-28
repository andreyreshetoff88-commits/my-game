package com.mygame.engine.entity;

import com.mygame.engine.graphics.Renderer;
import com.mygame.world.Block;
import lombok.Getter;
import org.joml.Vector3f;

import java.util.List;

@Getter
public class Player extends Entity {
    private final float mouseSensitivity = 0.1f;
    private final float moveSpeed = 3.0f;
    private final float jumpStrength = 4f;

    public Player(Vector3f startPosition) {
        this.position.set(startPosition);
        this.height = 0.9f;
        this.radius = 0.18f;
    }

    public void rotate(float xOffset, float yOffset) {
        xOffset *= mouseSensitivity;
        yOffset *= mouseSensitivity;

        yaw += xOffset;
        pitch += yOffset;

        if (pitch > 89f) pitch = 89f;
        if (pitch < -89f) pitch = -89f;
    }

    public Vector3f frontXZ() {
        return new Vector3f(
                (float) Math.cos(Math.toRadians(yaw)),
                0,
                (float) Math.sin(Math.toRadians(yaw))
        ).normalize();
    }

    public Vector3f rightXZ() {
        return new Vector3f(frontXZ()).cross(0, 1, 0).normalize();
    }

    public Vector3f getEyePosition() {
        return new Vector3f(
                position.x,
                position.y + height - 0.1f,
                position.z
        );
    }

    @Override
    public void update(float deltaTime, List<Block> nearbyBlocks) {
        physicsSystem.update(deltaTime, this, nearbyBlocks);
        updateVectors();
    }

    @Override
    public void render(Renderer renderer, Vector3f renderPos) {
        renderer.renderPlayer(radius, height, yaw, renderPos);
    }

    private void updateVectors() {
        front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.normalize();

        right.set(front).cross(up).normalize();
    }

    public void jump() {
        if (!onGround) {
            return;
        }
        velocity.y = jumpStrength;
        onGround = false;
    }
}
