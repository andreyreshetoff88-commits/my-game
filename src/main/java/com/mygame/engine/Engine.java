package com.mygame.engine;

import com.mygame.game.Game;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Engine {
    private final Game game;
    private Window window;

    public Engine(Game game) {
        this.game = game;
    }

    public void start(){
        window = new Window(1280, 720, "Sandbox");
        window.init();

        game.init(window);

        float lastTime = (float)glfwGetTime();

        while (!window.shouldClosed()){
            float currentTime = (float) glfwGetTime();
            float deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            game.update(deltaTime);
            game.render();
            window.update();
        }

        window.destroy();
    }
}
