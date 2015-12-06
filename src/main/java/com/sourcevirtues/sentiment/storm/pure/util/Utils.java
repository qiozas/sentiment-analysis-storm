package com.sourcevirtues.sentiment.storm.pure.util;

import backtype.storm.Constants;
import backtype.storm.tuple.Tuple;

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
