package com.mygame.engine;

import com.mygame.engine.graphics.Renderer;
import com.mygame.engine.graphics.shader.ShaderManager;
import com.mygame.game.Game;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Engine {
    private final Game game;
    private Window window;

    public Engine(Game game) {
        this.game = game;
    }

    public void start() {
        window = new Window(1280, 720, "Sandbox");

        game.init(window);

        float lastTime = (float) glfwGetTime();

        while (!window.shouldClosed()) {
            float currentTime = (float) glfwGetTime();
            float deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            game.update(deltaTime);
            game.render();
            window.update();
        }
        cleanup(game.getRenderer());
        window.destroy();
    }

    private void cleanup(Renderer renderer) {
        renderer.cleanup();
        ShaderManager.getInstance().cleanup();
        window.destroy();
    }
}
