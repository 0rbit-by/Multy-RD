package com.mojang.rubydung;

import com.mojang.rubydung.level.Chunk;
import com.mojang.rubydung.level.Level;
import com.mojang.rubydung.level.LevelRenderer;
import com.mojang.rubydung.level.Tesselator;
import java.awt.Component;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.swing.JOptionPane;
import com.mojang.rubydung.phys.AABB;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class RubyDung implements Runnable {
   private static final boolean FULLSCREEN_MODE = false;
   private int width;
   private int height;
   private FloatBuffer fogColor = BufferUtils.createFloatBuffer(4);
   private Timer timer = new Timer(60.0F);
   private Level level;
   private LevelRenderer levelRenderer;
   private Player player;
   private IntBuffer viewportBuffer = BufferUtils.createIntBuffer(16);
   private IntBuffer selectBuffer = BufferUtils.createIntBuffer(2000);
   private HitResult hitResult = null;
   private int[] inventory = {2, 1, 3, 4, 5, 6, 7, 8};
   private int selectedIndex = 0;
   private int iconTexture;

   public void init() throws LWJGLException, IOException {
      int col = 920330;
      float fr = 0.5F;
      float fg = 0.8F;
      float fb = 1.0F;
      this.fogColor.put(new float[]{(float)(col >> 16 & 255) / 255.0F, (float)(col >> 8 & 255) / 255.0F, (float)(col & 255) / 255.0F, 1.0F});
      this.fogColor.flip();
      Display.setDisplayMode(new DisplayMode(1024, 768));
      Display.create();
      Display.setVSyncEnabled(true);
      Keyboard.create();
      Mouse.create();
      this.width = Display.getDisplayMode().getWidth();
      this.height = Display.getDisplayMode().getHeight();
      this.iconTexture = Textures.loadTexture("/icons.png", 9728);
      GL11.glEnable(3553);
      GL11.glShadeModel(7425);
      GL11.glClearColor(fr, fg, fb, 0.0F);
      GL11.glClearDepth((double)1.0F);
      GL11.glEnable(2929);
      GL11.glDepthFunc(515);
      GL11.glMatrixMode(5889);
      GL11.glLoadIdentity();
      GL11.glMatrixMode(5888);
      this.level = new Level(256, 256, 64);
      this.levelRenderer = new LevelRenderer(this.level);
      this.player = new Player(this.level);
      Mouse.setGrabbed(true);
   }

   public void destroy() {
      this.level.save();
      Mouse.destroy();
      Keyboard.destroy();
      Display.destroy();
   }

   public void run() {
      try {
         this.init();
      } catch (Exception e) {
         JOptionPane.showMessageDialog((Component)null, e.toString(), "Failed to start RubyDung", 0);
         System.exit(0);
      }
      long lastTime = System.currentTimeMillis();
      int frames = 0;
      try {
         while(!Keyboard.isKeyDown(1) && !Display.isCloseRequested()) {
            this.timer.advanceTime();
            for(int i = 0; i < this.timer.ticks; ++i) {
               this.tick();
            }
            this.render(this.timer.a);
            ++frames;
            while(System.currentTimeMillis() >= lastTime + 1000L) {
               System.out.println(frames + " fps, " + Chunk.updates);
               Chunk.updates = 0;
               lastTime += 1000L;
               frames = 0;
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         this.destroy();
      }
   }

   public void tick() {
      this.player.tick();
      this.level.tick();
   }

   private void moveCameraToPlayer(float a) {
      GL11.glTranslatef(0.0F, 0.0F, -0.3F);
      GL11.glRotatef(this.player.xRot, 1.0F, 0.0F, 0.0F);
      GL11.glRotatef(this.player.yRot, 0.0F, 1.0F, 0.0F);
      float x = this.player.xo + (this.player.x - this.player.xo) * a;
      float y = this.player.yo + (this.player.y - this.player.yo) * a;
      float z = this.player.zo + (this.player.z - this.player.zo) * a;
      GL11.glTranslatef(-x, -y, -z);
   }

   private void setupCamera(float a) {
      GL11.glMatrixMode(5889);
      GL11.glLoadIdentity();
      GLU.gluPerspective(70.0F, (float)this.width / (float)this.height, 0.05F, 1000.0F);
      GL11.glMatrixMode(5888);
      GL11.glLoadIdentity();
      this.moveCameraToPlayer(a);
   }

   private void setupPickCamera(float a, int x, int y) {
      GL11.glMatrixMode(5889);
      GL11.glLoadIdentity();
      this.viewportBuffer.clear();
      GL11.glGetInteger(2978, this.viewportBuffer);
      this.viewportBuffer.flip();
      this.viewportBuffer.limit(16);
      GLU.gluPickMatrix((float)x, (float)y, 5.0F, 5.0F, this.viewportBuffer);
      GLU.gluPerspective(70.0F, (float)this.width / (float)this.height, 0.05F, 1000.0F);
      GL11.glMatrixMode(5888);
      GL11.glLoadIdentity();
      this.moveCameraToPlayer(a);
   }

   private void pick(float a) {
      this.selectBuffer.clear();
      GL11.glSelectBuffer(this.selectBuffer);
      GL11.glRenderMode(7170);
      this.setupPickCamera(a, this.width / 2, this.height / 2);
      this.levelRenderer.pick(this.player);
      int hits = GL11.glRenderMode(7168);
      this.selectBuffer.flip();
      this.selectBuffer.limit(this.selectBuffer.capacity());
      long closest = 0L;
      int[] names = new int[10];
      int hitNameCount = 0;
      for(int i = 0; i < hits; ++i) {
         int nameCount = this.selectBuffer.get();
         long minZ = (long)this.selectBuffer.get();
         this.selectBuffer.get();
         if (minZ >= closest && i != 0) {
            for(int j = 0; j < nameCount; ++j) this.selectBuffer.get();
         } else {
            closest = minZ;
            hitNameCount = nameCount;
            for(int j = 0; j < nameCount; ++j) names[j] = this.selectBuffer.get();
         }
      }
      if (hitNameCount > 0) this.hitResult = new HitResult(names[0], names[1], names[2], names[3], names[4]);
      else this.hitResult = null;
   }

   public void render(float a) {
      float xo = (float)Mouse.getDX();
      float yo = (float)Mouse.getDY();
      this.player.turn(xo, yo);
      this.pick(a);

      while(Mouse.next()) {
         if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && this.hitResult != null) {
            int x = this.hitResult.x;
            int y = this.hitResult.y;
            int z = this.hitResult.z;
            int tex = this.level.getTile(x, y, z);
            this.level.setTile(x, y, z, 0);
            this.level.spawnParticles(x, y, z, tex);
         }
         if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState() && this.hitResult != null) {
            int x = this.hitResult.x;
            int y = this.hitResult.y;
            int z = this.hitResult.z;
            if (this.hitResult.f == 0) --y;
            if (this.hitResult.f == 1) ++y;
            if (this.hitResult.f == 2) --z;
            if (this.hitResult.f == 3) ++z;
            if (this.hitResult.f == 4) --x;
            if (this.hitResult.f == 5) ++x;
            AABB blockBB = new AABB((float)x, (float)y, (float)z, (float)(x + 1), (float)(y + 1), (float)(z + 1));
            if (!this.player.bb.intersects(blockBB)) {
               this.level.setTile(x, y, z, inventory[selectedIndex]);
            }
         }
      }

      while(Keyboard.next()) {
         if (Keyboard.getEventKey() == Keyboard.KEY_F && Keyboard.getEventKeyState()) {
            player.flying = !player.flying;
         }
         if (Keyboard.getEventKey() == Keyboard.KEY_Q && Keyboard.getEventKeyState()) {
            selectedIndex = (selectedIndex + 1) % inventory.length;
         }
         if (Keyboard.getEventKey() == 28 && Keyboard.getEventKeyState()) {
            this.level.save();
         }
         if ((Keyboard.getEventKey() == Keyboard.KEY_BACKSLASH || Keyboard.getEventCharacter() == '\\') && Keyboard.getEventKeyState()) {
            java.io.File file = new java.io.File("level.dat");
            if (file.exists()) file.delete();
            this.level = new Level(256, 256, 64);
            this.levelRenderer = new LevelRenderer(this.level);
            this.player = new Player(this.level);
         }
      }

      GL11.glClear(16640);
      this.setupCamera(a);
      GL11.glEnable(2884);
      GL11.glEnable(2912);
      GL11.glFogi(2917, 2048);
      GL11.glFogf(2914, 0.2F);
      GL11.glFog(2918, this.fogColor);
      GL11.glDisable(2912);
      this.levelRenderer.render(this.player, 0);
      GL11.glEnable(2912);
      this.levelRenderer.render(this.player, 1);

      GL11.glEnable(3553);
      GL11.glEnable(2929);
      GL11.glDisable(GL11.GL_CULL_FACE);
      for(Particle p : level.getParticles()) {
         p.render(player.yRot, player.xRot);
      }
      GL11.glEnable(GL11.GL_CULL_FACE);

      GL11.glDisable(3553);
      if (this.hitResult != null) this.levelRenderer.renderHit(this.hitResult);
      GL11.glDisable(2912);
      GL11.glDisable(2929);
      GL11.glDisable(3553);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glLineWidth(1.5F);
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.7F);
      GL11.glMatrixMode(5889);
      GL11.glPushMatrix();
      GL11.glLoadIdentity();
      GL11.glOrtho(0.0D, (double)this.width, (double)this.height, 0.0D, -1.0D, 1.0D);
      GL11.glMatrixMode(5888);
      GL11.glPushMatrix();
      GL11.glLoadIdentity();
      GL11.glBegin(1);
      int centerX = this.width / 2;
      int centerY = this.height / 2;
      int size = 6;
      GL11.glVertex2i(centerX - size, centerY);
      GL11.glVertex2i(centerX + size, centerY);
      GL11.glVertex2i(centerX, centerY - size);
      GL11.glVertex2i(centerX, centerY + size);
      GL11.glEnd();
      GL11.glPopMatrix();
      GL11.glMatrixMode(5889);
      GL11.glPopMatrix();
      GL11.glMatrixMode(5888);
      GL11.glDisable(3042);

      this.levelRenderer.renderIcon(this.inventory[this.selectedIndex], this.iconTexture, this.width, this.height);

      GL11.glEnable(3553);
      GL11.glEnable(2929);
      Display.update();
   }

   public static void checkError() {
      int e = GL11.glGetError();
      if (e != 0) throw new IllegalStateException(GLU.gluErrorString(e));
   }

   public static void main(String[] args) throws LWJGLException {
      (new Thread(new RubyDung())).start();
   }
}