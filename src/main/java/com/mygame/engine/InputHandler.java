package com.mygame.engine;

import com.mygame.engine.entity.Player;
import com.mygame.engine.physics.PhysicsSystem;
import com.mygame.world.Block;
import org.joml.Vector3f;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/**
 * InputHandler - обработчик ввода с клавиатуры и мыши
 * Преобразует нажатия клавиш и движение мыши в действия игрока
 */
public class InputHandler {
    // Окно, в котором происходит ввод
    private final Window window;
    // Игрок, которым управляем
    private final Player player;
    // Физическая система для обработки движения
    private final PhysicsSystem physicsSystem;

    // Флаг для отслеживания первого движения мыши
    private boolean firstMouse = true;
    // Последние координаты мыши для вычисления смещения
    private double lastX;
    private double lastY;

    /**
     * Конструктор обработчика ввода
     * @param window окно для получения ввода
     * @param player игрок, которым управляем
     * @param physicsSystem физическая система для обработки движения
     */
    public InputHandler(Window window, Player player, PhysicsSystem physicsSystem) {
        this.window = window;
        this.player = player;
        this.physicsSystem = physicsSystem;
        initMouse(); // Инициализируем обработку мыши
    }

    /**
     * Инициализирует обработку мыши
     * Устанавливает callback для отслеживания движения курсора
     */
    private void initMouse() {
        // Устанавливаем callback для обработки движения мыши
        glfwSetCursorPosCallback(window.getHandle(), (win, xpos, ypos) -> {
            // При первом движении мыши просто запоминаем позицию
            if (firstMouse) {
                lastX = xpos;
                lastY = ypos;
                firstMouse = false;
            }

            // Вычисляем смещение мыши относительно последней позиции
            float xOffset = (float) (xpos - lastX);
            float yOffset = (float) (lastY - ypos); // Инвертируем Y-ось

            // Сохраняем текущую позицию как последнюю
            lastX = xpos;
            lastY = ypos;

            // Передаем смещение в метод поворота игрока
            player.rotate(xOffset, yOffset);
        });

        // Отключаем видимый курсор и захватываем мышь в окне
        glfwSetInputMode(window.getHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    /**
     * Обрабатывает ввод с клавиатуры
     * @param deltaTime время с последнего обновления для плавного движения
     * @param blocks блоки для проверки движения
     */
    public void processInput(float deltaTime, List<Block> blocks) {
        // Вычисляем скорость движения с учетом времени
        float speed = player.getMoveSpeed() * deltaTime;

        // Переменные для накопления движения по осям
        float dx = 0;
        float dz = 0;

        // Обработка движения вперед (клавиша W)
        if (glfwGetKey(window.getHandle(), GLFW_KEY_W) == GLFW_PRESS) {
            // Получаем вектор направления взгляда (только XZ, без вертикали)
            Vector3f f = player.frontXZ();
            // Добавляем движение в направлении взгляда
            dx += f.x * speed;
            dz += f.z * speed;
        }

        // Обработка движения назад (клавиша S)
        if (glfwGetKey(window.getHandle(), GLFW_KEY_S) == GLFW_PRESS) {
            // Получаем вектор направления взгляда
            Vector3f f = player.frontXZ();
            // Добавляем движение в противоположном направлении
            dx -= f.x * speed;
            dz -= f.z * speed;
        }

        // Обработка движения влево (клавиша A)
        if (glfwGetKey(window.getHandle(), GLFW_KEY_A) == GLFW_PRESS) {
            // Получаем вектор "вправо" и двигаемся в противоположном направлении
            Vector3f r = player.rightXZ();
            dx -= r.x * speed;
            dz -= r.z * speed;
        }

        // Обработка движения вправо (клавиша D)
        if (glfwGetKey(window.getHandle(), GLFW_KEY_D) == GLFW_PRESS) {
            // Получаем вектор "вправо" и двигаемся в этом направлении
            Vector3f r = player.rightXZ();
            dx += r.x * speed;
            dz += r.z * speed;
        }

        // Применяем горизонтальное движение с проверкой коллизий
        physicsSystem.moveHorizontal(dx, dz, player, blocks);

        // Обработка прыжка (клавиша пробел)
        if (glfwGetKey(window.getHandle(), GLFW_KEY_SPACE) == GLFW_PRESS)
            player.jump();

        // Обработка выхода из игры (клавиша Escape)
        if (glfwGetKey(window.getHandle(), GLFW_KEY_ESCAPE) == GLFW_PRESS)
            glfwSetWindowShouldClose(window.getHandle(), true);
    }
}
