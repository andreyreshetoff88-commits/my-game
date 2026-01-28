package com.mygame.engine.graphics.shader;

import java.util.HashMap;
import java.util.Map;

/**
 * Менеджер шейдеров - синглтон для управления всеми шейдерными программами
 * Позволяет централизованно управлять шейдерами во всем приложении
 */
public class ShaderManager {
    private static ShaderManager instance;
    private final Map<String, Shader> shaders;

    /**
     * Приватный конструктор - часть паттерна Singleton
     * Предотвращает создание экземпляров извне через new ShaderManager()
     */
    private ShaderManager() {
        shaders = new HashMap<>();
    }

    /**
     * Получает единственный экземпляр менеджера
     * Если экземпляр еще не создан - создает его
     *
     * @return единственный экземпляр ShaderManager
     */
    public static ShaderManager getInstance() {
        if (instance == null) {
            instance = new ShaderManager();
        }
        return instance;
    }

    /**
     * Добавляет шейдер в менеджер
     *
     * @param name   имя шейдера для последующего обращения
     * @param shader объект шейдера для хранения
     */
    public void addShader(String name, Shader shader) {
        shaders.put(name, shader);
    }

    /**
     * Получает шейдер по имени
     *
     * @param name имя шейдера
     * @return объект шейдера или null, если не найден
     */
    public Shader getShader(String name) {
        return shaders.get(name);
    }

    /**
     * Активирует шейдер по имени
     *
     * @param name имя шейдера для активации
     */
    public void bindShader(String name) {
        Shader shader = shaders.get(name);
        if (shader != null) {
            shader.bind();
        }
    }

    /**
     * Освобождает все ресурсы шейдеров
     * Вызывается при завершении программы
     */
    public void cleanup() {
        for (Shader shader : shaders.values()) {
            shader.cleanup();
        }
        shaders.clear();
    }
}
