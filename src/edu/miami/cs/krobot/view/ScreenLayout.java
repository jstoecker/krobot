package edu.miami.cs.krobot.view;

import edu.miami.cs.js.jgloo.Viewport;

/**
 * Maintains viewport arrangements that can be used to render 1-4 individual views on the same
 * screen space. Viewports are arranged in a grid.
 * 
 * @author justin
 */
public class ScreenLayout {

  public enum Position {
    SCREEN,
    CENTER,
    LEFT,
    RIGHT,
    TOP,
    BOTTOM,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
  }

  private int pad = 4;
  Viewport    screen;
  Viewport    center;
  Viewport    left;
  Viewport    right;
  Viewport    top;
  Viewport    bottom;
  Viewport    topLeft;
  Viewport    topRight;
  Viewport    bottomLeft;
  Viewport    bottomRight;

  public void setBorderPad(int pad) {
    this.pad = pad;
    update(screen.x, screen.y, screen.w, screen.h);
  }

  public Viewport get(Position pos) {
    switch (pos) {
    case SCREEN: return screen;
    case CENTER: return center;
    case LEFT: return left;
    case RIGHT: return right;
    case TOP: return top;
    case BOTTOM: return bottom;
    case TOP_LEFT: return topLeft;
    case TOP_RIGHT: return topRight;
    case BOTTOM_LEFT: return bottomLeft;
    case BOTTOM_RIGHT: return bottomRight;
    default: return null;
    }
  }

  public void update(int x, int y, int w, int h) {
    screen = new Viewport(x, y, w, h);
    center = new Viewport(x + pad, y + pad, w - 2 * pad, h - 2 * pad);
    bottom = new Viewport(center.x, center.y, center.w, (center.h - pad) / 2);
    top = new Viewport(center.x, center.y + center.h - bottom.h, center.w, bottom.h);
    left = new Viewport(center.x, center.y, (center.w - pad) / 2, center.h);
    right = new Viewport(center.x + center.w - left.w, center.y, left.w, center.h);
    topLeft = new Viewport(left.x, top.y, left.w, top.h);
    topRight = new Viewport(right.x, top.y, right.w, top.h);
    bottomLeft = new Viewport(left.x, bottom.y, left.w, bottom.h);
    bottomRight = new Viewport(right.x, bottom.y, right.w, bottom.h);
  }
}
