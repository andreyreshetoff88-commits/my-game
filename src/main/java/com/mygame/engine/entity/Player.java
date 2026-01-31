package com.mygame.engine.entity;

import com.mygame.engine.graphics.Renderer;
import com.mygame.world.Block;
import com.mygame.world.Chunk;
import com.mygame.world.World;
import lombok.Getter;
import org.joml.Vector3f;

import java.util.List;

@Getter
public class Player extends Entity {
    private static final float REACH_DISTANCE = 8.0f;
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

    private Block getTargetBlock(World world) {
        Vector3f origin = getEyePosition();
        Vector3f direction = new Vector3f(front).normalize();

        float step = 0.1f;
        for (float t = 0; t < REACH_DISTANCE; t += step) {
            Vector3f pos = new Vector3f(origin).fma(t, direction); // origin + direction * t

            List<Block> nearby = world.getNearbyBlocks(pos);
            for (Block block : nearby) {
                Vector3f bp = block.position(); // это уже мировая позиция блока
                float half = Chunk.BLOCK_SIZE / 2f;

                if (pos.x >= bp.x - half && pos.x <= bp.x + half &&
                        pos.y >= bp.y && pos.y <= bp.y + Chunk.BLOCK_SIZE &&
                        pos.z >= bp.z - half && pos.z <= bp.z + half) {
                    return block;
                }
            }
        }

        return null; // ничего не найдено
    }

    public void punchRightHand(World world) {
        Block block = getTargetBlock(world);
        if (block != null) {
            System.out.println(block.position());
            world.destroyBlock(block);
        }
    }

    public void jump() {
        if (!onGround) {
            return;
        }
        velocity.y = jumpStrength;
        onGround = false;
    }
}
