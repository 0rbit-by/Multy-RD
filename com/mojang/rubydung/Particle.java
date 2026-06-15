package com.mojang.rubydung;

import org.lwjgl.opengl.GL11;
import java.util.Random;

public class Particle {
    public float x, y, z, xd, yd, zd;
    public int tex, age;
    private float uOffset, vOffset;
    private static Random rand = new Random();

    public Particle(float x, float y, float z, float xd, float yd, float zd, int tex) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.tex = tex;
        this.age = 0;
        this.uOffset = rand.nextFloat() * 0.5F / 16.0F;
        this.vOffset = rand.nextFloat() * 0.5F / 16.0F;
    }

    public void tick() {
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;
        this.yd -= 0.002F;
        this.age++;
    }

    public void render(float rotY, float rotX) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);
        GL11.glRotatef(-rotY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(rotX, 1.0F, 0.0F, 0.0F);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        int id = this.tex - 1;
        float uBase = (float)(id % 16) / 16.0F;
        float vBase = (float)(id / 16) / 16.0F;

        float u0 = uBase + uOffset;
        float v0 = vBase + vOffset;
        float u1 = u0 + 0.5F / 16.0F;
        float v1 = v0 + 0.5F / 16.0F;

        float s = 0.07F;

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(u0, v0); GL11.glVertex3f(-s, -s, 0.0F);
        GL11.glTexCoord2f(u0, v1); GL11.glVertex3f(-s, s, 0.0F);
        GL11.glTexCoord2f(u1, v1); GL11.glVertex3f(s, s, 0.0F);
        GL11.glTexCoord2f(u1, v0); GL11.glVertex3f(s, -s, 0.0F);
        GL11.glEnd();
        GL11.glPopMatrix();
    }
}