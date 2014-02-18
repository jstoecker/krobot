package edu.miami.cs.krobot.model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import edu.miami.cs.krobot.model.joint.Joint1DOF;

/**
 * Sends joint angles to a UDP port on a host.
 * 
 * @author justin
 */
public class ModelMessenger implements ModelListener {

  boolean        active  = false;
  DatagramSocket socket;
  InetAddress    addr;
  Model          model;
  int            port;
  boolean        newData = false;

  public ModelMessenger(Model model, String addr, int port) {
    this.model = model;
    model.addListener(this);
//    setHost(addr, port);
  }

  public void setHost(String addr, int port) {

    if (this.socket != null) {
      socket.close();
    }

    try {
      this.port = port;
      socket = new DatagramSocket(32999);
      this.addr = InetAddress.getByName(addr);

    } catch (SocketException e) {
      e.printStackTrace();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
  }

  public void setActive(boolean active) {
    this.active = active;
  }
  
  public void sendStatus() {
    String msg = model.getStatus().toString() + "\n";
    DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), addr, port);

    try {
      socket.send(packet);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void modelCommitted(Model model) {
    if (active) sendStatus();
  }
}
