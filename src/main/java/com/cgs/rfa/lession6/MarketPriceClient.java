package com.cgs.rfa.lession6;

import com.reuters.rfa.common.Client;
import com.reuters.rfa.common.Event;
import com.reuters.rfa.dictionary.FidDef;
import com.reuters.rfa.dictionary.FieldDictionary;
import com.reuters.rfa.omm.OMMAttribInfo;
import com.reuters.rfa.omm.OMMData;
import com.reuters.rfa.omm.OMMDateTime;
import com.reuters.rfa.omm.OMMEnum;
import com.reuters.rfa.omm.OMMFieldEntry;
import com.reuters.rfa.omm.OMMFieldList;
import com.reuters.rfa.omm.OMMMsg;
import com.reuters.rfa.omm.OMMNumeric;
import com.reuters.rfa.omm.OMMState;
import com.reuters.rfa.omm.OMMTypes;
import com.reuters.rfa.rdm.RDMMsgTypes;
import com.reuters.rfa.session.omm.OMMItemEvent;
import java.util.Iterator;

public class MarketPriceClient implements Client {

  private FieldDictionary dictionary;

  // Constructs a client to use the given field dictionary for parsing MarketPrice data.

  public MarketPriceClient(FieldDictionary dict) {
    dictionary = dict;
  }

  public void processEvent(Event event) {

    // We know our event is an item event, because we registered this client
    // to receive only item events. So we can safely cast to OMMItemEvent. If
    // something goes wrong, the Java runtime will give us a bad cast exception.
    // We also know that this event's response message is a MarketPrice RDM type.

    OMMItemEvent ie = (OMMItemEvent) event;
    OMMMsg respMsg = ie.getMsg();
    assert respMsg.getMsgModelType() == RDMMsgTypes.MARKET_PRICE;
    System.out.println("Received MarketPrice message");

    // Print message type, attributes, indication flags, manifest, QoS, state

    System.out.println("  Message type: "
        + OMMMsg.MsgType.toString(respMsg.getMsgType()));

    // AttribInfo: this is where service name and item name are contained

    if (respMsg.has(OMMMsg.HAS_ATTRIB_INFO)) {
      System.out.println("  ATTRIB INFO:");
      OMMAttribInfo ai = respMsg.getAttribInfo();
      if (ai.has(OMMAttribInfo.HAS_SERVICE_NAME))
        System.out.println("    ServiceName: " + ai.getServiceName());
      if (ai.has(OMMAttribInfo.HAS_NAME))
        System.out.println("    Name: " + ai.getName());
    }

    // Indication flags provide hints on how to handle the event and the data.
    // DO_NOT_CACHE means it wouldn't make sense to cache this data item.
    // CLEAR_CACHE indicates the cache for this stream should be cleared.
    // REFRESH_COMPLETE indicates that all messages in a multi-part refresh have now
    // been received.
    // For all other flags please see the Reference Manual. The following is a check
    // for all possible flags that are currently defined in the API.

    if (respMsg.isSet(
        OMMMsg.Indication.ATTRIB_INFO_IN_UPDATES |
            OMMMsg.Indication.CONFLATION_INFO_IN_UPDATES |
            OMMMsg.Indication.REFRESH_COMPLETE |
            OMMMsg.Indication.CLEAR_CACHE |
            OMMMsg.Indication.DO_NOT_CACHE |
            OMMMsg.Indication.DO_NOT_CONFLATE |
            OMMMsg.Indication.DO_NOT_RIPPLE |
            OMMMsg.Indication.PRIVATE_STREAM |
            OMMMsg.Indication.PAUSE_REQ |
            OMMMsg.Indication.NONSTREAMING |
            OMMMsg.Indication.REFRESH |
            OMMMsg.Indication.VIEW |
            OMMMsg.Indication.NEED_ACK |
            OMMMsg.Indication.BATCH_REQ |
            OMMMsg.Indication.POST_INIT))
    {
      // Here we just print all the flags using RFA's convenience method.
      // In a production application you should check each flag individually
      // and take different appropriate actions for each one.
      System.out.println("  INDICATION MASK:");
      System.out.println("    " + OMMMsg.Indication.indicationString(respMsg));
    }
    else {
      // In case there are any other bits not tested above
      if (respMsg.isSet(0xFFFFFFFF))
        System.out.println("  UNRECOGNISED INDICATION FLAGS:");
      System.out.println("    " + OMMMsg.Indication.indicationString(respMsg));
    }

    // Message manifest incl. sequence number, item group, permissions

    if (respMsg.has(OMMMsg.HAS_SEQ_NUM)) {
      System.out.println("  SEQUENCE NUMBER:");
      System.out.println("    " + respMsg.getSeqNum());
    }

    if (respMsg.has(OMMMsg.HAS_CONFLATION_INFO)) {
      // Time in milliseconds over which the updates were conflated, and how many
      System.out.println("  CONFLATION INFO:");
      System.out.println("    Conflation time: " + respMsg.getConflationTime());
      System.out.println("    Conflation count: " + respMsg.getConflationCount());
    }

    if (respMsg.has(OMMMsg.HAS_ITEM_GROUP)) {
      System.out.println("  ITEM GROUP:");
      System.out.println("    " + respMsg.getItemGroup());
    }

    if (respMsg.has(OMMMsg.HAS_PERMISSION_DATA)) {
      System.out.println("  PERMISSION DATA:");
      System.out.print("    ");
      for (byte b : respMsg.getPermissionData())
        System.out.printf("%02x", b);
      System.out.println();
    }

    // Response type: whether solicited or unsolicited refresh
    if (respMsg.has(OMMMsg.HAS_RESP_TYPE_NUM)) {
      System.out.println("  RESPONSE TYPE:");
      System.out.println("    " + OMMMsg.RespType.toString(
          respMsg.getRespTypeNum()));
    }

    // Quality of Service: whether real-time or delayed, tick-by-tick or conflated etc
    if (respMsg.has(OMMMsg.HAS_QOS)) {
      System.out.println("  QOS:");
      System.out.println("    " + respMsg.getQos());
    }

    // State: whether the stream is open, the data is ok or suspect (stale), etc
    if (respMsg.has(OMMMsg.HAS_STATE)) {
      OMMState state = respMsg.getState();
      System.out.println("  STATE:");
      System.out.println("    Stream state: "
          + OMMState.Stream.toString(state.getStreamState()));
      System.out.println("    Data state: "
          + OMMState.Data.toString(state.getDataState()));
      System.out.println("    Code: "
          + OMMState.Code.toString(state.getCode()));
      System.out.println("    Text: "
          + state.getText());
    }

    // Payload - just print size for now. Will decode and print contents in next step.
    if (respMsg.getDataType() == OMMTypes.NO_DATA) {
      System.out.println("  PAYLOAD DATA:");
      System.out.println("    None");
    }
    else {
      System.out.printf("  PAYLOAD DATA (%d BYTES):%n",
          respMsg.getPayload().getEncodedLength());
    }

    System.out.println();
  }

