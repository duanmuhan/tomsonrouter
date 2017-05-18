package com.cgs.rfa.lession2;

import com.reuters.rfa.common.Context;
import com.reuters.rfa.common.DispatchException;
import com.reuters.rfa.common.Dispatchable;
import com.reuters.rfa.common.EventQueue;
import com.reuters.rfa.config.ConfigDb;
import com.reuters.rfa.session.Session;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
* 链接创建服务器链接ssl rfa 框架的骨架
* */
public class RMDExampleStep3 {

  private static final Logger rootLogger = Logger.getLogger("");

  private static final Logger rfaLogger = Logger.getLogger("com.cgs.rfa");

  private Session session;

  private EventQueue eventQueue;

  /*
  *创建构造函数
  * */
  public RMDExampleStep3(String sessionName){
    ConfigDb configDb = new ConfigDb();
    configDb.addVariable("_Default.Sessions.RSSLSession.connectionList","RSSLConnection");
    configDb.addVariable("_Default.Connections.RSSLConnection.connectionType","RSSL");
    configDb.addVariable("_Default.Connections.RSSLConnection.serverList","167.76.54.77");
    Context.initialize(configDb);
    session = Session.acquire(sessionName);
    if (session == null){
      System.out.println("Could not create session " + sessionName);
    }else{
      eventQueue = EventQueue.create(null);
      System.out.println("Session: " + session.getName() + "created");
    }
  }

  public static void setLoggerVerbosity(Level level){
    rfaLogger.setLevel(level);
    for (Handler handler : rootLogger.getHandlers()){
      handler.setLevel(level);
    }
  }

  public void runDispatchLoop(){
    boolean active = true;
    while (active){
      try{
        eventQueue.dispatch(Dispatchable.INFINITE_WAIT);
      }catch (DispatchException e){
        System.out.println("Event Queue has been deactivated");
        active = false;
      }
    }
  }

  public void cleanup(){
    eventQueue.destroy();
    session.release();
    Context.uninitialize();
  }

  public static void main(String[] args) {
    if (args.length != 1){
      System.err.println("Usage: java RDMExample session_name");
    }
    final String sessionName = args[0];
    setLoggerVerbosity(Level.ALL);
    RMDExampleStep3 app = new RMDExampleStep3(sessionName);
    if (app.session != null){
      app.runDispatchLoop();
      app.cleanup();
    }
  }

}
