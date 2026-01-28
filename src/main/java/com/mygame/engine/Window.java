package com.mygame.engine;

import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

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
        init();
    }

    public void init() {
        if (!GLFW.glfwInit()) {
            System.out.println("Не удалось инициализировать GLFW");
            return;
        } else {
            System.out.println("GLFW прошла инициализацию");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);

        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        assert videoMode != null;

        handle = glfwCreateWindow(videoMode.width(), videoMode.height(), title, 0, 0);

        if (handle == 0) {
            throw new RuntimeException("Не удалось создать GLFW окно");
        }

        glfwMakeContextCurrent(handle);
        createCapabilities();
        glfwSwapInterval(1);

        glfwSetFramebufferSizeCallback(handle, (window, width, height) -> {
            glViewport(0, 0, width, height);
        });

        int[] fbWidth = new int[1];
        int[] fbHeight = new int[1];
        glfwGetFramebufferSize(handle, fbWidth, fbHeight);
        glViewport(0, 0, fbWidth[0], fbHeight[0]);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);
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
