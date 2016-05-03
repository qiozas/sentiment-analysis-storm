package com.sourcevirtues.sentiment.storm.pure.bolt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.storm.Config;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

import com.sourcevirtues.sentiment.storm.pure.util.HBaseSimpleClient;
import com.sourcevirtues.sentiment.storm.pure.util.Utils;

/**
 * Simple HBase Bolt that writes to HBase.
 * 
 * @author Adrianos Dadis
 *
 */
public class HBaseBatchBolt extends BaseRichBolt {
   private static final long serialVersionUID = 1L;
   private static final Logger LOG = LoggerFactory.getLogger(HBaseBatchBolt.class);

   private transient HBaseSimpleClient hbaseClient;
   private transient List<Row> rows;
   private transient List<Tuple> tupleBatch;

   private OutputCollector collector;

   private int tickTupleSec = 1;
   private String tableName;
   private String columnFamily;
   private String columnName;

   @Override
   @SuppressWarnings("rawtypes")
   public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
      LOG.info("START prepare");

      this.collector = collector;

      hbaseClient = new HBaseSimpleClient(tableName);
      rows = new ArrayList<>();

      tupleBatch = new ArrayList<>();
   }

   @Override
   public void execute(Tuple tuple) {
      if (Utils.isTickTuple(tuple)) {
         processBatch();
      } else {
         processTuple(tuple);
      }
   }

   private void processTuple(Tuple tuple) {
      LOG.debug("START processTuple");
      try {
         String key = tuple.getString(0);
         String orig_msg = tuple.getString(1);

         Put put = hbaseClient.genPut(key, columnFamily, columnName, orig_msg);
         rows.add(put);

         tupleBatch.add(tuple);

      } catch (Exception e) {
         LOG.error("Cannot process input. Ignore it", e);
      }
   }

   private void processBatch() {
      LOG.debug("START processBatch");

      try {
         //TODO Check Result
         hbaseClient.writeBatchToTable(rows);

         for (Tuple t : tupleBatch) {
            collector.ack(t);
         }

         tupleBatch.clear();
         rows.clear();

      } catch (Exception e) {
         LOG.error("Cannot process batch. Ignore it", e);

         this.collector.reportError(e);
         for (Tuple t : tupleBatch) {
            collector.fail(t);
         }
         tupleBatch.clear();
         rows.clear();
      }
   }

   @Override
   public Map<String, Object> getComponentConfiguration() {
      if (tickTupleSec > 0) {
         Config conf = new Config();
         conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, tickTupleSec);
         return conf;
      } else {
         return super.getComponentConfiguration();
      }
   }

   @Override
   public void declareOutputFields(OutputFieldsDeclarer declarer) {}

   public HBaseBatchBolt withTickTupleSec(int tickTupleSecFrequency) {
      if (tickTupleSecFrequency < 1 || tickTupleSecFrequency > 60) {
         throw new IllegalArgumentException("Invalid tickTupleSecFrequency value=" + tickTupleSecFrequency);
      }
      this.tickTupleSec = tickTupleSecFrequency;
      return this;
   }

   public void cleanup() {
      hbaseClient.shutdown();
   }

   public HBaseBatchBolt withTable(String tableName, String cf, String column) {
      this.tableName = tableName;
      this.columnFamily = cf;
      this.columnName = column;
      return this;
   }
}
