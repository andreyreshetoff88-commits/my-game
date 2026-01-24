package com.mygame.game;

import com.mygame.engine.InputHandler;
import com.mygame.engine.Window;
import com.mygame.engine.graphics.Camera;
import com.mygame.engine.graphics.Renderer;
import com.mygame.engine.physics.PhysicsSystem;
import com.mygame.world.World;
import org.joml.Vector3f;

public class SandboxGame extends Game {
    private float physicsTick = 1.0f / 60.0f;
    private float accumulator = 0;

    @Override
    public void init(Window window) {
        this.window = window;
        camera = new Camera();
        renderer = new Renderer();
        world = new World(renderer);

        physicsSystem = new PhysicsSystem();
        inputHandler = new InputHandler(window, world.getPlayer(), physicsSystem);
    }

    @Override
    public void update(float deltaTime) {
        accumulator += deltaTime;
        while (accumulator >= physicsTick) {
            physicsSystem.update(physicsTick, world.getPlayer(), world.getNearbyBlocks(world.getPlayer().getPosition()));
            accumulator -= physicsTick;
        }
        inputHandler.processInput(deltaTime, world.getNearbyBlocks(world.getPlayer().getPosition()));
        camera.setPosition(world.getPlayer().getEyePosition());
        world.update(deltaTime, renderer);
        camera.update(world.getPlayer());
    }

    @Override
    public void render() {
        float alpha = accumulator / physicsTick;
        Vector3f renderPos = new Vector3f(world.getPlayer().getPrevPosition())
                .lerp(world.getPlayer().getPosition(), alpha);

        renderer.beginScene(camera, window.getWidth(), window.getHeight());
        world.render(renderer, renderPos);
    }
}
