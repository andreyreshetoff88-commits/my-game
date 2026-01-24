package com.mygame.engine.graphics;

import com.mygame.world.ChunkMesh;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL30.*;

public class Renderer {
    private int vaoId;         // VAO для чанка
    private int vboId;         // VBO для чанка
    private int vertexCount;   // количество вершин (для glDrawArrays)
    private boolean meshUploaded = false; // флаг: данные загружены в GPU

    public void beginScene(Camera camera, int width, int height) {
        glEnable(GL_DEPTH_TEST);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        // очистка экрана
        glClearColor(0.2f, 0.3f, 0.4f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);

        // настройка проекции
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        float near = 0.1f;
        float fov = 60f;
        float top = (float) Math.tan(Math.toRadians(fov / 2)) * near;
        float aspect = (float) width / height;
        float right = top * aspect;
        float far = 100f;
        glFrustum(-right, right, -top, top, near, far);

        // настройка модели и камеры
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        camera.applyView();
    }

    public void renderChunkMesh(ChunkMesh mesh) {
        if (mesh == null) return;

        glBindVertexArray(mesh.getVaoId());

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);

        glDrawArrays(GL_TRIANGLES, 0, mesh.getVertexCount());

        glDisableClientState(GL_COLOR_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);

        glBindVertexArray(0);
    }

    // очистка GPU
    public void cleanupChunkMesh(ChunkMesh mesh) {
        glDeleteBuffers(mesh.getVboId());
        glDeleteVertexArrays(mesh.getVaoId());
    }

    public void renderPlayer(float radius, float currentHeight, float yaw, Vector3f renderPos) {
        float halfWidth = radius / 2;
        float height = currentHeight - radius * 2;

        glPushMatrix();

        // переносим в позицию игрока
        glTranslatef(renderPos.x, renderPos.y, renderPos.z);
        glRotatef(yaw, 0, renderPos.z, 0);

        glBegin(GL_QUADS);

        // 🔴 передняя грань
        glVertex3f(-halfWidth, 0, halfWidth);
        glVertex3f(halfWidth, 0, halfWidth);
        glVertex3f(halfWidth, height, halfWidth);
        glVertex3f(-halfWidth, height, halfWidth);

        // 🔵 задняя
        glVertex3f(-halfWidth, 0, -halfWidth);
        glVertex3f(halfWidth, 0, -halfWidth);
        glVertex3f(halfWidth, height, -halfWidth);
        glVertex3f(-halfWidth, height, -halfWidth);

        // 🟢 левая
        glVertex3f(-halfWidth, 0, -halfWidth);
        glVertex3f(-halfWidth, 0, halfWidth);
        glVertex3f(-halfWidth, height, halfWidth);
        glVertex3f(-halfWidth, height, -halfWidth);

        // 🟡 правая
        glVertex3f(halfWidth, 0, -halfWidth);
        glVertex3f(halfWidth, 0, halfWidth);
        glVertex3f(halfWidth, height, halfWidth);
        glVertex3f(halfWidth, height, -halfWidth);

        // ⚪ верх
        glVertex3f(-halfWidth, height, -halfWidth);
        glVertex3f(halfWidth, height, -halfWidth);
        glVertex3f(halfWidth, height, halfWidth);
        glVertex3f(-halfWidth, height, halfWidth);


        glEnd();
        glPopMatrix();
    }
}
