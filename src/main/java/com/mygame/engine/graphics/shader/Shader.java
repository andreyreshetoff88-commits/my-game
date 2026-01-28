package com.mygame.engine.graphics.shader;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

/**
 * Класс для работы с шейдерами OpenGL
 * Отвечает за загрузку, компиляцию и управление шейдерными программами
 */
public class Shader {
    private final int programID;
    private final int vertexShaderID;
    private final int fragmentShaderID;

    /**
     * Конструктор шейдера
     * @param vertexFile путь к файлу вершинного шейдера
     * @param fragmentFile путь к файлу фрагментного шейдера
     */
    public Shader(String vertexFile, String fragmentFile) {
        programID = glCreateProgram();

        vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShaderID, readFile(vertexFile));
        glCompileShader(vertexShaderID);
        if (glGetShaderi(vertexShaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Ошибка компиляции вершинного шейдера: " + glGetShaderInfoLog(vertexShaderID));
            System.exit(-1);
        }

        fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShaderID, readFile(fragmentFile));
        glCompileShader(fragmentShaderID);

        if (glGetShaderi(fragmentShaderID, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Ошибка компиляции фрагментного шейдера: " + glGetShaderInfoLog(fragmentShaderID));
            System.exit(-1);
        }

        glAttachShader(programID, vertexShaderID);   // Вершинный шейдер
        glAttachShader(programID, fragmentShaderID); // Фрагментный шейдер

        glLinkProgram(programID);

        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println("Ошибка линковки шейдерной программы: " + glGetProgramInfoLog(programID));
            System.exit(-1);
        }

        glValidateProgram(programID);
        if (glGetProgrami(programID, GL_VALIDATE_STATUS) == GL_FALSE) {
            System.err.println("Ошибка валидации шейдерной программы: " + glGetProgramInfoLog(programID));
            System.exit(-1);
        }
    }

    /**
     * Активирует шейдерную программу для использования
     * После вызова этой функции все рендеринг будет использовать этот шейдер
     */
    public void bind() {
        glUseProgram(programID);
    }

    /**
     * Деактивирует текущую шейдерную программу
     * Устанавливает использование нулевой (пустой) программы
     */
    public void unbind() {
        glUseProgram(0);
    }

    /**
     * Устанавливает матричный uniform в шейдере
     * Uniform - это переменные в шейдере, которые могут быть изменены из Java кода
     * @param name имя uniform переменной в шейдре
     * @param matrix матрица 4x4 для передачи в шейдер
     */
    public void setUniform(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(programID, name);
        if (location != -1) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            matrix.get(buffer);
            glUniformMatrix4fv(location, false, buffer);
        }
    }

    /**
     * Устанавливает векторный uniform в шейдере
     * @param name имя uniform переменной
     * @param vector вектор из 3 компонентов (x, y, z)
     */
    public void setUniform(String name, Vector3f vector) {
        int location = glGetUniformLocation(programID, name);
        if (location != -1) {
            glUniform3f(location, vector.x, vector.y, vector.z);
        }
    }

    /**
     * Устанавливает скалярный uniform в шейдере
     * @param name имя uniform переменной
     * @param value значение типа float
     */
    public void setUniform(String name, float value) {
        int location = glGetUniformLocation(programID, name);
        if (location != -1) {
            glUniform1f(location, value);
        }
    }

    /**
     * Освобождает ресурсы шейдера
     * Вызывается при завершении программы для предотвращения утечек памяти
     */
    public void cleanup() {
        unbind(); // Сначала отключаем шейдер

        // Проверяем, что программа существует (ID не равен 0)
        if (programID != 0) {
            // Отсоединяем шейдеры от программы
            glDetachShader(programID, vertexShaderID);
            glDetachShader(programID, fragmentShaderID);

            // Удаляем шейдеры из памяти GPU
            glDeleteShader(vertexShaderID);
            glDeleteShader(fragmentShaderID);

            // Удаляем шейдерную программу
            glDeleteProgram(programID);
        }
    }

    /**
     * Читает содержимое файла шейдера
     * @param filename путь к файлу шейдера
     * @return содержимое файла в виде строки
     */
    private String readFile(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            System.err.println("Не удалось прочитать файл шейдера: " + filename);
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }
}
