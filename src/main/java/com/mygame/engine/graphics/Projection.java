package com.mygame.engine.graphics;

import lombok.Getter;
import org.joml.Matrix4f;

public class Projection {
    @Getter
    private Matrix4f projectionMatrix = new Matrix4f();

    private final float fov;
    private final float near;
    private final float far;

    private int width;
    private int height;

    public Projection(float fov, float near, float far) {
        this.fov = fov;
        this.near = near;
        this.far = far;
    }

    public void update(int width, int height) {
        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;

            projectionMatrix.setPerspective(
                    (float) Math.toRadians(fov),
                    (float) width / height,
                    near,
                    far
            );
        }
    }
}
