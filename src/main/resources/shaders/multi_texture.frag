#version 330 core

in vec2 texCoord;
in float textureType;

uniform sampler2D textures[8];

out vec4 FragColor;

void main() {
    int idx = int(textureType);
    vec4 texColor = texture(textures[idx], texCoord);

    if (texColor.a < 0.1)
        discard;

    FragColor = texColor;
}
