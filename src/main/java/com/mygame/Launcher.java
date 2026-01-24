package com.mygame;


import com.mygame.engine.Engine;
import com.mygame.engine.graphics.Camera;
import com.mygame.game.SandboxGame;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Launcher {

    public static void main(String[] args) {
        new Engine(new SandboxGame()).start();
    }
}