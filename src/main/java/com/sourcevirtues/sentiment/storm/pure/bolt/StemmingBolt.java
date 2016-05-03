package com.sourcevirtues.sentiment.storm.pure.bolt;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcevirtues.sentiment.storm.pure.util.Cons;

/**
 * Simple Bolt that checks incoming sentence and remove any words that are useless for scoring by
 * next processing Bolts.
 * 
 * @author Adrianos Dadis
 * 
 */
public class StemmingBolt extends BaseBasicBolt {
   private static final long serialVersionUID = 1L;
   private static final Logger LOG = LoggerFactory.getLogger(StemmingBolt.class);

   private transient ObjectMapper mapper;

   @SuppressWarnings("rawtypes")
   @Override
   public void prepare(Map stormConf, TopologyContext context) {
      mapper = new ObjectMapper();
      mapper.setSerializationInclusion(Include.NON_NULL);
   }

   @Override
   public void execute(Tuple tuple, BasicOutputCollector collector) {
      try {
         String text = tuple.getString(0);
         String[] words = text.split("\\b");
         StringBuilder modified = new StringBuilder(text.length() / 2);
         for (String w : words) {
            if (!UselessWords.get().contains(w)) {
               modified.append(w);
            }
         }

         ObjectNode node = mapper.createObjectNode();

         //TODO Generate a business ID or get it as input
         node.put(Cons.ID, UUID.randomUUID().toString());

         node.put(Cons.TEXT, text);
         node.put(Cons.MOD_TXT, modified.toString());

         collector.emit(new Values(node.toString()));
      } catch (Exception e) {
         LOG.error("Cannot process input. Ignore it", e);
      }
   }

   @Override
   public void declareOutputFields(OutputFieldsDeclarer declarer) {
      declarer.declare(new Fields(Cons.TUPLE_VAR_MSG));
   }

   @Override
   public Map<String, Object> getComponentConfiguration() {
      return null;
   }

   private static class UselessWords {
      private Set<String> uselessWords;
      private static UselessWords _singleton;

      private UselessWords() {
         uselessWords = new HashSet<>();

         //Add more "useless" words and load from file or database
         uselessWords.add("add");
         uselessWords.add("about");
         uselessWords.add("be");
         uselessWords.add("before");
      }

      static UselessWords get() {
         if (_singleton == null) {
            synchronized (UselessWords.class) {
               if (_singleton == null) {
                  _singleton = new UselessWords();
               }
            }
         }

         return _singleton;
      }

      boolean contains(String key) {
         return get().uselessWords.contains(key);
      }
   }

}
