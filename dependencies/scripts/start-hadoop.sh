#!/usr/bin/env bash

bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin"; pwd`

source $bin/cloud-install-bash-include.sh



# Start the namenode daemon
$HADOOP_PREFIX/sbin/hadoop-daemon.sh start namenode
# Start the datanode daemon
$HADOOP_PREFIX/sbin/hadoop-daemon.sh start datanode

## Start YARN daemons
# Start the resourcemanager daemon
$HADOOP_PREFIX/sbin/yarn-daemon.sh start resourcemanager
# Start the nodemanager daemon
$HADOOP_PREFIX/sbin/yarn-daemon.sh start nodemanager