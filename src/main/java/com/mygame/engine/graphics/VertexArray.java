package com.mygame.engine.graphics;

import lombok.Getter;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class VertexArray {
    private final int vaoID;
    private final int vboID;
    @Getter
    private final int vertexCount;

    public VertexArray(float[] vertices) {
        // 9 элементов на вершину: x,y,z,r,g,b,u,v,type
        this.vertexCount = vertices.length / 9;

        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // Позиция (3 float)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Цвет (3 float)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 9 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // UV координаты (2 float)
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 9 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        // Тип текстуры (1 float) - ВАЖНО!
        glVertexAttribPointer(3, 1, GL_FLOAT, false, 9 * Float.BYTES, 8 * Float.BYTES);
        glEnableVertexAttribArray(3);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void bind() {
        glBindVertexArray(vaoID);
    }

    public void unbind() {
        glBindVertexArray(0);
    }

    public void render() {
        bind();
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        unbind();
    }

    public void cleanup() {
        glDeleteVertexArrays(vaoID);
        glDeleteBuffers(vboID);
    }
}