  // Prints the field names and values of the given field list using the dictionary
  // that this object has been initialized with.

  private void parseMarketPriceData(OMMFieldList fl) {
    System.out.printf("  FieldList, StandardDataCount=%d%n", fl.getStandardCount());
    for (Iterator<?> iter = fl.iterator(); iter.hasNext(); ) {
      // We are iterating through the Field List, which contains Field Entries. Each
      // field entry has an accessor method for the FID and the data. To get the
      // data we need to specify the data type, which we get by looking up the FID
      // in the dictionary.

      OMMFieldEntry fe = (OMMFieldEntry) iter.next();
      short fid = fe.getFieldId();
      FidDef fiddef = dictionary.getFidDef(fid);
      if (fiddef != null) {
        OMMData data = fe.getData(fiddef.getOMMType());
        short dataType = data.getType();

        // Print the Field ID, name and type, before we print the value.
        System.out.printf("    FieldEntry, FID %4d, name %-11s type %-13s ",
            fid, fiddef.getName() + ",", OMMTypes.toString(dataType) + ":");

        // Now print the value according to its type. You can find the exact type
        // of a field manually by looking it up in the RDMFieldDictionary file.
        // The value is typically an OMMDataBuffer, OMMNumeric or OMMDateTime
        // subtype, each with its own conversions to native data types. To find
        // the appropriate subtype for a given data type, see the Reference
        // Manual. For general display purposes, any data type can also be printed
        // using the OMMData.toString() method.

        if (data.isBlank())
          System.out.printf("<blank>");
        else
          switch (dataType) {
            case OMMTypes.INT:
            case OMMTypes.UINT:
              System.out.print(((OMMNumeric) data).toLong());
              break;

            case OMMTypes.REAL:
              System.out.print(((OMMNumeric) data).toDouble());
              break;

            case OMMTypes.DATE:
              System.out.printf("%1$te-%1$tb-%1$tY",  // dd-Mmm-yyyy
                  ((OMMDateTime) data).toCalendar());
              break;

            case OMMTypes.TIME:
              System.out.printf("%tT", ((OMMDateTime) data).toCalendar());
              break;

            case OMMTypes.ENUM:
              // For enumerated types, print both raw and expanded values.
              int enumvalue = ((OMMEnum) data).getValue();
              System.out.printf("%d -> \"%s\"",
                  enumvalue,
                  dictionary.expandedValueFor(fid, enumvalue));
              break;

            case OMMTypes.ASCII_STRING:
            case OMMTypes.RMTES_STRING:
            default:
              System.out.printf("\"%s\"", data.toString());
              break;
          }

        System.out.println();
      }
      else
        System.out.println("    Received FID " + fid + " not defined in dictionary");
    }
}
