#version 120

const float DEPTH_EPSILON = 0.01;

uniform sampler2D diffuseTex;
uniform sampler2D depthTex;

uniform int numLights;

varying vec2 diffuseTexCoords;
varying vec2 depthTexCoords;
varying float depth;
varying vec3 normal;
varying vec4 vertexColor;

void main()
{
  vec3 N = normalize(normal);

  float depth_light = texture2D(depthTex, depthTexCoords).r;
  float k_shadow = 1.0;
  if (depth_light < depth - DEPTH_EPSILON)
    k_shadow = 0.8;
      
  vec4 color = gl_FrontMaterial.ambient * gl_LightModel.ambient;

  for (int i = 0; i < numLights; i++)
  {  
  	// only shadow from the first light
  	k_shadow = min(max(k_shadow, i), 1.0);
  	
    vec4 diffuse = gl_FrontMaterial.diffuse *
                   max(dot(N, normalize(gl_LightSource[i].position.xyz)), 0.0) *
                   gl_LightSource[i].diffuse;
    
    vec4 specular = gl_FrontMaterial.specular * 
                    max(pow(dot(N, gl_LightSource[i].halfVector.xyz), gl_FrontMaterial.shininess), 0.0) *
                    gl_LightSource[i].specular;
    
    color += (diffuse + specular) * k_shadow;
  } 
  
  vec4 texColor = texture2D(diffuseTex, diffuseTexCoords);
  gl_FragColor = color * ((1.0 - texColor.a) + texColor) * vertexColor;
}