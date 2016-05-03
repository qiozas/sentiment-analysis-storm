Simple Sentiment Analysis using Apache Storm
============

A very simple project that simulates how to execute sentiment analysis using [Apache Storm](https://github.com/apache/storm) 0.10.x.

Random sentences are emitted by a dummy Spout. Then words of each sentence are splitted (by space) and  stemmed based on a dummy collection (could be replaced with a real DB) of words. New sentence is emitted again without the "useless" word and processed by PositiveBolt, where a positive score is calculated and emitted. New sentence is processed again by NegativeBolt and a negative score is calculated and emitted, additional to positive score. ScoreBolt compares 2 previous scores and decides if this sentence is positive or negative. Then final result, original and modified sentences, are persisted to HBase (by HBaseBatchBolt) and logged (by LoggingBolt) too.

Storm external module [Flux](https://github.com/apache/storm/tree/master/external/flux) is used to define and deploy topology in Storm.

For extra details and comment check [here](http://sourcevirtues.com/2015/12/18/real-time-sentiment-analysis-example-with-apache-storm/).

Application has been tested with Apache HBase 1.1.2 and Cloudera 5.4.x/5.5.x

## Usage
### Prerequisites
- Download [HBase 1.1.x](https://hbase.apache.org/).
- Extract tgz
- Run single node of HBase
  - ```$> cd bin```
  - ```$> ./start-hbase.sh```
- Create required table
  - ```$> hbase shell```
  - ```hbase(main):001:0> create 'SentimentAnalysisStorm', 'cf'```

### Build
```mvn clean package -DskipTests```

### Running in local mode

Using Flux:
- ```storm jar target/sentiment-analysis-storm-0.0.1-SNAPSHOT.jar org.apache.storm.flux.Flux --local src/test/resources/flux/topology.yaml -s 10000```

### Running in cluster mode

Using Flux:
- ```storm jar target/sentiment-analysis-storm-0.0.1-SNAPSHOT.jar org.apache.storm.flux.Flux --remote src/test/resources/flux/topology.yaml --c nimbus.host=localhost```
