package com.sourcevirtues.sentiment.storm.pure.bolt;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcevirtues.sentiment.storm.pure.util.Cons;

/**
 * Simple Bolt that check incoming positive and negative scores and decides if this sentence is
 * positive or negative.
 * 
 * @author Adrianos Dadis
 * 
 */
public class ScoreBolt extends BaseBasicBolt {
   private static final long serialVersionUID = 1L;
   private static final Logger LOG = LoggerFactory.getLogger(ScoreBolt.class);

   private transient ObjectMapper mapper;

   @SuppressWarnings("rawtypes")
   @Override
   public void prepare(Map stormConf, TopologyContext context) {
      mapper = new ObjectMapper();
      //mapper.setSerializationInclusion(Include.NON_NULL);
   }

   @Override
   public void execute(Tuple tuple, BasicOutputCollector collector) {

      try {
         ObjectNode node = (ObjectNode) mapper.readTree(tuple.getString(0));

         boolean score = false;
         if (node.get(Cons.NUM_NEGATIVE).asDouble(Double.NEGATIVE_INFINITY) > node.get(Cons.NUM_POSITIVE).asDouble(
               Double.POSITIVE_INFINITY)) {

            node.put(Cons.SCORE, Cons.SENTIMENT_NEG);
            score = false;
         } else {
            node.put(Cons.SCORE, Cons.SENTIMENT_POS);
            score = true;

         }

         collector.emit(new Values(node.path(Cons.ID).asText(), node.toString(), score));

      } catch (Exception e) {
         LOG.error("Cannot process input. Ignore it", e);
      }

   }

   @Override
   public void declareOutputFields(OutputFieldsDeclarer declarer) {
      declarer.declare(new Fields(Cons.TUPLE_VAR_KEY, Cons.TUPLE_VAR_MSG, Cons.TUPLE_VAR_SCORE));
   }

   @Override
   public Map<String, Object> getComponentConfiguration() {
      return null;
   }

}
