#version 330 core

in vec2 texCoord;
in float textureType;

uniform sampler2D grassTopTexture;   // 0
uniform sampler2D grassSideTexture;  // 1
uniform sampler2D dirtTexture;       // 2
uniform sampler2D stoneTexture;      // 3
uniform sampler2D woodTopTexture;    // 4
uniform sampler2D woodSideTexture;   // 5

out vec4 FragColor;

void main() {
    vec4 texColor;

    if (textureType == 0) {
        texColor = texture(grassTopTexture, texCoord);
    } else if (textureType == 1) {
        texColor = texture(grassSideTexture, texCoord);
    } else if (textureType == 2) {
        texColor = texture(dirtTexture, texCoord);
    } else if (textureType == 3) {
        texColor = texture(stoneTexture, texCoord);
    } else if (textureType == 4) {
        texColor = texture(woodTopTexture, texCoord);
    } else if (textureType == 5) {
        texColor = texture(woodSideTexture, texCoord);
    } else {
        // отладка
        texColor = vec4(1.0, 0.0, 1.0, 1.0);
    }

    // прозрачность (листва, трава и т.п.)
    if (texColor.a < 0.1)
        discard;

    FragColor = texColor;
}
