package com.mygame.world;

import lombok.Getter;
import org.joml.Vector3f;
import com.mygame.noise.OpenSimplexNoise;
import java.util.ArrayList;
import java.util.List;

public class Chunk {

    public static final int SIZE = 16;
    public static final float BLOCK_SIZE = 0.5f;
    private static final double FREQUENCY = 0.05;
    private static final double MAX_HEIGHT = 20;
    @Getter
    private final List<Block> blocks = new ArrayList<>();

    @Getter
    private ChunkMesh mesh;

    private final int chunkX;
    private final int chunkZ;
    @Getter
    private boolean uploaded = false;

    private static final OpenSimplexNoise noise = new OpenSimplexNoise(123456);

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        generateBlocks();
        buildMesh();
    }

    private void generateBlocks() {
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                int worldX = chunkX * SIZE + x;
                int worldZ = chunkZ * SIZE + z;
                double nx = worldX * FREQUENCY;
                double ny = 0;               // фиксированное значение, т.к. мы хотим высоту по x/z
                double nz = worldZ * FREQUENCY;

                double value = noise.eval(nx, ny, nz);  // возвращает -1..1
                int height = (int)((value + 1) / 2 * MAX_HEIGHT);
                for (int y = 0; y <= height; y++) {
                    blocks.add(new Block(new Vector3f(worldX * BLOCK_SIZE, y * BLOCK_SIZE, worldZ * BLOCK_SIZE)));
                }
            }
        }
    }

    private void buildMesh() {
        List<Float> data = new ArrayList<>();
        float s = BLOCK_SIZE / 2f;
        for (Block block : blocks) {
            Vector3f p = block.position();
            addCube(data, p.x, p.y, p.z, s);
        }
        float[] vertices = new float[data.size()];
        for (int i = 0; i < data.size(); i++) vertices[i] = data.get(i);
        mesh = new ChunkMesh(vertices);
    }

    private void addCube(List<Float> v, float x, float y, float z, float s) {
        float[][] colors = {
                {0.3f, 0.8f, 0.3f}, // верх
                {0.5f, 0.25f, 0.1f}, // низ
                {0.5f, 0.25f, 0.1f}, // перед
                {0.5f, 0.25f, 0.1f}, // зад
                {0.5f, 0.25f, 0.1f}, // лево
                {0.5f, 0.25f, 0.1f}  // право
        };
        float[][][] faces = {
                {{-s,+s,-s},{+s,+s,-s},{+s,+s,+s},{+s,+s,+s},{-s,+s,+s},{-s,+s,-s}},
                {{-s,-s,-s},{+s,-s,-s},{+s,-s,+s},{+s,-s,+s},{-s,-s,+s},{-s,-s,-s}},
                {{-s,-s,+s},{+s,-s,+s},{+s,+s,+s},{+s,+s,+s},{-s,+s,+s},{-s,-s,+s}},
                {{-s,-s,-s},{+s,-s,-s},{+s,+s,-s},{+s,+s,-s},{-s,+s,-s},{-s,-s,-s}},
                {{-s,-s,-s},{-s,-s,+s},{-s,+s,+s},{-s,+s,+s},{-s,+s,-s},{-s,-s,-s}},
                {{+s,-s,-s},{+s,-s,+s},{+s,+s,+s},{+s,+s,+s},{+s,+s,-s},{+s,-s,-s}}
        };
        for (int f = 0; f < 6; f++) {
            for (int i = 0; i < 6; i++) {
                v.add(x + faces[f][i][0]);
                v.add(y + faces[f][i][1]);
                v.add(z + faces[f][i][2]);
                v.add(colors[f][0]);
                v.add(colors[f][1]);
                v.add(colors[f][2]);
            }
        }
    }

    // ★ ИЗМЕНЕНИЕ: метод для Renderer, чтобы отметить, что меш загружен в GPU
    public void markUploaded() {
        uploaded = true;
    }
}
