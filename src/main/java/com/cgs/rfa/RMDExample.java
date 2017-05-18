package com.cgs.rfa;

import com.reuters.rfa.common.Context;
import com.reuters.rfa.config.ConfigDb;
import com.reuters.rfa.session.Session;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
* 链接创建服务器链接ssl rfa 框架的骨架
* */
public class RMDExample {

  private static final Logger rootLogger = Logger.getLogger("");

  private static final Logger rfaLogger = Logger.getLogger("com.cgs.rfa");

  private Session session;

  /*
  *创建构造函数
  * */
  public RMDExample(String sessionName){
    ConfigDb configDb = new ConfigDb();
    configDb.addVariable("_Default.Sessions.RSSLSession.connectionList","RSSLConnection");
    configDb.addVariable("_Default.Connections.RSSLConnection.connectionType","RSSL");
    configDb.addVariable("_Default.Connections.RSSLConnection.serverList","167.76.54.77");
    Context.initialize(configDb);
    session = Session.acquire(sessionName);
    if (session == null){
      System.out.println("Could not create session " + sessionName);
    }else{
      System.out.println("Session: " + session.getName() + "created");
    }
  }

  public static void setLoggerVerbosity(Level level){
    rfaLogger.setLevel(level);
    for (Handler handler : rootLogger.getHandlers()){
      handler.setLevel(level);
    }
  }

  public void cleanup(){
    session.release();
    Context.uninitialize();
  }

  public static void main(String[] args) {
    if (args.length != 1){
      System.err.println("Usage: java RDMExample session_name");
    }
    final String sessionName = args[0];
    setLoggerVerbosity(Level.ALL);
    RMDExample app = new RMDExample(sessionName);
    if (app.session != null){
      app.cleanup();
    }
  }

}
