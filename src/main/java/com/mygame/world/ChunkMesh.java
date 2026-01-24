package com.mygame.world;

import lombok.Getter;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

@Getter
public class ChunkMesh {
    private int vaoId;
    private int vboId;
    private int vertexCount;

    public ChunkMesh(List<Block> blocks) {
        buildMesh(blocks);
    }

    private void buildMesh(List<Block> blocks) {

        // 36 вершин * (позиция 3 + цвет 3)
        float[] vertices = new float[blocks.size() * 36 * 6];
        int index = 0;

        // цвета граней
        float[] TOP    = {1f, 0f, 0f};
        float[] BOTTOM = {0f, 1f, 0f};
        float[] FRONT  = {0f, 0f, 1f};
        float[] BACK   = {1f, 1f, 0f};
        float[] LEFT   = {0f, 1f, 1f};
        float[] RIGHT  = {1f, 0f, 1f};

        for (Block block : blocks) {
            float x = block.position().x;
            float y = block.position().y;
            float z = block.position().z;
            float s = 0.25f;

            // ===== ВЕРХ =====
            float[][] top = {
                    {x - s, y + s, z - s}, {x + s, y + s, z - s}, {x + s, y + s, z + s},
                    {x + s, y + s, z + s}, {x - s, y + s, z + s}, {x - s, y + s, z - s},
            };
            index = pushFace(vertices, index, top, TOP);

            // ===== НИЗ =====
            float[][] bottom = {
                    {x - s, y - s, z - s}, {x + s, y - s, z - s}, {x + s, y - s, z + s},
                    {x + s, y - s, z + s}, {x - s, y - s, z + s}, {x - s, y - s, z - s},
            };
            index = pushFace(vertices, index, bottom, BOTTOM);

            // ===== ПЕРЕД =====
            float[][] front = {
                    {x - s, y - s, z + s}, {x + s, y - s, z + s}, {x + s, y + s, z + s},
                    {x + s, y + s, z + s}, {x - s, y + s, z + s}, {x - s, y - s, z + s},
            };
            index = pushFace(vertices, index, front, FRONT);

            // ===== ЗАД =====
            float[][] back = {
                    {x - s, y - s, z - s}, {x + s, y - s, z - s}, {x + s, y + s, z - s},
                    {x + s, y + s, z - s}, {x - s, y + s, z - s}, {x - s, y - s, z - s},
            };
            index = pushFace(vertices, index, back, BACK);

            // ===== ЛЕВО =====
            float[][] left = {
                    {x - s, y - s, z - s}, {x - s, y - s, z + s}, {x - s, y + s, z + s},
                    {x - s, y + s, z + s}, {x - s, y + s, z - s}, {x - s, y - s, z - s},
            };
            index = pushFace(vertices, index, left, LEFT);

            // ===== ПРАВО =====
            float[][] right = {
                    {x + s, y - s, z - s}, {x + s, y - s, z + s}, {x + s, y + s, z + s},
                    {x + s, y + s, z + s}, {x + s, y + s, z - s}, {x + s, y - s, z - s},
            };
            index = pushFace(vertices, index, right, RIGHT);
        }

        vertexCount = vertices.length / 6;

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        int stride = 6 * Float.BYTES;

        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(3, GL_FLOAT, stride, 0);

        glEnableClientState(GL_COLOR_ARRAY);
        glColorPointer(3, GL_FLOAT, stride, 3 * Float.BYTES);

        glBindVertexArray(0);
    }

    private int pushFace(float[] vertices, int index, float[][] face, float[] color) {
        for (float[] v : face) {
            vertices[index++] = v[0];
            vertices[index++] = v[1];
            vertices[index++] = v[2];
            vertices[index++] = color[0];
            vertices[index++] = color[1];
            vertices[index++] = color[2];
        }
        return index;
    }
}
