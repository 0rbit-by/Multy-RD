package com.mojang.rubydung.level;

import com.mojang.rubydung.HitResult;
import com.mojang.rubydung.Player;
import com.mojang.rubydung.phys.AABB;
import org.lwjgl.opengl.GL11;

public class LevelRenderer implements LevelListener {
   private static final int CHUNK_SIZE = 16;
   private Level level;
   private Chunk[] chunks;
   private int xChunks;
   private int yChunks;
   private int zChunks;
   Tesselator t = new Tesselator();

   public LevelRenderer(Level level) {
      this.level = level;
      level.addListener(this);
      this.xChunks = level.width / 16;
      this.yChunks = level.depth / 16;
      this.zChunks = level.height / 16;
      this.chunks = new Chunk[this.xChunks * this.yChunks * this.zChunks];

      for(int x = 0; x < this.xChunks; ++x) {
         for(int y = 0; y < this.yChunks; ++y) {
            for(int z = 0; z < this.zChunks; ++z) {
               int x0 = x * 16;
               int y0 = y * 16;
               int z0 = z * 16;
               int x1 = (x + 1) * 16;
               int y1 = (y + 1) * 16;
               int z1 = (z + 1) * 16;
               if (x1 > level.width) {
                  x1 = level.width;
               }

               if (y1 > level.depth) {
                  y1 = level.depth;
               }

               if (z1 > level.height) {
                  z1 = level.height;
               }

               this.chunks[(x + y * this.xChunks) * this.zChunks + z] = new Chunk(level, x0, y0, z0, x1, y1, z1);
            }
         }
      }

   }

   public void render(Player player, int layer) {
      GL11.glEnable(GL11.GL_ALPHA_TEST);
      GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

      Chunk.rebuiltThisFrame = 0;
      Frustum frustum = Frustum.getFrustum();

      for(int i = 0; i < this.chunks.length; ++i) {
         if (frustum.cubeInFrustum(this.chunks[i].aabb)) {
            this.chunks[i].render(layer);
         }
      }

      GL11.glDisable(GL11.GL_ALPHA_TEST);
   }

   public void renderIcon(int blockId, int textureId, int screenWidth, int screenHeight) {
      GL11.glDisable(GL11.GL_DEPTH_TEST);
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GL11.glMatrixMode(5889);
      GL11.glPushMatrix();
      GL11.glLoadIdentity();
      GL11.glOrtho(0.0D, (double)screenWidth, (double)screenHeight, 0.0D, -1.0D, 1.0D);
      GL11.glMatrixMode(5888);
      GL11.glPushMatrix();
      GL11.glLoadIdentity();
      GL11.glEnable(GL11.GL_TEXTURE_2D);
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

      int id = blockId - 1;
      float u0 = (float)(id % 16) / 16.0F;
      float v0 = (float)(id / 16) / 16.0F;
      float u1 = u0 + 1.0F / 16.0F;
      float v1 = v0 + 1.0F / 16.0F;

      this.t.init();
      this.t.tex(u0, v0);
      this.t.vertex(10.0F, 10.0F, 0.0F);
      this.t.tex(u0, v1);
      this.t.vertex(10.0F, 74.0F, 0.0F);
      this.t.tex(u1, v1);
      this.t.vertex(74.0F, 74.0F, 0.0F);
      this.t.tex(u1, v0);
      this.t.vertex(74.0F, 10.0F, 0.0F);
      this.t.flush();

      GL11.glDisable(GL11.GL_BLEND);
      GL11.glPopMatrix();
      GL11.glMatrixMode(5889);
      GL11.glPopMatrix();
      GL11.glMatrixMode(5888);
      GL11.glEnable(GL11.GL_DEPTH_TEST);
   }

   public void pick(Player player) {
      float r = 3.0F;
      AABB box = player.bb.grow(r, r, r);
      int x0 = (int)box.x0;
      int x1 = (int)(box.x1 + 1.0F);
      int y0 = (int)box.y0;
      int y1 = (int)(box.y1 + 1.0F);
      int z0 = (int)box.z0;
      int z1 = (int)(box.z1 + 1.0F);
      GL11.glInitNames();

      for(int x = x0; x < x1; ++x) {
         GL11.glPushName(x);

         for(int y = y0; y < y1; ++y) {
            GL11.glPushName(y);

            for(int z = z0; z < z1; ++z) {
               GL11.glPushName(z);
               if (this.level.isSolidTile(x, y, z)) {
                  GL11.glPushName(0);

                  for(int i = 0; i < 6; ++i) {
                     GL11.glPushName(i);
                     this.t.init();
                     Tile.cobblestone.renderFace(this.t, x, y, z, i);
                     this.t.flush();
                     GL11.glPopName();
                  }

                  GL11.glPopName();
               }

               GL11.glPopName();
            }

            GL11.glPopName();
         }

         GL11.glPopName();
      }

   }

   public void renderHit(HitResult h) {
      GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

      GL11.glEnable(GL11.GL_DEPTH_TEST);
      GL11.glDepthFunc(GL11.GL_LEQUAL);

      GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
      GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
      GL11.glPolygonOffset(-1.0F, -1.0F);

      GL11.glDisable(GL11.GL_LIGHTING);
      GL11.glDisable(GL11.GL_TEXTURE_2D);

      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.2F);

      this.t.init();
      for (int i = 0; i < 6; i++) {
         Tile.cobblestone.renderFace(this.t, h.x, h.y, h.z, i);
      }
      this.t.flush();

      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
      GL11.glLineWidth(2.0F);
      GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.2F);

      this.t.init();
      for (int i = 0; i < 6; i++) {
         Tile.cobblestone.renderFace(this.t, h.x, h.y, h.z, i);
      }
      this.t.flush();

      GL11.glPopAttrib();
   }

   public void setDirty(int x0, int y0, int z0, int x1, int y1, int z1) {
      x0 /= 16;
      x1 /= 16;
      y0 /= 16;
      y1 /= 16;
      z0 /= 16;
      z1 /= 16;
      if (x0 < 0) {
         x0 = 0;
      }

      if (y0 < 0) {
         y0 = 0;
      }

      if (z0 < 0) {
         z0 = 0;
      }

      if (x1 >= this.xChunks) {
         x1 = this.xChunks - 1;
      }

      if (y1 >= this.yChunks) {
         y1 = this.yChunks - 1;
      }

      if (z1 >= this.zChunks) {
         z1 = this.zChunks - 1;
      }

      for(int x = x0; x <= x1; ++x) {
         for(int y = y0; y <= y1; ++y) {
            for(int z = z0; z <= z1; ++z) {
               this.chunks[(x + y * this.xChunks) * this.zChunks + z].setDirty();
            }
         }
      }

   }

   public void tileChanged(int x, int y, int z) {
      this.setDirty(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
   }

   public void lightColumnChanged(int x, int z, int y0, int y1) {
      this.setDirty(x - 1, y0 - 1, z - 1, x + 1, y1 + 1, z + 1);
   }

   public void allChanged() {
      this.setDirty(0, 0, 0, this.level.width, this.level.depth, this.level.height);
   }
}