package com.mygame.world.block;

import org.joml.Vector3f;

public record Block(Vector3f position, BlockType blockType) {
}
