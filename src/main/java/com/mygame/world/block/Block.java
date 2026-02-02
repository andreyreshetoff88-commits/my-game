package com.mygame.world.block;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.joml.Vector3f;

@Data
@AllArgsConstructor
public class Block {
    private Vector3f position;
    private BlockType blockType;
}
