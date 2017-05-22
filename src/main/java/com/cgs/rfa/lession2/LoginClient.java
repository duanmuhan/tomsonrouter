package com.cgs.rfa.lession2;

import com.reuters.rfa.common.Client;
import com.reuters.rfa.common.Event;
import com.reuters.rfa.omm.OMMMsg;
import com.reuters.rfa.session.omm.OMMConnectionEvent;
import com.reuters.rfa.session.omm.OMMItemEvent;

public class LoginClient implements Client {

  public void processEvent(Event event) {
    if(event.getType() == Event.COMPLETION_EVENT){
      System.out.println("Receive a Completion_event" + event.getHandle());
      return;
    }

    if (event.getType() == Event.OMM_CONNECTION_EVENT){
      OMMConnectionEvent connectionEvent = (OMMConnectionEvent)event;
      System.out.println("LoginClient: Receive an OMM_CONNECTION_EVENT");
      System.out.println("Name: " + connectionEvent.getConnectionName());
      System.out.println("Status: " + connectionEvent.getConnectionStatus().toString());
      System.out.println("Host: " + connectionEvent.getConnectedHostName());
      System.out.println("Port: " + connectionEvent.getConnectedPort());
      System.out.println("ComponentVersion: " + connectionEvent.getConnectedComponentVersion());
      return;
    }

    if (event.getType() != Event.OMM_ITEM_EVENT){
      System.out.println("ERROR, Received an unsupported Event type.");
      return;
    }

    OMMItemEvent itemEvent = (OMMItemEvent)event;
    OMMMsg respMsg = itemEvent.getMsg();
  }
}
