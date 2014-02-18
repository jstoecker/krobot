package edu.miami.cs.krobot.model.joint;

import java.util.ArrayList;
import edu.miami.cs.js.math.vector.Vec3;
import edu.miami.cs.krobot.model.ModelPart;

/**
 * Base class for a joint with a single degree of freedom.
 * 
 * @author justin
 */
public abstract class Joint1DOF extends ModelPart {

  protected Vec3                axis            = new Vec3(0, 0, 1);
  protected float               value           = 0;
  protected float               unconstrainedValue;
  protected float               minValue;
  protected float               maxValue;
  protected ArrayList<Listener> listeners       = new ArrayList<Listener>();
  private boolean               changingValue   = false;

  public Joint1DOF() {
  }

  public Joint1DOF(String name, ModelPart parent, Vec3 offset, Vec3 axis, float minValue,
      float maxValue) {
    super(name, parent, offset);
    this.name = name;
    this.axis = axis;
    this.minValue = minValue;
    this.maxValue = maxValue;
    updateLocalTransform();
  }

  public Vec3 getAxis() {
    return axis;
  }

  public float getValue() {
    return value;
  }

  public float getMinValue() {
    return minValue;
  }

  public float getMaxValue() {
    return maxValue;
  }

  public float getRange() {
    return maxValue - minValue;
  }

  public float getUnconstrainedValue() {
    return unconstrainedValue;
  }

  public void setAxis(Vec3 axis) {
    this.axis = axis;
    updateLocalTransform();
  }

  public void setMinValue(float minValue) {
    this.minValue = minValue;
  }

  public void setMaxValue(float maxValue) {
    this.maxValue = maxValue;
  }

  /** Returns the current value in [0,1], where 0 -> min and 1 -> max */
  public float getNormValue() {
    return (value - minValue) / (maxValue - minValue);
  }

  /** Sets the current value */
  public void setValue(float value) {
    if (changingValue || this.value == value) return;

    changingValue = true;

    unconstrainedValue = value;
    this.value = Math.min(Math.max(unconstrainedValue, minValue), maxValue);
    updateLocalTransform();

    for (Listener l : listeners)
      l.valueChanged(this, value);

    changingValue = false;
  }

  public boolean isChangingValue() {
    return changingValue;
  }

  /** Sets value using a normalized value in [0,1], where 0 -> min and 1 -> max. */
  public void setNormValue(float nValue) {
    setValue((maxValue - minValue) * nValue + minValue);
  }

  @Override
  public String getName() {
    return name;
  }

  public void addListener(Listener l) {
    listeners.add(l);
  }

  public void removeListener(Listener l) {
    listeners.remove(l);
  }

  public interface Listener {
    void valueChanged(Joint1DOF joint, float newValue);
  }
}
