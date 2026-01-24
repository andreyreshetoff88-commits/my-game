package com.mygame.noise;

public class OpenSimplexNoise {
    private final int[] permutation = new int[512];

    public OpenSimplexNoise(long seed) {
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;

        // Простое перемешивание с использованием seed
        for (int i = 255; i > 0; i--) {
            int j = (int)((seed + i) % (i + 1));
            int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }

        // Дублируем массив для простого индексирования
        for (int i = 0; i < 512; i++) permutation[i] = p[i & 255];
    }

    private static float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }

    private static float grad(int hash, float x, float y) {
        int h = hash & 3;
        float u = h < 2 ? x : y;
        float v = h < 2 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    public float noise(float x, float y) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;

        float xf = x - (float)Math.floor(x);
        float yf = y - (float)Math.floor(y);

        float u = fade(xf);
        float v = fade(yf);

        int aa = permutation[permutation[X] + Y];
        int ab = permutation[permutation[X] + Y + 1];
        int ba = permutation[permutation[X + 1] + Y];
        int bb = permutation[permutation[X + 1] + Y + 1];

        float x1 = lerp(u, grad(aa, xf, yf), grad(ba, xf - 1, yf));
        float x2 = lerp(u, grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1));

        return (lerp(v, x1, x2) + 1) / 2f; // нормализуем в [0,1]
    }
}
