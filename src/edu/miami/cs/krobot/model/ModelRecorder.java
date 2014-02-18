package edu.miami.cs.krobot.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ModelRecorder implements ModelListener {

  private Model          model;
  private BufferedWriter writer;
  private boolean        active;

  public ModelRecorder(Model model) {
    this.model = model;
    model.addListener(this);
  }

  public void enable(String fileName) {
    try {
      writer = new BufferedWriter(new FileWriter(new File(fileName)));
      active = true;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void disable() {
    active = false;
    if (writer != null) {
      try {
        writer.close();
        writer = null;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void recordFrame() {
    try {
      writer.write(model.getStatus().toString());
      writer.write("\n");
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void modelCommitted(Model model) {
    if (!active) return;
    recordFrame();
  }
}
