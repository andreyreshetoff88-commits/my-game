package com.mygame.world;

import com.mygame.engine.entity.Entity;
import com.mygame.engine.entity.Player;
import com.mygame.engine.graphics.Renderer;
import lombok.Getter;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class World {
    private static final int VIEW_DISTANCE = 3;
    @Getter
    private Player player;
    private List<Entity> entities = new ArrayList<>();
    private final Map<ChunkPos, Chunk> chunks = new HashMap<>();

    public World(Renderer renderer) {
        generateChunksAround(new Vector3f(), renderer);
        player = new Player(generateSpawnPoint());
        entities.add(player);
    }

    public void update(float deltaTyme, Renderer renderer) {
        generateChunksAround(player.getPosition(), renderer);
        for (Entity entity : entities) {
            entity.update(deltaTyme, getNearbyBlocks(entity.getPosition()));
        }
    }

    private void generateChunksAround(Vector3f playerPos, Renderer renderer) {
        int playerChunkX = worldToChunk(playerPos.x);
        int playerChunkZ = worldToChunk(playerPos.z);

        for (int dx = -VIEW_DISTANCE; dx <= VIEW_DISTANCE; dx++) {
            for (int dz = -VIEW_DISTANCE; dz <= VIEW_DISTANCE; dz++) {

                ChunkPos pos = new ChunkPos(
                        playerChunkX + dx,
                        playerChunkZ + dz
                );

                if (!chunks.containsKey(pos)) {
                    Chunk chunk = new Chunk(pos.x(), pos.z());
                    chunks.put(pos, chunk);
                }
            }
        }

        chunks.entrySet().removeIf(entry -> {
            ChunkPos pos = entry.getKey();
            int distX = Math.abs(pos.x() - playerChunkX);
            int distZ = Math.abs(pos.z() - playerChunkZ);

            if (distX > VIEW_DISTANCE || distZ > VIEW_DISTANCE) {
                entry.getValue().cleanup(renderer); // удаляем VBO/VAO
                return true;
            }
            return false;
        });
    }

    public void render(Renderer renderer, Vector3f renderPos) {
        for (Entity entity : entities) {
            entity.render(renderer, renderPos);
        }
        for (Chunk chunk : chunks.values()) {
            chunk.render(renderer);
        }
    }

    public List<Block> getNearbyBlocks(Vector3f pos) {
        List<Block> result = new ArrayList<>();

        int cx = worldToChunk(pos.x);
        int cz = worldToChunk(pos.z);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {

                Chunk chunk = chunks.get(new ChunkPos(cx + dx, cz + dz));
                if (chunk != null) {
                    result.addAll(chunk.getBlocks());
                }
            }
        }
        return result;
    }

    public Vector3f generateSpawnPoint() {
        Chunk chunk = chunks.get(new ChunkPos(0, 0));
        Block topBlock = null;
        float maxY = Float.NEGATIVE_INFINITY;
        for (Block b : chunk.getBlocks()) {
            if (b.position().y > maxY) {
                maxY = b.position().y;
                topBlock = b;
            }
        }

        if (topBlock != null) {
            float centerX = topBlock.position().x + Chunk.BLOCK_SIZE / 2f;
            float centerZ = topBlock.position().z + Chunk.BLOCK_SIZE / 2f;
            float yAboveBlock = topBlock.position().y + Chunk.BLOCK_SIZE + 0.01f; // чуть выше блока

            return new Vector3f(centerX, yAboveBlock, centerZ);
        }
        return new Vector3f(0, 50, 0);
    }

    private int worldToChunk(float worldCoord) {
        return (int) Math.floor(worldCoord / (Chunk.SIZE * Chunk.BLOCK_SIZE));
    }
}
