#version 150

uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;

layout(std140) uniform MotionBlurParams {
    mat4 mvInverse;
    mat4 projInverse;
    mat4 prevModelView;
    mat4 prevProjection;
    vec3 cameraPos;
    vec3 prevCameraPos;
    vec2 view_res;
    float BlendFactor;
    float inverseSamples;
    float handDepthThreshold;
    int motionBlurSamples;
    int halfSamples;
    int blurAlgorithm;
};

in vec2 texCoord;
out vec4 fragColor;

vec3 reproject(vec3 screen_pos) {
    vec3 ndc = screen_pos * 2.0 - 1.0;
    vec4 view_pos4 = projInverse * vec4(ndc, 1.0);
    vec3 view_pos = view_pos4.xyz / view_pos4.w;

    vec3 world_pos = (mvInverse * vec4(view_pos, 1.0)).xyz + (cameraPos - prevCameraPos);
    vec4 prev_proj = prevProjection * (prevModelView * vec4(world_pos, 1.0));

    return (prev_proj.xyz / prev_proj.w) * 0.5 + 0.5;
}

vec2 clampLength(vec2 velocity) {
    float lenSq = dot(velocity, velocity);
    return (lenSq > 0.16) ? velocity * (0.4 * inversesqrt(lenSq)) : velocity;
}

float noise(vec2 pos) {
    return fract(52.9829189 * fract(0.06711056 * pos.x + 0.00583715 * pos.y));
}

void main() {
    ivec2 texel = ivec2(gl_FragCoord.xy);

    float depth = texelFetch(MainDepthSampler, texel, 0).x;

    if (depth < handDepthThreshold) {
        fragColor = texture(MainSampler, texCoord);
        return;
    }

    vec2 velocity = texCoord - reproject(vec3(texCoord, depth)).xy;
    velocity = clampLength(velocity);

    vec2 totalOffset = BlendFactor * velocity;
    vec2 baseStep = totalOffset * inverseSamples;

    vec3 color_sum = vec3(0.0);
    vec2 seed = texCoord * view_res;

    bool centerBlur = blurAlgorithm != 0;
    for (int i = 0; i < motionBlurSamples; ++i) {
        float fi = float(i);

        float jitter = noise(seed + vec2(fi, fi * 1.4));
        float offset_centered = fi - float(halfSamples);
        float sample_index = centerBlur ? offset_centered : fi;
        float sample_offset = sample_index + jitter;

        vec2 pos = texCoord + sample_offset * baseStep;
        vec3 c = texture(MainSampler, pos).rgb;

        color_sum += c * c;
    }
    fragColor = vec4(sqrt(color_sum * inverseSamples), 1.0);
}
