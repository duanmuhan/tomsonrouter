package com.cgs.rfa.lession4;

import com.reuters.rfa.common.Client;
import com.reuters.rfa.common.Event;
import com.reuters.rfa.omm.OMMAttribInfo;
import com.reuters.rfa.omm.OMMElementEntry;
import com.reuters.rfa.omm.OMMIterable;
import com.reuters.rfa.omm.OMMMsg;
import com.reuters.rfa.omm.OMMMsg.MsgType;
import com.reuters.rfa.omm.OMMState.Data;
import com.reuters.rfa.omm.OMMState.Stream;
import com.reuters.rfa.rdm.RDMMsgTypes;
import com.reuters.rfa.session.omm.OMMItemEvent;
import java.util.Iterator;

public class LoginClient implements Client {

  public  enum LoginState {PENDING, LOGGED_IN, DENIED}

  private LoginState currentState = LoginState.PENDING;

  public LoginState getCurrentState(){
    return  currentState;
  }

  public void processEvent(Event event) {
    OMMItemEvent itemEvent = (OMMItemEvent) event;
    OMMMsg respMsg = itemEvent.getMsg();
    assert respMsg.getMsgModelType() == RDMMsgTypes.LOGIN;
    if (respMsg.isFinal()) {
      processLoginFailure(respMsg);
    } else if (respMsg.getState().getStreamState() == Stream.OPEN
        && respMsg.getState().getDataState() == Data.OK
        && currentState != LoginState.LOGGED_IN) {
      processLoginSuccess(respMsg);
    } else {
      processLoginStatus(respMsg);
    }
  }

  private void processLoginSuccess(OMMMsg respMsg) {
    System.out.println("login success");
    if (respMsg.has(OMMMsg.HAS_STATE)) {
      System.out.println("State:" + respMsg.getState());
      System.out.println();
      showLoginResponseDetails(respMsg);
      currentState = LoginState.LOGGED_IN;
    }
  }

  private void processLoginFailure(OMMMsg respMsg) {
    if (respMsg.getMsgType() == MsgType.STATUS_RESP) {
      System.out.println("Received Login Status message.");
      if (respMsg.has(OMMMsg.HAS_STATE)) {
        System.out.println("State:" + respMsg.getState());
        System.out.println();
      }
    }
  }

  private static void processLoginStatus(OMMMsg respMsg) {
    if (respMsg.getMsgType() == MsgType.STATUS_RESP) {
      System.out.println("Received login status message");
      if (respMsg.has(OMMMsg.HAS_STATE)) {
        System.out.println("State:" + respMsg.getState());
        System.out.println();
      }
    }
  }

  private void showLoginResponseDetails((OMMMsg respMsg) {
    System.out.println("LOGIN RESPONSE FAILURE");
    System.out.println("INDICATION　MASK");
    System.out.println("  " + OMMMsg.Indication.indicationString(respMsg));
    if (respMsg.has(OMMMsg.HAS_ATTRIB_INFO)) {
      System.out.println("ATTRIBUTE_INFO");
      OMMAttribInfo ai = respMsg.getAttribInfo();
      if (ai.has(OMMAttribInfo.HAS_NAME)) {
        System.out.println("Name: " + ai.getName());
      }

      if (ai.has(OMMAttribInfo.HAS_ATTRIB)) {
        System.out.println("    Attrib: ELEMENT_LIST");
        OMMIterable attrib = (OMMIterable) ai.getAttrib();
        for (Iterator<?> iterator = attrib.iterator(); iterator.hasNext()) {
          OMMElementEntry entry = (OMMElementEntry) iterator.next();
          System.out.println("ELEMENT_ENTRY" + entry.getName());
          if (entry.getData().isBlank()) {
            System.out.println("BLANK Data");
          } else {
            System.out.println(entry.getData());
          }
        }
      }
    }
  }

}
