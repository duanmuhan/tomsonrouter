package com.cgs.rfa.lession2;

import com.cgs.rfa.lession2.utils.SymbolListUtils;
import com.reuters.rfa.common.Client;
import com.reuters.rfa.common.Event;
import com.reuters.rfa.common.Handle;
import com.reuters.rfa.omm.OMMMsg;
import com.reuters.rfa.omm.OMMMsg.Indication;
import com.reuters.rfa.omm.OMMMsg.MsgType;
import com.reuters.rfa.omm.OMMTypes;
import com.reuters.rfa.rdm.RDMInstrument.NameType;
import com.reuters.rfa.rdm.RDMMsgTypes;
import com.reuters.rfa.session.omm.OMMItemEvent;
import com.reuters.rfa.session.omm.OMMItemIntSpec;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

public class MarketPriceClient implements Client {

  private static final String SERVICE_NAME = "hEDD";
  private final SimpleDateFormat tradeDateFormat = new SimpleDateFormat("yyyyMMdd");
  private final SimpleDateFormat tradeTimeFormat = new SimpleDateFormat("HHmmss.SSS");

  private LinkedList<Handle> itemHandles;
  private ConsumerContext consumerContext;

  public MarketPriceClient(ConsumerContext consumerContext){
    this.consumerContext = consumerContext;
    this.itemHandles = new LinkedList<Handle>();
  }

  private String[] getItems(){
    List<String> symbolList = SymbolListUtils.getSymbolList();
    String[] list = new String[];
    if (symbolList != null){
      list = symbolList.toArray(new String[symbolList.size()]);
    }
    return list;
  }

  public void sendRequest(){
    OMMMsg msg = this.consumerContext.getOmmPool().acquireMsg();
    msg.setMsgType(MsgType.REQUEST);
    msg.setMsgModelType(RDMMsgTypes.MARKET_PRICE);
    msg.setPriority((byte)1,1);
    msg.setIndicationFlags(Indication.REFRESH | Indication.ATTRIB_INFO_IN_UPDATES);
    if (this.consumerContext.getLoginHandle() != null){
      msg.setAssociatedMetaInfo(this.consumerContext.getLoginHandle());
    }
    OMMItemIntSpec intSpec = new OMMItemIntSpec();
    for (String item : this.getItems()){
      msg.setAttribInfo(SERVICE_NAME,item, NameType.RIC);
      intSpec.setMsg(msg);
      Handle itemHandle = this.consumerContext.getOmmConsumer()
          .registerClient(this.consumerContext.getEventQueue(),intSpec,this,null);
      itemHandles.add(itemHandle);
    }
    this.consumerContext.getOmmPool().releaseMsg(msg);
  }

  public void processEvent(Event event) {
    if (event == null){
      return;
    }
    if (event.getType() == Event.COMPLETION_EVENT){
      return;
    }
    if (event.getType() != Event.OMM_ITEM_EVENT){
      return;
    }
    OMMItemEvent itemEvent = (OMMItemEvent)event;
    GenericOMMParser.parse(itemEvent.getMsg());

  }

  private void parseMessage(OMMItemEvent itemEvent){
    OMMMsg respMsg = itemEvent.getMsg();
    if (respMsg.getDataType() == OMMTypes.NO_DATA){
      return;
    }
    if (respMsg.getPayload() == null || respMsg.getPayload().isBlank()){
      return;
    }
    if (respMsg.getDataType() != OMMTypes.FIELD_LIST){
      return;
    }

    String symbol = respMsg.getAttribInfo().getName();
  }
}
