package com.codename1.impl.ios;
import com.codename1.ui.Display;

public class Main implements Runnable{
  
  private #MAIN_CLASS# ma;
  
  public static void main(String agrs[]){
    #CODENAMEONE_REGISTER_NATIVE_STUBS#
  
    Display.init(new Main());
  }

  public void run() {
    ma = new #MAIN_CLASS#();
    IOSImplementation.setMainClass(ma);
    ma.init(this);
    ma.start();
  }
  
}