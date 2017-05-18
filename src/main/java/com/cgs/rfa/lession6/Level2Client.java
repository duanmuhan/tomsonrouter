package com.cgs.rfa.lession6;// RFA Java RDM Example Consumer tutorial
// Step 7: Consuming Level 2 data.
// This file defines the Level 2 client object that we register with our OMMConsumer.
// It implements the processEvent callback which expects an item event with a
// MarketByOrder, MarketByPrice, MarketMaker, or SymbolList RDM response message.
// See ReadMe.html for details.

/***************************************************************/
/* RFA Demo Application                                        */
/* Copyright (C) 2010 Thomson Reuters. All rights reserved.    */
/* Duplication and/or redistribution prohibited                */
/*                                                             */
/* Disclaimer: This sample code has been provided for teaching */
/* and training purposes only. It is not intended for use      */
/* within production applications.                             */
/***************************************************************/

import com.reuters.rfa.common.Client;
import com.reuters.rfa.common.Event;
import com.reuters.rfa.dictionary.FidDef;
import com.reuters.rfa.dictionary.FieldDictionary;
import com.reuters.rfa.omm.OMMAttribInfo;
import com.reuters.rfa.omm.OMMData;
import com.reuters.rfa.omm.OMMEnum;
import com.reuters.rfa.omm.OMMFieldEntry;
import com.reuters.rfa.omm.OMMFieldList;
import com.reuters.rfa.omm.OMMMap;
import com.reuters.rfa.omm.OMMMapEntry;
import com.reuters.rfa.omm.OMMMapEntry.Action;
import com.reuters.rfa.omm.OMMMsg;
import com.reuters.rfa.omm.OMMTypes;
import com.reuters.rfa.omm.OMMState;
import com.reuters.rfa.rdm.RDMMsgTypes;
import com.reuters.rfa.session.omm.OMMItemEvent;
import java.util.Iterator;

public class Level2Client implements Client {

  private FieldDictionary dictionary;

  // Constructs a client to use the given field dictionary for parsing Level 2 RDM data.

  public Level2Client(FieldDictionary dict) {
    dictionary = dict;
  }

  @Override
  public void processEvent(Event event) {
    // We know our event is an item event, because we registered this client
    // to receive only item events. So we can safely cast to OMMItemEvent. If
    // something goes wrong, the Java runtime will give us a bad cast exception.

    OMMItemEvent ie = (OMMItemEvent) event;
    OMMMsg respMsg = ie.getMsg();

    System.out.println("Received "
        + RDMMsgTypes.toString(respMsg.getMsgModelType()) + " message");

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

    // Payload data
    if (respMsg.getDataType() == OMMTypes.NO_DATA) {
      System.out.println("  PAYLOAD DATA:");
      System.out.println("    None");
    }
    else {
      System.out.printf("  PAYLOAD DATA (%d BYTES):%n",
          respMsg.getPayload().getEncodedLength());
      // The payload for Level 2 data must be a Map
      if (respMsg.getDataType() == OMMTypes.MAP)
        parseMap((OMMMap) respMsg.getPayload(), 2);
      else
        System.out.println("    ERROR: payload data type is not Map.");
    }
  }

  // indent the output the specified number of tabs
  private static void indent(int indentLevel) {
    for (int t = 0; t < indentLevel; t++)
      System.out.print("  ");
  }

  // Parses an OMMMap and prints the contents, indented at the required level

  private void parseMap(OMMMap map, int indentLevel) {
    indent(indentLevel);
    System.out.printf("Map, Count=%d", map.getCount());

    // An optional count of the Map Entries in all parts of the Refresh, may
    // be used to optimize caching
    if (map.has(OMMMap.HAS_TOTAL_COUNT_HINT))
      System.out.printf(", TotalCountHint: %d", map.getTotalCountHint());
    System.out.println();

    // Summary data is a FieldList containing data applicable to all MapEntries
    if (map.has(OMMMap.HAS_SUMMARY_DATA)) {
      indent(indentLevel);
      System.out.println("Summary Data:");
      parseFieldList((OMMFieldList) map.getSummaryData(), indentLevel + 1);
    }

    // Iterate through each MapEntry in the Map
    for (Iterator<?> iter = map.iterator(); iter.hasNext(); ) {
      OMMMapEntry mapEntry = (OMMMapEntry) iter.next();

      System.out.println();
      indent(indentLevel);
      System.out.printf("MapEntry Action: %s%n",
          OMMMapEntry.Action.toString(mapEntry.getAction()));
      indent(indentLevel);
      System.out.printf("MapEntry Key: %s%n", mapEntry.getKey());

      // Each MapEntry contains a FieldList or nothing
      if (mapEntry.getDataType() == OMMTypes.FIELD_LIST
          && mapEntry.getAction() != Action.DELETE) {
        // MapEntry with Action of Delete has no corresponding data
        OMMFieldList fieldList = (OMMFieldList) mapEntry.getData();
        indent(indentLevel);
        System.out.println("MapEntry Data:");
        parseFieldList(fieldList, indentLevel + 1);
      }
    }
    System.out.println();
  }

  // Prints the field names and values of the given field list using the dictionary
  // that this object has been initialized with.

  private void parseFieldList(OMMFieldList fl, int indentLevel) {
    indent(indentLevel);
    System.out.printf("FieldList, StandardDataCount=%d%n", fl.getStandardCount());
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

        // Print the Field ID, name and type.
        indent(indentLevel + 1);
        System.out.printf("FieldEntry, FID %4d, name %-11s type %-13s ",
            fid, fiddef.getName() + ",", OMMTypes.toString(dataType) + ":");

        // Check if the field contains a nested OMMMap, which is allowed by the
        // MarketByPrice RDM.
        // The nested OMMMap will contain a list of Market Maker IDs.

        if (data.getType() == OMMTypes.MAP) {
          System.out.println();
          parseMap((OMMMap) data, indentLevel + 2);
        }
        else {
          // Print the field value.
          switch (dataType) {
            case OMMTypes.ENUM:
              // For enumerated types, print both raw and expanded values.
              int enumvalue = ((OMMEnum) data).getValue();
              System.out.printf("%d -> \"%s\"",
                  enumvalue,
                  dictionary.expandedValueFor(fid, enumvalue));
              break;

            default:
              // For any other data type, just use its toString() method
              // for simplicity. (If we want, we can convert it to a type
              // we can use, such as a long or a double; for an example
              // of how to do this, see MarketPriceClient.)
              System.out.print(data);
              break;
          }
        }
        System.out.println();
      } else {
        indent(indentLevel + 1);
        System.out.println("Received FID " + fid + " not defined in dictionary");
      }
    }
  }
}
