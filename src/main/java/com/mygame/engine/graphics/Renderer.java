package com.mygame.engine.graphics;

import com.mygame.engine.graphics.shader.Shader;
import com.mygame.engine.graphics.shader.ShaderManager;
import com.mygame.engine.graphics.textures.Texture;
import com.mygame.world.Chunk;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {
    private final Shader shader;
    private final Projection projection;
    private final Matrix4f modelMatrix = new Matrix4f();
    private final List<VertexArray> chunkVAOs = new ArrayList<>();
    private final Texture dirtTexture;

    public Renderer() {
        shader = new Shader("src/main/resources/shaders/basic.vert",
                "src/main/resources/shaders/basic.frag");
        ShaderManager.getInstance().addShader("basic", shader);
        projection = new Projection(60.0f, 0.1f, 100.0f);
        dirtTexture = new Texture("src/main/resources/textures/blocks/dirt.png");
    }

    public void beginScene(Camera camera, int width, int height) {
        glClearColor(0.2f, 0.3f, 0.4f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);

        projection.update(width, height);

        shader.bind();

        shader.setUniform("uProjection", projection.getProjectionMatrix());
        shader.setUniform("uView", camera.getViewMatrix());
        shader.setUniform("uModel", modelMatrix.identity());

        dirtTexture.bind(0);
        shader.setUniform("uTexture", 0);
    }

    public void uploadChunk(Chunk chunk) {
        if (chunk.getMesh() != null && chunk.getMesh().getVertices() != null) {
            VertexArray vao = new VertexArray(chunk.getMesh().getVertices());
            chunkVAOs.add(vao);
            chunk.markUploaded();
        }
    }

    public void renderChunk() {
        for (VertexArray vao : chunkVAOs) {
            vao.render();
        }
    }

    public void renderPlayer(float radius, float currentHeight, float yaw, Vector3f renderPos) {
        float[] playerVertices = createPlayerVertices(radius, currentHeight);
        VertexArray playerVAO = new VertexArray(playerVertices);

        Matrix4f playerModel = new Matrix4f()
                .translate(renderPos)
                .rotate((float) Math.toRadians(-yaw), 0, 1, 0);

        shader.setUniform("uModel", playerModel);
        playerVAO.render();
        playerVAO.cleanup();

        shader.setUniform("uModel", modelMatrix.identity());
    }

    private float[] createPlayerVertices(float radius, float height) {
        float halfWidth = radius / 2;
        float cubeHeight = height - radius * 2;

        List<Float> verticesList = new ArrayList<>();

        addFaceWithWinding(verticesList,
                new float[]{-halfWidth, cubeHeight, -halfWidth},
                new float[]{halfWidth, cubeHeight, -halfWidth},
                new float[]{halfWidth, cubeHeight, halfWidth},
                new float[]{-halfWidth, cubeHeight, halfWidth},
                new float[]{1.0f, 0.0f, 1.0f}
        );

        addFaceWithWinding(verticesList,
                new float[]{-halfWidth, 0, halfWidth},
                new float[]{halfWidth, 0, halfWidth},
                new float[]{halfWidth, 0, -halfWidth},
                new float[]{-halfWidth, 0, -halfWidth},
                new float[]{0.5f, 0.5f, 0.5f}
        );

        addFaceWithWinding(verticesList,
                new float[]{-halfWidth, 0, halfWidth},
                new float[]{-halfWidth, cubeHeight, halfWidth},
                new float[]{halfWidth, cubeHeight, halfWidth},
                new float[]{halfWidth, 0, halfWidth},
                new float[]{0.0f, 1.0f, 0.0f}
        );

        addFaceWithWinding(verticesList,
                new float[]{halfWidth, 0, -halfWidth},
                new float[]{halfWidth, cubeHeight, -halfWidth},
                new float[]{-halfWidth, cubeHeight, -halfWidth},
                new float[]{-halfWidth, 0, -halfWidth},
                new float[]{0.0f, 0.0f, 1.0f}
        );

        addFaceWithWinding(verticesList,
                new float[]{-halfWidth, 0, -halfWidth},
                new float[]{-halfWidth, cubeHeight, -halfWidth},
                new float[]{-halfWidth, cubeHeight, halfWidth},
                new float[]{-halfWidth, 0, halfWidth},
                new float[]{1.0f, 0.0f, 0.0f}
        );

        addFaceWithWinding(verticesList,
                new float[]{halfWidth, 0, halfWidth},
                new float[]{halfWidth, cubeHeight, halfWidth},
                new float[]{halfWidth, cubeHeight, -halfWidth},
                new float[]{halfWidth, 0, -halfWidth},
                new float[]{1.0f, 1.0f, 0.0f}
        );

        float[] vertices = new float[verticesList.size()];
        for (int i = 0; i < verticesList.size(); i++) {
            vertices[i] = verticesList.get(i);
        }

        return vertices;
    }

    private void addFaceWithWinding(List<Float> vertices, float[] p1, float[] p2, float[] p3, float[] p4, float[] color) {
        addVertex(vertices, p1, color);
        addVertex(vertices, p2, color);
        addVertex(vertices, p3, color);

        addVertex(vertices, p1, color);
        addVertex(vertices, p3, color);
        addVertex(vertices, p4, color);
    }

    private void addVertex(List<Float> vertices, float[] position, float[] color) {
        vertices.add(position[0]);
        vertices.add(position[1]);
        vertices.add(position[2]);
        vertices.add(color[0]);
        vertices.add(color[1]);
        vertices.add(color[2]);
    }

    public void cleanup() {
        for (VertexArray vao : chunkVAOs) {
            vao.cleanup();
        }
        chunkVAOs.clear();
        shader.cleanup();
    }
}
