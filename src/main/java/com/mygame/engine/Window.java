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
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        assert videoMode != null;
        handle = glfwCreateWindow(videoMode.width(), videoMode.height(), title, 0, 0);
        if (handle == 0) {
            throw new RuntimeException("Не удалось создать GLFW окно");
        }
        glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwMakeContextCurrent(handle);
        createCapabilities();
        glfwSetFramebufferSizeCallback(handle, (window, width, height) -> updateViewport(width, height));
        int[] fbWidth = new int[1];
        int[] fbHeight = new int[1];
        glfwGetFramebufferSize(handle, fbWidth, fbHeight);
        updateViewport(fbWidth[0], fbHeight[0]);
        glEnable(GL_DEPTH_TEST);
    }

    private void updateViewport(int fbWidth, int fbHeight) {
        float scaleX = (float) fbWidth / width;
        float scaleY = (float) fbHeight / height;
        float scale = Math.min(scaleX, scaleY);
        int viewportWidth = (int) (width * scale);
        int viewportHeight = (int) (height * scale);
        int viewportX = (fbWidth - viewportWidth) / 2;
        int viewportY = (fbHeight - viewportHeight) / 2;
        glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
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
