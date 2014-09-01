#!/usr/bin/env bash

bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin"; pwd`

source $bin/cloud-install-bash-include.sh

$HADOOP_HOME/sbin/stop-dfs.sh

sleep 10

$HADOOP_HOME/bin-mapreduce1/stop-mapred.sh
