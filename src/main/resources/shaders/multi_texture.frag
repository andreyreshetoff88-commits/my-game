#version 330 core

in vec2 texCoord;
in float textureType;

uniform sampler2D textures[6];

out vec4 FragColor;

void main() {
    int idx = int(textureType);
    FragColor = texture(textures[idx], texCoord);
}
