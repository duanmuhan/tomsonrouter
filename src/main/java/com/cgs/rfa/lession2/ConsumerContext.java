package com.cgs.rfa.lession2;

import com.reuters.mainloop.Session;
import com.reuters.rfa.common.EventQueue;
import com.reuters.rfa.common.Handle;
import com.reuters.rfa.omm.OMMEncoder;
import com.reuters.rfa.omm.OMMPool;
import com.reuters.rfa.session.omm.OMMConsumer;

public class ConsumerContext {

  private Session session;
  private EventQueue eventQueue;
  private OMMConsumer ommConsumer;
  private OMMEncoder ommEncoder;
  private OMMPool ommPool;
  private Handle loginHandle;

  public Session getSession() {
    return session;
  }

  public void setSession(Session session) {
    this.session = session;
  }

  public EventQueue getEventQueue() {
    return eventQueue;
  }

  public void setEventQueue(EventQueue eventQueue) {
    this.eventQueue = eventQueue;
  }

  public OMMConsumer getOmmConsumer() {
    return ommConsumer;
  }

  public void setOmmConsumer(OMMConsumer ommConsumer) {
    this.ommConsumer = ommConsumer;
  }

  public OMMEncoder getOmmEncoder() {
    return ommEncoder;
  }

  public void setOmmEncoder(OMMEncoder ommEncoder) {
    this.ommEncoder = ommEncoder;
  }

  public OMMPool getOmmPool() {
    return ommPool;
  }

  public void setOmmPool(OMMPool ommPool) {
    this.ommPool = ommPool;
  }

  public Handle getLoginHandle() {
    return loginHandle;
  }

  public void setLoginHandle(Handle loginHandle) {
    this.loginHandle = loginHandle;
  }
}
