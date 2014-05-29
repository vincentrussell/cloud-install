#!/bin/sh

bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin"; pwd`

source $bin/cloud-install-bash-include.sh


$HADOOP_HOME/sbin/start-dfs.sh

sleep 10

$HADOOP_HOME/bin-mapreduce1/start-mapred.sh


