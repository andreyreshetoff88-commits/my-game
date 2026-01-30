#version 330 core

in vec2 texCoord;
in float textureType;

uniform sampler2D textures[7]; // у тебя 7 текстур

out vec4 FragColor;

void main() {
    int idx = int(textureType);
    vec4 texColor = texture(textures[idx], texCoord);

    // если пиксель почти прозрачный, не рисуем его
    if (texColor.a < 0.1)
        discard;

    FragColor = texColor;
}
