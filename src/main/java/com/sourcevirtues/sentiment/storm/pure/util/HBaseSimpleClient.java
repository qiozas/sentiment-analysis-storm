package com.sourcevirtues.sentiment.storm.pure.util;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple HBase client.
 * 
 * @author Adrianos Dadis
 * 
 */
public class HBaseSimpleClient {
   private static final Logger LOG = LoggerFactory.getLogger(HBaseSimpleClient.class);

   private Table htable;
   private String tableName;

   @SuppressWarnings("unused")
   private HBaseSimpleClient() {}

   public HBaseSimpleClient(String tableName) {
      this.tableName = tableName;
      try {
         Connection con = ConnectionFactory.createConnection();
         htable = con.getTable(TableName.valueOf(tableName));

         LOG.info("Initialized for Table={}", tableName);
      } catch (IOException e) {
         throw new IllegalStateException(e);
      }
   }

   public Put genPut(String rowkey, String columnFamily, String column, String value) {
      Put put = new Put(Bytes.toBytes(rowkey));
      put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column), Bytes.toBytes(value));
      return put;
   }

   public Object[] writeBatchToTable(List<Row> batch) {
      if (batch == null || batch.isEmpty()) {
         return new Object[0];
      }

      Object[] results = new Object[batch.size()];

      try {
         htable.batch(batch, results);
      } catch (Exception e) {
         throw new IllegalStateException("Unable to execute all batch actions", e);
      }

      return results;
   }

   public void shutdown() {
      LOG.info("START shutdown: Table={}", tableName);
      if (htable != null) {
         try {
            htable.close();
         } catch (IOException e) {
            LOG.error("Error on shutdown for Table=" + tableName, e);
         }
      }
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("HBaseSimpleClient [tableName=");
      builder.append(tableName);
      builder.append("]");
      return builder.toString();
   }
}
