package com.cgs.rfa.lession2;

import com.reuters.rfa.common.Context;
import com.reuters.rfa.common.DispatchException;
import com.reuters.rfa.common.Dispatchable;
import com.reuters.rfa.common.EventQueue;
import com.reuters.rfa.common.EventSource;
import com.reuters.rfa.common.Handle;
import com.reuters.rfa.config.ConfigDb;
import com.reuters.rfa.omm.OMMElementList;
import com.reuters.rfa.omm.OMMEncoder;
import com.reuters.rfa.omm.OMMMsg;
import com.reuters.rfa.omm.OMMMsg.Indication;
import com.reuters.rfa.omm.OMMMsg.MsgType;
import com.reuters.rfa.omm.OMMPool;
import com.reuters.rfa.omm.OMMTypes;
import com.reuters.rfa.rdm.RDMMsgTypes;
import com.reuters.rfa.rdm.RDMUser.Attrib;
import com.reuters.rfa.rdm.RDMUser.NameType;
import com.reuters.rfa.rdm.RDMUser.Role;
import com.reuters.rfa.session.Session;
import com.reuters.rfa.session.omm.OMMConsumer;
import com.reuters.rfa.session.omm.OMMItemIntSpec;
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
  private OMMPool ommPool;
  private Handle loginHandle;


  /*
  *创建构造函数
  * */
  public RMDExampleStep3(String sessionName){
    ConfigDb configDb = new ConfigDb();
    configDb.addVariable("_Default.Sessions.RSSLSession.connectionList","RSSLConnection");
    configDb.addVariable("_Default.Connections.RSSLConnection.connectionType","RSSL");
    String port = "14002";
    configDb.addVariable("_Default.Connections.RSSLConnection.portNumber",port);
    String serverList = "159.220.108.145,159.220.108.207";
    configDb.addVariable("_Default.Connections.RSSLConnection.serverList",serverList);
    Context.initialize(configDb);
    session = Session.acquire(sessionName);
    if (session == null){
      System.out.println("Could not create session " + sessionName);
    }else{
      eventQueue = EventQueue.create("FlyingTigerEventQueue");
      ommPool = OMMPool.create();
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

  public void sendLoginRequest(String userName,String appId,String position){
    OMMEncoder ommEncoder = ommPool.acquireEncoder();
    OMMMsg msg = encodeLoginRequest(userName,appId,position,ommEncoder);
    OMMConsumer consumer = (OMMConsumer) session
        .createEventSource(EventSource.OMM_CONSUMER, "FlyingTigerEventSource", true);
    OMMItemIntSpec ommItemIntSpec = new OMMItemIntSpec();
    ommItemIntSpec.setMsg(msg);
    loginHandle = consumer.registerClient(eventQueue,ommItemIntSpec,new LoginClient(),true);
  }

  public OMMMsg encodeLoginRequest(String userName,String appId,String position,OMMEncoder ommEncoder){
    if (ommPool != null){
      OMMMsg ommMsg = ommPool.acquireMsg();
      ommMsg.setMsgType(MsgType.REQUEST);
      ommMsg.setMsgModelType(RDMMsgTypes.LOGIN);
      ommMsg.setIndicationFlags(Indication.REFRESH);
      ommMsg.setAttribInfo(null,userName, NameType.USER_NAME);

      ommEncoder.initialize(OMMTypes.MSG, 500);
      ommEncoder.encodeMsgInit(ommMsg,OMMTypes.ELEMENT_LIST,OMMTypes.NO_DATA);
      ommEncoder.encodeElementListInit(OMMElementList.HAS_STANDARD_DATA,(short) 0,(short) 0);
      ommEncoder.encodeElementEntryInit(Attrib.ApplicationId,OMMTypes.ASCII_STRING);
      ommEncoder.encodeString(appId, OMMTypes.ASCII_STRING);
      ommEncoder.encodeElementEntryInit(Attrib.Position,OMMTypes.ASCII_STRING);
      ommEncoder.encodeString(position,OMMTypes.ASCII_STRING);
      ommEncoder.encodeElementEntryInit(Attrib.Role,OMMTypes.UINT);
      ommEncoder.encodeUInt((long) Role.CONSUMER);
      ommEncoder.encodeAggregateComplete();

      OMMMsg encMsg = (OMMMsg) ommEncoder.acquireEncodedObject();
      ommPool.releaseMsg(encMsg);
      return encMsg;
    }
    return null;
  }

  public static void main(String[] args) {
    final String sessionName = "RSSLSession";
    setLoggerVerbosity(Level.ALL);
    RMDExampleStep3 app = new RMDExampleStep3(sessionName);
    if (app.session != null){
      app.sendLoginRequest("HK8_03_RHB_SWVPNTRIAL02","256","1.1.1.1/net");
      app.runDispatchLoop();
      app.cleanup();
    }
  }

}
