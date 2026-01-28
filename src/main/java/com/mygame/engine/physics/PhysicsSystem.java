package com.mygame.engine.physics;

import com.mygame.engine.entity.Entity;
import com.mygame.world.Block;
import org.joml.Vector3f;

import java.util.List;

public class PhysicsSystem {
    private static final float GRAVITY = -9.8f;
    private static final float STEP_HEIGHT = 0.25f;

    public void update(float deltaTime, Entity entity, List<Block> blocks) {
        entity.getPrevPosition().set(entity.getPosition());
        applyGravity(deltaTime, entity, blocks);
        verticalCollision(entity, blocks);
        handleStep(entity, blocks);
    }

    public void applyGravity(float deltaTime, Entity entity, List<Block> blocks) {
        entity.getVelocity().y += GRAVITY * deltaTime;

        float newY = entity.getPosition().y + entity.getVelocity().y * deltaTime;
        float maxY = getMaxY(blocks, entity);

        if (newY <= maxY) {
            entity.getPosition().y = maxY;
            entity.getVelocity().y = 0;
            entity.setOnGround(true);
        } else {
            entity.getPosition().y = newY;
            entity.setOnGround(false);
        }
    }

    public void verticalCollision(Entity entity, List<Block> blocks) {
        if (entity.getVelocity().y > 0) {
            for (Block block : blocks) {
                if (block.position().y - entity.getHeight() >= entity.getPosition().y + entity.getRadius()) {
                    boolean insideX = entity.getPosition().x + entity.getRadius() >=
                            block.position().x - entity.getRadius() &&
                            entity.getPosition().x - entity.getRadius() <= block.position().x + entity.getRadius();
                    boolean insideZ = entity.getPosition().z + entity.getRadius() >=
                            block.position().z - entity.getRadius() &&
                            entity.getPosition().z - entity.getRadius() <= block.position().z + entity.getRadius();

                    if (!insideX || !insideZ) {
                        continue;
                    }

                    if (entity.getPosition().y + entity.getHeight() + 0.25f >= block.position().y - 0.25f) {
                        entity.getVelocity().y = 0;
                    }
                }
            }
        }
    }

    public void moveHorizontal(float dx, float dz, Entity entity, List<Block> blocks) {
        entity.getPosition().x += dx;
        if (collides(entity, blocks)) {
            entity.getPosition().x -= dx;
        }

        entity.getPosition().z += dz;
        if (collides(entity, blocks)) {
            entity.getPosition().z -= dz;
        }
    }

    private boolean collides(Entity entity, List<Block> blocks) {
        for (Block block : blocks) {
            if (intersects(block, entity)) {
                return true;
            }
        }
        return false;
    }

    private boolean intersects(Block block, Entity entity) {
        Vector3f bp = block.position();

        boolean overlapX = entity.getPosition().x + entity.getRadius() > bp.x - 0.25f &&
                entity.getPosition().x - entity.getRadius() < bp.x + 0.25f;

        boolean overlapZ = entity.getPosition().z + entity.getRadius() > bp.z - 0.25f &&
                entity.getPosition().z - entity.getRadius() < bp.z + 0.25f;

        boolean overlapY = entity.getPosition().y < bp.y + 0.25f &&
                entity.getPosition().y + entity.getHeight() > bp.y;

        return overlapX && overlapZ && overlapY;
    }

    public void handleStep(Entity entity, List<Block> blocks) {
        Vector3f moveDelta = new Vector3f(entity.getVelocity());
        moveDelta.y = 0;
        Vector3f newPos = new Vector3f(entity.getPosition()).add(moveDelta);

        float playerFeet = entity.getPosition().y;

        for (Block block : blocks) {
            Vector3f bp = block.position();

            boolean overlapX = newPos.x + entity.getRadius() > bp.x - 0.25f &&
                    newPos.x - entity.getRadius() < bp.x + 0.25f;
            boolean overlapZ = newPos.z + entity.getRadius() > bp.z - 0.25f &&
                    newPos.z - entity.getRadius() < bp.z + 0.25f;

            if (!overlapX || !overlapZ) continue;

            float blockTop = bp.y + 0.25f;
            float stepDiff = blockTop - playerFeet;

            if (stepDiff > 0 && stepDiff <= STEP_HEIGHT) {
                entity.getPosition().y += stepDiff;
                entity.setOnGround(true);
            } else if (stepDiff > STEP_HEIGHT) {
                newPos.x = entity.getPosition().x;
                newPos.z = entity.getPosition().z;
            }
        }

        entity.getPosition().x = newPos.x;
        entity.getPosition().z = newPos.z;
    }

    private static float getMaxY(List<Block> blocks, Entity entity) {
        float maxY = Float.NEGATIVE_INFINITY;

        for (Block block : blocks) {
            Vector3f bp = block.position();

            if (entity.getPosition().x + entity.getRadius() >=
                    bp.x - 0.25f && entity.getPosition().x - 0.25f <= bp.x + 0.25f &&
                    entity.getPosition().z + entity.getRadius() >=
                            bp.z - 0.25f && entity.getPosition().z - 0.25f <= bp.z + 0.25f) {

                float topY = bp.y + 0.25f;
                if (topY <= entity.getPosition().y + 0.1f && topY > maxY) {
                    maxY = topY;
                }
            }
        }
        return maxY;
    }
}
