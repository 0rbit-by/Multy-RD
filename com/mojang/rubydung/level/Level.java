package com.mojang.rubydung.level;

import com.mojang.rubydung.Particle;
import com.mojang.rubydung.phys.AABB;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Level {
   public final int width;
   public final int height;
   public final int depth;
   private byte[] blocks;
   private int[] lightDepths;
   private ArrayList<LevelListener> levelListeners = new ArrayList();
   private List<Particle> particles = new ArrayList<>();
   private Random random = new Random();

   public Level(int w, int h, int d) {
      this.width = w;
      this.height = h;
      this.depth = d;
      this.blocks = new byte[w * h * d];
      this.lightDepths = new int[w * h];

      for(int x = 0; x < w; ++x) {
         for(int z = 0; z < h; ++z) {
            for(int y = 0; y < d; ++y) {
               int i = (y * this.height + z) * this.width + x;
               if (y == 0) this.blocks[i] = 4;
               else if (y == 42) this.blocks[i] = 2;
               else if (y >= 39 && y < 42) this.blocks[i] = 6;
               else if (y < 39) this.blocks[i] = 7;
               else this.blocks[i] = 0;
            }
         }
      }
      this.calcLightDepths(0, 0, w, h);
      this.load();
   }

   public void tick() {
      for(int i = 0; i < particles.size(); ++i) {
         Particle p = particles.get(i);
         p.tick();
         if (p.age > 40) {
            particles.remove(i--);
         }
      }
   }

   public List<Particle> getParticles() {
      return particles;
   }

   public void spawnParticles(int x, int y, int z, int tex) {
      for(int i = 0; i < 20; ++i) {
         float xd = (random.nextFloat() * 0.06F) - 0.03F;
         float yd = (random.nextFloat() * 0.04F) - 0.01F;
         float zd = (random.nextFloat() * 0.06F) - 0.03F;
         particles.add(new Particle(x + 0.5F, y + 0.5F, z + 0.5F, xd, yd, zd, tex));
      }
   }

   public void load() {
      try {
         File file = new File("level.dat");
         if (!file.exists()) return;
         DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(file)));
         dis.readFully(this.blocks);
         this.calcLightDepths(0, 0, this.width, this.height);
         for(int i = 0; i < this.levelListeners.size(); ++i) {
            ((LevelListener)this.levelListeners.get(i)).allChanged();
         }
         dis.close();
      } catch (Exception e) { e.printStackTrace(); }
   }

   public void save() {
      try {
         DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(new File("level.dat"))));
         dos.write(this.blocks);
         dos.close();
      } catch (Exception e) { e.printStackTrace(); }
   }

   public void calcLightDepths(int x0, int y0, int x1, int y1) {
      for(int x = x0; x < x0 + x1; ++x) {
         for(int z = y0; z < y0 + y1; ++z) {
            int oldDepth = this.lightDepths[x + z * this.width];
            int y;
            for(y = this.depth - 1; y > 0 && !this.isLightBlocker(x, y, z); --y) {
            }
            this.lightDepths[x + z * this.width] = y;
            if (oldDepth != y) {
               int yl0 = oldDepth < y ? oldDepth : y;
               int yl1 = oldDepth > y ? oldDepth : y;
               for(int i = 0; i < this.levelListeners.size(); ++i) {
                  ((LevelListener)this.levelListeners.get(i)).lightColumnChanged(x, z, yl0, yl1);
               }
            }
         }
      }
   }

   public void addListener(LevelListener levelListener) { this.levelListeners.add(levelListener); }

   public void removeListener(LevelListener levelListener) { this.levelListeners.remove(levelListener); }

   public int getTile(int x, int y, int z) {
      if (x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
         return this.blocks[(y * this.height + z) * this.width + x] & 255;
      }
      return 0;
   }

   public boolean isTile(int x, int y, int z) { return this.getTile(x, y, z) > 0; }

   public boolean isSolidTile(int x, int y, int z) { return this.isTile(x, y, z); }

   public boolean isOpaqueTile(int x, int y, int z) {
      int tile = this.getTile(x, y, z);
      if (tile == 0) return false;
      if (tile == 8) return false;
      return true;
   }

   public boolean isLightBlocker(int x, int y, int z) { return this.isOpaqueTile(x, y, z); }

   public ArrayList<AABB> getCubes(AABB aABB) {
      ArrayList<AABB> aABBs = new ArrayList();
      int x0 = (int)aABB.x0;
      int x1 = (int)(aABB.x1 + 1.0F);
      int y0 = (int)aABB.y0;
      int y1 = (int)(aABB.y1 + 1.0F);
      int z0 = (int)aABB.z0;
      int z1 = (int)(aABB.z1 + 1.0F);
      if (x0 < 0) x0 = 0;
      if (y0 < 0) y0 = 0;
      if (z0 < 0) z0 = 0;
      if (x1 > this.width) x1 = this.width;
      if (y1 > this.depth) y1 = this.depth;
      if (z1 > this.height) z1 = this.height;

      for(int x = x0; x < x1; ++x) {
         for(int y = y0; y < y1; ++y) {
            for(int z = z0; z < z1; ++z) {
               if (this.isSolidTile(x, y, z)) {
                  aABBs.add(new AABB((float)x, (float)y, (float)z, (float)(x + 1), (float)(y + 1), (float)(z + 1)));
               }
            }
         }
      }
      return aABBs;
   }

   public float getBrightness(int x, int y, int z) {
      if (x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
         int lightDepth = this.lightDepths[x + z * this.width];
         float diff = (y - lightDepth) / 5.0F;
         return Math.min(1.0F, Math.max(0.7F, 1.0F + diff));
      }
      return 1.0F;
   }

   public void setTile(int x, int y, int z, int type) {
      if (x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
         this.blocks[(y * this.height + z) * this.width + x] = (byte)type;
         this.calcLightDepths(x, z, 1, 1);
         for(int i = 0; i < this.levelListeners.size(); ++i) {
            ((LevelListener)this.levelListeners.get(i)).tileChanged(x, y, z);
         }
      }
   }
}