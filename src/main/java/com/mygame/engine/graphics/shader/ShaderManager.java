package com.mygame.engine.graphics.shader;

import java.util.HashMap;
import java.util.Map;

public class ShaderManager {
    private static ShaderManager instance;
    private final Map<String, Shader> shaders;

    private ShaderManager() {
        shaders = new HashMap<>();
    }

    public static ShaderManager getInstance() {
        if (instance == null) {
            instance = new ShaderManager();
        }
        return instance;
    }

    public void addShader(String name, Shader shader) {
        shaders.put(name, shader);
    }

    public Shader getShader(String name) {
        return shaders.get(name);
    }

    public void bindShader(String name) {
        Shader shader = shaders.get(name);
        if (shader != null) {
            shader.bind();
        }
    }

    public void cleanup() {
        for (Shader shader : shaders.values()) {
            shader.cleanup();
        }
        shaders.clear();
    }
}
