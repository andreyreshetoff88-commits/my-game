package com.mygame.engine.physics;

import com.mygame.engine.entity.Entity;
import com.mygame.world.block.Block;
import org.joml.Vector3f;

import java.util.List;

public class PhysicsSystem {
    private static final float GRAVITY = -9.8f;
    private static final float STEP_HEIGHT = 0.25f;
    private static final float HALF_BLOCK = 0.25f;

    public void update(float deltaTime, Entity entity, List<Block> blocks) {
        entity.getPrevPosition().set(entity.getPosition());
        applyGravity(deltaTime, entity, blocks);
        verticalCollision(entity, blocks);
        handleStep(entity, blocks);
    }

    private boolean aabbIntersect(
            float minAx, float maxAx,
            float minAy, float maxAy,
            float minAz, float maxAz,
            float minBx, float maxBx,
            float minBy, float maxBy,
            float minBz, float maxBz
    ) {
        return maxAx > minBx && minAx < maxBx &&
                maxAy > minBy && minAy < maxBy &&
                maxAz > minBz && minAz < maxBz;
    }

    private void getBlockAABB(Block block, Vector3f min, Vector3f max) {
        Vector3f p = block.position();

        min.set(
                p.x - HALF_BLOCK,
                p.y,
                p.z - HALF_BLOCK
        );

        max.set(
                p.x + HALF_BLOCK,
                p.y + HALF_BLOCK,
                p.z + HALF_BLOCK
        );
    }

    private void getEntityAABB(Entity e, Vector3f min, Vector3f max) {
        min.set(
                e.getPosition().x - e.getRadius(),
                e.getPosition().y,
                e.getPosition().z - e.getRadius()
        );

        max.set(
                e.getPosition().x + e.getRadius(),
                e.getPosition().y + e.getHeight(),
                e.getPosition().z + e.getRadius()
        );
    }

    public void applyGravity(float deltaTime, Entity entity, List<Block> blocks) {
        entity.getVelocity().y += GRAVITY * deltaTime;

        float newY = entity.getPosition().y + entity.getVelocity().y * deltaTime;

        float groundedY = findGroundY(entity, blocks);

        if (newY <= groundedY) {
            entity.getPosition().y = groundedY;
            entity.getVelocity().y = 0;
            entity.setOnGround(true);
        } else {
            entity.getPosition().y = newY;
            entity.setOnGround(false);
        }
    }

    private float findGroundY(Entity entity, List<Block> blocks) {
        float best = Float.NEGATIVE_INFINITY;

        for (Block b : blocks) {
            Vector3f bp = b.position();

            boolean overlapX =
                    entity.getPosition().x + entity.getRadius() > bp.x - HALF_BLOCK &&
                            entity.getPosition().x - entity.getRadius() < bp.x + HALF_BLOCK;

            boolean overlapZ =
                    entity.getPosition().z + entity.getRadius() > bp.z - HALF_BLOCK &&
                            entity.getPosition().z - entity.getRadius() < bp.z + HALF_BLOCK;

            if (!overlapX || !overlapZ) continue;

            float top = bp.y + HALF_BLOCK;

            if (top <= entity.getPosition().y + 0.05f && top > best) {
                best = top;
            }
        }
        return best;
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
        Vector3f emin = new Vector3f();
        Vector3f emax = new Vector3f();
        Vector3f bmin = new Vector3f();
        Vector3f bmax = new Vector3f();

        getEntityAABB(entity, emin, emax);

        for (Block block : blocks) {
            getBlockAABB(block, bmin, bmax);

            if (aabbIntersect(
                    emin.x, emax.x,
                    emin.y, emax.y,
                    emin.z, emax.z,
                    bmin.x, bmax.x,
                    bmin.y, bmax.y,
                    bmin.z, bmax.z
            )) {
                return true;
            }
        }
        return false;
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
}
