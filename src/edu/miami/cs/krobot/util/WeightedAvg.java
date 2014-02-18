package edu.miami.cs.krobot.util;

import java.util.LinkedList;

/**
 * Weighted running average. Maintains a history of N arrays of values. When getAverage is called,
 * the new array is added to the end of the history. The average returned is the weighted average
 * all arrays in the history:<br>
 * <br>
 * avg[i] = (history[i] * weights[i] + history[i+1] * weights[i+1] + ... + history[N-1] *
 * weights[N-1]) / totalWeight
 * 
 * @author justin
 */
public class WeightedAvg {

  LinkedList<float[]> history = new LinkedList<float[]>();
  float[]             weights;
  float               totalWeight;

  public WeightedAvg(float[] weights) {
    this.weights = weights;
    for (int i = 0; i < weights.length; i++)
      totalWeight += weights[i];
  }

  public float[] getAverage(float[] newValues) {

    history.add(newValues);
    if (history.size() > weights.length) history.removeFirst();

    int j = 0;
    float[] average = new float[newValues.length];
    for (float[] prevValues : history) {
      for (int i = 0; i < average.length; i++)
        average[i] += prevValues[i] * weights[j];
      j++;
    }

    for (int i = 0; i < average.length; i++)
      average[i] /= totalWeight;

    history.removeLast();
    history.add(average);

    return average;
  }
}
