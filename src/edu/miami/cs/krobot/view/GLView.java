package edu.miami.cs.krobot.view;

import javax.media.opengl.GL2;
import edu.miami.cs.js.jgloo.Viewport;

public abstract class GLView {

  protected final String     name;
  protected final MasterView mainView;
  protected Viewport         viewport;

  public GLView(String name, MasterView mainView) {
    this.name = name;
    this.mainView = mainView;
  }

  void setViewport(Viewport viewport) {
    this.viewport = viewport;
  }

  public Viewport getViewport() {
    return viewport;
  }
  
  public String getName() {
    return name;
  }

  public abstract void init(GL2 gl);

  public abstract void dispose(GL2 gl);

  public abstract void draw(GL2 gl);

  public void repaint() {
    mainView.canvas.repaint();
  }
}
