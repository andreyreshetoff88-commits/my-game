package com.mygame.game;

import com.mygame.engine.InputHandler;
import com.mygame.engine.Window;
import com.mygame.engine.graphics.Camera;
import com.mygame.engine.graphics.Renderer;
import com.mygame.engine.physics.PhysicsSystem;
import com.mygame.world.World;
import lombok.Getter;

public abstract class Game {
    protected Camera camera;
    @Getter
    protected Renderer renderer;
    protected Window window;
    @Getter
    protected World world;
    protected InputHandler inputHandler;
    protected PhysicsSystem physicsSystem;

    public abstract void init(Window window);
    public abstract void update(float deltaTime);
    public abstract void render();
}
