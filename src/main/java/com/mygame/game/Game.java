package com.mygame.game;

import com.mygame.engine.InputHandler;
import com.mygame.engine.Window;
import com.mygame.world.World;
import com.mygame.engine.entity.Player;
import com.mygame.engine.graphics.Camera;
import com.mygame.engine.graphics.Renderer;
import com.mygame.engine.physics.PhysicsSystem;

public abstract class Game {
    protected Camera camera;
    protected Renderer renderer;
    protected Window window;
    protected World world;
    protected InputHandler inputHandler;
    protected PhysicsSystem physicsSystem;

    public abstract void init(Window window);
    public abstract void update(float deltaTime);
    public abstract void render();
}
