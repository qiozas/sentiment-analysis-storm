package com.sourcevirtues.sentiment.storm.pure.bolt;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
 * Simple Bolt that check words of incoming sentence and mark sentence with a negative score.
 * 
 * @author Adrianos Dadis
 * 
 */
public class NegativeBolt extends BaseBasicBolt {
   private static final long serialVersionUID = 1L;
   private static final Logger LOG = LoggerFactory.getLogger(NegativeBolt.class);

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
         ObjectNode node = (ObjectNode) mapper.readTree(tuple.getString(0));

         String[] words = node.path(Cons.MOD_TXT).asText().split(" ");
         int wordsSize = words.length;
         int negativeWordsSize = 0;
         for (String word : words) {
            if (NegativeWords.get().contains(word)) {
               negativeWordsSize++;
            }
         }

         node.put(Cons.NUM_NEGATIVE, (double) negativeWordsSize / wordsSize);

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

   private static class NegativeWords {
      private Set<String> negativeWords;
      private static NegativeWords _singleton;

      private NegativeWords() {
         negativeWords = new HashSet<>();

         //Add more "negative" words and load from file or database
         negativeWords.add("abort");
         negativeWords.add("betray");
         negativeWords.add("crash");
         negativeWords.add("thief");
         negativeWords.add("disappointment");
         negativeWords.add("disease");
         negativeWords.add("bad");
         negativeWords.add("sad");
      }

      static NegativeWords get() {
         if (_singleton == null) {
            synchronized (NegativeWords.class) {
               if (_singleton == null) {
                  _singleton = new NegativeWords();
               }
            }
         }

         return _singleton;
      }

      boolean contains(String key) {
         return get().negativeWords.contains(key);
      }
   }

}
