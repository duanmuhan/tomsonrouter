package com.cgs.rfa.lession5;

import com.cgs.rfa.lession4.LoginClient;
import com.cgs.rfa.lession4.LoginClient.LoginState;
import com.cgs.rfa.lession5.MarketPriceClient;
import com.reuters.rfa.common.Client;
import com.reuters.rfa.common.Context;
import com.reuters.rfa.common.DispatchException;
import com.reuters.rfa.common.Dispatchable;
import com.reuters.rfa.common.EventQueue;
import com.reuters.rfa.common.EventSource;
import com.reuters.rfa.common.Handle;
import com.reuters.rfa.config.ConfigDb;
import com.reuters.rfa.dictionary.FidDef;
import com.reuters.rfa.dictionary.FieldDictionary;
import com.reuters.rfa.omm.OMMData;
import com.reuters.rfa.omm.OMMElementList;
import com.reuters.rfa.omm.OMMEncoder;
import com.reuters.rfa.omm.OMMFieldEntry;
import com.reuters.rfa.omm.OMMFieldList;
import com.reuters.rfa.omm.OMMMsg;
import com.reuters.rfa.omm.OMMMsg.Indication;
import com.reuters.rfa.omm.OMMMsg.MsgType;
import com.reuters.rfa.omm.OMMPool;
import com.reuters.rfa.omm.OMMTypes;
import com.reuters.rfa.rdm.RDMInstrument;
import com.reuters.rfa.rdm.RDMMsgTypes;
import com.reuters.rfa.rdm.RDMUser.Attrib;
import com.reuters.rfa.rdm.RDMUser.NameType;
import com.reuters.rfa.session.Session;
import com.reuters.rfa.session.omm.OMMConsumer;
import com.reuters.rfa.session.omm.OMMItemIntSpec;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
* 链接创建服务器链接ssl rfa 框架的骨架
* */
public class RMDExampleStep6 {

  private static final Logger rootLogger = Logger.getLogger("");

  private static final Logger rfaLogger = Logger.getLogger("com.cgs.rfa");

  private Session session;

  private OMMPool ommPool;

  private OMMConsumer ommConsumer;

  private com.cgs.rfa.lession5.LoginClient loginClient;

  private Handle loginHandle;

  private Handle marketPriceHandle;

  private EventQueue eventQueue;

  private FieldDictionary fieldDictionary;

  /*
  *创建构造函数
  * */
  public RMDExampleStep6(String sessionName,String fieldDictFile,String enumFile){
    /*
    * 初始化汤森路透的链接配置
    * */
    ConfigDb configDb = new ConfigDb();
    configDb.addVariable("_Default.Sessions.RSSLSession.connectionList","RSSLConnection");
    configDb.addVariable("_Default.Connections.RSSLConnection.connectionType","RSSL");
    configDb.addVariable("_Default.Connections.RSSLConnection.serverList","167.76.54.77");
    Context.initialize(configDb);
    session = Session.acquire(sessionName);
    if (session == null){
      System.out.println("Could not create session " + sessionName);
    }else{
      ommPool = OMMPool.create();
      ommConsumer = (OMMConsumer) session.createEventSource(EventSource.OMM_CONSUMER,null);
      eventQueue = EventQueue.create(null);
      fieldDictionary = FieldDictionary.create();
      //这个是从本地读取的字典文件。
      FieldDictionary.readRDMFieldDictionary(fieldDictionary,fieldDictFile);
      System.out.println("Enum dictionary from file" + enumFile);

      FieldDictionary.readEnumTypeDef(fieldDictionary, enumFile);
      System.out.println("Enum dictionary read from file " + enumFile);

      System.out.println("Session: " + session.getName() + "created");
    }
  }

  public static void setLoggerVerbosity(Level level){
    rfaLogger.setLevel(level);
  }

  public void runDispatchLoop(){
    boolean active = true;
    while (active){
      try{
        eventQueue.dispatch(Dispatchable.INFINITE_WAIT);
        if(loginClient.getCurrentState() != LoginState.PENDING){
          eventQueue.deactivate();
        }
      }catch (DispatchException e){
        System.out.println("Event Queue has been deactivated");
        active = false;
      }
    }
  }

