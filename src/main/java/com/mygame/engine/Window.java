package com.mygame.engine;

import lombok.Getter;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;

public class Window {
    @Getter
    private final int width, height;
    private final String title;
    @Getter
    private long handle;

    public Window(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public void init() {
        if (!GLFW.glfwInit()) {
            System.out.println("Не удалось инициализировать GLFW");
            return;
        } else {
            System.out.println("GLFW прошла инициализацию");
        }
        handle = glfwCreateWindow(width, height, title, 0, 0);
        if (handle == 0) {
            throw new RuntimeException("Не удалось создать GLFW окно");
        }
        glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwMakeContextCurrent(handle);
        createCapabilities();
        glEnable(GL_DEPTH_TEST);
    }

    public boolean shouldClosed() {
        return glfwWindowShouldClose(handle);
    }

    public void update() {
        glfwSwapBuffers(handle);
        glfwPollEvents();
    }

    public void destroy() {
        glfwDestroyWindow(handle);
        glfwTerminate();
    }
}
