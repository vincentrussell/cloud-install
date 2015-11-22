#!/usr/bin/env bash

bin=`dirname "${BASH_SOURCE-$0}"`
bin=`cd "$bin"; pwd`

source $bin/cloud-install-bash-include.sh



## Stop YARN daemons
# Stop the nodemanager daemon
$HADOOP_PREFIX/sbin/yarn-daemon.sh stop nodemanager

# Start the resourcemanager daemon
$HADOOP_PREFIX/sbin/yarn-daemon.sh stop resourcemanager


# Stop the datanode daemon
$HADOOP_PREFIX/sbin/hadoop-daemon.sh stop datanode
# Stop the namenode daemon
$HADOOP_PREFIX/sbin/hadoop-daemon.sh stop namenode




