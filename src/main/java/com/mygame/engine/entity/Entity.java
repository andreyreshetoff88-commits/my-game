package com.mygame.engine.entity;

import com.mygame.engine.graphics.Renderer;
import com.mygame.engine.physics.PhysicsSystem;
import com.mygame.world.block.Block;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

import java.util.List;

@Getter
public abstract class Entity {
    protected Vector3f position = new Vector3f();
    protected Vector3f prevPosition = new Vector3f();
    protected Vector3f velocity = new Vector3f();
    protected Vector3f front = new Vector3f(0, 0, -1);
    protected Vector3f up = new Vector3f(0, 1, 0);
    protected Vector3f right = new Vector3f(1, 0, 0);
    protected PhysicsSystem physicsSystem = new PhysicsSystem();

    protected float yaw = -90.0f;
    protected float pitch = 0.0f;

    protected float height = 1.8f;
    protected float radius = 0.05f;
    @Setter
    protected boolean onGround = false;

    public abstract void update(float deltaTime, List<Block> nearbyBlocks);
    public abstract void render(Renderer renderer, Vector3f renderPos);
}
