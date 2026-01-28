package com.mygame.engine;

import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;

/**
 * Window - класс для управления окном приложения
 * В современном OpenGL требует настройки Core Profile и версии контекста
 */
public class Window {
    // Ширина и высота окна
    @Getter
    private final int width, height;
    // Заголовок окна
    private final String title;
    // Handle (идентификатор) окна в GLFW
    @Getter
    private long handle;

    /**
     * Конструктор окна
     *
     * @param width  ширина окна
     * @param height высота окна
     * @param title  заголовок окна
     */
    public Window(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
        init(); // Инициализируем окно
    }

    /**
     * Инициализация окна и OpenGL контекста
     */
    public void init() {
        // Инициализируем GLFW - библиотеку для создания окон и управления вводом
        if (!GLFW.glfwInit()) {
            System.out.println("Не удалось инициализировать GLFW");
            return;
        } else {
            System.out.println("GLFW прошла инициализацию");
        }

        // ====== Настройка контекста OpenGL 3.3 Core Profile ======
        // Устанавливаем версию OpenGL 3.3 (современный функционал)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3); //_major = основная версия (3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3); // _minor = дополнительная версия (3)

        // Устанавливаем Core Profile - только современный функционал
        // (без устаревших функций типа glBegin/glEnd)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // Включаем поддержку forward compatibility (совместимость с будущими версиями)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        // Отключаем декорации окна (рамку) - опционально
        glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);

        // Получаем режим видео основного монитора для полноэкранного режима
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        assert videoMode != null; // Проверяем, что режим получен

        // Создаем окно с размерами экрана
        handle = glfwCreateWindow(videoMode.width(), videoMode.height(), title, 0, 0);

        // Проверяем успешность создания окна
        if (handle == 0) {
            throw new RuntimeException("Не удалось создать GLFW окно");
        }

        // Делаем контекст окна текущим (активным) для OpenGL вызовов
        glfwMakeContextCurrent(handle);

        // Создаем возможности OpenGL (инициализируем LWJGL OpenGL биндинги)
        createCapabilities();

        // Включаем вертикальную синхронизацию (V-Sync) для предотвращения tearing
        glfwSwapInterval(1);

        // Устанавливаем callback для изменения размера framebuffer'а
        glfwSetFramebufferSizeCallback(handle, (window, width, height) -> {
            // При изменении размера окна обновляем viewport
            glViewport(0, 0, width, height);
        });

        // Получаем текущий размер framebuffer'а (может отличаться от размера окна на retina дисплеях)
        int[] fbWidth = new int[1];
        int[] fbHeight = new int[1];
        glfwGetFramebufferSize(handle, fbWidth, fbHeight);
        // Устанавливаем viewport на весь размер framebuffer'а
        glViewport(0, 0, fbWidth[0], fbHeight[0]);

        // Включаем тест глубины для правильного отображения 3D объектов
        glEnable(GL_DEPTH_TEST);
        // Включаем отсечение задних граней для оптимизации
        glEnable(GL_CULL_FACE);
        // Отсекаем задние грани (не видимые снаружи)
        glCullFace(GL_FRONT);


    }

    /**
     * Проверяет, нужно ли закрывать окно
     *
     * @return true если окно должно быть закрыто
     */
    public boolean shouldClosed() {
        // glfwWindowShouldClose проверяет, была ли нажата кнопка закрытия окна
        return glfwWindowShouldClose(handle);
    }

    /**
     * Обновляет окно - меняет front и back буферы
     */
    public void update() {
        // glfwSwapBuffers меняет местами front и back буферы
        // Back buffer - куда мы рисуем
        // Front buffer - что видит пользователь
        glfwSwapBuffers(handle);
        // Обрабатываем события ввода (клавиатура, мышь)
        glfwPollEvents();
    }

    /**
     * Уничтожает окно и завершает GLFW
     */
    public void destroy() {
        // Уничтожаем окно
        glfwDestroyWindow(handle);
        // Завершаем работу GLFW
        glfwTerminate();
    }
}
