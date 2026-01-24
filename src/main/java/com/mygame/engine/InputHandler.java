package com.mygame.engine;

import com.mygame.engine.entity.Player;
import com.mygame.engine.physics.PhysicsSystem;
import com.mygame.world.Block;
import org.joml.Vector3f;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class InputHandler {
    private final Window window;
    private final Player player;
    private final PhysicsSystem physicsSystem;

    private boolean firstMouse = true;
    private double lastX;
    private double lastY;

    public InputHandler(Window window, Player player, PhysicsSystem physicsSystem) {
        this.window = window;
        this.player = player;
        this.physicsSystem = physicsSystem;
        initMouse();
    }

    private void initMouse() {
        glfwSetCursorPosCallback(window.getHandle(), (win, xpos, ypos) -> {
            if (firstMouse) {
                lastX = xpos;
                lastY = ypos;
                firstMouse = false;
            }

            float xOffset = (float) (xpos - lastX);
            float yOffset = (float) (lastY - ypos);

            lastX = xpos;
            lastY = ypos;

            player.rotate(xOffset, yOffset);
        });

        // Отключаем курсор
        glfwSetInputMode(window.getHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public void processInput(float deltaTime, List<Block> blocks) {
        float speed = player.getMoveSpeed() * deltaTime;

        float dx = 0;
        float dz = 0;

        if (glfwGetKey(window.getHandle(), GLFW_KEY_W) == GLFW_PRESS) {
            Vector3f f = player.frontXZ();
            dx += f.x * speed;
            dz += f.z * speed;
        }

        if (glfwGetKey(window.getHandle(), GLFW_KEY_S) == GLFW_PRESS) {
            Vector3f f = player.frontXZ();
            dx -= f.x * speed;
            dz -= f.z * speed;
        }

        if (glfwGetKey(window.getHandle(), GLFW_KEY_A) == GLFW_PRESS) {
            Vector3f r = player.rightXZ();
            dx -= r.x * speed;
            dz -= r.z * speed;
        }

        if (glfwGetKey(window.getHandle(), GLFW_KEY_D) == GLFW_PRESS) {
            Vector3f r = player.rightXZ();
            dx += r.x * speed;
            dz += r.z * speed;
        }

        physicsSystem.moveHorizontal(dx, dz, player, blocks);

        if (glfwGetKey(window.getHandle(), GLFW_KEY_SPACE) == GLFW_PRESS)
            player.jump();

        if (glfwGetKey(window.getHandle(), GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window.getHandle(), true);
    }
}
