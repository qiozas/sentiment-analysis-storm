package com.sourcevirtues.sentiment.storm.pure;

import com.sourcevirtues.sentiment.storm.pure.bolt.HBaseBatchBolt;
import com.sourcevirtues.sentiment.storm.pure.bolt.LoggingBolt;
import com.sourcevirtues.sentiment.storm.pure.bolt.NegativeBolt;
import com.sourcevirtues.sentiment.storm.pure.bolt.PositiveBolt;
import com.sourcevirtues.sentiment.storm.pure.bolt.ScoreBolt;
import com.sourcevirtues.sentiment.storm.pure.bolt.StemmingBolt;
import com.sourcevirtues.sentiment.storm.pure.spout.RandomSentenceSpout;
import com.sourcevirtues.sentiment.storm.pure.util.Cons;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;

/**
 * Simple topology class that define the process flow.
 * 
 * @author Adrianos Dadis
 * 
 */
public class SentimentAnalysisTopology {

   public static void main(String[] args) throws Exception {

      TopologyBuilder builder = new TopologyBuilder();

      //Use KafkaSpout or use dummy RandomSentenceSpout
      builder.setSpout("spout", new RandomSentenceSpout(), 1);

      builder.setBolt("stemming", new StemmingBolt(), 1).localOrShuffleGrouping("spout");
      builder.setBolt("positive", new PositiveBolt(), 1).localOrShuffleGrouping("stemming");
      builder.setBolt("negative", new NegativeBolt(), 1).localOrShuffleGrouping("positive");
      builder.setBolt("score", new ScoreBolt(), 1).localOrShuffleGrouping("negative");
      builder.setBolt("logging", new LoggingBolt().withFields(Cons.TUPLE_VAR_MSG, Cons.TUPLE_VAR_SCORE), 1)
            .localOrShuffleGrouping("score");

      if (false) {
         //  HBase shell -> create Table
         // $> create 'SentimentAnalysisStorm', 'cf'
         builder.setBolt("hbase", new HBaseBatchBolt().withTable("SentimentAnalysisStorm", "cf", "json"), 1)
               .localOrShuffleGrouping("score");
      }

      Config conf = new Config();
      conf.setDebug(true);

      if (args != null && args.length > 0) {
         conf.setNumWorkers(1);

         StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
      } else {
         //conf.setMaxTaskParallelism(3);

         LocalCluster cluster = new LocalCluster();
         cluster.submitTopology("sentiment_analysis", conf, builder.createTopology());

         Thread.sleep(10000);

         cluster.shutdown();
      }
   }
}