  public void sendLoginRequest(String userName,String appId,String position){
    OMMMsg loginReqMsg = encodeLoginReqMsg(userName,appId,position);
    OMMItemIntSpec loginIntSpec = new OMMItemIntSpec();
    loginIntSpec.setMsg(loginReqMsg);
    loginClient = new LoginClient();
    loginHandle = ommConsumer.registerClient(eventQueue,loginIntSpec,loginClient,null);
    ommPool.releaseMsg(loginReqMsg);
  }

  /*
  * 给信息进行编码
  * */
  private OMMMsg encodeLoginReqMsg(String user,String appId,String position){
    OMMMsg msg = ommPool.acquireMsg();
    msg.setMsgType(MsgType.REQUEST);
    msg.setMsgModelType(RDMMsgTypes.LOGIN);
    msg.setIndicationFlags(Indication.REFRESH);
    msg.setAttribInfo(null,user, NameType.USER_NAME);
    if (appId != null || position != null){
      OMMEncoder encoder = ommPool.acquireEncoder();
      encoder.initialize(OMMTypes.MSG,500);
      encoder.encodeMsgInit(msg,OMMTypes.ELEMENT_LIST,OMMTypes.NO_DATA);
      ommPool.releaseMsg(msg);
      //信息编码
      encoder.encodeElementListInit(OMMElementList.HAS_STANDARD_DATA, (short) 0, (short) 0);
      if (position != null){
        encoder.encodeElementEntryInit(Attrib.ApplicationId,OMMTypes.ASCII_STRING);
        encoder.encodeString(appId,OMMTypes.ASCII_STRING);
      }
      encoder.encodeAggregateComplete();
    }
    return msg;
  }

  /*
  * 发送市场信息请求的方法
  * */
  public void sendMarketPriceRequest(String serviceName,String itemName){
    OMMMsg msg = ommPool.acquireMsg();
    msg.setMsgType(MsgType.REQUEST);
    msg.setMsgModelType(RDMMsgTypes.MARKET_PRICE);
    msg.setIndicationFlags(Indication.REFRESH);
    msg.setAttribInfo(serviceName,itemName, RDMInstrument.NameType.RIC);

    OMMItemIntSpec intSpec = new OMMItemIntSpec();
    intSpec.setMsg(msg);
    Client client = new MarketPriceClient(fieldDictionary);
    marketPriceHandle = ommConsumer.registerClient(eventQueue,intSpec,client,null);
    ommPool.releaseMsg(msg);
  }

  public void cleanup(){
    ommConsumer.unregisterClient(loginHandle);
    ommConsumer.unregisterClient(marketPriceHandle);
    eventQueue.destroy();
    ommConsumer.destroy();
    session.release();
    Context.uninitialize();
  }

  /*
  * 增加字典的功能用来解析消息数据中的字段
  * */
  private void parseMarketPriceData(OMMFieldList fl){
    for (Iterator<?> iterator = fl.iterator(); iterator.hasNext();){
      OMMFieldEntry fe = (OMMFieldEntry) iterator.next();
      short id = fe.getFieldId();
      FidDef fiddef = fieldDictionary.getFidDef(id);
      if (fidef != null){
        OMMData data = fe.getData(fiddef.getOMMType());
        short dataType = data.getType();
        System.out.printf("    FieldEntry, FID %4d, name %-11s type %-13s ",id,fiddef.getName()+",",OMMTypes.toString(dataType) + ":");
      }
    }
  }

  public static void main(String[] args) {
    if (args.length != 1){
      System.err.println("Usage: java RDMExample session_name");
      System.exit(1);
    }

    final String sessionName = args[0];
    final String userName = args[1];
    final String appId = "256";
    final String position = "1.1.1.1/net";
    final String servicename = args[2];
    final String itemname = args[3];
    RMDExampleStep6 app = new RMDExampleStep6(sessionName);
    setLoggerVerbosity(Level.ALL);
    if (app.session != null){
      app.sendLoginRequest(userName,appId,position);
      app.sendMarketPriceRequest(servicename,itemname);
      app.runDispatchLoop();
      app.cleanup();
    }
  }

}
