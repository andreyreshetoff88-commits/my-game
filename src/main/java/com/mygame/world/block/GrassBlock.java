package com.mygame.world.block;

import org.joml.Vector3f;

public class GrassBlock extends Block {
    public GrassBlock(Vector3f position) {
        super(position, BlockType.GRASS);
    }
}
