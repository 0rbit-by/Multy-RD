package com.mojang.rubydung.level;

public class Tile {
   public static Tile cobblestone = new Tile(0);
   public static Tile grass = new Tile(1);
   public static Tile planks = new Tile(2);
   public static Tile bedrock = new Tile(3);
   public static Tile diamond_block = new Tile(4);
   public static Tile dirt = new Tile(5);
   public static Tile stone = new Tile(6);
   public static Tile glass = new Tile(7);
   private int tex = 0;

   private Tile(int tex) {
      this.tex = tex;
   }

   public void render(Tesselator t, Level level, int layer, int x, int y, int z) {
      float u0 = (float)this.tex / 16.0F;
      float u1 = u0 + 0.0624375F;
      float v0 = 0.0F;
      float v1 = v0 + 0.0624375F;
      float c1 = 1.0F;
      float c2 = 0.8F;
      float c3 = 0.6F;
      float x0 = (float)x + 0.0F;
      float x1 = (float)x + 1.0F;
      float y0 = (float)y + 0.0F;
      float y1 = (float)y + 1.0F;
      float z0 = (float)z + 0.0F;
      float z1 = (float)z + 1.0F;

      if (!level.isOpaqueTile(x, y - 1, z)) {
         float br = level.getBrightness(x, y - 1, z) * c1;
         if (br == c1 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u0, v1); t.vertex(x0, y0, z1);
            t.tex(u0, v0); t.vertex(x0, y0, z0);
            t.tex(u1, v0); t.vertex(x1, y0, z0);
            t.tex(u1, v1); t.vertex(x1, y0, z1);
         }
      }

      if (!level.isOpaqueTile(x, y + 1, z)) {
         float br = level.getBrightness(x, y, z) * c1;
         if (br == c1 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u1, v1); t.vertex(x1, y1, z1);
            t.tex(u1, v0); t.vertex(x1, y1, z0);
            t.tex(u0, v0); t.vertex(x0, y1, z0);
            t.tex(u0, v1); t.vertex(x0, y1, z1);
         }
      }

      if (!level.isOpaqueTile(x, y, z - 1)) {
         float br = level.getBrightness(x, y, z - 1) * c2;
         if (br == c2 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u1, v0); t.vertex(x0, y1, z0);
            t.tex(u0, v0); t.vertex(x1, y1, z0);
            t.tex(u0, v1); t.vertex(x1, y0, z0);
            t.tex(u1, v1); t.vertex(x0, y0, z0);
         }
      }

      if (!level.isOpaqueTile(x, y, z + 1)) {
         float br = level.getBrightness(x, y, z + 1) * c2;
         if (br == c2 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u0, v0); t.vertex(x0, y1, z1);
            t.tex(u0, v1); t.vertex(x0, y0, z1);
            t.tex(u1, v1); t.vertex(x1, y0, z1);
            t.tex(u1, v0); t.vertex(x1, y1, z1);
         }
      }

      if (!level.isOpaqueTile(x - 1, y, z)) {
         float br = level.getBrightness(x - 1, y, z) * c3;
         if (br == c3 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u1, v0); t.vertex(x0, y1, z1);
            t.tex(u0, v0); t.vertex(x0, y1, z0);
            t.tex(u0, v1); t.vertex(x0, y0, z0);
            t.tex(u1, v1); t.vertex(x0, y0, z1);
         }
      }

      if (!level.isOpaqueTile(x + 1, y, z)) {
         float br = level.getBrightness(x + 1, y, z) * c3;
         if (br == c3 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u0, v1); t.vertex(x1, y0, z1);
            t.tex(u1, v1); t.vertex(x1, y0, z0);
            t.tex(u1, v0); t.vertex(x1, y1, z0);
            t.tex(u0, v0); t.vertex(x1, y1, z1);
         }
      }
   }

   public void renderFace(Tesselator t, int x, int y, int z, int face) {
      float x0 = (float)x;
      float x1 = (float)x + 1.0F;
      float y0 = (float)y;
      float y1 = (float)y + 1.0F;
      float z0 = (float)z;
      float z1 = (float)z + 1.0F;

      if (face == 0) {
         t.vertex(x0, y0, z1); t.vertex(x0, y0, z0); t.vertex(x1, y0, z0); t.vertex(x1, y0, z1);
      } else if (face == 1) {
         t.vertex(x1, y1, z1); t.vertex(x1, y1, z0); t.vertex(x0, y1, z0); t.vertex(x0, y1, z1);
      } else if (face == 2) {
         t.vertex(x0, y1, z0); t.vertex(x1, y1, z0); t.vertex(x1, y0, z0); t.vertex(x0, y0, z0);
      } else if (face == 3) {
         t.vertex(x0, y1, z1); t.vertex(x0, y0, z1); t.vertex(x1, y0, z1); t.vertex(x1, y1, z1);
      } else if (face == 4) {
         t.vertex(x0, y1, z1); t.vertex(x0, y1, z0); t.vertex(x0, y0, z0); t.vertex(x0, y0, z1);
      } else if (face == 5) {
         t.vertex(x1, y1, z1); t.vertex(x1, y0, z1); t.vertex(x1, y0, z0); t.vertex(x1, y1, z0);
      }
   }
}