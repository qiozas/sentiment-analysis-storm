package com.sourcevirtues.sentiment.storm.pure.util;

import org.apache.storm.Constants;
import org.apache.storm.tuple.Tuple;

/**
 * Utility class.
 * 
 * @author Adrianos Dadis
 * 
 */
public class Utils {

   public static boolean isTickTuple(Tuple tuple) {
      return tuple != null && tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID)
            && tuple.getSourceStreamId().equals(Constants.SYSTEM_TICK_STREAM_ID);
   }
}
