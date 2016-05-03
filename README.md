Simple Sentiment Analysis using Apache Storm
============

A very simple project that simulates how to execute sentiment analysis using [Apache Storm](https://github.com/apache/storm) 1.0.x.

Random sentences are emitted by a dummy Spout. Words of each sentence are splitted (by space) and  stemmed based on a dummy collection (could be replaced with a real DB) of words. New sentence is emitted again without the "useless" words and processed by PositiveBolt, where a positive score is calculated and emitted. New sentence is processed again by NegativeBolt and a negative score is calculated and emitted, additional to positive score. ScoreBolt compares 2 previous scores and decides if this sentence is positive or negative. Then final result (original and modified sentences and score) logged (by LoggingBolt) too and persisted to HBase or to Kafka (topic "sentimentOut").

Storm external module [Flux](https://github.com/apache/storm/tree/master/external/flux) is used to define and deploy topology in Storm.

For extra details and comment check [here](http://sourcevirtues.com/2015/12/18/real-time-sentiment-analysis-example-with-apache-storm/).

Application has been tested with:
- Apache HBase 1.1.2 and HBase provided by Cloudera 5.4.x/5.5.x
- Apache Kafka 0.9.0.1

## Prerequisites
In case you need just to LOG result, then there is no dependencies. 

If you need to persist result, then you need any of the following:

- Download [HBase 1.1.x](https://hbase.apache.org/) and extract tgz.
  - Run single node of HBase
    - ```$> cd bin```
    - ```$> ./start-hbase.sh```
  - Create required table
    - ```$> hbase shell```
    - ```hbase(main):001:0> create 'SentimentAnalysisStorm', 'cf'```
- Download [Kafka 0.9.0.x](http://kafka.apache.org) and extract tgz.
  - Start internal Zookeeper  and then Kafka Broker ([detailed steps](http://kafka.apache.org/documentation.html#quickstart_download)).
  - Create Topic
    - ```bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic sentimentOut```
  - Consume data from topic via CLI
    - ```bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic sentimentOut --from-beginning```


## Build
```mvn clean package -DskipTests```

## Run Topology
### Running in local mode with Flux

- Just LOG:
  - ```storm jar target/sentiment-analysis-storm-0.0.1-SNAPSHOT.jar org.apache.storm.flux.Flux --local -s 10000 src/test/resources/flux/topology.yaml```
- HBase persistence:
  - ```storm jar target/sentiment-analysis-storm-0.0.1-SNAPSHOT.jar org.apache.storm.flux.Flux --local -s 10000 src/test/resources/flux/topology_hbase.yaml```
- Kafka persistence:
  - ```storm jar target/sentiment-analysis-storm-0.0.1-SNAPSHOT.jar org.apache.storm.flux.Flux --local -s 10000 src/test/resources/flux/topology_kafka.yaml```

### Running in cluster mode with Flux

- Just LOG:
  - ```storm jar target/sentiment-analysis-storm-0.0.1-SNAPSHOT.jar org.apache.storm.flux.Flux --remote --c nimbus.host=localhost src/test/resources/flux/topology.yaml```
- HBase persistence:
  - ```storm jar target/sentiment-analysis-storm-0.0.1-SNAPSHOT.jar org.apache.storm.flux.Flux --remote --c nimbus.host=localhost src/test/resources/flux/topology_hbase.yaml```
- Kafka persistence:
  - ```storm jar target/sentiment-analysis-storm-0.0.1-SNAPSHOT.jar org.apache.storm.flux.Flux --remote --c nimbus.host=localhost src/test/resources/flux/topology_kafka.yaml```
