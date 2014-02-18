package edu.miami.cs.krobot.util;
/**
 * Infinite impulse response filter.<br>
 * <br>
 * y[n] = 1/a[0] * (b0*x[n] + b1*x[n-1] + ... + bP*x[n-P] - a1*y[n-1] - a2*y[n-2] - ... - aQ*y[n-Q]) <br>
 * <br>
 * x = input signal<br>
 * y = output signal<br>
 * b = feedforward coefficients<br>
 * a = feedback coefficients<br>
 * P = feedforward filter order<br>
 * Q = feedback filter order<br>
 * 
 * @author justin
 */
public class IIRFilter {

  double[] b, a, x, y;

  /**
   * Constructs an IIR filter given a set of coefficients.
   * 
   * @param b - feedforward coefficients
   * @param a - feedback coefficients
   */
  public IIRFilter(double[] b, double[] a) {
    this.b = b;
    this.a = a;
    x = new double[b.length];
    y = new double[a.length];
  }
  
  /**
   * Applies filter to the input sample.
   */
  public double filter(double sample) {
    System.arraycopy(x, 0, x, 1, x.length - 1);
    x[0] = sample;
    
    System.arraycopy(y, 0, y, 1, y.length - 1);
    y[0] = 0;
    for (int i = 0; i < b.length; i++)
      y[0] += b[i] * x[i];
    for (int i = 1; i < a.length; i++)
      y[0] -= a[i] * y[i];
    y[0] /= a[0];
    
    return y[0];
  }

  /**
   * Applies filter to an entire signal and returns a new array of values.
   */
  public double[] filter(double[] samples) {
    double[] filtered = new double[samples.length];
    for (int i = 0; i < samples.length; i++)
      filtered[i] = filter(samples[i]);
    return filtered;
  }

  /**
   * Creates a second-order Butterworth low-pass filter with a provided sampling rate and cutoff
   * frequency.
   * 
   * @param sampleRate - sampling rate of input signal in Hz
   * @param cutoff - cutoff frequency in Hz
   */
  public static IIRFilter butterworth2(double sampleRate, double cutoff) {
    // http://baumdevblog.blogspot.com/2010/11/butterworth-lowpass-filter-coefficients.html
    double sqrt2 = Math.sqrt(2.0);
    double QcRaw = (2 * Math.PI * cutoff) / sampleRate;
    double QcWarp = Math.tan(QcRaw);
    double gain = 1 / (1 + sqrt2 / QcWarp + 2 / (QcWarp * QcWarp));

    double[] a = { 1, (2 - 2 * 2 / (QcWarp * QcWarp)) * gain,
        (1 - sqrt2 / QcWarp + 2 / (QcWarp * QcWarp)) * gain };

    double[] b = { 1 * gain, 2 * gain, 1 * gain };

    return new IIRFilter(b, a);
  }
}