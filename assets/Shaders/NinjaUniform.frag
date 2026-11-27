// Keep jME's lighting and skinning pipeline, then recolor only green cloth.
#define main ninjaLightingMain
#import "Shaders/JmePhongLighting.glsllib"
#undef main

uniform vec4 m_UniformColor;

void main() {
    ninjaLightingMain();
    vec3 source = texture2D(m_DiffuseMap, texCoord).rgb;
    float greenDominance = source.g - max(source.r, source.b);
    float clothMask = smoothstep(0.015, 0.08, greenDominance);
    float brightness = max(gl_FragColor.r, max(gl_FragColor.g, gl_FragColor.b));
    vec3 clothColor = brightness * m_UniformColor.rgb;
    gl_FragColor.rgb = mix(gl_FragColor.rgb, clothColor, clothMask);
}
