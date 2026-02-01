package com.mygame.world;

import com.mygame.engine.entity.Entity;
import com.mygame.engine.entity.Player;
import com.mygame.engine.graphics.Renderer;
import com.mygame.world.block.Block;
import com.mygame.world.chunk.Chunk;
import com.mygame.world.chunk.ChunkPos;
import lombok.Getter;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class World {
    private static final int VIEW_DISTANCE = 3;
    private static final int MAX_CHUNKS_PER_FRAME = 4;
    @Getter
    private Player player;
    private final List<Entity> entities = new ArrayList<>();
    private final Map<ChunkPos, Chunk> chunks = new HashMap<>();
    private final ConcurrentLinkedQueue<Chunk> readyChunks = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Chunk> chunksToUpload = new ConcurrentLinkedQueue<>();

    public World() {
        Chunk startChunk = new Chunk(0, 0);
        chunks.put(new ChunkPos(0, 0), startChunk);

        player = new Player(generateSpawnPoint(startChunk));
        entities.add(player);
        int chunkX = worldToChunk(player.getPosition().x);
        int chunkZ = worldToChunk(player.getPosition().z);
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        Chunk playerChunk = chunks.get(pos);
        chunksToUpload.add(playerChunk);
    }

    public void update(float deltaTime) {
        generateChunksAround(player.getPosition());
        while (!readyChunks.isEmpty()) {
            Chunk chunk = readyChunks.poll();
            if (chunk == null) continue;

            ChunkPos pos = new ChunkPos(chunk.getChunkX(), chunk.getChunkZ());
            chunks.put(pos, chunk);

            chunk.buildMesh(getNeighborChunks(chunk.getChunkX(), chunk.getChunkZ()));
            chunksToUpload.add(chunk);

            rebuildNeighbors(chunk.getChunkX(), chunk.getChunkZ());
        }
        for (Entity entity : entities) {
            entity.update(deltaTime, getNearbyBlocks(entity.getPosition()));
        }
    }

    private void rebuildNeighbors(int chunkX, int chunkZ) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;

                ChunkPos pos = new ChunkPos(chunkX + dx, chunkZ + dz);
                Chunk neighbor = chunks.get(pos);
                if (neighbor != null) {
                    neighbor.buildMesh(getNeighborChunks(
                            neighbor.getChunkX(),
                            neighbor.getChunkZ()
                    ));
                    chunksToUpload.add(neighbor);
                }
            }
        }
    }

    private void generateChunksAround(Vector3f playerPos) {
        int playerChunkX = worldToChunk(playerPos.x);
        int playerChunkZ = worldToChunk(playerPos.z);

        int chunksScheduled = 0;

        for (int dx = -VIEW_DISTANCE; dx <= VIEW_DISTANCE; dx++) {
            for (int dz = -VIEW_DISTANCE; dz <= VIEW_DISTANCE; dz++) {
                ChunkPos cp = new ChunkPos(playerChunkX + dx, playerChunkZ + dz);

                if (!chunks.containsKey(cp) && chunksScheduled < MAX_CHUNKS_PER_FRAME) {
                    chunksScheduled++;
                    Chunk newChunk = new Chunk(cp.x(), cp.z());
                    readyChunks.add(newChunk);
                }
            }
        }
        Iterator<Map.Entry<ChunkPos, Chunk>> it = chunks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ChunkPos, Chunk> entry = it.next();
            ChunkPos cp = entry.getKey();
            int dx = Math.abs(cp.x() - playerChunkX);
            int dz = Math.abs(cp.z() - playerChunkZ);
            if (dx > VIEW_DISTANCE || dz > VIEW_DISTANCE) {
                it.remove();
            }
        }
    }

    public void render(Renderer renderer, Vector3f renderPos) {
        for (Entity entity : entities) {
            entity.render(renderer, renderPos);
        }

        while (!chunksToUpload.isEmpty()) {
            Chunk chunk = chunksToUpload.poll();
            if (chunk != null && !chunk.isUploaded()) {
                renderer.uploadChunk(chunk);
                chunk.markUploaded();
            }
        }

        renderer.renderChunk();
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

    private Vector3f generateSpawnPoint(Chunk chunk) {
        float maxY = Float.NEGATIVE_INFINITY;
        Vector3f top = null;
        for (Block block : chunk.getBlocks()) {
            if (block.position().y > maxY) {
                maxY = block.position().y;
                top = block.position();
            }
        }
        if (top != null) {
            return new Vector3f(
                    top.x,
                    top.y + Chunk.BLOCK_SIZE + 0.01f,
                    top.z
            );
        }
        return new Vector3f(0, 5, 0);
    }

    private int worldToChunk(float worldCoord) {
        return (int) Math.floor(worldCoord / (Chunk.SIZE * Chunk.BLOCK_SIZE));
    }

    public void destroyBlock(Block block) {
        if (block == null) return;

        int chunkX = worldToChunk(block.position().x);
        int chunkZ = worldToChunk(block.position().z);

        Chunk chunk = chunks.get(new ChunkPos(chunkX, chunkZ));
        if (chunk == null) return;

        chunk.destroyBlock(block);

        chunk.buildMesh(getNeighborChunks(chunk.getChunkX(), chunk.getChunkZ()));
        chunksToUpload.add(chunk);

        rebuildNeighbors(chunkX, chunkZ);
    }

    public Map<Long, Chunk> getNeighborChunks(int chunkX, int chunkZ) {
        Map<Long, Chunk> neighbors = new HashMap<>();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                ChunkPos pos = new ChunkPos(chunkX + dx, chunkZ + dz);
                Chunk neighbor = chunks.get(pos);
                if (neighbor != null) {
                    long key = (((long) (chunkX + dx)) << 32) | ((chunkZ + dz) & 0xFFFFFFFFL);
                    neighbors.put(key, neighbor);
                }
            }
        }

        return neighbors;
    }
}
