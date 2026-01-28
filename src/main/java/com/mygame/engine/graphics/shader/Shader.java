package com.mygame.engine.graphics.shader;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final int programID;
    private final int vertexShaderID;
    private final int fragmentShaderID;

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

        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
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

    public void bind() {
        glUseProgram(programID);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void setUniform(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(programID, name);
        if (location != -1) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
            matrix.get(buffer);
            glUniformMatrix4fv(location, false, buffer);
        }
    }

    public void setUniform(String name, Vector3f vector) {
        int location = glGetUniformLocation(programID, name);
        if (location != -1) {
            glUniform3f(location, vector.x, vector.y, vector.z);
        }
    }

    public void setUniform(String name, float value) {
        int location = glGetUniformLocation(programID, name);
        if (location != -1) {
            glUniform1f(location, value);
        }
    }

    public void cleanup() {
        unbind();
        if (programID != 0) {
            glDetachShader(programID, vertexShaderID);
            glDetachShader(programID, fragmentShaderID);
            glDeleteShader(vertexShaderID);
            glDeleteShader(fragmentShaderID);
            glDeleteProgram(programID);
        }
    }

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
