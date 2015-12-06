package com.sourcevirtues.sentiment.storm.pure.bolt;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sourcevirtues.sentiment.storm.pure.util.Cons;

/**
 * Simple Bolt that check words of incoming sentence and mark sentence with a positive score.
 * 
 * @author Adrianos Dadis
 * 
 */
public class PositiveBolt extends BaseBasicBolt {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(PositiveBolt.class);

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
            int positiveWordsSize = 0;
            for (String word : words) {
                if (PositiveWords.get().contains(word)) {
                    positiveWordsSize++;
                }
            }

            node.put(Cons.NUM_POSITIVE, (double) positiveWordsSize / wordsSize);

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

    private static class PositiveWords {
        private Set<String> positiveWords;
        private static PositiveWords _singleton;

        private PositiveWords() {
            positiveWords = new HashSet<>();

            //Add more "positive" words and load from file or database
            positiveWords.add("admire");
            positiveWords.add("bonus");
            positiveWords.add("calm");
        }

        static PositiveWords get() {
            if (_singleton == null) {
                synchronized (PositiveWords.class) {
                    if (_singleton == null) {
                        _singleton = new PositiveWords();
                    }
                }
            }

            return _singleton;
        }

        boolean contains(String key) {
            return get().positiveWords.contains(key);
        }
    }

}
