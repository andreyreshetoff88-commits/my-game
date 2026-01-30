#version 330 core

in vec3 fragColor;
in vec2 texCoord;
in float textureType;

uniform sampler2D grassTopTexture;
uniform sampler2D grassSideTexture;
uniform sampler2D dirtTexture;
uniform sampler2D stoneTexture;
uniform sampler2D woodTopTexture;
uniform sampler2D woodSideTexture;

out vec4 FragColor;

void main() {
    vec4 texColor;

    // ОТЛАДКА: ПОКАЗЫВАЕМ ЦВЕТ В ЗАВИСИМОСТИ ОТ ТИПА ТЕКСТУРЫ
    if (textureType == 0.0) {
        // Красный для верха травы
        FragColor = vec4(1.0, 0.0, 0.0, 1.0);
        return;
    } else if (textureType == 1.0) {
        // Зеленый для боков травы
        FragColor = vec4(0.0, 1.0, 0.0, 1.0);
        return;
    } else if (textureType == 2.0) {
        // Синий для земли
        FragColor = vec4(0.0, 0.0, 1.0, 1.0);
        return;
    } else if (textureType == 3.0) {
        // Желтый для камня
        FragColor = vec4(1.0, 1.0, 0.0, 1.0);
        return;
    } else if (textureType == 4.0) {
        // Белый для верха/низа дерева
        FragColor = vec4(1.0, 1.0, 1.0, 1.0);
        return;
    } else if (textureType == 5.0) {
        // Черный для боков дерева
        FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    } else {
        // Фиолетовый для ошибки
        FragColor = vec4(1.0, 0.0, 1.0, 1.0);
        return;
    }
}
