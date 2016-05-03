package com.sourcevirtues.sentiment.storm.pure.spout;

import java.util.Map;
import java.util.Random;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import com.sourcevirtues.sentiment.storm.pure.util.Cons;

/**
 * Simple Spout that emit (for ever) a few hardcoded sentences.
 * It could be changed to load sentences from a file (or DB).
 * 
 * @author Adrianos Dadis
 * 
 */
public class RandomSentenceSpout extends BaseRichSpout {
   private static final long serialVersionUID = 1L;

   private SpoutOutputCollector _collector;
   private Random _rand;
   private String[] sentences;

   @SuppressWarnings("rawtypes")
   @Override
   public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
      _collector = collector;
      _rand = new Random();

      //Read from real firehose (Kafka or Twitter) or write more sentences
      sentences = new String[] {
            "abort and abort and calm",
            "admire FSF admire GNU crash DRM",
            "nothing relevant",
            "calm when others cannot",
            "bonus sometimes works",
            "abort and crash"};
   }

   @Override
   public void nextTuple() {
      Utils.sleep(100);

      String sentence = sentences[_rand.nextInt(sentences.length)];

      _collector.emit(new Values(sentence));
   }

   @Override
   public void ack(Object id) {}

   @Override
   public void fail(Object id) {}

   @Override
   public void declareOutputFields(OutputFieldsDeclarer declarer) {
      declarer.declare(new Fields(Cons.TUPLE_VAR_MSG));
   }
}