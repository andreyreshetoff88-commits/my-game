package com.mygame.engine.graphics;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;

public class Crosshair {

    private final VertexArray vao;

    public Crosshair(int windowHeight, int windowWidth) {
        float size = 0.03f;
        float aspect = (float) windowHeight / windowWidth;

        float[] vertices = new float[]{
                -size * aspect,  size, 0f, 1f,1f,1f, 0f,1f, 6f,
                size * aspect,  size, 0f, 1f,1f,1f, 1f,1f, 6f,
                size * aspect, -size, 0f, 1f,1f,1f, 1f,0f, 6f,

                -size * aspect,  size, 0f, 1f,1f,1f, 0f,1f, 6f,
                size * aspect, -size, 0f, 1f,1f,1f, 1f,0f, 6f,
                -size * aspect, -size, 0f, 1f,1f,1f, 0f,0f, 6f
        };

        vao = new VertexArray(vertices);
    }

    public void render() {
        vao.bind();
        glDrawArrays(GL_TRIANGLES, 0, vao.getVertexCount());
        vao.unbind();
    }
}
