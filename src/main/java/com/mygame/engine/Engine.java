package com.mygame.engine;

import com.mygame.engine.graphics.Renderer;
import com.mygame.engine.graphics.shader.ShaderManager;
import com.mygame.game.Game;
import com.mygame.world.World;

import java.util.concurrent.TimeUnit;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

/**
 * Engine - главный класс движка, управляет жизненным циклом приложения
 */
public class Engine {
    // Игра, которой управляет движок
    private final Game game;
    // Окно приложения
    private Window window;

    /**
     * Конструктор движка
     *
     * @param game объект игры для управления
     */
    public Engine(Game game) {
        this.game = game;
    }

    /**
     * Запускает игровой цикл
     */
    public void start() {
        // Создаем окно
        window = new Window(1280, 720, "Sandbox");

        // Инициализируем игру
        game.init(window);

        // Переменные для измерения времени между кадрами
        float lastTime = (float) glfwGetTime();

        // Основной игровой цикл
        while (!window.shouldClosed()) {
            // Вычисляем время, прошедшее с последнего кадра (delta time)
            float currentTime = (float) glfwGetTime();
            float deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            // Обновляем игру
            game.update(deltaTime);
            // Рендерим игру
            game.render();
            // Обновляем окно
            window.update();
        }
        // Очищаем ресурсы перед выходом
        cleanup(game.getWorld(), game.getRenderer());
        // Уничтожаем окно
        window.destroy();
    }

    /**
     * Очищает ресурсы перед завершением программы
     *
     * @param world    мир игры
     * @param renderer рендерер
     */
    private void cleanup(World world, Renderer renderer) {
        // Завершаем работу мира
        world.shutdown();
        try {
            // Ждем завершения потоков генерации чанков (максимум 2 секунды)
            if (!world.getChunkExecutor().awaitTermination(2, TimeUnit.SECONDS)) {
                // Если потоки не завершились, принудительно останавливаем их
                world.getChunkExecutor().shutdownNow();
            }
        } catch (InterruptedException e) {
            // В случае прерывания также принудительно останавливаем потоки
            world.getChunkExecutor().shutdownNow();
        }
        // Очищаем ресурсы рендера
        renderer.cleanup();
        // Очищаем ресурсы шейдеров
        ShaderManager.getInstance().cleanup();
        // Уничтожаем окно
        window.destroy();
    }
}
