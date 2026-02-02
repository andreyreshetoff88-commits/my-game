package com.mygame.world.block;

import org.joml.Vector3f;

public class BadRockBlock extends Block{
    public BadRockBlock(Vector3f position) {
        super(position, BlockType.BEDROCK);
    }
}
