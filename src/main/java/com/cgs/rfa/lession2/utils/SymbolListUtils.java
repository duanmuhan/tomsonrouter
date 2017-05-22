package com.cgs.rfa.lession2.utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SymbolListUtils {
  private static List<String> symbolList = new CopyOnWriteArrayList<String>();
  public static List<String> getSymbolList(){
    return symbolList;
  }

  public static void setSymbolList(List<String> symbolList){
    SymbolListUtils.symbolList.addAll(symbolList);
  }
}
