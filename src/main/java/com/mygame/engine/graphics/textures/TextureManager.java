package com.mygame.engine.graphics.textures;

import com.mygame.Utils.TextureScanner;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.stb.STBImage.*;

public class TextureManager {
    private final Map<String, Integer> texturesIds = new HashMap<>();

    public TextureManager() {
        TextureScanner.scanFolder().forEach(this::loadTexture);
    }

    private void loadTexture(String name, String filepath) {
        ByteBuffer image;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelsBuffer = stack.mallocInt(1);

            stbi_set_flip_vertically_on_load(true);
            image = stbi_load(filepath, widthBuffer, heightBuffer, channelsBuffer, 4);

            if (image == null) {
                System.err.println("Не удалось загрузить текстуру: " + filepath);
                return;
            }
            int width = widthBuffer.get();
            int height = heightBuffer.get();
            int textureId = glGenTextures();

            glBindTexture(GL_TEXTURE_2D, textureId);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                    GL_RGBA, GL_UNSIGNED_BYTE, image);

            glBindTexture(GL_TEXTURE_2D, 0);

            stbi_image_free(image);
            texturesIds.put(name, textureId);
        }
    }

    public void bindTexture(String name, int unit) {
        Integer textureId = texturesIds.get(name);
        if (textureId == null) {
            System.err.println("ПРЕДУПРЕЖДЕНИЕ: Текстура не найдена: " + name);
            textureId = 0;
        }
        unit = GL_TEXTURE0 + unit;
        glActiveTexture(unit);
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public void cleanup() {
        for (int textureId : texturesIds.values()) {
            glDeleteTextures(textureId);
        }
        texturesIds.clear();
    }
}
