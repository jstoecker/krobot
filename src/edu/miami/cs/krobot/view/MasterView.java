package edu.miami.cs.krobot.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import edu.miami.cs.js.jgloo.Viewport;
import edu.miami.cs.js.jgloo.content.ContentManager;
import edu.miami.cs.krobot.model.Model;

public class MasterView implements GLEventListener {

  public static ContentManager           CONTENT;

  protected GLCanvas                     canvas;
  ScreenLayout                           layout        = new ScreenLayout();
  ArrayList<GLView>                      views         = new ArrayList<GLView>();
  HashMap<GLView, ScreenLayout.Position> viewPositions = new HashMap<GLView, ScreenLayout.Position>();

  public MasterView(Model model) {
    CONTENT = new ContentManager(model.meshDir, model.textureDir, model.materialDir, null, true);
  }

  public void setCanvas(GLCanvas canvas) {
    this.canvas = canvas;
  }

  public List<GLView> getViews() {
    return views;
  }

  public void clearViews() {
    views.clear();
    viewPositions.clear();
  }

  public void setView(GLView view, ScreenLayout.Position position) {
    views.add(view);
    viewPositions.put(view, position);
  }

  @Override
  public void display(GLAutoDrawable drawable) {
    GL2 gl = drawable.getGL().getGL2();

    gl.glClearColor(0.4f, 0.4f, 0.4f, 1.0f);
    gl.glClear(GL.GL_COLOR_BUFFER_BIT);
    gl.glEnable(GL.GL_SCISSOR_TEST);
    for (GLView view : views) {
      drawGLView(gl, view);
    }
    gl.glDisable(GL.GL_SCISSOR_TEST);
  }

  private void drawGLView(GL2 gl, GLView view) {
    Viewport vp = layout.get(viewPositions.get(view));

    gl.glScissor(vp.x, vp.y, vp.w, vp.h);
    vp.apply(gl);

    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glPushMatrix();
    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glPushMatrix();

    view.setViewport(vp);
    view.draw(gl);

    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glPopMatrix();
    gl.glMatrixMode(GL2.GL_MODELVIEW);
    gl.glPopMatrix();
  }

  @Override
  public void dispose(GLAutoDrawable drawable) {
    GL2 gl = drawable.getGL().getGL2();

    CONTENT.disposeAll(gl);

    for (GLView view : views)
      view.dispose(gl);
  }

  @Override
  public void init(GLAutoDrawable drawable) {
    GL2 gl = drawable.getGL().getGL2();

    CONTENT.initialize(gl);

    for (GLView view : views)
      view.init(gl);
  }

  @Override
  public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
    layout.update(x, y, w, h);
  }
}
