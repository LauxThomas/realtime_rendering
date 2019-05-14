#version 330 core
layout (triangles) in;
layout (triangle_strip, max_vertices = 20) out;

void constructTreeTrunk(vec4 position)
{
    vec4 v0 = position+ vec4(-1, 0, -1, 0); //uhl
    vec4 v1 = position+ vec4(1, 0, -1, 0);  //uv
    vec4 v2 = position+ vec4(0, 0, 1, 0);   //uhr
    vec4 v3 = position+ vec4(-1, 2, -1, 0)*0.8; //ohl
    vec4 v4 = position+ vec4(1, 2, -1, 0)*0.8;  //ov
    vec4 v5 = position+ vec4(0, 2, 1, 0)*0.8;   //ohr

    //Bottom Pane
    gl_Position = v0;
    EmitVertex();
    gl_Position = v1;
    EmitVertex();
    gl_Position = v2;
    EmitVertex();
    EndPrimitive();

    //Top Pane
    gl_Position = v3;
    EmitVertex();
    gl_Position = v4;
    EmitVertex();
    gl_Position = v5;
    EmitVertex();
    EndPrimitive();

    //frontleft Pane1
    gl_Position = v0;
    EmitVertex();
    gl_Position = v1;
    EmitVertex();
    gl_Position = v4;
    EmitVertex();
    EndPrimitive();
    //frontleft Pane2
    gl_Position = v0;
    EmitVertex();
    gl_Position = v4;
    EmitVertex();
    gl_Position = v3;
    EmitVertex();
    EndPrimitive();

    //frontright Pane1
    gl_Position = v1;
    EmitVertex();
    gl_Position = v2;
    EmitVertex();
    gl_Position = v4;
    EmitVertex();
    EndPrimitive();
    //frontright Pane2
    gl_Position = v4;
    EmitVertex();
    gl_Position = v2;
    EmitVertex();
    gl_Position = v5;
    EmitVertex();
    EndPrimitive();

    //back Pane1
    gl_Position = v0;
    EmitVertex();
    gl_Position = v2;
    EmitVertex();
    gl_Position = v5;
    EmitVertex();
    EndPrimitive();
    //back Pane2
    gl_Position = v5;
    EmitVertex();
    gl_Position = v3;
    EmitVertex();
    gl_Position = v0;
    EmitVertex();
    EndPrimitive();

}

void main() {
    constructTreeTrunk(gl_in[0].gl_Position);
}