package com.mygame.world.block;

import org.joml.Vector3f;

public class DirtBlock extends Block{
    public DirtBlock(Vector3f position) {
        super(position, BlockType.DIRT);
    }
}
