package edu.miami.cs.krobot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Configuration settings loaded on startup. This can be set manually before starting the main
 * application or loaded from a YAML file.
 * 
 * @author justin
 */
public class Config {

  public String   sendAddress    = "127.0.0.1";
  public int      sendPort       = 32771;
  public boolean  msaaEnabled    = false;
  public int      msaaSamples    = 8;
  public boolean  filterSkeleton = false;
  public double[] filterSkelCoeffB;
  public double[] filterSkelCoeffA;
  public boolean  shadows        = false;
  public int      shadowSize     = 512;

  public void setFilterCoeffB(List<Double> filterCoeffB) {
    this.filterSkelCoeffB = new double[filterCoeffB.size()];
    for (int i = 0; i < filterCoeffB.size(); i++)
      this.filterSkelCoeffB[i] = filterCoeffB.get(i);
  }

  public void setFilterCoeffA(List<Double> filterCoeffA) {
    this.filterSkelCoeffA = new double[filterCoeffA.size()];
    for (int i = 0; i < filterCoeffA.size(); i++)
      this.filterSkelCoeffA[i] = filterCoeffA.get(i);
  }

  public static Config load(File file) {
    try {
      Yaml yaml = new Yaml(new Constructor(Config.class));
      Config config = (Config) yaml.load(new FileInputStream(file));

      return config;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }
}
